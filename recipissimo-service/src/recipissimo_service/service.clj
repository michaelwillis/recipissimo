(ns recipissimo-service.service
  (:require [datomic.api :as datomic]
            [clj-time.core :refer [plus days year month day]]
            [clj-time.local :refer [local-now]]
            [clj-time.format :refer [formatter-local]]
            [io.pedestal.service.http :as bootstrap]
            [io.pedestal.service.http.route :as route]
            [io.pedestal.service.http.sse :as sse]
            [io.pedestal.service.http.body-params :as body-params]
            [io.pedestal.service.http.ring-middlewares :as middlewares]
            [io.pedestal.service.http.route.definition :refer [defroutes]]
            [io.pedestal.service.interceptor :refer [definterceptor]]
            [io.pedestal.service.log :as log]
            [ring.middleware.session.cookie :as cookie]
            [ring.util.response :as ring-resp]))

(defn home-page
  [request]
  (ring-resp/response "Recipissimo Service"))

(def ^:private streaming-contexts (atom {}))

(def ^:private db-conn (atom nil))

(defn db []
  (when (nil? @db-conn)
    (reset! db-conn (datomic/connect "datomic:free://localhost:4334/recipissimo")))
  (datomic/db @db-conn))

(defn- session-from-context
  "Extract the session id from the streaming context."
  [streaming-context]
  (get-in streaming-context [:request :cookies "client-id" :value]))

(defn- session-from-request
  "Extract the session id from a request."
  [request]
  (get-in request [:cookies "client-id" :value]))

(defn- clean-up
  "Remove the given streaming context and shutdown the event stream."
  [streaming-context]
  (swap! streaming-contexts dissoc (session-from-context streaming-context))
  (sse/end-event-stream streaming-context))

(defn- notify
  "Send event-data to the connected client."
  [session-id event-name event-data]
  (when-let [streaming-context (get @streaming-contexts session-id)]
    (try
      (sse/send-event streaming-context event-name (pr-str event-data))
      (catch java.io.IOException ioe
        (clean-up streaming-context)))))

(defn- notify-all
  "Send event-data to all connected channels."
  [event-name event-data]
  (doseq [session-id (keys @streaming-contexts)]
    (notify session-id event-name event-data)))

(defn- notify-all-others
  "Send event-data to all connected channels except for the given session-id."
  [sending-session-id event-name event-data]
  (doseq [session-id (keys @streaming-contexts)]
    (when (not= session-id sending-session-id)
      (notify session-id event-name event-data))))

(defn- store-streaming-context [streaming-context]
  (let [session-id (session-from-context streaming-context)]
    (swap! streaming-contexts assoc session-id streaming-context)
    (notify-all "sessionids" (keys @streaming-contexts))))

(defn- session-id [] (.toString (java.util.UUID/randomUUID)))

(defn db-result-to-recipe [[id name url]]
  {:id id :name name :url (str url)})

(def handlers
  {:search
   (fn [msg-data session-id]
     (let [query (concat '[:find ?recipe ?name ?url
                           :where [?recipe :recipe/name ?name]
                           [?recipe :recipe/url ?url]]
                         (map (fn [term] `[(~'fulltext ~'$ ~':recipe/name ~term)
                                          [[~'?recipe ~'?name]]])
                              (:search-terms msg-data)))
           results (->> (datomic/q query (db))
                        (map db-result-to-recipe)
                        (take 15)
                        vec)]
       (notify-all "msg" {:type :search-results :value results})))
   :next-n-days
   (fn [msg-data session-id]
     (let [now (local-now)
           dates (reduce (fn [calendar delta]
                           (let [date (plus now (days delta))
                                 formatted-date (.print (formatter-local "E, d MMM y") date)
                                 [y m d :as ymd] ((juxt year month day) date)
                                 recipes (->> (datomic/q '[:find ?recipe ?name ?url
                                                           :in $ ?year ?month ?date
                                                           :where
                                                           [?recipe :recipe/name ?name]
                                                           [?recipe :recipe/url ?url]
                                                           [?menu :menu/recipes ?recipe]
                                                           [?menu :menu/year ?year]
                                                           [?menu :menu/month ?month]
                                                           [?menu :menu/date ?date]]
                                                         (db) y m d)
                                              (map db-result-to-recipe)
                                              vec)]
                             (assoc calendar ymd [formatted-date recipes])))
                         {} (range (:n-days msg-data)))]
       (notify-all "msg" {:type :next-n-days :value dates})))

   :plan-meal
   (fn [msg-data session-id]
     (let [rid (:rid msg-data)
           year (:year msg-data)
           month (:month msg-data)
           date (:date msg-data)
           recipe (->> (datomic/q '[:find ?recipe ?name ?url
                                    :in $ ?recipe
                                    :where
                                    [?recipe :recipe/name ?name]
                                    [?recipe :recipe/url ?url]]
                                  (db) rid)
                       (map db-result-to-recipe)
                       first)
           tx [{:db/id (datomic/tempid :db.part/user)
                :menu/year year
                :menu/month month
                :menu/date date
                :menu/recipes rid}]]
       (datomic/transact @db-conn tx)
       (notify-all "msg" {:type :meal-planned :recipe recipe :year year :month month :date date})))

   :unplan-meal
   (fn [msg-data session-id]
     (let [rid (:rid msg-data)
           year (:year msg-data)
           month (:month msg-data)
           date (:date msg-data)
           query '[:find ?menu
                   :in $ ?rid ?year ?month ?date
                   :where
                   [?menu :menu/year ?year]
                   [?menu :menu/month ?month]
                   [?menu :menu/date ?date]
                   [?menu :menu/recipes ?rid]]
           planned-recipes (datomic/q query (db) rid year month date)]
       (notify-all "boomboomboom" {:planned-recipes planned-recipes})
       (doseq [[planned-recipe] planned-recipes]
         (do
           (notify-all "boomboom" {:planned-recipe planned-recipe})
           (datomic/transact @db-conn [[:db.fn/retractEntity planned-recipe]])))
       (notify-all "msg" {:type :meal-unplanned :rid rid :year year :month month :date date})))})

(declare url-for)

(defn subscribe
  "Assign a session cookie to this request if one does not
  exist. Redirect to the events channel."
  [request]
  (notify-all "new-subscriber" {:request request})
  (let [session-id (or (session-from-request request)
                       (session-id))
        cookie {:client-id {:value session-id :path "/"}}]
    (notify-all "vars" { :session-id session-id :cookie cookie})
    (-> (ring-resp/redirect (url-for ::events))
        (update-in [:cookies] merge cookie))))

(definterceptor session-interceptor
  (middlewares/session {:store (cookie/cookie-store)}))

(defn publish
  "Publish a message to all other connected clients."
  [{msg-data :edn-params :as request}]
  (log/info :message "received message"
            :request request
            :msg-data msg-data)
  (let [session-id (or (session-from-request request)
                       (session-id))
        handler (handlers (:type msg-data))]
    (notify-all "effect" { :session-id session-id :msg msg-data})
    (handler msg-data session-id))
  (ring-resp/response ""))

(defroutes routes
  [[["/" {:get home-page}
     ;; Set default interceptors for /about and any other paths under /
     ^:interceptors [(body-params/body-params) bootstrap/html-body session-interceptor]
     ["/msgs" {:get subscribe :post publish}
      ["/events" {:get [::events (sse/start-event-stream store-streaming-context)]}]]]]])

;; You can use this fn or a per-request fn via io.pedestal.service.http.route/url-for
(def url-for (route/url-for-routes routes))

;; Consumed by recipissimo-service.server/create-server
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::boostrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"

              ;; Either :jetty or :tomcat (see comments in project.clj
              ;; to enable Tomcat)
              ;;::bootstrap/host "localhost"
              ::bootstrap/type :jetty
              ::bootstrap/port 8080})

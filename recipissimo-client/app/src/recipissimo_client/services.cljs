(ns recipissimo-client.services
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app.messages :as msg]
            [cljs.reader :as reader]))

(defn single-value-swap-handler [path]
  (fn [message] {msg/type :swap msg/topic path :value (:value message)}))

(defn handle-meal-planned [{:keys [recipe year month date]}]
  {msg/type :meal-planned msg/topic [:planner :calendar]
   :value {:recipe recipe :year year :month month :date date}})

(defn handle-meal-unplanned [{:keys [rid year month date]}]
  {msg/type :meal-unplanned msg/topic [:planner :calendar]
   :value {:rid rid :year year :month month :date date}})

(defn handle-new-category [{:keys [name]}]
  {msg/type :new-category msg/topic [:shopping-list] :name name})

(defn handle-category-deleted [{:keys [name]}]
  {msg/type :category-deleted msg/topic [:shopping-list] :name name})

(def handlers
  {:next-n-days (single-value-swap-handler [:planner :calendar])
   :search-results (single-value-swap-handler [:planner :search])
   :meal-planned handle-meal-planned
   :meal-unplanned handle-meal-unplanned
   :shopping-list (single-value-swap-handler [:shopping-list :ingredients])
   :new-category handle-new-category
   :category-deleted handle-category-deleted
   })

(defn receive-ss-event [app e]
  (let [message (reader/read-string (.-data e))
        handler (handlers (message :type))]
    (p/put-message (:input app) (handler message))))

(defrecord Services [app]
  p/Activity
  (start [this]
    (let [source (js/EventSource. "/msgs")]
      (.addEventListener source "msg" (fn [e]
                                        (receive-ss-event app e)) false)))
  (stop [this]))

(defn services-fn [message input-queue]
  (let [body (pr-str message)]
    (let [http (js/XMLHttpRequest.)]
      (.open http "POST" "/msgs" true)
      (.setRequestHeader http "Content-type" "application/edn")
      (.send http body))))

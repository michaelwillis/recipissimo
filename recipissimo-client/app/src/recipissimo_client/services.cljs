(ns recipissimo-client.services
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app.messages :as msg]
            [cljs.reader :as reader]))

(defn update-search-results [app message]
  (p/put-message (:input app) {msg/type :update msg/topic [:planner :search]
                               :results (:search-results message)}))

(def handlers
  {:search-results update-search-results})

(defn receive-ss-event [app e]
  (let [message (reader/read-string (.-data e))
        handler (handlers (message :type))]
    (handler app message)))

(defrecord Services [app]
  p/Activity
  (start [this]
    (let [source (js/EventSource. "/msgs")]
      (.addEventListener source "msg" (fn [e] (receive-ss-event app e)) false)))
  (stop [this]))

(defn services-fn [message input-queue]
  (let [body (pr-str message)]
    (let [http (js/XMLHttpRequest.)]
      (.open http "POST" "/msgs" true)
      (.setRequestHeader http "Content-type" "application/edn")
      (.send http body))))


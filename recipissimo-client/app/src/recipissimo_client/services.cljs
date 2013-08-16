(ns recipissimo-client.services
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app.messages :as msg]
            [cljs.reader :as reader]))

(def paths
  {:next-n-days [:planner :dates]
   :search-results [:planner :search]})

(defn receive-ss-event [app e]
  (let [message (reader/read-string (.-data e))
        path (paths (message :type))]
    (p/put-message (:input app) {msg/type :swap
                                 msg/topic path
                                 :value (:value message)})))

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

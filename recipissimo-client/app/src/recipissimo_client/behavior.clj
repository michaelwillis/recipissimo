(ns ^:shared recipissimo-client.behavior
    (:require [io.pedestal.app :as app]
              [io.pedestal.app.messages :as msg]))

(defn calendar-init [_ _] {:month "July"})

(defn week-init [_ _] {}) 

(defn day-init [_ _] "")

(defn search-init [_ _] {})

(defn update-search-results [_ message] (:results message))

(def recipissimo-app
  {:version 2
   :transform [[:init [:calendar] calendar-init]
               [:init [:calendar :weeks :*] week-init]
               [:init [:calendar :weeks :* :*] day-init]
               [:init [:search] search-init]
               [:update [:search :results] update-search-results]]
   :emit [[#{[:calendar] [:calendar :weeks] [:calendar :weeks :* :*]
             [:search] [:search :results]} (app/default-emitter [])]]
})


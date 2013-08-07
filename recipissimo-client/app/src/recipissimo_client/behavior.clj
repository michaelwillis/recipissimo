(ns ^:shared recipissimo-client.behavior
    (:require [io.pedestal.app :as app]
              [io.pedestal.app.messages :as msg]))

(def meals
  [{:id 1234567890
    :name "Rhubarb Cobbler"
    :url "http://thepioneerwoman.com/cooking/2011/06/rhubarb-cobbler/"}
   {:id 1234123454
    :name "French Bread Pizzas"
    :url "http://thepioneerwoman.com/cooking/2013/07/french-bread-pizzas/"}
   {:id 3245623456
    :name "Skillet Chicken Lasagna"
    :url "http://thepioneerwoman.com/cooking/2013/07/skillet-chicken-lasagna/"}])

(defn calendar-init [_ _] {:month "July"})

(defn week-init [_ _] {}) 

(defn day-init [_ _] "")

(defn search-init [_ _] {:boomboom 42})

(defn search-results-init [_ _] meals)

(def recipissimo-app
  {:version 2
   :transform [[:init [:calendar] calendar-init]
               [:init [:calendar :weeks :*] week-init]
               [:init [:calendar :weeks :* :*] day-init]
               [:init [:search] search-init]
               [:init [:search :results] search-results-init]]
   :emit [[#{[:calendar] [:calendar :weeks] [:calendar :weeks :* :*]
             [:search] [:search :results]} (app/default-emitter [])]]
})


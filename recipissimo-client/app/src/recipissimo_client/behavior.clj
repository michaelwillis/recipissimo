(ns ^:shared recipissimo-client.behavior
    (:require [io.pedestal.app :as app]
              [io.pedestal.app.messages :as msg]))

(def meals
  ["Fajitas"
   "Lasagna"
   "Saag Paneer"
   "Lamb Gyros"
   "Teryaki"
   "Pho"
   "Tempura"
   "Burritos"
   "Sloppy Joes"])

(comment (defn random-meals []
   (repeatedly )))

(defn calendar-init [_ _] {:month "September"})

(defn week-init [_ _] {}) 

(defn day-init [_ _] (rand-nth meals))

(def recipissimo-app
  {:version 2
   :transform [[:init [:calendar] calendar-init]
               [:init [:calendar :weeks :*] week-init]
               [:init [:calendar :weeks :* :*] day-init]]
   :emit [[#{[:calendar]
             [:calendar :weeks]
             [:calendar :weeks :* :*]} (app/default-emitter [])]]
})


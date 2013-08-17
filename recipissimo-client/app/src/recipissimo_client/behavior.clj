(ns ^:shared recipissimo-client.behavior
    (:require [io.pedestal.app :as app]
              [io.pedestal.app.messages :as msg]))

(defn init-planner [_]
  [[:node-create [:planner] :map]])

(defn swap-transform [_ message] (:value message))

(defn meal-planned [old-value message]
  (let [ymd ((juxt :year :month :date) (:value message))
        recipe (get-in message [:value :recipe])]
    (if (contains? old-value ymd)
      (update-in old-value [ymd]
                 (fn [[date-text recipes]]
                   [date-text (conj recipes recipe)]))
      old-value)))

(defn publish-search-terms [[search-terms]]
  (when search-terms
    [{:type :search :search-terms search-terms}]))

(defn publish-next-n-days-request [[days]]
  (when days
    [{:type :next-n-days :n-days days}]))

(defn publish-plan-meal [[planned-meal]]
  (when planned-meal
    [(merge {:type :plan-meal} planned-meal)]))

(def recipissimo-app
  {:version 2
   :transform [[:init [:planner] planner-init]
               [:swap [:planner :*] swap-transform]
               [:meal-planned [:planner :calendar] meal-planned]
               ]
   :effect #{[#{[:planner :search-terms]} publish-search-terms :vals]
             [#{[:planner :next-n-days]} publish-next-n-days-request :vals]
             [#{[:planner :plan-meal]} publish-plan-meal :vals]}
   :emit [{:init init-planner}
          [#{[:planner] [:planner :*]}
           (app/default-emitter [])]
          ]})


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
      (update-in old-value [ymd 1] #(conj % recipe))
      old-value)))

(defn meal-unplanned [old-value message]
  (let [ymd ((juxt :year :month :date) (:value message))]
    (if (contains? old-value ymd)
      (update-in old-value [ymd 1]
                 (fn [recipes]
                   (->> recipes (filter #(not= (:id val) (-> message :value :rid))))))
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

(defn publish-unplan-meal [[unplanned-meal]]
  (when unplanned-meal
    [(merge {:type :unplan-meal} unplanned-meal)]))

(def recipissimo-app
  {:version 2
   :transform [[:init [:planner] planner-init]
               [:swap [:planner :*] swap-transform]
               [:meal-planned [:planner :calendar] meal-planned]
               [:meal-unplanned [:planner :calendar] meal-unplanned]]
   :effect #{[#{[:planner :search-terms]} publish-search-terms :vals]
             [#{[:planner :next-n-days]} publish-next-n-days-request :vals]
             [#{[:planner :plan-meal]} publish-plan-meal :vals]
             [#{[:planner :unplan-meal]} publish-unplan-meal :vals]}
   
   :emit [{:init init-planner}
          [#{[:planner] [:planner :*]}
           (app/default-emitter [])]
          ]})


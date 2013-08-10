(ns ^:shared recipissimo-client.behavior
    (:require [io.pedestal.app :as app]
              [io.pedestal.app.messages :as msg]))

(defn init-planner [_]
  [[:node-create [:planner] :map]])

(defn swap-transform [_ message] (:value message))

(defn publish-search-terms [[search-terms]]
  (when search-terms
    [{:type :search :search-terms search-terms}]))

(defn publish-next-n-days-request [[days]]
  (when days
    [{:type :next-n-days :n-days days}]))

(def recipissimo-app
  {:version 2
   :transform [[:init [:planner] planner-init]
               [:swap [:planner :*] swap-transform]]
   
   :effect #{[#{[:planner :search-terms]} publish-search-terms :vals]
             [#{[:planner :next-n-days]} publish-next-n-days-request :vals]}
   :emit [{:init init-planner}
          [#{[:planner :search] [:planner :dates] [:planner] [:planner :next-n-days]}
           (app/default-emitter [])]
          ]})


(ns ^:shared recipissimo-client.behavior
    (:require [io.pedestal.app :as app]
              [io.pedestal.app.messages :as msg]))

(defn init-planner [_]
  [[:node-create [:planner] :map]])

(defn update-search-terms [_ message]
  (:search-terms message))

(defn update-search-results [_ message]
  (:results message))

(defn publish-search-terms [[search-terms]]
    [{:type :search :search-terms search-terms}])

(def recipissimo-app
  {:version 2
   :transform [[:init [:planner] planner-init]
               [:update [:planner :search-terms] update-search-terms]
               [:update [:planner :search] update-search-results]]
   :effect #{[#{[:planner :search-terms]} publish-search-terms :vals]}
   :emit [{:init init-planner}
          [#{[:planner :search]} (app/default-emitter [])]]})


(ns recipissimo-client.rendering
  (:require [domina :as dom]
            [io.pedestal.app.messages :as msg]
            [io.pedestal.app.protocols :as p]
            [io.pedestal.app.render.push :as render]
            [io.pedestal.app.render.push.templates :as templates]
            [io.pedestal.app.render.push.handlers :as h]
            [io.pedestal.app.render.push.handlers.automatic :as d]
            [clojure.string :as string])
  (:require-macros [recipissimo-client.html-templates :as html-templates]))

(def templates (identity (html-templates/recipissimo-client-templates)))

(defn update-search-results [renderer [_ path _ new-value] input-queue]
  (js/clearSearchResults)
  (doseq [{:keys [ id name url]} new-value]
    (js/addSearchResult id name url)))

(defn render-planner [renderer [_ path _] input-queue]
  (let [template (templates :planner)]
    (dom/append! (dom/by-id "content") (template {})))
  (js/initSearchBox (fn [val]
                      (let [search-terms (string/split val #"\s+")
                            message {msg/type :update
                                     msg/topic [:planner :search-terms]
                                     :search-terms search-terms}]

                        (p/put-message input-queue message)))))

(defn render-config [] 
  [[:node-create [:planner] render-planner]
   [:value [:planner :search] update-search-results]])

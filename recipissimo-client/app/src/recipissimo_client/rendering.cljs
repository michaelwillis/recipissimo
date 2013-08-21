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

(defn swap [input-queue topic value]
  (p/put-message input-queue {msg/type :swap msg/topic topic :value value}))

(defn render-planner [renderer [_ path _] input-queue]
  (dom/append! (dom/by-id "content") ((templates :planner) {}))
  (js/setTimeout #(swap input-queue [:planner :next-n-days] 14) 100)
  (js/initSearchBox #(swap input-queue [:planner :search-terms] (string/split % #"\s+")))
  (js/initCreateShoppingListButton
   #(p/put-message input-queue {msg/topic msg/app-model
                                msg/type :set-focus
                                :name :shopping-list})))

(defn remove-planner [renderer [_ path _] input-queue]
  (dom/destroy! (dom/by-id "planner")))

(defn render-calendar [renderer [_ path _ new-value] input-queue]
  (js/clearCalendar)
  (doseq [row (->> new-value keys sort (partition 7) (apply (partial map list)))]
    (let [tr (js/createCalendarRow)]
      (doseq [date row]
        (let [[y m d] date
              [text recipes] (new-value date)
              callback #(swap input-queue
                              [:planner :plan-meal]
                              {:rid % :year y :month m :date d})
              ul (js/createCalendarDay tr text callback)]
          (doseq [{:keys [id name url]} recipes]
            (let [delete #(swap input-queue
                                [:planner :unplan-meal]
                                {:rid id :year y :month m :date d})]
              (js/addRecipeToCalendar ul name url delete))))))))

(defn render-search-results [renderer [_ path _ new-value] input-queue]
  (js/clearSearchResults)
  (doseq [{:keys [ id name url]} new-value]
    (js/addSearchResult id name url)))

(defn render-config [] 
  [[:node-create [:planner] render-planner]
   [:node-destroy [:planner] remove-planner]
   [:value [:planner :calendar] render-calendar]
   [:value [:planner :search] render-search-results]])

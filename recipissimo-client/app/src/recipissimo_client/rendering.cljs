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

(defn render-planner [renderer [_ path _] input-queue]
  (let [template (templates :planner)
        swap (fn [t v] (p/put-message input-queue {msg/type :swap msg/topic t :value v}))]
    (dom/append! (dom/by-id "content") (template {}))
    (js/setTimeout (fn [] (swap [:planner :next-n-days] 14)) 1000)
    (js/initSearchBox
     (fn [value] (swap [:planner :search-terms] (string/split value #"\s+"))))))

(defn update-search-results [renderer [_ path _ new-value] input-queue]
  (js/clearSearchResults)
  (doseq [{:keys [ id name url]} new-value]
    (js/addSearchResult id name url)))

(defn render-calendar [renderer [_ path _ new-value] input-queue]
  (doseq [row (->> new-value keys sort (partition 7) (apply (partial map list)))]
    (let [tr (js/createCalendarRow)]
      (doseq [date row]
        (let [[y m d] date
              [text recipes] (new-value date)
              callback (fn [rid] (js/alert (str "Added recipe " rid " to " y " " m " " d)))
              ul (js/createCalendarDay tr text callback)]
          (doseq [{:keys [id name url]} recipes]
            (js/addRecipeToCalendar ul id name url)))))))

(defn render-config [] 
  [[:node-create [:planner] render-planner]
   [:value [:planner :dates] render-calendar]
   [:value [:planner :search] update-search-results]])

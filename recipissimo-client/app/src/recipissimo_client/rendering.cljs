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

(defn render-planner [_ _ input-queue]
  (dom/append! (dom/by-id "content") ((templates :planner) {}))
  (js/setTimeout #(swap input-queue [:planner :next-n-days] 14) 100)
  (js/initSearchBox #(swap input-queue [:planner :search-terms] (string/split % #"\s+"))))

(defn remove-planner [_ _ _]
  (dom/destroy! (dom/by-id "planner")))

(defn render-calendar [_ [_ _ _ new-value] input-queue]
  (js/clearCalendar)
  (js/initCreateShoppingListButton
   (fn []
     (doseq [msg [{msg/topic msg/app-model msg/type :set-focus :name :shopping-list}
                   {msg/topic [:shopping-list] msg/type :init :dates (keys new-value)}]]
        (p/put-message input-queue msg))))
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

(defn render-search-results [_ [_ _ _ new-value] _]
  (js/clearSearchResults)
  (doseq [{:keys [ id name url]} new-value]
    (js/addSearchResult id name url)))

(defn render-shopping-list [_ _ input-queue]
  (let [content-node (dom/by-id "content")
        template (templates :shopping-list)]
    (dom/append! content-node (template {}))
    (js/initShoppingList
     (fn [new-category-name]
       (when (and (seq new-category-name)
                  (not= new-category-name "other"))
         (swap input-queue [:shopping-list :new-category] new-category-name))))))

(defn render-ingredients [_ [_ _ _ categorized-ingredients] input-queue]
  (js/clearIngredients)
  (doseq [[category ingredients] categorized-ingredients]
    (let [drop-ingredient #(swap input-queue [:shopping-list :update-category]
                                 {:category category :ingredient %})
          delete #(swap input-queue [:shopping-list :delete-category] category)
          category-ul (js/renderCategory category drop-ingredient delete)]
      (doseq [{:keys [name raw-text]} ingredients]
        (js/addIngredientToCategory category-ul name raw-text)))))

(defn render-config [] 
  [[:node-create [:planner] render-planner]
   [:node-destroy [:planner] remove-planner]
   [:value [:planner :calendar] render-calendar]
   [:value [:planner :search] render-search-results]
   [:node-create [:shopping-list] render-shopping-list]
   [:value [:shopping-list :ingredients] render-ingredients]])

(ns ^:shared recipissimo-client.behavior
    (:require [io.pedestal.app :as app]
              [io.pedestal.app.messages :as msg]))

(defn init-planner [_]
  [[:node-create [:planner] :map]])

(defn swap-transform [old-value message] (:value message))

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
                   (->> recipes (filter #(not= (:id %) (-> message :value :rid))))))
      old-value)))

(defn init-shopping-list [_ message]
  (select-keys message '(:dates)))

(defn new-category [shopping-list {:keys [name]}]
  (assoc-in shopping-list [:ingredients name] []))

(defn category-deleted [shopping-list {:keys [name]}]
  (let [ingredients (get-in shopping-list [:ingredients name])
        other (-> (get-in shopping-list :ingredients other)
                  (concat ingredients) vec)]
    (-> shopping-list
        (update-in [:ingredients "other"] concat ingredients)
        (update-in [:ingredients] dissoc name))))

(defn category-updated [shopping-list {:keys [ingredient category]}]
  (let [ingredients (mapcat (fn [[_ category-ingredients]]
                              (filter #(= (% :name) ingredient) category-ingredients))
                            (shopping-list :ingredients))]
    (reduce (fn [shopping-list ingredient]
              (-> shopping-list
                  ;; remove from prior category
                  (update-in [:ingredients (ingredient :category)]
                             (partial filter (fn [i] (not= i ingredient))))
                  (update-in [:ingredients category] conj ingredient)))
            shopping-list ingredients)))

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

(defn publish-shopping-list-dates [[dates]]
  (when dates
    [{:type :shopping-list :dates dates}]))

(defn publish-new-category [[new-category-name]]
  (when new-category-name
    [{:type :new-category :name new-category-name}]))

(defn publish-delete-category [[deleted-category-name]]
  (when deleted-category-name
    [{:type :delete-category :name deleted-category-name}]))

(defn publish-update-category [[{:keys [category ingredient]}]]
  (when (and category ingredient)
    [{:type :update-category :category category :ingredient ingredient}]))

(def recipissimo-app
  {:version 2
   :transform [[:swap [:planner :*] swap-transform]
               [:swap [:shopping-list :*] swap-transform]
               [:init [:shopping-list] init-shopping-list]
               [:meal-planned [:planner :calendar] meal-planned]
               [:meal-unplanned [:planner :calendar] meal-unplanned]
               [:new-category [:shopping-list] new-category]
               [:category-deleted [:shopping-list] category-deleted]
               [:category-updated [:shopping-list] category-updated]
               ]
   :effect #{[#{[:planner :search-terms]} publish-search-terms :vals]
             [#{[:planner :next-n-days]} publish-next-n-days-request :vals]
             [#{[:planner :plan-meal]} publish-plan-meal :vals]
             [#{[:planner :unplan-meal]} publish-unplan-meal :vals]
             [#{[:shopping-list :dates]} publish-shopping-list-dates :vals]
             [#{[:shopping-list :new-category]} publish-new-category :vals]
             [#{[:shopping-list :delete-category]} publish-delete-category :vals]
             [#{[:shopping-list :update-category]} publish-update-category :vals]
             }
   :emit [{:init init-planner}
          [#{[:planner] [:planner :*] [:shopping-list :*]}
           (app/default-emitter [])]]
   :focus {:planner [[:planner]]
           :shopping-list [[:shopping-list]]
           :default :planner}})


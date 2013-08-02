(ns recipissimo-client.rendering
  (:require [domina :as dom]
            [io.pedestal.app.render.push :as render]
            [io.pedestal.app.render.push.templates :as templates]
            [io.pedestal.app.render.push.handlers :as h]
            [io.pedestal.app.render.push.handlers.automatic :as d])
  (:require-macros [recipissimo-client.html-templates :as html-templates]))

(def templates (html-templates/recipissimo-client-templates))

(defn default-dom-selector-fn [renderer path]
  (dom/by-id (render/get-parent-id renderer path)))

(defn render-template
  [template & {:keys [dom-selector-fn initial-value-fn]
               :or {dom-selector-fn default-dom-selector-fn
                    initial-value-fn (fn [] {})}}]
  (fn [renderer [_ path :as delta] input-queue]
    (let [id (render/new-id! renderer path)
          html (templates/add-template renderer path (templates template))]
      (dom/append! (dom-selector-fn renderer path)
                   (html (assoc (initial-value-fn delta) :id id))))))

(defn update-calendar [renderer [_ path _ new-value] input-queue]
  (templates/update-t renderer path {:month (:month new-value)}))

(def render-week
  (render-template :week
                   :dom-selector-fn (fn [_ _] (dom/by-id "calendar-table-body"))))

(def render-day
  (render-template :day
                   :initial-value-fn (fn [[_ path]] {:date (last path)})))

(defn render-config [] 
  [[:node-create [:calendar] (render-template :calendar)]
   [:value [:calendar] update-calendar]
   [:node-create [:calendar :weeks :*] render-week]
   [:node-create [:calendar :weeks :* :*] render-day]
   (comment 
           [:node-destroy [:main] d/default-destroy]
           [:node-create [:main :calendar] ]
           [:value [:main :calendar] (render-template :calendar (fn [] {}) )])])

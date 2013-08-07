(ns recipissimo-client.start
  (:require [io.pedestal.app.protocols :as p]
            [io.pedestal.app :as app]
            [io.pedestal.app.render.push :as push-render]
            [io.pedestal.app.render :as render]
            [io.pedestal.app.messages :as msg]
            [recipissimo-client.behavior :as behavior]
            [recipissimo-client.rendering :as rendering]))

(defn create-app [render-config]
  (let [app (app/build behavior/recipissimo-app)
        render-fn (push-render/renderer "content" render-config render/log-fn)
        app-model (render/consume-app-model app render-fn)
        weeks (partition 7 (concat [30] (range 1 32) (range 1 4)))]
    (app/begin app)
    (p/put-message (:input app) {msg/type :init msg/topic [:search :results]})
    (p/put-message (:input app) {msg/type :init msg/topic [:calendar]})
    (p/put-message (:input app) {msg/type :init msg/topic [:calendar :weeks]})
    (doseq [week (map-indexed #(vector %1 %2) weeks)]
      (p/put-message (:input app) {msg/type :init msg/topic [:calendar :weeks (first week)]})
      (doseq [day (second week)]
        (p/put-message (:input app) {msg/type :init msg/topic [:calendar :weeks (first week) day]})))
    {:app app :app-model app-model}))

(defn ^:export main []
  (create-app (rendering/render-config)))

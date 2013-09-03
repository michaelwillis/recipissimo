(ns recipissimo-client.html-templates
  (:use [io.pedestal.app.templates :only [tfn dtfn tnodes]]))

(defmacro recipissimo-client-templates []
  {:planner (tfn (tnodes "planner.html" "planner" [[:ul :td]]))
   :shopping-list (tfn (tnodes "shopping_list.html" "shopping-list" [[:#categories]]))})

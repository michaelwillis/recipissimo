(ns recipissimo-client.html-templates
  (:use [io.pedestal.app.templates :only [tfn dtfn tnodes]]))

(defmacro recipissimo-client-templates []
  {:calendar (dtfn (tnodes "recipissimo-client.html" "calendar" [[:tbody]]) #{:id})
   :week (dtfn (tnodes "recipissimo-client.html" "week" [[:tr]]) #{:id})
   :day (dtfn (tnodes "recipissimo-client.html" "day" [[:span]]) #{:id})})

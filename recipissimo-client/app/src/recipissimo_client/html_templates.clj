(ns recipissimo-client.html-templates
  (:use [io.pedestal.app.templates :only [tfn dtfn tnodes]]))

(defmacro recipissimo-client-templates
  []
  {:recipissimo-client-page (dtfn (tnodes "recipissimo-client.html" "hello") #{:id})})

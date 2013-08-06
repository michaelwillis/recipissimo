(defproject recipissimo-importer "0.1.0-SNAPSHOT"
  :description "Imports data from Open Recipes into Recipissimo's Datomic DB"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [cheshire "5.2.0"]]
  :profiles {:dev
             {:plugins [[lein-midje "3.1.0"]]
              :dependencies [[midje "1.5.1"]]}})


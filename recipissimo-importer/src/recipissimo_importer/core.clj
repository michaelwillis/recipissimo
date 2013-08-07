(ns recipissimo-importer.core
  (:require [cheshire.core :as json]
            [clojure.string :as string]
            [datomic.api :as datomic]))

(defn remove-parenthetic-phrases [text]
  (string/replace text #"\(.+?\)" ""))

(defn remove-comma-and-following-text [text]
  (first (string/split text #",")))

(defn remove-ellipsis-and-following-text [text]
  (first (string/split text #"\.\.")))

(defn split-on-whitespace [text]
  (string/split text #"\s+"))

(def measurements
  (set (->> ["one" "two" "three" "couple" "few" "lot"
             "t" "ts" "tsp" "tspn" "teaspoon"
             "tb" "tbs" "tbsp" "tblsp" "tblspn" "tablespoon"
             "c" "cup" "p" "pint" "gal" "gallon"
             "oz" "ounce" "lb" "pound"
             "ml" "milliliter" "millilitre" "l" "liter" "litre"
             "g" "gr" "gram" "kg" "kilogram"
             "handful" "pinch" "bunch" "dash"
             "bag" "bowl" "dollop" "pack" "package" "pouch" "sack" "spoonful" "x"]
            (mapcat (fn [name] (map #(str name %) ["" "." "s" "es" "'s"]))))))

(def numeric #"[\d¼½¾⅓⅔⅕⅖⅗⅘⅙⅚⅛⅜⅝⅞]+")

(def numeric? (partial re-find numeric))

(defn ingredient? [line]
  (let [words (split-on-whitespace line)]
    (or (some measurements words)
        (some numeric? words)
        (some #{"whole" "half" "third" "quarter" "fifth"} words)
        (some #{"of" "for" "or"} (rest words)))))

(defn remove-measurements [words]
  (->> words (partition-by measurements) last))

(defn remove-from-beginning [predicate words]
  (if (predicate (first words))
    (recur predicate (rest words))
    words))

(def rubbish
  #{"a" "an" "of" "or" "more" "for" "-" "*" "good" "great" "great-tasting"})

(defn parse-ingredient [ingredient]
  (let [ingredient (->> ingredient string/lower-case)]
    (when (ingredient? ingredient)
      (let [words (->> ingredient
                       remove-parenthetic-phrases
                       remove-comma-and-following-text
                       remove-ellipsis-and-following-text
                       split-on-whitespace
                       remove-measurements
                       (remove-from-beginning numeric?)
                       (remove-from-beginning rubbish))]
        (when (not-empty words) (string/join " " words))))))

(defn parse-recipe [recipe]
  (let [parsed (json/parse-string recipe)]
    {:name (parsed "name")
     :description (parsed "description")
     :image (parsed "image")
     :url (parsed "url")
     :ingredients (vec (->> (parsed "ingredients")
                            (string/split-lines)
                            (map string/trim)
                            (map (fn [i] [i (parse-ingredient i)]))
                            (filter (comp not nil? second))))}))

(defn parse-recipe-file
  "Parses each line in the given file as a recipe, passing each recipe to the handler function"
  [filename handler]
  (with-open [reader (clojure.java.io/reader filename)]
    (doseq [recipe-json (line-seq reader)]
      (let [recipe (parse-recipe recipe-json)]
        (when (not-any? nil? (vals recipe))
          (handler recipe))))))

(defn init-db
  "Creates a datomic database and sets up the schema"
  [uri]
  (when (datomic/create-database uri)
    (let [conn (datomic/connect uri)]
      @(datomic/transact conn (read-string (slurp "resources/schema.edn"))))))

(defn import-recipe-file-to-db
  "Parses the file and writes the results to datomic"
  [filename datomic-uri]
  (let [conn (datomic/connect datomic-uri)]
    (parse-recipe-file filename
     (fn [recipe]
       (let [tx (map (fn [[raw-text name]]
                       (let [id (datomic/tempid :db.part/user)]
                         {:db/id id
                          :ingredient/name name
                          :ingredient/raw-text raw-text}))
                     (:ingredients recipe))
             ingredient-ids (set (map :db/id tx))
             tx (concat tx [{:db/id (datomic/tempid :db.part/user)
                             :recipe/name (:name recipe)
                             :recipe/description (:description recipe)
                             :recipe/image (java.net.URI. (:image recipe))
                             :recipe/url (java.net.URI. (:url recipe))
                             :recipe/ingredients ingredient-ids}])]
         (datomic/transact conn tx))))))

(ns recipissimo-importer.core
  (:require [cheshire.core :as json]
            [clojure.string :as string]))

(defn remove-parenthetic-phrases [text]
  (string/replace text #"\(.+?\)" ""))

(defn remove-comma-and-following-text [text]
  (first (string/split text #",")))

(defn split-on-whitespace [text]
  (string/split text #"\s+"))

(def measures
  (->> ["t" "teaspoon" "tablespoon" "c" "cup" "gal" "gallon"
        "oz" "ounce" "lb" "pound"
        "ml" "milliliter" "millilitre" "l" "liter" "litre"
        "g" "gr" "gram" "kg" "kilogram"
        "heaping" "pinch" "scant"]
       (mapcat (fn [name] (map #(str name %) ["" "." "s" "es"])))
       set))

(defn measure-word? [word]
  (or (re-find #"[\d¼½¾⅓⅔⅕⅖⅗⅘⅙⅚⅛⅜⅝⅞]" word)
      (measures word)))

(defn parse-ingredient [ingredient]
  (let [words (->> ingredient
                   remove-parenthetic-phrases
                   remove-comma-and-following-text
                   split-on-whitespace
                   (map string/lower-case))
        measure-words (filter measure-word? words)
        ingredient-words (filter (complement measure-word?) words)]
    (if (or (empty? ingredient-words)
            (empty? measure-words)) nil
        (string/join " " ingredient-words))))

(defn parse-recipe [recipe]
  {:ingredients
   (->> ((json/parse-string recipe) "ingredients")
        (string/split-lines)
        (map string/trim)
        (map (fn [i] [i (parse-ingredient i)]))
        (filter (comp not nil? second))
        vec)})

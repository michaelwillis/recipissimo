(ns recipissimo-importer.core-test
  (:require [midje.sweet :refer :all]
            [recipissimo-importer.core :refer :all]))

(fact "parse-ingredient returns nil if there is no measurement"
      (parse-ingredient "Biscuits:") => nil)

(fact "parse-ingredient recognizes whole numbers"
      (parse-ingredient "1 large tomato") => "large tomato")

(fact "parse-ingredient recognizes plural and singular units"
      (parse-ingredient "1 Teaspoon butter") => "butter"
      (parse-ingredient "1 teaspoon butter") => "butter"
      (parse-ingredient "2 Teaspoons butter") => "butter"
      (parse-ingredient "2 teaspoons butter") => "butter"
      (parse-ingredient "1 Tablespoon butter") => "butter"
      (parse-ingredient "1 tablespoon butter") => "butter"
      (parse-ingredient "2 Tablespoon butter") => "butter"
      (parse-ingredient "2 tablespoon butter") => "butter"
      (parse-ingredient "1 Cup butter") => "butter"
      (parse-ingredient "1 cup butter") => "butter"
      (parse-ingredient "2 Cups butter") => "butter"
      (parse-ingredient "2 cups butter") => "butter")

(fact "parse-ingredient recognizes abbreviations"
      (parse-ingredient "1 t sugar") => "sugar"
      (parse-ingredient "1 t. sugar") => "sugar"
      (parse-ingredient "1 T sugar") => "sugar"
      (parse-ingredient "1 T. sugar") => "sugar"
      (parse-ingredient "1 c. sugar") => "sugar"
      (parse-ingredient "1 C. sugar") => "sugar"
      (parse-ingredient "12 gal cinnamon") => "cinnamon"
      (parse-ingredient "12 Gal cinnamon") => "cinnamon"
      (parse-ingredient "12 GAL cinnamon") => "cinnamon"      
      (parse-ingredient "12 gal. cinnamon") => "cinnamon"
      (parse-ingredient "12 Gal. cinnamon") => "cinnamon"
      (parse-ingredient "12 GAL. cinnamon") => "cinnamon"      
      (parse-ingredient "1 oz sugar") => "sugar"
      (parse-ingredient "1 oz. sugar") => "sugar"
      (parse-ingredient "1 Oz. sugar") => "sugar"
      (parse-ingredient "1 OZ. sugar") => "sugar"
      (parse-ingredient "1 lb sugar") => "sugar"
      (parse-ingredient "1 lb. sugar") => "sugar"
      (parse-ingredient "1 Lb sugar") => "sugar"
      (parse-ingredient "1 LB. sugar") => "sugar"
      (parse-ingredient "1 g sugar") => "sugar"
      (parse-ingredient "1 g. sugar") => "sugar"
      (parse-ingredient "1 gr sugar") => "sugar"
      (parse-ingredient "1 GR sugar") => "sugar"
      (parse-ingredient "1 kg sugar") => "sugar"
      (parse-ingredient "1 Kg. sugar") => "sugar"
      (parse-ingredient "1 KG sugar") => "sugar"
      (parse-ingredient "1 KG. sugar") => "sugar")

(fact "parse-ingredient recognizes known non-standard units and adjectives"
      (parse-ingredient "pinch salt") => "salt"
      (parse-ingredient "heaping tablespoon salt") => "salt"
      (parse-ingredient "scant tablespoon salt") => "salt")

(fact "parse-ingredient recognizes fractions"
      (parse-ingredient "¼ cup sugar") => "sugar"
      (parse-ingredient "½ cup sugar") => "sugar"
      (parse-ingredient "⅔ cup sugar") => "sugar"
      (parse-ingredient "¾ cup sugar") => "sugar"
      (parse-ingredient "⅛ teaspoon salt") => "salt"
      (parse-ingredient "1/2 tablespoon brown sugar") => "brown sugar")

(fact "parse-ingredient downcases ingredient names"
      (parse-ingredient "12 gallons Cinnamon") => "cinnamon"
      (parse-ingredient "12 gallons Tikka Masala Paste") => "tikka masala paste")

(fact "parse-ingredient eliminates comma and following text"
      (parse-ingredient "12 Tablespoons garlic, chopped") => "garlic"
      (parse-ingredient "12 Tablespoons cilantro, chopped or pureed") => "cilantro")

(def pistachio-chocolate-chip-muffins
  )

(fact "parse-recipe correctly extracts ingredients list from json"
      (:ingredients (parse-recipe "{ \"_id\" : { \"$oid\" : \"5160756b96cc62079cc2db15\" }, \"name\" : \"Drop Biscuits and Sausage Gravy\", \"ingredients\" : \"Biscuits\\n3 cups All-purpose Flour\\n2 Tablespoons Baking Powder\\n1/2 teaspoon Salt\\n1-1/2 stick (3/4 Cup) Cold Butter, Cut Into Pieces\\n1-1/4 cup Butermilk\\n SAUSAGE GRAVY\\n1 pound Breakfast Sausage, Hot Or Mild\\n1/3 cup All-purpose Flour\\n4 cups Whole Milk\\n1/2 teaspoon Seasoned Salt\\n2 teaspoons Black Pepper, More To Taste\", \"url\" : \"http://thepioneerwoman.com/cooking/2013/03/drop-biscuits-and-sausage-gravy/\", \"image\" : \"http://static.thepioneerwoman.com/cooking/files/2013/03/bisgrav.jpg\", \"ts\" : { \"$date\" : 1365276011104 }, \"cookTime\" : \"PT30M\", \"source\" : \"thepioneerwoman\", \"recipeYield\" : \"12\", \"datePublished\" : \"2013-03-11\", \"prepTime\" : \"PT10M\", \"description\" : \"Late Saturday afternoon, after Marlboro Man had returned home with the soccer-playing girls, and I had returned home with the...\" }")) =>
      [["3 cups All-purpose Flour" "all-purpose flour"]
       ["2 Tablespoons Baking Powder" "baking powder"]
       ["1/2 teaspoon Salt" "salt"]
       ["1-1/2 stick (3/4 Cup) Cold Butter, Cut Into Pieces" "stick cold butter"]
       ["1-1/4 cup Butermilk" "butermilk"]
       ["1 pound Breakfast Sausage, Hot Or Mild" "breakfast sausage"]
       ["1/3 cup All-purpose Flour" "all-purpose flour"]
       ["4 cups Whole Milk" "whole milk"]
       ["1/2 teaspoon Seasoned Salt" "seasoned salt"]
       ["2 teaspoons Black Pepper, More To Taste" "black pepper"]]

      (:ingredients (parse-recipe "{ \"_id\" : { \"$oid\" : \"5160757696cc6207a37ff779\" }, \"name\" : \"Za'atar\", \"ingredients\" : \"4 tablespoons fresh thyme leaves, stripped from stems (or equivalent dried)\\n2  teaspoons ground sumac*\\nscant 1/2 teaspoon fine sea salt, or to taste\\n1 tablespoon toasted sesame seeds\", \"url\" : \"http://www.101cookbooks.com/archives/zaatar-recipe.html\", \"image\" : \"http://www.101cookbooks.com/mt-static/images/food/zaatar.jpg\", \"ts\" : { \"$date\" : 1365276022231 }, \"cookTime\" : \"PT10M\", \"source\" : \"101cookbooks\", \"datePublished\" : \"2013-01-27\", \"prepTime\" : \"PT5M\", \"description\" : \"Za'atar is an incredibly versatile Middle Eastern spice blend, one of my favorites. Particularly this time of year when it's a welcome addition to all sorts of roasted vegetables, soups and stews, or simply sprinkled over everything from yogurt, to eggs, to savory granola.\" }")) =>
      [["4 tablespoons fresh thyme leaves, stripped from stems (or equivalent dried)" "fresh thyme leaves"]
       ["2  teaspoons ground sumac*" "ground sumac*"]
       ["scant 1/2 teaspoon fine sea salt, or to taste", "fine sea salt"]
       ["1 tablespoon toasted sesame seeds" "toasted sesame seeds"]]
      
      (:ingredients (parse-recipe "{ \"_id\" : { \"$oid\" : \"5160757796cc6207aada3230\" }, \"totalTime\" : \"PT30M\", \"description\" : \"These muffins are inspired by a new favorite snack of mine: A handful of roasted pistachios and chocolate chips. I really love the flavo\", \"ingredients\" : \"1½ cups whole wheat pastry flour\n1¾ cup roasted and unsalted shelled pistachios\n1 teaspoon baking powder\n1 teaspoon baking soda\n½ teaspoon sea salt\n¾ cup milk\n2 eggs\n½ cup coconut oil, walnut oil, or melted butter\n½ cup honey\n1½ cup chocolate chips\", \"url\" : \"http://naturallyella.com/2013/03/20/pistachio-and-chocolate-chip-muffins/\", \"image\" : \"http://cdn.naturallyella.com/files/2013/03/IMG_6411-2-200x300.jpg\", \"creator\" : \"Erin Alderson\", \"ts\" : { \"$date\" : 1365276023004 }, \"datePublished\" : \"2013-03-20T07:42:13+00:00\", \"source\" : \"naturallyella\", \"recipeYield\" : \"12\", \"cookTime\" : \"PT20M\", \"recipeCategory\" : \"Snack\", \"prepTime\" : \"PT10M\", \"name\" : \"Pistachio Chocolate Chip Muffins\" }")) =>
      [["1½ cups whole wheat pastry flour" "whole wheat pastry flour"]
       ["1¾ cup roasted and unsalted shelled pistachios" "roasted and unsalted shelled pistachios"]
       ["1 teaspoon baking powder" "baking powder"]
       ["1 teaspoon baking soda" "baking soda"]
       ["½ teaspoon sea salt" "sea salt"]
       ["¾ cup milk" "milk"]
       ["2 eggs" "eggs"]
       ["½ cup coconut oil, walnut oil, or melted butter" "coconut oil"]
       ["½ cup honey" "honey"]
       ["1½ cup chocolate chips" "chocolate chips"]]

      (:ingredients (parse-recipe "{ \"_id\" : { \"$oid\" : \"5160758296cc6207aada3233\" }, \"totalTime\" : \"PT10M\", \"description\" : \"I have this post that I’m not sure quite how to write without sounding like I’m a stones throw away to being a cliched analogy of standing on a soapbox or p\", \"ingredients\" : \"½ head green cabbage, approximately 4-5 cups shredded\\n6-8 carrots\\n1 bunch green onions\\nJuice from 1 large lime\\n½ cup cilantro, minced\\n10-12 rice paper wrappers\", \"url\" : \"http://naturallyella.com/2013/03/27/sexy-cabbage-cilantro-lime-carrot-and-cabbage-spring-rolls/\", \"image\" : \"http://cdn.naturallyella.com/files/2013/03/IMG_6561-200x300.jpg\", \"creator\" : \"Erin Alderson\", \"ts\" : { \"$date\" : 1365276034253 }, \"datePublished\" : \"2013-03-27T07:40:44+00:00\", \"source\" : \"naturallyella\", \"recipeYield\" : \"4-6\", \"prepTime\" : \"PT10M\", \"name\" : \"Sexy Cabbage (+ Cilantro-Lime Carrot and Cabbage Spring Rolls)\" }")) =>
      [["½ head green cabbage, approximately 4-5 cups shredded" "head green cabbage"]
       ["6-8 carrots" "carrots"]
       ["1 bunch green onions" "bunch green onions"]
       ["Juice from 1 large lime" "juice from large lime"]
       ["½ cup cilantro, minced" "cilantro"]
       ["10-12 rice paper wrappers" "rice paper wrappers"]])



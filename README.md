# Recipissimo

A menu and shopping list creator built on [Pedestal](http://pedestal.io/), [Datomic](http://www.datomic.com/), and [OpenRecipes](https://github.com/fictivekin/openrecipes)

## Set up

### Get Datomic, start the transactor

```bash
DATOMIC_VERSION=0.8.4143
wget http://downloads.datomic.com/$DATOMIC_VERSION/datomic-free-$DATOMIC_VERSION.zip
unzip datomic-free-$DATOMIC_VERSION.zip
cd datomic-free-$DATOMIC_VERSION
./bin/transactor config/samples/free-transactor-template.properties
```

### Import the sample file from [OpenRecipes](https://github.com/fictivekin/openrecipes)

```bash
# In recipissimo root dir
cd recipissimo-importer
# Note that this is only the sample file
# Importing the full OpenRecipes db would require a beefier 
# (pun not intended, but I'll keep it) datomic setup
wget http://openrecipes.s3.amazonaws.com/openrecipes.txt
lein repl
```

in the REPL:

```clojure
(use 'recipissimo-importer.core)
(require '[datomic.api :as datomic])
(def db-uri "datomic:free://localhost:4334/recipissimo")
(init-db db-uri)
(import-recipe-file-to-db "openrecipes.txt" db-uri)
```

### Run the app

```bash
# In recipissimo root dir
cd recipissimo-service
lein repl
```

In the REPL:

```clojure
(use 'dev)
(start)
```

In another terminal:

```bash
# Again in recipissimo root dir
cd recipissimo-client
lein repl
```

In the REPL:

```clojure
(use 'dev)
(start)
```

Navigate to:

[http://localhost:3000/recipissimo-client.html](http://localhost:3000/recipissimo-client.html)

### Calendar View

[Calendar View](screenshots/calendar-view.png)

This is the first view that you will see in the app.  The two-week calendar may take a few seconds to appear the first time you view it after the app is started.

Use the top-right search to find recipes.  If there are any matches, up to 15 will be presented.  Click a recipe link to view the original blog post about the given recipe.  Drag and drop recipes from the search results to the calendar.  Click the X to delete an unwanted recipe from the calendar.

Click "Create Shopping List" to move on to the next view.

### Shopping List View

[Shopping List View](screenshots/shopping-list-view.png)

This shows the ingredients needed to make the recipes that were put on the calendar.  It allows you to create categories and then drag and drop ingredients to categorize them.  The app will remember ingredient categories for future use.  Print this page to do your shopping.

## License

Copyright © 2013 Michael Willis

Distributed under the Eclipse Public License, the same as Clojure.

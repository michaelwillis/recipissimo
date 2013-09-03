var initSearchBox = function(callback) {
    var searchTimeout = null;
    jQuery("#recipe-search-text").on("keypress", function() {
        if (searchTimeout != null) {
            clearTimeout(searchTimeout);
        }

        searchTimeout = setTimeout(function() {
            callback(jQuery("#recipe-search-text").val());
        }, 500);
    });
};

var clearSearchResults = function() {
    jQuery("#search-results").empty();
};

var addSearchResult = function(recipeId, name, url) {
    jQuery("#search-results").append(
        jQuery("<li>")
            .draggable({revert:true})
            .data("recipe-id", recipeId)
            .append(jQuery("<a>").attr("href", url).attr("target", "_blank").text(name)));
};

var clearCalendar = function() {
    jQuery("#calendar").empty();
};

var createCalendarRow = function() {
    var tr = jQuery("<tr>");
    jQuery("#calendar").append(tr);
    return tr.get(0);
};

var createCalendarDay = function(tr, text, callback) {
    var ul = jQuery("<ul>")
    var td = jQuery("<td>")
        .append(jQuery("<span>").text(text))
        .append(ul)
        .droppable({
            hoverClass: "highlight",
            tolerance: "pointer",
            drop: function(event, ui) {
                callback(ui.draggable.data("recipe-id"));
                ui.draggable.remove();
            }
        }); 
    jQuery(tr).append(td);
    return ul.get(0);
};

var addRecipeToCalendar = function(ul, name, url, deleteCallback) {
    jQuery(ul).append(
        jQuery("<li>")
            .append(jQuery("<a>").attr("href", url).text(name))
            .append(jQuery("<img>").attr("src","/delete.png").click(deleteCallback)));
};

var initCreateShoppingListButton = function(callback) {
    jQuery("#create-shopping-list-button").click(callback);
};

var initShoppingList = function(addCategoryCallback) {
    jQuery("#add-category-button").click(
        function() { 
            addCategoryCallback(jQuery("#add-category-name").val());
        }
    );
};

var clearIngredients = function() {
    jQuery("#categories").empty();
};

var renderCategory = function(name, dropIngredientCallback, deleteCallback) {
    var ul = jQuery("<ul>");
    var fieldSet = jQuery("<fieldset>")        
        .droppable({
            drop: function(event, ui) {
                ui.draggable.remove();
                dropIngredientCallback(ui.draggable.text());
            }
        });

    var legend = jQuery("<legend>")
        .append(jQuery("<span>").text(name));

    if (name != "other") {
        legend.append(jQuery("<img>").attr("src","/delete.png").click(deleteCallback));
    }

    fieldSet.append(legend).append(ul);
    jQuery("#categories").append(fieldSet);
    return ul;
};

var addIngredientToCategory = function(category, name) {
    category.append(
        jQuery("<li>")
            .draggable({revert:true})
            .text(name))
};
    

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
}

var clearSearchResults = function() {
    jQuery("#search-results").empty();
}

var addSearchResult = function(recipeId, name, url) {
    jQuery("#search-results").append(
        jQuery("<li>")
            .draggable({revert:true})
            .data("recipe-id", recipeId)
            .append(jQuery("<a>").attr("href", url).attr("target", "_blank").text(name)));
}

var clearCalendar = function() {
    jQuery("#calendar").empty();
}

var createCalendarRow = function() {
    var tr = jQuery("<tr>");
    jQuery("#calendar").append(tr);
    return tr.get(0);
}

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
}

var addRecipeToCalendar = function(ul, recipeId, name, url) {
    jQuery(ul).append(
        jQuery("<li>").append(
            jQuery("<a>").attr("href", url).text(name)));
}

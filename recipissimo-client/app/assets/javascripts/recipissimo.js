var clearSearchResults = function() {
    jQuery("#search-results").empty();
}

var addSearchResult = function(recipeId, name, url) {
    jQuery("#search-results").append(
        jQuery("<li>")
            .draggable({revert:true})
            .data("recipe-id", recipeId)
            .append(jQuery("<a>").attr("href", url).text(name)));
}

var makeCalendarDayDroppable = function(element) {
    jQuery(element).droppable({
        hoverClass: "highlight",
        tolerance: "pointer",
        drop: function(event, ui) {
            alert(ui.draggable.data("recipe-id"));
        }
    });
}

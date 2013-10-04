function getForkName(inRecipeTitle) {
	bootbox.prompt('Please name your new recipe', function(res) { if ( res != null && res != '') { document.location.href = '/recipes/' + inRecipeTitle + '/fork?newName=' + res; } });
	return false;
}

function addIngredient(inRecipeTitle, inIngredientSelect) {
	document.location.href = '/recipes/' + inRecipeTitle + '/' + inIngredientSelect.select2('data').displayName + '/add';
	return false;
}

function createAddIngredient(inIngredientSelect) {
	document.location.href = '/recipes/create/' + inIngredientSelect.select2('data').displayName + '/add';
	return false;
}

function cancelCreate() {
	document.location.href = '/recipes/create/cancel';
	return false;
}

function addInclTag(inTag) {
    $.post("/user/filter/inc/addTag=" + inTag, function(data) { location.reload(false); });
}

function addExclTag(inTag) {
    $.post("/user/filter/exc/addTag=" + inTag, function(data) { location.reload(false); });
}

function clearFilters() {
    $.post("/user/filter/clearAll", function(data) { location.reload(false); });
}
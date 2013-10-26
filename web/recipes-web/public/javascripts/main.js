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


function clearAllIngredients() {
	document.location.href = '/recipes/create/clear';
	return false;
}

function addInclTag(inName, inValue) {
	var adjustedName = inName.replace("' tag",'').replace("'", "");  // Yuk!
	var url = inValue != null ? ("/user/filter/inc/add=" + adjustedName + "/v=" + inValue) : ("/user/filter/inc/add=" + adjustedName);
    $.post( url, function(data) { location.reload(false); });
}

function addExclTag(inName, inValue) {
	var adjustedName = inName.replace("' tag",'').replace("'", "");  // Yuk!
	var url = inValue != null ? ("/user/filter/exc/add=" + adjustedName + "/v=" + inValue) : ("/user/filter/exc/add=" + adjustedName);
    $.post( url, function(data) { location.reload(false); });
}

function removeInclTag(inName) {
	var adjustedName = inName.replace("' tag",'').replace("'", "");  // Yuk!
    $.post("/user/filter/inc/rem=" + adjustedName, function(data) { location.reload(false); });
}

function removeExclTag(inName) {
	var adjustedName = inName.replace("' tag",'').replace("'", "");  // Yuk!
    $.post("/user/filter/exc/rem=" + adjustedName, function(data) { location.reload(false); });
}

function clearFilters() {
    $.post("/user/filter/clearAll", function(data) { location.reload(false); });
}

function faveRecipe(inRecipeTitle) {
	document.location.href = '/recipes/' + inRecipeTitle + '/fave';
	return false;
}

function faveItem(inItem) {
	document.location.href = '/items/' + inItem + '/fave';
	return false;
}

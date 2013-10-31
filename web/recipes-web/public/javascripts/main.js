function getForkName(inRecipeTitle) {
	bootbox.prompt('Please name your new recipe', function(res) { if ( res != null && res != '') { document.location.href = '/recipes/' + inRecipeTitle + '/fork?newName=' + res; } });
	return false;
}

function createNewRecipe() {
	bootbox.prompt('Please name your new recipe', function(res) { if ( res != null && res != '') { document.location.href = '/recipes/create/finish/' + res; } });
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
	doPost(url);
}

function addExclTag(inName, inValue) {
	var adjustedName = inName.replace("' tag",'').replace("'", "");  // Yuk!
	var url = inValue != null ? ("/user/filter/exc/add=" + adjustedName + "/v=" + inValue) : ("/user/filter/exc/add=" + adjustedName);
	doPost(url);
}

function removeInclTag(inName, inValue) {
	var adjustedName = inName.replace("' tag",'').replace("'", "");  // Yuk!
	var url = inValue != null ? ("/user/filter/inc/rem=" + adjustedName + "/v=" + inValue) : ("/user/filter/inc/rem=" + adjustedName);
	doPost(url);
}

function removeExclTag(inName, inValue) {
	var adjustedName = inName.replace("' tag",'').replace("'", "");  // Yuk!
	var url = inValue != null ? ("/user/filter/exc/rem=" + adjustedName + "/v=" + inValue) : ("/user/filter/exc/rem=" + adjustedName);
	doPost(url);
}

function doPost(url) {
    $.post( url, function(data) { location.reload(false); }).error( function() { alert('An error occurred - please contact us.') });
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

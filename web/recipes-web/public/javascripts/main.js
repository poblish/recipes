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
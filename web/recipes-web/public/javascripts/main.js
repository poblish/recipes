function getForkName(inRecipeTitle) {
	bootbox.prompt('Please name your new recipe', function(res) { if ( res != null && res != '') { document.location.href = '/recipes/' + inRecipeTitle + '/fork?newName=' + res; } });
	return false;
}
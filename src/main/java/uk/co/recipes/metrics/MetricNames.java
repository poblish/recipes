/**
 * 
 */
package uk.co.recipes.metrics;

/**
 * @author andrewr
 *
 */
public interface MetricNames {

    String TIMER_RECIPES_NAME_GETS = "recipes.by_name.get";
    String TIMER_RECIPES_PUTS = "recipes.put";
    String TIMER_RECIPES_SEARCHES = "recipes.searches";
    String TIMER_RECIPES_IDS_SEARCHES = "recipe_ids.searches";
	String TIMER_RECIPES_RECOMMENDATIONS = "recipes.recommendations";
	String TIMER_RECIPES_FILTERED_RECOMMENDATIONS = "recipes.recommendations.filtered";
	String TIMER_RECIPES_MOSTSIMILAR = "recipes.mostsimilar";

    String TIMER_RECIPE_PARSE = "recipe.parse";

    String TIMER_ITEMS_NAME_GETS = "items.by_name.get";
    String TIMER_ITEMS_ID_GETS = "items.by_id.get";
    String TIMER_ITEMS_PUTS = "items.put";
	String TIMER_ITEMS_SEARCHES = "items.searches";
	String TIMER_ITEMS_MOSTSIMILAR = "items.mostsimilar";
	String TIMER_ITEMS_COUNT_AMONG_RECIPES = "items.count_among_recipes";

	String TIMER_USER_ITEMS_RECOMMENDATIONS = "recommendations_user:items";
	String TIMER_RECIPE_ITEMS_RECOMMENDATIONS = "recommendations_recipe:items";

    String TIMER_EXPLORER_FILTER_IDS_GET = "recipe.explorer_filter.ids.get";
    String TIMER_BUILD_FILTER_GET_IDS = "recipe.build_explorer_filter.ids.get";

    String TIMER_USER_LOCAL_GET = "recipe.getLocalUser";

    String TIMER_LOAD_ITEM_PROCESSITEM = "recipes.laod.item.processItem";

}
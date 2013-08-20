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
	String TIMER_RECIPES_RECOMMENDATIONS = "recipes.recommendations";
	String TIMER_RECIPES_MOSTSIMILAR = "recipes.mostsimilar";

    String TIMER_ITEMS_NAME_GETS = "items.by_name.get";
    String TIMER_ITEMS_ID_GETS = "items.by_id.get";
    String TIMER_ITEMS_PUTS = "items.put";
	String TIMER_ITEMS_SEARCHES = "items.searches";
	String TIMER_ITEMS_RECOMMENDATIONS = "items.recommendations";
	String TIMER_ITEMS_MOSTSIMILAR = "items.mostsimilar";
}
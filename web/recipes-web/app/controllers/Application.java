package controllers;

import java.io.IOException;

import net.myrrix.client.ClientRecommender;

import org.apache.mahout.cf.taste.common.TasteException;

import play.mvc.Controller;
import play.mvc.Result;
import uk.co.recipes.DaggerModule;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.service.api.IItemPersistence;
import uk.co.recipes.service.api.IRecipePersistence;
import uk.co.recipes.service.api.IUserPersistence;
import dagger.ObjectGraph;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class Application extends Controller {

	private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );
	private final static IItemPersistence ITEMS = GRAPH.get( EsItemFactory.class );
    private final static IRecipePersistence RECIPES = GRAPH.get( EsRecipeFactory.class );
    private final static IUserPersistence USERS = GRAPH.get( EsUserFactory.class );
    private final static ClientRecommender RECOMMENDER = GRAPH.get( ClientRecommender.class );

	public static Result index() {
        return ok(views.html.index.render("Your new application is ready."));
    }

	public static Result stats() {
        try {
        	return ok(views.html.stats.render( ITEMS.countAll(), RECIPES.countAll(), USERS.countAll(), /* Ugh! FIXME */ RECOMMENDER.getAllUserIDs().size(), /* Ugh! FIXME */ RECOMMENDER.getAllItemIDs().size()));
        }
        catch (IOException e) {  // Yuk!!!
        	return ok(views.html.stats.render( -1L, -1L, -1L, -1, -1));
        }
        catch (TasteException e) {  // Yuk!!!
        	return ok(views.html.stats.render( -1L, -1L, -1L, -1, -1));
        }
    }
}
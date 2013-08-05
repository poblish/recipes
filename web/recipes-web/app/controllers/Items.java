package controllers;

import java.io.IOException;
import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;
import uk.co.recipes.DaggerModule;
import uk.co.recipes.User;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IUser;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.service.api.IExplorerAPI;
import uk.co.recipes.service.api.IItemPersistence;
import uk.co.recipes.service.api.IUserPersistence;
import uk.co.recipes.service.impl.MyrrixExplorerService;

import com.google.common.base.Supplier;

import dagger.ObjectGraph;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class Items extends Controller {

	private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );
	private final static IItemPersistence ITEMS = GRAPH.get( EsItemFactory.class );
    private final static IUserPersistence USERS = GRAPH.get( EsUserFactory.class );
    private final static IExplorerAPI EXPLORER_API = GRAPH.get( MyrrixExplorerService.class );

    public static Result display( final String name) throws IOException {
        final ICanonicalItem item = ITEMS.get(name).get();
        final List<ICanonicalItem> similarities = EXPLORER_API.similarIngredients( item, 10);
        return ok(views.html.item.render( item, similarities, ( item != null) ? "Found" : "Not Found"));
    }

    public static Result test() throws IOException {
        final String[] theInputs = request().queryString().get("input");
        final boolean gotInput = ( theInputs != null && theInputs.length > 0 && !theInputs[0].isEmpty());
        final String theInput = gotInput ? theInputs[0] : "ginger";
        final ICanonicalItem item = ITEMS.get(theInput).get();
        final List<ICanonicalItem> similarities = EXPLORER_API.similarIngredients( item, 10);

        return ok(views.html.items.render( theInput, similarities, gotInput ? "Search Results" : "Test"));
    }
}
package controllers;

import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.service.api.IUserPersistence;
import com.google.common.base.Supplier;
import uk.co.recipes.User;
import uk.co.recipes.api.IUser;
import uk.co.recipes.service.impl.MyrrixRecommendationService;
import uk.co.recipes.service.api.IRecommendationsAPI;
import java.io.IOException;
import java.util.List;
import play.mvc.Controller;
import play.mvc.Result;
import uk.co.recipes.DaggerModule;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.service.api.IExplorerAPI;
import uk.co.recipes.service.api.IItemPersistence;
import uk.co.recipes.service.impl.MyrrixExplorerService;
import views.html.index;
import dagger.ObjectGraph;

public class Application extends Controller {

	private final static ObjectGraph GRAPH = ObjectGraph.create( new DaggerModule() );
	private final static IItemPersistence ITEMS = GRAPH.get( EsItemFactory.class );
    private final static IUserPersistence USERS = GRAPH.get( EsUserFactory.class );
    private final static IExplorerAPI EXPLORER_API = GRAPH.get( MyrrixExplorerService.class );
    private final static IRecommendationsAPI RECS_API = GRAPH.get( MyrrixRecommendationService.class );

	public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static Result test() throws IOException {
        final String[] theInputs = request().queryString().get("input");
        final boolean gotInput = ( theInputs != null && theInputs.length > 0 && !theInputs[0].isEmpty());
        final String theInput = gotInput ? theInputs[0] : "ginger";
        final ICanonicalItem item = ITEMS.get(theInput).get();
        final List<ICanonicalItem> similarities = EXPLORER_API.similarIngredients( item, 10);

        final IUser user1 = USERS.getOrCreate( "Andrew Regan", new Supplier<IUser>() {

            @Override
            public IUser get() {
                return new User();
            }
        } );
        final List<ICanonicalItem> recommendations = RECS_API.recommendIngredients( user1, 10);

        return ok(views.html.test.render( theInput, similarities, user1, recommendations, gotInput ? "Search Results" : "Test"));
    }
}
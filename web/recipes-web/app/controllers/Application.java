package controllers;

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
	private final static IItemPersistence itemFactory = GRAPH.get( EsItemFactory.class );
    private final static IExplorerAPI explorerApi = GRAPH.get( MyrrixExplorerService.class );

	public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static Result test() throws IOException {
        final ICanonicalItem item = itemFactory.get("ginger").get();
        final List<ICanonicalItem> similarities = explorerApi.similarIngredients( item, 10);

        return ok(views.html.test.render("" + similarities));
    }

    public static Result search() {
        return ok(views.html.test.render("Hello, Wold!"));
    }
}
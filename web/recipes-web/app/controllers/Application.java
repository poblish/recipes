package controllers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

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

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;

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
    private final static MetricRegistry METRICS = GRAPH.get( MetricRegistry.class );

    public static String getMetricsString() {
        if (METRICS.getMetrics().isEmpty()) {
            return "No Metrics in... " + METRICS;
        }

        // See: http://ediweissmann.com/blog/2013/03/10/yammer-metrics-and-playframework/
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(baos);
        ConsoleReporter.forRegistry(METRICS).outputTo(ps).build().report();
        ps.flush();
        ps.close();
        return "Metrics: " + new String( baos.toByteArray() );
    }

	public static Result index() {
        return ok(views.html.index.render("Your new application is ready."));
    }

	public static Result stats() {
        try {
        	return ok(views.html.stats.render( getMetricsString(), ITEMS.countAll(), RECIPES.countAll(), USERS.countAll(), /* Ugh! FIXME */ RECOMMENDER.getAllUserIDs().size(), /* Ugh! FIXME */ RECOMMENDER.getAllItemIDs().size()));
        }
        catch (IOException e) {  // Yuk!!!
        	return ok(views.html.stats.render( "???", -1L, -1L, -1L, -1, -1));
        }
        catch (TasteException e) {  // Yuk!!!
        	return ok(views.html.stats.render( "???", -1L, -1L, -1L, -1, -1));
        }
    }
}
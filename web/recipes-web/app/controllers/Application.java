package controllers;

import static com.google.common.base.Preconditions.checkNotNull;
import uk.co.recipes.User;
import play.mvc.Http.Session;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.PlayAuthenticate;
import uk.co.recipes.api.IUser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import javax.inject.Inject;
import net.myrrix.client.ClientRecommender;
import org.apache.mahout.cf.taste.common.TasteException;
import play.mvc.Controller;
import play.mvc.Result;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.service.api.IItemPersistence;
import uk.co.recipes.service.api.IRecipePersistence;
import uk.co.recipes.service.api.IUserPersistence;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class Application extends Controller {

    private IItemPersistence items;
    private IUserPersistence users;
    private IRecipePersistence recipes;
    private MetricRegistry metrics;
    private ClientRecommender recommender;

    @Inject
    public Application( final EsItemFactory items, final EsRecipeFactory recipes, final EsUserFactory users, final ClientRecommender recommender, final MetricRegistry metrics) {
        this.items = checkNotNull(items);
        this.recipes = checkNotNull(recipes);
        this.users = checkNotNull(users);
        this.metrics = checkNotNull(metrics);
        this.recommender = checkNotNull(recommender);
    }

    public static IUser getLocalUser(final Session session) {
        final AuthUser currentAuthUser = PlayAuthenticate.getUser(session);
//        final User localUser = User.findByAuthUserIdentity(currentAuthUser);
        return new User( "aregan", "aregan"); // FIXME!
    }

    public String getMetricsString() {
//    	System.out.println( metrics + " / " + ((EsRecipeFactory)recipes).getMetricRegistry());
//    	metrics.counter("foop").inc();

    	if (metrics.getMetrics().isEmpty()) {
            return "No Metrics in... " + metrics;
        }

        // See: http://ediweissmann.com/blog/2013/03/10/yammer-metrics-and-playframework/
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(baos);
        ConsoleReporter.forRegistry(metrics).outputTo(ps).build().report();
        ps.flush();
        ps.close();
        return "Metrics: " + new String( baos.toByteArray() );
    }

	public static Result index() {
        return ok(views.html.index.render("Your new application is ready."));
    }

	public Result stats() {
        try {
        	return ok(views.html.stats.render( getMetricsString(), items.countAll(), recipes.countAll(), users.countAll(), /* Ugh! FIXME */ recommender.getAllUserIDs().size(), /* Ugh! FIXME */ recommender.getAllItemIDs().size()));
        }
        catch (IOException e) {  // Yuk!!!
        	return ok(views.html.stats.render( "???", -1L, -1L, -1L, -1, -1));
        }
        catch (TasteException e) {  // Yuk!!!
        	return ok(views.html.stats.render( "???", -1L, -1L, -1L, -1, -1));
        }
    }

	public static Result oAuthDenied(final String providerKey) {
		flash(/* FLASH_ERROR_KEY, */ "You need to accept the OAuth connection in order to use this website!");
		return redirect(routes.Application.index());
	}
}
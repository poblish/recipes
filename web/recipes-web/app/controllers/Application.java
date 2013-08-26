package controllers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

import net.myrrix.client.ClientRecommender;

import org.apache.mahout.cf.taste.common.TasteException;

import play.mvc.Controller;
import play.mvc.Result;
import service.PlayAuthUserServicePlugin;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.IUser;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.service.api.IExplorerFilter;
import uk.co.recipes.service.api.IItemPersistence;
import uk.co.recipes.service.api.IRecipePersistence;
import uk.co.recipes.service.api.IUserPersistence;
import uk.co.recipes.service.impl.EsExplorerFilters;
import uk.co.recipes.tags.CommonTags;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

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

	public static Set<ITag> allTags() {
		return FluentIterable.from( EnumSet.allOf( CommonTags.class ) ).transform( new Function<CommonTags,ITag>() {

			@Override
			@Nullable
			public ITag apply( @Nullable CommonTags inTag) {
				return inTag;
			}
		} ).toSortedSet( Ordering.usingToString() );
	}

	public static Collection<ITag> getExplorerIncludeTags() {
        final IUser currUser = /* Yuk! */ PlayAuthUserServicePlugin.getLocalUser( session() );
        if ( currUser == null) {
            return Collections.emptyList();
        }

        return currUser.getPrefs().getExplorerIncludeTags();
	}

	public static Collection<ITag> getExplorerExcludeTags() {
        final IUser currUser = /* Yuk! */ PlayAuthUserServicePlugin.getLocalUser( session() );
        if ( currUser == null) {
            return Collections.emptyList();
        }

        return currUser.getPrefs().getExplorerExcludeTags();
	}

	public static IExplorerFilter getExplorerFilter( final EsExplorerFilters inFilters) {
        final IUser currUser = /* Yuk! */ PlayAuthUserServicePlugin.getLocalUser( session() );
        if ( currUser == null) {
            return EsExplorerFilters.nullFilter();
        }

        try {
			return inFilters.includeExcludeTags( Iterables.get( getExplorerIncludeTags(), 0, null), Iterables.get( getExplorerExcludeTags(), 0, null) );
		}
        catch (Exception e) {
			return EsExplorerFilters.nullFilter();
		}
	}

	public Result explorerIncludeAddTag( final String inTag) {
        return handleUserPreference( new UserTask() {

			@Override
			public void run( IUser inUser) {
				inUser.getPrefs().explorerIncludeAdd( CommonTags.valueOf(inTag) );
			}} );
    }

	public Result explorerIncludeRemoveTag( final String inTag) {
        return handleUserPreference( new UserTask() {

			@Override
			public void run( IUser inUser) {
				inUser.getPrefs().explorerIncludeRemove( CommonTags.valueOf(inTag) );
			}} );
    }

	public Result explorerExcludeAddTag( final String inTag) {
        return handleUserPreference( new UserTask() {

			@Override
			public void run( IUser inUser) {
				inUser.getPrefs().explorerExcludeAdd( CommonTags.valueOf(inTag) );
			}} );
    }

	public Result explorerExcludeRemoveTag( final String inTag) {
        return handleUserPreference( new UserTask() {

			@Override
			public void run( IUser inUser) {
				inUser.getPrefs().explorerExcludeRemove( CommonTags.valueOf(inTag) );
			}} );
    }

	public Result handleUserPreference( final UserTask inTask) {
        final IUser currUser = /* Yuk! */ PlayAuthUserServicePlugin.getLocalUser( session() );
        if ( currUser == null) {
		    return unauthorized("Not logged-in");
        }

        inTask.run(currUser);

        try {
			((EsUserFactory) users).update(currUser);
		}
        catch (IOException e) {
			throw Throwables.propagate(e);
		}

        return ok();
    }

	private interface UserTask {
		void run( final IUser inUser);
	}
}
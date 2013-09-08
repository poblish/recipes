package controllers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

import net.myrrix.client.ClientRecommender;

import org.apache.mahout.cf.taste.common.TasteException;

import play.mvc.Controller;
import play.mvc.Result;
import service.PlayAuthUserServicePlugin;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.IUser;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.service.api.IItemPersistence;
import uk.co.recipes.service.api.IRecipePersistence;
import uk.co.recipes.service.api.IUserPersistence;
import uk.co.recipes.tags.CommonTags;
import uk.co.recipes.tags.TagUtils;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.feth.play.module.pa.PlayAuthenticate;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import com.google.common.collect.FluentIterable;
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
    private Cache<String,ICanonicalItem> itemsCache;

    @Inject
    public Application( final EsItemFactory items, final EsRecipeFactory recipes, final EsUserFactory users, final ClientRecommender recommender, final MetricRegistry metrics, final Cache<String,ICanonicalItem> inItemsCache) {
        this.items = checkNotNull(items);
        this.recipes = checkNotNull(recipes);
        this.users = checkNotNull(users);
        this.metrics = checkNotNull(metrics);
        this.recommender = checkNotNull(recommender);
        this.itemsCache = checkNotNull(inItemsCache);
    }

    public String getMetricsString() {
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
        	return ok(views.html.stats.render( getMetricsString(), items.countAll(), recipes.countAll(), users.countAll(), itemsCache.stats(), /* Ugh! FIXME */ recommender.getAllUserIDs().size(), /* Ugh! FIXME */ recommender.getAllItemIDs().size()));
        }
        catch (IOException e) {  // Yuk!!!
        	return ok(views.html.stats.render( "???", -1L, -1L, -1L, null, -1, -1));
        }
        catch (TasteException e) {  // Yuk!!!
        	return ok(views.html.stats.render( "???", -1L, -1L, -1L, null, -1, -1));
        }
    }

	public static Result oAuthDenied(final String providerKey) {
		flash(/* FLASH_ERROR_KEY, */ "You need to accept the OAuth connection in order to use this website!");
		return redirect(routes.Application.index());
	}

	public static void storeLoginRedirectUrl() {
		PlayAuthenticate.storeOriginalUrl( ctx() );
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

	public Result explorerIncludeAddTag( final String inTag) {
        return handleUserPreference( new UserTask() {

			@Override
			public boolean makeChanges( IUser inUser) {
				return inUser.getPrefs().explorerIncludeAdd( TagUtils.forName(inTag) );
			}} );
    }

	public Result explorerIncludeRemoveTag( final String inTag) {
        return handleUserPreference( new UserTask() {

			@Override
			public boolean makeChanges( IUser inUser) {
				return inUser.getPrefs().explorerIncludeRemove( TagUtils.forName(inTag) );
			}} );
    }

	public Result explorerExcludeAddTag( final String inTag) {
        return handleUserPreference( new UserTask() {

			@Override
			public boolean makeChanges( IUser inUser) {
				return inUser.getPrefs().explorerExcludeAdd( TagUtils.forName(inTag) );
			}} );
    }

	public Result explorerExcludeRemoveTag( final String inTag) {
        return handleUserPreference( new UserTask() {

			@Override
			public boolean makeChanges( IUser inUser) {
				return inUser.getPrefs().explorerExcludeRemove( TagUtils.forName(inTag) );
			}} );
    }

	public Result explorerClearAll() {
        return handleUserPreference( new UserTask() {

			@Override
			public boolean makeChanges( IUser inUser) {
				return inUser.getPrefs().explorerClearAll();
			}} );
    }

	public Result handleUserPreference( final UserTask inTask) {
        final IUser currUser = /* Yuk! */ PlayAuthUserServicePlugin.getLocalUser( metrics, session());
        if ( currUser == null) {
		    return unauthorized("Not logged-in");
        }

        if (!inTask.makeChanges(currUser)) {
        	return ok();
        }

        try {
			((EsUserFactory) users).update(currUser);
		}
        catch (IOException e) {
			throw Throwables.propagate(e);
		}

        try {
			users.waitUntilRefreshed();
		}
        catch (InterruptedException e) {
			// NOOP
		}

        return ok();
    }

	private interface UserTask {
		// Return false if no change
		boolean makeChanges( final IUser inUser);
	}
}
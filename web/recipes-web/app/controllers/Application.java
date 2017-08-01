package controllers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;
import javax.inject.Inject;

import net.myrrix.client.ClientRecommender;

import org.apache.mahout.cf.taste.common.TasteException;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import play.mvc.Controller;
import play.mvc.Result;
import service.PlayAuthUserServicePlugin;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.IUser;
import uk.co.recipes.loader.BbcGoodFoodLoader;
import uk.co.recipes.loader.CurryFrenzyLoader;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsRecipeFactory;
import uk.co.recipes.persistence.EsSequenceFactory;
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
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class Application extends Controller {

	private static Application STATIC_INST = null;  // FIXME

	private IItemPersistence items;
    private IUserPersistence users;
    private IRecipePersistence recipes;
    private EsSequenceFactory sequences;  // For deleteAll() only
    private MetricRegistry metrics;
    private ClientRecommender recommender;
    private Cache<String,ICanonicalItem> itemsCache;
    private BbcGoodFoodLoader bbcGfLoader;
    private CurryFrenzyLoader cfLoader;
    private Client esClient;
	private final PlayAuthenticate auth;

    private final ExecutorService loadPool = Executors.newFixedThreadPool(3);

    @Inject
    public Application( final EsItemFactory items, final EsRecipeFactory recipes, final EsUserFactory users, final ClientRecommender recommender, final MetricRegistry metrics, final Cache<String,ICanonicalItem> inItemsCache,
    				    final EsSequenceFactory seqs, final BbcGoodFoodLoader bbcGfLoader, final CurryFrenzyLoader cfLoader,
    				    final Client esClient,
						final PlayAuthenticate auth) {
        this.items = checkNotNull(items);
        this.recipes = checkNotNull(recipes);
        this.users = checkNotNull(users);
        this.metrics = checkNotNull(metrics);
        this.recommender = checkNotNull(recommender);
        this.itemsCache = checkNotNull(inItemsCache);
        this.sequences = checkNotNull(seqs);
        this.bbcGfLoader = checkNotNull(bbcGfLoader);
        this.cfLoader = checkNotNull(cfLoader);
		this.esClient = checkNotNull(esClient);
		this.auth = checkNotNull(auth);

		STATIC_INST = this; // FIXME
    }

    public Result itemStats() {
    	final Set<String> names = Sets.newTreeSet();
    	for ( SearchHit each : esClient.prepareSearch("recipe").setTypes("items").setQuery( QueryBuilders.matchAllQuery() ).setSize(9999).execute().actionGet().getHits()) {
    		names.add( each.getSource().get("canonicalName").toString() );
    	}

    	return ok(views.html.stats_items.render( Lists.newArrayList(names) ));
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

	public Result index() {
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

	public Result oAuthDenied(final String providerKey) {
		flash(/* FLASH_ERROR_KEY, */ "You need to accept the OAuth connection in order to use this website!");
		return redirect(routes.Application.index());
	}

	public static void storeLoginRedirectUrl() {
		STATIC_INST.auth.storeOriginalUrl( ctx() );
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

	public Result explorerIncludeAdd( final String inName) {
        return handleUserPreference( new UserTask() {

			@Override
			public boolean makeChanges( IUser inUser) {
				try {
					return inUser.getPrefs().explorerIncludeAdd( tagForFilterItemName(inName) );
				}
				catch (RuntimeException e) {
					return inUser.getPrefs().explorerIncludeAdd( getItem(inName) );
				}
			}} );
    }

	public Result explorerIncludeAddWithValue( final String inName, final String inValue) {
        return handleUserPreference( new UserTask() {

			@Override
			public boolean makeChanges( IUser inUser) {
				try {
					return inUser.getPrefs().explorerIncludeAdd( tagForFilterItemName(inName), inValue);
				}
				catch (RuntimeException e) {
					throw new UnsupportedOperationException("Should this work?");
				}
			}} );
    }

	public Result explorerIncludeRemove( final String inName) {
        return handleUserPreference( new UserTask() {

			@Override
			public boolean makeChanges( IUser inUser) {
				try {
					return inUser.getPrefs().explorerIncludeRemove( tagForFilterItemName(inName) );
				}
				catch (RuntimeException e) {
					return inUser.getPrefs().explorerIncludeRemove( getItem(inName) );
				}
			}} );
    }

	public Result explorerIncludeRemoveWithValue( final String inName, final String inValue) {
        return handleUserPreference( new UserTask() {

			@Override
			public boolean makeChanges( IUser inUser) {
				try {
					return inUser.getPrefs().explorerIncludeRemove( tagForFilterItemName(inName), inValue);
				}
				catch (RuntimeException e) {
					return inUser.getPrefs().explorerIncludeRemove( getItem(inName) );
				}
			}} );
    }

	public Result explorerExcludeAdd( final String inName) {
        return handleUserPreference( new UserTask() {

			@Override
			public boolean makeChanges( IUser inUser) {
				try {
					return inUser.getPrefs().explorerExcludeAdd( tagForFilterItemName(inName) );
				}
				catch (RuntimeException e) {
					return inUser.getPrefs().explorerExcludeAdd( getItem(inName) );
				}
			}} );
    }

	public Result explorerExcludeAddWithValue( final String inName, final String inValue) {
        return handleUserPreference( new UserTask() {

			@Override
			public boolean makeChanges( IUser inUser) {
				try {
					return inUser.getPrefs().explorerExcludeAdd( tagForFilterItemName(inName), inValue);
				}
				catch (RuntimeException e) {
					throw new UnsupportedOperationException("Should this work?");
				}
			}} );
    }

	public Result explorerExcludeRemove( final String inName) {
        return handleUserPreference( new UserTask() {

			@Override
			public boolean makeChanges( IUser inUser) {
				try {
					return inUser.getPrefs().explorerExcludeRemove( tagForFilterItemName(inName) );
				}
				catch (RuntimeException e) {
					return inUser.getPrefs().explorerExcludeRemove( getItem(inName) );
				}
			}} );
    }

	public Result explorerExcludeRemoveWithValue( final String inName, final String inValue) {
        return handleUserPreference( new UserTask() {

			@Override
			public boolean makeChanges( IUser inUser) {
				try {
					return inUser.getPrefs().explorerExcludeRemove( tagForFilterItemName(inName), inValue);
				}
				catch (RuntimeException e) {
					return inUser.getPrefs().explorerExcludeRemove( getItem(inName) );
				}
			}} );
    }

	private ITag tagForFilterItemName( final String inName) {
		return TagUtils.forName( /* FIXME, lame: */ inName.toUpperCase() );
	}

	public Result explorerClearAll() {
        return handleUserPreference( new UserTask() {

			@Override
			public boolean makeChanges( IUser inUser) {
				return inUser.getPrefs().explorerClearAll();
			}} );
    }

	public Result clearDataAndCache() throws IOException {
	    itemsCache.invalidateAll();
	    users.deleteAll();
		items.deleteAll();
		recipes.deleteAll();
		sequences.deleteAll();
		return ok("Cleared");
	}

	public Result loadBbcGoodFood() throws IOException, InterruptedException {
		loadPool.submit( new Runnable() {

			@Override
			public void run() {
				try {
					System.out.println("Start BBC Good Food load...");
					bbcGfLoader.start(false);
					System.out.println("DONE BBC Good Food load...");
				} catch (IOException e) {
					Throwables.propagate(e);
				} catch (InterruptedException e) {
					Throwables.propagate(e);
				}
			}
		} );

		return ok("Started");
	}

	public Result loadCurryFrenzy() throws IOException, InterruptedException {
		loadPool.submit( new Runnable() {

			@Override
			public void run() {
				try {
					System.out.println("Start Curry Frenzy load...");
					cfLoader.start();
					System.out.println("DONE Curry Frenzy load...");
				} catch (IOException e) {
					Throwables.propagate(e);
				} catch (InterruptedException e) {
					Throwables.propagate(e);
				}
			}
		} );

		return ok("Started");
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

	// Yuk
	private ICanonicalItem getItem( final String inName) {
		try {
			return items.get(inName).get();
		}
		catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}
}
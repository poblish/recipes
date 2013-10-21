package controllers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Collections;

import play.mvc.Controller;
import service.PlayAuthUserServicePlugin;
import uk.co.recipes.api.IExplorerFilterItem;
import uk.co.recipes.api.IUser;
import uk.co.recipes.events.api.IEventService;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.service.api.IExplorerAPI;
import uk.co.recipes.service.api.IExplorerFilterDef;
import uk.co.recipes.service.api.IItemPersistence;
import uk.co.recipes.service.api.IRecommendationsAPI;
import uk.co.recipes.service.impl.EsExplorerFilters;
import uk.co.recipes.service.impl.ExplorerFilterDefs;
import uk.co.recipes.service.impl.MyrrixExplorerService;
import uk.co.recipes.service.impl.MyrrixRecommendationService;
import uk.co.recipes.ui.CuisineColours;

import com.codahale.metrics.MetricRegistry;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public abstract class AbstractExplorableController extends Controller {

	protected IItemPersistence items;
    protected IExplorerAPI explorer;
    protected EsExplorerFilters explorerFilters;
    protected MetricRegistry metrics;
    protected IRecommendationsAPI recsApi;
    protected IEventService events;
    protected CuisineColours colours;

    public AbstractExplorableController( final EsItemFactory items, final EsExplorerFilters explorerFilters, final MyrrixExplorerService explorer,
    									 final MyrrixRecommendationService inRecService, final MetricRegistry metrics, final IEventService eventService,
    									 final CuisineColours colours) {
        this.items = checkNotNull(items);
        this.explorer = checkNotNull(explorer);
        this.explorerFilters = checkNotNull(explorerFilters);
        this.metrics = checkNotNull(metrics);
        this.recsApi = checkNotNull(inRecService);
        this.events = checkNotNull(eventService);
        this.colours = checkNotNull(colours);
    }

    protected IUser getLocalUser() {
        return /* Yuk! */ PlayAuthUserServicePlugin.getLocalUser( metrics, session());
    }

    protected Collection<IExplorerFilterItem<?>> getExplorerIncludeTags() {
        final IUser currUser = getLocalUser();
        if ( currUser == null) {
            return Collections.emptyList();
        }

        return currUser.getPrefs().getExplorerIncludes();
    }

    protected Collection<IExplorerFilterItem<?>> getExplorerExcludeTags() {
        final IUser currUser = getLocalUser();
        if ( currUser == null) {
            return Collections.emptyList();
        }

        return currUser.getPrefs().getExplorerExcludes();
    }

    public IExplorerFilterDef getExplorerFilter( final EsExplorerFilters inFilters) {
        final IUser currUser = getLocalUser();
        if ( currUser == null) {
            return ExplorerFilterDefs.nullFilter();
        }

        try {
            return new ExplorerFilterDefs().build().includeTags( getExplorerIncludeTags() ).excludeTags( getExplorerExcludeTags() ).toFilterDef();
        }
        catch (Exception e) {
            return ExplorerFilterDefs.nullFilter();
        }
    }
}
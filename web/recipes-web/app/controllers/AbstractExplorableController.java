package controllers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Collections;

import play.mvc.Controller;
import service.PlayAuthUserServicePlugin;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.IUser;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.service.api.IExplorerAPI;
import uk.co.recipes.service.api.IExplorerFilter;
import uk.co.recipes.service.api.IItemPersistence;
import uk.co.recipes.service.api.IRecommendationsAPI;
import uk.co.recipes.service.impl.EsExplorerFilters;
import uk.co.recipes.service.impl.MyrrixExplorerService;
import uk.co.recipes.service.impl.MyrrixRecommendationService;

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

    public AbstractExplorableController( final EsItemFactory items, final EsExplorerFilters explorerFilters, final MyrrixExplorerService explorer, final MyrrixRecommendationService inRecService, final MetricRegistry metrics) {
        this.items = checkNotNull(items);
        this.explorer = checkNotNull(explorer);
        this.explorerFilters = checkNotNull(explorerFilters);
        this.metrics = checkNotNull(metrics);
        this.recsApi = checkNotNull(inRecService);
    }

    protected IUser getLocalUser() {
        return /* Yuk! */ PlayAuthUserServicePlugin.getLocalUser( metrics, session());
    }

    protected Collection<ITag> getExplorerIncludeTags() {
        final IUser currUser = getLocalUser();
        if ( currUser == null) {
            return Collections.emptyList();
        }

        return currUser.getPrefs().getExplorerIncludeTags();
    }

    protected Collection<ITag> getExplorerExcludeTags() {
        final IUser currUser = getLocalUser();
        if ( currUser == null) {
            return Collections.emptyList();
        }

        return currUser.getPrefs().getExplorerExcludeTags();
    }

    public IExplorerFilter getExplorerFilter( final EsExplorerFilters inFilters) {
        final IUser currUser = getLocalUser();
        if ( currUser == null) {
            return EsExplorerFilters.nullFilter();
        }

        try {
            return inFilters.build().includeTags( getExplorerIncludeTags() ).excludeTags( getExplorerExcludeTags() ).toFilter();
        }
        catch (Exception e) {
            return EsExplorerFilters.nullFilter();
        }
    }
}
package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import play.mvc.Controller;
import service.UserProvider;
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

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractExplorableController extends Controller {

	protected IItemPersistence items;
    protected IExplorerAPI explorer;
    protected EsExplorerFilters explorerFilters;
    protected IRecommendationsAPI recsApi;
    protected IEventService events;
    protected CuisineColours colours;
    protected PlayAuthenticate auth;
    protected UserProvider userProvider;

    public AbstractExplorableController(final EsItemFactory items, final EsExplorerFilters explorerFilters, final MyrrixExplorerService explorer,
                                        final MyrrixRecommendationService inRecService, final IEventService eventService,
                                        final CuisineColours colours,
                                        final PlayAuthenticate auth, final UserProvider userProvider) {
        this.items = checkNotNull(items);
        this.explorer = checkNotNull(explorer);
        this.explorerFilters = checkNotNull(explorerFilters);
        this.recsApi = checkNotNull(inRecService);
        this.events = checkNotNull(eventService);
        this.colours = checkNotNull(colours);
        this.auth = checkNotNull(auth);
        this.userProvider = checkNotNull(userProvider);
    }

    protected IUser getLocalUser() {
        return userProvider.getUser(session());
    }

    public IExplorerFilterDef getExplorerFilter( final EsExplorerFilters /* FIXME?? */ inFilters) {
        final IUser currUser = getLocalUser();
        if ( currUser == null) {
            return ExplorerFilterDefs.nullFilter();
        }

        try {
            return ExplorerFilterDefs.forPrefs( currUser.getPrefs() );
        }
        catch (Exception e) {
            return ExplorerFilterDefs.nullFilter();
        }
    }
}
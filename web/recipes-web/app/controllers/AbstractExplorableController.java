package controllers;

import com.codahale.metrics.MetricRegistry;
import java.util.Collection;
import java.util.Collections;
import play.mvc.Controller;
import service.PlayAuthUserServicePlugin;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.IUser;
import uk.co.recipes.service.api.IExplorerFilter;
import uk.co.recipes.service.impl.EsExplorerFilters;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public abstract class AbstractExplorableController extends Controller {

    protected MetricRegistry metrics;

    public AbstractExplorableController( final MetricRegistry metrics) {
        this.metrics = checkNotNull(metrics);
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
package controllers;

import static com.google.common.base.Preconditions.checkNotNull;
import com.codahale.metrics.MetricRegistry;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import play.mvc.Result;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IUser;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.ratings.ItemRating;
import uk.co.recipes.ratings.UserRatings;
import uk.co.recipes.service.impl.EsExplorerFilters;
import uk.co.recipes.service.impl.MyrrixExplorerService;
import com.google.common.base.Optional;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class Items extends AbstractExplorableController {

    private UserRatings ratings;

    @Inject
    public Items( final EsItemFactory items, final EsExplorerFilters explorerFilters, final MyrrixExplorerService inExplorerService, final EsUserFactory users,
                  final UserRatings inRatings, final MetricRegistry metrics) {
        super( items, explorerFilters, inExplorerService, metrics);
        this.ratings = checkNotNull(inRatings);
    }

    public Result display( final String name) throws IOException {
        final Optional<ICanonicalItem> optItem = items.get(name);
        if (!optItem.isPresent()) {
            return notFound("'" + name + "' not found!");
        }

        final ICanonicalItem item = optItem.get();
        final List<ICanonicalItem> similarities = explorer.similarIngredients( item, getExplorerFilter(explorerFilters), 20);
        return ok(views.html.item.render( item, similarities));
    }

    public Result rate( final String name, final int inScore) throws IOException {
        final ICanonicalItem item = items.get(name).get();

        final IUser user1 = getLocalUser();
        if ( user1 == null) {
            return unauthorized("Not logged-in");
        }

		ratings.addRating( user1, new ItemRating( item, inScore) );
  
        return redirect("/items/" + item.getCanonicalName());  // FIXME - horrible way to reload!
    }
}
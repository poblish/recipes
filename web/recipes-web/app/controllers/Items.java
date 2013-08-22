package controllers;

import static com.google.common.base.Preconditions.checkNotNull;
import service.PlayAuthUserServicePlugin;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import play.mvc.Controller;
import play.mvc.Result;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.api.IUser;
import uk.co.recipes.persistence.EsItemFactory;
import uk.co.recipes.persistence.EsUserFactory;
import uk.co.recipes.ratings.ItemRating;
import uk.co.recipes.ratings.UserRatings;
import uk.co.recipes.service.api.IExplorerAPI;
import uk.co.recipes.service.api.IItemPersistence;
import uk.co.recipes.service.impl.MyrrixExplorerService;

/**
 * 
 * TODO
 *
 * @author andrewregan
 *
 */
public class Items extends Controller {

    private IItemPersistence items;
//    private IUserPersistence users;
    private IExplorerAPI explorerService;
    private UserRatings ratings;

    @Inject
    public Items( final EsItemFactory items, final MyrrixExplorerService inExplorerService, final EsUserFactory users, final UserRatings inRatings) {
        this.items = checkNotNull(items);
        this.explorerService = checkNotNull(inExplorerService);
//        this.users = checkNotNull(users);
        this.ratings = checkNotNull(inRatings);
    }

    public Result display( final String name) throws IOException {
        final ICanonicalItem item = items.get(name).get();
        final List<ICanonicalItem> similarities = explorerService.similarIngredients( item, 20);
        return ok(views.html.item.render( item, similarities, ( item != null) ? "Found" : "Not Found"));
    }

    public Result rate( final String name, final int inScore) throws IOException {
        final ICanonicalItem item = items.get(name).get();

        final IUser user1 = /* Yuk! */ PlayAuthUserServicePlugin.getLocalUser( session() );
        if ( user1 == null) {
            return unauthorized("Not logged-in");
        }

		ratings.addRating( user1, new ItemRating( item, inScore) );
  
        return redirect("/items/" + item.getCanonicalName());  // FIXME - horrible way to reload!
    }
}
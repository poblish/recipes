package controllers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import play.mvc.Controller;
import play.mvc.Result;
import uk.co.recipes.api.ICanonicalItem;
import uk.co.recipes.persistence.EsItemFactory;
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
    private IExplorerAPI explorerService;

    @Inject
    public Items( final EsItemFactory items, final MyrrixExplorerService inExplorerService) {
        this.items = checkNotNull(items);
        this.explorerService = checkNotNull(inExplorerService);
    }

    public Result display( final String name) throws IOException {
        final ICanonicalItem item = items.get(name).get();
        final List<ICanonicalItem> similarities = explorerService.similarIngredients( item, 20);
        return ok(views.html.item.render( item, similarities, ( item != null) ? "Found" : "Not Found"));
    }

    public Result rate( final String name, final int inScore) throws IOException {
        return display(name);
    }
}
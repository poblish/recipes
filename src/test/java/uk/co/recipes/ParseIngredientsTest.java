package uk.co.recipes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import uk.co.recipes.parse.IngredientParser;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class ParseIngredientsTest {

	@Test
	public void parseIngredients() throws IOException {
		final List<Ingredient> allIngredients = Lists.newArrayList();

		for ( String eachLine : Files.readLines( new File("src/test/resources/ingredients/inputs.txt"), Charset.forName("utf-8"))) {
			final Optional<Ingredient> theIngr = IngredientParser.parse(eachLine);
			if (theIngr.isPresent()) {
				allIngredients.add( theIngr.get() );
			}
			else {
				Assert.fail(eachLine + " not matched");
			}
		}

		assertThat( allIngredients.toString(), is("[Ingredient{q=1 TBSP, item=NamedItem{name=sunflower oil, canonical=CanonicalItem{name=sunflower oil}}}, Ingredient{q=200 GRAMMES, item=NamedItem{name=smoked streaky bacon, canonical=CanonicalItem{name=smoked streaky bacon}}, notes={en=preferably in one piece, skinned and cut into pieces}}, Ingredient{q=900 GRAMMES, item=NamedItem{name=lamb neck fillets, canonical=CanonicalItem{name=lamb neck fillets}}, notes={en=cut into large chunks}}, Ingredient{q=350 GRAMMES, item=NamedItem{name=baby onions, canonical=CanonicalItem{name=baby onions}}, notes={en=peeled}}, Ingredient{q=5, item=NamedItem{name=carrot, canonical=CanonicalItem{name=carrot}}, notes={en=cut into large chunks}}, Ingredient{q=350 GRAMMES, item=NamedItem{name=small button mushrooms, canonical=CanonicalItem{name=small button mushrooms}}}, Ingredient{q=3 TBSP, item=NamedItem{name=plain flour, canonical=CanonicalItem{name=plain flour}}}, Ingredient{q=3, item=NamedItem{name=bay leaves, canonical=CanonicalItem{name=bay leaves}}}, Ingredient{q=SMALL BUNCHES, item=NamedItem{name=thyme, canonical=CanonicalItem{name=thyme}}}, Ingredient{q=350 ML, item=NamedItem{name=red wine, canonical=CanonicalItem{name=red wine}}}, Ingredient{q=350 ML, item=NamedItem{name=lamb or beef stock, canonical=CanonicalItem{name=lamb or beef stock}}}, Ingredient{q=LARGE SPLASHES, item=NamedItem{name=Worcestershire sauce, canonical=CanonicalItem{name=Worcestershire sauce}}}, Ingredient{q=350 GRAMMES, item=NamedItem{name=self-raising flour, canonical=CanonicalItem{name=self-raising flour}}}, Ingredient{q=4 TBSP, item=NamedItem{name=chopped mixed herbs, canonical=CanonicalItem{name=chopped mixed herbs}}, notes={en=including thyme, rosemary and parsley}}, Ingredient{q=200 GRAMMES, item=NamedItem{name=chilled butter, canonical=CanonicalItem{name=chilled butter}}, notes={en=grated}}, Ingredient{q=1, item=NamedItem{name=lemon, canonical=CanonicalItem{name=lemon}}, notes={en=Juice of}}, Ingredient{q=5, item=NamedItem{name=bay leaves, canonical=CanonicalItem{name=bay leaves}}}, Ingredient{q=1, item=NamedItem{name=beaten egg, canonical=CanonicalItem{name=beaten egg}}, notes={en=to glaze}}]"));
	}
}

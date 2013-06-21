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
import uk.co.recipes.persistence.JacksonFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class ParseIngredientsTest {

	@Test
	public void parseIngredients1() throws IOException {
		final List<Ingredient> allIngredients = parseIngredientsFrom("inputs.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=1 TBSP, item=NamedItem{name=Sunflower Oil, canonical=CanonicalItem{name=Sunflower Oil, tags={OIL=true}}}}, Ingredient{q=200 GRAMMES, item=NamedItem{name=smoked streaky bacon, canonical=CanonicalItem{name=smoked streaky bacon}}, notes={en=preferably in one piece, skinned and cut into pieces}}, Ingredient{q=900 GRAMMES, item=NamedItem{name=lamb neck fillets, canonical=CanonicalItem{name=lamb neck fillets}}, notes={en=cut into large chunks}}, Ingredient{q=350 GRAMMES, item=NamedItem{name=baby onions, canonical=CanonicalItem{name=baby onions}}, notes={en=peeled}}, Ingredient{q=5, item=NamedItem{name=carrot, canonical=CanonicalItem{name=carrot}}, notes={en=cut into large chunks}}, Ingredient{q=350 GRAMMES, item=NamedItem{name=small button mushrooms, canonical=CanonicalItem{name=small button mushrooms}}}, Ingredient{q=3 TBSP, item=NamedItem{name=plain flour, canonical=CanonicalItem{name=plain flour}}}, Ingredient{q=3, item=NamedItem{name=bay leaves, canonical=CanonicalItem{name=bay leaves}}}, Ingredient{q=SMALL BUNCHES, item=NamedItem{name=thyme, canonical=CanonicalItem{name=thyme}}}, Ingredient{q=350 ML, item=NamedItem{name=red wine, canonical=CanonicalItem{name=red wine}}}, Ingredient{q=350 ML, item=NamedItem{name=lamb or beef stock, canonical=CanonicalItem{name=lamb or beef stock}}}, Ingredient{q=LARGE SPLASHES, item=NamedItem{name=Worcestershire sauce, canonical=CanonicalItem{name=Worcestershire sauce}}}, Ingredient{q=350 GRAMMES, item=NamedItem{name=self-raising flour, canonical=CanonicalItem{name=self-raising flour}}}, Ingredient{q=4 TBSP, item=NamedItem{name=chopped mixed herbs, canonical=CanonicalItem{name=chopped mixed herbs}}, notes={en=including thyme, rosemary and parsley}}, Ingredient{q=200 GRAMMES, item=NamedItem{name=chilled butter, canonical=CanonicalItem{name=chilled butter}}, notes={en=grated}}, Ingredient{q=1, item=NamedItem{name=lemon, canonical=CanonicalItem{name=lemon}}, notes={en=Juice of}}, Ingredient{q=5, item=NamedItem{name=bay leaves, canonical=CanonicalItem{name=bay leaves}}}, Ingredient{q=1, item=NamedItem{name=beaten egg, canonical=CanonicalItem{name=beaten egg}}, notes={en=to glaze}}]"));
	}

	@Test
	public void parseIngredients2() throws IOException {
		final List<Ingredient> allIngredients = parseIngredientsFrom("inputs2.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=300 GRAMMES, item=NamedItem{name=fresh gnocchi, canonical=CanonicalItem{name=fresh gnocchi}}}, Ingredient{q=1 TBSP, item=NamedItem{name=olive oil, canonical=CanonicalItem{name=olive oil, tags={OIL=true}}}}, Ingredient{q=1, item=NamedItem{name=red chilli, canonical=CanonicalItem{name=red chilli}}, notes={en=sliced, deseeded if you like}}, Ingredient{q=1, item=NamedItem{name=medium courgette, canonical=CanonicalItem{name=medium courgette}}, notes={en=cut into thin ribbons with a peeler}}, Ingredient{q=4, item=NamedItem{name=spring onions, canonical=CanonicalItem{name=spring onions}}, notes={en=chopped}}, Ingredient{q=1, item=NamedItem{name=lemon, canonical=CanonicalItem{name=lemon}}, notes={en=Juice of}}, Ingredient{q=2 HEAPED_TBSP, item=NamedItem{name=mascarpone, canonical=CanonicalItem{name=mascarpone}}}, Ingredient{q=50 GRAMMES, item=NamedItem{name=parmesan, canonical=CanonicalItem{name=parmesan}}, notes={en=(or vegetarian alternative), grated}}, Ingredient{q=Some, item=NamedItem{name=dressed mixed leaves, canonical=CanonicalItem{name=dressed mixed leaves}}, notes={en=to serve}}]"));
	}

	@Test
	public void parseIngredients3() throws IOException {
		final List<Ingredient> allIngredients = parseIngredientsFrom("inputs3.txt");
		assertThat( allIngredients.toString(), is("[Ingredient{q=1, item=NamedItem{name=large onion, canonical=CanonicalItem{name=large onion}}}, Ingredient{q=6, item=NamedItem{name=garlic cloves, canonical=CanonicalItem{name=garlic cloves}}, notes={en=roughly chopped}}, Ingredient{q=50 GRAMMES, item=NamedItem{name=ginger, canonical=CanonicalItem{name=ginger}}, notes={en=roughly chopped}}, Ingredient{q=4 TBSP, item=NamedItem{name=vegetable oil, canonical=CanonicalItem{name=vegetable oil, tags={OIL=true}}}}, Ingredient{q=2 TSP, item=NamedItem{name=cumin seeds, canonical=CanonicalItem{name=cumin seeds, tags={SPICE=true}}}}, Ingredient{q=1 TSP, item=NamedItem{name=fennel seed, canonical=CanonicalItem{name=fennel seed, tags={SPICE=true}}}}, Ingredient{q=5 CM, item=NamedItem{name=cinnamon stick, canonical=CanonicalItem{name=cinnamon stick}}}, Ingredient{q=1 TSP, item=NamedItem{name=chilli flakes, canonical=CanonicalItem{name=chilli flakes}}}, Ingredient{q=1 TSP, item=NamedItem{name=garam masala, canonical=CanonicalItem{name=garam masala}}}, Ingredient{q=1 TSP, item=NamedItem{name=turmeric, canonical=CanonicalItem{name=turmeric}}}, Ingredient{q=1 TSP, item=NamedItem{name=caster sugar, canonical=CanonicalItem{name=caster sugar}}}, Ingredient{q=400 GRAMMES, item=NamedItem{name=can chopped tomatoes, canonical=CanonicalItem{name=can chopped tomatoes}}}, Ingredient{q=8, item=NamedItem{name=chicken thighs, canonical=CanonicalItem{name=chicken thighs}}, notes={en=skinned, boneless (about 800g)}}, Ingredient{q=250 ML, item=NamedItem{name=hot chicken stock, canonical=CanonicalItem{name=hot chicken stock}}}, Ingredient{q=2 TBSP, item=NamedItem{name=chopped coriander, canonical=CanonicalItem{name=chopped coriander}}}]"));
	}

	private List<Ingredient> parseIngredientsFrom( final String inFilename) throws IOException {
		final List<Ingredient> allIngredients = Lists.newArrayList();

		for ( String eachLine : Files.readLines( new File("src/test/resources/ingredients/" + inFilename), Charset.forName("utf-8"))) {
			final Optional<Ingredient> theIngr = IngredientParser.parse(eachLine);
			if (theIngr.isPresent()) {

				System.out.println( JacksonFactory.getMapper().writeValueAsString( theIngr.get() ) );

				allIngredients.add( theIngr.get() );
			}
			else {
				Assert.fail(eachLine + " not matched");
			}
		}

		return allIngredients;
	}
}

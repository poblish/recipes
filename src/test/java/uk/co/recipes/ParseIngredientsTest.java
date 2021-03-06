package uk.co.recipes;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;
import com.google.common.io.Files;
import dagger.Component;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.client.Client;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.co.recipes.api.IIngredient;
import uk.co.recipes.api.ITag;
import uk.co.recipes.api.IUser;
import uk.co.recipes.api.Units;
import uk.co.recipes.cats.Categorisation;
import uk.co.recipes.myrrix.MyrrixUpdater;
import uk.co.recipes.parse.DeferralStatus;
import uk.co.recipes.parse.IDeferredIngredientHandler;
import uk.co.recipes.parse.IngredientParser;
import uk.co.recipes.persistence.*;
import uk.co.recipes.tags.FlavourTags;
import uk.co.recipes.tags.NationalCuisineTags;
import uk.co.recipes.test.TestDataUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

/**
 * TODO
 *
 * @author andrewregan
 */
public class ParseIngredientsTest {

    @Inject
    EsUserFactory userFactory;
    @Inject
    EsItemFactory itemFactory;
    @Inject
    EsRecipeFactory recipeFactory;
    @Inject
    EsSequenceFactory sequenceFactory;

    @Inject
    Client esClient;
    @Inject
    ItemsLoader loader;
    @Inject
    TestDataUtils dataUtils;
    @Inject
    MyrrixUpdater updater;
    @Inject
    MetricRegistry metrics;
    @Inject
    IngredientParser parser;

    private static IUser ADMIN_USER;

    @BeforeClass
    public void cleanIndices() throws IOException {
        DaggerParseIngredientsTest_TestComponent.create().inject(this);

        itemFactory.deleteAll();
        recipeFactory.deleteAll();

        ADMIN_USER = userFactory.adminUser();
    }

    @BeforeClass
    public void loadIngredientsFromYaml() throws InterruptedException, IOException {
        loader.load();
    }

    @Test
    public void testCompleteParse() {
        testCompleteParse("415g can refried beans (we used Discovery)", "Ingredient{q=415 GRAMMES, item=CanonicalItem{name=Refried beans, tags={MEXICAN=true, PULSE=true, USA=true}}, notes={en=[(we used Discovery)]}}");
    }

    @Test
    public void testCompleteParseWithHalfAReplaced() {
        testCompleteParse("½ x 100g bag watercress", "Ingredient{q=50 GRAMMES, item=CanonicalItem{name=Watercress, tags={VEGETABLE=true}}}");
        testCompleteParse("½ cucumber", "Ingredient{q=0.5, item=CanonicalItem{name=Cucumber, tags={VEGETABLE=true}}}");
        testCompleteParse("½ 20g pack tarragon", "Ingredient{q=10 GRAMMES, item=CanonicalItem{name=Tarragon, tags={GRASSY=true, HERB=true}}}");
        testCompleteParse("half 22g bunch coriander", "Ingredient{q=11 GRAMMES, item=CanonicalItem{name=Coriander, tags={GRASSY=true, HERB=true}}}");
        testCompleteParse("Half an 802g can mixed pulses", "Ingredient{q=401 GRAMMES, item=CanonicalItem{name=mixed pulses}}");
        testCompleteParse("0.5 x 400g can mixed pulses", "Ingredient{q=200 GRAMMES, item=CanonicalItem{name=mixed pulses}}");
    }

    private void testCompleteParse(final String inInput, final String inExpectedToString) {
        assertThat(inInput + " not parsed!", parser.parse(inInput, ingr -> assertThat(ingr.toString(), is(inExpectedToString)), new DummyDeferralHandler()), is(true));
    }

    @Test
    public void testQuantityParsing() {
        assertThat(parser.parseQuantity("150g tub").get(), is(new Quantity(Units.GRAMMES, 150)));
        assertThat(parser.parseQuantity("150 g tub").get(), is(new Quantity(Units.GRAMMES, 150)));
        assertThat(parser.parseQuantity("150 g punnet").get(), is(new Quantity(Units.GRAMMES, 150)));
        assertThat(parser.parseQuantity("150ml tub").get(), is(new Quantity(Units.ML, 150)));
        assertThat(parser.parseQuantity("150 ml tub").get(), is(new Quantity(Units.ML, 150)));
        assertThat(parser.parseQuantity("2kg tub").get(), is(new Quantity(Units.KG, 2)));
        assertThat(parser.parseQuantity("2 kg tub").get(), is(new Quantity(Units.KG, 2)));
        assertThat(parser.parseQuantity("5 heaped tsps").get(), is(new Quantity(Units.HEAPED_TSP, 5)));

        assertThat(parser.parseQuantity("pinch").get(), is(new Quantity(Units.PINCH, 1)));
        assertThat(parser.parseQuantity("3 pinches").get(), is(new Quantity(Units.PINCH, 3)));
        assertThat(parser.parseQuantity("big pinch").get(), is(new Quantity(Units.BIG_PINCH, 1)));

        assertThat(parser.parseQuantity("1 bottle").get(), is(new Quantity(Units.BOTTLE, 1)));
        assertThat(parser.parseQuantity("bunch").get(), is(new Quantity(Units.BUNCHES, 1)));
        assertThat(parser.parseQuantity("big bunch").get(), is(new Quantity(Units.BIG_BUNCHES, 1)));
        assertThat(parser.parseQuantity("½ large bunch").get(), is(new Quantity(Units.BIG_BUNCHES, 0.5)));
        assertThat(parser.parseQuantity("3 small bunches").get(), is(new Quantity(Units.SMALL_BUNCHES, 3)));
        assertThat(parser.parseQuantity("500g pack").get(), is(new Quantity(Units.GRAMMES, 500)));
        assertThat(parser.parseQuantity("500ml pack").get(), is(new Quantity(Units.ML, 500)));
        assertThat(parser.parseQuantity("2kg pack").get(), is(new Quantity(Units.KG, 2)));
//        assertThat( parser.parseQuantity("500g bunch").get(), is( new Quantity( Units.BUNCHES, 1) ));   FIXME This should pass!!!

        assertThat(parser.parseQuantity("5 tbsp").get(), is(new Quantity(Units.TBSP, 5)));
        assertThat(parser.parseQuantity("7 pots").get(), is(new Quantity(Units.POT, 7)));
        assertThat(parser.parseQuantity("198g pot").get(), is(new Quantity(Units.GRAMMES, 198)));
        assertThat(parser.parseQuantity("398g jar").get(), is(new Quantity(Units.GRAMMES, 398)));
        assertThat(parser.parseQuantity("20g sachet").get(), is(new Quantity(Units.GRAMMES, 20)));
        assertThat(parser.parseQuantity("601g bag").get(), is(new Quantity(Units.GRAMMES, 601)));

        assertThat(parser.parseQuantity("about 397g").get(), is(new Quantity(Units.GRAMMES, 397)));
        assertThat(parser.parseQuantity("About 7 tsp").get(), is(new Quantity(Units.TSP, 7)));

        assertThat(parser.parseQuantity("1 x 400g tub").get(), is(new Quantity(Units.GRAMMES, 400)));
        assertThat(parser.parseQuantity("2 x 400g tubs").get(), is(new Quantity(Units.GRAMMES, 800)));
        assertThat(parser.parseQuantity("½ x 300g jar").get(), is(new Quantity(Units.GRAMMES, 150)));
        assertThat(parser.parseQuantity("½ x 400g tin").get(), is(new Quantity(Units.GRAMMES, 200)));
        assertThat(parser.parseQuantity("½ x 400g tins").get(), is(new Quantity(Units.GRAMMES, 200)));
        assertThat(parser.parseQuantity("½ x 500g can").get(), is(new Quantity(Units.GRAMMES, 250)));
        assertThat(parser.parseQuantity("½ x 500g cans").get(), is(new Quantity(Units.GRAMMES, 250)));
        assertThat(parser.parseQuantity("2 x 300g pack").get(), is(new Quantity(Units.GRAMMES, 600)));
        assertThat(parser.parseQuantity("3 x 300g packs").get(), is(new Quantity(Units.GRAMMES, 900)));

        assertThat(parser.parseQuantity("3 level tsp").get(), is(new Quantity(Units.TSP, 3)));
        assertThat(parser.parseQuantity("level tsp").get(), is(new Quantity(Units.TSP, 1)));
        assertThat(parser.parseQuantity("level tbsp").get(), is(new Quantity(Units.TBSP, 1)));
    }

    @Test()
    public void testBadQuantities() {
        assertThat(parser.parseQuantity("x 900g tub").isPresent(), is(false));
        assertThat(parser.parseQuantity("900 tub").isPresent(), is(false));
    }

    @Test
    public void testItemNameParsing() {
        assertThat(parser.parseItemName("Coriander Seeds"), is(true));
        assertThat(parser.parseItemName("Puréed Lung"), is(true));
        assertThat(parser.parseItemName("Goat's Cheese"), is(true));
        assertThat(parser.parseItemName("Green & Black's white chocolate"), is(true));
        assertThat(parser.parseItemName("Green & Black's white chocolate"), is(true));
        assertThat(parser.parseItemName("boneless, skinless chicken breasts"), is(true));
        assertThat(parser.parseItemName("skinless, boneless chicken breasts"), is(true));
        assertThat(parser.parseItemName("Large free-range eggs"), is(true));
        assertThat(parser.parseItemName("Large, free-range eggs"), is(true));
        assertThat(parser.parseItemName("Small, free-range eggs"), is(true));
        assertThat(parser.parseItemName("Unknownprefix, free-range eggs"), is(false));
        assertThat(parser.parseItemName("1855 Cabernet Wine"), is(false));  // *Should* work, but we're not ready to deal with numbers mixed up with names (yet)
        assertThat(parser.parseItemName("70% plain chocolate"), is(false));  // *Should* work, but we're not ready to deal with numbers mixed up with names (yet)

//        assertThat( parser.parseItemName("1 Cup/300ml Stock"), is(true));  // Won't work, as these have Q + name!
//        assertThat( parser.parseItemName("2 Cups/600ml Stock"), is(true));
//        assertThat( parser.parseItemName("4 Cups/1200ml Stock"), is(true));
    }

    @Test
    public void testAutocompleteAnalyzer1() {
        // See: http://jontai.me/blog/2013/02/adding-autocomplete-to-an-elasticsearch-search-application/
        final AnalyzeResponse resp = esClient.admin().indices().prepareAnalyze("recipe", "red onion").setAnalyzer("autocomplete").execute().actionGet();
        final Collection<String> strs = FluentIterable.from(resp.getTokens()).transform(EsUtils.getAnalyzeTokenToStringFunc()).toSortedList(Ordering.natural());
        assertThat(strs.toString(), is("[ion, nio, nion, oni, onio, onion, red]"));
    }

    @Test
    public void testAutocompleteAnalyzer2() {
        // See: http://jontai.me/blog/2013/02/adding-autocomplete-to-an-elasticsearch-search-application/
        final AnalyzeResponse resp = esClient.admin().indices().prepareAnalyze("recipe", "chCashBlackSpiceCurry.txt").setAnalyzer("autocomplete").execute().actionGet();
        final Collection<String> strs = FluentIterable.from(resp.getTokens()).transform(EsUtils.getAnalyzeTokenToStringFunc()).toSortedList(Ordering.natural());
        assertThat(strs.toString(), is("[.tx, .txt, ack, acks, acksp, ackspi, ackspic, ackspice, ackspicec, ackspicecu, ackspicecur, ackspicecurr, ackspicecurry, ackspicecurry., ackspicecurry.t, ash, ashb, ashbl, ashbla, ashblac, ashblack, ashblacks, ashblacksp, ashblackspi, ashblackspic, ashblackspice, ashblackspicec, ashblackspicecu, bla, blac, black, blacks, blacksp, blackspi, blackspic, blackspice, blackspicec, blackspicecu, blackspicecur, blackspicecurr, blackspicecurry, cas, cash, cashb, cashbl, cashbla, cashblac, cashblack, cashblacks, cashblacksp, cashblackspi, cashblackspic, cashblackspice, cashblackspicec, cec, cecu, cecur, cecurr, cecurry, cecurry., cecurry.t, cecurry.tx, cecurry.txt, chc, chca, chcas, chcash, chcashb, chcashbl, chcashbla, chcashblac, chcashblack, chcashblacks, chcashblacksp, chcashblackspi, chcashblackspic, cks, cksp, ckspi, ckspic, ckspice, ckspicec, ckspicecu, ckspicecur, ckspicecurr, ckspicecurry, ckspicecurry., ckspicecurry.t, ckspicecurry.tx, cur, curr, curry, curry., curry.t, curry.tx, curry.txt, ecu, ecur, ecurr, ecurry, ecurry., ecurry.t, ecurry.tx, ecurry.txt, hbl, hbla, hblac, hblack, hblacks, hblacksp, hblackspi, hblackspic, hblackspice, hblackspicec, hblackspicecu, hblackspicecur, hblackspicecurr, hca, hcas, hcash, hcashb, hcashbl, hcashbla, hcashblac, hcashblack, hcashblacks, hcashblacksp, hcashblackspi, hcashblackspic, hcashblackspice, ice, icec, icecu, icecur, icecurr, icecurry, icecurry., icecurry.t, icecurry.tx, icecurry.txt, ksp, kspi, kspic, kspice, kspicec, kspicecu, kspicecur, kspicecurr, kspicecurry, kspicecurry., kspicecurry.t, kspicecurry.tx, kspicecurry.txt, lac, lack, lacks, lacksp, lackspi, lackspic, lackspice, lackspicec, lackspicecu, lackspicecur, lackspicecurr, lackspicecurry, lackspicecurry., pic, pice, picec, picecu, picecur, picecurr, picecurry, picecurry., picecurry.t, picecurry.tx, picecurry.txt, rry, rry., rry.t, rry.tx, rry.txt, ry., ry.t, ry.tx, ry.txt, shb, shbl, shbla, shblac, shblack, shblacks, shblacksp, shblackspi, shblackspic, shblackspice, shblackspicec, shblackspicecu, shblackspicecur, spi, spic, spice, spicec, spicecu, spicecur, spicecurr, spicecurry, spicecurry., spicecurry.t, spicecurry.tx, spicecurry.txt, txt, urr, urry, urry., urry.t, urry.tx, urry.txt, y.t, y.tx, y.txt]"));
    }

    @Test
    public void parseIngredientsBulk() throws IOException {
        final List<IIngredient> allIngredients = dataUtils.parseIngredientsFrom(ADMIN_USER, "bulk.txt");
        assertThat(allIngredients.toString(), is("[Ingredient{q=500 GRAMMES, item=CanonicalItem{name=Potato, tags={EARTHY=true, VEGETABLE=true}}, notes={en=[cut into chunks]}}, Ingredient{q=85 GRAMMES, item=CanonicalItem{name=Broccoli, tags={VEGETABLE=true}}, notes={en=[cut into small florets]}}, Ingredient{q=2, item=CanonicalItem{name=Salmon Fillet, parent=CanonicalItem{name=Salmon, parent=CanonicalItem{name=Oily Fish, tags={FISH=true, SEAFOOD=true}}, tags={FISH=true, SEAFOOD=true}}, tags={FISH=true, SEAFOOD=true}}, notes={en=[pack of]}}, Ingredient{q=1, item=CanonicalItem{name=Lemon, tags={CITRUS=true, FRUIT=true, LEMON=true, TART=true}}, notes={en=[juice]}}, Ingredient{q=1 SMALL_BUNCHES, item=CanonicalItem{name=Dill, tags={GRASSY=true, HERB=true}}, notes={en=[chopped]}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Sunflower Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={FAT=true, OIL=true}}, tags={FAT=true, OIL=true}}, tags={FAT=true, OIL=true}}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Dijon Mustard, parent=CanonicalItem{name=Mustard, tags={HOT=true, MUSTARDY=true, SAUCE=true}}, tags={HOT=true, MUSTARDY=true, SAUCE=true}}}, Ingredient{q=1, item=CanonicalItem{name=Avocado, tags={CREAMY=true, FRUIT=true, MEXICAN=true}}, notes={en=[peeled, stoned and roughly chopped]}}, Ingredient{q=100 GRAMMES, item=CanonicalItem{name=Cherry Tomatoes, parent=CanonicalItem{name=Tomato, tags={UMAMI=true, VEGETABLE=true}}, tags={UMAMI=true, VEGETABLE=true}}, notes={en=[halved]}}, Ingredient{q=100 GRAMMES, item=CanonicalItem{name=Watercress, tags={VEGETABLE=true}}}, Ingredient{q=SMALL PIECE, item=CanonicalItem{name=Ginger, tags={CHINESE=true, CITRUS=true, INDIAN=true, PEPPERY=true, SPICE=true, SPICY=true, WARM=true}}}, Ingredient{q=2, item=CanonicalItem{name=Garlic Cloves, tags={GARLIC=true, VEGETABLE=true}}}, Ingredient{q=1, item=CanonicalItem{name=Lime, tags={CITRUS=true, FRUIT=true, LIME=true, TROPICAL=true}}, notes={en=[zest and juice]}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Clear Honey, parent=CanonicalItem{name=Honey, tags={CONDIMENT=true}}, tags={CONDIMENT=true}}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Soy Sauce, tags={CHINESE=true, SAUCE=true, UMAMI=true}}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Mild Curry Powder, parent=CanonicalItem{name=Curry Powder, tags={INDIAN=true, SOUTH_ASIAN=true, SPICE=true}}, tags={INDIAN=true, SOUTH_ASIAN=true, SPICE=true}}}, Ingredient{q=3 TBSP, item=CanonicalItem{name=Smooth Peanut Butter, parent=CanonicalItem{name=Peanut Butter, parent=CanonicalItem{name=Peanuts, tags={NUT=true, SWEET_NUTTY=true}}, tags={CONDIMENT=true, NUT=true, SWEET_NUTTY=true}}, tags={CONDIMENT=true, NUT=true, SWEET_NUTTY=true}}}, Ingredient{q=500 GRAMMES, item=CanonicalItem{name=Chicken Breast Fillets, parent=CanonicalItem{name=Chicken Breast, parent=CanonicalItem{name=Chicken, tags={MEAT=true, POULTRY=true, WHITE_MEAT=true}}, tags={MEAT=true, POULTRY=true, WHITE_MEAT=true}}, tags={MEAT=true, POULTRY=true, WHITE_MEAT=true}}, notes={en=[skinless]}}, Ingredient{q=165 ML, item=CanonicalItem{name=Coconut Milk, parent=CanonicalItem{name=Coconut, tags={ASIAN=true, CREAMY=true, FRUIT=true, NUT=true, TROPICAL=true}}, tags={ASIAN=true, CREAMY=true, FRUIT=true, NUT=true, TROPICAL=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={FAT=true, OIL=true}}, tags={FAT=true, OIL=true}}}, Ingredient{q=1, item=CanonicalItem{name=Cucumber, tags={VEGETABLE=true}}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=White Wine Vinegar, parent=CanonicalItem{name=Vinegar, tags={VINEGAR=true}}, tags={VINEGAR=true}}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Golden Caster Sugar, parent=CanonicalItem{name=Sugar, tags={BAKING=true, SUGAR=true}}, tags={BAKING=true, SUGAR=true}}}, Ingredient{q=Some, item=CanonicalItem{name=Sweet Chilli Sauce, parent=CanonicalItem{name=Chilli Sauce, tags={CHILLI=true, SAUCE=true}}, tags={CHILLI=true, SAUCE=true}}}, Ingredient{q=1 BUNCHES, item=CanonicalItem{name=Coriander, tags={GRASSY=true, HERB=true}}, notes={en=[leaves picked (optional)]}}, Ingredient{q=7, item=CanonicalItem{name=Eggs, tags={EGG=true}}, notes={en=[separated, large]}}, Ingredient{q=150 GRAMMES, item=CanonicalItem{name=Caster Sugar, parent=CanonicalItem{name=Sugar, tags={BAKING=true, SUGAR=true}}, tags={BAKING=true, SUGAR=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Vanilla Essence, parent=CanonicalItem{name=Vanilla, tags={FLAVOURING=true}}, tags={BAKING=true, FLAVOURING=true}}}, Ingredient{q=150 GRAMMES, item=CanonicalItem{name=Plain Flour, parent=CanonicalItem{name=White Flour, parent=CanonicalItem{name=Flour, tags={FLOUR=true}}, tags={FLOUR=true}}, tags={FLOUR=true}}, notes={en=[sifted]}}, Ingredient{q=125 GRAMMES, item=CanonicalItem{name=Caster Sugar, parent=CanonicalItem{name=Sugar, tags={BAKING=true, SUGAR=true}}, tags={BAKING=true, SUGAR=true}}}, Ingredient{q=100 GRAMMES, item=CanonicalItem{name=Hazelnuts, tags={NUT=true}}}, Ingredient{q=125 GRAMMES, item=CanonicalItem{name=Dark Chocolate, parent=CanonicalItem{name=Chocolate, tags={SWEET=true}}, tags={DARK_RICH=true, SWEET=true}}}, Ingredient{q=6, item=CanonicalItem{name=Egg Yolks, parent=CanonicalItem{name=Eggs, tags={EGG=true}}, tags={BAKING=true, EGG=true}}, notes={en=[large]}}, Ingredient{q=125 GRAMMES, item=CanonicalItem{name=Caster Sugar, parent=CanonicalItem{name=Sugar, tags={BAKING=true, SUGAR=true}}, tags={BAKING=true, SUGAR=true}}}, Ingredient{q=225 GRAMMES, item=CanonicalItem{name=Unsalted Butter, parent=CanonicalItem{name=Butter, tags={DAIRY=true, FAT=true}}, tags={DAIRY=true, FAT=true}}, notes={en=[softened]}}, Ingredient{q=300 GRAMMES, item=CanonicalItem{name=Dark Chocolate, parent=CanonicalItem{name=Chocolate, tags={SWEET=true}}, tags={DARK_RICH=true, SWEET=true}}}, Ingredient{q=100 GRAMMES, item=CanonicalItem{name=Unsalted Butter, parent=CanonicalItem{name=Butter, tags={DAIRY=true, FAT=true}}, tags={DAIRY=true, FAT=true}}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Double Cream, parent=CanonicalItem{name=Cream, tags={CREAMY=true, DAIRY=true}}, tags={CREAMY=true, DAIRY=true}}}, Ingredient{q=100 GRAMMES, item=CanonicalItem{name=Brown Rice Noodles, parent=CanonicalItem{name=Rice Noodles, parent=CanonicalItem{name=Noodles, tags={NOODLES=true}}, tags={EGG=true, NOODLES=true, RICE=true}}, tags={EGG=true, NOODLES=true, RICE=true}}}, Ingredient{q=500 ML, item=CanonicalItem{name=Chicken Stock, parent=CanonicalItem{name=Stock, tags={SAUCE=true, STOCK=true}}, tags={MEAT=true, POULTRY=true, SAUCE=true, STOCK=true}}}, Ingredient{q=500 ML, item=CanonicalItem{name=Fish Stock, parent=CanonicalItem{name=Stock, tags={SAUCE=true, STOCK=true}}, tags={FISH=true, SAUCE=true, STOCK=true, UMAMI=true}}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Thai Red Curry Paste, parent=CanonicalItem{name=Curry Paste, tags={SAUCE=true}}, tags={SAUCE=true, THAI=true}}}, Ingredient{q=4, item=CanonicalItem{name=Kaffir Lime leaves, tags={LIME=true}}, notes={en=[dried, or, fresh]}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Fish Sauce, tags={FISH=true, SAUCE=true, THAI=true, VIETNAMESE=true}}}, Ingredient{q=200 GRAMMES, item=CanonicalItem{name=White Fish, tags={FISH=true, SEAFOOD=true}}, notes={en=[such as pollack, skinless, sustainable]}}, Ingredient{q=100 GRAMMES, item=CanonicalItem{name=King Prawns, parent=CanonicalItem{name=Prawns, tags={SEAFOOD=true}}, tags={SEAFOOD=true}}, notes={en=[raw]}}, Ingredient{q=2, item=CanonicalItem{name=Pak Choi, tags={CHINESE=true, VEGETABLE=true}}, notes={en=[leaves separated]}}, Ingredient{q=1 HANDFUL, item=CanonicalItem{name=Coriander, tags={GRASSY=true, HERB=true}}}, Ingredient{q=300 GRAMMES, item=CanonicalItem{name=Unsalted Butter, parent=CanonicalItem{name=Butter, tags={DAIRY=true, FAT=true}}, tags={DAIRY=true, FAT=true}}, notes={en=[at room temperature]}}, Ingredient{q=270 GRAMMES, item=CanonicalItem{name=Self-raising Flour, parent=CanonicalItem{name=Flour, tags={FLOUR=true}}, tags={FLOUR=true}}}, Ingredient{q=1.5 TSP, item=CanonicalItem{name=Baking Powder, tags={BAKING=true}}}, Ingredient{q=300 GRAMMES, item=CanonicalItem{name=Golden Caster Sugar, parent=CanonicalItem{name=Sugar, tags={BAKING=true, SUGAR=true}}, tags={BAKING=true, SUGAR=true}}}, Ingredient{q=6, item=CanonicalItem{name=Eggs, tags={EGG=true}}}, Ingredient{q=4 TBSP, item=CanonicalItem{name=Cocoa, tags={BAKING=true}}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Instant Coffee, parent=CanonicalItem{name=Coffee, tags={COFFEE=true, DARK_RICH=true}}, tags={COFFEE=true, DARK_RICH=true}}, notes={en=[dissolved in 1 tbsp boiling water]}}, Ingredient{q=4 TBSP, item=CanonicalItem{name=Tia Maria, tags={ALCOHOL=true, COFFEE=true, SPIRIT=true}}}, Ingredient{q=250 ML, item=CanonicalItem{name=Mascarpone, tags={CHEESE=true, DAIRY=true, ITALIAN=true}}}, Ingredient{q=568 ML, item=CanonicalItem{name=Double Cream, parent=CanonicalItem{name=Cream, tags={CREAMY=true, DAIRY=true}}, tags={CREAMY=true, DAIRY=true}}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Amaretto, tags={ALCOHOL=true, ALMOND=true, ITALIAN=true, SPIRIT=true}}}, Ingredient{q=50 GRAMMES, item=CanonicalItem{name=Dark Chocolate, parent=CanonicalItem{name=Chocolate, tags={SWEET=true}}, tags={DARK_RICH=true, SWEET=true}}}, Ingredient{q=1 KG, item=CanonicalItem{name=Potato, tags={EARTHY=true, VEGETABLE=true}}, notes={en=[peeled and quartered]}}, Ingredient{q=200 ML, item=CanonicalItem{name=Milk, tags={DAIRY=true}}}, Ingredient{q=50 GRAMMES, item=CanonicalItem{name=Butter, tags={DAIRY=true, FAT=true}}}, Ingredient{q=1.5 TBSP, item=CanonicalItem{name=Wholegrain Mustard, parent=CanonicalItem{name=Mustard, tags={HOT=true, MUSTARDY=true, SAUCE=true}}, tags={HOT=true, MUSTARDY=true, SAUCE=true}}}, Ingredient{q=2 BUNCHES, item=CanonicalItem{name=Spring Onions, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[washed and sliced]}}, Ingredient{q=1.9 KG, item=CanonicalItem{name=Lamb Shoulder, parent=CanonicalItem{name=Lamb, tags={MEAT=true, RED_MEAT=true}}, tags={MEAT=true, RED_MEAT=true}}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Olive Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={FAT=true, OIL=true}}, tags={FAT=true, OIL=true}}, tags={FAT=true, OIL=true}}}, Ingredient{q=3, item=CanonicalItem{name=Oregano, tags={GRASSY=true, HERB=true, ITALIAN=true}}, notes={en=[leaves stripped from 2, sprigs]}}, Ingredient{q=3, item=CanonicalItem{name=Rosemary, tags={HERB=true}}, notes={en=[leaves stripped from 2, sprigs]}}, Ingredient{q=3, item=CanonicalItem{name=Garlic Cloves, tags={GARLIC=true, VEGETABLE=true}}, notes={en=[roughly chopped]}}, Ingredient{q=600 ML, item=CanonicalItem{name=Red Wine, tags={ALCOHOL=true, WINE=true}}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Caster Sugar, parent=CanonicalItem{name=Sugar, tags={BAKING=true, SUGAR=true}}, tags={BAKING=true, SUGAR=true}}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Olive Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={FAT=true, OIL=true}}, tags={FAT=true, OIL=true}}, tags={FAT=true, OIL=true}}}, Ingredient{q=1.75 KG, item=CanonicalItem{name=Pork Shoulder, parent=CanonicalItem{name=Pork, tags={MEAT=true}}, tags={MEAT=true}}, notes={en=[boned and rolled]}}, Ingredient{q=2, item=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, notes={en=[sliced]}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Paprika, tags={HUNGARIAN=true, SMOKY_SALTY=true, SPANISH=true, SPICE=true, WARM=true}}, notes={en=[smoked]}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Caraway Seeds, tags={ANISEED=true, SPICE=true}}}, Ingredient{q=1, item=CanonicalItem{name=Red Chillies, parent=CanonicalItem{name=Chilli, tags={CHILLI=true, HOT=true}}, tags={CHILLI=true, CHINESE=true, HOT=true, INDIAN=true, THAI=true}}, notes={en=[finely chopped]}}, Ingredient{q=400 GRAMMES, item=CanonicalItem{name=Tomato, tags={UMAMI=true, VEGETABLE=true}}, notes={en=[chopped]}}, Ingredient{q=1, item=CanonicalItem{name=Red Pepper, parent=CanonicalItem{name=Peppers, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[deseeded and cut into wedges]}}, Ingredient{q=1.5 KG, item=CanonicalItem{name=Potato, tags={EARTHY=true, VEGETABLE=true}}, notes={en=[cut into quarters]}}, Ingredient{q=1, item=CanonicalItem{name=Cabbage, tags={MUSTARDY=true, VEGETABLE=true}}, notes={en=[finely sliced]}}, Ingredient{q=1, item=CanonicalItem{name=Soured Cream, parent=CanonicalItem{name=Cream, tags={CREAMY=true, DAIRY=true}}, tags={CREAMY=true, DAIRY=true}}, notes={en=[to serve]}}, Ingredient{q=2.5 KG, item=CanonicalItem{name=Pork Loin, parent=CanonicalItem{name=Pork, tags={MEAT=true}}, tags={MEAT=true}}}, Ingredient{q=7, item=CanonicalItem{name=Garlic Cloves, tags={GARLIC=true, VEGETABLE=true}}, notes={en=[peeled and cut into thin slivers]}}, Ingredient{q=7 TBSP, item=CanonicalItem{name=Olive Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={FAT=true, OIL=true}}, tags={FAT=true, OIL=true}}, tags={FAT=true, OIL=true}}}, Ingredient{q=1, item=CanonicalItem{name=Lemon, tags={CITRUS=true, FRUIT=true, LEMON=true, TART=true}}, notes={en=[juice only]}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Fennel Seeds, tags={ANISEED=true, FLORAL=true, SEED=true, SPICE=true}}, notes={en=[dried]}}, Ingredient{q=8, item=CanonicalItem{name=Oregano, tags={GRASSY=true, HERB=true, ITALIAN=true}}, notes={en=[sprigs, fresh]}}, Ingredient{q=300 GRAMMES, item=CanonicalItem{name=Shallot, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}}, Ingredient{q=1, item=CanonicalItem{name=Celeriac, tags={VEGETABLE=true}}, notes={en=[quartered and peeled, large]}}, Ingredient{q=600 ML, item=CanonicalItem{name=Red Wine, tags={ALCOHOL=true, WINE=true}}, notes={en=[; drink the remainder!, full-bodied]}}, Ingredient{q=800 GRAMMES, item=CanonicalItem{name=Chicken, tags={MEAT=true, POULTRY=true, WHITE_MEAT=true}}, notes={en=[breast or dark meat as you prefer, diced]}}, Ingredient{q=300 ML, item=CanonicalItem{name=Chicken Stock, parent=CanonicalItem{name=Stock, tags={SAUCE=true, STOCK=true}}, tags={MEAT=true, POULTRY=true, SAUCE=true, STOCK=true}}}, Ingredient{q=2, item=CanonicalItem{name=White Onions, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[small, chopped, finely]}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Ginger Puree, parent=CanonicalItem{name=Ginger, tags={CHINESE=true, CITRUS=true, INDIAN=true, PEPPERY=true, SPICE=true, SPICY=true, WARM=true}}, tags={CHINESE=true, CITRUS=true, INDIAN=true, PEPPERY=true, SPICE=true, SPICY=true, WARM=true}}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Garlic Puree, parent=CanonicalItem{name=Garlic Cloves, tags={GARLIC=true, VEGETABLE=true}}, tags={GARLIC=true, VEGETABLE=true}}}, Ingredient{q=200 GRAMMES, item=CanonicalItem{name=Ghee, tags={DAIRY=true, FAT=true, INDIAN=true}}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Turmeric, tags={EARTHY=true, INDIAN=true, SPICE=true}}}, Ingredient{q=4 TSP, item=CanonicalItem{name=Mild Curry Powder, parent=CanonicalItem{name=Curry Powder, tags={INDIAN=true, SOUTH_ASIAN=true, SPICE=true}}, tags={INDIAN=true, SOUTH_ASIAN=true, SPICE=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Chilli Powder, tags={INDIAN=true, SPICE=true, WARM=true}}}, Ingredient{q=3 TSP, item=CanonicalItem{name=Garam Masala, tags={INDIAN=true, SPICE=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Mustard Seeds, tags={HOT=true, MUSTARDY=true, SEED=true}}, notes={en=[whole]}}, Ingredient{q=100 ML, item=CanonicalItem{name=Single Cream, parent=CanonicalItem{name=Cream, tags={CREAMY=true, DAIRY=true}}, tags={CREAMY=true, DAIRY=true}}}, Ingredient{q=100 ML, item=CanonicalItem{name=Natural Yogurt, parent=CanonicalItem{name=Yogurt, tags={CREAMY=true, DAIRY=true}}, tags={CREAMY=true, DAIRY=true}}}, Ingredient{q=4 TBSP, item=CanonicalItem{name=Tomato Purée, parent=CanonicalItem{name=Tomato, tags={UMAMI=true, VEGETABLE=true}}, tags={SAUCE=true, UMAMI=true, VEGETABLE=true}}}, Ingredient{q=200 ML, item=CanonicalItem{name=Pureed Onion, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}}, Ingredient{q=Some, item=CanonicalItem{name=Coriander, tags={GRASSY=true, HERB=true}}, notes={en=[to garnish, chopped, fresh]}}, Ingredient{q=800 GRAMMES, item=CanonicalItem{name=Chicken, tags={MEAT=true, POULTRY=true, WHITE_MEAT=true}}, notes={en=[breast or dark meat as you prefer, diced]}}, Ingredient{q=300 ML, item=CanonicalItem{name=Chicken Stock, parent=CanonicalItem{name=Stock, tags={SAUCE=true, STOCK=true}}, tags={MEAT=true, POULTRY=true, SAUCE=true, STOCK=true}}}, Ingredient{q=2, item=CanonicalItem{name=White Onions, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[small, chopped, finely]}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Ginger Puree, parent=CanonicalItem{name=Ginger, tags={CHINESE=true, CITRUS=true, INDIAN=true, PEPPERY=true, SPICE=true, SPICY=true, WARM=true}}, tags={CHINESE=true, CITRUS=true, INDIAN=true, PEPPERY=true, SPICE=true, SPICY=true, WARM=true}}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Garlic Puree, parent=CanonicalItem{name=Garlic Cloves, tags={GARLIC=true, VEGETABLE=true}}, tags={GARLIC=true, VEGETABLE=true}}}, Ingredient{q=200 GRAMMES, item=CanonicalItem{name=Ghee, tags={DAIRY=true, FAT=true, INDIAN=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Turmeric, tags={EARTHY=true, INDIAN=true, SPICE=true}}}, Ingredient{q=4 TSP, item=CanonicalItem{name=Mild Curry Powder, parent=CanonicalItem{name=Curry Powder, tags={INDIAN=true, SOUTH_ASIAN=true, SPICE=true}}, tags={INDIAN=true, SOUTH_ASIAN=true, SPICE=true}}}, Ingredient{q=7 TSP, item=CanonicalItem{name=Chilli Powder, tags={INDIAN=true, SPICE=true, WARM=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Garam Masala, tags={INDIAN=true, SPICE=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Sizzling Seeds, tags={INDIAN=true, SEED=true}}, notes={en=[whole]}}, Ingredient{q=1, item=CanonicalItem{name=Tomato, tags={UMAMI=true, VEGETABLE=true}}, notes={en=[tin, chopped]}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Tomato Purée, parent=CanonicalItem{name=Tomato, tags={UMAMI=true, VEGETABLE=true}}, tags={SAUCE=true, UMAMI=true, VEGETABLE=true}}}, Ingredient{q=300 ML, item=CanonicalItem{name=Pureed Onion, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}}, Ingredient{q=8, item=CanonicalItem{name=Chilli, tags={CHILLI=true, HOT=true}}, notes={en=[sliced lengthways to garnish]}}, Ingredient{q=800 GRAMMES, item=CanonicalItem{name=Prawns, tags={SEAFOOD=true}}, notes={en=[cooked or uncooked, small, peeled]}}, Ingredient{q=2, item=CanonicalItem{name=White Onions, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[small, chopped, finely]}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Ginger Puree, parent=CanonicalItem{name=Ginger, tags={CHINESE=true, CITRUS=true, INDIAN=true, PEPPERY=true, SPICE=true, SPICY=true, WARM=true}}, tags={CHINESE=true, CITRUS=true, INDIAN=true, PEPPERY=true, SPICE=true, SPICY=true, WARM=true}}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Garlic Puree, parent=CanonicalItem{name=Garlic Cloves, tags={GARLIC=true, VEGETABLE=true}}, tags={GARLIC=true, VEGETABLE=true}}}, Ingredient{q=200 GRAMMES, item=CanonicalItem{name=Ghee, tags={DAIRY=true, FAT=true, INDIAN=true}}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Turmeric, tags={EARTHY=true, INDIAN=true, SPICE=true}}}, Ingredient{q=4 TSP, item=CanonicalItem{name=Mild Curry Powder, parent=CanonicalItem{name=Curry Powder, tags={INDIAN=true, SOUTH_ASIAN=true, SPICE=true}}, tags={INDIAN=true, SOUTH_ASIAN=true, SPICE=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Chilli Powder, tags={INDIAN=true, SPICE=true, WARM=true}}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Garam Masala, tags={INDIAN=true, SPICE=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Fennel Seeds, tags={ANISEED=true, FLORAL=true, SEED=true, SPICE=true}}, notes={en=[whole]}}, Ingredient{q=50 ML, item=CanonicalItem{name=Natural Yogurt, parent=CanonicalItem{name=Yogurt, tags={CREAMY=true, DAIRY=true}}, tags={CREAMY=true, DAIRY=true}}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Tomato Purée, parent=CanonicalItem{name=Tomato, tags={UMAMI=true, VEGETABLE=true}}, tags={SAUCE=true, UMAMI=true, VEGETABLE=true}}}, Ingredient{q=200 ML, item=CanonicalItem{name=Pureed Onion, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}}, Ingredient{q=400 ML, item=CanonicalItem{name=Lentils, tags={PULSE=true}}}, Ingredient{q=Some, item=CanonicalItem{name=Coriander, tags={GRASSY=true, HERB=true}}, notes={en=[chopped, fresh, to garnish]}}, Ingredient{q=Some, item=CanonicalItem{name=Rice, tags={RICE=true}}, notes={en=[to serve, cooked]}}, Ingredient{q=Some, item=CanonicalItem{name=Lime Wedge, parent=CanonicalItem{name=Lime, tags={CITRUS=true, FRUIT=true, LIME=true, TROPICAL=true}}, tags={CITRUS=true, FRUIT=true, LIME=true, TROPICAL=true}}, notes={en=[to serve, cooked]}}, Ingredient{q=150 GRAMMES, item=CanonicalItem{name=Sugar, tags={BAKING=true, SUGAR=true}}}, Ingredient{q=100 ML, item=CanonicalItem{name=Espresso, parent=CanonicalItem{name=Coffee, tags={COFFEE=true, DARK_RICH=true}}, tags={COFFEE=true, DARK_RICH=true}}, notes={en=[cooled]}}, Ingredient{q=100 ML, item=CanonicalItem{name=Instant Coffee, parent=CanonicalItem{name=Coffee, tags={COFFEE=true, DARK_RICH=true}}, tags={COFFEE=true, DARK_RICH=true}}, notes={en=[cooled]}}, Ingredient{q=300 ML, item=CanonicalItem{name=Water}}, Ingredient{q=3, item=CanonicalItem{name=Carrot, tags={VEGETABLE=true}}}, Ingredient{q=3, item=CanonicalItem{name=pureed}}, Ingredient{q=300 ML, item=CanonicalItem{name=Water}}, Ingredient{q=300 ML, item=CanonicalItem{name=Water}}, Ingredient{q=300 ML, item=CanonicalItem{name=Stock, tags={SAUCE=true, STOCK=true}}}]"));
    }

    @Test
    public void parseIngredients1() throws IOException {
        final List<IIngredient> allIngredients = dataUtils.parseIngredientsFrom(ADMIN_USER, "inputs.txt");
        assertThat(allIngredients.toString(), is("[Ingredient{q=1 TBSP, item=CanonicalItem{name=Sunflower Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={FAT=true, OIL=true}}, tags={FAT=true, OIL=true}}, tags={FAT=true, OIL=true}}}, Ingredient{q=200 GRAMMES, item=CanonicalItem{name=Streaky Bacon, parent=CanonicalItem{name=Bacon, parent=CanonicalItem{name=Pork, tags={MEAT=true}}, tags={MEAT=true, SMOKY_SALTY=true, UMAMI=true}}, tags={MEAT=true, SMOKY_SALTY=true, UMAMI=true}}, notes={en=[preferably in one piece, skinned and cut into pieces, smoked]}}, Ingredient{q=900 GRAMMES, item=CanonicalItem{name=Lamb Neck Fillets, parent=CanonicalItem{name=Lamb, tags={MEAT=true, RED_MEAT=true}}, tags={MEAT=true, RED_MEAT=true}}, notes={en=[cut into large chunks]}}, Ingredient{q=350 GRAMMES, item=CanonicalItem{name=Baby Onions, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[peeled]}}, Ingredient{q=5, item=CanonicalItem{name=Carrot, tags={VEGETABLE=true}}, notes={en=[cut into large chunks]}}, Ingredient{q=350 GRAMMES, item=CanonicalItem{name=Button Mushrooms, parent=CanonicalItem{name=Mushrooms, tags={EARTHY=true, UMAMI=true, VEGETABLE=true}}, tags={EARTHY=true, UMAMI=true, VEGETABLE=true}}, notes={en=[small]}}, Ingredient{q=3 TBSP, item=CanonicalItem{name=Plain Flour, parent=CanonicalItem{name=White Flour, parent=CanonicalItem{name=Flour, tags={FLOUR=true}}, tags={FLOUR=true}}, tags={FLOUR=true}}}, Ingredient{q=3, item=CanonicalItem{name=Bay Leaf, tags={HERB=true}}}, Ingredient{q=1 SMALL_BUNCHES, item=CanonicalItem{name=Thyme, tags={GRASSY=true, HERB=true}}}, Ingredient{q=350 ML, item=CanonicalItem{name=Red Wine, tags={ALCOHOL=true, WINE=true}}}, Ingredient{q=350 ML, item=CanonicalItem{name=Lamb Stock, parent=CanonicalItem{name=Stock, tags={SAUCE=true, STOCK=true}}, tags={MEAT=true, SAUCE=true, STOCK=true}}}, Ingredient{q=350 ML, item=CanonicalItem{name=Beef Stock, parent=CanonicalItem{name=Stock, tags={SAUCE=true, STOCK=true}}, tags={MEAT=true, RED_MEAT=true, SAUCE=true, STOCK=true}}}, Ingredient{q=LARGE SPLASHES, item=CanonicalItem{name=Worcestershire Sauce, tags={ENGLISH=true, SAUCE=true}}}, Ingredient{q=350 GRAMMES, item=CanonicalItem{name=Self-raising Flour, parent=CanonicalItem{name=Flour, tags={FLOUR=true}}, tags={FLOUR=true}}}, Ingredient{q=4 TBSP, item=CanonicalItem{name=Mixed Herbs, tags={HERB=true}}, notes={en=[including thyme, rosemary and parsley, chopped]}}, Ingredient{q=200 GRAMMES, item=CanonicalItem{name=Butter, tags={DAIRY=true, FAT=true}}, notes={en=[grated, chilled]}}, Ingredient{q=1, item=CanonicalItem{name=Lemon, tags={CITRUS=true, FRUIT=true, LEMON=true, TART=true}}, notes={en=[juice]}}, Ingredient{q=5, item=CanonicalItem{name=Bay Leaf, tags={HERB=true}}}, Ingredient{q=1, item=CanonicalItem{name=Eggs, tags={EGG=true}}, notes={en=[to glaze, beaten]}}]"));
    }

    @Test
    public void parseIngredients2() throws IOException {
        final List<IIngredient> allIngredients = dataUtils.parseIngredientsFrom(ADMIN_USER, "inputs2.txt");
        assertThat(allIngredients.toString(), is("[Ingredient{q=300 GRAMMES, item=CanonicalItem{name=Gnocchi, parent=CanonicalItem{name=Pasta, tags={ITALIAN=true, PASTA=true}}, tags={ITALIAN=true, PASTA=true}}, notes={en=[fresh]}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Olive Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={FAT=true, OIL=true}}, tags={FAT=true, OIL=true}}, tags={FAT=true, OIL=true}}}, Ingredient{q=1, item=CanonicalItem{name=Red Chillies, parent=CanonicalItem{name=Chilli, tags={CHILLI=true, HOT=true}}, tags={CHILLI=true, CHINESE=true, HOT=true, INDIAN=true, THAI=true}}, notes={en=[sliced, deseeded if you like]}}, Ingredient{q=1, item=CanonicalItem{name=Courgette, tags={VEGETABLE=true}}, notes={en=[cut into thin ribbons with a peeler, medium]}}, Ingredient{q=4, item=CanonicalItem{name=Spring Onions, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[chopped]}}, Ingredient{q=1, item=CanonicalItem{name=Lemon, tags={CITRUS=true, FRUIT=true, LEMON=true, TART=true}}, notes={en=[zest]}}, Ingredient{q=2 HEAPED_TBSP, item=CanonicalItem{name=Mascarpone, tags={CHEESE=true, DAIRY=true, ITALIAN=true}}}, Ingredient{q=50 GRAMMES, item=CanonicalItem{name=Parmesan, tags={CHEESE=true, DAIRY=true, ITALIAN=true, UMAMI=true}}, notes={en=[(or vegetarian alternative), grated]}}, Ingredient{q=Some, item=CanonicalItem{name=Mixed Leaves, tags={VEGETABLE=true}}, notes={en=[to serve, dressed]}}]"));
    }

    @Test
    public void parseIngredients3() throws IOException {
        final List<IIngredient> allIngredients = dataUtils.parseIngredientsFrom(ADMIN_USER, "inputs3.txt");
        assertThat(allIngredients.toString(), is("[Ingredient{q=1, item=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, notes={en=[large]}}, Ingredient{q=6, item=CanonicalItem{name=Garlic Cloves, tags={GARLIC=true, VEGETABLE=true}}, notes={en=[roughly chopped]}}, Ingredient{q=50 GRAMMES, item=CanonicalItem{name=Ginger, tags={CHINESE=true, CITRUS=true, INDIAN=true, PEPPERY=true, SPICE=true, SPICY=true, WARM=true}}, notes={en=[roughly chopped]}}, Ingredient{q=4 TBSP, item=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={FAT=true, OIL=true}}, tags={FAT=true, OIL=true}}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Cumin Seeds, parent=CanonicalItem{name=Cumin, tags={EARTHY=true, INDIAN=true, SPICE=true, WARM=true}}, tags={EARTHY=true, INDIAN=true, SPICE=true, WARM=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Fennel Seeds, tags={ANISEED=true, FLORAL=true, SEED=true, SPICE=true}}}, Ingredient{q=5 CM, item=CanonicalItem{name=Cinnamon Stick, parent=CanonicalItem{name=Cinnamon, tags={SPICE=true, SPICY=true}}, tags={INDIAN=true, SPICE=true, SPICY=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Chilli Flakes, parent=CanonicalItem{name=Chilli, tags={CHILLI=true, HOT=true}}, tags={CHILLI=true, HOT=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Garam Masala, tags={INDIAN=true, SPICE=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Turmeric, tags={EARTHY=true, INDIAN=true, SPICE=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Caster Sugar, parent=CanonicalItem{name=Sugar, tags={BAKING=true, SUGAR=true}}, tags={BAKING=true, SUGAR=true}}}, Ingredient{q=400 GRAMMES, item=CanonicalItem{name=Tomato, tags={UMAMI=true, VEGETABLE=true}}, notes={en=[chopped]}}, Ingredient{q=8, item=CanonicalItem{name=Chicken Thighs, parent=CanonicalItem{name=Chicken, tags={MEAT=true, POULTRY=true, WHITE_MEAT=true}}, tags={MEAT=true, POULTRY=true, WHITE_MEAT=true}}, notes={en=[skinned, boneless (about 800g)]}}, Ingredient{q=250 ML, item=CanonicalItem{name=Chicken Stock, parent=CanonicalItem{name=Stock, tags={SAUCE=true, STOCK=true}}, tags={MEAT=true, POULTRY=true, SAUCE=true, STOCK=true}}, notes={en=[hot]}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Coriander, tags={GRASSY=true, HERB=true}}, notes={en=[chopped]}}]"));
    }

    @Test
    public void parseIngredientsChCashBlackSpiceCurry() throws IOException {
        final List<IIngredient> allIngredients = dataUtils.parseIngredientsFrom(ADMIN_USER, "chCashBlackSpiceCurry.txt");
        assertThat(allIngredients.toString(), is("[Ingredient{q=1 KG, item=CanonicalItem{name=Chicken, tags={MEAT=true, POULTRY=true, WHITE_MEAT=true}}, notes={en=[skinned]}}, Ingredient{q=6, item=CanonicalItem{name=Cloves, tags={INDIAN=true, SPICE=true, SPICY=true}}}, Ingredient{q=100 GRAMMES, item=CanonicalItem{name=Coconut, tags={ASIAN=true, CREAMY=true, FRUIT=true, NUT=true, TROPICAL=true}}, notes={en=[grated]}}, Ingredient{q=3 INCH, item=CanonicalItem{name=Cinnamon Stick, parent=CanonicalItem{name=Cinnamon, tags={SPICE=true, SPICY=true}}, tags={INDIAN=true, SPICE=true, SPICY=true}}}, Ingredient{q=12, item=CanonicalItem{name=Garlic Cloves, tags={GARLIC=true, VEGETABLE=true}}, notes={en=[peeled, plump]}}, Ingredient{q=225 GRAMMES, item=CanonicalItem{name=Cashew Nuts, tags={NUT=true, SWEET_NUTTY=true}}}, Ingredient{q=0.5 INCH, item=CanonicalItem{name=Ginger, tags={CHINESE=true, CITRUS=true, INDIAN=true, PEPPERY=true, SPICE=true, SPICY=true, WARM=true}}, notes={en=[chopped, piece, of, fresh]}}, Ingredient{q=1, item=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, notes={en=[chopped, large]}}, Ingredient{q=0.5 TBSP, item=CanonicalItem{name=Coriander Seeds, parent=CanonicalItem{name=Coriander, tags={GRASSY=true, HERB=true}}, tags={INDIAN=true, SPICE=true}}}, Ingredient{q=0.5 TSP, item=CanonicalItem{name=Black Pepper, parent=CanonicalItem{name=Pepper, tags={PEPPERY=true, SPICE=true, SPICY=true}}, tags={PEPPERY=true, SPICE=true, SPICY=true}}, notes={en=[coarse]}}, Ingredient{q=0.5 TSP, item=CanonicalItem{name=Cumin Seeds, parent=CanonicalItem{name=Cumin, tags={EARTHY=true, INDIAN=true, SPICE=true, WARM=true}}, tags={EARTHY=true, INDIAN=true, SPICE=true, WARM=true}}}, Ingredient{q=4 TBSP, item=CanonicalItem{name=Oil, tags={FAT=true, OIL=true}}}, Ingredient{q=4, item=CanonicalItem{name=Red Chillies, parent=CanonicalItem{name=Chilli, tags={CHILLI=true, HOT=true}}, tags={CHILLI=true, CHINESE=true, HOT=true, INDIAN=true, THAI=true}}, notes={en=[whole, dried]}}, Ingredient{q=1, item=CanonicalItem{name=Salt, tags={SALT=true}}}]"));
    }

//	@Test
//	public void parseIngredientsVenisonBurgundy() throws IOException {
//		final List<IIngredient> allIngredients = dataUtils.parseIngredientsFrom( ADMIN_USER, "venisonBurgundy.txt");
//		assertThat( allIngredients.toString(), is("[Ingredient{q=3 CUP, item=CanonicalItem{name=Red Wine, tags={ALCOHOL=true, WINE=true}}, notes={en=[dry]}}, Ingredient{q=2 CUP, item=CanonicalItem{name=Beef Stock, parent=CanonicalItem{name=Stock, tags={SAUCE=true, STOCK=true}}, tags={MEAT=true, RED_MEAT=true, SAUCE=true, STOCK=true}}}, Ingredient{q=0.25 CUP, item=CanonicalItem{name=Cognac, parent=CanonicalItem{name=Brandy, tags={ALCOHOL=true, FRENCH=true, SPIRIT=true}}, tags={ALCOHOL=true, FRENCH=true, SPIRIT=true}}, notes={en=[(or good quality brandy)]}}, Ingredient{q=1, item=CanonicalItem{name=Yellow Onion, parent=CanonicalItem{name=Onion, tags={SULPHUROUS=true, VEGETABLE=true}}, tags={SULPHUROUS=true, VEGETABLE=true}}, notes={en=[chopped into large pieces, large]}}, Ingredient{q=2, item=CanonicalItem{name=Carrot, tags={VEGETABLE=true, WOODLAND=true}}, notes={en=[peeled and cut into 2-inch-long pieces]}}, Ingredient{q=3 CLOVE, item=CanonicalItem{name=Garlic Cloves, tags={GARLIC=true, SULPHUROUS=true, VEGETABLE=true}}, notes={en=[crushed and chopped]}}, Ingredient{q=0.25 CUP, item=CanonicalItem{name=Parsley, tags={GREEN_GRASSY=true, HERB=true}}, notes={en=[chopped, fresh]}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Thyme, tags={BRAMBLE_HEDGE=true, HERB=true}}, notes={en=[dried]}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Rosemary, tags={BRAMBLE_HEDGE=true, HERB=true}}, notes={en=[dried]}}, Ingredient{q=10, item=CanonicalItem{name=Black Pepper, parent=CanonicalItem{name=Pepper, tags={SPICE=true}}, tags={SPICE=true}}}, Ingredient{q=3, item=CanonicalItem{name=Cloves, tags={INDIAN=true, SPICE=true, SPICY=true}}, notes={en=[whole]}}, Ingredient{q=1, item=CanonicalItem{name=Allspice, tags={SPICE=true}}}, Ingredient{q=1, item=CanonicalItem{name=Bay Leaf, tags={HERB=true}}, notes={en=[dried]}}, Ingredient{q=3 POUNDS, item=CanonicalItem{name=Venison, tags={MEAT=true, RED_MEAT=true}}, notes={en=[(shoulder cuts), cut into 2-inch chunks]}}, Ingredient{q=0.5 POUNDS, item=CanonicalItem{name=Bacon, parent=CanonicalItem{name=Pork, tags={MEAT=true}}, tags={BRINE_SALT=true, MEAT=true}}, notes={en=[cut into thick slices, and then coarsely chopped]}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Tomato Paste, parent=CanonicalItem{name=Tomato, tags={FRESH_FRUITY=true, VEGETABLE=true}}, tags={FRESH_FRUITY=true, SAUCE=true, VEGETABLE=true}}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Olive Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={FAT=true, OIL=true}}, tags={FAT=true, OIL=true}}, tags={FAT=true, OIL=true}}}, Ingredient{q=1 POUNDS, item=CanonicalItem{name=Pearl Onions, parent=CanonicalItem{name=Onion, tags={SULPHUROUS=true, VEGETABLE=true}}, tags={SULPHUROUS=true, VEGETABLE=true}}, notes={en=[peeled]}}, Ingredient{q=1 POUNDS, item=CanonicalItem{name=White Mushrooms, parent=CanonicalItem{name=Mushrooms, tags={EARTHY=true, VEGETABLE=true}}, tags={EARTHY=true, VEGETABLE=true}}, notes={en=[wiped clean and bottoms trimmed]}}, Ingredient{q=0.5 TSP, item=CanonicalItem{name=Salt, tags={SALT=true}}}, Ingredient{q=0.25 TSP, item=CanonicalItem{name=Black Pepper, parent=CanonicalItem{name=Pepper, tags={SPICE=true}}, tags={SPICE=true}}, notes={en=[ground]}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Flour, tags={FLOUR=true}}}]"));
//	}
//
//	@Test
//	public void parseIngredientsBeefStewOrzo() throws IOException {
//		final List<IIngredient> allIngredients = dataUtils.parseIngredientsFrom( ADMIN_USER, "beefStewOrzo.txt");
//		assertThat( allIngredients.toString(), is("[Ingredient{q=0.5 POUNDS, item=CanonicalItem{name=Beef, tags={MEAT=true, RED_MEAT=true}}, notes={en=[cut into 2-inch chunks (see note)]}}, Ingredient{q=0.5 POUNDS, item=CanonicalItem{name=Lamb, tags={MEAT=true, RED_MEAT=true}}, notes={en=[cut into 2-inch chunks (see note)]}}, Ingredient{q=0.5 CUP, item=CanonicalItem{name=Olive Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={FAT=true, OIL=true}}, tags={FAT=true, OIL=true}}, tags={FAT=true, OIL=true}}, notes={en=[(separated)]}}, Ingredient{q=1, item=CanonicalItem{name=Onion, tags={SULPHUROUS=true, VEGETABLE=true}}, notes={en=[diced, large]}}, Ingredient{q=4 CLOVE, item=CanonicalItem{name=Garlic Cloves, tags={GARLIC=true, SULPHUROUS=true, VEGETABLE=true}}, notes={en=[minced finely]}}, Ingredient{q=1, item=CanonicalItem{name=Leek, tags={VEGETABLE=true}}, notes={en=[(cleaned, trimmed and cut in half), large]}}, Ingredient{q=1, item=CanonicalItem{name=Carrot, tags={VEGETABLE=true, WOODLAND=true}}, notes={en=[cut into thirds, large]}}, Ingredient{q=0.5 CUP, item=CanonicalItem{name=White Wine, tags={ALCOHOL=true, WINE=true}}, notes={en=[dry]}}, Ingredient{q=3.5, item=CanonicalItem{name=Allspice, tags={SPICE=true}}, notes={en=[whole]}}, Ingredient{q=28 OUNCES, item=CanonicalItem{name=Tomato, tags={FRESH_FRUITY=true, VEGETABLE=true}}, notes={en=[can, crushed]}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Sugar, tags={BAKING=true, SUGAR=true}}}, Ingredient{q=1 QUART, item=CanonicalItem{name=Water}}, Ingredient{q=1 POUNDS, item=CanonicalItem{name=Orzo, parent=CanonicalItem{name=Pasta, tags={ITALIAN=true, PASTA=true}}, tags={ITALIAN=true, PASTA=true}}}, Ingredient{q=1, item=CanonicalItem{name=Salt, tags={SALT=true}}}, Ingredient{q=1, item=CanonicalItem{name=Pepper, tags={SPICE=true}}}]"));
//	}

    @Test
    public void parseIngredientsTtFishCurry() throws IOException {
        final List<IIngredient> allIngredients = dataUtils.parseIngredientsFrom(ADMIN_USER, "ttFishCurry.txt");
        assertThat(allIngredients.toString(), is("[Ingredient{q=6, item=CanonicalItem{name=Garlic Cloves, tags={GARLIC=true, VEGETABLE=true}}}, Ingredient{q=1, item=CanonicalItem{name=Red Chillies, parent=CanonicalItem{name=Chilli, tags={CHILLI=true, HOT=true}}, tags={CHILLI=true, CHINESE=true, HOT=true, INDIAN=true, THAI=true}}, notes={en=[roughly chopped (deseeded if you don't like it too hot)]}}, Ingredient{q=THUMB_SIZE PIECE, item=CanonicalItem{name=Ginger, tags={CHINESE=true, CITRUS=true, INDIAN=true, PEPPERY=true, SPICE=true, SPICY=true, WARM=true}}, notes={en=[peeled and roughly chopped]}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Turmeric, tags={EARTHY=true, INDIAN=true, SPICE=true}}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Coriander, tags={GRASSY=true, HERB=true}}, notes={en=[ground]}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Rapeseed Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={FAT=true, OIL=true}}, tags={FAT=true, OIL=true}}, tags={FAT=true, OIL=true}}}, Ingredient{q=2 TSP, item=CanonicalItem{name=Cumin Seeds, parent=CanonicalItem{name=Cumin, tags={EARTHY=true, INDIAN=true, SPICE=true, WARM=true}}, tags={EARTHY=true, INDIAN=true, SPICE=true, WARM=true}}}, Ingredient{q=1 TSP, item=CanonicalItem{name=Fennel Seeds, tags={ANISEED=true, FLORAL=true, SEED=true, SPICE=true}}}, Ingredient{q=200 GRAMMES, item=CanonicalItem{name=Green Beans, tags={VEGETABLE=true}}, notes={en=[trimmed and halved]}}, Ingredient{q=1 TBSP, item=CanonicalItem{name=Tamarind Paste, parent=CanonicalItem{name=Tamarind, tags={FRUIT=true, TART=true}}, tags={FRUIT=true, SAUCE=true, TART=true}}}, Ingredient{q=4, item=CanonicalItem{name=White Fish Fillets, parent=CanonicalItem{name=White Fish, tags={FISH=true, SEAFOOD=true}}, tags={FISH=true, SEAFOOD=true}}, notes={en=[(we used hake), firm]}}, Ingredient{q=1 HANDFUL, item=CanonicalItem{name=Coriander, tags={GRASSY=true, HERB=true}}, notes={en=[roughly chopped]}}, Ingredient{q=Some, item=CanonicalItem{name=Basmati Rice, parent=CanonicalItem{name=Rice, tags={RICE=true}}, tags={INDIAN=true, RICE=true}}, notes={en=[to serve, cooked]}}]"));
    }

    @Test
    public void parseIngredientsNoodles() throws IOException {
        final List<IIngredient> allIngredients = dataUtils.parseIngredientsFrom(ADMIN_USER, "noodles.txt");
        assertThat(allIngredients.toString(), is("[Ingredient{q=1, item=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, notes={en=[roughly chopped, large]}}, Ingredient{q=THUMB_SIZE PIECE, item=CanonicalItem{name=Root Ginger, parent=CanonicalItem{name=Ginger, tags={CHINESE=true, CITRUS=true, INDIAN=true, PEPPERY=true, SPICE=true, SPICY=true, WARM=true}}, tags={CHINESE=true, CITRUS=true, INDIAN=true, PEPPERY=true, SPICE=true, SPICY=true, WARM=true}}, notes={en=[fresh]}}, Ingredient{q=1.5, item=CanonicalItem{name=Red Chillies, parent=CanonicalItem{name=Chilli, tags={CHILLI=true, HOT=true}}, tags={CHILLI=true, CHINESE=true, HOT=true, INDIAN=true, THAI=true}}, notes={en=[finely chopped (seeds in or out, you decide), long]}}, Ingredient{q=1, item=CanonicalItem{name=Garlic Cloves, tags={GARLIC=true, VEGETABLE=true}}, notes={en=[crushed]}}, Ingredient{q=6, item=CanonicalItem{name=White Pepper, parent=CanonicalItem{name=Pepper, tags={PEPPERY=true, SPICE=true, SPICY=true}}, tags={EARTHY=true, PEPPERY=true, SPICE=true, SPICY=true}}, notes={en=[crushed]}}, Ingredient{q=20 GRAMMES, item=CanonicalItem{name=Coriander, tags={GRASSY=true, HERB=true}}, notes={en=[stalks, roots if you have them and leaves, chopped and kept separate, plus sprigs to finish]}}, Ingredient{q=50 ML, item=CanonicalItem{name=Milk, tags={DAIRY=true}}}, Ingredient{q=100 GRAMMES, item=CanonicalItem{name=White Breadcrumbs, tags={BREAD=true}}, notes={en=[fresh]}}, Ingredient{q=1 KG, item=CanonicalItem{name=Chicken Mince, parent=CanonicalItem{name=Chicken, tags={MEAT=true, POULTRY=true, WHITE_MEAT=true}}, tags={MEAT=true, POULTRY=true, WHITE_MEAT=true}}, notes={en=[quality]}}, Ingredient{q=3 TBSP, item=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={FAT=true, OIL=true}}, tags={FAT=true, OIL=true}}}, Ingredient{q=1.5 LITRE, item=CanonicalItem{name=Chicken Stock, parent=CanonicalItem{name=Stock, tags={SAUCE=true, STOCK=true}}, tags={MEAT=true, POULTRY=true, SAUCE=true, STOCK=true}}}, Ingredient{q=2 TBSP, item=CanonicalItem{name=Sesame Oil, parent=CanonicalItem{name=Vegetable Oil, parent=CanonicalItem{name=Oil, tags={FAT=true, OIL=true}}, tags={FAT=true, OIL=true}}, tags={CHINESE=true, FAT=true, OIL=true}}, notes={en=[toasted]}}, Ingredient{q=3 TBSP, item=CanonicalItem{name=Fish Sauce, tags={FISH=true, SAUCE=true, THAI=true, VIETNAMESE=true}}}, Ingredient{q=6, item=CanonicalItem{name=Star Anise, tags={CHINESE=true, FLORAL=true, SPICE=true, SPICY=true}}}, Ingredient{q=THUMB_SIZE PIECE, item=CanonicalItem{name=Root Ginger, parent=CanonicalItem{name=Ginger, tags={CHINESE=true, CITRUS=true, INDIAN=true, PEPPERY=true, SPICE=true, SPICY=true, WARM=true}}, tags={CHINESE=true, CITRUS=true, INDIAN=true, PEPPERY=true, SPICE=true, SPICY=true, WARM=true}}, notes={en=[sliced, fresh]}}, Ingredient{q=0.5, item=CanonicalItem{name=Black Pepper, parent=CanonicalItem{name=Pepper, tags={PEPPERY=true, SPICE=true, SPICY=true}}, tags={PEPPERY=true, SPICE=true, SPICY=true}}}, Ingredient{q=8, item=CanonicalItem{name=Spring Onions, parent=CanonicalItem{name=Onion, tags={VEGETABLE=true}}, tags={VEGETABLE=true}}, notes={en=[thinly sliced]}}, Ingredient{q=300 GRAMMES, item=CanonicalItem{name=Egg Noodles, parent=CanonicalItem{name=Noodles, tags={NOODLES=true}}, tags={EGG=true, NOODLES=true}}, notes={en=[cooked]}}, Ingredient{q=Some, item=CanonicalItem{name=Chilli, tags={CHILLI=true, HOT=true}}, notes={en=[sliced]}}, Ingredient{q=1 SMALL_BUNCHES, item=CanonicalItem{name=Basil, tags={HERB=true, ITALIAN=true}}, notes={en=[leaves picked]}}]"));
    }

    @Test
    public void testAliases() throws IOException {
        final List<IIngredient> namings1 = dataUtils.parseIngredientsFrom(ADMIN_USER, "namings1.txt");
        final List<IIngredient> namings2 = dataUtils.parseIngredientsFrom(ADMIN_USER, "namings2.txt");
//		assertThat( Similarity.amongIngredients( namings1, namings2), is(1.0));
        assertThat(namings1, is(namings2));
        assertThat(namings2, is(namings1));
    }

    @Test
    public void testSimilarity() throws IOException {
        final List<IIngredient> ingr1 = dataUtils.parseIngredientsFrom(ADMIN_USER, "inputs.txt");
        final List<IIngredient> ingr2 = dataUtils.parseIngredientsFrom(ADMIN_USER, "inputs2.txt");
        final List<IIngredient> ingr3 = dataUtils.parseIngredientsFrom(ADMIN_USER, "inputs3.txt");
        final List<IIngredient> ingr4 = dataUtils.parseIngredientsFrom(ADMIN_USER, "chCashBlackSpiceCurry.txt");
        final List<IIngredient> ingrBol1 = dataUtils.parseIngredientsFrom(ADMIN_USER, "bol1.txt");
        final List<IIngredient> ingrBol2 = dataUtils.parseIngredientsFrom(ADMIN_USER, "bol2.txt");
        final List<IIngredient> ingrChBeef = dataUtils.parseIngredientsFrom(ADMIN_USER, "chineseBeef.txt");

        assertThat(Categorisation.forIngredients(ingr1).toString(), is("[ALCOHOL, CITRUS, DAIRY, EARTHY, EGG, ENGLISH, FAT x 2, FLOUR x 2, FRUIT, GRASSY, HERB x 4, LEMON, MEAT x 4, OIL, RED_MEAT x 2, SAUCE x 3, SMOKY_SALTY, STOCK x 2, TART, UMAMI x 2, VEGETABLE x 3, WINE]"));
        assertThat(Categorisation.forIngredients(ingr2).toString(), is("[CHEESE x 2, CHILLI, CHINESE, CITRUS, DAIRY x 2, FAT, FRUIT, HOT, INDIAN, ITALIAN x 3, LEMON, OIL, PASTA, TART, THAI, UMAMI, VEGETABLE x 3]"));
        assertThat(Categorisation.forIngredients(ingr4).toString(), is("[ASIAN, CHILLI, CHINESE x 2, CITRUS, CREAMY, EARTHY, FAT, FRUIT, GARLIC, HOT, INDIAN x 6, MEAT, NUT x 2, OIL, PEPPERY x 2, POULTRY, SALT, SPICE x 6, SPICY x 4, SWEET_NUTTY, THAI, TROPICAL, VEGETABLE x 2, WARM x 2, WHITE_MEAT]"));
        assertThat(Categorisation.forIngredients(ingrBol2).toString(), is("[ALCOHOL, DAIRY x 2, FAT, MEAT x 3, OFFAL, POULTRY, RED_MEAT, SMOKY_SALTY, SPICE, SPICY, UMAMI x 3, VEGETABLE x 4, WINE]"));
        assertThat(Categorisation.forIngredients(ingrChBeef).toString(), is("[ALCOHOL x 2, ANISEED, BAKING, CHILLI, CHINESE x 6, CITRUS, FAT, FLORAL, FLOUR, GARLIC, HOT, INDIAN x 3, MEAT x 2, OIL, PEPPERY, RED_MEAT x 2, RICE, SAUCE x 2, SPANISH, SPICE x 3, SPICY x 2, SPIRIT, STOCK, SUGAR, THAI, UMAMI, VEGETABLE x 2, WARM, WINE]"));

        final ITag[] tags = NationalCuisineTags.values();
        assertThat(Categorisation.forIngredients(ingr1, tags).toString(), is("[ENGLISH]"));
        assertThat(Categorisation.forIngredients(ingr2, tags).toString(), is("[CHINESE, INDIAN, ITALIAN x 3, THAI]"));
        assertThat(Categorisation.forIngredients(ingr3, tags).toString(), is("[CHINESE, INDIAN x 5]"));
        assertThat(Categorisation.forIngredients(ingr4, tags).toString(), is("[ASIAN, CHINESE x 2, INDIAN x 6, THAI]"));
        assertThat(Categorisation.forIngredients(ingrBol1, tags).toString(), is("[ITALIAN]"));
        assertThat(Categorisation.forIngredients(ingrBol2, tags).toString(), is("[]"));
        assertThat(Categorisation.forIngredients(ingrChBeef, tags).toString(), is("[CHINESE x 6, INDIAN x 3, SPANISH, THAI]"));
    }

    @Test
    public void testWeightedCategorisation() throws IOException {
        final List<IIngredient> ingrChBeef = dataUtils.parseIngredientsFrom(ADMIN_USER, "chineseBeef.txt");
        System.out.println(ingrChBeef);

        final ITag[] natTags = NationalCuisineTags.values();
        assertThat(Categorisation.forIngredientsWeighted(ingrChBeef, natTags).toString(), is("[CHINESE x 11, INDIAN x 5, SPANISH, THAI x 2]"));

        final ITag[] flavourTags = FlavourTags.values();
        assertThat(Categorisation.forIngredientsWeighted(ingrChBeef, flavourTags).toString(), is("[ANISEED x 2, CITRUS x 2, FLORAL x 2, GARLIC x 2, HOT x 2, PEPPERY x 2, SPICY x 4, UMAMI x 2, WARM x 2]"));
    }

    @Test
    public void testParseFailures() throws IOException {
        int numSuccesses = 0;
        for (String eachLine : Files.readLines(new File("src/test/resources/parse_failures.txt"), Charset.forName("utf-8"))) {
            try {
                if (dataUtils.parseIngredient(eachLine)) {
                    numSuccesses++;
                } else {
                    System.err.println("Could not parse: " + eachLine);
                }
            } catch (RuntimeException e) {
                System.err.println("Actual error: " + e);
            }
        }

        assertThat(numSuccesses, greaterThanOrEqualTo(352));  // Need this to hit 420 eventually!
    }

    @AfterClass
    public void shutDown() {
        esClient.close();
    }

    private static class DummyDeferralHandler implements IDeferredIngredientHandler {

        @Override
        public void deferIngredient(DeferralStatus status) {
            // NOOP
        }
    }

    @Singleton
    @Component(modules = {DaggerModule.class, ProductionMyrrixModule.class})
    public interface TestComponent {
        void inject(final ParseIngredientsTest runner);
    }
}
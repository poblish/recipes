/**
 *
 */
package uk.co.recipes.external;

import com.google.common.base.Optional;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

/**
 * TODO
 *
 * @author andrewregan
 */
public class WikipediaTest {

    @Test(enabled = false)
    public void testWikipediaTextParse() throws IOException {
        final Optional<WikipediaResults> results = new WikipediaGetter().getResultsFor("Coriander seed");

        assertThat(results.get().getText(), is("Coriander (Coriandrum sativum), also known as cilantro, Chinese parsley or dhania, is an annual herb in the family Apiaceae. Coriander is native to regions spanning from southern Europe and North Africa to southwestern Asia. It is a soft plant growing to 50 cm (20 in) tall. The leaves are variable in shape, broadly lobed at the base of the plant, and slender and feathery higher on the flowering stems. The flowers are borne in small umbels, white or very pale pink, asymmetrical, with the petals pointing away from the centre of the umbel longer (5–6 mm) than those pointing toward it (only 1–3 mm long). The fruit is a globular, dry schizocarp 3–5 mm (0.12–0.20 in) in diameter. Although sometimes eaten alone, the seeds often are used as a spice or an added ingredient in other foods."));
        assertThat(results.get().getSecondaryText(), is("First attested in English late fourteenth century, the word coriander derives from the Old French: coriandre, which comes from Latin: coriandrum, in turn from Ancient Greek: κορίαννον koriannon. The earliest attested form of the word is the Mycenaean Greek 𐀒𐀪𐀊𐀅𐀙 ko-ri-ja-da-na (written in Linear B syllabic script, reconstructed as koriadnon), similar to the name of Minos's daughter Ariadne, which later evolved to koriannon or koriandron."));
        assertThat(results.get().getImgUrl(), startsWith("http://upload.wikimedia.org/wikipedia/commons/thumb/"));
        assertThat(results.get().getUrl(), is("http://en.wikipedia.org/wiki/Coriander_seed"));
    }
}

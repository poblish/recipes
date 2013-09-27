/**
 * 
 */
package uk.co.recipes.external;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.testng.annotations.Test;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class WikipediaTest {

	@Test
	public void testWikipediaTextParse() throws IOException {
		final Document doc = Jsoup.connect("http://en.wikipedia.org/wiki/Coriander").get();
		final Elements contentPs = doc.select("#mw-content-text p");
		final String firstParaText = contentPs.iterator().next().text();

		assertThat( firstParaText, is("Coriander (Coriandrum sativum), also known as cilantro, Chinese parsley or dhania,[1] is an annual herb in the family Apiaceae. Coriander is native to regions spanning from southern Europe and North Africa to southwestern Asia. It is a soft plant growing to 50 cm (20 in) tall. The leaves are variable in shape, broadly lobed at the base of the plant, and slender and feathery higher on the flowering stems. The flowers are borne in small umbels, white or very pale pink, asymmetrical, with the petals pointing away from the centre of the umbel longer (5–6 mm) than those pointing toward it (only 1–3 mm long). The fruit is a globular, dry schizocarp 3–5 mm (0.12–0.20 in) in diameter. Although sometimes eaten alone, the seeds often are used as a spice or an added ingredient in other foods."));
	}
}

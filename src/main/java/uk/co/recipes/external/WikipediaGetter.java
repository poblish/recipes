/**
 * 
 */
package uk.co.recipes.external;

import java.io.IOException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Optional;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class WikipediaGetter {

	public Optional<WikipediaResults> getResultsFor( final String inPage) throws IOException {
		final Document doc = Jsoup.connect("http://en.wikipedia.org/wiki/Coriander").get();
		final Elements contentPs = doc.select("#mw-content-text p");
		final String firstParaText = contentPs.iterator().next().text();

		final Elements imgs = doc.select("img");
		for ( Element each : imgs) {
			final URL theURL = new URL("http:" + each.attr("src") );
//			final URLConnection conn = theURL.openConnection();
			return Optional.of( new WikipediaResults( firstParaText, "http:" + each.attr("src") ) );
//			System.out.println( each.attr("src") );
		}

		return Optional.absent();
	}
}

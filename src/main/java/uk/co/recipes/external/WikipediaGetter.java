/**
 * 
 */
package uk.co.recipes.external;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;

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
		final String url = "http://en.wikipedia.org/wiki/" + URLEncoder.encode( inPage.replace( ' ', '_'), "utf-8");
		System.out.println("Connecting to " + url);

		final Document doc = Jsoup.connect(url).get();
		final Elements contentPs = doc.select("#mw-content-text p");
		final Iterator<Element> itr = contentPs.iterator();
		final String firstParaText = itr.next().text();
		final String secondParaText = itr.next().text();

		final Elements imgs = doc.select("img");
		for ( Element each : imgs) {
			final String theURL = "http:" + each.attr("src");
//			final URLConnection conn = theURL.openConnection();
			return Optional.of( new WikipediaResults( url, firstParaText, secondParaText, theURL) );
//			System.out.println( each.attr("src") );
		}

		return Optional.absent();
	}
}

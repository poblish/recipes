/**
 * 
 */
package uk.co.recipes.external;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.regex.Pattern;

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

	private final static Pattern LINK_PATTERN = Pattern.compile("\\[[0-9]+\\]");

	public Optional<WikipediaResults> getResultsFor( final String inPage) throws IOException {
		final String url = "http://en.wikipedia.org/wiki/" + URLEncoder.encode( inPage.replace( ' ', '_'), "utf-8");
		System.out.println("Connecting to " + url);

		final Document doc = Jsoup.connect(url).get();
		final Elements contentPs = doc.select("#mw-content-text p");
		final Iterator<Element> itr = contentPs.iterator();
		final String firstParaText = LINK_PATTERN.matcher( itr.next().text() ).replaceAll("");
		final String secondParaText = itr.hasNext() ? LINK_PATTERN.matcher( itr.next().text() ).replaceAll("") : "";

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

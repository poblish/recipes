/**
 *
 */
package uk.co.recipes.crawl;

import com.google.common.base.Throwables;
import com.google.common.io.Files;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * TODO
 *
 * @author andrewregan
 */
public class CurryFrenzyCrawler {

    public static void main(String[] args) throws Exception {
        try {
            try {
                new File("/Users/andrewregan/Development/java/recipe_explorer/src/test/resources/ingredients/curryfrenzy/").mkdir();
            } catch (RuntimeException e) {
            }

            long st = System.currentTimeMillis();
            new CurryFrenzyCrawler().crawlBbcGoodFood();
            System.out.println("Finished loading in " + ((System.currentTimeMillis() - st) / 1000d) + " msecs");
            System.exit(0);
        } catch (IOException | InterruptedException e) {
            Throwables.propagate(e);
        }
    }

    public void crawlBbcGoodFood() throws Exception {
        String crawlStorageFolder = "/Users/andrewregan/Development/java/recipe_explorer";
        int numberOfCrawlers = 5;

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setMaxPagesToFetch(2000);

        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
        controller.addSeed("http://www.curryfrenzy.com/curry/html/curry-recipes.asp");

        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
        controller.start(MyCrawler.class, numberOfCrawlers);
    }

    public static class MyCrawler extends WebCrawler {

        private static final Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g"
                + "|png|tiff?|mid|mp2|mp3|mp4"
                + "|wav|avi|mov|mpeg|ram|m4v|pdf"
                + "|css|jpg|asp"
                + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

        /**
         * You should implement this function to specify whether the given url should be crawled or not (based on your crawling logic).
         */
        @Override
        public boolean shouldVisit(WebURL url) {
            String href = url.getURL().toLowerCase();
            return !FILTERS.matcher(href).matches() && (href.contains("/recipe/") || href.contains("/recipes/"));
        }

        /**
         * This function is called when a page is fetched and ready to be processed by your program.
         */
        @Override
        public void visit(Page page) {
            try {
                final String url = page.getWebURL().getURL();
                final Document doc = Jsoup.connect(url).get();

                String title = doc.select("div#content h1").text();
                if (title.isEmpty()) {
                    return;
                }

                if (title.endsWith(" Recipe")) {
                    title = title.substring(0, title.length() - 7);
                }

                final File theFile = new File("/Users/andrewregan/Development/java/recipe_explorer/src/test/resources/ingredients/curryfrenzy/" + title.toLowerCase().replace(' ', '_') + ".txt");
                if (theFile.exists()) {
                    return;  // Skip
                }

                final StringBuilder sb = new StringBuilder(800);
                sb.append("// ").append(title).append("\n");
                sb.append("// ").append(url).append("\n");

                for (Element each : doc.select("div#content td li")) {
                    final String text = each.text();

                    if (text.startsWith("Buy a") || text.startsWith("Save a")) {
                        break;
                    }

                    sb.append(text).append("\n");
                }

                Files.write(sb.toString(), theFile, Charset.forName("utf-8"));
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        }
    }
}

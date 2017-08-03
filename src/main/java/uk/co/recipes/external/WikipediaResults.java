/**
 *
 */
package uk.co.recipes.external;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TODO
 *
 * @author andrewregan
 */
public class WikipediaResults {

    private String url;
    private String text;
    private String secondText;
    private String imgUrl;

    public WikipediaResults(String url, String text, String secondText, String imgUrl) {
        this.url = checkNotNull(url);
        this.text = checkNotNull(text);
        this.secondText = checkNotNull(secondText);
        this.imgUrl = checkNotNull(imgUrl);
    }

    public String getUrl() {
        return url;
    }

    public String getText() {
        return text;
    }

    public String getSecondaryText() {
        return secondText;
    }

    public String getImgUrl() {
        return imgUrl;
    }
}

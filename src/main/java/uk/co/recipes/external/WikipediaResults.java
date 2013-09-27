/**
 * 
 */
package uk.co.recipes.external;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class WikipediaResults {

	private String text;
	private String imgUrl;

	public WikipediaResults(String text, String imgUrl) {
		this.text = text;
		this.imgUrl = imgUrl;
	}

	public String getText() {
		return text;
	}

	public String getImgUrl() {
		return imgUrl;
	}
}

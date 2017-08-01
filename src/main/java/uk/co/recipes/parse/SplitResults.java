/**
 * 
 */
package uk.co.recipes.parse;

import java.util.Arrays;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;


/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class SplitResults {

	private String[] firstResults;
	private String[] secondResults;

	public SplitResults(String[] res1, String[] res2) {
		this.firstResults = res1;
		this.secondResults = res2;
	}

	public String[] getFirstResults() {
		return firstResults;
	}
	public String[] getSecondResults() {
		return secondResults;
	}

	public String toString() {
		return MoreObjects.toStringHelper(this).add( "first", Arrays.toString(firstResults)).add( "second", Arrays.toString(secondResults)).toString();
	}
}

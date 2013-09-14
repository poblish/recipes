/**
 * 
 */
package uk.co.recipes.parse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.testng.annotations.Test;

/**
 * Test the Ngram-style for turning two optional names into all possible (English) name combinations
 * 
 * @author andrewr
 *
 */
public class OptionalNameSplittingTest {

	@Test
	public void testSplitting() {
		assertThat( strFor("beef", "lamb"), is("SplitResults{first=[beef], second=[lamb]}"));
		assertThat( strFor("red", "white wine"), is("SplitResults{first=[red], second=[red wine, white wine]}"));
		assertThat( strFor("Chinese cooking wine", "dry sherry"), is("SplitResults{first=[Chinese cooking dry sherry, Chinese dry sherry, Chinese cooking wine], second=[Chinese cooking wine sherry, dry sherry]}"));
		assertThat( strFor("Kefalotyri cheese", "Pecorino Romano"), is("SplitResults{first=[Kefalotyri Pecorino Romano, Kefalotyri cheese], second=[Kefalotyri cheese Romano, Pecorino Romano]}"));
		assertThat( strFor("caster sugar", "vanilla sugar"), is("SplitResults{first=[caster sugar], second=[vanilla sugar]}"));
		assertThat( strFor("caster cane sugar", "vanilla cane sugar"), is("SplitResults{first=[caster cane sugar], second=[vanilla cane sugar]}"));
		assertThat( strFor("red wine", "red blood"), is("SplitResults{first=[red wine], second=[red blood]}"));
		assertThat( strFor("Water", "Chicken Stock"), is("SplitResults{first=[Water], second=[Water Stock, Chicken Stock]}"));
		assertThat( strFor("dried", "fresh kaffir lime leaves"), is("SplitResults{first=[dried], second=[dried kaffir lime leaves, dried lime leaves, dried leaves, fresh kaffir lime leaves]}"));
		assertThat( strFor("espresso", "strong instant coffee"), is("SplitResults{first=[espresso], second=[espresso instant coffee, espresso coffee, strong instant coffee]}"));
	}

	private String strFor( final String s1, final String s2) {
		return new OptionalNameSplitter().split(s1, s2).toString();
	}
}

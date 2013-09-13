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
		final OptionalNameSplitter s = new OptionalNameSplitter();
		assertThat( s.split("beef", "lamb"), is(new String[]{"beef", "lamb"}));
		assertThat( s.split("Chinese cooking wine", "dry sherry"), is(new String[]{"Chinese cooking wine", "dry sherry", "Chinese cooking dry sherry", "Chinese cooking wine sherry", "Chinese dry sherry"}));
		assertThat( s.split("Grated Kefalotyri cheese", "Pecorino Romano"), is(new String[]{"Grated Kefalotyri cheese", "Pecorino Romano", "Grated Kefalotyri Pecorino Romano", "Grated Kefalotyri cheese Romano", "Grated Pecorino Romano"}));
		assertThat( s.split("caster sugar", "vanilla sugar"), is(new String[]{"caster sugar", "vanilla sugar"}));
		assertThat( s.split("caster cane sugar", "vanilla cane sugar"), is(new String[]{"caster cane sugar", "vanilla cane sugar"}));
		assertThat( s.split("red wine", "red blood"), is(new String[]{"red wine", "red blood"}));
		assertThat( s.split("Water", "Chicken Stock"), is(new String[]{"Water", "Chicken Stock", "Water Stock"}));
		assertThat( s.split("dried", "fresh kaffir lime leaves"), is(new String[]{"dried", "fresh kaffir lime leaves", "dried kaffir lime leaves", "dried lime leaves", "dried leaves"}));
		assertThat( s.split("espresso", "strong instant coffee"), is(new String[]{"espresso", "strong instant coffee", "espresso instant coffee", "espresso coffee"}));
	}
}

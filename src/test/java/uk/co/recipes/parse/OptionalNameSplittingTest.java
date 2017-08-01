package uk.co.recipes.parse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Component;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.co.recipes.DaggerModule;

/**
 * Test the Ngram-style for turning two optional names into all possible (English) name combinations
 * 
 * @author andrewr
 *
 */
public class OptionalNameSplittingTest {

	@Inject OptionalNameSplitter splitter;

	@BeforeClass
	private void injectDependencies() {
		DaggerOptionalNameSplittingTest_TestComponent.create().inject(this);
	}

	@Test
	public void testSplitting() {
		assertThat( strFor("beef", "lamb"), is("SplitResults{first=[beef], second=[lamb]}"));
		assertThat( strFor("red", "white wine"), is("SplitResults{first=[red wine, red], second=[white wine]}"));
		assertThat( strFor("Chinese cooking wine", "dry sherry"), is("SplitResults{first=[Chinese cooking wine sherry, Chinese cooking wine], second=[Chinese cooking dry sherry, Chinese dry sherry, sherry]}"));  // 'dry sherry' adjusted => 'sherry'
		assertThat( strFor("Kefalotyri cheese", "Pecorino Romano"), is("SplitResults{first=[Kefalotyri cheese Romano, Kefalotyri cheese], second=[Kefalotyri Pecorino Romano, Pecorino Romano]}"));
		assertThat( strFor("caster sugar", "vanilla sugar"), is("SplitResults{first=[caster sugar], second=[vanilla sugar]}"));
		assertThat( strFor("caster cane sugar", "vanilla cane sugar"), is("SplitResults{first=[caster cane sugar], second=[vanilla cane sugar]}"));
		assertThat( strFor("red wine", "red blood"), is("SplitResults{first=[red wine], second=[red blood]}"));
		assertThat( strFor("Water", "Chicken Stock"), is("SplitResults{first=[Water Stock, Water], second=[Chicken Stock]}"));
		assertThat( strFor("dried", "fresh kaffir lime leaves"), is("SplitResults{first=[dried kaffir lime leaves, dried lime leaves, dried leaves, dried], second=[kaffir lime leaves]}"));  // 'fresh kaffir lime leaves' adjusted => 'kaffir lime leaves'
		assertThat( strFor("espresso", "strong instant coffee"), is("SplitResults{first=[espresso instant coffee, espresso coffee, espresso], second=[strong instant coffee]}"));
		assertThat( strFor("spring onions", "finely chopped onions"), is("SplitResults{first=[spring onions chopped onions, spring onions], second=[spring finely chopped onions, onions]}"));  // 'finely chopped onions' adjusted => 'onions'
		assertThat( strFor("tomatoes", "lovely cherry tomatoes"), is("SplitResults{first=[tomatoes], second=[lovely cherry tomatoes]}"));
	}

	private String strFor( final String s1, final String s2) {
		return splitter.split(s1, s2).toString();
	}

	@Singleton
	@Component(modules={ DaggerModule.class })
	public interface TestComponent {
		void inject(final OptionalNameSplittingTest runner);
	}
}

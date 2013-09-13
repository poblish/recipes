/**
 * 
 */
package uk.co.recipes.parse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Collection;
import java.util.List;

import org.elasticsearch.common.base.Joiner;
import org.elasticsearch.common.collect.Iterables;
import org.testng.annotations.Test;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * Test the Ngram-style for turning two optional names into all possible (English) name combinations
 * 
 * @author andrewr
 *
 */
public class OptionalNameSplittingTest {

    @Test
    public void testSplitting() {
        assertThat( split("beef", "lamb"), is(new String[]{"beef", "lamb"}));
        assertThat( split("Chinese cooking wine", "dry sherry"), is(new String[]{"Chinese cooking wine", "dry sherry", "Chinese cooking dry sherry", "Chinese cooking wine sherry", "Chinese dry sherry"}));
        assertThat( split("Grated Kefalotyri cheese", "Pecorino Romano"), is(new String[]{"Grated Kefalotyri cheese", "Pecorino Romano", "Grated Kefalotyri Pecorino Romano", "Grated Kefalotyri cheese Romano", "Grated Pecorino Romano"}));
        assertThat( split("caster sugar", "vanilla sugar"), is(new String[]{"caster sugar", "vanilla sugar", "caster vanilla sugar", "caster sugar sugar"}));
        assertThat( split("Water", "Chicken Stock"), is(new String[]{"Water", "Chicken Stock", "Water Stock"}));
        assertThat( split("dried", "fresh kaffir lime leaves"), is(new String[]{"dried", "fresh kaffir lime leaves", "dried kaffir lime leaves", "dried lime leaves", "dried leaves"}));
        assertThat( split("espresso", "strong instant coffee"), is(new String[]{"espresso", "strong instant coffee", "espresso instant coffee", "espresso coffee"}));
    }

    private String[] split( final String s1, final String s2) {
        // FIXME Factor out constant
        final Collection<String> wordsColl1 = Splitter.on(' ').trimResults().splitToList(s1);
        final Collection<String> wordsColl2 = Splitter.on(' ').trimResults().splitToList(s2);

        final String[] words1 = Iterables.toArray( wordsColl1, String.class);
        final String[] words2 = Iterables.toArray( wordsColl2, String.class);
 
        List<String> s = Lists.newArrayList();

    	s.add(s1);
    	s.add(s2);

        for ( int num = 1; num < Math.max( words1.length, words2.length); num++)
        {
        	if ( num < words1.length) {
	        	StringBuilder each = new StringBuilder();

	        	for ( int i = 0; i < words1.length - num; i++) {
	        		if ( each.length() > 0) {
	        			each.append(" ");
	        		}
	        		each.append( words1[i] );
	        	}

	        	each.append( " " + Joiner.on(' ').join(wordsColl2) );

	        	s.add(each.toString());
        	}

        	if ( num < words2.length) {
            	StringBuilder each = new StringBuilder();

	        	each.append( Joiner.on(' ').join(wordsColl1) );

	        	for ( int j = num; j < words2.length; j++) {
	        		if ( each.length() > 0) {
	        			each.append(" ");
	        		}
	        		each.append( words2[j] );
	        	}

	        	s.add(each.toString());
        	}
        }
        for ( int i = 0; i < words1.length; i++) {
            
        }

        return Iterables.toArray( s, String.class);
    }
}

/**
 * 
 */
package uk.co.recipes.parse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Collection;
import java.util.List;

import org.testng.annotations.Test;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
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
        assertThat( split("caster sugar", "vanilla sugar"), is(new String[]{"caster sugar", "vanilla sugar"}));
        assertThat( split("caster cane sugar", "vanilla cane sugar"), is(new String[]{"caster cane sugar", "vanilla cane sugar"}));
        assertThat( split("red wine", "red blood"), is(new String[]{"red wine", "red blood"}));
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

    	int maxNumWordsToSkip  = Math.max( words1.length, words2.length);
    	int startIndex = 0;

    	// Skip shared prefix words
    	while (words1[startIndex].equalsIgnoreCase( words2[startIndex] )) {
    		startIndex++;
    	}

    	// Skip shared Suffix words
    	int k = 1;
    	while (words1[ words1.length - k].equalsIgnoreCase( words2[ words2.length - k] )) {
    		maxNumWordsToSkip--;
    		k++;
    	}

        for ( int numWordsToSkip = 1; numWordsToSkip < maxNumWordsToSkip; numWordsToSkip++)
        {
        	if ( numWordsToSkip + startIndex < words1.length) {
	        	StringBuilder each = new StringBuilder();

	        	for ( int i = startIndex; i < words1.length - numWordsToSkip; i++) {
	        		if ( each.length() > 0) {
	        			each.append(" ");
	        		}
	        		each.append( words1[i] );
	        	}

	        	each.append( " " + Joiner.on(' ').join(wordsColl2) );

	        	s.add(each.toString());
        	}

        	if ( numWordsToSkip + startIndex < words2.length) {
            	StringBuilder each = new StringBuilder();

	        	each.append( Joiner.on(' ').join(wordsColl1) );

	        	for ( int i = numWordsToSkip; i < words2.length - startIndex; i++) {
	        		if ( each.length() > 0) {
	        			each.append(" ");
	        		}
	        		each.append( words2[i] );
	        	}

	        	s.add(each.toString());
        	}
        }

        return Iterables.toArray( s, String.class);
    }
}

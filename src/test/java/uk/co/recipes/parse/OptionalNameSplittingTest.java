/**
 * 
 */
package uk.co.recipes.parse;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.elasticsearch.common.collect.Iterables;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


/**
 * TODO
 * 
 * @author andrewr
 *
 */
public class OptionalNameSplittingTest {

    @Test
    public void testSplitting() {
        assertThat( split("Chinese cooking wine", "dry sherry"), is(new String[]{""}));
        assertThat( split("beef", "lamb"), is(new String[]{""}));
        assertThat( split("Grated Kefalotyri cheese", "Pecorino Romano"), is(new String[]{""}));
        assertThat( split("caster sugar", "vanilla sugar"), is(new String[]{""}));
        assertThat( split("Water", "Chicken Stock"), is(new String[]{""}));
        assertThat( split("dried", "fresh kaffir lime leaves"), is(new String[]{""}));
        assertThat( split("espresso", "strong instant coffee"), is(new String[]{""}));
        assertThat( split("Chinese cooking wine", "dry sherry"), is(new String[]{""}));
    }

    private String[] split( final String s1, final String s2) {
        // FIXME Factor out constant
        final String[] words1 = Iterables.toArray( Splitter.on(' ').trimResults().splitToList(s1), String.class);
        final String[] words2 = Iterables.toArray( Splitter.on(' ').trimResults().splitToList(s2), String.class);
 
        List<String> l1 = ngrams( 2, s1);
        System.out.println(l1);

        List<String> s = Lists.newArrayList();

        for ( int i = 0; i < words1.length; i++) {
            
        }

        return Iterables.toArray( s, String.class);
    }

    public static List<String> ngrams(int n, String str) {
        List<String> ngrams = new ArrayList<String>();
        String[] words = str.split(" ");
        for (int i = 0; i < words.length - n + 1; i++)
            ngrams.add(concat(words, i, i+n));
        return ngrams;
    }

    public static String concat(String[] words, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++)
            sb.append((i > start ? " " : "") + words[i]);
        return sb.toString();
    }
}

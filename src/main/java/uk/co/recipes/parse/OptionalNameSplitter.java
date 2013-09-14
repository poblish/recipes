/**
 * 
 */
package uk.co.recipes.parse;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Splits an and/or pair into all possible (English) word combinations
 *
 * @author andrewregan
 *
 */
public class OptionalNameSplitter {

	private final static Splitter WORD_SPLITTER = Splitter.on(' ').trimResults();
	private final static Joiner WORD_JOINER = Joiner.on(' ');


	public SplitResults split( final String s1, final String s2) {

		final Collection<String> wordsColl1 = WORD_SPLITTER.splitToList(s1);
		final Collection<String> wordsColl2 = WORD_SPLITTER.splitToList(s2);

		final String[] words1 = Iterables.toArray( wordsColl1, String.class);
		final String[] words2 = Iterables.toArray( wordsColl2, String.class);

		final List<String> poss1 = Lists.newArrayList();
		final List<String> poss2 = Lists.newArrayList();

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

				each.append( " " + WORD_JOINER.join(wordsColl2) );

				poss2.add(each.toString());
			}

			if ( numWordsToSkip + startIndex < words2.length) {
				StringBuilder each = new StringBuilder();

				each.append( WORD_JOINER.join(wordsColl1) );

				for ( int i = numWordsToSkip; i < words2.length - startIndex; i++) {
					if ( each.length() > 0) {
						each.append(" ");
					}
					each.append( words2[i] );
				}

				poss1.add(each.toString());
			}
		}

		poss1.add(s1);
		poss2.add(s2);

		return new SplitResults( Iterables.toArray( poss1, String.class), Iterables.toArray( poss2, String.class));
	}
}

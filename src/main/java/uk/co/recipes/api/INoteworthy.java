/**
 *
 */
package uk.co.recipes.api;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Has 'notes'
 *
 * @author andrewregan
 */
public interface INoteworthy {

    void addNote(Locale inLocale, String inNote);
    void addNotes(Locale inLocale, Collection<String> inNotes);
    Map<Locale,List<String>> getNotes();
}

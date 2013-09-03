/**
 * 
 */
package uk.co.recipes.parse;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;


/**
 * TODO
 * 
 * @author andrewr
 *
 */
public class AdjustedName {

    private String adjustedName;
    private Collection<String> notesToAdd;

    /**
     * 
     */
    public AdjustedName( final String inName, final Collection<String> inNotes) {
        adjustedName = checkNotNull(inName);
        notesToAdd = checkNotNull(inNotes);
    }

    public String getName() {
        return adjustedName;
    }

    public Collection<String> getNotes() {
        return notesToAdd;
    }
}

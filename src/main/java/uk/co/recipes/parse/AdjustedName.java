/**
 *
 */
package uk.co.recipes.parse;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * TODO
 *
 * @author andrewr
 */
public class AdjustedName {

    private String adjustedName;
    private Collection<String> notesToAdd;

    /**
     *
     */
    public AdjustedName(final String inName, final Collection<String> inNotes) {
        adjustedName = checkNotNull(inName);
        notesToAdd = checkNotNull(inNotes);
    }

    public String getName() {
        return adjustedName;
    }

    public Collection<String> getNotes() {
        return notesToAdd;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(adjustedName, notesToAdd);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AdjustedName)) {
            return false;
        }
        final AdjustedName other = (AdjustedName) obj;
        return Objects.equal(adjustedName, other.adjustedName) && Objects.equal(notesToAdd, other.notesToAdd);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", adjustedName).add("notes", notesToAdd).toString();
    }
}

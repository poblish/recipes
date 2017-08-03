/**
 *
 */
package uk.co.recipes.parse;

import uk.co.recipes.api.IQuantity;

import java.util.Collection;

/**
 * TODO
 *
 * @author andrewregan
 */
public class DeferralStatus {

    private String[] names;
    private IQuantity q;
    private String note;
    private Collection<String> notes;

    public DeferralStatus(final String[] inNamePossibilities, final IQuantity inQuantity, final String inNote, final Collection<String> inExtraNotes) {
        names = inNamePossibilities;
        q = inQuantity;
        note = inNote;
        notes = inExtraNotes;
    }

    public String[] getNamePossibilities() {
        return names;
    }

    public String getOriginalName() {
        return names[names.length - 1];
    }

    public IQuantity getQuantity() {
        return q;
    }

    public String getNote() {
        return note;
    }

    public Collection<String> getExtraNotes() {
        return notes;
    }
}
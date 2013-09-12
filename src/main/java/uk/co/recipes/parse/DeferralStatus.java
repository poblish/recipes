/**
 * 
 */
package uk.co.recipes.parse;

import java.util.Collection;

import uk.co.recipes.api.IQuantity;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class DeferralStatus {

	private String name;
	private IQuantity q;
	private String note;
	private Collection<String> notes;

	public DeferralStatus( final String inItemName, final IQuantity inQuantity, final String inNote, final Collection<String> inExtraNotes) {
		name = inItemName;
		q = inQuantity;
		note = inNote;
		notes = inExtraNotes;
	}

	public String getName() {
		return name;
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
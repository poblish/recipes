/**
 * 
 */
package uk.co.recipes;

import org.elasticsearch.common.Preconditions;

import uk.co.recipes.api.IUser;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class User implements IUser {

	private final static long UNSET_ID = -1L;

	private long id = UNSET_ID;

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId( long inId) {
		if ( id == UNSET_ID && inId == UNSET_ID) {
			// Let Jackson off...
			return;
		}

		Preconditions.checkArgument( inId >= 0, "New Id must be >= 0 [" + inId +"]");
		Preconditions.checkState( id == UNSET_ID, "Cannot change Item Id");
		id = inId;
	}
}
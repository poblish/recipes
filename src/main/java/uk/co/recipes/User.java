/**
 * 
 */
package uk.co.recipes;

import org.elasticsearch.common.Preconditions;

import uk.co.recipes.api.IUser;

import com.google.common.base.Objects;

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

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public boolean equals( Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof User)) {
			return false;
		}
		final User other = (User) obj;
		return ( id == other.id);
	}

	public String toString() {
		return Objects.toStringHelper(this).omitNullValues()
						.add( "id", ( id == UNSET_ID) ? "NEW" : Long.valueOf(id))
						.toString();
	}
}
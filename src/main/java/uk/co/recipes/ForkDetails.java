/**
 * 
 */
package uk.co.recipes;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import org.joda.time.DateTime;

import uk.co.recipes.api.IForkDetails;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

/**
 * TODO
 *
 * @author andrewregan
 *
 */
public class ForkDetails implements IForkDetails {

	private long originalId;
	private String originalTitle;
	private IUser originalUser;
	private DateTime forkTime;

    @JsonCreator
	public ForkDetails( @JsonProperty("originalId") final long inOriginalId, @JsonProperty("originalTitle") final String inTitle, @JsonProperty("forkTime") final DateTime inTime, @JsonProperty("originalUser") final IUser inUser) {
		originalId = inOriginalId;
		originalTitle = checkNotNull(inTitle);
		forkTime = checkNotNull(inTime);
		originalUser = checkNotNull(inUser);
	}

	public ForkDetails( final IRecipe inOriginal) {
		originalId = inOriginal.getId();
		originalTitle = inOriginal.getTitle();
		originalUser = inOriginal.getCreator();
		forkTime = new DateTime();
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IForkDetails#getOriginalId()
	 */
	@Override
	public long getOriginalId() {
		return originalId;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IForkDetails#getOriginalTitle()
	 */
	@Override
	public String getOriginalTitle() {
		return originalTitle;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IForkDetails#getOriginalUser()
	 */
	@Override
	public IUser getOriginalUser() {
		return originalUser;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IForkDetails#getForkTime()
	 */
	@Override
	public DateTime getForkTime() {
		return forkTime;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode( originalId, originalTitle, forkTime, originalUser);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ForkDetails)) {
			return false;
		}

		final ForkDetails other = (ForkDetails) obj;
		return originalId == other.originalId && Objects.equal( originalTitle, other.originalTitle) && Objects.equal( forkTime, other.forkTime) && Objects.equal( originalUser, other.originalUser);
	}

	public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
                        .add( "originalId", originalId)
                        .add( "originalTitle", originalTitle)
                        .add( "creator", originalUser)
                        .add( "forkTime", forkTime)
						.toString();
	}
}

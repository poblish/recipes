/**
 * 
 */
package uk.co.recipes;

import uk.co.recipes.api.IForkDetails;
import uk.co.recipes.api.IRecipe;
import uk.co.recipes.api.IUser;

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

	public ForkDetails( final IRecipe inOriginal) {
		originalId = inOriginal.getId();
		originalTitle = inOriginal.getTitle();
		originalUser = null; // inOriginal.getCreator();
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
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode( originalId, originalTitle, originalUser);
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
		return originalId == other.originalId && Objects.equal( originalTitle, other.originalTitle) && Objects.equal( originalUser, other.originalUser);
	}
}

/**
 * 
 */
package uk.co.recipes;

import uk.co.recipes.api.IQuantity;
import uk.co.recipes.api.IUnit;

import com.google.common.base.Objects;

/**
 * TODO
 * 
 * @author andrewregan
 *
 */
public class Quantity implements IQuantity {

	private IUnit units;
	private int number;

	/**
	 * @param units
	 * @param number
	 */
	public Quantity(IUnit units, int number) {
		this.units = units;
		this.number = number;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IQuantity#units()
	 */
	@Override
	public IUnit units() {
		// TODO Auto-generated method stub
		return units;
	}

	/* (non-Javadoc)
	 * @see uk.co.recipes.api.IQuantity#number()
	 */
	@Override
	public int number() {
		// TODO Auto-generated method stub
		return number;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode( units, number);
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
		if (!(obj instanceof Quantity)) {
			return false;
		}
		final Quantity other = (Quantity) obj;
		return number == other.number && Objects.equal( units, other.units);
	}
}

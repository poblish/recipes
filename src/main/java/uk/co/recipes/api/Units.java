/**
 *
 */
package uk.co.recipes.api;

import com.google.common.base.Preconditions;

/**
 * TODO
 *
 * @author andrewregan
 */
public enum Units implements IUnit {

    INSTANCES,

    KG, GRAMMES("g", "g"), POUNDS(" lb"), OUNCES(" oz"), ML, CM, INCH, MM,

    TSP(" tsp"), HEAPED_TSP, ROUNDED_TSP,
    TBSP(" tbsp"), HEAPED_TBSP, ROUNDED_TBSP,

    DASH, FEW, LITTLE, SPLASHES(" splash", " splashes"), DRIZZLE, SQUEEZE,

    BUNCHES(" bunch"),
    BIG_BUNCHES,
    SMALL_BUNCHES(" small bunch"),

    DROP, HANDFUL, PINCH, BIG_PINCH, KNOB, CLOVE, STICK, PIECE, GLASS, BOTTLE, CUP, POT, JAR, PACKET, QUART, LITRE;

    private final String displaySingular;
    private final String displayPlural;

    Units() {
        this.displaySingular = this.displayPlural = " " + this.name().toLowerCase().replace('_', ' ');
    }

    Units(final String displaySingular) {
        this(displaySingular, displaySingular + "s");
    }

    Units(final String displaySingular, final String displayPlural) {
        // FIXME Use properties!!!
        this.displaySingular = Preconditions.checkNotNull(displaySingular);
        this.displayPlural = Preconditions.checkNotNull(displayPlural);
    }

    public String getDisplayString(boolean inPlural) {
        return inPlural ? displayPlural : displaySingular;
    }
}
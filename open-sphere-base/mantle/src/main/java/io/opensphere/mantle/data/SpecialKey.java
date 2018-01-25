package io.opensphere.mantle.data;

import java.io.Serializable;

/**
 * The Interface SpecialKey.
 *
 * Note that the hashcode and equals method of any implementor should only
 * compare the key name and ignore the unit.
 */
public interface SpecialKey extends Serializable
{
    /** The Constant UNDEFINED_UNIT. */
    String UNDEFINED_UNIT = "UNDEFINED_UNIT";

    /**
     * Gets the key name.
     *
     * @return the key name
     */
    String getKeyName();

    /**
     * Gets the key unit. Should never be null, must return UNDEFINED_UNIT if
     * not set.
     *
     * @return the key unit or UNDEFINED_UNIT
     */
    Object getKeyUnit();

    /**
     * Checks if unit is defined.
     *
     * @return true, if unit is defined
     */
    boolean isUnitDefined();
}

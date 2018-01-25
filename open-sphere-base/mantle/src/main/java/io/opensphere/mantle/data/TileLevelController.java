package io.opensphere.mantle.data;

/**
 * The Interface TileLevelController.
 */
public interface TileLevelController
{
    /**
     * Gets the current tile generation.
     *
     * @return the current tile generation.
     */
    int getCurrentGeneration();

    /**
     * Gets the division hold generation.
     *
     * @return the division hold generation
     */
    int getDivisionHoldGeneration();

    /**
     * Gets the max generation.
     *
     * @return the max generation (-1 implies unknown or not set).
     */
    int getMaxGeneration();

    /**
     * Checks if is division override.
     *
     * @return true, if is division override
     */
    boolean isDivisionOverride();

    /**
     * Sets the division hold generation.
     *
     * @param gen the new division hold generation
     */
    void setDivisionHoldGeneration(int gen);

    /**
     * Sets the division override.
     *
     * @param enabled the new division override
     */
    void setDivisionOverride(boolean enabled);
}

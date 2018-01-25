package io.opensphere.core.geometry;

/**
 * Interface for geometries that request additional data after they are created.
 */
@FunctionalInterface
interface DataRequestingGeometry
{
    /**
     * Get the data request agent.
     *
     * @return The agent.
     */
    DataRequestAgent getDataRequestAgent();
}

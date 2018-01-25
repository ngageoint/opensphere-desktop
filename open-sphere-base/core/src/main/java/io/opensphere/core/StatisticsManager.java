package io.opensphere.core;

import io.opensphere.core.orwell.ApplicationStatistics;
import io.opensphere.core.orwell.GraphicsStatistics;
import io.opensphere.core.orwell.SessionStatistics;
import io.opensphere.core.orwell.SystemStatistics;
import io.opensphere.core.orwell.UserStatistics;

/**
 * A simple manager interface in which the various objects used to gather and maintain statistics.
 */
public interface StatisticsManager
{
    /**
     * Gets the container in which the statistics describing the operating system are stored.
     *
     * @return the container in which the statistics describing the operating system are stored.
     */
    SystemStatistics getSystemStatistics();

    /**
     * Gets the container in which the statistics describing the graphics system are stored.
     *
     * @return the container in which the statistics describing the graphics system are stored.
     */
    GraphicsStatistics getGraphicsStatistics();

    /**
     * Gets the container in which the statistics describing the application are stored.
     *
     * @return the container in which the statistics describing the application are stored.
     */
    ApplicationStatistics getApplicationStatistics();

    /**
     * Gets the container describing the user currently executing the application.
     *
     * @return the container describing the user currently executing the application.
     */
    UserStatistics getUserStatistics();

    /**
     * Gets the statistics container describing the user's current session.
     *
     * @return the statistics container describing the user's current session.
     */
    SessionStatistics getSessionStatistics();
}

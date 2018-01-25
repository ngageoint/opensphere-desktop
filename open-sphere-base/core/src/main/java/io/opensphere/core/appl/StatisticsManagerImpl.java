package io.opensphere.core.appl;

import io.opensphere.core.StatisticsManager;
import io.opensphere.core.orwell.ApplicationStatistics;
import io.opensphere.core.orwell.GraphicsStatistics;
import io.opensphere.core.orwell.SessionStatistics;
import io.opensphere.core.orwell.SystemStatistics;
import io.opensphere.core.orwell.UserStatistics;

/**
 * A default implementation of the {@link StatisticsManager} interface, providing the containers in which statistics are stored.
 * Note that gathering of statistics is beyond the scope of this class. Statistics are to be provided by various parts of the
 * system, and provided to the containers managed within an instance of this class.
 */
public class StatisticsManagerImpl implements StatisticsManager
{
    /**
     * The container in which the statistics describing the application are stored.
     */
    private final ApplicationStatistics myApplicationStatistics;

    /**
     * The container in which the statistics describing the graphics system are stored.
     */
    private final GraphicsStatistics myGraphicsStatistics;

    /**
     * The statistics container describing the user's current session.
     */
    private final SessionStatistics mySessionStatistics;

    /**
     * The container in which the statistics describing the operating system are stored.
     */
    private final SystemStatistics mySystemStatistics;

    /**
     * The container describing the user currently executing the application.
     */
    private final UserStatistics myUserStatistics;

    /**
     * Creates a new manager instance, initializing the containers in which statistics are stored. Note that no statistics are
     * gathered at this time.
     */
    public StatisticsManagerImpl()
    {
        myApplicationStatistics = new ApplicationStatistics();
        myGraphicsStatistics = new GraphicsStatistics();
        mySessionStatistics = new SessionStatistics();
        mySystemStatistics = new SystemStatistics();
        myUserStatistics = new UserStatistics();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.StatisticsManager#getSystemStatistics()
     */
    @Override
    public SystemStatistics getSystemStatistics()
    {
        return mySystemStatistics;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.StatisticsManager#getGraphicsStatistics()
     */
    @Override
    public GraphicsStatistics getGraphicsStatistics()
    {
        return myGraphicsStatistics;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.StatisticsManager#getApplicationStatistics()
     */
    @Override
    public ApplicationStatistics getApplicationStatistics()
    {
        return myApplicationStatistics;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.StatisticsManager#getUserStatistics()
     */
    @Override
    public UserStatistics getUserStatistics()
    {
        return myUserStatistics;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.StatisticsManager#getSessionStatistics()
     */
    @Override
    public SessionStatistics getSessionStatistics()
    {
        return mySessionStatistics;
    }
}

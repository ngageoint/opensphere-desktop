package io.opensphere.server.toolbox.impl;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.Constants;
import io.opensphere.server.services.ServerRefreshEvent;
import io.opensphere.server.toolbox.ServerRefreshController;

/**
 * The Class ServerRefreshControllerImpl.
 */
public class ServerRefreshControllerImpl implements ServerRefreshController
{
    /** The default refresh rate in minutes. */
    private static final int DEFAULT_REFRESH_RATE = 15;

    /** Logging reference. */
    private static final Logger LOGGER = Logger.getLogger(ServerRefreshControllerImpl.class);

    /** The core preference key that stores whether refresh is enabled. */
    private static final String REFRESH_ENABLED_PREFERENCE_KEY = "RefreshEnabled";

    /** The core preference key that stores the refresh interval in minutes. */
    private static final String REFRESH_INTERVAL_PREFERENCE_KEY = "RefreshIntervalMinutes";

    /** String to use when retrieving the top-level preferences. */
    private final String myPrefsTopic;

    /** Flag indicating whether refresh is currently enabled. */
    private boolean myRefreshEnabled;

    /** The current refresh interval. */
    private int myRefreshInterval;

    /** Interval timer that triggers refresh events. */
    private Timer myTimer;

    /** The core toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new implementation of the {@link ServerRefreshController}.
     *
     * @param preferencesTopic the top-level preferences topic
     * @param toolbox the core toolbox
     */
    public ServerRefreshControllerImpl(String preferencesTopic, Toolbox toolbox)
    {
        myToolbox = toolbox;
        myPrefsTopic = preferencesTopic;

        Preferences serverPrefs = getServerPrefs();
        if (serverPrefs != null)
        {
            myRefreshEnabled = serverPrefs.getBoolean(REFRESH_ENABLED_PREFERENCE_KEY, false);
            myRefreshInterval = serverPrefs.getInt(REFRESH_INTERVAL_PREFERENCE_KEY, DEFAULT_REFRESH_RATE);
        }

        if (myRefreshEnabled)
        {
            startTimer();
        }
    }

    @Override
    public int getRefreshInterval()
    {
        return myRefreshInterval;
    }

    @Override
    public boolean isRefreshEnabled()
    {
        return myRefreshEnabled;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.server.toolbox.ServerRefreshController#restoreDefaults()
     */
    @Override
    public void restoreDefaults()
    {
        setRefreshEnabled(false);
        setRefreshInterval(DEFAULT_REFRESH_RATE);
    }

    @Override
    public void setRefreshEnabled(boolean isEnabled)
    {
        boolean isChanged = isEnabled != myRefreshEnabled;
        myRefreshEnabled = isEnabled;
        if (isChanged)
        {
            if (isEnabled)
            {
                startTimer();
            }
            else
            {
                stopTimer();
            }
            savePrefs();
        }
    }

    @Override
    public void setRefreshInterval(int interval)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Setting server refresh interval to " + interval + " minutes");
        }
        boolean isChanged = interval != myRefreshInterval;
        if (isChanged)
        {
            myRefreshInterval = interval;
            if (myRefreshEnabled)
            {
                startTimer();
            }
            savePrefs();
        }
    }

    /**
     * Gets the server preferences from the core preferences registry.
     *
     * @return the server-specific preferences
     */
    private Preferences getServerPrefs()
    {
        if (myToolbox == null || myToolbox.getPreferencesRegistry() == null)
        {
            return null;
        }
        return myToolbox.getPreferencesRegistry().getPreferences(myPrefsTopic);
    }

    /**
     * Save the server refresh preferences.
     */
    private void savePrefs()
    {
        Preferences serverPrefs = getServerPrefs();
        if (serverPrefs != null)
        {
            serverPrefs.putBoolean(REFRESH_ENABLED_PREFERENCE_KEY, myRefreshEnabled, this);
            serverPrefs.putInt(REFRESH_INTERVAL_PREFERENCE_KEY, myRefreshInterval, this);
        }
    }

    /**
     * Send an event that prompts a server refresh.
     */
    private void sendRefreshEvent()
    {
        ServerRefreshEvent event = new ServerRefreshEvent(this, new Date(System.currentTimeMillis()));
        myToolbox.getEventManager().publishEvent(event);
    }

    /**
     * Start timer. If the timer is already running, stop it and start it with
     * the new interval.
     */
    private void startTimer()
    {
        stopTimer();
        myTimer = new Timer(true);
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                sendRefreshEvent();
            }
        };
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Starting periodic Server Refreshes every " + myRefreshInterval + " minutes.");
        }
        long intervalMillis = (long)myRefreshInterval * Constants.SECONDS_PER_MINUTE * Constants.MILLI_PER_UNIT;
        myTimer.scheduleAtFixedRate(task, intervalMillis, intervalMillis);
    }

    /**
     * Stop the interval timer.
     */
    private void stopTimer()
    {
        if (myTimer != null)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Stopping periodic Server Refreshes.");
            }
            myTimer.cancel();
            myTimer = null;
        }
    }
}

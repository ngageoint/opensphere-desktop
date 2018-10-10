package io.opensphere.core.util.swing;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowStateListener;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;

/**
 * The Class FramePreferencesMonitor.
 */
public class FramePreferencesMonitor
{
    /**
     * The property name for the component's user property telling whether the
     * component has been rolled up.
     */
    public static final String ROLL_PROPERTY = "frameRolledUp";

    /** The window. */
    private final Container myWindow;

    /** The window height. */
    private final String myWindowHeightPrefName;

    /** The window is maximized. */
    private final String myWindowIsMaximizedPrefName;

    /** The window preferences. */
    private final Preferences myWindowPreferences;

    /** The preference name for whether the window has be rolled up. */
    private final String myWindowRolledUpPrefName;

    /** The window width. */
    private final String myWindowWidthPrefName;

    /** The window x loc. */
    private final String myWindowXLocPrefName;

    /** The window y loc. */
    private final String myWindowYLocPrefName;

    /** The component listener. */
    private final ComponentListener myComponentListener;

    /** The window state listener. */
    private WindowStateListener myWindowStateListener;

    /** The property change listener. */
    private PropertyChangeListener myPropertyChangeListener;

    /**
     * Instantiates a new window preferences monitor.
     *
     * @param prefsReg the {@link PreferencesRegistry}
     * @param prefNamePrefix the window preferences name prefix
     * @param container the w
     * @param defaultSizeAndLocation the default size and location of the window
     */
    public FramePreferencesMonitor(PreferencesRegistry prefsReg, String prefNamePrefix, Container container,
            Rectangle defaultSizeAndLocation)
    {
        if (container == null)
        {
            throw new IllegalArgumentException();
        }

        myWindowPreferences = prefsReg.getPreferences(FramePreferencesMonitor.class);

        myWindow = container;

        myComponentListener = new ComponentListener()
        {
            @Override
            public void componentHidden(ComponentEvent e)
            {
            }

            @Override
            public void componentMoved(ComponentEvent e)
            {
                savePreferences();
            }

            @Override
            public void componentResized(ComponentEvent e)
            {
                savePreferences();
            }

            @Override
            public void componentShown(ComponentEvent e)
            {
            }
        };
        myWindow.addComponentListener(myComponentListener);

        myWindowXLocPrefName = prefNamePrefix + ".X";
        myWindowYLocPrefName = prefNamePrefix + ".Y";
        myWindowWidthPrefName = prefNamePrefix + ".W";
        myWindowHeightPrefName = prefNamePrefix + ".H";
        myWindowIsMaximizedPrefName = prefNamePrefix + ".MAXIMIZED";
        myWindowRolledUpPrefName = prefNamePrefix + ".ROLLED";

        int prefX = myWindowPreferences.getInt(myWindowXLocPrefName, defaultSizeAndLocation.x);
        int prefY = myWindowPreferences.getInt(myWindowYLocPrefName, defaultSizeAndLocation.y);
        int prefWidth = myWindowPreferences.getInt(myWindowWidthPrefName, defaultSizeAndLocation.width);
        int prefHeight = myWindowPreferences.getInt(myWindowHeightPrefName, defaultSizeAndLocation.height);
        boolean prefMax = myWindowPreferences.getBoolean(myWindowIsMaximizedPrefName, false);
        boolean prefRoll = myWindowPreferences.getBoolean(myWindowRolledUpPrefName, false);

        myWindow.setSize(new Dimension(prefWidth, prefHeight));
        myWindow.setLocation(prefX, prefY);

        if (myWindow instanceof Frame)
        {
            Frame frame = (Frame)myWindow;
            if (prefMax)
            {
                frame.setExtendedState(Frame.MAXIMIZED_BOTH);
            }

            myWindowStateListener = e -> savePreferences();
            frame.addWindowStateListener(myWindowStateListener);
        }

        if (myWindow instanceof JComponent)
        {
            JComponent comp = (JComponent)myWindow;
            comp.putClientProperty(ROLL_PROPERTY, Boolean.valueOf(prefRoll));
            myPropertyChangeListener = evt -> savePreferences();
            comp.addPropertyChangeListener(ROLL_PROPERTY, myPropertyChangeListener);
        }
    }

    /** Perform any required cleanup before releasing this monitor. */
    public synchronized void close()
    {
        myWindow.removeComponentListener(myComponentListener);
        if (myWindow instanceof Frame && myWindowStateListener != null)
        {
            ((Frame)myWindow).removeWindowStateListener(myWindowStateListener);
        }
        if (myWindow instanceof JComponent && myPropertyChangeListener != null)
        {
            ((JComponent)myWindow).removePropertyChangeListener(ROLL_PROPERTY, myPropertyChangeListener);
        }
    }

    /**
     * Save the current preferences.
     */
    private synchronized void savePreferences()
    {
        boolean maximized = false;
        if (myWindow instanceof Frame)
        {
            maximized = (((Frame)myWindow).getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH;
        }
        myWindowPreferences.putBoolean(myWindowIsMaximizedPrefName, maximized, this);

        boolean rolledUp = false;
        if (myWindow instanceof JComponent)
        {
            Boolean rolledProp = (Boolean)((JComponent)myWindow).getClientProperty(ROLL_PROPERTY);
            if (rolledProp != null)
            {
                rolledUp = rolledProp.booleanValue();
            }
        }
        myWindowPreferences.putBoolean(myWindowRolledUpPrefName, rolledUp, this);

        Rectangle bounds = myWindow.getBounds();

        myWindowPreferences.putInt(myWindowXLocPrefName, bounds.x, this);
        myWindowPreferences.putInt(myWindowYLocPrefName, bounds.y, this);

        if (!rolledUp)
        {
            myWindowPreferences.putInt(myWindowWidthPrefName, bounds.width, this);
            myWindowPreferences.putInt(myWindowHeightPrefName, bounds.height, this);
        }
    }
}

package io.opensphere.core.api.adapter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.Arrays;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.PluginProperty;
import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * Abstract plug-in implementation that installs a menu item.
 */
public abstract class AbstractMenuItemPlugin extends PluginAdapter
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AbstractMenuItemPlugin.class);

    /** Key for visibility preference. */
    private static final String PREF_KEY_VISIBLE = "visible";

    /** The menu item. */
    private JMenuItem myMenuItem;

    /** The preferences for this menu item. */
    private Preferences myPreferences;

    /** The Remember visibility state flag. */
    private final boolean myRememberVisibilityState;

    /** The application toolbox. */
    private Toolbox myToolbox;

    /**
     * Constructor with remember visibility state flag.
     *
     * @param rememberVisibilityState Indicates if the visibility state should
     *            be persisted in the preferences.
     */
    public AbstractMenuItemPlugin(boolean rememberVisibilityState)
    {
        myRememberVisibilityState = rememberVisibilityState;
    }

    /**
     * Get the application toolbox.
     *
     * @return The toolbox.
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    @Override
    public void initialize(PluginLoaderData plugindata, final Toolbox toolbox)
    {
        myToolbox = toolbox;

        String parentMenuName = null;
        String menuButtonLabel = null;
        KeyStroke hotKeyStroke = null;

        Integer menuPosition = null;
        for (PluginProperty pluginProperty : plugindata.getPluginProperty())
        {
            String key = pluginProperty.getKey();
            if ("parentMenuName".equals(key))
            {
                parentMenuName = pluginProperty.getValue();
            }
            else if ("menuButtonLabel".equals(key))
            {
                menuButtonLabel = pluginProperty.getValue();
            }
            else if ("menuPosition".equals(key))
            {
                try
                {
                    menuPosition = Integer.valueOf(pluginProperty.getValue());
                }
                catch (NumberFormatException e)
                {
                    LOGGER.warn("Unable to determine menu position by parsing '" + pluginProperty.getValue() + "' as an integer",
                            e);
                    menuPosition = null;
                }
            }
            else if ("menuAccelerator".equals(key))
            {
                hotKeyStroke = getAcceleratorKeyStroke(pluginProperty);
            }
            else
            {
                LOGGER.warn("Unexpected plugin property for plugin [" + plugindata.getId() + "]: " + key);
            }
        }
        if (parentMenuName == null)
        {
            throw new IllegalArgumentException("No parentMenuName in plugin configuration.");
        }
        myPreferences = myToolbox.getPreferencesRegistry().getPreferences("MenuItem." + menuButtonLabel);

        initializeMenu(toolbox, parentMenuName, menuButtonLabel, hotKeyStroke, menuPosition);
    }

    /**
     * Called when the menu button is de-selected.
     */
    protected abstract void buttonDeselected();

    /**
     * Called when the menu button is selected.
     */
    protected abstract void buttonSelected();

    /**
     * Creates the menu item.
     *
     * @param menuButtonLabel The menu button label.
     * @return JCheckBoxMenuItem The menu item.
     */
    protected JCheckBoxMenuItem createMenuItem(String menuButtonLabel)
    {
        return new JCheckBoxMenuItem(menuButtonLabel);
    }

    /**
     * Get my menu item. This is {@code null} prior to initialization.
     *
     * @return The menu item.
     */
    protected JMenuItem getMenuItem()
    {
        return myMenuItem;
    }

    /**
     * Get the parent menu for my menu item.
     *
     * @param parentMenuName A '/' separated string indicating the menu path.
     * @param toolbox A toolbox.
     * @return The desired menu, or {@code null} if it could not be
     *         found/created.
     */
    protected JMenu getParentMenu(String parentMenuName, Toolbox toolbox)
    {
        String[] menus = parentMenuName.split("/");
        return toolbox.getUIRegistry().getMenuBarRegistry().getMenu(menus[0], Arrays.copyOfRange(menus, 1, menus.length));
    }

    /**
     * Checks if is remember visibility state.
     *
     * @return true, if is remember visibility state
     */
    protected boolean isRememberVisibilityState()
    {
        return myRememberVisibilityState;
    }

    /**
     * Gets the visibility preference.
     *
     * @return the visibility preference
     */
    protected boolean isVisibilityPreference()
    {
        return myPreferences.getBoolean(PREF_KEY_VISIBLE, false);
    }

    /**
     * Update visibility preference.
     */
    protected void updateVisibilityPreference()
    {
        if (myRememberVisibilityState)
        {
            myPreferences.putBoolean(PREF_KEY_VISIBLE, getMenuItem().isSelected(), this);
        }
    }

    /**
     * Attempts to map the menuAccelerator key to a KeyEvent and returns a
     * KeyStroke that uses the created key event.
     *
     * @param pluginProperty the plugin property
     * @return the accelerator key stroke
     */
    private KeyStroke getAcceleratorKeyStroke(PluginProperty pluginProperty)
    {
        KeyStroke hotKeyStroke = null;
        int keyCode = -1;
        Field[] fields = KeyEvent.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++)
        {
            Field f = fields[i];

            if (f.getName().equals(pluginProperty.getValue()))
            {
                f.setAccessible(true);
                try
                {
                    keyCode = ((Integer)f.get(null)).intValue();
                    hotKeyStroke = KeyStroke.getKeyStroke(keyCode, 0);
                }
                catch (IllegalArgumentException e)
                {
                    LOGGER.info("Cannot map keyevent: " + pluginProperty.getValue(), e);
                }
                catch (IllegalAccessException e)
                {
                    LOGGER.info("Cannot access keyevent for: " + pluginProperty.getValue(), e);
                }
            }
        }
        return hotKeyStroke;
    }

    /**
     * Initialize the menu for the plugin.
     *
     * @param toolbox The toolbox.
     * @param parentMenuName The parent menu name.
     * @param menuButtonLabel The menu button label.
     * @param hotKeyStroke The hot key.
     * @param menuPosition The menu position.
     */
    private void initializeMenu(final Toolbox toolbox, final String parentMenuName, final String menuButtonLabel,
            final KeyStroke hotKeyStroke, final Integer menuPosition)
    {
        EventQueueUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                JMenu menu = getParentMenu(parentMenuName, toolbox);
                if (menu == null)
                {
                    throw new IllegalArgumentException("Menu with name [" + parentMenuName + "] could not be found.");
                }
                myMenuItem = createMenuItem(menuButtonLabel);
                if (hotKeyStroke != null)
                {
                    myMenuItem.setAccelerator(hotKeyStroke);
                }
                myMenuItem.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent event)
                    {
                        if (myMenuItem.isSelected())
                        {
                            buttonSelected();
                            Quantify.collectMetric("mist3d.menu-bar.help." + myMenuItem.getText() + ".enabled");
                            updateVisibilityPreference();
                        }
                        else
                        {
                            buttonDeselected();
                            Quantify.collectMetric("mist3d.menu-bar.help." + myMenuItem.getText() + ".disabled");
                            updateVisibilityPreference();
                        }
                    }
                });

                if (menuPosition == null)
                {
                    menu.add(myMenuItem);
                }
                else
                {
                    menu.insert(myMenuItem, menuPosition.intValue());
                }

                // The subclass needs to be initialized before this is called.
                if (isRememberVisibilityState() && isVisibilityPreference())
                {
                    getMenuItem().doClick();
                }
            }
        });
    }
}

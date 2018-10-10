package io.opensphere.core.pipeline;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.event.ApplicationLifecycleEvent;
import io.opensphere.core.event.ApplicationLifecycleEvent.Stage;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.geometry.debug.ellipsoid.EllipsoidDebugUI;
import io.opensphere.core.pipeline.renderer.AbstractTileRenderer;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.swing.EventQueueUtilities;

/** The pipeline menu initializer. */
public class PipelineMenuInit
{
    /** Subscriber to be notified when the LAF is installed. */
    private EventListener<ApplicationLifecycleEvent> myLifecycleSubscriber = new EventListener<>()
    {
        /**
         * Flag used to determine if both the LAF has been installed and the
         * pipeline has been initialized.
         */
        private boolean myFlag;

        @Override
        public void notify(final ApplicationLifecycleEvent event)
        {
            EventQueueUtilities.invokeLater(() ->
            {
                if (event.getStage() == Stage.LAF_INSTALLED)
                {
                    myToolbox.getUIRegistry().getMenuBarRegistry()
                            .getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.DEBUG_MENU)
                            .add(AbstractTileRenderer.getDebugMenu(myToolbox.getMapManager()));
                    myToolbox.getUIRegistry().getMenuBarRegistry()
                            .getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.DEBUG_MENU)
                            .add(EllipsoidDebugUI.getDebugMenu(myToolbox));
                }
                if (event.getStage() == Stage.LAF_INSTALLED || event.getStage() == Stage.PIPELINE_INITIALIZED)
                {
                    if (myFlag)
                    {
                        installRestartInSafemodeMenu();

                        myToolbox.getEventManager().unsubscribe(ApplicationLifecycleEvent.class, myLifecycleSubscriber);

                        // Allow the subscriber to be cleaned up.
                        myLifecycleSubscriber = null;
                    }
                    else
                    {
                        myFlag = true;
                    }
                }
            });
        }

        /**
         * If not in safe mode, install a "Restart in safemode" menu item.
         */
        private void installRestartInSafemodeMenu()
        {
            if (!myToolbox.getPreferencesRegistry().getPreferences(Pipeline.class).getBoolean(mySafemodePrefsKey, true)
                    && Boolean.getBoolean("opensphere.enableRestart"))
            {
                final JMenuItem mi = new JMenuItem("Restart in safe mode");
                mi.addActionListener(e ->
                {
                    final Preferences prefs = myToolbox.getPreferencesRegistry().getPreferences(Pipeline.class);
                    prefs.putBoolean(mySafemodePrefsKey, true, this);
                    prefs.waitForPersist();
                    myToolbox.getSystemToolbox().requestRestart();
                });

                final JMenu fileMenu = myToolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR,
                        MenuBarRegistry.FILE_MENU);
                fileMenu.add(mi, fileMenu.getMenuComponentCount() - 1);
            }
        }
    };

    /** The key for the safemode preference. */
    private String mySafemodePrefsKey;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox.
     */
    public PipelineMenuInit(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    /**
     * Initialize.
     */
    public void init()
    {
        myToolbox.getEventManager().subscribe(ApplicationLifecycleEvent.class, myLifecycleSubscriber);
    }

    /**
     * Set the preferences key for the safemode.
     *
     * @param safemodePrefsKey The key.
     */
    public void setSafemodePrefsKey(String safemodePrefsKey)
    {
        mySafemodePrefsKey = safemodePrefsKey;
    }
}

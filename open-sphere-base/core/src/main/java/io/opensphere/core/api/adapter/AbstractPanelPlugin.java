package io.opensphere.core.api.adapter;

import java.awt.Component;

import javax.swing.JTabbedPane;

import io.opensphere.core.Plugin;
import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.PluginProperty;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.SharedComponentListener;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * An abstract implementation of {@link Plugin} that handles installing a panel
 * in a configurable parent tabbed panel. The plug-in configuration must include
 * a property with the name "parentComponentName", whose value will be used to
 * locate the tabbed pane to put my panel in.
 */
public abstract class AbstractPanelPlugin extends PluginAdapter
{
    /** The name of my parent component. */
    private String myParentComponentName;

    /** A listener to be notified when shared components are added. */
    private final SharedComponentListener mySharedComponentListener = new SharedComponentListener()
    {
        @Override
        public void componentAdded(String name)
        {
            if (name.equals(myParentComponentName))
            {
                Component component = myToolbox.getUIRegistry().getSharedComponentRegistry().getComponent(myParentComponentName);
                if (component instanceof JTabbedPane)
                {
                    final JTabbedPane tabbedPane = (JTabbedPane)component;
                    EventQueueUtilities.invokeLater(() -> addTab(tabbedPane));
                }
            }
        }

        @Override
        public void componentRemoved(String name)
        {
        }
    };

    /** The toolbox. */
    private Toolbox myToolbox;

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        myToolbox = toolbox;

        for (PluginProperty pluginProperty : plugindata.getPluginProperty())
        {
            if (pluginProperty.getKey().equals("parentComponentName"))
            {
                myParentComponentName = pluginProperty.getValue();
                break;
            }
        }
        if (myParentComponentName == null)
        {
            throw new IllegalArgumentException("No parentComponentName in plugin configuration.");
        }

        mySharedComponentListener.componentAdded(myParentComponentName);

        myToolbox.getUIRegistry().getSharedComponentRegistry().addListener(mySharedComponentListener);
    }

    @Override
    public void close()
    {
        myToolbox.getUIRegistry().getSharedComponentRegistry().removeListener(mySharedComponentListener);
    }

    /**
     * Method for subclasses to implement to add a pane to the tabbed pane.
     *
     * @param tabbedPane The tabbed pane.
     */
    protected abstract void addTab(JTabbedPane tabbedPane);

    /**
     * Accessor for the toolbox.
     *
     * @return The toolbox.
     */
    protected final Toolbox getToolbox()
    {
        return myToolbox;
    }
}

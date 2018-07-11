package io.opensphere.controlpanels.layers;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import io.opensphere.controlpanels.layers.importdata.ImportButtonMenuProvider;
import io.opensphere.controlpanels.layers.importdata.ImportButtonPanel;
import io.opensphere.controlpanels.layers.prefs.LayerManagerOptionsProvider;
import io.opensphere.controlpanels.toolbox.ControlPanelToolboxImpl;
import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractHUDFrameMenuItemPlugin;
import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.control.ui.ToolbarManager.ToolbarLocation;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.property.PluginPropertyUtils;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.IconButton;

/**
 * Plug-in that provides the timeline UIs.
 */
public class LayerManagementPlugin extends AbstractHUDFrameMenuItemPlugin
{
    /** The layer manager / data discovery UI / active data/layer frame. */
    private LayersFrame myDataDiscoveryFrame;

    /** The Import button menu provider. */
    private transient ImportButtonMenuProvider myImportButtonMenuProvider;

    /** The import button panel. */
    private transient ImportButtonPanel myImportButtonPanel;

    /** The Activation button. */
    private IconButton myLayerManagerActivationButton;

    /** The Options provider. */
    private transient LayerManagerOptionsProvider myOptionsProvider;

    /** The tool bar box. */
    private transient Box myToolBarBox;

    /**
     * Instantiates a new DataDiscoveryPlugin plugin.
     */
    public LayerManagementPlugin()
    {
        super(LayersFrame.TITLE, true, true);
    }

    @Override
    public void close()
    {
        myImportButtonMenuProvider.close();
    }

    @Override
    public void initialize(PluginLoaderData data, final Toolbox toolbox)
    {
        super.initialize(data, toolbox);
        myImportButtonMenuProvider = new ImportButtonMenuProvider(toolbox);
        myOptionsProvider = new LayerManagerOptionsProvider(toolbox);
        toolbox.getUIRegistry().getOptionsRegistry().addOptionsProvider(myOptionsProvider);

        Properties pluginProperties = PluginPropertyUtils.convertToProperties(data.getPluginProperty());
        toolbox.getPluginToolboxRegistry().registerPluginToolbox(new ControlPanelToolboxImpl(toolbox, pluginProperties));

        EventQueueUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                toolbox.getUIRegistry().getToolbarComponentRegistry().registerToolbarComponent(ToolbarLocation.NORTH, "Layers",
                        getToolbarBox(), 100, SeparatorLocation.NONE, new Insets(0, 2, 0, 2));

                // Ensure this is created in order to allow the 'add data' GUI
                // to be shown when the layer manager is not visible
                createInternalFrame(toolbox);
            }
        });
    }

    @Override
    protected LayersFrame createInternalFrame(Toolbox toolbox)
    {
        assert SwingUtilities.isEventDispatchThread();
        if (myDataDiscoveryFrame == null)
        {
            myDataDiscoveryFrame = new LayersFrame(toolbox);
            myDataDiscoveryFrame.setLocation(0, 20);
        }
        return myDataDiscoveryFrame;
    }

    /**
     * Creates the toolbar activation button.
     *
     * @return the j button
     */
    private JButton createToolbarActivationButton()
    {
        if (myLayerManagerActivationButton == null)
        {
            myLayerManagerActivationButton = new IconButton("Layers");
            IconUtil.setIcons(myLayerManagerActivationButton, IconType.STACK);
            myLayerManagerActivationButton.setToolTipText("Open the Layers Panel");
            myLayerManagerActivationButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    getHUDFrame().setVisible(!getHUDFrame().isVisible());
                }
            });
        }
        return myLayerManagerActivationButton;
    }

    /**
     * Gets the import button panel.
     *
     * @return the import button panel
     */
    private ImportButtonPanel getImportButtonPanel()
    {
        if (myImportButtonPanel == null)
        {
            myImportButtonPanel = new ImportButtonPanel(getToolbox().getUIRegistry().getContextActionManager(),
                    getToolbox().getEventManager(), "Add Data");
        }
        return myImportButtonPanel;
    }

    /**
     * Gets the toolbar box.
     *
     * @return the toolbar box
     */
    private Box getToolbarBox()
    {
        assert SwingUtilities.isEventDispatchThread();
        if (myToolBarBox == null)
        {
            myToolBarBox = Box.createHorizontalBox();
            myToolBarBox.add(Box.createHorizontalStrut(3));
            myToolBarBox.add(createToolbarActivationButton());
            myToolBarBox.add(Box.createHorizontalStrut(4));
            myToolBarBox.add(getImportButtonPanel());
        }
        return myToolBarBox;
    }
}

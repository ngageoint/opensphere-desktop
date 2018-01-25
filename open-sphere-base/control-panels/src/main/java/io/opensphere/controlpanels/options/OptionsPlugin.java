package io.opensphere.controlpanels.options;

import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JButton;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractHUDFrameMenuItemPlugin;
import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.control.ui.ToolbarManager.ToolbarLocation;
import io.opensphere.core.options.OptionsProvider;
import io.opensphere.core.options.OptionsRegistry;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.IconButton;

/**
 * The plug-in for the options user interface.
 */
public class OptionsPlugin extends AbstractHUDFrameMenuItemPlugin implements OptionsRegistry.OptionsRegistryListener
{
    /** The options frame. */
    private OptionsFrame myOptionsFrame;

    /**
     * Instantiates a new layer manager plugin.
     */
    public OptionsPlugin()
    {
        super(OptionsFrame.TITLE, true, true);
    }

    @Override
    public void initialize(PluginLoaderData data, Toolbox toolbox)
    {
        super.initialize(data, toolbox);

        toolbox.getUIRegistry().getToolbarComponentRegistry().registerToolbarComponent(ToolbarLocation.NORTH, "Settings",
                createToolbarActivationButton(), 350, SeparatorLocation.NONE, new Insets(0, 2, 0, 2));

        Icon optionsIcon = IconUtil.getNormalIcon(IconType.COGS);
        toolbox.getUIRegistry().getIconLegendRegistry().addIconToLegend(optionsIcon, "Settings",
                "Opens the 'Settings' dialog. Tool settings are used to customize the application for a better user experience.");

        toolbox.getUIRegistry().getOptionsRegistry().addOptionsRegistryListener(this);
    }

    @Override
    public void optionsProviderAdded(OptionsProvider provider)
    {
        refreshTopicTree();
    }

    @Override
    public void optionsProviderChanged(OptionsProvider provider)
    {
        refreshTopicTree();
    }

    @Override
    public void optionsProviderRemoved(OptionsProvider provider)
    {
        refreshTopicTree();
    }

    @Override
    public void showProvider(final OptionsProvider provider)
    {
        if (provider != null)
        {
            EventQueueUtilities.runOnEDT(new Runnable()
            {
                @Override
                public void run()
                {
                    getHUDFrame().setVisible(true);
                    myOptionsFrame.showProvider(provider);
                }
            });
        }
    }

    @Override
    protected OptionsFrame createInternalFrame(Toolbox toolbox)
    {
        return getOptionsFrame(toolbox);
    }

    /**
     * Refresh topic tree.
     */
    private void refreshTopicTree()
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                if (myOptionsFrame != null)
                {
                    myOptionsFrame.refreshTopicTree();
                }
            }
        });
    }

    /**
     * Creates the toolbar activation button.
     *
     * @return the button
     */
    private JButton createToolbarActivationButton()
    {
        IconButton button = new IconButton("Settings");
        IconUtil.setIcons(button, IconType.COGS);
        button.setToolTipText("Settings");
        button.addActionListener(evt -> getHUDFrame().setVisible(true));
        return button;
    }

    /**
     * Gets the options frame.
     *
     * @param toolbox the toolbox
     * @return the options frame
     */
    private OptionsFrame getOptionsFrame(Toolbox toolbox)
    {
        if (myOptionsFrame == null)
        {
            myOptionsFrame = new OptionsFrame(toolbox);
            myOptionsFrame.setVisible(false);
            myOptionsFrame.setLocation(200, 100);
        }
        return myOptionsFrame;
    }
}

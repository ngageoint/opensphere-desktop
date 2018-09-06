package io.opensphere.controlpanels.animation;

import javax.swing.Icon;
import javax.swing.JMenu;

import io.opensphere.controlpanels.animation.controller.AnimationController;
import io.opensphere.controlpanels.animation.state.AnimationStateController;
import io.opensphere.controlpanels.animation.state.LiveStateController;
import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.util.ThreadConfined;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * The plugin that provides time controls.
 */
public class TimelinePlugin extends PluginAdapter
{
    /** The animation controller. */
    @ThreadConfined("EDT")
    private AnimationController myController;

    /** The state controller. */
    private AnimationStateController myStateController;

    /**
     * Saves/activates the state of the live mode.
     */
    private LiveStateController myLiveStateController;

    @Override
    public void initialize(PluginLoaderData plugindata, final Toolbox toolbox)
    {
        myController = new AnimationController(toolbox);

        myStateController = new AnimationStateController(myController);
        myLiveStateController = new LiveStateController(myController);
        toolbox.getModuleStateManager().registerModuleStateController("Animation", myStateController);
        toolbox.getModuleStateManager().registerModuleStateController("Time", myLiveStateController);
        addLegendIcons(toolbox);

        EventQueueUtilities.runOnEDTAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                myController.open();

                JMenu animationTesting = TimelineDebugUtil.createTestMenuItems(toolbox);
                toolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.DEBUG_MENU)
                        .add(animationTesting);
            }
        });
    }

    @Override
    public void close()
    {
        EventQueueUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                myStateController = null;
                if (myController != null)
                {
                    myController.close();
                }
            }
        });
    }

    /**
     * Adds icons to the icon legend.
     *
     * @param toolbox the toolbox
     */
    private void addLegendIcons(Toolbox toolbox)
    {
        Icon timeIcon = IconUtil.getNormalIcon(IconType.CLOCK);
        toolbox.getUIRegistry().getIconLegendRegistry().addIconToLegend(timeIcon, "Timeline",
                "Opens the timeline, where data in temporal layers can be viewed in an animation. "
                    + "Additionally, in the active layers window, this button toggles the layer being included in the timeline.");
    }
}

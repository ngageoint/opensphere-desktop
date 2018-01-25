package io.opensphere.feedback;

import java.awt.Insets;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.control.ui.ToolbarManager.ToolbarLocation;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.SplitButton;

/**
 * The Class FeedbackPlugin. This plugin provides a mechanism for users to
 * submit feedback via a web page.
 */
public class FeedbackPlugin extends PluginAdapter
{
    /** The Feedback button. */
    private SplitButton myFeedbackButton;

    /** The Feedback manager. */
    private FeedbackManager myFeedbackManager;

    /**
     * Gets the feedback button.
     *
     * @return the feedback button
     */
    public SplitButton getFeedbackButton()
    {
        if (myFeedbackButton == null)
        {
            myFeedbackButton = new SplitButton("Feedback", null, false);
            IconUtil.setIcons(myFeedbackButton, IconType.BUG);
            myFeedbackButton.setToolTipText("Please provide your feedback!");
        }
        return myFeedbackButton;
    }

    /**
     * Gets the feedback manager.
     *
     * @return the feedback manager
     */
    public FeedbackManager getFeedbackManager()
    {
        return myFeedbackManager;
    }

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        super.initialize(plugindata, toolbox);

        // Check for one of the URL's and make sure there is a value before
        // loading the button
        // for this plugin.
        Preferences prefs = toolbox.getPreferencesRegistry().getPreferences(FeedbackManager.class);
        final String provideFeedbackStr = prefs.getString(FeedbackManager.PROVIDE_FEEDBACK, null);
        if (provideFeedbackStr != null)
        {
            myFeedbackManager = new FeedbackManager();
            myFeedbackManager.addMenuItems(toolbox, getFeedbackButton());
            toolbox.getUIRegistry().getToolbarComponentRegistry().registerToolbarComponent(ToolbarLocation.NORTH, "Feedback",
                    getFeedbackButton(), 11000, SeparatorLocation.NONE, new Insets(0, 2, 0, 2));
        }
    }
}

package io.opensphere.controlpanels.animation.view;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;

import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.core.options.impl.AbstractOptionsProvider;
import io.opensphere.core.options.impl.OptionsPanel;
import io.opensphere.core.util.swing.input.ViewPanel;
import io.opensphere.core.util.swing.input.controller.ControllerFactory;

/**
 * Animation options provider.
 */
public class AnimationOptionsProvider extends AbstractOptionsProvider
{
    /** The topic. */
    public static final String TOPIC = "Timeline and Animation";

    /** The animation model. */
    private final AnimationModel myAnimationModel;

    /** The timeline frame. */
    private final AnimationInternalFrame myTimelineFrame;

    /** The panel. */
    private JPanel myPanel;

    /**
     * Constructor.
     *
     * @param animationModel the animation model
     * @param timelineFrame the timeline frame
     */
    public AnimationOptionsProvider(AnimationModel animationModel, AnimationInternalFrame timelineFrame)
    {
        super(TOPIC);
        myAnimationModel = animationModel;
        myTimelineFrame = timelineFrame;
    }

    @Override
    public void applyChanges()
    {
    }

    @Override
    public JPanel getOptionsPanel()
    {
        if (myPanel == null)
        {
            myAnimationModel.getRememberTimes().setNameAndDescription("Remember loop span between sessions?",
                    "<html>Selected means the loop span will be saved between sessions.<br>"
                            + "Not selected means the loop span will start out including the current day.</html>");
            myAnimationModel.getViewPreference().setNameAndDescription("Timeline to show on startup",
                    "Which timeline display to show on startup.");
            myAnimationModel.getLoopSpanLocked().setNameAndDescription("Lock loop span",
                    "Do not allow the loop span to be changed by the active span");
            ToolTipManager.sharedInstance().setDismissDelay(10000);

            JButton resetFrameButton = new JButton("Reset timeline size & location");
            resetFrameButton.addActionListener(e -> myTimelineFrame.resizeAndPositionToDefault());

            ViewPanel panel = new ViewPanel();
            panel.addComponent(ControllerFactory.createComponent(myAnimationModel.getRememberTimes()));
            panel.addLabelComponent(ControllerFactory.createLabelAndComponent(myAnimationModel.getViewPreference()));
            panel.addComponent(ControllerFactory.createComponent(myAnimationModel.getLoopSpanLocked()));
            panel.addComponent(resetFrameButton);
            myPanel = new OptionsPanel(panel);
        }
        return myPanel;
    }

    @Override
    public void restoreDefaults()
    {
    }

    @Override
    public boolean usesApply()
    {
        return false;
    }

    @Override
    public boolean usesRestore()
    {
        return false;
    }
}

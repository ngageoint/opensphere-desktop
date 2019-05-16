package io.opensphere.controlpanels.animation.state;

import java.util.Collection;

import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.controlpanels.animation.controller.AnimationController;
import io.opensphere.controlpanels.animation.model.ViewPreference;
import io.opensphere.core.animation.impl.ExportAnimationState;
import io.opensphere.core.animation.impl.ExportTimeState;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.modulestate.AbstractModuleStateController;
import io.opensphere.core.modulestate.StateUtilities;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * Animation state controller.
 */
public class AnimationStateController extends AbstractModuleStateController
{
    /** The animation controller. */
    private final AnimationController myAnimationController;

    /**
     * Constructor.
     *
     * @param animationController the animation controller
     */
    public AnimationStateController(AnimationController animationController)
    {
        super();
        myAnimationController = animationController;
    }

    @Override
    public boolean canActivateState(Node node)
    {
        return StateXML.getChildStateNode(node, "/:time") != null;
    }

    @Override
    public boolean canActivateState(StateType state)
    {
        return state.getTime() != null && state.getTime().getCurrent() != null;
    }

    @Override
    public void activateState(String id, String description, Collection<? extends String> tags, Node node)
    {
        ExportTimeState timeState = StateXML.getStateBean(node, "/:time", ExportTimeState.class);
        ExportAnimationState animationState = StateXML.getStateBean(node, "/:time/:animation", ExportAnimationState.class);
        if (timeState != null && animationState != null)
        {
            showTimeline(animationState.getLoopInterval(), timeState.getCurrent());
        }
    }

    @Override
    public void activateState(String id, String description, Collection<? extends String> tags, StateType state)
    {
        if (canActivateState(state))
        {
            TimeSpan loopSpan = StateUtilities.getLoopSpan(state.getTime());
            TimeSpan activeSpan = StateUtilities.parseSpan(state.getTime().getCurrent());
            showTimeline(loopSpan, activeSpan);
        }
    }

    @Override
    public void deactivateState(String id, Node node)
    {
    }

    @Override
    public void deactivateState(String id, StateType state)
    {
    }

    @Override
    public boolean isSaveStateByDefault()
    {
        return false;
    }

    @Override
    public boolean canSaveState()
    {
        return false;
    }

    @Override
    public void saveState(Node node)
    {
    }

    @Override
    public void saveState(StateType state)
    {
    }

    /**
     * Shows the timeline if necessary.
     *
     * @param loopSpan The loop span
     * @param activeSpan The active span
     */
    private void showTimeline(TimeSpan loopSpan, TimeSpan activeSpan)
    {
        if (loopSpan.getDuration().isGreaterThan(activeSpan.getDuration()))
        {
            EventQueueUtilities.invokeLater(() ->
            {
                myAnimationController.showView(ViewPreference.TIMELINE);
                myAnimationController.fitUIToLoopSpan();
            });
        }
    }
}

package io.opensphere.controlpanels.animation.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.controlpanels.timeline.TimeWindowLayer;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.context.TimespanContextKey;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.core.util.swing.SwingUtilities;

/**
 * Layer for held intervals.
 */
class HeldIntervalsLayer extends IntervalsLayer
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The animation model. */
    private final AnimationModel myAnimationModel;

    /** The parent component. */
    private final Component myParent;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param animationModel the animation model
     * @param parent the parent
     */
    public HeldIntervalsLayer(Toolbox toolbox, AnimationModel animationModel, Component parent)
    {
        super(animationModel, animationModel.getHeldIntervals(), "hold", "held",
                new GenericFontIcon(AwesomeIconSolid.HAND_ROCK, Color.WHITE), new Color(0, 175, 255),
                ColorUtilities.opacitizeColor(AnimationConstants.ACTIVE_HANDLE_COLOR, 64));
        myToolbox = toolbox;
        myAnimationModel = animationModel;
        myParent = parent;
    }

    @Override
    public void getMenuItems(Point p, List<JMenuItem> menuItems)
    {
        if (!myAnimationModel.getPlayState().isPlaying())
        {
            TimeInstant time = getUIModel().xToTime(p.x);
            TimeWindowLayer matchingLayer = getOverlappingLayer(time);
            if (matchingLayer != null)
            {
                final TimeSpan span = matchingLayer.getTimeSpan().get();
                if (myAnimationModel.getHeldIntervalToLayersMap().containsKey(span))
                {
                    menuItems.add(SwingUtilities.newMenuItem("Edit held span", e -> handleHoldLayers(span)));
                }
            }
        }

        super.getMenuItems(p, menuItems);
    }

    @Override
    public List<? extends Component> getMenuItems(String contextId, final TimespanContextKey key)
    {
        List<Component> menuItems = null;
        List<? extends Component> superMenuItems = super.getMenuItems(contextId, key);
        if (superMenuItems != null)
        {
            menuItems = New.list(2);
            menuItems.addAll(superMenuItems);
            menuItems.add(SwingUtilities.newMenuItem("Hold span for layers...",
                    new GenericFontIcon(AwesomeIconSolid.HAND_ROCK, Color.WHITE),
                    e -> handleHoldLayers(snapSpan(key.getTimeSpan()))));
        }
        return menuItems;
    }

    @Override
    protected void handleRemoveAction(TimeSpan span)
    {
        myAnimationModel.getHeldIntervalToLayersMap().remove(span);
        super.handleRemoveAction(span);
    }

    /**
     * Handles a hold layer action.
     *
     * @param span the time span
     */
    private void handleHoldLayers(TimeSpan span)
    {
        Collection<String> currentHeldLayers = myAnimationModel.getHeldIntervalToLayersMap().get(span);
        PickLayerDialog dialog = new PickLayerDialog(myToolbox, myParent, currentHeldLayers);
        dialog.buildAndShow();
        if (dialog.getSelection() == JOptionPane.OK_OPTION)
        {
            myAnimationModel.getHeldIntervalToLayersMap().put(span, dialog.getSelectedGroupIDs());
            int index = myAnimationModel.getHeldIntervals().indexOf(span);
            if (index == -1)
            {
                myAnimationModel.getHeldIntervals().add(span);
            }
            else
            {
                // Trigger an update to persist the layer map to the preferences
                myAnimationModel.getHeldIntervals().set(index, span);
            }
        }
    }
}

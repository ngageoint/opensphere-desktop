package io.opensphere.controlpanels.animation.view;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;

import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.controlpanels.timeline.AbstractTimeSpanLayer;
import io.opensphere.core.animation.AnimationState.Direction;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.ObservableValue;

/**
 * Layer that draws the fade span.
 */
class FadeLayer extends AbstractTimeSpanLayer
{
    /** The animation model. */
    private final transient AnimationModel myAnimationModel;

    /** The minimum fade color. */
    private final Color myFadeMinColor;

    /** The background color of the time window layer. */
    private final Color myTimeWindowBGColor;

    /**
     * Constructor.
     *
     * @param timeSpan The time span.
     * @param animationModel The animation model.
     * @param timeWindowBGColor The background color of the time window layer.
     */
    public FadeLayer(ObservableValue<TimeSpan> timeSpan, AnimationModel animationModel, Color timeWindowBGColor)
    {
        super(timeSpan);
        myAnimationModel = animationModel;
        myTimeWindowBGColor = timeWindowBGColor;
        myFadeMinColor = ColorUtilities.opacitizeColor(myTimeWindowBGColor, 0);
    }

    @Override
    public void paint(Graphics2D g2d)
    {
        super.paint(g2d);
        if (myAnimationModel.getFade().get().intValue() > 0)
        {
            Duration activeDuration = getTimeSpan().get().getDuration();
            if (activeDuration.signum() > 0)
            {
                Duration fadeOut = activeDuration.multiply((double)myAnimationModel.getFade().get().intValue() / 100);

                boolean isForward = getDirection() == Direction.FORWARD;
                TimeSpan fadeSpan = isForward ? TimeSpan.get(fadeOut, getTimeSpan().get().getStart())
                        : TimeSpan.get(getTimeSpan().get().getEnd(), fadeOut);
                if (fadeSpan.overlaps(getUIModel().getUISpan().get()))
                {
                    int startX = getUIModel().timeToX(fadeSpan.getStart());
                    int endX = getUIModel().timeToX(fadeSpan.getEnd());
                    int y = getUIModel().getTimelinePanelBounds().y;
                    Color c1 = isForward ? myFadeMinColor : myTimeWindowBGColor;
                    Color c2 = isForward ? myTimeWindowBGColor : myFadeMinColor;
                    GradientPaint paint1 = new GradientPaint(startX, y, c1, endX, y, c2);
                    Shape polygon;
                    if (isForward)
                    {
                        polygon = new Polygon(new int[] { startX, endX, endX },
                                new int[] { y + getUIModel().getTimelinePanelBounds().height,
                                    y + getUIModel().getTimelinePanelBounds().height, y },
                                3);
                    }
                    else
                    {
                        polygon = new Polygon(new int[] { startX, startX, endX }, new int[] { y,
                            y + getUIModel().getTimelinePanelBounds().height, y + getUIModel().getTimelinePanelBounds().height },
                                3);
                    }
                    g2d.setPaint(paint1);
                    g2d.fill(polygon);
                }
            }
        }
    }
}

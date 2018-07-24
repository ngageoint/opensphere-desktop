package io.opensphere.overlay.controls;

import java.awt.Color;
import java.util.stream.Collectors;

import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.Renderable;
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.util.collections.New;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;

/**
 * 
 */
public class ButtonContainer2 extends Renderable implements ControlComponent
{
    private static final Color BORDER_COLOR = new Color(0xE7E7E7);

    /** The buttons housed within the button container. */
    private ObservableList<BufferedImageButton> myButtons;

    /**
     * The property in which the state of the vertical orientation field is
     * maintained. Defaults to true.
     */
    private BooleanProperty myVerticalOrientationProperty;

    /**
     * @param parent
     */
    public ButtonContainer2(Component parent, BufferedImageButton... controls)
    {
        super(parent);
        myButtons.addAll(controls);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.overlay.controls.ControlComponent#getWidth()
     */
    @Override
    public int getWidth()
    {
        if (myVerticalOrientationProperty.get())
        {
            return myButtons.stream().map(c -> c.getWidth()).max(Integer::compare).orElse(30);
        }
        return myButtons.stream().map(c -> c.getWidth() + 2).collect(Collectors.summingInt(Integer::intValue));
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.overlay.controls.ControlComponent#getHeight()
     */
    @Override
    public int getHeight()
    {
        if (myVerticalOrientationProperty.get())
        {
            return myButtons.stream().map(c -> c.getHeight() + 2).collect(Collectors.summingInt(Integer::intValue));
        }
        return myButtons.stream().map(c -> c.getHeight()).max(Integer::compare).orElse(30);

    }

    /**
     * Gets the {@link #verticalOrientationProperty} property.
     *
     * @return the {@link #verticalOrientationProperty} property.
     */
    public BooleanProperty verticalOrientationProperty()
    {
        return myVerticalOrientationProperty;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.hud.framework.Renderable#init()
     */
    @Override
    public void init()
    {
        PolylineGeometry.Builder<Position> borderBuilder = new PolylineGeometry.Builder<>();
        PolylineRenderProperties borderProperties = new DefaultPolylineRenderProperties(getBaseZOrder(), true, false);
        borderProperties.setWidth(3.0f);
        borderProperties.setColor(BORDER_COLOR);
        borderBuilder.setLineSmoothing(true);
        borderBuilder.setLineType(LineType.STRAIGHT_LINE);

        ScreenBoundingBox drawBounds = getDrawBounds();

        borderBuilder.setVertices(New.list(drawBounds.getUpperLeft(), drawBounds.getUpperRight(), drawBounds.getLowerRight(),
                drawBounds.getLowerLeft(), drawBounds.getUpperLeft()));

        PolylineGeometry border = new PolylineGeometry(borderBuilder, borderProperties, null);
        getGeometries().add(border);

        for (BufferedImageButton bufferedImageButton : myButtons)
        {
            bufferedImageButton.init();
            getGeometries().addAll(bufferedImageButton.getGeometries());
        }
    }
}

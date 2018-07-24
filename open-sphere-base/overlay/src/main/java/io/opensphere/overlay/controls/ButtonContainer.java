package io.opensphere.overlay.controls;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.hud.border.SimpleLineBorder;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.Window;
import io.opensphere.core.hud.framework.layout.GridLayout;
import io.opensphere.core.hud.framework.layout.GridLayoutConstraints;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.javafx.ConcurrentBooleanProperty;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * 
 */
public class ButtonContainer extends Window<GridLayoutConstraints, GridLayout>
{
    private static final Color BORDER_COLOR = new Color(0xE7E7E7);

    /**
     * The property in which the state of the vertical orientation field is
     * maintained. Defaults to true.
     */
    private BooleanProperty myVerticalOrientationProperty;

    /** The buttons housed within the button container. */
    private ObservableList<ControlComponent> myButtons;

    /**
     * Constructor.
     *
     * @param hudTransformer The transformer.
     * @param size The bounding box we will use for size (but not location on
     *            screen).
     * @param locationHint The predetermined location.
     * @param resize The resize behavior.
     * @param controlCreators the set of functions used to create the control
     *            components to include in the container.
     */
    public ButtonContainer(TransformerHelper hudTransformer, ScreenBoundingBox size, ToolLocation locationHint,
            ResizeOption resize, Function<Component, ControlComponent>... controlCreators)
    {
        super(hudTransformer, size, locationHint, resize, ZOrderRenderProperties.TOP_Z - 30);
        myVerticalOrientationProperty = new ConcurrentBooleanProperty(true);
        myButtons = FXCollections.observableArrayList();
        for (Function<Component, ControlComponent> function : controlCreators)
        {
            myButtons.add(function.apply(this));
        }
    }

    @Override
    public Rectangle getBounds()
    {
        return getFrameLocation().asRectangle();
    }

    @Override
    public void init()
    {
        // set the border
        SimpleLineBorder.Builder borderBuilder = new SimpleLineBorder.Builder();
        borderBuilder.setHeight(3);
        borderBuilder.setWidth(3);
        borderBuilder.setLineWidth(2);
        borderBuilder.setLineColor(BORDER_COLOR);
        setBorder(new SimpleLineBorder(this, borderBuilder));
        initBorder();

        // add the buttons:
        int xPosition = 0;
        int yPosition = 0;

        int buttonSize = 30;

        if (myVerticalOrientationProperty.get())
        {
            int width = 4 + myButtons.stream().map(c -> c.getWidth()).max(Integer::compare).orElse(30);
            int height = 4 + myButtons.stream().map(c -> c.getHeight()).collect(Collectors.summingInt(Integer::intValue));

            setLayout(new GridLayout(width, height, this));
//            setLayout(new GridLayout(buttonSize + 4, buttonSize * myButtons.size() + 4, this));
        }
        else
        {
            int width = 4 + myButtons.stream().map(c -> c.getWidth()).collect(Collectors.summingInt(Integer::intValue));
            int height = 4 + myButtons.stream().map(c -> c.getHeight()).max(Integer::compare).orElse(30);

            setLayout(new GridLayout(width, height, this));
//            setLayout(new GridLayout(buttonSize * myButtons.size() + 4, buttonSize + 4, this));
        }

        for (ControlComponent component : myButtons)
        {
            if (component instanceof BufferedImageButton)
            {
                BufferedImageButton button = (BufferedImageButton)component;
                GridLayoutConstraints constraints = new GridLayoutConstraints(
                        new ScreenBoundingBox(new ScreenPosition(xPosition, yPosition),
                                new ScreenPosition(xPosition + buttonSize, yPosition + buttonSize)));
                add(button, constraints);

                if (myVerticalOrientationProperty.get())
                {
                    yPosition += buttonSize + 2;
                }
                else
                {
                    xPosition += buttonSize + 2;
                }
            }
            else if (component instanceof ControlSpacer)
            {
                if (myVerticalOrientationProperty.get())
                {
                    yPosition += component.getHeight();
                }
                else
                {
                    xPosition += component.getWidth();
                }
            }
        }

        getLayout().complete();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.hud.util.PositionBoundedFrame#repositionForInsets()
     */
    @Override
    public void repositionForInsets()
    {
        // TODO Auto-generated method stub
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
}

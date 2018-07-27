package io.opensphere.overlay.controls;

import java.awt.Rectangle;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.EmptyBorder;
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
 * A container in which control components are rendered in a single row. Layout
 * may be vertical or horizontal, and defaults to vertical.
 */
public class ControlComponentContainer extends Window<GridLayoutConstraints, GridLayout>
{
    /** The default component size (assuming square components), in pixels. */
    public static final int DEFAULT_COMPONENT_SIZE = 22;

    /**
     * The property in which the state of the vertical orientation field is
     * maintained. Defaults to true.
     */
    private final BooleanProperty myVerticalOrientationProperty;

    /** The buttons housed within the button container. */
    private final ObservableList<ControlComponent> myButtons;

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
    public ControlComponentContainer(TransformerHelper hudTransformer, ScreenBoundingBox size, ToolLocation locationHint,
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
        initializeBorder();
        initializeLayout();

        // add the components:
        int xPosition = 0;
        int yPosition = 0;
        for (ControlComponent component : myButtons)
        {
            if (component instanceof BufferedImageButton)
            {
                BufferedImageButton button = (BufferedImageButton)component;
                GridLayoutConstraints constraints = new GridLayoutConstraints(
                        new ScreenBoundingBox(new ScreenPosition(xPosition, yPosition),
                                new ScreenPosition(xPosition + DEFAULT_COMPONENT_SIZE, yPosition + DEFAULT_COMPONENT_SIZE)));
                add(button, constraints);

                if (myVerticalOrientationProperty.get())
                {
                    yPosition += DEFAULT_COMPONENT_SIZE + 2;
                }
                else
                {
                    xPosition += DEFAULT_COMPONENT_SIZE + 2;
                }
            }
            else if (component instanceof ControlSpacer)
            {
                // Control spacers do not actually get added, just used to
                // calculate offsets.
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
     * Configures the border of the container.
     */
    private void initializeBorder()
    {
        setBorder(new EmptyBorder());
        initBorder();
    }

    /**
     * Configures the layout of the container, calculating the total size of the
     * container using the child components.
     */
    private void initializeLayout()
    {
        int width;
        int height;
        if (myVerticalOrientationProperty.get())
        {
            width = 4 + myButtons.stream().map(c -> c.getWidth()).max(Integer::compare).orElse(DEFAULT_COMPONENT_SIZE);
            height = 4 + myButtons.stream().map(c -> c.getHeight()).collect(Collectors.summingInt(Integer::intValue));
        }
        else
        {
            width = 4 + myButtons.stream().map(c -> c.getWidth()).collect(Collectors.summingInt(Integer::intValue));
            height = 4 + myButtons.stream().map(c -> c.getHeight()).max(Integer::compare).orElse(DEFAULT_COMPONENT_SIZE);
        }
        setLayout(new GridLayout(width, height, this));
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.hud.util.PositionBoundedFrame#repositionForInsets()
     */
    @Override
    public void repositionForInsets()
    {
        /* intentionally blank */
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

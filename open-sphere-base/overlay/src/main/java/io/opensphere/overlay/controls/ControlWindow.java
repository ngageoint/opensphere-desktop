package io.opensphere.overlay.controls;

import java.awt.Rectangle;
import java.util.stream.Collectors;

import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.Window;
import io.opensphere.core.hud.framework.layout.GridLayout;
import io.opensphere.core.hud.framework.layout.GridLayoutConstraints;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.javafx.ConcurrentBooleanProperty;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * 
 */
public class ControlWindow extends Window<GridLayoutConstraints, GridLayout>
{
    /**
     * The property in which the state of the vertical orientation field is
     * maintained. Defaults to true.
     */
    private BooleanProperty myVerticalOrientationProperty;

    private ObservableList<ButtonContainer2> myContainers;

    /**
     * @param hudTransformer
     * @param location
     * @param geographicLocation
     * @param zOrder
     */
    public ControlWindow(TransformerHelper hudTransformer, ScreenBoundingBox location, GeographicBoundingBox geographicLocation,
            int zOrder, ButtonContainer2... containers)
    {
        super(hudTransformer, location, geographicLocation, zOrder);
        myVerticalOrientationProperty = new ConcurrentBooleanProperty(true);
        myContainers = FXCollections.observableArrayList();
        myContainers.addAll(containers);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.hud.util.PositionBoundedFrame#getBounds()
     */
    @Override
    public Rectangle getBounds()
    {
        return getFrameLocation().asRectangle();
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
     * {@inheritDoc}
     *
     * @see io.opensphere.core.hud.framework.Component#init()
     */
    @Override
    public void init()
    {
        // don't call super.init(), as that puts a border on there.
        // add the buttons:
        int xPosition = 0;
        int yPosition = 0;

        if (myVerticalOrientationProperty.get())
        {
            int width = 4 + myContainers.stream().map(c -> c.getWidth()).max(Integer::compare).orElse(35);
            int height = myContainers.stream().map(c -> c.getHeight() + 5).collect(Collectors.summingInt(Integer::intValue));

            setLayout(new GridLayout(width, height, this));
        }
        else
        {
            int width = 4 + myContainers.stream().map(c -> c.getWidth() + 5).collect(Collectors.summingInt(Integer::intValue));
            int height = 4 + myContainers.stream().map(c -> c.getHeight()).max(Integer::compare).orElse(30);

            setLayout(new GridLayout(width, height, this));
        }

        for (ButtonContainer2 buttonContainer2 : myContainers)
        {
            GridLayoutConstraints constraints = new GridLayoutConstraints(new ScreenBoundingBox(
                    new ScreenPosition(xPosition, yPosition),
                    new ScreenPosition(xPosition + buttonContainer2.getWidth(), yPosition + buttonContainer2.getHeight())));
            if (myVerticalOrientationProperty.get())
            {
                yPosition += buttonContainer2.getHeight() + 5;
            }
            else
            {
                xPosition += buttonContainer2.getWidth() + 5;
            }

            add(buttonContainer2, constraints);
        }

        getLayout().complete();
    }

}

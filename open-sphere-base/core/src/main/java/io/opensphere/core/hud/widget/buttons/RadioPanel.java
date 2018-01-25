package io.opensphere.core.hud.widget.buttons;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.Panel;
import io.opensphere.core.hud.framework.layout.GridLayout;
import io.opensphere.core.hud.framework.layout.GridLayoutConstraints;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;

/** A helper for arranging radio buttons. */
public class RadioPanel extends Panel<GridLayoutConstraints, GridLayout>
{
    /** The default horizontal alignment for buttons. */
    private final float myButtonHAlign;

    /** Selectable Items. */
    private final List<RadioButton> myButtons = new ArrayList<>();

    /** The default vertical alignment for buttons. */
    private final float myButtonVAlign;

    /** Arrangement for the available selections. */
    private final Orientation myOrientation;

    /**
     * Construct a RadioPanel.
     *
     * @param parent parent component.
     * @param orient arrangement.
     */
    public RadioPanel(Component parent, Orientation orient)
    {
        super(parent);
        myOrientation = orient;
        myButtonHAlign = 0f;
        myButtonVAlign = .5f;
    }

    /**
     * Construct a RadioPanel.
     *
     * @param parent parent component.
     * @param orient arrangement.
     * @param hAlign The default horizontal alignment for buttons.
     * @param vAlign The default vertical alignment for buttons.
     */
    public RadioPanel(Component parent, Orientation orient, float hAlign, float vAlign)
    {
        super(parent);
        myOrientation = orient;
        myButtonHAlign = hAlign;
        myButtonVAlign = vAlign;
    }

    /**
     * Add a button.
     *
     * @param button button to add.
     */
    public void addButton(RadioButton button)
    {
        myButtons.add(button);
        button.setParent(this);
        button.setVerticalAlignment(myButtonVAlign);
        button.setHorizontalAlignment(myButtonHAlign);
    }

    /**
     * Add a button at the given location.
     *
     * @param button button to add.
     * @param index location at which to add the button.
     */
    public void addButtonAt(RadioButton button, int index)
    {
        myButtons.add(index, button);
        button.setParent(this);
    }

    @Override
    public void init()
    {
        initBorder();
        if (myOrientation == Orientation.HORIZONTAL)
        {
            setLayout(new GridLayout(myButtons.size(), 1, this));
        }
        else
        {
            setLayout(new GridLayout(1, myButtons.size(), this));
        }

        int index = 0;
        for (RadioButton entry : myButtons)
        {
            ScreenPosition location = null;
            if (myOrientation == Orientation.HORIZONTAL)
            {
                location = new ScreenPosition(index, 0);
            }
            else
            {
                location = new ScreenPosition(0, index);
            }

            GridLayoutConstraints constraint = new GridLayoutConstraints(new ScreenBoundingBox(location, location));
            add(entry, constraint);

            ++index;
        }

        getLayout().complete();
    }

    /**
     * Remove a button.
     *
     * @param button button to remove.
     */
    public void removeButton(RadioButton button)
    {
        myButtons.remove(button);
    }

    /**
     * Remove a button.
     *
     * @param index position at which the entry resides.
     */
    public void removeButtonAt(int index)
    {
        myButtons.remove(index);
    }

    /** Arrangement for the available selections. */
    public enum Orientation
    {
        /** Horizontal orientation. */
        HORIZONTAL,

        /** Vertical orientation. */
        VERTICAL;
    }
}

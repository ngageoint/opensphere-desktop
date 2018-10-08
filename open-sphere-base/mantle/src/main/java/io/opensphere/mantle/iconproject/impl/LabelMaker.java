package io.opensphere.mantle.iconproject.impl;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/** Creates a custom label. */
public class LabelMaker extends Text
{
    /**
     * Creates a custom label with white text.
     *
     * @param theText the Text to edit.
     */
    public LabelMaker(String theText)
    {
        setText(theText);
        setFill(Color.WHITE);
    }
}

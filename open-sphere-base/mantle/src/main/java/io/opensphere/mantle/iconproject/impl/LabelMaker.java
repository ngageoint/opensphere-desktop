package io.opensphere.mantle.iconproject.impl;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/** Creates a custom label. */
public class LabelMaker extends Text
{
    /**
     * Creates a custom label with white text.
     *
     * @param text the Text to edit.
     */
    public LabelMaker(String text)
    {
        setText(text);
        setFill(Color.WHITE);
    }
}

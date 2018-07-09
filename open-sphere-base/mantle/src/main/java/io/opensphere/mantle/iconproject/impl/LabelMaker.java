package io.opensphere.mantle.iconproject.impl;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class LabelMaker extends Text
{

    public LabelMaker(String theText)
    {
        setText(theText);
        setFill(Color.WHITE);
    }

}
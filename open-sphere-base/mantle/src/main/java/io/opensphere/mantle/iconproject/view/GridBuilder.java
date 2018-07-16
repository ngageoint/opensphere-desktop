package io.opensphere.mantle.iconproject.view;

import io.opensphere.mantle.icon.IconRecord;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

/** Crates the Icon Display Grid */
public class GridBuilder extends AnchorPane
{
    /** the width used for icon buttons. */
    private int myTileWidth;

    /** the record of icons to be used */
    private IconRecord myIconRecord;

    public Button GridButtonBuilder()
    {
        Button generic = new Button();
        generic.setMaxSize(myTileWidth, myTileWidth);
        generic.setGraphic(null);
        // TODO
        // Set an actual image to the icon.
        return generic;
    }

}

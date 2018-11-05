package io.opensphere.mantle.icons.view;

import javafx.geometry.Orientation;
import javafx.scene.layout.FlowPane;

/**
 *
 */
public class SelectIconEditor extends FlowPane
{
    /** The unique identifier used for serialization operations. */
    private static final long serialVersionUID = 5761553428708639250L;

    public SelectIconEditor()
    {
        SearchPanel searchPanel = new SearchPanel();
        IconDetailPanel detailPanel = new IconDetailPanel();

        setOrientation(Orientation.HORIZONTAL);
        getChildren().addAll(searchPanel, detailPanel);
    }

}

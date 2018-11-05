package io.opensphere.mantle.icons.view;

import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.fx.FxIcons;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 *
 */
public class SearchPanel extends BorderPane
{
    private final TextField mySearchField;

    private final MenuButton myAddButton;

    private final TabPane mySetTabPane;

    private final Slider mySizeSlider;

    /**
     *
     */
    public SearchPanel()
    {
        mySearchField = new TextField();
        myAddButton = new MenuButton("Add Icon", FxIcons.createIconLabel(AwesomeIconSolid.PLUS, Color.GREEN, 16));
        myAddButton.getItems().add(new MenuItem("Add Icon"));
        myAddButton.getItems().add(new MenuItem("Import Icon Set"));

        mySetTabPane = new TabPane();
        mySizeSlider = new Slider(1, 100, 25);

        HBox topBox = new HBox(mySearchField, myAddButton);
        setTop(topBox);
        setCenter(mySetTabPane);
        setBottom(mySizeSlider);

    }
}

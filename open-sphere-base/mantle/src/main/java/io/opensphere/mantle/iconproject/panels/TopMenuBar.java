package io.opensphere.mantle.iconproject.panels;

import org.controlsfx.control.textfield.CustomTextField;

import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.fx.FxIcons;
import io.opensphere.mantle.iconproject.model.PanelModel;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/** An HBox containing display size controls, view style, and filter options. */
public class TopMenuBar extends HBox
{
    /** The bar to enter text to filter icon results. */
    private final CustomTextField mySearchField;

    /** The menu button used to add icons. */
    private final MenuButton myAddIconsButton;

    /**
     * Creates the top menu bar of the icon manager UI.
     *
     * @param panelModel the current UI model.
     */
    public TopMenuBar(PanelModel panelModel)
    {
        mySearchField = new CustomTextField();

        Label searchIcon = FxIcons.createClearIcon(AwesomeIconSolid.SEARCH, Color.LIGHTGREY, 16);
        searchIcon.setPadding(new Insets(0, 5, 0, 0));
        mySearchField.setRight(searchIcon);

        HBox.setHgrow(mySearchField, Priority.ALWAYS);

        mySearchField.textProperty().bindBidirectional(panelModel.searchTextProperty());

        Label addIcon = FxIcons.createClearIcon(AwesomeIconSolid.PLUS, Color.LIME, 16);
        myAddIconsButton = new MenuButton("Add Icons", addIcon);
        myAddIconsButton.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        myAddIconsButton.getItems()
                .add(new MenuItem("Add single icon", FxIcons.createClearIcon(AwesomeIconSolid.PLUS_CIRCLE, Color.LIGHTGREY, 12)));
        myAddIconsButton.getItems().add(
                new MenuItem("Add local icon set", FxIcons.createClearIcon(AwesomeIconSolid.PLUS_CIRCLE, Color.LIGHTGREY, 12)));
        myAddIconsButton.getItems().add(
                new MenuItem("Add remote icon set", FxIcons.createClearIcon(AwesomeIconSolid.PLUS_CIRCLE, Color.LIGHTGREY, 12)));

        getChildren().addAll(mySearchField, myAddIconsButton);

        setAlignment(javafx.geometry.Pos.TOP_CENTER);
    }
}

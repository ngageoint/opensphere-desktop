package io.opensphere.core.util.fx;

import java.util.List;

import org.controlsfx.control.SegmentedButton;

import io.opensphere.core.util.AwesomeIconSolid;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

/**
 *
 */
public class OSTabBar extends AnchorPane
{
    private final ObservableList<OSTab> myTabs;

    /**
     *
     */
    public OSTabBar(ObservableList<OSTab> tabs)
    {
        myTabs = tabs;

        myTabs.addListener((Change<? extends OSTab> c) ->
        {
            while (c.next())
            {
                List<? extends OSTab> addedSubList = c.getAddedSubList();

                c.getRemoved();
            }
        });

        ObservableList<ToggleButton> buttons = FXCollections.observableArrayList();
        for (OSTab osTab : myTabs)
        {
            buttons.add(createTabButton(osTab));
        }

        ToggleButton addSetButton = new ToggleButton();
        addSetButton.setGraphic(FxIcons.createClearIcon(AwesomeIconSolid.PLUS_CIRCLE, Color.LIME, 12));
        addSetButton.setStyle(
                "-fx-border-width: 0; -fx-background-radius: 0; -fx-background-color: transparent; -fx-content-display: graphic-only;");
        addSetButton.setOnMouseEntered(e -> addSetButton.setStyle(
                "-fx-border-width: 0; -fx-background-radius: 0; -fx-background-color: transparent; -fx-effect: dropshadow(three-pass-box, lime, 15,.25, 0, 0); -fx-content-display: graphic-only;"));
        addSetButton.setOnMouseExited(e -> addSetButton.setStyle(
                "-fx-border-width: 0; -fx-background-radius: 0; -fx-background-color: transparent; -fx-content-display: graphic-only;"));
        addSetButton.setTooltip(new Tooltip("Add a new icon set."));

        addSetButton.setOnAction(e ->
        {
            OSTab osTab = new OSTab("<unnamed>");
            myTabs.add(osTab);
            OSTabButton newTab = createTabButton(osTab);
            buttons.add(buttons.size() - 1, newTab);
            newTab.editText();
        });

        buttons.add(addSetButton);

        SegmentedButton buttonBar = new SegmentedButton(buttons);
        buttonBar.getStylesheets().add(SegmentedButton.STYLE_CLASS_DARK);

        AnchorPane.setLeftAnchor(buttonBar, 0.0);
        AnchorPane.setRightAnchor(buttonBar, 0.0);
        AnchorPane.setTopAnchor(buttonBar, 0.0);
        AnchorPane.setBottomAnchor(buttonBar, 0.0);

        getChildren().add(buttonBar);
    }

    /**
     * @param osTab
     * @return
     */
    private OSTabButton createTabButton(OSTab osTab)
    {
        return new OSTabButton(osTab.textProperty());
    }
}

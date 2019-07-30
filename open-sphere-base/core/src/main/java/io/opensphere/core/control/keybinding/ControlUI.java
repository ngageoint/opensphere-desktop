package io.opensphere.core.control.keybinding;

import io.opensphere.core.util.AwesomeIconSolid;
import javafx.geometry.Insets;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

/**
 * Creates a table/legend of the current Opensphere shortcut keys which pertain
 * to General and Map controls.
 */
public class ControlUI extends GridPane
{
    /**
     * The directory of the "plus" icon.
     */
    private static final String myPlusDir = "images/keys/plus.png";

    /**
     * The directory of the "mouse left button clicked" icon.
     */
    private static final String mouseLeft = "images/keys/MouseLeft.png";

    /**
     * The directory of the "mouse middle button clicked" icon.
     */
    private static final String mouseMiddle = "images/keys/MouseMiddle.png";

    /**
     * The directory of the "mouse right button clicked" icon.
     */
    private static final String mouseRight = "images/keys/MouseRight.png";

    /**
     * The directory of the "four way arrow All" icon.
     */
    private static final String arrowAll = "images/keys/arrows.png";

    /**
     * The directory of the "mouse right button clicked" icon.
     */
    private static final String shiftKey = "Shift";

    /**
     * The directory of the "mouse right button clicked" icon.
     */
    private static final String ctrlKey = "Ctrl";

    /**
     * Creates the main pannel for the "Globe Controls" tab. Use of column and
     * row constraints to ensure proper parent fitting.
     * 
     * @param width the horizontal size desired.
     * @param height the vertical size desired.
     */
    public ControlUI(int width, int height)
    {
        // setMinSize(width, height);
        setStyle("-fx-background-color : derive(-fx-base, 18%)");
        setHgap(10);
        setVgap(10);
        setPadding(new Insets(5));

        GridPane gen = createGeneral();
        GridPane generalMap = createGeneralMap();
        GridPane mapZoom = createMapZoom();
        GridPane mapTools = createMapTools();

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        getColumnConstraints().addAll(col1, col2);

        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(50);
        RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(46);
        RowConstraints row3 = new RowConstraints();
        row3.setPercentHeight(4);
        getRowConstraints().addAll(row1, row2, row3);

        add(gen, 0, 0);
        add(generalMap, 1, 0);
        add(mapZoom, 0, 1);
        add(mapTools, 1, 1);
    }

    /**
     * Creates a sub-window containing Map Tool Controls.
     * 
     * @return the sub-window contained in a GridPane.
     */
    private GridPane createMapTools()
    {
        ControlTab controlTab = new ControlTab();
        GridPane mainBox = controlTab.createBlankPane(" Map Tools");
        GridPane dispArea = controlTab.getDisplayArea();

        VBox l1 = controlTab.createLeftLabel("Draw Geometry");
        HBox r1 = controlTab.createCustomText(ctrlKey);
        r1.getChildren().addAll(new ImageView(myPlusDir), controlTab.customImageView(mouseLeft));

        VBox l2 = controlTab.createLeftLabel("Toggle Arc Length");
        HBox r2 = controlTab.createCustomText("M");

        VBox l3 = controlTab.createLeftLabel("Live Track Mode");
        HBox r3 = controlTab.createCustomText("Ctrl [hold]");

        VBox l4 = controlTab.createLeftLabel("Bullseye Vector");
        HBox r4 = controlTab.createCustomText(",");

        VBox l5 = controlTab.createLeftLabel("Copy Coordinates");
        HBox r5 = controlTab.createCustomText("C");

        VBox l6 = controlTab.createLeftLabel("Display All Coordinates");
        HBox r6 = controlTab.createCustomText(".");

        dispArea.add(l1, 0, 1);
        dispArea.add(r1, 2, 1);
        dispArea.add(l2, 0, 2);
        dispArea.add(r2, 2, 2);
        dispArea.add(l3, 0, 3);
        dispArea.add(r3, 2, 3);
        dispArea.add(l4, 0, 4);
        dispArea.add(r4, 2, 4);
        dispArea.add(l5, 0, 5);
        dispArea.add(r5, 2, 5);
        dispArea.add(l6, 0, 6);
        dispArea.add(r6, 2, 6);
        return mainBox;
    }

    /**
     * Creates a sub-window containing the General Controls.
     * 
     * @return the sub-window contained in a GridPane.
     */
    private GridPane createGeneral()
    {
        ControlTab controlTab = new ControlTab();
        GridPane mainBox = controlTab.createBlankPane(" General Controls");
        GridPane dispArea = controlTab.getDisplayArea();

        VBox l1 = controlTab.createLeftLabel("Save State");
        HBox r1 = controlTab.createCustomText(ctrlKey);
        r1.getChildren().addAll(new ImageView(myPlusDir), controlTab.createCustomText("S"));

        VBox l2 = controlTab.createLeftLabel("Undo");
        HBox r2 = controlTab.createCustomText("Ctrl");
        r2.getChildren().addAll(new ImageView(myPlusDir), controlTab.createCustomText("Z"));

        VBox l3 = controlTab.createLeftLabel("Redo");
        HBox r3 = controlTab.createCustomText(ctrlKey);
        r3.getChildren().addAll(new ImageView(myPlusDir), controlTab.createCustomText("Y"));

        VBox l4 = controlTab.createLeftLabel("Collect Garbage ");
        HBox r4 = controlTab.createCustomText("G");

        VBox l5 = controlTab.createLeftLabel("Cancel Query");
        HBox r5 = controlTab.createCustomText("Esc");

        dispArea.add(l1, 0, 1);
        dispArea.add(r1, 2, 1);
        dispArea.add(l2, 0, 2);
        dispArea.add(r2, 2, 2);
        dispArea.add(l3, 0, 3);
        dispArea.add(r3, 2, 3);
        dispArea.add(l4, 0, 4);
        dispArea.add(r4, 2, 4);
        dispArea.add(l5, 0, 5);
        dispArea.add(r5, 2, 5);
        return mainBox;
    }

    /**
     * Creates a sub-window containing Map Zoom Controls.
     * 
     * @return the sub-window contained in a GridPane.
     */
    private GridPane createMapZoom()
    {
        ControlTab controlTab = new ControlTab();
        GridPane mainBox = controlTab.createBlankPane(" Zoom Controls");
        GridPane dispArea = controlTab.getDisplayArea();

        VBox l1 = controlTab.createLeftLabel("Zoom In / Out");
        HBox r1 = controlTab.createCustomText("Mouse Wheel");

        VBox l2 = controlTab.createLeftLabel("");
        HBox r2 = controlTab.createCustomText(shiftKey);
        r2.getChildren().addAll(new ImageView(myPlusDir), controlTab.createIconButton(AwesomeIconSolid.ARROW_UP),
                controlTab.createIconButton(AwesomeIconSolid.ARROW_DOWN));

        VBox l3 = controlTab.createLeftLabel("Zoom Way In / Out");
        HBox r3 = controlTab.createCustomText("");
        r3.getChildren().addAll(controlTab.customImageView(mouseMiddle), new ImageView(myPlusDir),
                controlTab.createIconButton(AwesomeIconSolid.ARROWS_ALT_V));

        VBox l4 = controlTab.createLeftLabel("Smooth Zoom In / Out");
        HBox r4 = controlTab.createCustomText("");
        r4.getChildren().addAll(controlTab.customImageView(mouseLeft), new ImageView(myPlusDir),
                controlTab.customImageView(mouseRight), new ImageView(myPlusDir),
                controlTab.createIconButton(AwesomeIconSolid.ARROWS_ALT_V));

        VBox l5 = controlTab.createLeftLabel("Draw Zoom Box");
        HBox r5 = controlTab.createCustomText(ctrlKey);
        r5.getChildren().addAll(new ImageView(myPlusDir), controlTab.customImageView(mouseLeft));

        dispArea.add(l1, 0, 1);
        dispArea.add(r1, 2, 1);
        dispArea.add(l2, 0, 2);
        dispArea.add(r2, 2, 2);
        dispArea.add(l3, 0, 3);
        dispArea.add(r3, 2, 3);
        dispArea.add(l4, 0, 4);
        dispArea.add(r4, 2, 4);
        dispArea.add(l5, 0, 5);
        dispArea.add(r5, 2, 5);
        return mainBox;
    }

    /**
     * Creates a sub-window containing Map Movement Controls.
     * 
     * @return the sub-window contained in a GridPane.
     */
    private GridPane createGeneralMap()
    {
        ControlTab controlTab = new ControlTab();
        GridPane mainBox = controlTab.createBlankPane(" Map Movement");
        GridPane dispArea = controlTab.getDisplayArea();

        VBox l1 = controlTab.createLeftLabel("Pan View");
        HBox r1 = controlTab.createCustomText("");
        r1.getChildren().addAll(controlTab.customImageView(mouseLeft), new ImageView(myPlusDir),
                controlTab.createStyledButton(arrowAll));

        VBox l2 = controlTab.createLeftLabel("");
        HBox r2 = controlTab.createCustomText("");
        r2.getChildren().addAll(controlTab.createIconButton(AwesomeIconSolid.ARROW_LEFT),
                controlTab.createIconButton(AwesomeIconSolid.ARROW_RIGHT), controlTab.createIconButton(AwesomeIconSolid.ARROW_UP),
                controlTab.createIconButton(AwesomeIconSolid.ARROW_DOWN));

        VBox l3 = controlTab.createLeftLabel("Fine Pan");
        HBox r3 = controlTab.createCustomText("");
        r3.getChildren().addAll(controlTab.createCustomText("Alt"), new ImageView(myPlusDir),
                controlTab.createStyledButton(arrowAll));

        VBox l4 = controlTab.createLeftLabel("Context Menu");
        HBox r4 = controlTab.createCustomText("");
        r4.getChildren().add(controlTab.customImageView(mouseRight));

        VBox l5 = controlTab.createLeftLabel("Reset View");
        HBox r5 = controlTab.createCustomText("R");

        VBox l6 = controlTab.createLeftLabel("Tilt Globe");
        HBox r6 = controlTab.createCustomText("");
        r6.getChildren().addAll(controlTab.customImageView(mouseRight), new ImageView(myPlusDir),
                controlTab.createIconButton(AwesomeIconSolid.ARROWS_ALT_V));

        VBox l7 = controlTab.createLeftLabel("Roll Globe");
        HBox r7 = controlTab.createCustomText(shiftKey);
        r7.getChildren().addAll(new ImageView(myPlusDir), controlTab.createIconButton(AwesomeIconSolid.ARROW_LEFT),
                controlTab.createIconButton(AwesomeIconSolid.ARROW_RIGHT));

        dispArea.add(l1, 0, 1);
        dispArea.add(r1, 2, 1);
        dispArea.add(l2, 0, 2);
        dispArea.add(r2, 2, 2);
        dispArea.add(l3, 0, 3);
        dispArea.add(r3, 2, 3);
        dispArea.add(l4, 0, 4);
        dispArea.add(r4, 2, 4);
        dispArea.add(l5, 0, 5);
        dispArea.add(r5, 2, 5);
        dispArea.add(l6, 0, 6);
        dispArea.add(r6, 2, 6);
        dispArea.add(l7, 0, 7);
        dispArea.add(r7, 2, 7);
        return mainBox;
    }
}

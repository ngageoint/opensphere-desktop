package io.opensphere.core.control.keybinding;

import io.opensphere.core.util.AwesomeIconSolid;
import javafx.geometry.Insets;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
    private String mouseRight = "images/keys/MouseRight.png";

    /**
     * Creates the main pannel for the "Globe Controls" tab.
     * 
     * @param width the horizontal size desired.
     * @param height the vertical size desired.
     */
    public ControlUI(int width, int height)
    {
        setMinSize(width, height);
        setStyle("-fx-background-color : derive(-fx-base, 18%)");
        setHgap(10);
        setVgap(10);
        setPadding(new Insets(5));

        add(createGeneral(), 0, 0);
        add(createGeneralMap(), 1, 0);
        add(createMapZoom(), 0, 1);
        add(createMapTools(), 1, 1);
    }

    /**
     * Creates a sub-window containing Map Tool Controls.
     * 
     * @return the sub-window contained in a GridPane.
     */
    private GridPane createMapTools()
    {
        ControlTab controlT = new ControlTab();
        GridPane mainBox = controlT.createBlankPane(" Map Tools");
        GridPane dispArea = controlT.getDisplayArea();

        VBox l1 = controlT.createLeftLabel("Draw Geometry");
        HBox r1 = controlT.createCustomText("Shift");
        r1.getChildren().addAll(new ImageView(myPlusDir), controlT.customImageView(mouseLeft));

        VBox l2 = controlT.createLeftLabel("Toggle Arc Length");
        HBox r2 = controlT.createCustomText("m");

        VBox l3 = controlT.createLeftLabel("Live Track Mode");
        HBox r3 = controlT.createCustomText("Ctrl [hold]");

        VBox l4 = controlT.createLeftLabel("Bullseye Vector");
        HBox r4 = controlT.createCustomText(",");

        VBox l5 = controlT.createLeftLabel("Copy Coordinates");
        HBox r5 = controlT.createCustomText("C");

        VBox l6 = controlT.createLeftLabel("Display All Coordinates");
        HBox r6 = controlT.createCustomText(".");

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
        ControlTab controlT = new ControlTab();
        GridPane mainBox = controlT.createBlankPane(" General Controls");
        GridPane dispArea = controlT.getDisplayArea();

        VBox l1 = controlT.createLeftLabel("Save State");
        HBox r1 = controlT.createCustomText("Ctrl");
        r1.getChildren().addAll(new ImageView(myPlusDir), controlT.createCustomText("S"));

        VBox l2 = controlT.createLeftLabel("Undo");
        HBox r2 = controlT.createCustomText("Ctrl");
        r2.getChildren().addAll(new ImageView(myPlusDir), controlT.createCustomText("Z"));

        VBox l3 = controlT.createLeftLabel("Redo");
        HBox r3 = controlT.createCustomText("Ctrl");
        r3.getChildren().addAll(new ImageView(myPlusDir), controlT.createCustomText("Y"));

        VBox l4 = controlT.createLeftLabel("Collect Garbage ");
        HBox r4 = controlT.createCustomText("g");

        VBox l5 = controlT.createLeftLabel("Cancel Query");
        HBox r5 = controlT.createCustomText("Esc");

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
        ControlTab controlT = new ControlTab();
        GridPane mainBox = controlT.createBlankPane(" Zoom Controls");
        GridPane dispArea = controlT.getDisplayArea();

        VBox l1 = controlT.createLeftLabel("Zoom In / Out");
        HBox r1 = controlT.createCustomText("Mouse Wheel");

        VBox l2 = controlT.createLeftLabel("");
        HBox r2 = controlT.createCustomText("Shift");
        r2.getChildren().addAll(new ImageView(myPlusDir), controlT.createIconButton(AwesomeIconSolid.ARROW_UP),
                controlT.createIconButton(AwesomeIconSolid.ARROW_DOWN));

        VBox l3 = controlT.createLeftLabel("Zoom Fast");
        HBox r3 = controlT.createCustomText("");
        r3.getChildren().addAll(controlT.customImageView(mouseMiddle), new ImageView(myPlusDir),
                controlT.createIconButton(AwesomeIconSolid.ARROWS_ALT_V));

        VBox l4 = controlT.createLeftLabel("Zoom Way In / Out");
        HBox r4 = controlT.createCustomText("");
        r4.getChildren().addAll(controlT.customImageView(mouseLeft), new ImageView(myPlusDir),
                controlT.customImageView(mouseRight), new ImageView(myPlusDir),
                controlT.createIconButton(AwesomeIconSolid.ARROWS_ALT_V));

        VBox l5 = controlT.createLeftLabel("Draw Zoom Box");
        HBox r5 = controlT.createCustomText("Ctrl");
        r5.getChildren().addAll(new ImageView(myPlusDir), controlT.customImageView(mouseLeft));

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
        ControlTab controlT = new ControlTab();
        GridPane mainBox = controlT.createBlankPane(" Map Movement");
        GridPane dispArea = controlT.getDisplayArea();

        VBox l1 = controlT.createLeftLabel("Pan View");
        HBox r1 = controlT.createCustomText("");
        r1.getChildren().addAll(controlT.customImageView(mouseLeft), new ImageView(myPlusDir),
                controlT.createIconButton(AwesomeIconSolid.ARROWS_ALT));

        VBox l2 = controlT.createLeftLabel("");
        HBox r2 = controlT.createCustomText("");
        r2.getChildren().addAll(controlT.createIconButton(AwesomeIconSolid.ARROW_LEFT),
                controlT.createIconButton(AwesomeIconSolid.ARROW_RIGHT),
                controlT.createIconButton(AwesomeIconSolid.ARROW_UP),
                controlT.createIconButton(AwesomeIconSolid.ARROW_DOWN));

        VBox l3 = controlT.createLeftLabel("Fine Pan");
        HBox r3 = controlT.createCustomText("");
        r3.getChildren().addAll(controlT.createCustomText("Alt"), new ImageView(myPlusDir),
                controlT.createIconButton(AwesomeIconSolid.ARROWS_ALT));

        VBox l4 = controlT.createLeftLabel("Context Menu");
        HBox r4 = controlT.createCustomText("");
        r4.getChildren().add(controlT.customImageView(mouseRight));

        VBox l5 = controlT.createLeftLabel("Reset View");
        HBox r5 = controlT.createCustomText("R");

        VBox l6 = controlT.createLeftLabel("Pitch Camera");
        HBox r6 = controlT.createCustomText("");
        r6.getChildren().addAll(controlT.customImageView(mouseRight), new ImageView(myPlusDir),
                controlT.createIconButton(AwesomeIconSolid.ARROWS_ALT_V));

        VBox l7 = controlT.createLeftLabel("Roll Camera");
        HBox r7 = controlT.createCustomText("Shift");
        r7.getChildren().addAll(new ImageView(myPlusDir), controlT.createIconButton(AwesomeIconSolid.ARROW_LEFT),
                controlT.createIconButton(AwesomeIconSolid.ARROW_RIGHT));

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

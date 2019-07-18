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
    private String myPlusDir = "images/keys/plus.png";

    /**
     * The directory of the "mouse left button clicked" icon.
     */
    private String mouseLeft = "images/keys/MouseLeft.png";

    /**
     * The directory of the "mouse middle button clicked" icon.
     */
    private String mouseMiddle = "images/keys/MouseMiddle.png";

    /**
     * The directory of the "mouse right button clicked" icon.
     */
    private String mouseRight = "images/keys/MouseRight.png";

    /**
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
     * @return mainBox the sub-window contained in a GridPane.
     */
    private GridPane createMapTools()
    {
        ControlTab thecontrols = new ControlTab();
        GridPane mainBox = thecontrols.createBlankPane(" Map Tools");
        GridPane dispArea = thecontrols.getDisplayArea();

        VBox l1 = thecontrols.createLeftLabel("Draw Geometry");
        HBox r1 = thecontrols.createCustomText("Shift");
        r1.getChildren().addAll(new ImageView(myPlusDir), thecontrols.customImageView(mouseLeft));

        VBox l2 = thecontrols.createLeftLabel("Toggle Arc Length");
        HBox r2 = thecontrols.createCustomText("m");

        VBox l3 = thecontrols.createLeftLabel("Live Track Mode");
        HBox r3 = thecontrols.createCustomText("Ctrl [hold]");

        VBox l4 = thecontrols.createLeftLabel("Bullseye Vector");
        HBox r4 = thecontrols.createCustomText(",");

        VBox l5 = thecontrols.createLeftLabel("Copy Coordinates");
        HBox r5 = thecontrols.createCustomText("C");

        VBox l6 = thecontrols.createLeftLabel("Display All Coordinates");
        HBox r6 = thecontrols.createCustomText(".");

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

        dispArea.add(new HBox(), 3, 5);
        dispArea.add(new HBox(), 4, 5);
        return mainBox;
    }

    /**
     * Creates a sub-window containing the General Controls.
     * 
     * @return mainBox the sub-window contained in a GridPane.
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
     * @return mainBox the sub-window contained in a GridPane.
     */
    private GridPane createMapZoom()
    {
        ControlTab thecontrols = new ControlTab();
        GridPane mainBox = thecontrols.createBlankPane(" Zoom Controls");
        GridPane dispArea = thecontrols.getDisplayArea();

        VBox l1 = thecontrols.createLeftLabel("Zoom In / Out");
        HBox r1 = thecontrols.createCustomText("Mouse Wheel");

        VBox l2 = thecontrols.createLeftLabel("");
        HBox r2 = thecontrols.createCustomText("Shift");
        r2.getChildren().addAll(new ImageView(myPlusDir), thecontrols.customIco(AwesomeIconSolid.ARROW_UP),
                thecontrols.customIco(AwesomeIconSolid.ARROW_DOWN));

        VBox l3 = thecontrols.createLeftLabel("Zoom Fast");
        HBox r3 = thecontrols.createCustomText("");
        r3.getChildren().addAll(thecontrols.customImageView(mouseMiddle), new ImageView(myPlusDir),
                thecontrols.customIco(AwesomeIconSolid.ARROWS_ALT_V));

        VBox l4 = thecontrols.createLeftLabel("Zoom Way In / Out");
        HBox r4 = thecontrols.createCustomText("");
        r4.getChildren().addAll(thecontrols.customImageView(mouseLeft), new ImageView(myPlusDir),
                thecontrols.customImageView(mouseRight), new ImageView(myPlusDir),
                thecontrols.customIco(AwesomeIconSolid.ARROWS_ALT_V));

        VBox l5 = thecontrols.createLeftLabel("Draw Zoom Box");
        HBox r5 = thecontrols.createCustomText("Ctrl");
        r5.getChildren().addAll(new ImageView(myPlusDir), thecontrols.customImageView(mouseLeft));

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
     * @return mainBox the sub-window contained in a GridPane.
     */
    private GridPane createGeneralMap()
    {
        ControlTab thecontrols = new ControlTab();
        GridPane mainBox = thecontrols.createBlankPane(" Map Movement");
        GridPane dispArea = thecontrols.getDisplayArea();

        VBox l1 = thecontrols.createLeftLabel("Pan View");
        HBox r1 = thecontrols.createCustomText("");
        r1.getChildren().addAll(thecontrols.customImageView(mouseLeft), new ImageView(myPlusDir),
                thecontrols.customIco(AwesomeIconSolid.ARROWS_ALT));

        VBox l2 = thecontrols.createLeftLabel("");
        HBox r2 = thecontrols.createCustomText("");
        r2.getChildren().addAll(thecontrols.customIco(AwesomeIconSolid.ARROW_LEFT),
                thecontrols.customIco(AwesomeIconSolid.ARROW_RIGHT), thecontrols.customIco(AwesomeIconSolid.ARROW_UP),
                thecontrols.customIco(AwesomeIconSolid.ARROW_DOWN));

        VBox l3 = thecontrols.createLeftLabel("Fine Pan");
        HBox r3 = thecontrols.createCustomText("");
        r3.getChildren().addAll(thecontrols.createCustomText("Alt"), new ImageView(myPlusDir),
                thecontrols.customIco(AwesomeIconSolid.ARROWS_ALT));

        VBox l4 = thecontrols.createLeftLabel("Context Menu");
        HBox r4 = thecontrols.createCustomText("");
        r4.getChildren().add(thecontrols.customImageView(mouseRight));

        VBox l5 = thecontrols.createLeftLabel("Reset View");
        HBox r5 = thecontrols.createCustomText("R");

        VBox l6 = thecontrols.createLeftLabel("Pitch Camera");
        HBox r6 = thecontrols.createCustomText("");
        r6.getChildren().addAll(thecontrols.customImageView(mouseRight), new ImageView(myPlusDir),
                thecontrols.customIco(AwesomeIconSolid.ARROWS_ALT_V));

        VBox l7 = thecontrols.createLeftLabel("Roll Camera");
        HBox r7 = thecontrols.createCustomText("Shift");
        r7.getChildren().addAll(new ImageView(myPlusDir), thecontrols.customIco(AwesomeIconSolid.ARROW_LEFT),
                thecontrols.customIco(AwesomeIconSolid.ARROW_RIGHT));

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

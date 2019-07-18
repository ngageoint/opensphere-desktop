package io.opensphere.core.control.keybinding;

import io.opensphere.core.util.AwesomeIconSolid;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

/**
 * Creates a table/legend of the current Opensphere shortcut keys which pertain
 * to General and Map controls.
 */
public class ControlUI extends FlowPane
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
//        ColumnConstraints column1 = new ColumnConstraints();
//        column1.setPercentWidth(50);
//        getColumnConstraints().add(column1);
//        
//        RowConstraints r1 = new RowConstraints();
//        r1.setPercentHeight(50);
//        getRowConstraints().add(r1);

        setMinSize(width, height);
        setStyle("-fx-background-color : derive(-fx-base, 18%)");
        setHgap(10);
        setVgap(10);
//        getChildren().add(createGeneral(width / 3, height / 3));
//        getChildren().add(createGeneralMap(width / 3, height / 3));
//        getChildren().add(createMapZoom(width / 3, height / 3));
//        getChildren().add(createMapTools(width / 3, height / 3));
        GridPane gen = createGeneral(width / 3, height / 3);
        HBox.setHgrow(gen, Priority.ALWAYS);
        // add(gen, 0, 0);

        GridPane genm = createGeneralMap(width / 3, height / 3);
        HBox.setHgrow(genm, Priority.ALWAYS);
        // add(genm, 1, 0);

        GridPane genmz = createMapZoom(width / 3, height / 3);
        HBox.setHgrow(genmz, Priority.ALWAYS);
        // add(genmz, 0, 1);

        GridPane genmt = createMapTools();
        HBox.setHgrow(genmt, Priority.ALWAYS);
        // add(genmt, 1, 1);
        getChildren().addAll(genm, genmz, gen, genmt);
    }

    /**
     * @param width the desired horizontal size for the sub window.
     * @param height the desired vertical size for the sub window.
     * @return theHbox an HBox containing controls.
     */
    private GridPane createMapTools()
    {
        ControlTab thecontrols = new ControlTab();
        GridPane mainBox = thecontrols.createBlankPane(" Map Tools");
        GridPane dispArea = thecontrols.getDisplayArea();

        VBox l5 = thecontrols.createLeftLabel("Bullseye Vector");
        HBox r5 = thecontrols.createCustomText(",");

        VBox l2 = thecontrols.createLeftLabel("Arc Length");
        HBox r2 = thecontrols.createCustomText("m");

        VBox l3 = thecontrols.createLeftLabel("Live Tracks");
        HBox r3 = thecontrols.createCustomText("Ctrl [hold]");

        VBox l4 = thecontrols.createLeftLabel("Cancel Query");
        HBox r4 = thecontrols.createCustomText("Esc");

        VBox l1 = thecontrols.createLeftLabel("Draw Geometry");
        HBox r1 = thecontrols.createCustomText("Shift");
        r1.getChildren().addAll(new ImageView(myPlusDir), thecontrols.customImageView(mouseLeft), new ImageView(myPlusDir),
                thecontrols.customIco(AwesomeIconSolid.ARROW_UP));

//        VBox l4 = thecontrols.createLeftLabel("Context Menu");
//        HBox r4 = thecontrols.createCustomText("");
//        r4.getChildren().add(thecontrols.customImageView(mouseRight));
//
//        VBox l5 = thecontrols.createLeftLabel("Reset View");
//        HBox r5 = thecontrols.createCustomText("R");

        VBox l6 = thecontrols.createLeftLabel("Copy Coordinates");
        HBox r6 = thecontrols.createCustomText("C");

        VBox l7 = thecontrols.createLeftLabel("Display All Coordinates");
        HBox r7 = thecontrols.createCustomText(".");
        
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
        
        dispArea.add(new HBox(), 3, 5);
        dispArea.add(new HBox(), 4, 5);
        return mainBox;
    }

    /**
     * @param width the desired horizontal size for the sub window.
     * @param height the desired vertical size for the sub window.
     * @return theHbox an HBox containing controls.
     */
    private GridPane createGeneral(int width, int height)
    {
        ControlTab thecontrols = new ControlTab();
        GridPane mainBox = thecontrols.createBlankPane(width, height, " General Controls");
        GridPane dispArea = thecontrols.getDisplayArea();

        VBox l1 = thecontrols.createLeftLabel("Save State");
        HBox r1 = thecontrols.createCustomText("Ctrl");
        r1.getChildren().addAll(new ImageView(myPlusDir), thecontrols.createCustomText("S"));

        VBox l2 = thecontrols.createLeftLabel("Undo");
        HBox r2 = thecontrols.createCustomText("Ctrl");
        r2.getChildren().addAll(new ImageView(myPlusDir), thecontrols.createCustomText("Z"));

        VBox l3 = thecontrols.createLeftLabel("Redo");
        HBox r3 = thecontrols.createCustomText("Ctrl");
        r3.getChildren().addAll(new ImageView(myPlusDir), thecontrols.createCustomText("Y"));

        VBox l4 = thecontrols.createLeftLabel("Collect Garbage ");
        HBox r4 = thecontrols.createCustomText("g");

        dispArea.add(l1, 0, 1);
        dispArea.add(r1, 2, 1);
        dispArea.add(l2, 0, 2);
        dispArea.add(r2, 2, 2);
        dispArea.add(l3, 0, 3);
        dispArea.add(r3, 2, 3);
        dispArea.add(l4, 0, 4);
        dispArea.add(r4, 2, 4);
//        dispArea.add(l5, 0, 5);
//        dispArea.add(r5, 2, 5);
//        dispArea.add(l6, 0, 6);
//        dispArea.add(r6, 2, 6);

        return mainBox;
    }

    private GridPane createMapZoom(int width, int height)
    {
        ControlTab thecontrols = new ControlTab();
        GridPane mainBox = thecontrols.createBlankPane(width, height, " Zoom Controls");
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

//        VBox l5 = thecontrols.createLeftLabel("Pitch Camera");
//        HBox r5 = thecontrols.createCustomText("");
//        r5.getChildren().addAll(thecontrols.customImageView(mouseRight), new ImageView(myPlusDir),
//                thecontrols.customIco(AwesomeIconSolid.ARROWS_ALT_V));

//      VBox l5 = thecontrols.createLeftLabel("Roll Camera");
//      HBox r5 = thecontrols.createCustomText("Shift");
//      r5.getChildren().addAll(new ImageView(myPlusDir), thecontrols.customIco(AwesomeIconSolid.ARROW_UP),
//              thecontrols.customIco(AwesomeIconSolid.ARROW_DOWN));

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
//        dispArea.add(l6, 0, 6);
//        dispArea.add(r6, 2, 6);
//        dispArea.add(l7, 0, 7);
//        dispArea.add(r7, 2, 7);
        return mainBox;
    }

    private GridPane createGeneralMap(int width, int height)
    {
        ControlTab thecontrols = new ControlTab();
        GridPane mainBox = thecontrols.createBlankPane(width, height, " Map Controls");
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

        VBox l6 = thecontrols.createLeftLabel("Copy Coordinates");
        HBox r6 = thecontrols.createCustomText("C");

        VBox l7 = thecontrols.createLeftLabel("Display All Coordinates");
        HBox r7 = thecontrols.createCustomText(".");

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

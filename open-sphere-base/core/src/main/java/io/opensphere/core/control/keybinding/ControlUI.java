package io.opensphere.core.control.keybinding;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * @author crombiek Creates a table/legend of the current Opensphere shortcut
 *         keys which pertain to General and Map controls.
 */
public class ControlUI extends HBox
{

    private String myPlusDir = "images/plus.png";

    /**
     * @param width the horizontal size desired.
     * @param height the vertical size desired.
     */
    public ControlUI(int width, int height)
    {
        setMinSize(width, height);
        setStyle("-fx-background-color : derive(-fx-base, 18%)");
        getChildren().add(createGeneral(width / 3, height / 3));
        getChildren().add(createGeneralMap(width / 3, height / 3));
    }

    /**
     * @param width the desired horizontal size for the sub window.
     * @param height the desired vertical size for the sub window.
     * @return theHbox an HBox containing controls.
     */
    private GridPane createGeneral(int width, int height)
    {
        GridPane theHbox = createBlankPane(width, height);

        VBox mainHeader = createHeader(" General Controls");
        VBox l1 = leftLabel("Save State");
        HBox r1 = customText("Ctrl");
        r1.getChildren().addAll(new ImageView(myPlusDir), customText("S"));

        VBox l2 = leftLabel("Undo");
        HBox r2 = customText("Ctrl");
        r2.getChildren().addAll(new ImageView(myPlusDir), customText("Z"));

        VBox l3 = leftLabel("Redo");
        HBox r3 = customText("Ctrl");
        r3.getChildren().addAll(new ImageView(myPlusDir), customText("Y"));

        theHbox.add(mainHeader, 0, 0, 3, 1);
        theHbox.add(l1, 0, 1);
        theHbox.add(r1, 2, 1);
        theHbox.add(l2, 0, 2);
        theHbox.add(r2, 2, 2);
        theHbox.add(l3, 0, 3);
        theHbox.add(r3, 2, 3);

        return theHbox;
    }

    private GridPane createGeneralMap(int width, int height)
    {
        GridPane theHbox = createBlankPane(width, height);
        VBox mainHeader = createHeader(" General Map Controls");

        VBox l1 = leftLabel("Draw Geometry");
        HBox r1 = customText("Shift");
        r1.getChildren().addAll(new ImageView(myPlusDir),customImageView("images/keys/MouseLeft.png"), new ImageView(myPlusDir),
                new IconDispButton("images/keys/arrow.png", true));
        VBox l2 = leftLabel("Context Menu");
        HBox r2 = new HBox();
        r2.getChildren().add(new ImageView("images/keys/MouseRight.png"));

        VBox l3 = leftLabel("Reset View");
        HBox r3 = customText("V");

        VBox l4 = leftLabel("Reset Coordinates");
        HBox r4 = customText("R");

        theHbox.add(mainHeader, 0, 0, 3, 1);
        theHbox.add(l1, 0, 1);
        theHbox.add(r1, 2, 1);
        theHbox.add(l2, 0, 2);
        theHbox.add(r2, 2, 2);
        theHbox.add(l3, 0, 3);
        theHbox.add(r3, 2, 3);
        theHbox.add(l4, 0, 4);
        theHbox.add(r4, 2, 4);
        return theHbox;
    }

    private GridPane createBlankPane(int width, int height)
    {
        GridPane thePane = new GridPane();
        ColumnConstraints col1Constraints = new ColumnConstraints();
        col1Constraints.setPercentWidth(47);
        ColumnConstraints col2Constraints = new ColumnConstraints();
        col2Constraints.setPercentWidth(6);
        ColumnConstraints col3Constraints = new ColumnConstraints();
        col3Constraints.setPercentWidth(47);
        thePane.getColumnConstraints().addAll(col1Constraints, col2Constraints, col3Constraints);

        thePane.setMinWidth(width);
        thePane.setMaxHeight(height);
        thePane.setStyle(
                "-fx-background-color : #363636;-fx-border-color: #004080;-fx-border-width: 2;-fx-background-radius: 5 5 5 5;"
                        + "    -fx-border-radius: 5 5 5 5;");
        return thePane;
    }

    private VBox createHeader(String theLabel)
    {
        VBox mainHeader = new VBox();
        mainHeader.setStyle("-fx-background-color : #181a1c");
        Label header = new Label(theLabel);
        header.setFont(new Font("Arial Black", 16));
        mainHeader.getChildren().add(header);
        mainHeader.setAlignment(Pos.CENTER_LEFT);
        return mainHeader;
    }

    public HBox customText(String b1)
    {
        HBox comboText = new HBox();
        comboText.setMinHeight(30);
        comboText.setSpacing(8);
        comboText.setAlignment(Pos.CENTER_LEFT);
        comboText.getChildren().add(new IconDispButton(b1, false));
        return comboText;

    }

    private ImageView customImageView(String string)
    {
        ImageView customView = new ImageView(string);
        customView.setFitHeight(30);
        customView.setFitWidth(30);
        customView.setPreserveRatio(true);
        return customView;
    }

    public VBox leftLabel(String label)
    {
        VBox theLabelBox = new VBox();
        Label text2 = new Label(label);
        text2.setFont(new Font("Arial Black", 14));
        theLabelBox.getChildren().add(text2);
        theLabelBox.setAlignment(Pos.CENTER_RIGHT);
        return theLabelBox;
    }

    /**
     * A custom button for putting text or images into buttons.
     */
    public class IconDispButton extends Button
    {
        public IconDispButton(String key, boolean image)
        {
            setDisable(true);
            setStyle("-fx-background-color:#0066cc;-fx-opacity: 1;");
            if (image == false)
            {
                setFont(Font.font("Arial Black", FontWeight.MEDIUM, 12));
                setTextFill(Color.web("white"));
                setText(key);
            }
            else if (image == true)
            {
                setMaxSize(30, 30);
                setGraphic(customImageView(key));
            }
        }
    }
}

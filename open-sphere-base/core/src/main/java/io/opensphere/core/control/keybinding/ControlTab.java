package io.opensphere.core.control.keybinding;

import io.opensphere.core.util.FontIconEnum;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Contains all the methods used for generating the panels and custom objects
 * within the "Key Map" sub tab named "Controls"
 *
 */
public class ControlTab
{

    /**
     * The JavaFX gridpane containing the shortcut key keys and keys in a column
     * arranged fashion. Since this pane is a child of another pane it must be
     * accessed via the get method when adding items to it outside of this
     * class.
     */
    private GridPane myDisplayArea;

    @SuppressWarnings("unused")
    public Button customIco(FontIconEnum text)
    {
        Button theButton = new Button();

        final Label label = new Label(text.getFontCode());
        Color color = Color.WHITE;
        label.setTextFill(color);
        label.setOpacity(75);

        final String fontName = "icons-" + text.getFont().getFontName().replaceAll("\\s", "-");
        label.getStyleClass().addAll(fontName);
        label.setStyle("-fx-font-size: 16 px;");

        theButton.setGraphic(label);
        theButton.setStyle("-fx-background-color:#0066cc;-fx-opacity: 1;");
        theButton.setDisable(true);
        theButton.setMaxSize(22, 22);
        return theButton;
    }

    @SuppressWarnings("unused")
    public GridPane createBlankPane(int width, int height, String header)
    {
        GridPane thePane = new GridPane();
        thePane.setMinWidth(width);
        thePane.setMinHeight(height);
        thePane.setStyle(
                "-fx-background-color : #363636;-fx-border-color: #004080;-fx-border-width: 4;-fx-background-radius: 8 8 8 8;"
                        + "    -fx-border-radius: 8 8 8 8;");

        myDisplayArea = new GridPane();
        ColumnConstraints col1Constraints = new ColumnConstraints();
        col1Constraints.setPercentWidth(47);
        ColumnConstraints col2Constraints = new ColumnConstraints();
        col2Constraints.setPercentWidth(6);
        ColumnConstraints col3Constraints = new ColumnConstraints();
        col3Constraints.setPercentWidth(47);
        myDisplayArea.getColumnConstraints().addAll(col1Constraints, col2Constraints, col3Constraints);
        myDisplayArea.setVgap(5);
        myDisplayArea.setPadding(new Insets(0, 10, 10, 10));
        thePane.add(createHeader(header), 0, 0);
        thePane.add(myDisplayArea, 0, 1);

        return thePane;

    }

    public GridPane createBlankPane(String header)
    {
        GridPane thePane = new GridPane();
        thePane.setStyle(
                "-fx-background-color : #363636;-fx-border-color: #004080;-fx-border-width: 4;-fx-background-radius: 8 8 8 8;"
                        + "    -fx-border-radius: 8 8 8 8;");

        myDisplayArea = new GridPane();
        ColumnConstraints col1Constraints = new ColumnConstraints();
        col1Constraints.setPercentWidth(47);
        ColumnConstraints col2Constraints = new ColumnConstraints();
        col2Constraints.setPercentWidth(6);
        ColumnConstraints col3Constraints = new ColumnConstraints();
        col3Constraints.setPercentWidth(47);
        myDisplayArea.getColumnConstraints().addAll(col1Constraints, col2Constraints, col3Constraints);
        myDisplayArea.setVgap(5);
        myDisplayArea.setPadding(new Insets(0, 10, 10, 10));
        thePane.add(createHeader(header), 0, 0);
        thePane.add(myDisplayArea, 0, 1);

        return thePane;

    }

    public GridPane getDisplayArea()
    {
        return myDisplayArea;
    }

    public void setDisplayArea(GridPane myDisplayArea)
    {
        this.myDisplayArea = myDisplayArea;
    }

    public HBox createHeader(String theLabel)
    {
        HBox mainHeader = new HBox();
        mainHeader.setStyle("-fx-background-color : #181a1c");
        Label header = new Label(theLabel);
        header.setFont(Font.font(16));
      //  header.setStyle("-fx-font-weight: bold;");
        mainHeader.getChildren().add(header);
        mainHeader.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(header, Priority.ALWAYS);
        return mainHeader;
    }

    public HBox createCustomText(String b1)
    {
        HBox comboText = new HBox();
        comboText.setMinHeight(30);
        comboText.setSpacing(8);
        comboText.setAlignment(Pos.CENTER_LEFT);
        if (!b1.equals(""))
        {
            comboText.getChildren().add(new IconDispButton(b1, false));
        }
        return comboText;
    }

    public ImageView customImageView(String string)
    {
        ImageView customView = new ImageView(string);
        customView.setFitHeight(30);
        customView.setFitWidth(30);
        customView.setPreserveRatio(true);
        return customView;
    }

    public VBox createLeftLabel(String label)
    {
        VBox theLabelBox = new VBox();
        Label text2 = new Label(label);
        // text2.setFont(new Font("Arial Black", 14));
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
                // setFont(Font.font("Arial Black", FontWeight.MEDIUM, 12));
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

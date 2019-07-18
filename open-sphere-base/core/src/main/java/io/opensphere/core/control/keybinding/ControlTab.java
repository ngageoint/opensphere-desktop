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

    /**
     * @param text the icon to be set.
     * @return a custom styled Button with various FontIconEnums inside.
     */
    public Button createIconButton(FontIconEnum text)
    {
        Button customIcon = new Button();

        final Label label = new Label(text.getFontCode());
        label.setTextFill(Color.WHITE);
        label.setOpacity(75);

        final String fontName = "icons-" + text.getFont().getFontName().replaceAll("\\s", "-");
        label.getStyleClass().addAll(fontName);
        label.setStyle("-fx-font-size: 16 px;");

        customIcon.setGraphic(label);
        customIcon.setStyle("-fx-background-color:#0066cc;-fx-opacity: 1;");
        customIcon.setDisable(true);
        customIcon.setMaxSize(22, 22);
        return customIcon;
    }

    /**
     * 
     * @return a blank Grid Pane with opensphere styling.
     */
    public GridPane createBlankPane(String header)
    {
        GridPane styledPane = new GridPane();
        styledPane.setStyle(
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
        HBox topheader = createHeader(header);
        GridPane.setHgrow(topheader, Priority.ALWAYS);
        styledPane.add(topheader, 0, 0);
        styledPane.add(myDisplayArea, 0, 1);

        return styledPane;
    }

    /**
     * @return the sub-child of the main styledPane.
     */
    public GridPane getDisplayArea()
    {
        return myDisplayArea;
    }

    /**
     * @param displayArea sets the child of the main styledPane.
     */
    public void setDisplayArea(GridPane displayArea)
    {
        this.myDisplayArea = displayArea;
    }

    /**
     * @param theLabel the text to set.
     * @return HBox containing the sub window title.
     */
    public HBox createHeader(String theLabel)
    {
        HBox mainHeader = new HBox();
        mainHeader.setStyle("-fx-background-color : #181a1c");
        Label header = new Label(theLabel);
        header.setFont(Font.font(16));
        mainHeader.getChildren().add(header);
        mainHeader.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(header, Priority.ALWAYS);
        return mainHeader;
    }

    /**
     * @param buttonText the text to be set.
     * @return styled HBox containing text or a styled button.
     */
    public HBox createCustomText(String buttonText)
    {
        HBox comboText = new HBox();
        comboText.setMinHeight(30);
        comboText.setSpacing(8);
        comboText.setAlignment(Pos.CENTER_LEFT);
        if (!buttonText.equals(""))
        {
            comboText.getChildren().add(new IconDispButton(buttonText));
        }
        return comboText;
    }

    /**
     * @param string the image to load.
     * @return an Image display with a locked size and ratio.
     */
    public ImageView customImageView(String string)
    {
        ImageView customView = new ImageView(string);
        customView.setFitHeight(30);
        customView.setFitWidth(30);
        customView.setPreserveRatio(true);
        return customView;
    }

    /**
     * @param displayText the text to be set.
     * @return a VBox containing the text, right justified.
     */
    public VBox createLeftLabel(String displayText)
    {
        VBox labelBox = new VBox();
        labelBox.getChildren().add(new Label(displayText));
        labelBox.setAlignment(Pos.CENTER_RIGHT);
        return labelBox;
    }

    /**
     * A custom button for putting text or images into buttons.
     */
    private class IconDispButton extends Button
    {
        private IconDispButton(String key)
        {
            setDisable(true);
            setStyle("-fx-background-color:#0066cc;-fx-opacity: 1;");
            setTextFill(Color.web("white"));
            setText(key);
        }
    }
}

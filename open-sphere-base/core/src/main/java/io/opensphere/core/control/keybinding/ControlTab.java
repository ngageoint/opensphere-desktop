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
public class ControlTab {
	/**
	 * The JavaFX gridpane containing the shortcut key keys and keys in a column
	 * arranged fashion. Since this pane is a child of another pane it must be
	 * accessed via the get method when adding items to it outside of this class.
	 */
	private GridPane myDisplayArea;

	/**
	 * creates a custom styled button with special text,styling, and sizing.
	 * 
	 * @param text the icon to be set.
	 * @return a custom styled Button with various FontIconEnums inside.
	 */
	public Button createIconButton(FontIconEnum text) {
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
	 * creates a window to later be populated with the controls menu shortcuts.
	 * 
	 * @return a blank Grid Pane with opensphere styling.
	 */
	public GridPane createBlankPane(String header) {
		GridPane styledPane = new GridPane();
		styledPane.setStyle(
				"-fx-background-color : #363636;-fx-border-color: #004080;-fx-border-width: 4;-fx-background-radius: 8 8 8 8;"
						+ "    -fx-border-radius: 8 8 8 8;");

		myDisplayArea = new GridPane();
		ColumnConstraints col1 = new ColumnConstraints();
		col1.setPercentWidth(47);
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setPercentWidth(6);
		ColumnConstraints col3 = new ColumnConstraints();
		col3.setPercentWidth(47);
		myDisplayArea.getColumnConstraints().addAll(col1, col2, col3);
		myDisplayArea.setVgap(5);
		myDisplayArea.setPadding(new Insets(0, 10, 10, 10));
		HBox topheader = createHeader(header);
		GridPane.setHgrow(topheader, Priority.ALWAYS);
		styledPane.add(topheader, 0, 0);
		styledPane.add(myDisplayArea, 0, 1);

		return styledPane;
	}

	/**
	 * Gets the sub child gridpane.
	 * 
	 * @return the sub-child of the main styledPane.
	 */
	public GridPane getDisplayArea() {
		return myDisplayArea;
	}

	/**
	 * Sets the gridpane property.
	 * 
	 * @param displayArea sets the child of the main styledPane.
	 */
	public void setDisplayArea(GridPane displayArea) {
		this.myDisplayArea = displayArea;
	}

	/**
	 * creates the top bar of each sub menu and sets the text.
	 * 
	 * @param theLabel the text to set.
	 * @return HBox containing the sub window title.
	 */
	public HBox createHeader(String theLabel) {
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
	 * creates custom styled text for the shortcut key menu.
	 * 
	 * @param buttonText the text to be set.
	 * @return styled HBox containing text or a styled button.
	 */
	public HBox createCustomText(String buttonText) {
		HBox comboText = new HBox();
		comboText.setMinHeight(30);
		comboText.setSpacing(8);
		comboText.setAlignment(Pos.CENTER_LEFT);
		if (!buttonText.equals("")) {
			comboText.getChildren().add(new IconDispButton(buttonText));
		}
		return comboText;
	}

	/**
	 * creates a styled java ImageView to match other display sizes.
	 * 
	 * @param string the image to load.
	 * @return an Image display with a locked size and ratio.
	 */
	public ImageView customImageView(String string) {
		ImageView customView = new ImageView(string);
		customView.setFitHeight(30);
		customView.setFitWidth(30);
		customView.setPreserveRatio(true);
		return customView;
	}

	/**
	 * creates a box to bound text for the legend of shortcuts.
	 * 
	 * @param displayText the text to be set.
	 * @return a VBox containing the text, right justified.
	 */
	public VBox createLeftLabel(String displayText) {
		VBox labelBox = new VBox();
		labelBox.getChildren().add(new Label(displayText));
		labelBox.setAlignment(Pos.CENTER_RIGHT);
		return labelBox;
	}

	/**
	 * creates a custom blue styled button with an image inside.
	 * 
	 * @param image the icon to be set.
	 * @return a Button containing the image with fixed size and matching styles.
	 */
	public Button createStyledButton(String image) {
		Button styledButton = new Button();
		styledButton.setStyle("-fx-background-color:#0066cc;-fx-opacity: 1;");
		ImageView customView = new ImageView(image);
		customView.setFitHeight(22);
		customView.setFitWidth(22);
		customView.setPreserveRatio(true);
		styledButton.setAlignment(Pos.CENTER);
		styledButton.setGraphic(customView);
		return styledButton;
	}

	/**
	 * A custom button for putting text or images into buttons.
	 */
	public class IconDispButton extends Button {
		public IconDispButton(String key) {
			setDisable(true);
			setStyle("-fx-background-color:#0066cc;-fx-opacity: 1;");
			setTextFill(Color.web("white"));
			setText(key);
		}
	}
}

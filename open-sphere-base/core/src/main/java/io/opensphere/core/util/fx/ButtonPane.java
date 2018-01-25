package io.opensphere.core.util.fx;

import java.util.Optional;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import io.opensphere.core.util.Colors;
import io.opensphere.core.util.ValidationStatus;

/** Standard pane with button bar at bottom. */
public class ButtonPane extends BorderPane
{
    /** Vertical space. */
    public static final int VERTICAL_SPACE = 8;

    /** Horizontal space. */
    public static final int HORIZONTAL_SPACE = 6;

    /** Outer space. */
    public static final int OUTER_SPACE = VERTICAL_SPACE;

    /** The button bar. */
    private final ObservableButtonBar myButtonBar;

    /** The status label. */
    private final Label myStatusLabel;

    /**
     * Constructor.
     */
    public ButtonPane()
    {
        myButtonBar = new OpenSphereButtonBar();
        myStatusLabel = new Label();
        myStatusLabel.setPadding(new Insets(3));
        myStatusLabel.setVisible(false);

        setPadding(new Insets(OUTER_SPACE));
        setBottom(newHBox(myStatusLabel, FXUtilities.newHSpacer(), myButtonBar));
        setMargin(getBottom(), new Insets(VERTICAL_SPACE, 0, 0, 0));
    }

    /**
     * Adds a listener for button clicks.
     *
     * @param listener the listener
     */
    public void addButtonClickListener(Consumer<ButtonData> listener)
    {
        myButtonBar.addButtonClickListener(listener);
    }

    /**
     * Gets the button bar.
     *
     * @return the button bar
     */
    public ButtonBar getButtonBar()
    {
        return myButtonBar;
    }

    /**
     * Sets the validation status and error message.
     *
     * @param status The validation status.
     * @param message A message detailing why the object is valid or not.
     */
    protected void setValidationStatus(ValidationStatus status, String message)
    {
        Optional<Node> okButton = myButtonBar.getButtons().stream().filter(b -> ButtonBar.getButtonData(b) == ButtonData.OK_DONE)
                .findAny();
        if (okButton.isPresent())
        {
            okButton.get().setDisable(status == ValidationStatus.ERROR);
        }

        Color textColor = Color.WHITE;
        Color bgColor = FXUtilities.fromAwtColor(Colors.LF_SECONDARY3);
        if (status == ValidationStatus.ERROR)
        {
            textColor = Color.RED;
            bgColor = Color.WHITE;
        }
        else if (status == ValidationStatus.WARNING)
        {
            textColor = Color.YELLOW;
        }
        myStatusLabel.setVisible(message != null);
        myStatusLabel.setText(message);
        myStatusLabel.setTextFill(textColor);
        myStatusLabel.setBackground(new Background(new BackgroundFill(bgColor, new CornerRadii(3), new Insets(0))));
    }

    /**
     * Utility method to create a new HBox.
     *
     * @param elements the elements
     * @return the HBox
     */
    public static HBox newHBox(Node... elements)
    {
        HBox box = new HBox(HORIZONTAL_SPACE, elements);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    /**
     * Utility method to create a new VBox.
     *
     * @param elements the elements
     * @return the VBox
     */
    public static VBox newVBox(Node... elements)
    {
        VBox box = new VBox(VERTICAL_SPACE, elements);
        return box;
    }
}

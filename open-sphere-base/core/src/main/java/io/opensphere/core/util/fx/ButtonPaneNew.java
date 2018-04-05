package io.opensphere.core.util.fx;

import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.core.util.ValidatorSupport.ValidationStatusChangeListener;

/** Standard pane with button bar at bottom. */
public class ButtonPaneNew extends BorderPane
{
    /** Vertical space. */
    public static final int VERTICAL_SPACE = 8;

    /** Outer space. */
    public static final int OUTER_SPACE = VERTICAL_SPACE;

    /** The editor. */
    private Editor myEditor;

    /** The button bar. */
    private ObservableButtonBar myButtonBar;

    /** The status label. */
    private Label myStatusLabel;

    /** A simpler alternative to using an Editor. */
    private Runnable acceptEar;

    /**
     * Create with a Node and an Editor.
     * @param gui the main GUI Node
     * @param ed an Editor
     */
    public ButtonPaneNew(Node gui, Editor ed)
    {
        this(new OpenSphereButtonBar(), gui, ed);
    }

    /**
     * Create with a Node, an Editor, and an ObservableButtonBar.
     * @param btn buttons
     * @param gui the main GUI Node
     * @param ed an Editor
     */
    public ButtonPaneNew(ObservableButtonBar btn, Node gui, Editor ed)
    {
        init(btn, gui, ed);
    }

    /**
     * Install an Editor.
     * @param ed an Editor
     */
    public void setEditor(Editor ed)
    {
        myEditor = ed;
        if (myEditor != null)
            FXUtilities.runOnFXThreadAndWait(() -> validate((o, s, m) -> setValidationStatus(s, m)));
        // By using runAndWait, we delay attaching and invoking the
        // validation listener until after everything is constructed.  If
        // we don't, then styling (during construction) can interfere with
        // reporting errors or warnings (from the validation support).
    }

    /**
     * Perform initial setup, including laying out the GUI.
     * @param btn buttons
     * @param gui the main GUI Node
     * @param ed an Editor
     */
    private void init(ObservableButtonBar btn, Node gui, Editor ed)
    {
        myButtonBar = btn;
        myStatusLabel = new Label();
        myStatusLabel.setTooltip(new Tooltip());
        myStatusLabel.setPadding(new Insets(3));
        myStatusLabel.setVisible(false);

        setPadding(new Insets(OUTER_SPACE));
        setCenter(gui);
        setBottom(FXUtilities.newHBox(myStatusLabel, FXUtilities.newHSpacer(), myButtonBar));
        setMargin(getBottom(), new Insets(VERTICAL_SPACE, 0, 0, 0));

        setEditor(ed);
    }

    /**
     * Attach the specified <i>ear</i> to the resident Editor's
     * ValidatorSupport, if possible.
     * @param ear a listener
     */
    private void validate(ValidationStatusChangeListener ear)
    {
        if (myEditor == null)
            return;
        ValidatorSupport val = myEditor.getValidatorSupport();
        if (val == null)
            return;
        val.addAndNotifyValidationListener(ear);
    }

    /**
     * Register a callback for acceptance.  This is a lighter-weight option in
     * comparison with using the Editor framework.
     * @param r the callback
     */
    public void setAcceptEar(Runnable r)
    {
        acceptEar = r;
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
     * Sets the response.
     *
     * @param response the response
     */
    public void setResponse(ButtonData response)
    {
        if (response == ButtonData.OK_DONE)
        {
            if (myEditor != null)
                myEditor.accept();
            if (acceptEar != null)
                acceptEar.run();
        }
    }

    /**
     * Sets the validation status and error message.
     *
     * @param status The validation status.
     * @param message A message detailing why the object is valid or not.
     */
    public void setValidationStatus(ValidationStatus status, String message)
    {
        Node okButton = myButtonBar.getButtons().stream().filter(b -> ButtonBar.getButtonData(b) == ButtonData.OK_DONE)
                .findAny().orElse(null);
        if (okButton != null)
        {
            okButton.setDisable(status == ValidationStatus.ERROR);
        }

        Color textColor = Color.WHITE;
        if (status == ValidationStatus.ERROR)
        {
            textColor = Color.TOMATO.brighter();
        }
        else if (status == ValidationStatus.WARNING)
        {
            textColor = Color.YELLOW;
        }
        myStatusLabel.setVisible(message != null);
        myStatusLabel.setText(message);
        myStatusLabel.getTooltip().setText(message);
        myStatusLabel.setTextFill(textColor);
    }
}

package io.opensphere.controlpanels.layers.importdata;

import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

import io.opensphere.core.importer.FileOrURLImporter;
import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.core.util.fx.Editor;

/**
 * Gui that pops up to allow the user to choose an importer when more than one
 * is available for the current operation.
 */
public class ChooserPane
{
    /** Bla. */
    private static final double THREE = 5.0;
    /** Bla. */
    private static final double FIVE = 5.0;
    /** Bla. */
    private static final double TEN = 5.0;
    /** Bla. */
    private static final double THREE_HUNDRED = 300.0;

    /** Show this message until the user makes a selection. */
    private static final String ERROR_MSG = "No importer is selected.";

    /** The root of the visible elements in this GUI. */
    private Gui mainPane = new Gui();
    /** The vertical list of selectable elements. */
    private VBox vList = new VBox();
    /** Shows the source to be imported to reduce potential confusion. */
    private TextField sourceTxt = new TextField();
    {
        sourceTxt.setEditable(false);
        sourceTxt.setPromptText("Source was not specified.");
        sourceTxt.setPrefWidth(THREE_HUNDRED);
        vList.setSpacing(THREE);
        vList.setPadding(new Insets(FIVE, TEN, FIVE, TEN));
        vList.getChildren().add(sourceTxt);
        Label spacer = new Label();
        spacer.prefHeight(TEN);
        vList.getChildren().add(spacer);
        mainPane.setContent(vList);
    }

    /** Validation support. */
    private DefaultValidatorSupport valSupp = new DefaultValidatorSupport(this);
    {
        valSupp.setValidationResult(ValidationStatus.ERROR, ERROR_MSG);
    }

    /** Manages radio buttons. */
    private ToggleGroup tGroup = new ToggleGroup();

    /** The selected importer, if any, or null. */
    private FileOrURLImporter sel;
    /** Flag indicates whether the user has accepted (vs. canceled). */
    private boolean accepted;

    /**
     * Show the user the source to be imported.
     * @param txt the source path or URL
     */
    public void setSource(String txt)
    {
        sourceTxt.setText(txt);
        sourceTxt.selectPositionCaret(txt.length());
    }

    /**
     * Bla.
     * @return bla
     */
    public Node getMainPane()
    {
        return mainPane;
    }

    /**
     * Bla.
     * @return bla
     */
    public boolean isAccepted()
    {
        return accepted;
    }

    /**
     * Bla.
     * @return bla
     */
    public FileOrURLImporter getSelected()
    {
        return sel;
    }

    /**
     * Bla.
     * @param imp bla
     */
    private void setSelected(FileOrURLImporter imp)
    {
        sel = imp;
        if (sel == null)
        {
            valSupp.setValidationResult(ValidationStatus.ERROR, ERROR_MSG);
        }
        else
        {
            valSupp.setValidationResult(ValidationStatus.VALID, null);
        }
    }

    /**
     * Specify the list of selectable importers.
     * @param fouis bla
     */
    public void setFoui(List<FileOrURLImporter> fouis)
    {
        // construct the GUI subcomponents
        for (FileOrURLImporter imp :  fouis)
        {
            RadioButton rb = new RadioButton(imp.getName());
            rb.setPadding(new Insets(0.0, 0.0, 0.0, TEN));
            rb.setToggleGroup(tGroup);
            rb.setOnAction(e -> setSelected(imp));
            vList.getChildren().add(rb);
        }
        tGroup.selectToggle(null);
    }

    /**
     * Actual root of the GUI.  Note the use of the "magic" interface
     * Editor, which must be implemented by the class the instance of
     * which is the GUI root in order to get validation support.
     */
    private class Gui extends ScrollPane implements Editor
    {
        @Override
        public ValidatorSupport getValidatorSupport()
        {
            return valSupp;
        }

        @Override
        public void accept()
        {
            accepted = true;
        }
    }
}

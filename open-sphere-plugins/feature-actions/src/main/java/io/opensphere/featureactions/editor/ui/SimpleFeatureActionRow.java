package io.opensphere.featureactions.editor.ui;

import java.awt.Component;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import javax.swing.SwingUtilities;

import io.opensphere.controlpanels.iconpicker.ui.IconPickerButton;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.fx.AutoCompleteComboBoxListener;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.featureactions.editor.model.CriteriaOptions;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.editor.model.SimpleFeatureActionGroup;
import io.opensphere.featureactions.editor.model.SimpleFeatureActions;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * A User Interface representing one feature action in the list of feature
 * actions.
 */
public class SimpleFeatureActionRow extends ListCell<SimpleFeatureAction> implements SimpleFeatureActionRowView
{
    /** The preferred width for the input text fields. */
    private static final int ourPreferredFieldWidth = 70;

    /** Contains all actions for a given layer. */
    private final SimpleFeatureActions myActions;

    /** Enables the combo box to be auto completed. */
    @SuppressWarnings("unused")
    private final AutoCompleteComboBoxListener<String> myAutoComplete;

    /** Binds this ui to the model. */
    private SimpleFeatureActionRowBinder myBinder;

    /** The color picker button. */
    private final ColorPicker myColorPicker = new ColorPicker();

    /** The copy button. */
    private final Button myCopyButton = createCopyButton();

    /** The edit button. */
    private final Button myEditButton = createEditButton();

    /** The enabled checkbox. */
    private final CheckBox myEnabled = new CheckBox();

    /** The column picker combo box. */
    private final ComboBox<String> myField = new ComboBox<>();

    /** The group the action belongs to. */
    private final SimpleFeatureActionGroup myGroup;

    /** The icon picker button. */
    private IconPickerButton myIconPicker;

    /** The maximum value field. */
    private final TextField myMaximumValue = new TextField();

    /** The minimum value field. */
    private final TextField myMinimumValue = new TextField();

    /** The model to bind to. */
    private SimpleFeatureAction myModel;

    /** The name field. */
    private final TextField myName = new TextField();

    /** The criteria picker combo box. */
    private final ComboBox<CriteriaOptions> myOptions = new ComboBox<>();

    /** The remove button. */
    private final Button myRemoveButton = createRemoveButton();

    /** The system toolbox. */
    private final Toolbox myToolbox;

    /** The value field. */
    private final TextField myValue = new TextField();

    /** A message indicating the complex filter editor is required. */
    private final Label filterMask = new Label("Complex Filter Criteria");

    /** A message indicating the complex filter editor is required. */
    private final Label styleMask = new Label("No Style Options");

    /** The layer (needed for editing the filter component). */
    private final DataTypeInfo layer;

    /** Callback for changes that may affect the display. */
    private Runnable editEar;

    /** Parent dialog for the detail editor. */
    private final Component parentDialog;

    /**
     * Constructs a new UI for an action row.
     *
     * @param toolbox The system toolbox.
     * @param actions Contains all actions for a given layer.
     * @param group The group the action belongs too.
     * @param type The layer whose feature action is being edited
     * @param dialog the dialog containing this editor
     */
    public SimpleFeatureActionRow(Toolbox toolbox, SimpleFeatureActions actions, SimpleFeatureActionGroup group,
            DataTypeInfo type, Component dialog)
    {
        myToolbox = toolbox;
        myActions = actions;
        myGroup = group;
        layer = type;
        myAutoComplete = new AutoCompleteComboBoxListener<>(myField);
        parentDialog = dialog;
    }

    @Override
    public IconPickerButton getIconPicker()
    {
        return myIconPicker;
    }

    @Override
    public ColorPicker getColorPicker()
    {
        return myColorPicker;
    }

    @Override
    public Button getCopyButton()
    {
        return myCopyButton;
    }

    @Override
    public CheckBox getEnabled()
    {
        return myEnabled;
    }

    @Override
    public ComboBox<String> getField()
    {
        return myField;
    }

    @Override
    public TextField getMaximumValue()
    {
        return myMaximumValue;
    }

    @Override
    public TextField getMinimumValue()
    {
        return myMinimumValue;
    }

    @Override
    public TextField getName()
    {
        return myName;
    }

    @Override
    public ComboBox<CriteriaOptions> getOptions()
    {
        return myOptions;
    }

    @Override
    public Button getRemoveButton()
    {
        return myRemoveButton;
    }

    @Override
    public TextField getValue()
    {
        return myValue;
    }

    @Override
    public Node getComplexFilterMask()
    {
        return filterMask;
    }

    @Override
    public Node getStyleAbsentMask()
    {
        return styleMask;
    }

    @Override
    public void setEditListener(Runnable ear)
    {
        editEar = ear;
    }

    @Override
    protected void updateItem(SimpleFeatureAction item, boolean empty)
    {
        super.updateItem(item, empty);
        myModel = item;
        if (myBinder != null)
        {
            myBinder.close();
        }
        if (myModel == null)
        {
            myBinder = null;
            setGraphic(null);
        }
        else
        {
            myBinder = new SimpleFeatureActionRowBinder(this,
                    MantleToolboxUtils.getMantleToolbox(myToolbox).getDataTypeController(), myActions, myGroup, myModel);
            setGraphic(createUI());
        }
    }

    /**
     * Creates the copy button.
     *
     * @return the button
     */
    private Button createCopyButton()
    {
        Button button = FXUtilities.newIconButton(IconType.COPY);
        button.setTooltip(new Tooltip("Copy"));
        return button;
    }

    /**
     * Creates the edit button.
     *
     * @return the button
     */
    private Button createEditButton()
    {
        Button button = FXUtilities.newIconButton(IconType.EDIT);
        button.setTooltip(new Tooltip("Edit"));
        // listener is called on JFX thread, so we must thread-shift Swing stuff
        button.setOnAction(e -> SwingUtilities.invokeLater(() -> handleEdit()));
        return button;
    }

    /**
     * Creates the remove button.
     *
     * @return the button
     */
    private Button createRemoveButton()
    {
        Button button = FXUtilities.newIconButton(IconType.CLOSE, Color.RED);
        button.setTooltip(new Tooltip("Remove Action"));
        return button;
    }

    /**
     * Create the ui components.
     *
     * @return The created UI.
     */
    private Node createUI()
    {
        myIconPicker = new IconPickerButton(myToolbox, myModel.iconIdProperty());

        myEnabled.setTooltip(new Tooltip("When checked this action is used to style features"));
        myName.setPrefWidth(ourPreferredFieldWidth);
        myName.setTooltip(new Tooltip("The name of the action"));
        myName.getStyleClass().add("copyable-label");
        myField.setPromptText("Choose a Column...");
        myField.setTooltip(new Tooltip("The column to compare when applying feature actions"));
        myOptions.setTooltip(new Tooltip("Pick either value or range"));
        myValue.setPrefWidth(ourPreferredFieldWidth);
        myValue.setPromptText("e.g. B*");
        myValue.setTooltip(
                new Tooltip("The value the rows' cells need to be in order to apply this action, use * as a wildcard"));
        myMinimumValue.setPrefWidth(ourPreferredFieldWidth);
        myMinimumValue.setTooltip(new Tooltip("The lowest value the rows' cells need to be in order to apply this action"));
        myMaximumValue.setPrefWidth(ourPreferredFieldWidth);
        myMaximumValue.setTooltip(new Tooltip("The highest value the rows' cells need to be in order to apply this action"));
        myColorPicker.setStyle("-fx-color-label-visible: false ;");
        myColorPicker.setTooltip(new Tooltip("The color to change the row to that meet the values specified"));
        myIconPicker.setTooltip(new Tooltip("The icon to change the row to that meet the values specified"));

        GridPane box = new GridPane();
        box.setHgap(5);
        box.add(myEnabled, 0, 0);
        box.add(myName, 1, 0);
        box.add(filterMask, 2, 0, 4, 1);
        box.add(myField, 2, 0);
        box.add(myOptions, 3, 0);
        box.add(myValue, 4, 0);
        box.add(myMinimumValue, 4, 0);
        box.add(myMaximumValue, 5, 0);
        box.add(styleMask, 6, 0, 2, 1);
        box.add(myColorPicker, 6, 0);
        box.add(myIconPicker, 7, 0);
        Region spacer = new Region();
        spacer.setPrefWidth(50);
        box.add(spacer, 8, 0);
        box.add(myEditButton, 9, 0);
        box.add(myCopyButton, 10, 0);
        box.add(myRemoveButton, 11, 0);

        return box;
    }

    /** Launches the editor, on the swing thread, for this feature action. */
    private void handleEdit()
    {
        DetailEditor ed = new DetailEditor();
        ed.setup(myToolbox, myModel, layer);
        ed.setEditListener(editEar);
        ed.launch(parentDialog);
    }
}

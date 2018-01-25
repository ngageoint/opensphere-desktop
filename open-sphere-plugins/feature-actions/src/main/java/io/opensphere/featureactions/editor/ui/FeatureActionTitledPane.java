package io.opensphere.featureactions.editor.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.featureactions.editor.model.SimpleFeatureActionGroup;
import io.opensphere.featureactions.editor.model.SimpleFeatureActions;

/**
 * Applies a custom header to the {@link TitledPane} allowing user to edit and
 * manipulate the groups.
 */
public class FeatureActionTitledPane extends TitledPane
{
    /** The add button. */
    private Button myAddButton;

    /**
     * Binds this UI to a {@link SimpleFeatureActionGroup} model.
     */
    private final FeatureActionTitledPaneBinder myBinder;

    /**
     * Indicates if this UI has been rendered yet.
     */
    private boolean myIsRendered;

    /**
     * The name of the feature action group.
     */
    private TextField myLabel;

    /**
     * The remove group button.
     */
    private Button myRemoveButton;

    /**
     * The checkbox to disable/enable the whole feature action group.
     */
    private CheckBox mySelectDeselectAll;

    /**
     * Constructs a new titled pane.
     *
     * @param containingAccordion The {@link Accordion} this titled pane is
     *            contained it.
     * @param mainModel The main model for simple feature actions.
     * @param group The feature action group.
     * @param content The content of the pane.
     * @param registry the UI Registry used to coordinate user interactions.
     */
    public FeatureActionTitledPane(Accordion containingAccordion, SimpleFeatureActions mainModel, SimpleFeatureActionGroup group,
            Node content, UIRegistry registry)
    {
        super("", content);
        createUI();
        myBinder = new FeatureActionTitledPaneBinder(containingAccordion, this, mainModel, group, registry);
    }

    /**
     * Stops updating the model.
     */
    public void close()
    {
        myBinder.close();
    }

    /**
     * Gets the add feature action button.
     *
     * @return The add feature action button.
     */
    public Button getAddButton()
    {
        return myAddButton;
    }

    /**
     * Gets the remove feature action group button.
     *
     * @return The remove feature action group button.
     */
    public Button getRemoveButton()
    {
        return myRemoveButton;
    }

    /**
     * Gets the checkbox where the user can enable/disable the whole group of
     * actions.
     *
     * @return The select all deselect all checkbox.
     */
    public CheckBox getSelectDeselectAll()
    {
        return mySelectDeselectAll;
    }

    /**
     * Gets the group name label.
     *
     * @return The groups name.
     */
    public TextField getTitle()
    {
        return myLabel;
    }

    @Override
    protected void layoutChildren()
    {
        super.layoutChildren();

        if (!myIsRendered)
        {
            myIsRendered = true;
            Pane arrow = (Pane)lookup(".arrow");
            if (arrow != null)
            {
                arrow.translateXProperty().bind(widthProperty().subtract(arrow.widthProperty().multiply(2)));
            }
        }
    }

    /**
     * Creates the UI components.
     */
    private void createUI()
    {
        mySelectDeselectAll = new CheckBox();
        myLabel = new TextField();
        myLabel.getStyleClass().add("copyable-label");
        myAddButton = FXUtilities.newIconButton("Add", IconType.PLUS, Color.LIME);
        myAddButton.setTooltip(new Tooltip("Add a new Feature Action"));

        myRemoveButton = FXUtilities.newIconButton(IconType.CLOSE, Color.RED);
        myRemoveButton.setTooltip(new Tooltip("Remove this Feature Group"));

        HBox box = new HBox(5);
        box.setPadding(new Insets(2, 0, 2, 0));
        box.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().addAll(mySelectDeselectAll, myLabel, myAddButton, myRemoveButton);
        setGraphic(box);
        getStylesheets().add(
                getClass().getResource("/io/opensphere/featureactions/editor/ui/FeatureActionTitlePane.css").toExternalForm());
    }
}

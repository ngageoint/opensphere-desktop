package io.opensphere.featureactions.editor.ui;

import java.awt.Dimension;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.editor.model.SimpleFeatureActionGroup;
import io.opensphere.featureactions.editor.model.SimpleFeatureActions;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.featureactions.model.StyleAction;

/**
 * Binds the {@link FeatureActionTitledPane} to a
 * {@link SimpleFeatureActionGroup} so that the values in the group are
 * reflected to the UI and the values in the UI get set in the group.
 */
public class FeatureActionTitledPaneBinder
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(FeatureActionTitledPaneBinder.class);

    /**
     * Listens for the select all check box changes.
     */
    private final ChangeListener<Boolean> myCheckChangedListener = (obs, old, val) -> handleCheckChange(val.booleanValue());

    /**
     * The accordion containing the the titled panes.
     */
    private final Accordion myContainingAccordion;

    /**
     * The model to bind.
     */
    private final SimpleFeatureActionGroup myGroup;

    /**
     * The main model for simple feature actions.
     */
    private final SimpleFeatureActions myMainModel;

    /**
     * The UI to bind.
     */
    private final FeatureActionTitledPane myView;

    /** The registry through which UI interaction is handled. */
    private final UIRegistry myUiRegistry;

    /**
     * Constructs a new binder.
     *
     * @param containingAccordion The accordion containing the the titled panes.
     * @param view The UI to bind to the model.
     * @param mainModel The main model for simple feature actions.
     * @param group The model to bind to the UI.
     * @param uiRegistry The registry through which UI interaction is handled.
     */
    public FeatureActionTitledPaneBinder(Accordion containingAccordion, FeatureActionTitledPane view,
            SimpleFeatureActions mainModel, SimpleFeatureActionGroup group, UIRegistry uiRegistry)
    {
        myContainingAccordion = containingAccordion;
        myView = view;
        myMainModel = mainModel;
        myGroup = group;
        myUiRegistry = uiRegistry;
        bindUI();
    }

    /**
     * Stops synchronizing the UI with the model.
     */
    public void close()
    {
        myView.getTitle().textProperty().unbindBidirectional(myGroup.groupNameProperty());
        myView.getAddButton().setOnAction(null);
        myView.getSelectDeselectAll().selectedProperty().removeListener(myCheckChangedListener);
    }

    /**
     * Binds the UI and the model together.
     */
    private void bindUI()
    {
        myView.getTitle().textProperty().bindBidirectional(myGroup.groupNameProperty());
        myView.getAddButton().setOnAction(e -> handleAdd());
        myView.getRemoveButton().setOnAction(e -> handleRemove());

        boolean isAllSelected = true;
        for (SimpleFeatureAction action : myGroup.getActions())
        {
            if (!action.getFeatureAction().isEnabled())
            {
                isAllSelected = false;
                break;
            }
        }
        buildTooltip(isAllSelected);
        myView.getSelectDeselectAll().selectedProperty().set(isAllSelected);
        myView.getSelectDeselectAll().selectedProperty().addListener(myCheckChangedListener);
    }

    /**
     * Builds and sets an appropriate tooltip for the select all check box.
     *
     * @param isChecked True if the check box is checked, false if unchecked.
     */
    private void buildTooltip(boolean isChecked)
    {
        StringBuilder builder = new StringBuilder(50);
        builder.append("Click to ");
        if (!isChecked)
        {
            builder.append("enable");
        }
        else
        {
            builder.append("disable");
        }
        builder.append(" all actions in ");
        builder.append(myGroup.getGroupName());
        myView.getSelectDeselectAll().setTooltip(new Tooltip(builder.toString()));
    }

    /** Responds when the add button is pressed. */
    private void handleAdd()
    {
        FeatureAction act = new FeatureAction();
        act.getActions().add(new StyleAction());
        myGroup.getActions().add(new SimpleFeatureAction(act));
        if (!myView.equals(myContainingAccordion.getExpandedPane()))
        {
            myContainingAccordion.setExpandedPane(myView);
        }
    }

    /**
     * Responds to checkbox changes for the Select All check box.
     *
     * @param isChecked The new checked value of the select all check box.
     */
    private void handleCheckChange(boolean isChecked)
    {
        buildTooltip(isChecked);
        for (SimpleFeatureAction action : myGroup.getActions())
        {
            action.getFeatureAction().setEnabled(isChecked);
        }
    }

    /** Responds when the add button is pressed. */
    @SuppressWarnings("PMD.AvoidCatchingNPE")
    private void handleRemove()
    {
        try
        {
            ReadOnlyObjectProperty<Scene> scene = myView.sceneProperty();
            if (scene != null)
            {
                EventQueueUtilities.invokeLater(() ->
                {
                    String message = "Delete the feature group " + myGroup.getGroupName() + "?";
                    JFrame parent = myUiRegistry.getMainFrameProvider().get();
                    JFXDialog dialog = new JFXDialog(parent, "Delete Group?", () -> new Label(message));
                    dialog.setLocationRelativeTo(parent);
                    dialog.setAcceptEar(() -> myMainModel.getFeatureGroups().remove(myGroup));
                    dialog.setModal(true);
                    dialog.setSize(new Dimension(320, 120));
                    dialog.setVisible(true);
                });
            }
        }
        catch (NullPointerException e)
        {
            LOGGER.debug("Null pointer exception encountered while getting scene property. Deleting anyway.", e);
            myMainModel.getFeatureGroups().remove(myGroup);
        }
    }
}

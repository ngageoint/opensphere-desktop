package io.opensphere.featureactions.editor.ui;

import java.awt.Component;

import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.core.util.fx.Editor;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.featureactions.registry.FeatureActionsRegistry;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The main UI where the user is able to create, view, update, and delete
 * feature actions for any feature layer.
 */
public class SimpleFeatureActionEditorUI extends BorderPane implements SimpleFeatureActionEditor, Editor
{
    /** The accordion showing all feature actions and groups. */
    private Accordion myAccordion;

    /** The add button. */
    private Button myAddButton;

    /** Keeps the UI and model in sync. */
    private final SimpleFeatureActionEditorBinder myBinder;

    /** Saves any changes. */
    private Runnable mySaveListener;

    /**
     * Constructs a new editor UI.
     *
     * @param toolbox The system toolbox.
     * @param actionRegistry Place to save and read all feature actions.
     * @param hostWindow The window holding this GUI
     * @param layer The layer we are editing feature actions for.
     */
    public SimpleFeatureActionEditorUI(Toolbox toolbox, FeatureActionsRegistry actionRegistry, Component hostWindow,
            DataTypeInfo layer)
    {
        createUI();
        myBinder = new SimpleFeatureActionEditorBinder(toolbox, this, actionRegistry, layer, hostWindow);
    }

    @Override
    public void accept()
    {
        mySaveListener.run();
    }

    /** Stops updating the model. */
    public void close()
    {
        myBinder.close();
    }

    @Override
    public Accordion getAccordion()
    {
        return myAccordion;
    }

    @Override
    public Button getAddButton()
    {
        return myAddButton;
    }

    @Override
    public ValidatorSupport getValidatorSupport()
    {
        return null;
    }

    @Override
    public void setSaveListener(Runnable runnable)
    {
        mySaveListener = runnable;
    }

    /** Creates the UI components. */
    private void createUI()
    {
        myAddButton = FXUtilities.newIconButton("New Group", IconType.PLUS, Color.LIME);
        myAddButton.setTooltip(new Tooltip("Add a new Feature Action Group"));
        HBox box = new HBox(5);
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        box.getChildren().addAll(spacer, myAddButton);
        setTop(box);
        BorderPane.setMargin(getTop(), new Insets(0, 0, 8, 0));

        myAccordion = new Accordion();
        setCenter(myAccordion);
    }
}

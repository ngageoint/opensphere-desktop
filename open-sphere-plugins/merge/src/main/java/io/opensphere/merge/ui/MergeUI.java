package io.opensphere.merge.ui;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.core.util.fx.Editor;
import io.opensphere.mantle.data.columns.gui.Constants;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.merge.controller.ColumnAssociationsLauncher;
import io.opensphere.merge.controller.MergeController;
import io.opensphere.merge.controller.MergeValidator;
import io.opensphere.merge.model.MergeModel;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

/**
 * The merge UI that allows the user to set a new merged layer name and to
 * configure any column associations.
 */
public class MergeUI extends BorderPane implements Editor
{
    /**
     * The controller, actually performs the merge.
     */
    private final MergeController myController;

    /**
     * The button the user can click in order to launch the column associations
     * editor.
     */
    private Button myLaunchAssociations;

    /**
     * Launches the column associations.
     */
    private final ColumnAssociationsLauncher myLauncher;

    /**
     * The model.
     */
    private final MergeModel myModel;

    /**
     * The new layer name text field.
     */
    private TextField myNewName;

    /**
     * The label that will show information messages to the user.
     */
    private Label myUserMessage;

    /**
     * Used to validate the model.
     */
    private final MergeValidator myValidator;

    /**
     * The merge UI constructor.
     *
     * @param toolbox The system toolbox.
     * @param controller The controller that drives the merge.
     * @param model The model.
     */
    public MergeUI(Toolbox toolbox, MergeController controller, MergeModel model)
    {
        super();
        myModel = model;
        myLauncher = new ColumnAssociationsLauncher(toolbox);
        myValidator = new MergeValidator(MantleToolboxUtils.getMantleToolbox(toolbox).getDataGroupController(), model);
        myController = controller;
        createUI();
    }

    @Override
    public void accept()
    {
        myController.performMerge();
    }

    @Override
    public ValidatorSupport getValidatorSupport()
    {
        return myValidator.getValidatorSupport();
    }

    /**
     * Gets the value of the {@link #myNewName} field.
     *
     * @return the value stored in the {@link #myNewName} field.
     */
    protected TextField getNewName()
    {
        return myNewName;
    }

    /**
     * Gets the value of the {@link #myUserMessage} field.
     *
     * @return the value stored in the {@link #myUserMessage} field.
     */
    protected Label getUserMessage()
    {
        return myUserMessage;
    }

    /**
     * Creates the ui.
     */
    private void createUI()
    {
        GridPane grid = new GridPane();

        grid.setVgap(5);

        myNewName = new TextField();
        myNewName.textProperty().bindBidirectional(myModel.getNewLayerName());
        myNewName.setTooltip(new Tooltip("The name of the merged layer"));
        myNewName.setMinWidth(430);
        grid.add(new Label("Layer Name:"), 0, 0);
        grid.add(myNewName, 0, 1, 4, 1);

        setTop(grid);

        GridPane bottom = new GridPane();

        myUserMessage = new Label();
        myUserMessage.textProperty().bindBidirectional(myModel.getUserMessage());

        bottom.add(myUserMessage, 0, 2);

        myLaunchAssociations = new Button(Constants.COLUMN_MAPPING.pluralTitleCase());
        myLaunchAssociations.setTooltip(new Tooltip("Launches the " + Constants.COLUMN_MAPPING.pluralTitleCase() + " editor"));
        myLaunchAssociations.setOnAction(e -> myLauncher.launchColumnAssociations());
        Image image = new Image(getClass().getResourceAsStream("/images/columnassociations.png"));
        myLaunchAssociations.setGraphic(new ImageView(image));

        bottom.add(myLaunchAssociations, 0, 3);

        setBottom(bottom);
    }
}

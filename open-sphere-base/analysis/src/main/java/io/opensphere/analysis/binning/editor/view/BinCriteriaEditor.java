package io.opensphere.analysis.binning.editor.view;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

import io.opensphere.analysis.binning.criteria.BinCriteria;
import io.opensphere.analysis.binning.criteria.BinCriteriaElement;
import io.opensphere.analysis.binning.editor.controller.BinCriteriaController;
import io.opensphere.analysis.binning.editor.model.BinCriteriaModel;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.mantle.controller.DataGroupController;

/**
 * The UI that allows users to create and edit bin criteria. Bin criteria is one
 * of the inputs to the binning algorithm.
 */
public class BinCriteriaEditor extends BorderPane implements BinCriteriaView
{
    /**
     * The add button that allows the user to add new criteria elements to the
     * criteria.
     */
    private Button myAddButton;

    /**
     * Keeps the view in sync with the model.
     */
    private final BinCriteriaBinder myBinder;

    /**
     * The controller of this view.
     */
    private final BinCriteriaController myController;

    /**
     * The view containing the list of individual criterion rows.
     */
    private ListView<BinCriteriaElement> myCriterionView;

    /**
     * The model containing all the criteria data.
     */
    private final BinCriteriaModel myModel;

    /**
     * Constructs a new criteria editor to create a new criteria.
     *
     * @param ctrl Used to get column information for the specified layer.
     * @param dataTypeId The layer we are creating criteria for.
     */
    public BinCriteriaEditor(DataGroupController ctrl, String dataTypeId)
    {
        this(ctrl, dataTypeId, null);
    }

    /**
     * Constructs a new criteria editor to edit an existing criteria.
     *
     * @param ctrl Used to get column information for the specified layer.
     * @param dataTypeId The layer we are creating criteria for.
     * @param existingCriteria The existing criteria to edit.
     */
    public BinCriteriaEditor(DataGroupController ctrl, String dataTypeId, BinCriteria existingCriteria)
    {
        setCenter(createCenterPane());
        myController = new BinCriteriaController(ctrl, dataTypeId, existingCriteria);
        myModel = myController.getModel();
        myBinder = new BinCriteriaBinder(this, myModel);
    }

    /**
     * Stops listening for any changes made by the user.
     */
    public void close()
    {
        myBinder.close();
    }

    @Override
    public Button getAddButton()
    {
        return myAddButton;
    }

    /**
     * Gets the edited {@link BinCriteria}.
     *
     * @return The bin criteria.
     */
    public BinCriteria getCriteria()
    {
        return myModel.getCriteria();
    }

    @Override
    public ListView<BinCriteriaElement> getCriterionView()
    {
        return myCriterionView;
    }

    /**
     * Creates the add field button.
     *
     * @return the button.
     */
    private Node createAddFieldButton()
    {
        myAddButton = FXUtilities.newIconButton("Add Field", IconType.PLUS, Color.LIME);
        myAddButton.setTooltip(new Tooltip("Add a field"));
        return myAddButton;
    }

    /**
     * Creates the center pane.
     *
     * @return the pane.
     */
    private Node createCenterPane()
    {
        BorderPane pane = new BorderPane();
        pane.setTop(createAddFieldButton());
        setMargin(pane.getTop(), new Insets(0, 0, 8, 0));
        pane.setCenter(createCriteriaList());

        return pane;
    }

    /**
     * Creates the criteria list view.
     *
     * @return the list view
     */
    private Node createCriteriaList()
    {
        myCriterionView = new ListView<>();
        myCriterionView.setCellFactory(param -> new CriterionCell(myModel));

        return myCriterionView;
    }
}

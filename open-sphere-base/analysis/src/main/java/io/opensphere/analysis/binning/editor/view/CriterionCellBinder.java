package io.opensphere.analysis.binning.editor.view;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

import io.opensphere.analysis.binning.criteria.BinCriteriaElement;
import io.opensphere.analysis.binning.criteria.CriteriaType;
import io.opensphere.analysis.binning.criteria.CriteriaTypeFactory;
import io.opensphere.analysis.binning.criteria.RangeCriteria;
import io.opensphere.analysis.binning.editor.model.BinCriteriaModel;
import io.opensphere.analysis.binning.editor.model.CriterionModel;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;

/**
 * Binds a {@link CriterionCellView} to its {@link BinCriteriaElement} model so
 * that the values in the view are in sync with the values in the model.
 */
public class CriterionCellBinder implements Observer, EventHandler<ActionEvent>, ListChangeListener<BinCriteriaElement>
{
    /**
     * The model containing the single row criteria data.
     */
    private final CriterionModel myCriterionModel;

    /**
     * The main model which contains the criteria data we are editing.
     */
    private final BinCriteriaModel myModel;

    /**
     * The property containing the currently selected bin type in the bin
     * criteria. Used to bind the model to the ui.
     */
    private final StringProperty mySelectedBinTypeProperty;

    /**
     * The property containing the currently selected field in the bin criteria
     * row. Used to bind the model to the ui.
     */
    private final StringProperty mySelectedFieldProperty;

    /**
     * The Converts the string tolerance (from UI) to double value so we can put
     * the value in the model.
     */
    private final StringConverter<Number> myToleranceConverter;

    /**
     * The bins range double property used for binding the model to the ui.
     */
    private final DoubleProperty myToleranceProperty;

    /**
     * The bins range string property used for binding the model to the ui.
     */
    private final StringProperty myToleranceStringProperty;

    /**
     * The view showing the data from the criterion model.
     */
    private final CriterionCellView myView;

    /**
     * Constructs a new binder.
     *
     * @param view The view to bind.
     * @param criterionModel The model to bind the view to.
     * @param model The model containing element.
     */
    public CriterionCellBinder(CriterionCellView view, CriterionModel criterionModel, BinCriteriaModel model)
    {
        myView = view;
        myCriterionModel = criterionModel;
        myModel = model;
        myToleranceProperty = new SimpleDoubleProperty();
        setToleranceOnView();
        mySelectedFieldProperty = new SimpleStringProperty();
        setFieldOnView();
        mySelectedBinTypeProperty = new SimpleStringProperty();
        setBinTypeOnView();
        myToleranceStringProperty = new SimpleStringProperty();
        setToleranceOnView();
        myToleranceConverter = new NumberStringConverter();
        bind();
    }

    /**
     * Stops synchronizing changes between view and model.
     */
    public void close()
    {
        myCriterionModel.getElement().deleteObserver(this);
        myView.getRemoveButton().setOnAction(null);

        myView.getTolerance().textProperty().unbindBidirectional(myToleranceStringProperty);
        myView.getFieldBox().valueProperty().unbindBidirectional(mySelectedFieldProperty);
        myView.getBinTypeBox().valueProperty().unbindBidirectional(mySelectedBinTypeProperty);

        myView.getBinTypeBox().setItems(FXCollections.observableArrayList());
        myModel.getCriteria().getCriterias().removeListener(this);
    }

    @Override
    public void handle(ActionEvent event)
    {
        myModel.getCriteria().getCriterias().remove(myCriterionModel.getElement());
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (BinCriteriaElement.FIELD_PROP.equals(arg))
        {
            setFieldOnView();
        }
        else if (BinCriteriaElement.CRITERIA_TYPE_PROP.equals(arg))
        {
            setBinTypeOnView();
            setToleranceOnView();
        }
    }

    /**
     * Binds the UI to the model so their values are in sync.
     */
    private void bind()
    {
        myView.getBinTypeBox().setItems(myCriterionModel.getBinTypes());
        myView.getBinTypeBox().valueProperty().bindBidirectional(mySelectedBinTypeProperty);
        mySelectedBinTypeProperty.addListener(this::setBinTypeOnModel);

        List<String> fieldNames = New.list();
        for (Pair<String, Class<?>> field : myModel.getLayersFields())
        {
            fieldNames.add(field.getFirstObject());
        }
        myView.getFieldBox().getItems().clear();
        myView.getFieldBox().getItems().addAll(fieldNames);
        myView.getFieldBox().valueProperty().bindBidirectional(mySelectedFieldProperty);
        mySelectedFieldProperty.addListener(this::setFieldOnModel);

        Bindings.bindBidirectional(myToleranceStringProperty, myToleranceProperty, myToleranceConverter);
        myView.getTolerance().textProperty().bindBidirectional(myToleranceStringProperty);
        myToleranceProperty.addListener(this::setToleranceOnModel);

        myView.getRemoveButton().setOnAction(this);
        myCriterionModel.getElement().addObserver(this);
        hideOrShowTolerance();
        hideOrShowRemoveButton();
        myModel.getCriteria().getCriterias().addListener(this);
    }

    /**
     * Hides or shows the remove button depending on how many criteria elements
     * are present.
     */
    private void hideOrShowRemoveButton()
    {
        if (myModel.getCriteria().getCriterias().size() > 1)
        {
            myView.getRemoveButton().setVisible(true);
        }
        else
        {
            myView.getRemoveButton().setVisible(false);
        }
    }

    /**
     * Hides or shows the tolerance text field depending on the bin type.
     */
    private void hideOrShowTolerance()
    {
        boolean visible = myCriterionModel.getElement().getCriteriaType() instanceof RangeCriteria;
        myView.getTolerance().setVisible(visible);
        myView.getToleranceLabel().setVisible(visible);
    }

    /**
     * Takes the bin type from the UI and sets it in the model.
     *
     * @param observable The new selected bin type.
     * @param oldValue The old selected bin type.
     * @param newValue The new selected bin type.
     */
    private void setBinTypeOnModel(ObservableValue<? extends String> observable, String oldValue, String newValue)
    {
        CriteriaType criteriaType = CriteriaTypeFactory.getInstance().newCriteriaType(newValue);
        myCriterionModel.getElement().setCriteriaType(criteriaType);
        hideOrShowTolerance();
    }

    /**
     * Takes the selected bin type from the model and applies it to the UI.
     */
    private void setBinTypeOnView()
    {
        if (myCriterionModel.getElement().getCriteriaType() != null)
        {
            mySelectedBinTypeProperty.setValue(myCriterionModel.getElement().getCriteriaType().getCriteriaType());
        }
    }

    /**
     * Takes the selected field from the UI and sets it in the model.
     *
     * @param observable The currently selected field.
     * @param oldValue The previously selected field.
     * @param newValue The currently selected field.
     */
    private void setFieldOnModel(ObservableValue<? extends String> observable, String oldValue, String newValue)
    {
        myCriterionModel.getElement().setField(newValue);
    }

    /**
     * Takes the selected field from the model and applies it to the UI.
     */
    private void setFieldOnView()
    {
        mySelectedFieldProperty.setValue(myCriterionModel.getElement().getField());
    }

    /**
     * Takes the tolerance set in the UI and applies it to the model.
     *
     * @param observable The current tolerance value.
     * @param oldValue The previous tolerance value.
     * @param newValue The new tolerance value.
     */
    private void setToleranceOnModel(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
    {
        CriteriaType criteriaType = myCriterionModel.getElement().getCriteriaType();
        if (criteriaType instanceof RangeCriteria)
        {
            ((RangeCriteria)criteriaType).setBinWidth(observable.getValue().doubleValue());
        }
    }

    /**
     * Takes the tolerance from the model and applies it to the UI.
     */
    private void setToleranceOnView()
    {
        CriteriaType criteriaType = myCriterionModel.getElement().getCriteriaType();
        if (criteriaType instanceof RangeCriteria)
        {
            myToleranceProperty.setValue(Double.valueOf(((RangeCriteria)criteriaType).getBinWidth()));
        }
        else
        {
            myToleranceProperty.setValue(Double.valueOf(0));
        }
    }

    @Override
    public void onChanged(javafx.collections.ListChangeListener.Change<? extends BinCriteriaElement> c)
    {
        hideOrShowRemoveButton();
    }
}

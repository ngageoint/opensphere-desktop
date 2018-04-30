package io.opensphere.filterbuilder2.editor.model;

import java.util.Collection;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.core.model.LatLonAltParser;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.DefaultPredicateWithMessage;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.ObservableValueValidatorSupport;
import io.opensphere.core.util.PredicateWithMessage;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.predicate.NonEmptyPredicate;
import io.opensphere.core.util.predicate.NumberPredicate;
import io.opensphere.core.util.swing.input.model.ChoiceModel;
import io.opensphere.core.util.swing.input.model.DateModel;
import io.opensphere.core.util.swing.input.model.GhostTextModel;
import io.opensphere.core.util.swing.input.model.PropertyChangeListener;
import io.opensphere.core.util.swing.input.model.TextModel;
import io.opensphere.core.util.swing.input.model.TextModelListValidatorSupport;
import io.opensphere.core.util.swing.input.model.WrappedModel;
import io.opensphere.filterbuilder.filter.v1.Criteria;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LongitudeKey;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;

/**
 * A GUI model for a filter criterion.
 */
@SuppressWarnings("PMD.GodClass")
public class CriterionModel extends WrappedModel<Criteria>
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(CriterionModel.class);

    /** The filter. */
    private final DataFilter myFilter;

    /** The data type meta data the filter is built against. */
    private final DataTypeInfo myDataType;

    /** The field. */
    private final ChoiceModel<String> myField = new ChoiceModel<>();

    /** The operator. */
    private final ChoiceModel<Conditional> myOperator = new ChoiceModel<>();

    /** The value (or min value). */
    private GhostTextModel myCriterionValue = new GhostTextModel();

    /** The max value. */
    private TextModel myCriterionMaxValue = new TextModel();

    /** The parent. */
    private GroupModel myParent;

    /** The special type for the field, which may be null. */
    private SpecialKey mySpecialType;

    /** The current validator, which changes based on the field and operator. */
    private PredicateWithMessage<String> myValidator;

    /**
     * Gets the operators for the given class type.
     *
     * @param clazz the class type
     * @return the operators
     */
    private static Conditional[] getOperators(Class<?> clazz)
    {
        Conditional[] operators = Conditional.STRING_VALUES;
        if (clazz != null && Number.class.isAssignableFrom(clazz))
        {
            operators = Conditional.NUMBER_VALUES;
        }
        return operators;
    }

    /**
     * Copy constructor.
     *
     * @param other the CriterionModel from which to copy
     */
    public CriterionModel(CriterionModel other)
    {
        this(other.myFilter, other.myDataType);
        setFromModelInternal(other);
    }

    /**
     * Constructor.
     *
     * @param filter The filter
     * @param dataType the data type
     */
    public CriterionModel(DataFilter filter, final DataTypeInfo dataType)
    {
        myFilter = filter;
        myDataType = dataType;
        TreeSet<String> colsLexOrder = new TreeSet<>(filter.getColumns());
        myField.setOptions(colsLexOrder.toArray(new String[colsLexOrder.size()]));

        myField.setNameAndDescription("Field", "The field");
        myOperator.setNameAndDescription("Comparison Operator", "The comparison operator");

        addModel(myField);
        addModel(myOperator);

        initValueModels();

        myField.addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                Class<?> valueType;
                if (dataType != null && dataType.getMetaDataInfo() != null)
                {
                    valueType = dataType.getMetaDataInfo().getKeyClassType(myField.get());
                    checkModels();
                    mySpecialType = dataType.getMetaDataInfo().getSpecialTypeForKey(myField.get());
                }
                else
                {
                    mySpecialType = null;
                    valueType = null;
                }

                updateValidator(valueType);
                updateViewModelValidatorSupports();
                myOperator.setOptions(getOperators(valueType));
            }
        });

        myOperator.addListener((obs, v0, v1) ->
        {
            Conditional cond = myOperator.get();
            if (cond == Conditional.IN_LIST || cond == Conditional.NOT_IN_LIST)
            {
                setOperandHelp("The value in the format: VALUE1, VALUE2, etc.", "e.g., a,b,c");
            }
            else if (cond == Conditional.LIKE_LIST || cond == Conditional.NOT_LIKE_LIST)
            {
                setOperandHelp("The value in the format: VALUE1, VALUE2, etc. The wildcard character is *",
                        "e.g., abc*, *def, etc.");
            }
            else if (cond == Conditional.BETWEEN)
            {
                setOperandHelp("The minimum value", null);
            }
            else if (cond == Conditional.LIKE)
            {
                setOperandHelp("The value.  The wildcard character is *", "e.g., abc*");
            }
            else
            {
                setOperandHelp("The value", null);
            }

            myCriterionValue.setVisible(cond != Conditional.EMPTY && cond != Conditional.NOT_EMPTY);
            myCriterionMaxValue.setVisible(cond == Conditional.BETWEEN);

            updateViewModelValidatorSupports();

            // Trigger value event for validation
            myCriterionValue.fireChangeEvent();
            if (cond == Conditional.BETWEEN)
            {
                myCriterionMaxValue.fireChangeEvent();
            }
        });
    }

    /**
     * Specify parameters for the current operator.
     *
     * @param desc description
     * @param ghost ghost text
     */
    private void setOperandHelp(String desc, String ghost)
    {
        myCriterionValue.setDescription(desc);
        myCriterionValue.setGhostText(ghost);
    }

    /**
     * Gets the max value.
     *
     * @return the max value
     */
    public TextModel getCriterionMaxValue()
    {
        return myCriterionMaxValue;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public TextModel getCriterionValue()
    {
        checkModels();
        return myCriterionValue;
    }

    /**
     * Gets the field.
     *
     * @return the field
     */
    public ChoiceModel<String> getField()
    {
        return myField;
    }

    /**
     * Gets the operator.
     *
     * @return the operator
     */
    public ChoiceModel<Conditional> getOperator()
    {
        return myOperator;
    }

    /**
     * Gets the parent.
     *
     * @return the parent
     */
    public GroupModel getParent()
    {
        return myParent;
    }

    /**
     * Gets the special type.
     *
     * @return the special type
     */
    public SpecialKey getSpecialType()
    {
        return mySpecialType;
    }

    /**
     * Sets the values in this object from the given model.
     *
     * @param other the other model
     */
    public void setFromModel(CriterionModel other)
    {
        setFromModelInternal(other);
    }

    /**
     * Sets the parent.
     *
     * @param parent the parent
     */
    public void setParent(GroupModel parent)
    {
        myParent = parent;
    }

    @Override
    protected void updateDomainModel(Criteria domainModel)
    {
        domainModel.setField(myField.get());
        domainModel.setComparisonOperator(myOperator.get());
        if (domainModel.getComparisonOperator() == Conditional.BETWEEN)
        {
            domainModel.setValue(
                    StringUtilities.concat(getCriterionValueValue(), InRangeConverter.SEPARATOR, getCriterionMaxValueValue()));
        }
        else
        {
            domainModel.setValue(getCriterionValueValue());
        }
    }

    @Override
    protected void updateViewModel(Criteria domainModel)
    {
        myField.set(domainModel.getField());
        myField.fireChangeEvent();
        myOperator.set(domainModel.getComparisonOperator());
        if (domainModel.getComparisonOperator() == Conditional.BETWEEN)
        {
            String[] tokens = domainModel.getValue().split(InRangeConverter.SPLIT_REGEX);
            myCriterionValue.set(tokens.length > 0 ? tokens[0] : null);
            myCriterionMaxValue.set(tokens.length > 1 ? tokens[1] : null);
        }
        else
        {
            myCriterionValue.set(domainModel.getValue());
        }
    }

    /**
     * Checks to see if the value models need to change from TextModels to
     * DateModels and vice versa.
     */
    private void checkModels()
    {
        if (myDataType != null)
        {
            SpecialKey specialKey = myDataType.getMetaDataInfo().getSpecialTypeForKey(myField.get());
            if (specialKey instanceof TimeKey)
            {
                if (!(myCriterionValue instanceof DateModel))
                {
                    switchValueModels(DateModel.class);
                }
            }
            else
            {
                if (myCriterionValue instanceof DateModel)
                {
                    switchValueModels(GhostTextModel.class);
                }
            }
        }
    }

    /**
     * Get the value of the model, taking the {@link Conditional#IN_LIST}
     * operator and the special type into consideration.
     *
     * @param value The value taken from the model.
     *
     * @return The converted value.
     */
    private String getConvertedValue(String value)
    {
        Conditional cond = myOperator.get();
        if (cond != Conditional.IN_LIST && cond != Conditional.NOT_IN_LIST && cond != Conditional.LIKE_LIST
                && cond != Conditional.NOT_LIKE_LIST)
        {
            return getSpecialTypeValue(value);
        }

        String[] tokens = value == null ? null : value.split(InListConverter.SPLIT_REGEX);
        if (tokens == null || tokens.length == 0)
        {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (String token : tokens)
        {
            sb.append(getSpecialTypeValue(token)).append(InListConverter.SEPARATOR);
        }
        return sb.toString();
    }

    /**
     * Get the value of the max value model, taking the special type into
     * consideration.
     *
     * @return The value.
     */
    private String getCriterionMaxValueValue()
    {
        return getConvertedValue(myCriterionMaxValue.get());
    }

    /**
     * Get the value of the value model, taking the special type into
     * consideration.
     *
     * @return The value.
     */
    private String getCriterionValueValue()
    {
        return getConvertedValue(myCriterionValue.get());
    }

    /**
     * Get the value of the model, taking the special type into consideration,
     * but not the {@link Conditional#IN_LIST} operator.
     *
     * @param value The value taken from the model.
     *
     * @return The converted value.
     */
    private String getSpecialTypeValue(String value)
    {
        if (mySpecialType instanceof LatitudeKey)
        {
            return Double.toString(LatLonAltParser.parseLat(value));
        }
        if (mySpecialType instanceof LongitudeKey)
        {
            return Double.toString(LatLonAltParser.parseLon(value));
        }
        return StringUtilities.trim(value);
    }

    /**
     * Initializes the value models.
     */
    private void initValueModels()
    {
        myCriterionValue.setName("Value");
        myCriterionMaxValue.setNameAndDescription("Maximum value", "The maximum value");

        addModel(myCriterionValue);
        addModel(myCriterionMaxValue);

        Conditional cond = myOperator.get();
        myCriterionValue.setVisible(cond != Conditional.EMPTY && cond != Conditional.NOT_EMPTY);
        myCriterionMaxValue.setVisible(cond == Conditional.BETWEEN);
    }

    /**
     * Sets the values in this object from the given model.
     *
     * @param other the other model
     */
    private void setFromModelInternal(CriterionModel other)
    {
        Criteria domainModel = new Criteria();
        other.updateDomainModel(domainModel);
        set(domainModel);
    }

    /**
     * Switches the class type of the value models.
     *
     * @param <T> The type of class supported
     * @param clazz the class
     */
    private <T extends GhostTextModel> void switchValueModels(Class<T> clazz)
    {
        String criterionValue = myCriterionValue.get();
        String criterionMaxValue = myCriterionMaxValue.get();

        removeModel(myCriterionValue);
        removeModel(myCriterionMaxValue);

        Collection<PropertyChangeListener> valueListeners = myCriterionValue.getPropertyChangeListeners();
        Collection<PropertyChangeListener> maxValueListeners = myCriterionMaxValue.getPropertyChangeListeners();
        myCriterionValue.removeAllListeners();
        myCriterionMaxValue.removeAllListeners();

        try
        {
            myCriterionValue = clazz.getDeclaredConstructor().newInstance();
            myCriterionMaxValue = clazz.getDeclaredConstructor().newInstance();
        }
        catch (ReflectiveOperationException e)
        {
            LOGGER.error(e, e);
        }

        initValueModels();

        for (PropertyChangeListener listener : valueListeners)
        {
            myCriterionValue.addPropertyChangeListener(listener);
        }
        for (PropertyChangeListener listener : maxValueListeners)
        {
            myCriterionMaxValue.addPropertyChangeListener(listener);
        }

        myCriterionValue.set(criterionValue);
        myCriterionMaxValue.set(criterionMaxValue);
    }

    /**
     * Update the current validator based on the value type and the special
     * type.
     *
     * @param valueType The type of the value in the model.
     */
    private void updateValidator(Class<?> valueType)
    {
        if (myCriterionValue instanceof DateModel)
        {
            myValidator = null;
        }
        else if (mySpecialType instanceof LongitudeKey)
        {
            LatLonAltParser.validLat("");
            myValidator = new DefaultPredicateWithMessage<>(value -> LatLonAltParser.validLon(value), null,
                    myField.get() + " must be a valid longitude.");
        }
        else if (mySpecialType instanceof LatitudeKey)
        {
            myValidator = new DefaultPredicateWithMessage<>(value -> LatLonAltParser.validLat(value), null,
                    myField.get() + " must be a valid latitude.");
        }
        else if (valueType != null && Number.class.isAssignableFrom(valueType))
        {
            myValidator = new DefaultPredicateWithMessage<>(new NumberPredicate(), null, myField.get() + " must be a number.");
        }
        else
        {
            myValidator = new DefaultPredicateWithMessage<>(new NonEmptyPredicate(), null, myField.get() + " cannot be empty.");
        }
    }

    /**
     * Update the validator support in the view models using the latest
     * validator and validator message.
     */
    private void updateViewModelValidatorSupports()
    {
        if (myValidator == null)
        {
            return;
        }
        Conditional cond = myOperator.get();
        if (cond == Conditional.IN_LIST || cond == Conditional.NOT_IN_LIST || cond == Conditional.LIKE_LIST
                || cond == Conditional.NOT_LIKE_LIST)
        {
            myCriterionValue.setValidatorSupport(new TextModelListValidatorSupport(myCriterionValue, myValidator));
            myCriterionMaxValue.setValidatorSupport(new TextModelListValidatorSupport(myCriterionMaxValue, myValidator));
        }
        else
        {
            myCriterionValue.setValidatorSupport(new ObservableValueValidatorSupport<>(myCriterionValue, myValidator));
            myCriterionMaxValue.setValidatorSupport(new ObservableValueValidatorSupport<>(myCriterionMaxValue, myValidator));
        }
    }
}

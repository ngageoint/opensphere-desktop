package io.opensphere.mantle.data.element.mdfilter.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import io.opensphere.core.datafilter.DataFilterCriteria;
import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.core.model.time.DayOfWeek;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationKey;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationRegistry;

/**
 * The Class CriteriaEvaluator.
 */
@SuppressWarnings("PMD.GodClass")
public class DataFilterCriteriaEvaluator
{
    /** The LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(DataFilterCriteriaEvaluator.class);

    /** Pattern for functions. */
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("(.+?)\\((.+?)\\)");

    /** The map of known functions. */
    private static final Map<String, Function<Object, Object>> FUNCTION_MAP = New.map();
    static
    {
        FUNCTION_MAP.put("HOUR", DataFilterCriteriaEvaluator::hour);
        FUNCTION_MAP.put("DAY_OF_WEEK", DataFilterCriteriaEvaluator::dayOfWeek);
        FUNCTION_MAP.put("DAY", DataFilterCriteriaEvaluator::day);
        FUNCTION_MAP.put("YEAR", DataFilterCriteriaEvaluator::year);
    }

    /** The Conditional. */
    private final Conditional myConditional;

    /** The dynamic enumeration registry. */
    private final DynamicEnumerationRegistry myDynamicEnumRegistry;

    /** The Evaluator. */
    private final Evaluator myEvaluator;

    /** The Field. */
    private final String myField;

    /** The Value. */
    private final String myValue;

    /** The optional name of the function to run on the value. */
    private final String myFieldFunction;

    /**
     * Instantiates a new criteria evaluator.
     *
     * @param dataFilterCriteria the data filter criteria
     * @param dynamicEnumRegistry the dynamic enumeration registry
     */
    public DataFilterCriteriaEvaluator(DataFilterCriteria dataFilterCriteria, DynamicEnumerationRegistry dynamicEnumRegistry)
    {
        Matcher matcher = FUNCTION_PATTERN.matcher(dataFilterCriteria.getField());
        if (matcher.find())
        {
            myFieldFunction = matcher.group(1);
            myField = matcher.group(2);
        }
        else
        {
            myField = dataFilterCriteria.getField();
            myFieldFunction = null;
        }

        myValue = dataFilterCriteria.getValue();
        myConditional = dataFilterCriteria.getComparisonOperator();
        myEvaluator = createEvaluator(myConditional, myValue);
        myDynamicEnumRegistry = dynamicEnumRegistry;
    }

    /**
     * Accepts.
     *
     * @param element the data element
     * @return true, if successful
     */
    public boolean accepts(DataElement element)
    {
        MetaDataProvider metaDataProvider = element.getMetaData();
        if (myEvaluator == null || !metaDataProvider.hasKey(myField))
        {
            return false;
        }
        Object value = metaDataProvider.getValue(myField);
        if (value instanceof DynamicEnumerationKey)
        {
            value = myDynamicEnumRegistry.getEnumerationValue((DynamicEnumerationKey)value);
        }
        else if (value == null && element.getDataTypeInfo().getMetaDataInfo().getSpecialTypeForKey(myField) == TimeKey.DEFAULT)
        {
            value = element.getTimeSpan();
        }
        if (myFieldFunction != null)
        {
            value = applyFunction(value);
        }
        return myEvaluator.evaluate(value);
    }

    /**
     * Gets the conditional.
     *
     * @return the conditional
     */
    public Conditional getConditional()
    {
        return myConditional;
    }

    /**
     * Gets the field.
     *
     * @return the field
     */
    public String getField()
    {
        return myField;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue()
    {
        return myValue;
    }

    /**
     * Creates an evaluator.
     *
     * @param conditional the conditional
     * @param value the value
     * @return the evaluator
     */
    private Evaluator createEvaluator(Conditional conditional, String value)
    {
        Evaluator evaluator;
        if (conditional != null)
        {
            switch (conditional)
            {
                case EQ:
                    evaluator = new EqualsEvaluator(value);
                    break;
                case BETWEEN:
                    evaluator = new BetweenEvaluator(value);
                    break;
                case GT:
                    evaluator = new GreaterThanEvaluator(value, false);
                    break;
                case GTE:
                    evaluator = new GreaterThanEvaluator(value, true);
                    break;
                case EMPTY:
                    evaluator = new IsEmptyEvaluator(value);
                    break;
                case NOT_EMPTY:
                    evaluator = new Inverse(new IsEmptyEvaluator(value));
                    break;
                case LIKE:
                    evaluator = new LikeEvaluator(value);
                    break;
                case NOT_LIKE:
                    evaluator = new Inverse(new LikeEvaluator(value));
                    break;
                case CONTAINS:
                    evaluator = new LikeEvaluator("*" + value + "*");
                    break;
                case MATCHES:
                    evaluator = new LikeEvaluator(value, true);
                    break;
                case LT:
                    evaluator = new Inverse(new GreaterThanEvaluator(value, true));
                    break;
                case LTE:
                    evaluator = new Inverse(new GreaterThanEvaluator(value, false));
                    break;
                case NEQ:
                    evaluator = new Inverse(new EqualsEvaluator(value));
                    break;
                default:
                    LOGGER.error("No Evaluator Available for comparison operator " + conditional);
                    evaluator = null;
                    break;
            }
        }
        else
        {
            evaluator = null;
        }
        return evaluator;
    }

    /**
     * Applies the function to the value.
     *
     * @param value the value
     * @return the result value
     */
    private Object applyFunction(Object value)
    {
        Object newValue = value;
        Function<Object, Object> function = FUNCTION_MAP.get(myFieldFunction);
        if (function != null)
        {
            newValue = function.apply(value);
        }
        else
        {
            LOGGER.error("Unknown function: " + myFieldFunction);
        }
        return newValue;
    }

    /**
     * Converts the value to an hour if possible.
     *
     * @param value the value
     * @return the converted value if possible, or the original value
     */
    private static Object hour(Object value)
    {
        Object newValue = value;
        Date date = AbstractEvaluator.getAsDate(value);
        if (date != null)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            newValue = String.valueOf(hour);
        }
        return newValue;
    }

    /**
     * Converts the value to a day of the week if possible.
     *
     * @param value the value
     * @return the converted value if possible, or the original value
     */
    private static Object dayOfWeek(Object value)
    {
        Object newValue = value;
        Date date = AbstractEvaluator.getAsDate(value);
        if (date != null)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            newValue = String.valueOf(DayOfWeek.getDayOfWeek(dayOfWeek));
        }
        return newValue;
    }

    /**
     * Converts the value to a specific day if possible.
     *
     * @param value the value
     * @return the converted value if possible, or the original value
     */
    private static Object day(Object value)
    {
        Object newValue = value;
        Date date = AbstractEvaluator.getAsDate(value);
        if (date != null)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            newValue = sdf.format(date);
        }
        return newValue;
    }

    /**
     * Converts the value to a year if possible.
     *
     * @param value the value
     * @return the converted value if possible, or the original value
     */
    private static Object year(Object value)
    {
        Object newValue = value;
        Date date = AbstractEvaluator.getAsDate(value);
        if (date != null)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            newValue = sdf.format(date);
        }
        return newValue;
    }
}

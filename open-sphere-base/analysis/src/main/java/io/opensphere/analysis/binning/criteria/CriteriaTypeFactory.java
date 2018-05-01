package io.opensphere.analysis.binning.criteria;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import io.opensphere.core.util.collections.New;

/**
 * Given a criteria type string, this class news up a new {@link CriteriaType}
 * class that matches the string type.
 */
public final class CriteriaTypeFactory
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(CriteriaTypeFactory.class);

    /**
     * The instance of this class.
     */
    private static final CriteriaTypeFactory ourInstance = new CriteriaTypeFactory();

    /**
     * The map of currently available types.
     */
    private final Map<String, Class<? extends CriteriaType>> myCriteriaTypes;

    /**
     * Gets the instance of this class.
     *
     * @return The instance of this class.
     */
    public static CriteriaTypeFactory getInstance()
    {
        return ourInstance;
    }

    /**
     * Not constructible.
     */
    private CriteriaTypeFactory()
    {
        myCriteriaTypes = new LinkedHashMap<>();
        myCriteriaTypes.put(UniqueCriteria.CRITERIA_TYPE, UniqueCriteria.class);
        myCriteriaTypes.put(RangeCriteria.CRITERIA_TYPE, RangeCriteria.class);
    }

    /**
     * Gets a list of available criteria types.
     *
     * @return The list of available criteria types.
     */
    public List<String> getAvailableTypes()
    {
        return New.list(myCriteriaTypes.keySet());
    }

    /**
     * Creates a new criteria type.
     *
     * @param type The criteria type to create such as Unique or Range.
     * @return The new criteria type, or null if we do not have a criteria type
     *         that matches the specified type.
     */
    public CriteriaType newCriteriaType(String type)
    {
        CriteriaType criteriaType = null;
        Class<? extends CriteriaType> typeClass = myCriteriaTypes.get(type);
        if (typeClass != null)
        {
            try
            {
                Constructor<? extends CriteriaType> constructor = typeClass.getConstructor();
                criteriaType = constructor.newInstance();
            }
            catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e)
            {
                LOGGER.error(e, e);
            }
        }

        return criteriaType;
    }
}

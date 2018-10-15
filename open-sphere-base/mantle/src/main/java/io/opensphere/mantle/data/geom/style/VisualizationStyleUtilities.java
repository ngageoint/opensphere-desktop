package io.opensphere.mantle.data.geom.style;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.VisualizationSupport;

/**
 * The Class VisualizationStyleUtilities.
 */
public final class VisualizationStyleUtilities
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(VisualizationStyleUtilities.class);

    /**
     * Gets the set of parameters in style 2 that are different than parameters
     * in style 1, only for parameters with the same keys.
     *
     * Note that if the two styles have no parameters in common ( by parameter
     * key ), the function will always return an empty set.
     *
     * @param style1 the first style
     * @param style2 the second style
     * @return the set of changed parameters, or empty set if none.
     */
    public static Set<VisualizationStyleParameter> getChangedParameters(VisualizationStyle style1, VisualizationStyle style2)
    {
        Utilities.checkNull(style1, "style1");
        Utilities.checkNull(style2, "style2");
        Set<VisualizationStyleParameter> result = New.set();

        Set<VisualizationStyleParameter> style1PSet = style1.getStyleParameterSet();
        Set<VisualizationStyleParameter> style2PSet = style2.getStyleParameterSet();
        for (VisualizationStyleParameter set2P : style2PSet)
        {
            for (VisualizationStyleParameter set1P : style1PSet)
            {
                if (Objects.equals(set2P.getKey(), set1P.getKey()) && !Objects.equals(set2P.getValue(), set1P.getValue()))
                {
                    result.add(set2P);
                }
            }
        }
        return result.isEmpty() ? Collections.<VisualizationStyleParameter>emptySet() : result;
    }

    /**
     * Gets the first mgs interface for the class or interface provided.
     *
     * @param mgsClass the mgs class
     * @return the first mgs interface
     */
    public static Class<? extends VisualizationSupport> getFirstMGSInterface(Class<? extends VisualizationSupport> mgsClass)
    {
        Class<? extends VisualizationSupport> result = null;
        Class<?>[] interfaces = mgsClass.getInterfaces();
        if (interfaces != null && interfaces.length > 0)
        {
            for (Class<?> cl : interfaces)
            {
                if (VisualizationSupport.class.isAssignableFrom(cl))
                {
                    result = cl.asSubclass(VisualizationSupport.class);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Checks for changed parameters in style 2 from style 1. If any parameters
     * with the same keys have different values the function will return true.
     *
     * Note that if the two styles have no parameters in common ( by parameter
     * key ), the function will always return false.
     *
     * @param style1 the style1
     * @param style2 the style2
     * @return true, if different false if not
     */
    public static boolean hasChangedParameters(VisualizationStyle style1, VisualizationStyle style2)
    {
        Utilities.checkNull(style1, "style1");
        Utilities.checkNull(style2, "style2");
        boolean different = false;
        Set<VisualizationStyleParameter> style1PSet = style1.getStyleParameterSet();
        Set<VisualizationStyleParameter> style2PSet = style2.getStyleParameterSet();
        for (VisualizationStyleParameter set2P : style2PSet)
        {
            for (VisualizationStyleParameter set1P : style1PSet)
            {
                if (Objects.equals(set2P.getKey(), set1P.getKey()) && !Objects.equals(set2P.getValue(), set1P.getValue()))
                {
                    different = true;
                    break;
                }
            }
            if (different)
            {
                break;
            }
        }

        return different;
    }

    /**
     * Search for style class.
     *
     * @param <T> the generic type
     * @param searchMap the search map
     * @param mgsClass the mgs class
     * @return the class
     */
    public static <T> T searchForItemByMGSClass(Map<Class<? extends VisualizationSupport>, T> searchMap,
            Class<? extends VisualizationSupport> mgsClass)
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Checking for Class: " + mgsClass.getName());
        }
        Class<? extends VisualizationSupport> mgsClosestInterface = null;
        T result = null;
        if (!mgsClass.isInterface())
        {
            mgsClosestInterface = getFirstMGSInterface(mgsClass);
        }
        if (searchMap.containsKey(mgsClass))
        {
            result = searchMap.get(mgsClass);
        }
        else if (mgsClosestInterface != null && searchMap.containsKey(mgsClosestInterface))
        {
            result = searchMap.get(mgsClosestInterface);
        }
        else
        {
            Class<?> superClass = null;
            if (mgsClass.isInterface())
            {
                superClass = getFirstMGSInterface(mgsClass);
            }
            else
            {
                superClass = mgsClass.getSuperclass();
            }

            if (superClass != null)
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Parent Class: " + superClass.getName());
                }
                if (VisualizationSupport.class.isAssignableFrom(superClass))
                {
                    if (LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace("Parent Class is still VisualizationSupport Checking for Style");
                    }
                    result = searchForItemByMGSClass(searchMap, superClass.asSubclass(VisualizationSupport.class));
                }
            }
        }
        return result;
    }

    /**
     * Instantiates a new visualization style utilities.
     */
    private VisualizationStyleUtilities()
    {
        // No instantiation.
    }
}

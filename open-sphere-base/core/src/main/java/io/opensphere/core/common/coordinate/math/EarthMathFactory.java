package io.opensphere.core.common.coordinate.math;

import io.opensphere.core.common.coordinate.math.strategy.Haversine;
import io.opensphere.core.common.coordinate.math.strategy.Vincenty;

public class EarthMathFactory
{

    public static String HAVERSINE_SPHERE = Haversine.class.getName();

    public static String VINCENTY_ELLIPSOID = Vincenty.class.getName();

    public static EarthMath getInstance(String className)
    {
        Class<?> clazz = null;
        try
        {
            clazz = Class.forName(className);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        try
        {
            return (EarthMath)clazz.getDeclaredConstructor().newInstance();
        }
        catch (ReflectiveOperationException e)
        {
            e.printStackTrace();
        }
        return null;

    }

}

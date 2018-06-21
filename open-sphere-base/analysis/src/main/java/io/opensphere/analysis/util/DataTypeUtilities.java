package io.opensphere.analysis.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.jcip.annotations.GuardedBy;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.DateTimeFormats;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationKey;

/** Data type utilities. */
public final class DataTypeUtilities
{
//    /** The decimal format. */
//    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.########");

    /** The date time format. */
    @GuardedBy("DATE_TIME_FORMAT")
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat(DateTimeFormats.DATE_TIME_FORMAT);

    /**
     * Determines if the object is numeric.
     *
     * @param o the object
     * @return whether it's numeric
     */
    public static boolean isNumeric(Object o)
    {
        return isNumeric(o.getClass());
    }

    /**
     * Determines if the class is numeric.
     *
     * @param c the class
     * @return whether it's numeric
     */
    public static boolean isNumeric(Class<?> c)
    {
        return Number.class.isAssignableFrom(c) || Date.class.isAssignableFrom(c) || TimeSpan.class.isAssignableFrom(c);
    }

    /**
     * Converts the object to a double, or throws an exception.
     *
     * @param o the object
     * @return the value
     */
    public static double toDouble(Object o)
    {
        double value;
        if (o instanceof Number)
        {
            value = ((Number)o).doubleValue();
        }
        else if (o instanceof Date)
        {
            value = ((Date)o).getTime();
        }
        else if (o instanceof TimeSpan)
        {
            value = ((TimeSpan)o).getStart();
        }
        else if (o instanceof String)
        {
            value = Double.parseDouble((String)o);
        }
        else
        {
            throw new IllegalArgumentException(o + " can't be converted to a double");
        }
        return value;
    }

    /**
     * Converts the double value to an object, or throws an exception.
     *
     * @param value the value
     * @param theClass the class of the object to return
     * @return the object
     */
    public static Object fromDouble(double value, Class<?> theClass)
    {
        Object o;
        if (Number.class.isAssignableFrom(theClass))
        {
            o = Double.valueOf(value);
        }
        else if (Date.class.isAssignableFrom(theClass))
        {
            o = new Date((long)value);
        }
        else if (TimeSpan.class.isAssignableFrom(theClass))
        {
            o = TimeSpan.get((long)value);
        }
        else if (String.class.isAssignableFrom(theClass))
        {
            o = String.valueOf(value);
        }
        else
        {
            throw new IllegalArgumentException(value + " can't be converted to an object");
        }
        return o;
    }

    /**
     * Compares the two objects.
     *
     * @param o1 the first object
     * @param o2 the second object
     * @return the comparison result
     */
    public static int compareTo(Object o1, Object o2)
    {
        int result = 0;
        if (o1 == null)
        {
            result = 1;
        }
        else if (o2 == null)
        {
            result = -1;
        }
        else if (o1 instanceof Double)
        {
            result = ((Double)o1).compareTo((Double)o2);
        }
        else if (o1 instanceof Long)
        {
            result = ((Long)o1).compareTo((Long)o2);
        }
        else if (o1 instanceof Integer)
        {
            result = ((Integer)o1).compareTo((Integer)o2);
        }
        else if (o1 instanceof String)
        {
            result = ((String)o1).compareToIgnoreCase((String)o2);
        }
        else if (o1 instanceof DynamicEnumerationKey)
        {
            result = Short.compare(((DynamicEnumerationKey)o1).getValueId(), ((DynamicEnumerationKey)o2).getValueId());
        }
        else if (o1 instanceof Geometry)
        {
            result = ((Geometry)o1).compareTo(o2);
        }
        else if (o1 instanceof Date)
        {
            result = ((Date)o1).compareTo((Date)o2);
        }
        else if (o1 instanceof TimeSpan)
        {
            result = ((TimeSpan)o1).compareTo((TimeSpan)o2);
        }
        return result;
    }

    /**
     * Generates a label for the object.
     *
     * @param o the object
     * @return the label
     */
    public static String getLabel(Object o)
    {
        String label = "NONE";
        if (o != null)
        {
            if (o instanceof Number)
            {
                label = o.toString();
            }
            else if (o instanceof Date)
            {
                synchronized (DATE_TIME_FORMAT)
                {
                    label = DATE_TIME_FORMAT.format((Date)o);
                }
            }
            else if (o instanceof TimeSpan)
            {
                label = ((TimeSpan)o).toSmartString();
            }
            else
            {
                label = o.toString();
            }
        }
        return label;
    }

    /** Disallow instantiation. */
    private DataTypeUtilities()
    {
    }
}

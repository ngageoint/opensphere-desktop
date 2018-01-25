package io.opensphere.mantle.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 * The Class CustomNumericComparator.
 */
public class CustomNumericComparator implements Comparator<Object>, Serializable
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Object o1, Object o2)
    {
        if (o1 != null && o2 != null)
        {
            boolean o1Number = o1 instanceof Number;
            boolean o2Number = o2 instanceof Number;

            if (o1Number && o2Number)
            {
                return Double.compare(((Number)o1).doubleValue(), ((Number)o2).doubleValue());
            }
            String s1 = o1.toString();
            String s2 = o2.toString();

            if (s1.isEmpty() && s2.isEmpty())
            {
                return 0;
            }
            else if (s1.isEmpty() && !s2.isEmpty())
            {
                return -1;
            }
            else if (!s1.isEmpty() && s2.isEmpty())
            {
                return 1;
            }
            else
            {
                try
                {
                    double d1 = o1Number ? ((Number)o1).doubleValue() : Double.parseDouble(s1);
                    double d2 = o2Number ? ((Number)o2).doubleValue() : Double.parseDouble(s2);
                    return Double.compare(d1, d2);
                }
                catch (NumberFormatException e)
                {
                    return o1.toString().compareTo(s2);
                }
            }
        }
        return 0;
    }
}

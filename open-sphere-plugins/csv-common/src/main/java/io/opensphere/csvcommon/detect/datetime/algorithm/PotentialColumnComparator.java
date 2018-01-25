package io.opensphere.csvcommon.detect.datetime.algorithm;

import java.io.Serializable;
import java.util.Comparator;

import io.opensphere.csvcommon.detect.datetime.model.PotentialColumn;
import io.opensphere.csvcommon.detect.datetime.model.SuccessfulFormat;
import io.opensphere.csvcommon.detect.datetime.util.PotentialColumnUtils;

/**
 * Sorts a list of PotentialColumns by the most successful column to the least
 * successful.
 *
 */
public class PotentialColumnComparator implements Comparator<PotentialColumn>, Serializable
{
    /**
     * Default serialization id.
     */
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(PotentialColumn o1, PotentialColumn o2)
    {
        int o1MaxSuccesses = getMaxSuccesses(o1);
        int o2MaxSuccesses = getMaxSuccesses(o2);

        int compareValue = 0;

        if (o1MaxSuccesses > o2MaxSuccesses)
        {
            compareValue = -1;
        }
        else if (o2MaxSuccesses > o1MaxSuccesses)
        {
            compareValue = 1;
        }

        return compareValue;
    }

    /**
     * Gets the max number of successes for the given potential column.
     *
     * @param column The potential column.
     * @return The max number of successes of a given format.
     */
    private int getMaxSuccesses(PotentialColumn column)
    {
        SuccessfulFormat format = PotentialColumnUtils.getMostSuccessfulFormat(column);
        return format.getNumberOfSuccesses();
    }
}

package io.opensphere.csvcommon.detect.datetime.algorithm;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.detect.datetime.algorithm.PotentialColumnComparator;
import io.opensphere.csvcommon.detect.datetime.model.PotentialColumn;
import io.opensphere.csvcommon.detect.datetime.model.SuccessfulFormat;

/**
 * Tests the PotentialColumnComparator.
 *
 *
 */
public class PotentialColumnComparatorTest
{
    /**
     * Tests the PotentialColumnComparator.
     */
    @Test
    public void testCompare()
    {
        List<PotentialColumn> testData = New.list();

        for (int i = 0; i < 3; i++)
        {
            PotentialColumn column = new PotentialColumn();
            column.setColumnIndex(i);
            for (int j = 0; j < 3; j++)
            {
                SuccessfulFormat format = new SuccessfulFormat();
                format.setNumberOfSuccesses(j * i);

                column.getFormats().put("format" + j, format);
            }

            testData.add(column);
        }

        Collections.sort(testData, new PotentialColumnComparator());

        int index = testData.size() - 1;
        for (PotentialColumn column : testData)
        {
            assertEquals(index, column.getColumnIndex());
            index--;
        }
    }
}

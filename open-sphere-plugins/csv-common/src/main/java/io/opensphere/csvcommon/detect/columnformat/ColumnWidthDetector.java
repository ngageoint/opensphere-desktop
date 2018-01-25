package io.opensphere.csvcommon.detect.columnformat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import io.opensphere.csvcommon.common.LineSampler;
import io.opensphere.csvcommon.detect.LineDetector;
import io.opensphere.csvcommon.detect.ValueWithConfidence;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.columnformat.FixedWidthColumnFormatParameters;

/**
 * Module that identifies the column widths.
 */
public class ColumnWidthDetector implements LineDetector<FixedWidthColumnFormatParameters>
{
    /**
     * Given a multi-line sample of some fixed field data this function will
     * attempt to guess at where the column breaks are located.
     *
     * It does this by detecting either white space breaks or tab breaks and
     * histograming them by end index. And then thresholding by the provided
     * percentage where only occurrences with the number of detection equals or
     * exceeds the threshold are considered "Passing"
     *
     * This will work better with more data.
     *
     * @param sampler the sampler
     * @param thresholdPercentage - the threshold.
     * @return int[] if determined, null if none passed threshold
     */
    private static int[] guessColumnBreaks(LineSampler sampler, double thresholdPercentage)
    {
        // Get the index count map
        TIntIntHashMap indexCountMap = new TIntIntHashMap();
        Pattern p = Pattern.compile("\\s+|\t+");
        for (String row : sampler.getBeginningSampleLines())
        {
            Matcher m = p.matcher(row);
            while (m.find())
            {
                int end = m.end();
                if (end != row.length())
                {
                    if (!indexCountMap.containsKey(end))
                    {
                        indexCountMap.put(end, 0);
                    }
                    indexCountMap.increment(end);
                }
            }
        }

        // Figure out what passed
        TIntArrayList passIndexes = new TIntArrayList();
        for (int key : indexCountMap.keys())
        {
            double fraction = (double)indexCountMap.get(key) / sampler.getBeginningSampleLines().size();
            if (fraction >= thresholdPercentage)
            {
                passIndexes.add(key);
            }
        }

        if (!passIndexes.isEmpty())
        {
            passIndexes.sort();
        }

        return passIndexes.toArray();
    }

    @Override
    public ValuesWithConfidence<FixedWidthColumnFormatParameters> detect(LineSampler sampler)
    {
        ValueWithConfidence<FixedWidthColumnFormatParameters> widthsWithConfidence = new ValueWithConfidence<>();

        for (int thresholdPercentage = 100; thresholdPercentage > 0; thresholdPercentage -= 100)
        {
            float thresholdFraction = thresholdPercentage / (float)100;
            int[] columnBreaks = guessColumnBreaks(sampler, thresholdFraction);
            if (columnBreaks.length > 0)
            {
                widthsWithConfidence.setValue(new FixedWidthColumnFormatParameters(columnBreaks));
                widthsWithConfidence.setConfidence(0.5f);
                break;
            }
        }

        if (widthsWithConfidence.getValue() == null)
        {
            widthsWithConfidence.setValue(new FixedWidthColumnFormatParameters(new int[0]));
        }

        return new ValuesWithConfidence<>(widthsWithConfidence);
    }
}

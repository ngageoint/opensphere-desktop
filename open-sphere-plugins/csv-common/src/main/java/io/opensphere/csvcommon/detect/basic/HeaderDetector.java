package io.opensphere.csvcommon.detect.basic;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.detect.CellDetector;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.location.LocationMatchMakerDetector;
import io.opensphere.csvcommon.detect.location.model.LocationResults;

/**
 * A detector that tries to pick the header from some sample lines.
 */
public class HeaderDetector implements CellDetector<Integer>
{
    /** Regex pattern for CSV headers. */
    private static final Pattern HEADER_PATTERN = Pattern.compile("[\"'\\w\\s\\(\\)]+");

    /** The expected number of columns. */
    private final int myColumnCount;

    /**
     * The expected number of columns.
     *
     * @param columnCount The column count.
     */
    public HeaderDetector(int columnCount)
    {
        myColumnCount = columnCount;
    }

    @Override
    public ValuesWithConfidence<Integer> detect(CellSampler sampler)
    {
        ValuesWithConfidence<Integer> returnValue;
        if (myColumnCount > 1)
        {
            returnValue = detectFromMultipleColumns(sampler);
        }
        else
        {
            returnValue = detectFromSingleColumn(sampler);
        }
        return returnValue;
    }

    /**
     * Detect the parameter.
     *
     * @param pSampler the line sampler
     * @return the parameter
     */
    protected ValuesWithConfidence<Integer> detectFromSingleColumn(CellSampler pSampler)
    {
        List<? extends List<? extends String>> sampleCells = pSampler.getBeginningSampleCells();
        float bestConfidence = 0f;
        Integer headerLineIndexGuess = null;

        // only look at the first row in this case:
        List<? extends String> cells = sampleCells.get(0);

        // As there is only one column, extract it and examine it:
        String cell = cells.get(0);

        // check that the value contains only characters permitted in headers.
        // If not, it can't be a header row, so skip the
        // more expensive checks:
        if (HEADER_PATTERN.matcher(cell).matches())
        {
            // the value could be a header. Now start with more expensive tests,
            // to determine if the format actually matches a
            // data format. If it matches with 100% confidence, then assess that
            // the value is not a header:
            List<CellDetector<LocationResults>> detectors = getCellDetectors();
            for (CellDetector<LocationResults> cellDetector : detectors)
            {
                ValuesWithConfidence<LocationResults> result = cellDetector.detect(pSampler);
                if (result.getBestConfidence() == 1.0f)
                {
                    bestConfidence = Math.max(bestConfidence, 0.5f);
                }
            }
        }

        return new ValuesWithConfidence<>(headerLineIndexGuess, Math.max(0f, bestConfidence));
    }

    /**
     * @return list of cell detectors
     */
    protected List<CellDetector<LocationResults>> getCellDetectors()
    {
        List<CellDetector<LocationResults>> returnValue = New.list();
        LocationMatchMakerDetector lmmd = new LocationMatchMakerDetector();
        returnValue.add(lmmd);

        return returnValue;
    }

    /**
     * Detects parameters from multiple columns.
     *
     * @param sampler the sampler
     * @return the parameters
     */
    protected ValuesWithConfidence<Integer> detectFromMultipleColumns(CellSampler sampler)
    {
        Integer headerLineIndexGuess = null;
        float bestConfidence = 0f;
        int bestDupeCells = 0;

        List<? extends List<? extends String>> sampleCells = sampler.getBeginningSampleCells();
        for (int lineIndex = 0; lineIndex < sampleCells.size(); ++lineIndex)
        {
            List<? extends String> cells = sampleCells.get(lineIndex);
            List<? extends String> nextCells = null;

            if (lineIndex + 1 < sampleCells.size())
            {
                nextCells = sampleCells.get(lineIndex + 1);
            }

            if (cells.size() <= myColumnCount)
            {
                int goodCells = 0;
                int dupeCells = 0;
                int columnIndex = 0;
                for (String cell : cells)
                {
                    if (HEADER_PATTERN.matcher(cell).matches())
                    {
                        goodCells++;
                    }

                    if (nextCells != null && columnIndex < nextCells.size()
                            && Objects.equals(cell, nextCells.get(columnIndex)))
                    {
                        dupeCells++;
                    }

                    columnIndex++;
                }
                float confidence = (float)goodCells / myColumnCount;
                if (confidence > bestConfidence || headerLineIndexGuess == null)
                {
                    headerLineIndexGuess = Integer.valueOf(lineIndex);
                    bestConfidence = confidence;
                    bestDupeCells = dupeCells;

                    if (confidence > .5f)
                    {
                        break;
                    }
                }
            }
        }

        float confidence = (float)(myColumnCount - bestDupeCells) / myColumnCount;
        if (confidence < .5f)
        {
            headerLineIndexGuess = null;
            bestConfidence = 0f;
        }

        return new ValuesWithConfidence<>(headerLineIndexGuess, Math.max(0f, bestConfidence));
    }
}

package io.opensphere.csvcommon.detect.datetime.algorithm.deciders;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.csvcommon.common.datetime.DateColumn;
import io.opensphere.csvcommon.detect.datetime.algorithm.deciders.Decider;
import io.opensphere.csvcommon.detect.datetime.model.PotentialColumn;
import io.opensphere.csvcommon.detect.datetime.model.SuccessfulFormat;
import io.opensphere.csvcommon.detect.datetime.util.Constants;
import io.opensphere.csvcommon.detect.datetime.util.PotentialColumnUtils;

/**
 * Abstract class that contains all common functionality for a decider that only
 * has to look at one column.
 *
 */
public abstract class SingleValueDecider implements Decider
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(SingleValueDecider.class);

    /**
     * The threshold to use to reject potentially formatted dates in the future.
     */
    private static final long ourFiftyYearsInMilliseconds = 1576800000000L;

    /**
     * The set custom confidence to use instead of the system default.
     */
    private int myCustomConfidence = -1;

    @Override
    public List<Pair<PotentialColumn, Integer>> calculateConfidence(List<PotentialColumn> potentials,
            List<? extends List<? extends String>> sampleData)
    {
        List<Pair<PotentialColumn, Integer>> scores = New.list();

        for (PotentialColumn potential : potentials)
        {
            int rowIndex = 0;
            int successCount = 0;

            List<SuccessfulFormat> formats = PotentialColumnUtils.getMostSuccessfulFormats(potential, getTimeType());

            for (SuccessfulFormat format : formats)
            {
                int numberOfNonEmptyCells = 0;
                for (List<? extends String> row : sampleData)
                {
                    String cellValue = row.get(potential.getColumnIndex());

                    if (format != null && StringUtils.isNotEmpty(cellValue))
                    {
                        numberOfNonEmptyCells++;
                        try
                        {
                            boolean canProceed = true;
                            if ((getTimeType() == Type.DATE || getTimeType() == Type.TIMESTAMP)
                                    && !format.getFormat().getSdf().contains("y"))
                            {
                                canProceed = false;
                            }

                            if (canProceed)
                            {
                                Date date = format.getFormat().getFormat().parse(cellValue);

                                if (date.getTime() - System.currentTimeMillis() < ourFiftyYearsInMilliseconds)
                                {
                                    successCount++;
                                }
                            }
                        }
                        catch (ParseException e)
                        {
                            if (LOGGER.isDebugEnabled())
                            {
                                LOGGER.debug("Row Index : " + rowIndex + " Column Index : " + potential.getColumnIndex()
                                        + " Format : " + format.getFormat().getSdf() + " Cell Value : " + cellValue, e);
                            }
                        }
                    }

                    rowIndex++;
                }

                int score = (int)(successCount / (float)numberOfNonEmptyCells * Constants.PERCENT);
                if (score > getPassingScore())
                {
                    potential.setMostSuccessfulFormat(format);
                    Pair<PotentialColumn, Integer> pair = new Pair<>(potential, score);
                    scores.add(pair);
                    break;
                }
            }
        }

        return scores;
    }

    @Override
    public List<Pair<DateColumn, Integer>> compileResults(List<Pair<PotentialColumn, Integer>> potentials)
    {
        Integer maxScore = -1;
        PotentialColumn bestColumn = null;

        for (Pair<PotentialColumn, Integer> pair : potentials)
        {
            if (pair.getSecondObject().compareTo(maxScore) > 0)
            {
                maxScore = pair.getSecondObject();
                bestColumn = pair.getFirstObject();
            }
        }

        Pair<DateColumn, Integer> result = null;

        if (bestColumn != null)
        {
            DateColumn column = new DateColumn();
            column.setDateColumnType(getTimeType());
            column.setPrimaryColumnIndex(bestColumn.getColumnIndex());

            SuccessfulFormat format = bestColumn.getMostSuccessfulFormat();
            column.setPrimaryColumnFormat(format.getFormat().getSdf());

            result = new Pair<>(column, maxScore);
        }

        List<Pair<DateColumn, Integer>> returnList = New.list();
        returnList.add(result);

        return returnList;
    }

    @Override
    public void setConfidence(int passingScore)
    {
        myCustomConfidence = passingScore;
    }

    /**
     * The date format type this decider is expecting to look at.
     *
     * @return The date/time format type.
     */
    protected abstract Type getTimeType();

    /**
     * Gets the score that determines if a potential column passes.
     *
     * @return The passing score.
     */
    private int getPassingScore()
    {
        int passingScore = Constants.THRESHOLD_SCORE;

        if (myCustomConfidence > 0)
        {
            passingScore = myCustomConfidence;
        }

        return passingScore;
    }
}

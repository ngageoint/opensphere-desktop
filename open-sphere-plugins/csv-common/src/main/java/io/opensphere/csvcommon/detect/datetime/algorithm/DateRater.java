package io.opensphere.csvcommon.detect.datetime.algorithm;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.csvcommon.common.datetime.DateColumn;
import io.opensphere.csvcommon.common.datetime.DateColumnResults;
import io.opensphere.csvcommon.detect.ValueWithConfidence;
import io.opensphere.csvcommon.detect.datetime.algorithm.deciders.Decider;
import io.opensphere.csvcommon.detect.datetime.algorithm.deciders.DeciderFactory;
import io.opensphere.csvcommon.detect.datetime.model.PotentialColumn;
import io.opensphere.csvcommon.detect.datetime.util.Constants;
import io.opensphere.csvcommon.detect.datetime.util.DateColumnValueProvider;

/**
 * Rates all of the potential date columns and their formats and returns the
 * date/time columns and the formats to use. The results will also contain a
 * confidence from 0 - 100 where 100 is very sure and 0 is unsure.
 *
 */
public class DateRater
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(DateRater.class);

    /**
     * The passing score for figuring out end time.
     */
    private static final int ourEndTimeScore = 25;

    /**
     * True if the date rater is just going through the data to detect formats.
     */
    private boolean myIsJustDetectFormats;

    /**
     * Rates all of the potential date columns and their formats and returns the
     * date/time columns and the formats to use. The results will also contain a
     * confidence from 0 - 100 where 100 is very sure and 0 is unsure.
     *
     * @param mappedPotentials The potential date columns.
     * @param sampleData A sample of the csv data.
     * @return The date columns and the confidence related to this decision.
     */
    public ValueWithConfidence<DateColumnResults> rateAndPick(Map<Integer, PotentialColumn> mappedPotentials,
            List<? extends List<? extends String>> sampleData)
    {
        List<PotentialColumn> potentials = New.list(mappedPotentials.values());

        Collections.sort(potentials, new PotentialColumnComparator());

        List<Decider> deciders = DeciderFactory.getInstance().buildDeciders();

        DateColumn firstDate = null;
        int firstDateScore = 0;

        DateColumn secondDate = null;
        int secondDateScore = 0;

        for (Decider decider : deciders)
        {
            boolean stillDeciding = true;

            if (myIsJustDetectFormats)
            {
                decider.setConfidence(1);
            }

            while (!potentials.isEmpty() && stillDeciding)
            {
                if (firstDate != null)
                {
                    decider.setConfidence(ourEndTimeScore);
                }

                List<Pair<PotentialColumn, Integer>> scores = decider.calculateConfidence(potentials, sampleData);

                if (!scores.isEmpty())
                {
                    List<Pair<DateColumn, Integer>> results = decider.compileResults(scores);

                    for (Pair<DateColumn, Integer> result : results)
                    {
                        DateColumn dateColumn = result.getFirstObject();
                        int secondObject = result.getSecondObject().intValue();

                        removePotentials(potentials, dateColumn);

                        if (firstDate == null)
                        {
                            firstDate = dateColumn;
                            firstDateScore = secondObject;
                        }
                        else if (secondDate == null)
                        {
                            secondDate = dateColumn;
                            secondDateScore = secondObject;
                        }
                        else if (firstDateScore <= result.getSecondObject().intValue()
                                && firstDate.getPrimaryColumnIndex() > dateColumn.getPrimaryColumnIndex()
                                && firstDate.getDateColumnType() == dateColumn.getDateColumnType())
                        {
                            firstDate = dateColumn;
                            firstDateScore = secondObject;
                        }
                        else if (secondDateScore <= result.getSecondObject().intValue()
                                && secondDate.getPrimaryColumnIndex() > dateColumn.getPrimaryColumnIndex()
                                && secondDate.getDateColumnType() == dateColumn.getDateColumnType())
                        {
                            secondDate = dateColumn;
                            secondDateScore = secondObject;
                        }
                        else
                        {
                            break;
                        }
                    }
                }
                else
                {
                    stillDeciding = false;
                }
            }

            if (potentials.isEmpty() || firstDate != null && secondDate != null)
            {
                break;
            }
        }

        return compileResults(firstDate, firstDateScore, secondDate, secondDateScore, sampleData);
    }

    /**
     * Sets whether or not this date rater should just go through the steps to
     * detect formats, or if it should actually pick date columns.
     *
     * @param isJustFormats True if it should ignore some of the detection rules
     *            in order to find formats.
     */
    public void setIsJustDetectFormats(boolean isJustFormats)
    {
        myIsJustDetectFormats = isJustFormats;
    }

    /**
     * Compiles the results and decides which column is the first date column
     * and second date column if applicable.
     *
     * @param firstDate The first date column.
     * @param firstDateScore The confidence score of the first date column.
     * @param secondDate The second date column or null if there isn't one.
     * @param secondDateScore The confidence score of the second date column.
     * @param sampleData The sample data.
     * @return The compiled result with the overall score.
     */
    private ValueWithConfidence<DateColumnResults> compileResults(DateColumn firstDate, int firstDateScore, DateColumn secondDate,
            int secondDateScore, List<? extends List<? extends String>> sampleData)
    {
        DateColumnResults results = new DateColumnResults();
        results.setUpTimeColumn(firstDate);

        int overallScore = firstDateScore;

        if (secondDate != null && secondDate.getDateColumnType() != Type.TIME)
        {
            overallScore = checkDateOrder(results, firstDate, firstDateScore, secondDate, secondDateScore, sampleData);
        }
        else if (secondDate != null && secondDate.getDateColumnType() == Type.TIME
                && secondDate.getPrimaryColumnIndex() > firstDate.getPrimaryColumnIndex())
        {
            results.setDownTimeColumn(secondDate);
            overallScore += secondDateScore;
            overallScore /= 2;
        }

        // We couldn't find any dates so return nothing.
        if (results.getUpTimeColumn() != null && results.getUpTimeColumn().getDateColumnType() == Type.TIME
                && !myIsJustDetectFormats)
        {
            results.setDownTimeColumn(null);
            results.setUpTimeColumn(null);
            overallScore = 0;
        }

        return new ValueWithConfidence<DateColumnResults>(results, calculateScore(overallScore));
    }

    /**
     * Checks the first and second date order.
     *
     * @param results The results to populate.
     * @param firstDate The first date column.
     * @param firstDateScore The confidence score of the first date column.
     * @param secondDate The second date column or null if there isn't one.
     * @param secondDateScore The confidence score of the second date column.
     * @param sampleData The sample data.
     * @return The overall score.
     */
    private int checkDateOrder(DateColumnResults results, DateColumn firstDate, int firstDateScore, DateColumn secondDate,
            int secondDateScore, List<? extends List<? extends String>> sampleData)
    {
        int overallScore = firstDateScore;

        results.setDownTimeColumn(secondDate);
        overallScore += secondDateScore;
        overallScore /= 2;

        DateColumnValueProvider valueProvider = new DateColumnValueProvider();
        int reverseOrderScore = 0;
        int orderScore = 0;
        // Now determine which column is the up time column and which is the
        // down time column.
        for (List<? extends String> row : sampleData)
        {
            Date firstDateValue = new Date(Long.MAX_VALUE);

            try
            {
                firstDateValue = valueProvider.getDate(row, firstDate);
            }
            catch (ParseException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(e.getMessage(), e);
                }
            }

            Date secondDateValue = null;

            try
            {
                secondDateValue = valueProvider.getDate(row, secondDate);
            }
            catch (ParseException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(e.getMessage(), e);
                }
            }

            if (secondDateValue != null && firstDateValue.compareTo(secondDateValue) <= 0)
            {
                orderScore++;
            }
            else if (secondDateValue != null)
            {
                reverseOrderScore++;
            }
        }

        boolean addOrderScore = true;
        // We guessed the order wrong, second date column is actually up
        // time column.

        if (reverseOrderScore != 0 && reverseOrderScore > sampleData.size() / 2)
        {
            results.setUpTimeColumn(secondDate);
            results.setDownTimeColumn(firstDate);

            orderScore = sampleData.size() - orderScore;
        }
        else if (orderScore == 0)
        {
            results.setDownTimeColumn(null);
            addOrderScore = false;
        }

        if (addOrderScore)
        {
            // Now add the order score to the overall score.
            overallScore += orderScore;
            overallScore /= 2;
        }

        return overallScore;
    }

    /**
     * Calculates the score to a confidence between 0 - 1.
     *
     * @param overallScore The overall score between 0 - 100;
     * @return The score.
     */
    private float calculateScore(int overallScore)
    {
        float score = overallScore / Constants.PERCENT;
        if (score > 1.0f)
        {
            score = 1f;
        }
        else if (score < 0f)
        {
            score = 0f;
        }

        return score;
    }

    /**
     * Removes the potential columns that have been put in the DateColumn.
     *
     * @param potentials The potential columns.
     * @param dateColumn The data column.
     */
    private void removePotentials(List<PotentialColumn> potentials, DateColumn dateColumn)
    {
        List<PotentialColumn> potentialsToRemove = New.list();
        for (PotentialColumn potential : potentials)
        {
            if (dateColumn.getPrimaryColumnIndex() == potential.getColumnIndex()
                    || dateColumn.getSecondaryColumnIndex() == potential.getColumnIndex())
            {
                potentialsToRemove.add(potential);
            }
        }

        potentials.removeAll(potentialsToRemove);
    }
}

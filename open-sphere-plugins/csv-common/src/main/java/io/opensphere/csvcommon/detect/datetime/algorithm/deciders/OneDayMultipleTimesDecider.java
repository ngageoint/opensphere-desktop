package io.opensphere.csvcommon.detect.datetime.algorithm.deciders;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.csvcommon.common.datetime.DateColumn;
import io.opensphere.csvcommon.detect.datetime.algorithm.deciders.Decider;
import io.opensphere.csvcommon.detect.datetime.model.PotentialColumn;
import io.opensphere.csvcommon.detect.datetime.util.DateColumnValueProvider;

/**
 * This decider finds the date/time pattern where one column is a day, and there
 * are two other columns representing the up time in the day and the down time
 * in the day.
 */
public class OneDayMultipleTimesDecider implements Decider
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(OneDayMultipleTimesDecider.class);

    /**
     * Finds potential columns that score well for being a date.
     */
    private final Decider myDateDecider = new DateDecider();

    /**
     * Finds potential columns that score well for being time data.
     */
    private final Decider myTimeDecider = new TimeDecider();

    @Override
    public List<Pair<PotentialColumn, Integer>> calculateConfidence(List<PotentialColumn> potentials,
            List<? extends List<? extends String>> sampleData)
    {
        List<Pair<PotentialColumn, Integer>> results = New.list();

        List<Pair<PotentialColumn, Integer>> dateScores = myDateDecider.calculateConfidence(potentials, sampleData);

        // There must only be one date for this decider.
        if (dateScores.size() == 1)
        {
            int dateIndex = dateScores.get(0).getFirstObject().getColumnIndex();

            List<PotentialColumn> potentialsWithoutDate = New.list();
            for (PotentialColumn potential : potentials)
            {
                if (potential.getColumnIndex() != dateIndex)
                {
                    potentialsWithoutDate.add(potential);
                }
            }

            List<Pair<PotentialColumn, Integer>> timePotentials = myTimeDecider.calculateConfidence(potentialsWithoutDate,
                    sampleData);

            if (timePotentials.size() >= 2)
            {
                results.addAll(dateScores);

                List<Pair<DateColumn, Integer>> aTimeColumn = myTimeDecider.compileResults(timePotentials);

                DateColumn firstTimeColumn = aTimeColumn.get(0).getFirstObject();

                movePotentialToResults(firstTimeColumn, timePotentials, results);

                findSecondTimeColumn(firstTimeColumn, timePotentials, sampleData, results);
            }
        }

        return results;
    }

    @Override
    public List<Pair<DateColumn, Integer>> compileResults(List<Pair<PotentialColumn, Integer>> potentials)
    {
        List<Pair<DateColumn, Integer>> results = New.list();

        if (potentials.size() == 3)
        {
            List<Pair<DateColumn, Integer>> dateColumns = myDateDecider.compileResults(potentials);

            if (dateColumns.size() == 1)
            {
                Pair<DateColumn, Integer> dateColumn = dateColumns.get(0);
                movePotentialToResults(dateColumn.getFirstObject(), potentials, null);

                List<Pair<DateColumn, Integer>> firstTimes = myTimeDecider.compileResults(potentials);
                if (firstTimes.size() == 1)
                {
                    Pair<DateColumn, Integer> firstTime = firstTimes.get(0);
                    movePotentialToResults(firstTime.getFirstObject(), potentials, null);

                    List<Pair<DateColumn, Integer>> secondTimes = myTimeDecider.compileResults(potentials);
                    if (secondTimes.size() == 1)
                    {
                        Pair<DateColumn, Integer> secondTime = secondTimes.get(0);

                        DateColumn firstDate = new DateColumn();
                        firstDate.setDateColumnType(Type.TIMESTAMP);
                        firstDate.setPrimaryColumnFormat(dateColumn.getFirstObject().getPrimaryColumnFormat());
                        firstDate.setPrimaryColumnIndex(dateColumn.getFirstObject().getPrimaryColumnIndex());
                        firstDate.setSecondaryColumnFormat(firstTime.getFirstObject().getPrimaryColumnFormat());
                        firstDate.setSecondaryColumnIndex(firstTime.getFirstObject().getPrimaryColumnIndex());

                        DateColumn secondDate = new DateColumn();
                        secondDate.setDateColumnType(Type.TIMESTAMP);
                        secondDate.setPrimaryColumnFormat(dateColumn.getFirstObject().getPrimaryColumnFormat());
                        secondDate.setPrimaryColumnIndex(dateColumn.getFirstObject().getPrimaryColumnIndex());
                        secondDate.setSecondaryColumnFormat(secondTime.getFirstObject().getPrimaryColumnFormat());
                        secondDate.setSecondaryColumnIndex(secondTime.getFirstObject().getPrimaryColumnIndex());

                        int firstDateScore = (dateColumn.getSecondObject() + firstTime.getSecondObject()) / 2;
                        int secondDateScore = (dateColumn.getSecondObject() + secondTime.getSecondObject()) / 2;

                        results.add(new Pair<DateColumn, Integer>(firstDate, firstDateScore));
                        results.add(new Pair<DateColumn, Integer>(secondDate, secondDateScore));
                    }
                }
            }
        }

        return results;
    }

    @Override
    public void setConfidence(int passingScore)
    {
        myDateDecider.setConfidence(passingScore);
        myTimeDecider.setConfidence(passingScore);
    }

    /**
     * Finds the second time column.
     *
     * @param firstTimeColumn The already picked first time column.
     * @param timePotentials The list of other potential time columns.
     * @param sampleData The data to sample.
     * @param results The results to add the second time column to.
     */
    private void findSecondTimeColumn(DateColumn firstTimeColumn, List<Pair<PotentialColumn, Integer>> timePotentials,
            List<? extends List<? extends String>> sampleData, List<Pair<PotentialColumn, Integer>> results)
    {
        Pair<DateColumn, Integer> secondTime = null;
        DateColumnValueProvider valueProvider = new DateColumnValueProvider();

        while (secondTime == null && !timePotentials.isEmpty())
        {
            Pair<DateColumn, Integer> anotherTime = myTimeDecider.compileResults(timePotentials).get(0);

            int greaterCount = 0;
            int lesserCount = 0;

            for (List<? extends String> row : sampleData)
            {
                try
                {
                    Date anotherDate = valueProvider.getDate(row, anotherTime.getFirstObject());
                    Date firstTime = valueProvider.getDate(row, firstTimeColumn);

                    int compare = firstTime.compareTo(anotherDate);
                    if (compare < 0)
                    {
                        greaterCount++;
                    }
                    else if (compare > 0)
                    {
                        lesserCount++;
                    }
                }
                catch (ParseException e)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug(e.getMessage(), e);
                    }
                }
            }

            if (greaterCount > lesserCount
                    && anotherTime.getFirstObject().getPrimaryColumnIndex() > firstTimeColumn.getPrimaryColumnIndex()
                    || lesserCount > greaterCount
                            && anotherTime.getFirstObject().getPrimaryColumnIndex() < firstTimeColumn.getPrimaryColumnIndex())
            {
                secondTime = anotherTime;
            }
            else
            {
                movePotentialToResults(anotherTime.getFirstObject(), timePotentials, null);
            }
        }

        if (secondTime != null)
        {
            movePotentialToResults(secondTime.getFirstObject(), timePotentials, results);
        }
        else
        {
            results.clear();
        }
    }

    /**
     * Moves the Potential from time timePotentials to results.
     *
     * @param column The column to remove.
     * @param timePotentials The list to remove the column from.
     * @param results The results to add the potential to, or null if there
     *            isn't one.
     */
    private void movePotentialToResults(DateColumn column, List<Pair<PotentialColumn, Integer>> timePotentials,
            List<Pair<PotentialColumn, Integer>> results)
    {
        Pair<PotentialColumn, Integer> theMover = null;

        for (Pair<PotentialColumn, Integer> potential : timePotentials)
        {
            if (column.getPrimaryColumnIndex() == potential.getFirstObject().getColumnIndex())
            {
                theMover = potential;
                break;
            }
        }

        timePotentials.remove(theMover);

        if (results != null)
        {
            results.add(theMover);
        }
    }
}

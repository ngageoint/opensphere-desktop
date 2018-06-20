package io.opensphere.csvcommon.detect.datetime.algorithm.deciders;

import java.util.List;
import java.util.Set;

import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.csvcommon.common.datetime.DateColumn;
import io.opensphere.csvcommon.detect.datetime.model.PotentialColumn;
import io.opensphere.csvcommon.detect.datetime.model.SuccessfulFormat;
import io.opensphere.csvcommon.detect.datetime.util.Constants;

/**
 * Scores the potential date columns assuming that they are paired together to
 * make one date/time value.
 *
 */
public class CompositeDateTimeDecider implements Decider
{
    /**
     * Scores individual date columns.
     */
    private final DateDecider myDateDecider = new DateDecider();

    /**
     * Scores individual time columns.
     */
    private final TimeDecider myTimeDecider = new TimeDecider();

    @Override
    public List<Pair<PotentialColumn, Integer>> calculateConfidence(List<PotentialColumn> potentials,
            List<? extends List<? extends String>> sampleData)
    {
        List<Pair<PotentialColumn, Integer>> dateScores = myDateDecider.calculateConfidence(potentials, sampleData);

        Set<Integer> dateColumns = New.set();
        for (Pair<PotentialColumn, Integer> pair : dateScores)
        {
            dateColumns.add(Integer.valueOf(pair.getFirstObject().getColumnIndex()));
        }

        List<PotentialColumn> timePotentials = New.list();
        for (PotentialColumn column : potentials)
        {
            if (!dateColumns.contains(Integer.valueOf(column.getColumnIndex())))
            {
                timePotentials.add(column);
            }
        }

        List<Pair<PotentialColumn, Integer>> timeScores = myTimeDecider.calculateConfidence(timePotentials, sampleData);

        Pair<PotentialColumn, Integer> bestDate = getBestPassingColumn(dateScores);

        List<Pair<PotentialColumn, Integer>> bestColumns = New.list();

        if (bestDate != null)
        {
            Pair<PotentialColumn, Integer> bestTime = getBestPassingColumn(timeScores);

            if (bestTime != null && bestTime.getFirstObject().getColumnIndex() > bestDate.getFirstObject().getColumnIndex())
            {
                bestColumns.add(bestDate);
                bestColumns.add(bestTime);
            }
        }

        return bestColumns;
    }

    @SuppressWarnings("null")
    @Override
    public List<Pair<DateColumn, Integer>> compileResults(List<Pair<PotentialColumn, Integer>> potentials)
    {
        PotentialColumn datePart = null;
        SuccessfulFormat dateFormat = null;
        Integer datePartScore = null;
        PotentialColumn timePart = null;
        SuccessfulFormat timeFormat = null;
        Integer timePartScore = null;

        for (Pair<PotentialColumn, Integer> potential : potentials)
        {
            PotentialColumn column = potential.getFirstObject();
            SuccessfulFormat successfulFormat = column.getMostSuccessfulFormat();

            if (successfulFormat != null && successfulFormat.getFormat() != null
                    && successfulFormat.getFormat().getType() == Type.DATE && datePart == null)
            {
                datePart = column;
                dateFormat = successfulFormat;
                datePartScore = potential.getSecondObject();
            }

            if (successfulFormat != null && successfulFormat.getFormat() != null
                    && successfulFormat.getFormat().getType() == Type.TIME && timePart == null)
            {
                timePart = column;
                timeFormat = successfulFormat;
                timePartScore = potential.getSecondObject();
            }

            if (datePart != null && timePart != null)
            {
                break;
            }
        }

        List<Pair<DateColumn, Integer>> result = New.list();

        if (dateFormat != null && timeFormat != null)
        {
            DateColumn column = new DateColumn();
            column.setDateColumnType(Type.TIMESTAMP);
            column.setPrimaryColumnFormat(dateFormat.getFormat().getSdf());
            column.setPrimaryColumnIndex(datePart.getColumnIndex());
            column.setSecondaryColumnFormat(timeFormat.getFormat().getSdf());
            column.setSecondaryColumnIndex(timePart.getColumnIndex());

            int score = (datePartScore.intValue() + timePartScore.intValue()) / 2;

            result.add(new Pair<DateColumn, Integer>(column, Integer.valueOf(score)));
        }

        return result;
    }

    @Override
    public void setConfidence(int passingScore)
    {
        myDateDecider.setConfidence(passingScore);
        myTimeDecider.setConfidence(passingScore);
    }

    /**
     * Gets the best passing score out of the list of scores.
     *
     * @param scores The list of scores.
     * @return The best passing score or null, if there isn't one.
     */
    private Pair<PotentialColumn, Integer> getBestPassingColumn(List<Pair<PotentialColumn, Integer>> scores)
    {
        Pair<PotentialColumn, Integer> bestPassing = null;

        Integer maxScore = Integer.valueOf(Constants.THRESHOLD_SCORE);

        for (Pair<PotentialColumn, Integer> score : scores)
        {
            if (score.getSecondObject().compareTo(maxScore) > 0)
            {
                maxScore = score.getSecondObject();
                bestPassing = score;
            }
        }

        return bestPassing;
    }
}

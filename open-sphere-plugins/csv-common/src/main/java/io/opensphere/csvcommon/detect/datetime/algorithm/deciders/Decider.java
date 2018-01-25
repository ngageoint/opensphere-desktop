package io.opensphere.csvcommon.detect.datetime.algorithm.deciders;

import java.util.List;

import io.opensphere.core.util.lang.Pair;
import io.opensphere.csvcommon.common.datetime.DateColumn;
import io.opensphere.csvcommon.detect.datetime.model.PotentialColumn;

/**
 * Scores the list of potential date columns at how confident this decider is at
 * building the results.
 *
 */
public interface Decider
{
    /**
     * Calculate the confidence at how well this decider would be at compiling
     * the results.
     *
     * @param potentials The list of potential date columns sorted by successful
     *            formats.
     * @param sampleData A sampling of the csv file data of all columns and a
     *            few rows.
     * @return The list of columns and their corresponding confidence value.
     */
    List<Pair<PotentialColumn, Integer>> calculateConfidence(List<PotentialColumn> potentials,
            List<? extends List<? extends String>> sampleData);

    /**
     * Compiles the results and returns date column which identifies the column
     * or columns that represent a date.
     *
     * @param potentials The list of potential date columns.
     * @return The compiled date column or columns.
     */
    List<Pair<DateColumn, Integer>> compileResults(List<Pair<PotentialColumn, Integer>> potentials);

    /**
     * Changes the passing score.
     *
     * @param passingScore The score to change to.
     */
    void setConfidence(int passingScore);
}

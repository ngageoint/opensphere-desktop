package io.opensphere.csvcommon.detect.datetime.algorithm.deciders;

import java.util.List;

import io.opensphere.core.util.collections.New;

/**
 * Creates the list of deciders that score the potential date columns on whether
 * or not they are date/time columns, date columns, time columns.
 *
 */
public final class DeciderFactory
{
    /**
     * The instance of this class.
     */
    private static final DeciderFactory ourInstance = new DeciderFactory();

    /**
     * Gets the instance of this class.
     *
     * @return The instance of this class.
     */
    public static DeciderFactory getInstance()
    {
        return ourInstance;
    }

    /**
     * Not constructible.
     */
    private DeciderFactory()
    {
    }

    /**
     * Builds the deciders and returns them in the order to execute them.
     *
     * @return The deciders to execute in the order to execute them in.
     */
    public List<Decider> buildDeciders()
    {
        return New.list(new OneDayMultipleTimesDecider(), new CompositeDateTimeDecider(), new DateTimeDecider(),
                new DateDecider(), new TimeDecider());
    }
}

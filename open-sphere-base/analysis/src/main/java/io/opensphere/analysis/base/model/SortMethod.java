package io.opensphere.analysis.base.model;

import java.util.Comparator;

import io.opensphere.analysis.binning.bins.Bin;
import io.opensphere.analysis.util.DataTypeUtilities;
import io.opensphere.mantle.data.element.DataElement;

/** The sorting method for bin data. */
public enum SortMethod
{
    /** Value - ascending. */
    VALUE_ASCENDING("Value Ascending", (o1, o2) -> DataTypeUtilities.compareTo(o1.getValueObject(), o2.getValueObject())),

    /** Value - descending. */
    VALUE_DESCENDING("Value Descending", (o1, o2) -> DataTypeUtilities.compareTo(o2.getValueObject(), o1.getValueObject())),

    /** Count - ascending. */
    COUNT_ASCENDING("Count Ascending", (o1, o2) -> Integer.compare(o1.getSize(), o2.getSize())),

    /** Count - descending. */
    COUNT_DESCENDING("Count Descending", (o1, o2) -> Integer.compare(o2.getSize(), o1.getSize()));

    /** The display text. */
    private final String myText;

    /** The comparator. */
    private final Comparator<Bin<DataElement>> myComparator;

    /**
     * Constructor.
     *
     * @param text The display text
     * @param comparator The comparator
     */
    private SortMethod(String text, Comparator<Bin<DataElement>> comparator)
    {
        myText = text;
        myComparator = comparator;
    }

    @Override
    public String toString()
    {
        return myText;
    }

    /**
     * Gets the comparator.
     *
     * @return the comparator
     */
    public Comparator<Bin<DataElement>> getComparator()
    {
        return myComparator;
    }
}

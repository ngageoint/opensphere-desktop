package io.opensphere.merge.algorithm;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.merge.model.MergedDataRow;

/**
 * Join data.
 */
public class JoinData extends DatasetOperation
{
    /** Error reporting. */
    private static final String NAME_CONFLICT_PREFIX = "There is a column name conflict for ";

    /** The columns. */
    private final List<Col> joinCols = new LinkedList<>();

    /** The sources. */
    private final List<JoinInfo> src = new LinkedList<>();

    /** Use exact. */
    private boolean useExact;

    /**
     * Create a Stream that visits all but the first element in the provided
     * Collection. Note: if the Collection does not define an order, then the
     * skipped element is also not specified.
     *
     * @param collection the Collection to be traversed
     * @param <T> the data type of the supplied collection.
     * @return a stream that visits all but the first element in the supplied
     *         collection.
     */
    private static <T> Stream<T> skipStream(Collection<T> collection)
    {
        return collection.stream().filter(new JoinData.SkipOne<>());
    }

    /**
     * Get sources.
     *
     * @return the src
     */
    public List<JoinInfo> getSrc()
    {
        return src;
    }

    /**
     * Use exact.
     *
     * @return the useExact
     */
    public boolean isUseExact()
    {
        return useExact;
    }

    /**
     * Get a list of columns that are included in the result of the join.
     *
     * @return List of Col
     */
    public List<Col> getColumnDefs()
    {
        List<Col> ret = new LinkedList<>();
        ret.add(joinCols.get(0));
        for (List<Col> eq : equiv)
        {
            ret.add(eq.get(0));
        }
        return ret;
    }

    /** Joins. Stuff. */
    public void join()
    {
        prepareForJoinOperation();
        // if an error message was generated, we cannot proceed
        if (errorMessage != null)
        {
            return;
        }

        // join records on exact value matches
        JoinInfo primary = src.get(0);
        for (DataElement elt : getSupp().getRecords(primary.getType()))
        {
            Map<String, Serializable> valMap = new TreeMap<>();
            primary.mergeRecordWithValueMap(valMap, elt);
            Object val = elt.getMetaData().getValue(primary.getJoinKey());
            if (val == null)
            {
                continue;
            }

            if (useExact)
            {
                skipStream(src).forEach(ji -> ji.mergeInMatch(valMap, val));
            }
            else
            {
                skipStream(src).forEach(ji -> ji.mergeInSub(valMap, val));
            }

            MapGeometrySupport geometry = getMapGeometry(elt);
            getAllData().add(new MergedDataRow(valMap, geometry, elt.getTimeSpan()));
        }
    }

    /**
     * Sets use exact.
     *
     * @param b the useExact to set
     */
    public void setUseExact(boolean b)
    {
        useExact = b;
    }

    /**
     * Add keeper.
     *
     * @param c The column.
     */
    private void addKeeper(Col c)
    {
        src.stream().filter(x -> x.getType() == c.owner).findFirst().ifPresent(x ->
        {
            if (c != null && c.definedName != null && !c.definedName.isEmpty())
            {
                x.getKeepKeys().add(c.definedName);
            }
            else
            {
                x.getKeepKeys().add(c.name);
            }
        });
    }

    /**
     * enjoin.
     *
     * @param cols The columns.
     * @param joinKey The join key.
     */
    private void enjoin(List<Col> cols, String joinKey)
    {
        for (Col c : cols)
        {
            if (c.name.equals(joinKey))
            {
                joinCols.add(c);
            }
            else
            {
                insertEquiv(c);
            }
        }
    }

    /**
     * Make sure there is no conflict with the join column.
     *
     * @return an error message, if an error is found, or null
     */
    private String noMatchJoin()
    {
        String joinKey = joinCols.get(0).name;
        for (List<Col> eq : equiv)
        {
            if (eq.get(0).name.equals(joinKey))
            {
                return NAME_CONFLICT_PREFIX + joinKey + ".";
            }
        }
        return null;
    }

    /** Check preconditions and prepare to perform a join operation. */
    private void prepareForJoinOperation()
    {
        // put columns into equivalence classes and separate join columns
        for (JoinInfo joinColumn : src)
        {
            enjoin(getCols(joinColumn.getType()), joinColumn.getJoinKey());
        }

        // check for illegal conditions
        if (!croakOnError(noMatchJoin()) || !croakOnError(Util.validateAll(equiv)))
        {
            return;
        }

        // attribute which fields to keep from each source type
        src.get(0).getKeepKeys().add(joinCols.get(0).name);
        for (List<Col> columns : equiv)
        {
            addKeeper(columns.get(0));
        }
        // assemble the list of keys in the product
        List<String> keyList = new LinkedList<>();
        for (JoinInfo joinColumnDefinition : src)
        {
            keyList.addAll(joinColumnDefinition.getKeepKeys());
        }
        newKeys = keyList;
        // index the secondary sources by their join column values
        skipStream(src).forEach(joinColumns -> joinColumns.indexData(getSupp().getRecords(joinColumns.getType()), useExact));
    }

    /**
     * This class is a contrivance that filters a Stream so that all but the
     * first element are visited (called from the
     * {@link JoinData#skipStream(Collection)} method.
     *
     * @param <T> the Stream element type
     */
    private static class SkipOne<T> implements Predicate<T>
    {
        /** A flag used to indicate that the element should be kept. */
        private boolean keep;

        /**
         * {@inheritDoc}
         *
         * @see java.util.function.Predicate#test(java.lang.Object)
         */
        @Override
        public boolean test(T element)
        {
            boolean returnValue = keep;
            keep = true;
            return returnValue;
        }
    }
}

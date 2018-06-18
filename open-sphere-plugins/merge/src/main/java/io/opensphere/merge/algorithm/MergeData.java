package io.opensphere.merge.algorithm;

import java.io.Serializable;
import java.util.LinkedHashMap; // added import
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.merge.model.MergedDataRow;

/**
 * The merge data.
 */
public class MergeData extends DatasetOperation
{
    /** The merge sources. */
    private final List<DataTypeInfo> src = new LinkedList<>();

    /**
     * Gets the meta value.
     *
     * @param meta The metadata provider.
     * @param key The column to get the value for.
     * @return The value of that cell.
     */
    private static Object getMetaVal(MetaDataProvider meta, String key)
    {
        if (key == null)
        {
            return null;
        }
        return meta.getValue(key);
    }

    /**
     * Keys for merge.
     *
     * @param equiv The equivalents.
     * @return The keys to merge.
     */
    private static List<String> keysForMerge(List<List<Col>> equiv)
    {
        List<String> keys = new LinkedList<>();
        for (List<Col> eq : equiv)
        {
            Col c = eq.get(0);
            if (c.definedName != null && !c.definedName.isEmpty())
            {
                keys.add(c.definedName);
            }
            else
            {
                keys.add(c.name);
            }
        }
        return keys;
    }

    /**
     * Gets the keys for the type.
     *
     * @param t The layer.
     * @param equiv equivs.
     * @return The keys.
     */
    private static String[] keysForType(DataTypeInfo t, List<List<Col>> equiv)
    {
        String[] keys = new String[equiv.size()];
        int i = 0;
        for (List<Col> eq : equiv)
        {
            keys[i++] = eq.stream().filter(c -> c.owner == t).map(c -> c.name).findAny().orElse(null);
        }
        return keys;
    }

    /**
     * Gets the sources to merge.
     *
     * @return the src
     */
    public List<DataTypeInfo> getSrc()
    {
        return src;
    }

    /** Merges. Stuff. */
    public void merge()
    {
        // put columns into equivalence classes
        for (DataTypeInfo t : src)
        {
            enjoin(getCols(t));
        }

        // make sure there are no illegal matchups
        if (!croakOnError(Util.validateAll(equiv)))
        {
            return;
        }

        // assemble the key names for the new data type
        newKeys = keysForMerge(equiv);
        // add the source records to the new data set in list order
        for (DataTypeInfo t : src)
        {
            mergeIn(t);
        }
    }

    /**
     * En joins.
     *
     * @param cols The columns.
     */
    private void enjoin(List<Col> cols)
    {
        for (Col c : cols)
        {
            insertEquiv(c);
        }
    }

    /**
     * Merges in.
     *
     * @param t The layer.
     */
    private void mergeIn(DataTypeInfo t)
    {
        // assemble the key names for the original data type
        String[] oldKeys = keysForType(t, equiv);
        // get the records for this type and merge them in
        for (DataElement elt : getSupp().getRecords(t))
        {
            MetaDataProvider meta = elt.getMetaData();
            Map<String, Serializable> valMap = new LinkedHashMap<>(); //orig TreeMap
            for (int i = 0; i < newKeys.size(); i++)
            {
                Object value = getMetaVal(meta, oldKeys[i]);
                if (value instanceof Serializable)
                {
                    Util.putNonNull(valMap, newKeys.get(i), (Serializable)value);
                }
            }

            MapGeometrySupport geometry = getMapGeometry(elt);
            getAllData().add(new MergedDataRow(valMap, geometry, elt.getTimeSpan()));
        }
    }
}

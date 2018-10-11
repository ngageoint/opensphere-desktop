
package io.opensphere.mantle.crust;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.impl.DefaultDataElement;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapPointGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapPolylineGeometrySupport;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LongitudeKey;

/**
 * The purpose of this class is to reduce the effort required to deal with the
 * complex APIs in Mantle (and, to some extent, Core).
 */
public class DataUtil implements NamedDataSetManager
{
    /** Static counter for unique IDs. */
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    /** We need this to create a DefaultDataTypeInfo. */
    private Toolbox myToolbox;

    /** For manipulating data groups. */
    private DataGroupController myGroupController;

    /** The name of the root data element in the data type tree. */
    private String myRootName;

    /** All layers created by this DataUtil are subordinate to this root. */
    private DataGroupInfo myRootGroup;

    /** Map of data sets by their unique layer names. */
    private final Map<String, GenericDataSet> myNamedDataSets = new TreeMap<>();

    /**
     * Grant access to the Toolbox and all of its many, many subcomponents.
     *
     * @param toolbox the toolbox through which application state is accessed.
     */
    public void setToolbox(Toolbox toolbox)
    {
        myToolbox = toolbox;
        MantleToolbox mantleTools = myToolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        myGroupController = mantleTools.getDataGroupController();
    }

    /**
     * Gets the value of the {@link #myRootName} field.
     *
     * @return the value stored in the {@link #myRootName} field.
     */
    public String getRootName()
    {
        return myRootName;
    }

    /**
     * Create a root DataGroupInfo; all layers created by this DataUtil instance
     * will be subordinate to the root. Note: can only be called after
     * {@link #setToolbox(Toolbox)} is called.
     *
     * @param name as you might suspect
     */
    public void setupRoot(String name)
    {
        myRootName = name;
        myRootGroup = new DefaultDataGroupInfo(true, myToolbox, myRootName, myRootName);
        myGroupController.addRootDataGroupInfo(myRootGroup, this);
    }

    /**
     * This is the main method for the class. Put simply, it takes a set of
     * key-value maps and installs it in Mantle as a Layer.
     *
     * @param cat DataModelCategory
     * @param typeId the layer name
     * @param meta type structure for the layer
     * @param recs the data set
     */
    public void registerData(DataModelCategory cat, String typeId, MetaDataInfo meta, List<Map<String, Object>> recs)
    {
        GenericDataSet data = new GenericDataSet(this, myToolbox, cat, typeId, this::getRootName);
        data.create(myRootGroup, meta);
        // special keys for latitude and longitude may be null; in that case,
        // the records will not be shown on the map
        String latKey = meta.getKeyForSpecialType(LatitudeKey.DEFAULT);
        String lonKey = meta.getKeyForSpecialType(LongitudeKey.DEFAULT);
        data.setMapSupport(latKey, lonKey);
        data.populate(recs, new TreeSet<>(meta.getKeyNames()));
        addDataSet(data);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Index a new data set by its name within the resident Map (thread-safe).
     * </p>
     *
     * @see io.opensphere.mantle.crust.NamedDataSetManager#addDataSet(io.opensphere.mantle.crust.GenericDataSet)
     */
    @Override
    public void addDataSet(GenericDataSet data)
    {
        synchronized (myNamedDataSets)
        {
            myNamedDataSets.put(data.getName(), data);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Remove a data set from the resident Map (thread-safe).
     * </p>
     *
     * @see io.opensphere.mantle.crust.NamedDataSetManager#removeDataSet(java.lang.String)
     */
    @Override
    public void removeDataSet(String name)
    {
        synchronized (myNamedDataSets)
        {
            myNamedDataSets.remove(name);
        }
    }

    /**
     * Deal with the annoying Mantle API.
     *
     * @param cols a Map of column names to types
     * @param special a Map of column names to SpecialKeys
     * @return all of that as a MetaDataInfo
     */
    public static MetaDataInfo getMetaDataInfo(Map<String, Class<?>> cols, Map<String, SpecialKey> special)
    {
        DefaultMetaDataInfo meta = new DefaultMetaDataInfo();
        // add the columns
        cols.entrySet().forEach(e -> meta.addKey(e.getKey(), e.getValue(), null));

        // add the specials
        special.entrySet().forEach(e -> meta.setSpecialKey(e.getKey(), e.getValue(), null));

        return meta;
    }

    /**
     * Create a DataElement from a Map of String to Object.
     *
     * @param valMap the mapping of keys to values
     * @return all of that as a DataElement
     */
    public static DataElement createDataElement(Map<String, Object> valMap)
    {
        DefaultDataElement defaultDataElement = new DefaultDataElement(ID_COUNTER.getAndIncrement());
        // new DefaultDataElement(ID_COUNTER++, TimeSpan.TIMELESS, sink);
        defaultDataElement.setMetaDataProvider(new SimpleMetaDataProvider(valMap));
        return defaultDataElement;
    }

    /**
     * Create a DataElement from a Map of String to Object. This method also
     * accepts a set of field names, which can include those which do not show
     * up in the map because their values are null.
     *
     * @param valMap the mapping of keys to values
     * @param cols the full set of keys
     * @return all of that as a DataElement
     */
    public static DataElement createDataElement(Map<String, Object> valMap, Set<String> cols)
    {
        DefaultDataElement dde = new DefaultDataElement(ID_COUNTER.getAndIncrement());
        // new DefaultDataElement(ID_COUNTER++, TimeSpan.TIMELESS, sink);
        dde.setMetaDataProvider(new SimpleMetaDataProvider(valMap, cols));
        return dde;
    }

    /**
     * Create a DataElement with support for being shown on the map.
     *
     * @param mdp the data fields
     * @param type the data layer
     * @param firstPoint The first point of the polyline.
     * @param secondPoint The second point in the polyline or null if we should
     *            just create a point.
     * @return all of that as a DataElement
     */
    public static DataElement createMapDataElement(MetaDataProvider mdp, DataTypeInfo type, LatLonAlt firstPoint,
            LatLonAlt secondPoint)
    {
        MapGeometrySupport mgs = null;
        if (secondPoint == null)
        {
            mgs = new SimpleMapPointGeometrySupport(firstPoint);
        }
        else
        {
            mgs = new SimpleMapPolylineGeometrySupport(New.list(firstPoint, secondPoint));
        }

        mgs.setColor(type.getBasicVisualizationInfo().getTypeColor(), null);
        return new DefaultMapDataElement(ID_COUNTER.getAndIncrement(), null, type, mdp, mgs);
    }
}

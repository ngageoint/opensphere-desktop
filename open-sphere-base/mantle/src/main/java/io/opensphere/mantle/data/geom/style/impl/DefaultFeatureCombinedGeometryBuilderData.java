package io.opensphere.mantle.data.geom.style.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.geom.style.FeatureCombinedGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.FeatureIndividualGeometryBuilderData;

/**
 * The Class DefaultFeatureIndividualGeometryBuilderData.
 */
public class DefaultFeatureCombinedGeometryBuilderData implements FeatureCombinedGeometryBuilderData
{
    /** The BD list. */
    private List<FeatureIndividualGeometryBuilderData> myBDList;

    /**
     * Instantiates a new default feature combined geometry builder data.
     */
    public DefaultFeatureCombinedGeometryBuilderData()
    {
        myBDList = New.linkedList();
    }

    /**
     * Instantiates a new default feature combined geometry builder data.
     *
     * @param bdCollection the bd collection
     */
    public DefaultFeatureCombinedGeometryBuilderData(Collection<FeatureIndividualGeometryBuilderData> bdCollection)
    {
        setList(bdCollection);
    }

    /**
     * Adds the builder data to the list.
     *
     * @param bd the {@link FeatureIndividualGeometryBuilderData}
     */
    public void addBuidler(FeatureIndividualGeometryBuilderData bd)
    {
        myBDList.add(bd);
    }

    @Override
    public int getFeatureCount()
    {
        return myBDList.size();
    }

    @Override
    public Iterator<FeatureIndividualGeometryBuilderData> iterator()
    {
        return myBDList.iterator();
    }

    /**
     * Sets all fields.
     *
     * @param bdCollection the bd collection
     */
    public final void setList(Collection<FeatureIndividualGeometryBuilderData> bdCollection)
    {
        myBDList = New.linkedList(bdCollection);
    }
}

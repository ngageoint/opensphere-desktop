package io.opensphere.kml.mantle.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import gnu.trove.TLongCollection;
import gnu.trove.iterator.TLongIterator;
import io.opensphere.core.util.collections.New;
import io.opensphere.kml.common.model.KMLFeature;

/**
 * Cache for Feature &lt;=&gt; Id mappings.
 */
@ThreadSafe
public final class KMLFeatureIdCache
{
    /** Map from id to feature. */
    @GuardedBy("this")
    private final Map<Long, KMLFeature> myIdToFeatureMap;

    /**
     * Private constructor.
     */
    public KMLFeatureIdCache()
    {
        myIdToFeatureMap = new HashMap<>();
    }

    /**
     * Gets the ids for the given features.
     *
     * @param features The features
     * @return The mapped ids
     */
    public static List<Long> getIds(Collection<KMLFeature> features)
    {
        List<Long> ids = new ArrayList<>(features.size());
        for (KMLFeature feature : features)
        {
            if (feature.getId() != 0)
            {
                ids.add(Long.valueOf(feature.getId()));
            }
        }
        return ids;
    }

    /**
     * Adds mappings between features and ids.
     *
     * @param features The features
     * @param ids The IDs
     */
    public synchronized void addFeatures(List<KMLFeature> features, List<Long> ids)
    {
        if (features.isEmpty() || ids.isEmpty())
        {
            return;
        }

        for (int i = 0, n = Math.min(features.size(), ids.size()); i < n; i++)
        {
            KMLFeature feature = features.get(i);
            Long id = ids.get(i);

            // Map feature to id
            feature.setId(id.longValue());

            // Map id to feature
            myIdToFeatureMap.put(id, feature);
        }
    }

    /**
     * Gets the feature for the given id.
     *
     * @param id The ID
     * @return The mapped feature, or null
     */
    public synchronized KMLFeature getFeature(Long id)
    {
        return myIdToFeatureMap.get(id);
    }

    /**
     * Gets the feature for the given id.
     *
     * @param ids The IDs
     * @return The mapped feature, or null
     */
    public synchronized List<KMLFeature> getFeatures(TLongCollection ids)
    {
        List<KMLFeature> features = New.list(ids.size());
        for (TLongIterator iter = ids.iterator(); iter.hasNext();)
        {
            long id = iter.next();
            KMLFeature feature = myIdToFeatureMap.get(Long.valueOf(id));
            if (feature != null)
            {
                features.add(feature);
            }
        }
        return features;
    }

    /**
     * Removes mappings between features and ids.
     *
     * @param features The features
     * @return The ids that were removed
     */
    public synchronized Collection<Long> removeFeatures(Collection<KMLFeature> features)
    {
        Collection<Long> ids = getIds(features);
        for (Long id : ids)
        {
            disassociate(id);
        }
        return ids;
    }

    /**
     * Replaces existing features with new equivalent objects.
     *
     * @param features The updated features
     */
    public synchronized void updateFeatures(Collection<KMLFeature> features)
    {
        for (KMLFeature feature : features)
        {
            // Re-map id to the new feature
            if (feature.getId() != 0)
            {
                myIdToFeatureMap.put(Long.valueOf(feature.getId()), feature);
            }
        }
    }

    /**
     * Disassociates an id with its feature.
     *
     * @param id The id
     */
    private void disassociate(Long id)
    {
        KMLFeature feature = myIdToFeatureMap.remove(id);
        if (feature != null)
        {
            feature.setId(0);
        }
    }
}

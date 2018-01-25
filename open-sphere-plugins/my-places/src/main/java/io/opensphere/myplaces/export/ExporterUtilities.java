package io.opensphere.myplaces.export;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.myplaces.models.MyPlacesDataGroupInfo;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;

/**
 * My Places exporter utilities.
 */
public final class ExporterUtilities
{
    /**
     * Flattens a KML structure to placemarks.
     *
     * @param feature the root feature
     * @param placemarks the collection of placemarks to build up
     */
    public static void flattenToPlacemarks(Feature feature, Collection<Placemark> placemarks)
    {
        if (feature instanceof Placemark)
        {
            placemarks.add((Placemark)feature);
        }
        else if (feature instanceof Folder)
        {
            for (Feature child : ((Folder)feature).getFeature())
            {
                flattenToPlacemarks(child, placemarks);
            }
        }
    }

    /**
     * Gets a list of first-level feature trees from the given objects.
     *
     * @param objects the objects
     * @return the feature trees
     */
    public static Collection<Feature> getFeatures(Collection<?> objects)
    {
        Collection<Feature> features = New.list();

        Map<String, MyPlacesDataGroupInfo> selectedGroups = getGroups(objects);
        Set<String> selectedTypeIds = getTypeIds(objects);
        MyPlacesDataGroupInfo rootGroup = getRootGroup(objects);

        Folder rootFolder = rootGroup.getKmlFolder().clone();

        removeUnselectedFeatures(rootFolder, selectedTypeIds);
        removeUnselectedFolders(rootFolder, selectedGroups);

        reIdFeatures(rootFolder);

        for (Feature feature : rootFolder.getFeature())
        {
            features.add(feature);
        }

        return features;
    }

    /**
     * Gets a list of all descendant placemarks from the given objects.
     *
     * @param objects the objects
     * @return the descendant placemarks
     */
    public static Collection<Placemark> getPlacemarks(Collection<?> objects)
    {
        Collection<Placemark> placemarks = New.list();
        for (Feature feature : getFeatures(objects))
        {
            flattenToPlacemarks(feature, placemarks);
        }
        return placemarks;
    }

    /**
     * Gets the groups.
     *
     * @param objects the objects
     * @return the groups
     */
    private static Map<String, MyPlacesDataGroupInfo> getGroups(Collection<?> objects)
    {
        Collection<MyPlacesDataGroupInfo> groups = New.list();
        CollectionUtilities.filterDowncast(objects, MyPlacesDataGroupInfo.class, groups);

        Map<String, MyPlacesDataGroupInfo> groupMap = New.map();

        for (MyPlacesDataGroupInfo group : groups)
        {
            groupMap.put(group.getKmlFolder().getId(), group);
        }

        return groupMap;
    }

    /**
     * Gets the root types from the given types.
     *
     * @param objects the objects to export.
     * @return the root group
     */
    private static MyPlacesDataGroupInfo getRootGroup(Collection<?> objects)
    {
        MyPlacesDataGroupInfo rootGroup = null;
        if (!objects.isEmpty())
        {
            Object firstObject = objects.iterator().next();
            DataGroupInfo parent = null;

            if (firstObject instanceof DataGroupInfo)
            {
                parent = (DataGroupInfo)firstObject;
            }
            else if (firstObject instanceof DataTypeInfo)
            {
                parent = ((DataTypeInfo)firstObject).getParent();
            }

            if (parent != null)
            {
                while (parent.getParent() != null)
                {
                    parent = parent.getParent();
                }
            }

            if (parent instanceof MyPlacesDataGroupInfo)
            {
                rootGroup = (MyPlacesDataGroupInfo)parent;
            }
        }

        return rootGroup;
    }

    /**
     * Gets the types.
     *
     * @param objects the objects
     * @return the types
     */
    private static Set<String> getTypeIds(Collection<?> objects)
    {
        Collection<MyPlacesDataTypeInfo> types = New.list();
        flattenToDataTypes(objects, types);

        HashSet<String> typeIds = new HashSet<>();
        for (MyPlacesDataTypeInfo type : types)
        {
            typeIds.add(type.getKmlPlacemark().getId());
        }

        return typeIds;
    }

    /**
     * Re ids the features within the folder.
     *
     * @param folder The folder containing features to re id.
     */
    private static void reIdFeatures(Folder folder)
    {
        folder.setId(UUID.randomUUID().toString());
        for (Feature feature : folder.getFeature())
        {
            if (feature instanceof Folder)
            {
                reIdFeatures((Folder)feature);
            }
            else
            {
                feature.setId(UUID.randomUUID().toString());
            }
        }
    }

    /**
     * Removes all non folder features that are not selected for export.
     *
     * @param folder The folder to walk.
     * @param selectedTypeIds The selected type ids.
     */
    private static void removeUnselectedFeatures(Folder folder, Set<String> selectedTypeIds)
    {
        List<Feature> featuresToRemove = New.list();
        for (Feature feature : folder.getFeature())
        {
            if (feature instanceof Folder)
            {
                removeUnselectedFeatures((Folder)feature, selectedTypeIds);
            }
            else if (!selectedTypeIds.contains(feature.getId()))
            {
                featuresToRemove.add(feature);
            }
        }

        folder.getFeature().removeAll(featuresToRemove);
    }

    /**
     * Removes all folder features that are not selected for export.
     *
     * @param folder The folder to walk.
     * @param selectedGroups The selected group ids.
     */
    private static void removeUnselectedFolders(Folder folder, Map<String, MyPlacesDataGroupInfo> selectedGroups)
    {
        List<Folder> foldersToRemove = New.list();
        for (Feature feature : folder.getFeature())
        {
            if (feature instanceof Folder)
            {
                Folder childFolder = (Folder)feature;
                removeUnselectedFolders(childFolder, selectedGroups);

                boolean hasNonFolderFeatures = !childFolder.getFeature().isEmpty();

                if (!hasNonFolderFeatures && selectedGroups.containsKey(childFolder.getId()))
                {
                    MyPlacesDataGroupInfo dataGroup = selectedGroups.get(childFolder.getId());
                    Folder folderToCopyFrom = dataGroup.getKmlFolder();
                    for (Feature copyFeature : folderToCopyFrom.getFeature())
                    {
                        childFolder.addToFeature(copyFeature.clone());
                    }
                }
                else if (!hasNonFolderFeatures && !selectedGroups.containsKey(childFolder.getId()))
                {
                    foldersToRemove.add(childFolder);
                }
            }
        }

        folder.getFeature().removeAll(foldersToRemove);
    }

    /**
     * Flattens a data group / data type structure to data types.
     *
     * @param objects the objects
     * @param types the collection of data types to build up
     */
    private static void flattenToDataTypes(Collection<?> objects, Collection<MyPlacesDataTypeInfo> types)
    {
        for (Object o : objects)
        {
            if (o instanceof MyPlacesDataTypeInfo)
            {
                types.add((MyPlacesDataTypeInfo)o);
            }
            else if (o instanceof MyPlacesDataGroupInfo)
            {
                MyPlacesDataGroupInfo group = (MyPlacesDataGroupInfo)o;
                Collection<Object> children = New.list();
                children.addAll(group.getChildren());
                children.addAll(group.getMembers(false));
                flattenToDataTypes(children, types);
            }
        }
    }

    /** Disallow instantiation. */
    private ExporterUtilities()
    {
    }
}

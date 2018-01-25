package io.opensphere.myplaces.util;

import java.util.List;
import java.util.UUID;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.myplaces.models.DataCouple;
import io.opensphere.myplaces.models.MyPlacesDataGroupInfo;

/**
 * Utility methods for data groups.
 *
 */
public final class GroupUtils
{
    /**
     * Creates and adds a group.
     *
     * @param name The name of the new group.
     * @param parent The parent to add the group to.
     * @param toolbox The toolbox.
     * @param source The source.
     * @return The new group.
     */
    public static MyPlacesDataGroupInfo createAndAddGroup(String name, MyPlacesDataGroupInfo parent, Toolbox toolbox,
            Object source)
    {
        Folder parentFolder = parent.getKmlFolder();
        Folder folder = parentFolder.createAndAddFolder();
        folder.setName(name);
        folder.setId(UUID.randomUUID().toString());
        folder.setVisibility(Boolean.TRUE);

        MyPlacesDataGroupInfo group = new MyPlacesDataGroupInfo(false, toolbox, folder);
        parent.addChild(group, source);

        group.activationProperty().setActive(true);

        return group;
    }

    /**
     * Gets the data type and its parent, given a type key.
     *
     * @param key The key of the data type to retrieve.
     * @param dataGroup The data group to search.
     * @return The data type and its parent or null if one was not found.
     */
    public static DataCouple getDataTypeAndParent(String key, DataGroupInfo dataGroup)
    {
        DataCouple couple = null;

        for (DataTypeInfo member : dataGroup.getMembers(false))
        {
            if (member.getTypeKey().equals(key))
            {
                couple = new DataCouple(member, dataGroup);
                break;
            }
        }

        if (couple == null)
        {
            for (DataGroupInfo child : dataGroup.getChildren())
            {
                couple = getDataTypeAndParent(key, child);
                if (couple != null)
                {
                    break;
                }
            }
        }

        return couple;
    }

    /**
     * Gets the data type and its parent, given a type key.
     *
     * @param dataGroup The data group to search.
     * @return The data type and its parent or null if one was not found.
     */
    public static List<DataCouple> getDataTypesAndParents(DataGroupInfo dataGroup)
    {
        List<DataCouple> couples = New.list();

        for (DataTypeInfo member : dataGroup.getMembers(false))
        {
            couples.add(new DataCouple(member, dataGroup));
        }

        for (DataGroupInfo child : dataGroup.getChildren())
        {
            couples.addAll(getDataTypesAndParents(child));
        }

        return couples;
    }

    /**
     * Not constructible.
     */
    private GroupUtils()
    {
    }
}

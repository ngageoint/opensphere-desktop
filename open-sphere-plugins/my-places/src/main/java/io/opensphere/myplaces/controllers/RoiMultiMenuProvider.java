package io.opensphere.myplaces.controllers;

import java.awt.Component;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfo.MultiDataGroupContextKey;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.specific.regions.utils.RegionUtils;

/** ROI MyPlaces menu provider. */
public class RoiMultiMenuProvider implements ContextMenuProvider<MultiDataGroupContextKey>
{
    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a new ROI myplaces menu provider to be used for the context
     * menu in the layers window.
     *
     * @param toolbox The system toolbox.
     */
    public RoiMultiMenuProvider(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    @Override
    public Collection<? extends Component> getMenuItems(String contextId, MultiDataGroupContextKey key)
    {
        List<Component> menuItems = Collections.emptyList();
        Collection<Geometry> geometries = New.list();
        Collection<DataTypeInfo> dataTypes = getDataTypes(key);

        for (DataTypeInfo dti : dataTypes)
        {
            if (dti instanceof MyPlacesDataTypeInfo)
            {
                MyPlacesDataTypeInfo type = (MyPlacesDataTypeInfo)dti;
                PolylineGeometry theGeom = RegionUtils.createGeometry(type.getKmlPlacemark());

                geometries.add(theGeom);
            }
        }
        if (!geometries.isEmpty())
        {
            menuItems = MantleToolboxUtils.getMantleToolbox(myToolbox).getSelectionHandler().getMultiGeometryMenu(geometries);
        }

        return menuItems;
    }

    @Override
    public int getPriority()
    {
        return 0;
    }

    /**
     * Gets the data types for the context key.
     *
     * @param key the context key
     * @return the data types
     */
    private Collection<DataTypeInfo> getDataTypes(MultiDataGroupContextKey key)
    {
        Collection<DataTypeInfo> dataTypes = key.getActualDataTypes();

        if (dataTypes.isEmpty())
        {
            for (DataGroupInfo group : key.getActualDataGroups())
            {
                dataTypes.addAll(group.getMembers(false));
            }
        }
        return dataTypes;
    }

}

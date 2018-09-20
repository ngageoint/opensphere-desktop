package io.opensphere.myplaces.controllers;

import java.awt.Component;
import java.util.Collections;
import java.util.List;

import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.specific.regions.utils.RegionUtils;

/**
 * Provides the context menu for ROI selections.
 */
public class RoiContextMenuProvider implements ContextMenuProvider<DataGroupContextKey>
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * Constructs region of interest context menu provider.
     *
     * @param toolbox The toolbox.
     */
    public RoiContextMenuProvider(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    @Override
    public List<Component> getMenuItems(String contextId, DataGroupContextKey key)
    {
        List<Component> menuItems = Collections.emptyList();
        if (key.getDataType() instanceof MyPlacesDataTypeInfo)
        {
            MyPlacesDataTypeInfo type = (MyPlacesDataTypeInfo)key.getDataType();
            Placemark kmlPlacemark = type.getKmlPlacemark();

            if (kmlPlacemark.getGeometry() instanceof Polygon)
            {
                PolylineGeometry geom = RegionUtils.createGeometry(kmlPlacemark);
                menuItems = MantleToolboxUtils.getMantleToolbox(myToolbox).getSelectionHandler().getGeometryMenuItems(geom);
            }

        }
        return menuItems;
    }

    @Override
    public int getPriority()
    {
        return 6;
    }
}

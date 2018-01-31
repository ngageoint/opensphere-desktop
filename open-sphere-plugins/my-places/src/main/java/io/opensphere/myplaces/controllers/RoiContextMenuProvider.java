package io.opensphere.myplaces.controllers;

import java.util.List;

import javax.swing.JMenuItem;

import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.geometry.PolygonGeometry;
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
     * @param model The my places model.
     */
    public RoiContextMenuProvider(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    @Override
    public List<JMenuItem> getMenuItems(String contextId, DataGroupContextKey key)
    {
        PolygonGeometry geom = null;
        MyPlacesDataTypeInfo type = (MyPlacesDataTypeInfo)key.getDataType();

        if (type != null)
        {
            Placemark placemark = type.getKmlPlacemark();
            geom = RegionUtils.createGeometry(placemark);
        }

        return MantleToolboxUtils.getMantleToolbox(myToolbox).getSelectionHandler().getGeometryMenuItems(geom);
    }

    @Override
    public int getPriority()
    {
        return 6;
    }

}

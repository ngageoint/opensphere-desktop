package io.opensphere.myplaces.controllers;

import java.util.List;

import javax.swing.JMenuItem;

import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.specific.regions.utils.RegionUtils;

/**
 * Provides the context menu for ROI selections.
 */
public class RoiContextMenuProvider implements ContextMenuProvider<DataGroupInfo.DataGroupContextKey>
{
    
    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The Current placemark. */
    private Placemark myPlacemark;

    /** The Type. */
    private MyPlacesDataTypeInfo myType;

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
        List<JMenuItem> menuItems = null;
        myType = (MyPlacesDataTypeInfo)key.getDataType();
         
        if (myType != null)
        {
            myPlacemark = myType.getKmlPlacemark();
            
        }
        PolygonGeometry geom = RegionUtils.createGeometry(myPlacemark);
        
        return MantleToolboxUtils.getMantleToolbox(myToolbox).getSelectionHandler().getGeomtryMenuItems(menuItems, geom);
    }

    @Override
    public int getPriority()
    {
        return 6;
    }
}

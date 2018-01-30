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
import io.opensphere.myplaces.models.MyPlacesDataGroupInfo;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.models.MyPlacesModel;
import io.opensphere.myplaces.specific.regions.utils.RegionUtils;

public class RoiContextMenuProvider implements ContextMenuProvider<DataGroupInfo.DataGroupContextKey>
{

    /**
     * The toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * The places model.
     */
    private final MyPlacesModel myModel;

    /** The Current placemark. */
    private Placemark myPlacemark;

    /** The Group. */
    private MyPlacesDataGroupInfo myGroup;

    /** The Type. */
    private MyPlacesDataTypeInfo myType;

    /**
     * Constructs region of interest context menu provider.
     *
     * @param toolbox The toolbox.
     * @param model The my places model.
     */
    public RoiContextMenuProvider(Toolbox toolbox, MyPlacesModel model)
    {
        myToolbox = toolbox;
        myModel = model;

    }

    /**
     * The menu provider for events related to single ROI geometry selection.
     * 
     */
    @Override
    public List<JMenuItem> getMenuItems(String contextId, DataGroupContextKey key)
    {

        myGroup = (MyPlacesDataGroupInfo)key.getDataGroup();
        myType = (MyPlacesDataTypeInfo)key.getDataType();

        if (myType != null)
        {
            myPlacemark = myType.getKmlPlacemark();
        }
        PolygonGeometry geom = RegionUtils.createGeometry(myPlacemark);
        List<JMenuItem> menuItems = null;

        return MantleToolboxUtils.getMantleToolbox(myToolbox).getSelectionHandler().getGeomtryMenuItems(menuItems, geom);
    }

    @Override
    public int getPriority()
    {
        return 6;
    }
}

package io.opensphere.myplaces.importer;

import java.awt.EventQueue;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;

import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.control.action.MenuOption;
import io.opensphere.core.export.ExportException;
import io.opensphere.core.util.MemoizingSupplier;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.geom.MapPolygonGeometrySupport;
import io.opensphere.myplaces.models.MyPlacesDataGroupInfo;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.models.MyPlacesEditListener;
import io.opensphere.myplaces.specific.factory.TypeControllerFactory;
import io.opensphere.myplaces.specific.regions.utils.RegionUtils;
import io.opensphere.myplaces.util.PlacemarkUtils;

/**
 * An exporter that creates regions from {@link DataElement}s.
 */
public class DataElementRegionExporter extends AbstractDataElementExporter
{
    @Override
    public MenuOption getMenuOption()
    {
        return new MenuOption("ROI", "ROI", "Copy selected data elements to ROI.");
    }

    @Override
    protected void export(DataTypeInfo dataType, Collection<? extends MapDataElement> elements) throws ExportException
    {
        if (!acceptSize(elements.size()))
        {
            return;
        }

        Supplier<String> nameColumnKey = new MemoizingSupplier<>(
            () -> EventQueueUtilities.happyOnEdt(() -> getNameColumnKey(dataType)));
        MyPlacesDataGroupInfo parentDataGroup = getParentGroup();

        List<MyPlacesDataTypeInfo> regionTypes = elements.stream()
                .filter(e -> e.getMapGeometrySupport() instanceof MapPolygonGeometrySupport)
                .map(e -> createDataType(e, nameColumnKey, parentDataGroup)).collect(Collectors.toList());

        if (!regionTypes.isEmpty())
        {
            if (regionTypes.get(0) != null)
            {
                for (MyPlacesDataTypeInfo regionType : regionTypes)
                {
                    parentDataGroup.addMember(regionType, this);
                }
            }
            // else the user cancelled
        }
        else
        {
            throw new ExportException("The selected data do not contain any polygons.");
        }
    }

    /**
     * Gets the name column key.
     *
     * @param dataType the data type
     * @return the name column key
     */
    private String getNameColumnKey(DataTypeInfo dataType)
    {
        assert EventQueue.isDispatchThread();

        String nameColumnKey = null;

        // Try some common keys
        String[] defaultKeys = { "Name", "NAME" };
        for (String defaultKey : defaultKeys)
        {
            if (dataType.getMetaDataInfo().hasKey(defaultKey))
            {
                nameColumnKey = defaultKey;
                break;
            }
        }

        // Ask the user if we can't figure it out
        if (nameColumnKey == null)
        {
            Object[] keyNames = dataType.getMetaDataInfo().getKeyNames().toArray();
            Object initialSelectionValue = keyNames.length > 0 ? keyNames[0] : null;
            nameColumnKey = (String)JOptionPane.showInputDialog(getToolbox().getUIRegistry().getMainFrameProvider().get(),
                    "Select column to use as name:", "Select Name Column", JOptionPane.QUESTION_MESSAGE, null, keyNames,
                    initialSelectionValue);
        }

        return nameColumnKey;
    }

    /**
     * Creates a my places data type from the data element.
     *
     * @param dataElement the data element
     * @param nameColumnKey the name column key supplier
     * @param parentDataGroup The parent data group for the region.
     * @return the data type
     */
    private MyPlacesDataTypeInfo createDataType(MapDataElement dataElement, Supplier<String> nameColumnKey,
            MyPlacesDataGroupInfo parentDataGroup)
    {
        String nameColumn = nameColumnKey.get();
        if (nameColumn == null)
        {
            return null;
        }

        String name = null;
        Object nameValue = dataElement.getMetaData().getValue(nameColumn);
        if (nameValue != null)
        {
            name = nameValue.toString();
        }
        if (StringUtils.isEmpty(name))
        {
            name = "UNKNOWN";
        }

        MapPolygonGeometrySupport polygon = (MapPolygonGeometrySupport)dataElement.getMapGeometrySupport();

        Placemark placemark = RegionUtils.createRegionFromLLAs(parentDataGroup.getKmlFolder(), name, polygon.getLocations(),
                polygon.getHoles());

        MyPlacesEditListener listener = TypeControllerFactory.getInstance()
                .getController(MapVisualizationType.ANNOTATION_REGIONS);

        return PlacemarkUtils.createDataType(placemark, getToolbox(), this, listener);
    }
}

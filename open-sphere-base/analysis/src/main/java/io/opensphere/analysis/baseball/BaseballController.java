package io.opensphere.analysis.baseball;

import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Window;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.analysis.util.MGRSUtilities;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.core.util.jts.JTSUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.viewer.impl.Viewer3D;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.event.DataElementDoubleClickedEvent;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.util.DataElementLookupUtils;
import io.opensphere.mantle.transformer.MapDataElementTransformer;
import io.opensphere.mantle.transformer.impl.StyleMapDataElementTransformer;
import io.opensphere.mantle.transformer.impl.StyleTransformerGeometryProcessor;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The baseball card controller.
 */
public class BaseballController extends EventListenerService
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     */
    public BaseballController(Toolbox toolbox)
    {
        super(toolbox.getEventManager());
        myToolbox = toolbox;
        bindEvent(DataElementDoubleClickedEvent.class, this::handleDoubleClickEvent);
    }

    /**
     * Handles a data element double click event.
     *
     * @param event the event
     */
    private void handleDoubleClickEvent(DataElementDoubleClickedEvent event)
    {
        if (!event.isConsumed())
        {
            DataElementLookupUtils lookupUtils = MantleToolboxUtils.getDataElementLookupUtils(myToolbox);
            List<DataElement> dataElements = New.list();
            GeometryFactory geometryFactory = new GeometryFactory();
            MapGeometrySupport support = lookupUtils.getMapGeometrySupport(event.getRegistryId());

            GeographicBoundingBox boundingBox = support.getBoundingBox(myToolbox.getMapManager()
                    .getProjection(Viewer3D.class).getSnapshot());
            if (boundingBox != null)
            {
                LatLonAlt center = boundingBox.getCenter().getLatLonAlt();
                GeographicPosition innerPosition = new GeographicPosition(center);
                Vector2i innerVector = myToolbox.getMapManager().convertToPoint(innerPosition);
                Vector2i outerVector = new Vector2i(innerVector.getX(), innerVector.getY() + 10);
                GeographicPosition outerPosition = myToolbox.getMapManager().convertToPosition(outerVector, ReferenceLevel.TERRAIN);
                double radius = GeographicBody3D.greatCircleDistanceM(center, outerPosition.getLatLonAlt(),
                        WGS84EarthConstants.RADIUS_EQUATORIAL_M);
                LatLonAlt edge = GeographicBody3D.greatCircleEndPosition(center, 0, WGS84EarthConstants.RADIUS_EQUATORIAL_M, radius);
                Polygon polygon = JTSUtilities.createCircle(center, edge, JTSUtilities.NUM_CIRCLE_SEGMENTS);
                StyleTransformerGeometryProcessor processor = null;

                DataTypeController dataTypeController = MantleToolboxUtils.getMantleToolbox(myToolbox).getDataTypeController();
                List<String> typeKeyList = New.list();
                dataTypeController.getDataTypeInfo().stream().forEach(e -> typeKeyList.add(e.getTypeKey()));

                for (String typeKey : typeKeyList)
                {
                    MapDataElementTransformer transformer = dataTypeController.getTransformerForType(typeKey);
                    if (transformer instanceof StyleMapDataElementTransformer)
                    {
                        processor = ((StyleMapDataElementTransformer)transformer).getGeometryProcessor();
                        processor.getGeometrySetLock().lock();
                        try
                        {
                            for (Geometry geometry : processor.getGeometrySet())
                            {
                                if (geometry.jtsIntersectionTests(new Geometry.JTSIntersectionTests(true, true, false),
                                        Collections.singletonList(polygon), geometryFactory))
                                {
                                    dataElements.add(lookupUtils.getDataElement(geometry.getDataModelId()
                                            & processor.getDataModelIdFromGeometryIdBitMask(), null, null));
                                }
                            }
                        }
                        finally
                        {
                            processor.getGeometrySetLock().unlock();
                        }
                    }
                }
                EventQueueUtilities.runOnEDT(() -> showDialog(dataElements));
            }
        }
    }

    /**
     * Shows the baseball dialog for the data elements.
     *
     * @param elements the data elements
     */
    private void showDialog(List<DataElement> elements)
    {
        Window owner = myToolbox.getUIRegistry().getMainFrameProvider().get();
        JFXDialog dialog = new JFXDialog(owner, "Feature Info", false);
        dialog.setFxNode(new BaseballPanel(myToolbox, elements));
        dialog.setSize(new Dimension(800, 600));
        dialog.setResizable(true);
        dialog.setLocationRelativeTo(owner);
        dialog.setModalityType(ModalityType.MODELESS);
        dialog.setVisible(true);
    }
}

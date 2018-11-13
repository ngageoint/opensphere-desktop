package io.opensphere.analysis.baseball;

import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Window;
import java.util.Collections;
import java.util.List;

import javax.swing.WindowConstants;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

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
import io.opensphere.core.util.ThreadConfined;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.core.util.jts.JTSUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.viewer.impl.Viewer3D;
import io.opensphere.mantle.data.element.DataElement;
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

    /** The dialog. */
    @ThreadConfined("EDT")
    private BaseballDialog myDialog;

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
//            final DataElement element = lookupUtils.getDataElement(event.getRegistryId(), null, event.getDataTypeKey());
            GeometryFactory gf = new GeometryFactory();
//            Projection proj = myToolbox.getMapManager().getProjection().getSnapshot();
            MapGeometrySupport mgs = MantleToolboxUtils.getDataElementLookupUtils(myToolbox).getMapGeometrySupport(event.getRegistryId());

            GeographicBoundingBox bb = mgs.getBoundingBox(myToolbox.getMapManager().getProjection(Viewer3D.class).getSnapshot());
            if (bb != null)
            {
                LatLonAlt center = bb.getCenter().getLatLonAlt();
                GeographicPosition innerPosition = new GeographicPosition(center);
                Vector2i innerVector = myToolbox.getMapManager().convertToPoint(innerPosition);
                Vector2i outerVector = new Vector2i(innerVector.getX(), innerVector.getY() + 10);
                GeographicPosition outerPosition = myToolbox.getMapManager().convertToPosition(outerVector, ReferenceLevel.TERRAIN);
                double radius = GeographicBody3D.greatCircleDistanceM(center, outerPosition.getLatLonAlt(), WGS84EarthConstants.RADIUS_EQUATORIAL_M);
                System.out.println("Radius: " + radius);
                LatLonAlt edge = GeographicBody3D.greatCircleEndPosition(center, 0, WGS84EarthConstants.RADIUS_EQUATORIAL_M, radius);
                Polygon pg = JTSUtilities.createCircle(center, edge, JTSUtilities.NUM_CIRCLE_SEGMENTS);
                StyleTransformerGeometryProcessor processor = null;
                MapDataElementTransformer transformer = MantleToolboxUtils.getMantleToolbox(myToolbox).getDataTypeController()
                        .getTransformerForType(event.getDataTypeKey());
                if (transformer instanceof StyleMapDataElementTransformer)
                {
                    processor = ((StyleMapDataElementTransformer)transformer).getGeometryProcessor();
                }
                int count = 0;
//                Set<Geometry> geometries = New.set(processor.getGeometrySet());
//                for (Geometry geometry : geometries)
                try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
                for (Geometry geometry : processor.getGeometrySet())
                {
                	count++;
                	if (geometry.jtsIntersectionTests(new Geometry.JTSIntersectionTests(true, true, false), Collections.singletonList(pg), gf))
                	{
                	    dataElements.add(lookupUtils.getDataElement(geometry.getDataModelId() & processor.getDataModelIdFromGeometryIdBitMask(), null, null));
                	}
                }
                dataElements.forEach(e -> System.out.println(e.getId()));
                System.out.println("Total points checked: " + count);
            }
//            MapGeometrySupportUtils.generateArcLengthCircle(mgs, 50000, 16, proj);
//            MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().getAllElementIdsAsList();
//            MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache();

//            if (element != null)
//            {
            EventQueueUtilities.runOnEDT(() -> newDialog(dataElements));
//            }
        }
    }

    /**
     * Shows the dialog for the data element.
     *
     * @param element the data element
     */
    private void showDialog(List<DataElement> elements)
    {
        if (myDialog == null)
        {
            myDialog = new BaseballDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(),
                    myToolbox.getPreferencesRegistry());
            myDialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            System.out.println("Null dialog");
        }
        myDialog.setDataElement(elements);
        myDialog.setVisible(true);
    }

    private void newDialog(List<DataElement> elements)
    {
        Window owner = myToolbox.getUIRegistry().getMainFrameProvider().get();
        JFXDialog dialog = new JFXDialog(owner, "Feature Info");
        dialog.setFxNode(new BaseballPanel(myToolbox, elements));
        dialog.setSize(new Dimension(900, 600));
        dialog.setResizable(true);
        dialog.setLocationRelativeTo(owner);
        dialog.setModalityType(ModalityType.MODELESS);
        dialog.setVisible(true);
    }
}

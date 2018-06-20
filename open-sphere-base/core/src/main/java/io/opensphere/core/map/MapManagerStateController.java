package io.opensphere.core.map;

import java.util.Collection;
import java.util.Map.Entry;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.MapType;
import com.bitsys.fade.mist.state.v4.ProjectionType;
import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.core.MapManager;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.modulestate.AbstractModuleStateController;
import io.opensphere.core.modulestate.ModuleStateController;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.xml.MutableNamespaceContext;
import io.opensphere.core.viewer.Viewer.ViewerPosition;
import io.opensphere.core.viewer.impl.AbstractDynamicViewer;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.DynamicViewer.KMLCompatibleCamera;
import net.opengis.kml._220.AltitudeModeEnumType;
import net.opengis.kml._220.CameraType;
import net.opengis.kml._220.ObjectFactory;

/**
 * A state controller for a {@link MapManager}.
 */
public class MapManagerStateController extends AbstractModuleStateController
{
    /** The KML namespace. */
    private static final String KML_NAMESPACE = "http://www.opengis.net/kml/2.2";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MapManagerStateController.class);

    /** The map manager for which I manage state. */
    private final MapManager myMapManager;

    /**
     * Create a KML element.
     *
     * @param parent The parent for the element.
     * @param childName The name of the element.
     * @return The element.
     */
    static Node createKmlElement(Node parent, String childName)
    {
        return parent.appendChild(XMLUtilities.getDocument(parent).createElementNS(KML_NAMESPACE, "kml:" + childName));
    }

    /**
     * Constructor.
     *
     * @param mapManager The map manager for which I manage state.
     */
    public MapManagerStateController(MapManager mapManager)
    {
        myMapManager = mapManager;
    }

    @Override
    public void activateState(String id, String description, Collection<? extends String> tags, Node node)
    {
        try
        {
            XPath xpath = StateXML.newXPath();
            ((MutableNamespaceContext)xpath.getNamespaceContext()).addNamespace("kml", KML_NAMESPACE);

            // Get the projection from the saved state and set the projection in
            // the map manager.
            // Expecting either Equirectangular or Perspective
            String projectionType = xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:map/:projection", node);
            if (StringUtils.isNotEmpty(projectionType))
            {
                Class<? extends AbstractDynamicViewer> projectionClass = getProjectionClass(projectionType);
                if (projectionClass != null)
                {
                    myMapManager.setProjection(projectionClass);
                }
            }
            DynamicViewer viewer = myMapManager.getStandardViewer();

            // Create the camera from the node.
            String latString = xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:map/kml:Camera/kml:latitude", node);
            String lonString = xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:map/kml:Camera/kml:longitude", node);
            String altString = xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:map/kml:Camera/kml:altitude", node);
            double latD = Utilities.parseDouble(latString, 0.);
            double lonD = Utilities.parseDouble(lonString, 0.);
            double altM = Utilities.parseDouble(altString, 0.);
            LatLonAlt location = LatLonAlt.createFromDegreesMeters(latD, lonD, altM, ReferenceLevel.ELLIPSOID);

            String tiltString = xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:map/kml:Camera/kml:tilt", node);
            String headingString = xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:map/kml:Camera/kml:heading", node);
            String rollString = xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:map/kml:Camera/kml:roll", node);
            double tilt = Utilities.parseDouble(tiltString, 0.);
            double heading = Utilities.parseDouble(headingString, 0.);
            double roll = Utilities.parseDouble(rollString, 0.);

            KMLCompatibleCamera camera = new KMLCompatibleCamera(location, heading, tilt, roll);

            // Set the viewer position for the camera.
            ViewerPosition viewerPosition = viewer.getViewerPosition(camera);
            viewer.setPosition(viewerPosition);
        }
        catch (XPathExpressionException e)
        {
            LOGGER.error(e, e);
        }
    }

    @Override
    public void activateState(String id, String description, Collection<? extends String> tags, StateType state)
    {
        MapType map = state.getMap();
        if (map != null)
        {
            // Get the projection from the saved state and set the projection in
            // the map manager.
            // Expecting either Equirectangular or Perspective
            Class<? extends AbstractDynamicViewer> projectionClass = getProjectionClass(map.getProjection());
            if (projectionClass != null)
            {
                myMapManager.setProjection(projectionClass);
            }

            // Create the camera from the node.
            if (map.getCamera() != null)
            {
                double latD = doubleValue(map.getCamera().getLatitude());
                double lonD = doubleValue(map.getCamera().getLongitude());
                double altM = doubleValue(map.getCamera().getAltitude());
                double tilt = doubleValue(map.getCamera().getTilt());
                double heading = doubleValue(map.getCamera().getHeading());
                double roll = doubleValue(map.getCamera().getRoll());
                LatLonAlt location = LatLonAlt.createFromDegreesMeters(latD, lonD, altM, ReferenceLevel.ELLIPSOID);

                KMLCompatibleCamera camera = new KMLCompatibleCamera(location, heading, tilt, roll);

                // Set the viewer position for the camera.
                DynamicViewer viewer = myMapManager.getStandardViewer();
                viewer.setPosition(viewer.getViewerPosition(camera));
            }
        }
    }

    @Override
    public boolean canActivateState(Node node)
    {
        XPath xpath = StateXML.newXPath();
        ((MutableNamespaceContext)xpath.getNamespaceContext()).addNamespace("kml", KML_NAMESPACE);

        try
        {
            String projectionType = xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:map/:projection", node);
            if (getProjectionClass(projectionType) != null)
            {
                return true;
            }

            // Create the camera from the node.
            String latString = xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:map/kml:Camera/kml:latitude", node);
            String lonString = xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:map/kml:Camera/kml:longitude", node);
            String altString = xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:map/kml:Camera/kml:altitude", node);
            try
            {
                // Attempt parsing the values.
                Double.valueOf(latString);
                Double.valueOf(lonString);
                if (!StringUtils.isBlank(altString))
                {
                    Double.valueOf(altString);
                }
                return true;
            }
            catch (NumberFormatException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(e, e);
                }
                return false;
            }
        }
        catch (XPathExpressionException e)
        {
            LOGGER.error(e, e);
            return false;
        }
    }

    @Override
    public boolean canActivateState(StateType state)
    {
        return state.getMap() != null && (state.getMap().getProjection() != null || state.getMap().getCamera() != null);
    }

    @Override
    public boolean canSaveState()
    {
        return true;
    }

    @Override
    public void deactivateState(String id, Node node)
    {
    }

    @Override
    public void deactivateState(String id, StateType state)
    {
    }

    @Override
    public boolean isSaveStateByDefault()
    {
        return true;
    }

    @Override
    public void saveState(Node node)
    {
        Document doc = node instanceof Document ? (Document)node : node.getOwnerDocument();
        try
        {
            // Collect the state information to save.
            String projectionType = "3-D".equals(myMapManager.getProjection().getName()) ? "Perspective" : "Equirectangular";
            DynamicViewer viewer = myMapManager.getStandardViewer();
            ViewerPosition viewerPosition = viewer.getPosition();
            KMLCompatibleCamera camera = viewer.getCamera(viewerPosition);

            // The map node is the base of the tree for map manager states
            Node mapNode = StateXML.createChildNode(node, doc, node, "/" + ModuleStateController.STATE_QNAME + "/:map", "map");

            Node projectionNode = StateXML.createChildNode(node, doc, mapNode,
                    "/" + ModuleStateController.STATE_QNAME + "/:map/:projection", "projection");
            projectionNode.setTextContent(projectionType);

            Node cameraNode = MapManagerStateController.createKmlElement(mapNode, "Camera");

            Node latNode = MapManagerStateController.createKmlElement(cameraNode, "latitude");
            latNode.setTextContent(Double.toString(camera.getLocation().getLatD()));
            Node lonNode = MapManagerStateController.createKmlElement(cameraNode, "longitude");
            lonNode.setTextContent(Double.toString(camera.getLocation().getLonD()));
            Node altNode = MapManagerStateController.createKmlElement(cameraNode, "altitude");
            altNode.setTextContent(Double.toString(camera.getLocation().getAltM()));

            Node headingNode = MapManagerStateController.createKmlElement(cameraNode, "heading");
            headingNode.setTextContent(Double.toString(camera.getHeading()));
            Node tiltNode = MapManagerStateController.createKmlElement(cameraNode, "tilt");
            tiltNode.setTextContent(Double.toString(camera.getTilt()));
            Node rollNode = MapManagerStateController.createKmlElement(cameraNode, "roll");
            rollNode.setTextContent(Double.toString(camera.getRoll()));

            // We always use clamp to ground when restoring state, but include
            // it here in case an external reader looks for the value.
            Node altModeNode = MapManagerStateController.createKmlElement(cameraNode, "altitudeMode");
            altModeNode.setTextContent("clampToGround");
        }
        catch (XPathExpressionException e)
        {
            LOGGER.error(e, e);
        }
    }

    @Override
    public void saveState(StateType state)
    {
        MapType map = new MapType();
        state.setMap(map);

        // Collect the state information to save.
        ProjectionType projectionType = "3-D".equals(myMapManager.getProjection().getName()) ? ProjectionType.PERSPECTIVE
                : ProjectionType.EQUIRECTANGULAR;
        DynamicViewer viewer = myMapManager.getStandardViewer();
        KMLCompatibleCamera camera = viewer.getCamera(viewer.getPosition());

        map.setProjection(projectionType);

        map.setCamera(new CameraType());
        map.getCamera().setLatitude(Double.valueOf(camera.getLocation().getLatD()));
        map.getCamera().setLongitude(Double.valueOf(camera.getLocation().getLonD()));
        map.getCamera().setAltitude(Double.valueOf(camera.getLocation().getAltM()));
        map.getCamera().setHeading(Double.valueOf(camera.getHeading()));
        map.getCamera().setTilt(Double.valueOf(camera.getTilt()));
        map.getCamera().setRoll(Double.valueOf(camera.getRoll()));
        // We always use clamp to ground when restoring state, but include
        // it here in case an external reader looks for the value.
        map.getCamera().setAltitudeModeGroup(new ObjectFactory().createAltitudeMode(AltitudeModeEnumType.CLAMP_TO_GROUND));
    }

    /**
     * Given a projection type from a saved state, find the appropriate
     * projection class registered with the map manager.
     *
     * @param projectionType The projection type.
     * @return The projection class or {@code null} if none was found.
     */
    private Class<? extends AbstractDynamicViewer> getProjectionClass(String projectionType)
    {
        Class<? extends AbstractDynamicViewer> projectionClass = null;
        if (!StringUtils.isBlank(projectionType))
        {
            String projectionName = "Equirectangular".equals(projectionType) ? "Equirectangular" : "3-D";
            for (Entry<Projection, Class<? extends AbstractDynamicViewer>> projection : myMapManager.getProjections().entrySet())
            {
                if (projection.getKey().getName().equals(projectionName))
                {
                    projectionClass = projection.getValue();
                    break;
                }
            }
        }
        return projectionClass;
    }

    /**
     * Given a projection type from a saved state, find the appropriate
     * projection class registered with the map manager.
     *
     * @param projectionType The projection type.
     * @return The projection class or {@code null} if none was found.
     */
    private Class<? extends AbstractDynamicViewer> getProjectionClass(ProjectionType projectionType)
    {
        Class<? extends AbstractDynamicViewer> projectionClass = null;
        if (projectionType != null)
        {
            String projectionName = projectionType == ProjectionType.EQUIRECTANGULAR ? "Equirectangular" : "3-D";
            for (Entry<Projection, Class<? extends AbstractDynamicViewer>> projection : myMapManager.getProjections().entrySet())
            {
                if (projection.getKey().getName().equals(projectionName))
                {
                    projectionClass = projection.getValue();
                    break;
                }
            }
        }
        return projectionClass;
    }

    /**
     * Gets the double value or 0.
     *
     * @param value the value
     * @return the double value
     */
    private static double doubleValue(Double value)
    {
        return value != null ? value.doubleValue() : 0;
    }
}

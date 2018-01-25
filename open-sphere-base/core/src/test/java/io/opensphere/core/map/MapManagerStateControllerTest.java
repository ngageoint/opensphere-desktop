package io.opensphere.core.map;

import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.MapType;
import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.core.MapManager;
import io.opensphere.core.modulestate.ModuleStateController;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.viewer.impl.AbstractDynamicViewer;

/** Test for {@link MapManagerStateController}. */
public class MapManagerStateControllerTest
{
    /**
     * Test for {@link MapManagerStateController#canActivateState(Node)}.
     *
     * @throws ParserConfigurationException Configuration error.
     */
    @Test
    public void testCanActivateState() throws ParserConfigurationException
    {
        Document doc = XMLUtilities.newDocument();
        Node mapNode = doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME))
                .appendChild(StateXML.createElement(doc, "map"));

        Projection proj = EasyMock.createMock(Projection.class);
        EasyMock.expect(proj.getName()).andReturn("Equirectangular").anyTimes();
        EasyMock.replay(proj);

        Map<Projection, Class<? extends AbstractDynamicViewer>> projections = New.map();
        projections.put(proj, AbstractDynamicViewer.class);

        MapManager mapManager = EasyMock.createMock(MapManager.class);
        EasyMock.expect(mapManager.getProjections()).andReturn(projections).anyTimes();
        EasyMock.replay(mapManager);

        MapManagerStateController controller = new MapManagerStateController(mapManager);
        Assert.assertFalse(controller.canActivateState(doc));

        Node projectionNode = mapNode.appendChild(StateXML.createElement(doc, "projection"));
        Assert.assertFalse(controller.canActivateState(doc));

        projectionNode.setTextContent(" ");
        Assert.assertFalse(controller.canActivateState(doc));

        projectionNode.setTextContent("nonsense");
        Assert.assertFalse(controller.canActivateState(doc));

        projectionNode.setTextContent("Equirectangular");
        Assert.assertTrue(controller.canActivateState(doc));

        mapNode.removeChild(projectionNode);

        Node cameraNode = MapManagerStateController.createKmlElement(mapNode, "Camera");
        Assert.assertFalse(controller.canActivateState(doc));

        Node latNode = MapManagerStateController.createKmlElement(cameraNode, "latitude");
        latNode.setTextContent("35");
        Assert.assertFalse(controller.canActivateState(doc));

        Node lonNode = MapManagerStateController.createKmlElement(cameraNode, "longitude");
        lonNode.setTextContent("35");
        Assert.assertTrue(controller.canActivateState(doc));

        Node altNode = MapManagerStateController.createKmlElement(cameraNode, "altitude");
        altNode.setTextContent("35");
        Assert.assertTrue(controller.canActivateState(doc));

        // Try some bad values for latitude.
        latNode.setTextContent(" ");
        Assert.assertFalse(controller.canActivateState(doc));

        latNode.setTextContent("bad");
        Assert.assertFalse(controller.canActivateState(doc));

        latNode.setTextContent("-35");
        Assert.assertTrue(controller.canActivateState(doc));

        // Try some bad values for longitude.
        lonNode.setTextContent(" ");
        Assert.assertFalse(controller.canActivateState(doc));

        lonNode.setTextContent("bad");
        Assert.assertFalse(controller.canActivateState(doc));

        lonNode.setTextContent("-35");
        Assert.assertTrue(controller.canActivateState(doc));

        // Try some bad values for altitude.
        altNode.setTextContent(" ");
        Assert.assertTrue(controller.canActivateState(doc));

        altNode.setTextContent("bad");
        Assert.assertFalse(controller.canActivateState(doc));

        altNode.setTextContent("-35");
        Assert.assertTrue(controller.canActivateState(doc));
    }

    /**
     * Tests V4 code with incomplete values.
     */
    @Test
    public void testV4Incomplete()
    {
        MapManagerStateController controller = new MapManagerStateController(null);

        // Test completely empty
        StateType state = new StateType();
        Assert.assertFalse(controller.canActivateState(state));
        controller.activateState(null, null, null, state);

        // Test partially empty
        state.setMap(new MapType());
        Assert.assertFalse(controller.canActivateState(state));
        controller.activateState(null, null, null, state);
    }
}

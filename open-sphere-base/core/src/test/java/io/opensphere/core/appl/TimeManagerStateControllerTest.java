package io.opensphere.core.appl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.StateType;
import com.bitsys.fade.mist.state.v4.TimeType;

import io.opensphere.core.AnimationManager;
import io.opensphere.core.TimeManager;
import io.opensphere.core.TimeManager.Fade;
import io.opensphere.core.matchers.EasyMockHelper;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.modulestate.ModuleStateController;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.units.duration.Hours;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.units.duration.Minutes;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;

/** Tests for {@link TimeManagerStateController}. */
public class TimeManagerStateControllerTest
{
    /**
     * Test for
     * {@link TimeManagerStateController#activateState(String, String, Collection, Node)}
     * .
     *
     * @throws ParserConfigurationException If the test fails.
     * @throws XPathExpressionException If the test fails.
     */
    @Test
    public void testActivateState() throws ParserConfigurationException, XPathExpressionException
    {
        TimeManager timeManager = EasyMock.createMock(TimeManager.class);
        TimeSpan timeSpan1 = TimeSpan.get(new Date(1389905587000L), Hours.ONE);
        timeManager.setPrimaryActiveTimeSpan(EasyMock.eq(timeSpan1));
        TimeSpan timeSpan2 = TimeSpan.get(new Date(1390005587000L), Hours.ONE);
        timeManager.setSecondaryActiveTimeSpans(EasyMock.eq("heldkey1"), EasyMockHelper.eq(Collections.singleton(timeSpan2)));
        TimeSpan timeSpan3 = TimeSpan.get(new Date(1390015587000L), Hours.ONE);
        TimeSpan timeSpan4 = TimeSpan.get(new Date(1390025587000L), Hours.ONE);
        timeManager.setSecondaryActiveTimeSpans(EasyMock.eq("heldkey2"), EasyMockHelper.eq(Arrays.asList(timeSpan3, timeSpan4)));
        TimeSpan timeSpan5 = TimeSpan.get(new Date(1390035587000L), Hours.ONE);
        timeManager.setSecondaryActiveTimeSpans(EasyMock.eq(TimeManager.WILDCARD_CONSTRAINT_KEY),
                EasyMockHelper.eq(Collections.singleton(timeSpan5)));
        Fade fade = new DefaultFade(new Milliseconds(100), new Milliseconds(150));
        timeManager.setFade(EasyMock.eq(fade));
        EasyMock.replay(timeManager);

        AnimationManager animationManager = EasyMock.createMock(AnimationManager.class);

        Document doc = XMLUtilities.newDocument();
        Node timeNode = doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME))
                .appendChild(StateXML.createElement(doc, "time"));
        timeNode.appendChild(StateXML.createElement(doc, "current")).setTextContent(timeSpan1.toISO8601String());
        Node heldNode = timeNode.appendChild(StateXML.createElement(doc, "held"));
        heldNode.appendChild(StateXML.createElement(doc, "key")).setTextContent("heldkey1");
        heldNode.appendChild(StateXML.createElement(doc, "interval")).setTextContent(timeSpan2.toISO8601String());
        heldNode = timeNode.appendChild(StateXML.createElement(doc, "held"));
        heldNode.appendChild(StateXML.createElement(doc, "key")).setTextContent("heldkey2");
        heldNode.appendChild(StateXML.createElement(doc, "interval")).setTextContent(timeSpan3.toISO8601String());
        heldNode.appendChild(StateXML.createElement(doc, "interval")).setTextContent(timeSpan4.toISO8601String());
        heldNode = timeNode.appendChild(StateXML.createElement(doc, "held"));
        heldNode.appendChild(StateXML.createElement(doc, "interval")).setTextContent(timeSpan5.toISO8601String());
        Node fadeNode = timeNode.appendChild(StateXML.createElement(doc, "fade"));
        fadeNode.appendChild(StateXML.createElement(doc, "in")).setTextContent("P0.100S");
        fadeNode.appendChild(StateXML.createElement(doc, "out")).setTextContent("P0.150S");

        TimeManagerStateController controller = new TimeManagerStateController(timeManager, animationManager);
        controller.activateState("test", null, null, doc.getDocumentElement());

        EasyMock.verify(timeManager);
    }

    /**
     * Test for {@link TimeManagerStateController#canActivateState(Node)}.
     *
     * @throws ParserConfigurationException If the test fails.
     * @throws XPathExpressionException If the test fails.
     */
    @Test
    public void testCanActivateState() throws ParserConfigurationException, XPathExpressionException
    {
        TimeManager timeManager = EasyMock.createNiceMock(TimeManager.class);
        AnimationManager animationManager = EasyMock.createNiceMock(AnimationManager.class);
        EasyMock.replay(timeManager, animationManager);

        Document doc = XMLUtilities.newDocument();
        Node timeNode = doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME))
                .appendChild(StateXML.createElement(doc, "time"));

        TimeManagerStateController controller = new TimeManagerStateController(timeManager, animationManager);
        Assert.assertFalse(controller.canActivateState(doc));

        Node currentNode = timeNode.appendChild(StateXML.createElement(doc, "current"));
        currentNode.setTextContent("2003-03-18T00:00:00");
        Assert.assertTrue(controller.canActivateState(doc));

        timeNode.removeChild(currentNode);
        Assert.assertFalse(controller.canActivateState(doc));

        Node heldNode = timeNode.appendChild(StateXML.createElement(doc, "held"));
        heldNode.appendChild(StateXML.createElement(doc, "key")).setTextContent("heldkey");
        Assert.assertTrue(controller.canActivateState(doc));

        timeNode.removeChild(heldNode);

        Node fadeNode = timeNode.appendChild(StateXML.createElement(doc, "fade"));
        Node inNode = fadeNode.appendChild(StateXML.createElement(doc, "in"));
        inNode.setTextContent("P0.100S");
        Assert.assertTrue(controller.canActivateState(doc));

        fadeNode.removeChild(inNode);
        Node outNode = fadeNode.appendChild(StateXML.createElement(doc, "out"));
        outNode.setTextContent("P0.150S");
        Assert.assertTrue(controller.canActivateState(doc));

        fadeNode.removeChild(outNode);
        Assert.assertFalse(controller.canActivateState(doc));
    }

    /**
     * Test for {@link TimeManagerStateController#saveState(Node)}.
     *
     * @throws ParserConfigurationException If the test fails.
     * @throws XPathExpressionException If the test fails.
     */
    @Test
    public void testSaveState() throws ParserConfigurationException, XPathExpressionException
    {
        TimeManager timeManager = EasyMock.createMock(TimeManager.class);
        TimeSpan timeSpan1 = TimeSpan.get(new Date(), Hours.ONE);
        EasyMock.expect(timeManager.getPrimaryActiveTimeSpans()).andReturn(TimeSpanList.singleton(timeSpan1));

        Map<Object, Collection<? extends TimeSpan>> map = New.insertionOrderMap();
        TimeSpan timeSpan2 = TimeSpan.get(new Date(1390005587000L), Hours.ONE);
        map.put("heldkey1", Collections.singleton(timeSpan2));
        TimeSpan timeSpan3 = TimeSpan.get(new Date(1390015587000L), Hours.ONE);
        TimeSpan timeSpan4 = TimeSpan.get(new Date(1390025587000L), Hours.ONE);
        map.put("heldkey2", Arrays.asList(timeSpan3, timeSpan4));
        TimeSpan timeSpan5 = TimeSpan.get(new Date(1390035587000L), Hours.ONE);
        map.put(TimeManager.WILDCARD_CONSTRAINT_KEY, Collections.singleton(timeSpan5));
        EasyMock.expect(timeManager.getSecondaryActiveTimeSpans()).andReturn(map);

        Fade fade = new DefaultFade(new Milliseconds(100), new Milliseconds(150));
        EasyMock.expect(timeManager.getFade()).andReturn(fade);
        EasyMock.replay(timeManager);

        AnimationManager animationManager = EasyMock.createNiceMock(AnimationManager.class);
        EasyMock.replay(animationManager);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME));

        TimeManagerStateController controller = new TimeManagerStateController(timeManager, animationManager);
        controller.saveState(doc.getDocumentElement());

        XPath xpath = StateXML.newXPath();
        Assert.assertEquals(timeSpan1.toISO8601String(),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:current", doc));

        Assert.assertEquals("heldkey1", xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:held[1]/:key", doc));
        Assert.assertEquals(timeSpan2.toISO8601String(),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:held[1]/:interval", doc));
        Assert.assertEquals("heldkey2", xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:held[2]/:key", doc));
        Assert.assertEquals(timeSpan3.toISO8601String(),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:held[2]/:interval[1]", doc));
        Assert.assertEquals(timeSpan4.toISO8601String(),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:held[2]/:interval[2]", doc));
        Assert.assertEquals("", xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:held[3]/:key", doc));
        Assert.assertEquals(timeSpan5.toISO8601String(),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:held[3]/:interval[1]", doc));

        Assert.assertEquals(fade.getFadeIn().toISO8601String(),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:fade/:in", doc));
        Assert.assertEquals(fade.getFadeOut().toISO8601String(),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:fade/:out", doc));

        EasyMock.verify(timeManager);
    }

    /**
     * Tests V4 code.
     */
    @Test
    public void testV4()
    {
        TestTimeManager timeManager = new TestTimeManager();
        TestAnimationManager animationManager = new TestAnimationManager();
        TimeManagerStateController controller = new TimeManagerStateController(timeManager, animationManager);

        timeManager.setPrimaryActiveTimeSpan(TimeSpan.get(100, 200));
        timeManager.setSecondaryActiveTimeSpans("key", Collections.singletonList(TimeSpan.get(200, 300)));
        timeManager.setSecondaryActiveTimeSpans(TimeManager.WILDCARD_CONSTRAINT_KEY,
                Collections.singletonList(TimeSpan.get(300, 400)));
        timeManager.setFade(new DefaultFade(new Minutes(5), new Hours(6)));

        StateType state = new StateType();
        controller.saveState(state);

        timeManager.setPrimaryActiveTimeSpan(null);
        timeManager.getSecondaryActiveTimeSpans().clear();
        timeManager.setFade(null);

        Assert.assertTrue(controller.canActivateState(state));
        controller.activateState(null, null, null, state);

        Assert.assertEquals(TimeSpan.get(100, 200), timeManager.getPrimaryActiveTimeSpans().get(0));
        Assert.assertEquals(TimeSpan.get(200, 300), timeManager.getSecondaryActiveTimeSpans("key").iterator().next());
        Assert.assertEquals(TimeSpan.get(300, 400),
                timeManager.getSecondaryActiveTimeSpans(TimeManager.WILDCARD_CONSTRAINT_KEY).iterator().next());
        Assert.assertEquals(new DefaultFade(new Minutes(5), new Hours(6)), timeManager.getFade());
    }

    /**
     * Tests V4 code with incomplete values.
     */
    @Test
    public void testV4Incomplete()
    {
        TimeManagerStateController controller = new TimeManagerStateController(new TestTimeManager(), new TestAnimationManager());

        // Test completely empty
        StateType state = new StateType();
        Assert.assertFalse(controller.canActivateState(state));
        controller.activateState(null, null, null, state);

        // Test partially empty
        state.setTime(new TimeType());
        Assert.assertFalse(controller.canActivateState(state));
        controller.activateState(null, null, null, state);
    }
}

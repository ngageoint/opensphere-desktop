package io.opensphere.core.appl;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.StateType;
import com.bitsys.fade.mist.state.v4.TimeAnimationType;
import com.bitsys.fade.mist.state.v4.TimeType;

import io.opensphere.core.AnimationManager;
import io.opensphere.core.TimeManager;
import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.animation.AnimationPlan.EndBehavior;
import io.opensphere.core.animation.AnimationPlanModificationException;
import io.opensphere.core.animation.AnimationState;
import io.opensphere.core.animation.AnimationState.Direction;
import io.opensphere.core.animation.ContinuousAnimationPlan;
import io.opensphere.core.animation.impl.DefaultAnimationPlan;
import io.opensphere.core.animation.impl.DefaultContinuousAnimationPlan;
import io.opensphere.core.animation.impl.DefaultContinuousAnimationState;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.modulestate.ModuleStateController;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Hours;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.units.duration.Minutes;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;

/** Test {@link AnimationManagerStateController}. */
public class AnimationManagerStateControllerTest
{
    /** Animation string. */
    private static final String ANIMATION = "animation";

    /** Interval element name. */
    private static final String INTERVAL = "interval";

    /** Test string. */
    private static final String TEST = "test";

    /** Time tag. */
    private static final String TIME_TAG = "time";

    /**
     * Test
     * {@link AnimationManagerStateController#activateState(String, String, java.util.Collection, org.w3c.dom.Node)}
     * with a continuous plan.
     *
     * @throws ParserConfigurationException If the test fails.
     * @throws XPathExpressionException If the test fails.
     * @throws AnimationPlanModificationException Impossible.
     */
    @Test
    public void testActivateStateContinuous()
        throws ParserConfigurationException, XPathExpressionException, AnimationPlanModificationException
    {
        final List<TimeSpan> sequence = getSequence();
        TimeSpan current = TimeSpan.get(sequence.get(1).getStartDate(), new Minutes(30));

        Document doc = XMLUtilities.newDocument();
        Node timeNode = doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME))
                .appendChild(StateXML.createElement(doc, TIME_TAG));
        timeNode.appendChild(StateXML.createElement(doc, "current")).setTextContent(current.toISO8601String());
        timeNode.appendChild(StateXML.createElement(doc, "advance")).setTextContent("PT10M");
        Node sequenceNode = timeNode.appendChild(StateXML.createElement(doc, "sequence"));
        sequenceNode.appendChild(StateXML.createElement(doc, INTERVAL)).setTextContent(sequence.get(0).toISO8601String());
        sequenceNode.appendChild(StateXML.createElement(doc, INTERVAL)).setTextContent(sequence.get(1).toISO8601String());
        sequenceNode.appendChild(StateXML.createElement(doc, INTERVAL)).setTextContent(sequence.get(2).toISO8601String());

        Node animationNode = timeNode.appendChild(StateXML.createElement(doc, ANIMATION));
        animationNode.appendChild(StateXML.createElement(doc, "loopBehavior")).setTextContent("taperEndTaperStart");
        animationNode.appendChild(StateXML.createElement(doc, "loop"))
                .setTextContent(sequence.get(0).simpleUnion(sequence.get(2)).toISO8601String());
        animationNode.appendChild(StateXML.createElement(doc, "millisPerFrame")).setTextContent("200");
        animationNode.appendChild(StateXML.createElement(doc, "playState")).setTextContent("Forward");

        AnimationManager animationManager = EasyMock.createMock(AnimationManager.class);

        Capture<AnimationPlan> planCapture = EasyMock.newCapture();
        Capture<AnimationState> stateCapture = EasyMock.newCapture();
        animationManager.setPlan(EasyMock.and(EasyMock.isA(ContinuousAnimationPlan.class), EasyMock.capture(planCapture)),
                EasyMock.capture(stateCapture), EasyMock.eq(Direction.FORWARD), EasyMock.cmpEq(new Milliseconds(200)));
        EasyMock.replay(animationManager);

        TimeManager timeManager = EasyMock.createMock(TimeManager.class);
        EasyMock.replay(timeManager);

        AnimationManagerStateController controller = new AnimationManagerStateController(animationManager, timeManager);

        controller.activateState(TEST, null, null, doc.getDocumentElement());

        EasyMock.verify(animationManager);

        ContinuousAnimationPlan capturedPlan = (ContinuousAnimationPlan)planCapture.getValue();
        AnimationState capturedState = stateCapture.getValue();

        Assert.assertEquals(AnimationState.Direction.FORWARD, capturedState.getDirection());

        Assert.assertEquals(new Minutes(10), capturedPlan.getAdvanceDuration());
        // For micro animation the sequence will be combined before the plan is
        // created.
        List<? extends TimeSpan> microSequence = AnimationManagerStateController.getCombinedSequence(sequence);
        Assert.assertTrue(capturedPlan.getAnimationSequence().equals(microSequence));
        Assert.assertEquals(current, capturedPlan.getTimeSpanForState(capturedState));

        EasyMock.reset(animationManager);
        animationManager.setPlan(EasyMock.and(EasyMock.isA(ContinuousAnimationPlan.class), EasyMock.capture(planCapture)),
                EasyMock.capture(stateCapture), EasyMock.eq(Direction.BACKWARD), EasyMock.cmpEq(new Milliseconds(300)));
        EasyMock.replay(animationManager);

        XPath xpath = StateXML.newXPath();
        ((Node)xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:animation/:millisPerFrame", doc,
                XPathConstants.NODE)).setTextContent("300");
        ((Node)xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:animation/:playState", doc, XPathConstants.NODE))
                .setTextContent("Reverse");
        TimeSpan limit = TimeSpan.get(sequence.get(1).getMidpointInstant(), sequence.get(2).getMidpointInstant());
        ((Node)xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:animation/:loop", doc, XPathConstants.NODE))
                .setTextContent(limit.toISO8601String());
        controller.activateState(TEST, null, null, doc.getDocumentElement());

        EasyMock.verify(animationManager);

        capturedPlan = (ContinuousAnimationPlan)planCapture.getValue();
        Assert.assertEquals(limit, capturedPlan.getLimitWindow());
    }

    /**
     * Test
     * {@link AnimationManagerStateController#activateState(String, String, java.util.Collection, org.w3c.dom.Node)}
     * with a default plan.
     *
     * @throws ParserConfigurationException If the test fails.
     * @throws XPathExpressionException If the test fails.
     * @throws AnimationPlanModificationException Impossible.
     */
    @Test
    public void testActivateStateDefault()
        throws ParserConfigurationException, XPathExpressionException, AnimationPlanModificationException
    {
        final List<TimeSpan> sequence = getSequence();

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node timeNode = doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME))
                .appendChild(StateXML.createElement(doc, TIME_TAG));
        timeNode.appendChild(StateXML.createElement(doc, "advance")).setTextContent("PT3600S");
        Node sequenceNode = timeNode.appendChild(StateXML.createElement(doc, "sequence"));
        sequenceNode.appendChild(StateXML.createElement(doc, INTERVAL)).setTextContent(sequence.get(0).toISO8601String());
        sequenceNode.appendChild(StateXML.createElement(doc, INTERVAL)).setTextContent(sequence.get(1).toISO8601String());
        sequenceNode.appendChild(StateXML.createElement(doc, INTERVAL)).setTextContent(sequence.get(2).toISO8601String());

        Node animationNode = timeNode.appendChild(StateXML.createElement(doc, ANIMATION));
        animationNode.appendChild(StateXML.createElement(doc, "loopBehavior")).setTextContent("taperEndTaperStart");
        animationNode.appendChild(StateXML.createElement(doc, "loop"))
                .setTextContent(sequence.get(0).simpleUnion(sequence.get(2)).toISO8601String());
        animationNode.appendChild(StateXML.createElement(doc, "millisPerFrame")).setTextContent("200");
        animationNode.appendChild(StateXML.createElement(doc, "playState")).setTextContent("Stop");

        AnimationManager animationManager = EasyMock.createMock(AnimationManager.class);

        Capture<AnimationPlan> planCapture = EasyMock.newCapture();
        Capture<AnimationState> stateCapture = EasyMock.newCapture();
        animationManager.setPlan(EasyMock.capture(planCapture), EasyMock.capture(stateCapture), (Direction)EasyMock.eq(null),
                EasyMock.cmpEq(new Milliseconds(200)));
        EasyMock.replay(animationManager);

        TimeManager timeManager = EasyMock.createMock(TimeManager.class);
        EasyMock.replay(timeManager);

        AnimationManagerStateController controller = new AnimationManagerStateController(animationManager, timeManager);

        controller.activateState(TEST, null, null, doc.getDocumentElement());

        EasyMock.verify(animationManager);

        AnimationPlan capturedPlan = planCapture.getValue();
        AnimationState capturedState = stateCapture.getValue();

        Assert.assertEquals(AnimationState.Direction.FORWARD, capturedState.getDirection());
        Assert.assertFalse(capturedPlan instanceof ContinuousAnimationPlan);

        Assert.assertTrue(capturedPlan.getAnimationSequence().equals(sequence));
        Assert.assertEquals(sequence.get(0), capturedPlan.getTimeSpanForState(capturedState));
    }

    /**
     * Test
     * {@link AnimationManagerStateController#activateState(String, String, java.util.Collection, org.w3c.dom.Node)}
     * with a plan with the minimum information.
     *
     * @throws ParserConfigurationException If the test fails.
     * @throws XPathExpressionException If the test fails.
     * @throws AnimationPlanModificationException Impossible.
     */
    @Test
    public void testActivateStateMinimalData()
        throws ParserConfigurationException, XPathExpressionException, AnimationPlanModificationException
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        TimeSpan current = TimeSpan.get(cal.getTime(), Hours.ONE);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node timeNode = doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME))
                .appendChild(StateXML.createElement(doc, TIME_TAG));
        timeNode.appendChild(StateXML.createElement(doc, "current")).setTextContent(current.toISO8601String());

        Node animationNode = timeNode.appendChild(StateXML.createElement(doc, ANIMATION));
        animationNode.appendChild(StateXML.createElement(doc, "playState")).setTextContent("Stop");

        AnimationManager animationManager = EasyMock.createMock(AnimationManager.class);

        Capture<AnimationPlan> planCapture = EasyMock.newCapture();
        Capture<AnimationState> stateCapture = EasyMock.newCapture();
        animationManager.setPlan(EasyMock.capture(planCapture), EasyMock.capture(stateCapture), (Direction)EasyMock.eq(null),
                EasyMock.cmpEq(new Milliseconds(500)));
        EasyMock.replay(animationManager);

        TimeManager timeManager = EasyMock.createMock(TimeManager.class);
        EasyMock.replay(timeManager);

        AnimationManagerStateController controller = new AnimationManagerStateController(animationManager, timeManager);

        controller.activateState(TEST, null, null, doc.getDocumentElement());

        EasyMock.verify(animationManager);

        AnimationPlan capturedPlan = planCapture.getValue();
        AnimationState capturedState = stateCapture.getValue();

        Assert.assertEquals(AnimationState.Direction.FORWARD, capturedState.getDirection());
        Assert.assertFalse(capturedPlan instanceof ContinuousAnimationPlan);

        List<TimeSpan> expectedSequence = Collections.singletonList(TimeSpan.get(cal.getTime(), Hours.ONE));
        Assert.assertTrue(capturedPlan.getAnimationSequence().equals(expectedSequence));
        Assert.assertEquals(expectedSequence.get(0), capturedPlan.getTimeSpanForState(capturedState));
    }

    /**
     * Test
     * {@link AnimationManagerStateController#activateState(String, String, java.util.Collection, org.w3c.dom.Node)}
     * with no plan and no current time.
     *
     * @throws ParserConfigurationException If the test fails.
     * @throws XPathExpressionException If the test fails.
     * @throws AnimationPlanModificationException Impossible.
     */
    @Test
    public void testActivateStateNoAnimationNoTime()
        throws ParserConfigurationException, XPathExpressionException, AnimationPlanModificationException
    {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node timeNode = doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME))
                .appendChild(StateXML.createElement(doc, TIME_TAG));

        timeNode.appendChild(StateXML.createElement(doc, ANIMATION));

        AnimationManager animationManager = EasyMock.createMock(AnimationManager.class);
        animationManager.abandonPlan();
        EasyMock.replay(animationManager);

        TimeManager timeManager = EasyMock.createMock(TimeManager.class);
        timeManager.setPrimaryActiveTimeSpan(TimeSpan.ZERO);
        EasyMock.replay(timeManager);

        AnimationManagerStateController controller = new AnimationManagerStateController(animationManager, timeManager);

        controller.activateState(TEST, null, null, doc.getDocumentElement());

        EasyMock.verify(animationManager, timeManager);
    }

    /**
     * Test
     * {@link AnimationManagerStateController#activateState(String, String, java.util.Collection, org.w3c.dom.Node)}
     * with no plan but with a current time.
     *
     * @throws ParserConfigurationException If the test fails.
     * @throws XPathExpressionException If the test fails.
     * @throws AnimationPlanModificationException Impossible.
     */
    @Test
    public void testActivateStateNoAnimationWithTime()
        throws ParserConfigurationException, XPathExpressionException, AnimationPlanModificationException
    {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node timeNode = doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME))
                .appendChild(StateXML.createElement(doc, TIME_TAG));

        TimeSpan timeSpan = TimeSpan.get(new Date(1389905587000L), Hours.ONE);
        Node currentNode = timeNode.appendChild(StateXML.createElement(doc, "current"));
        currentNode.setTextContent(timeSpan.toISO8601String());

        timeNode.appendChild(StateXML.createElement(doc, ANIMATION));

        AnimationManager animationManager = EasyMock.createMock(AnimationManager.class);
        animationManager.abandonPlan();
        EasyMock.replay(animationManager);

        TimeManager timeManager = EasyMock.createMock(TimeManager.class);
        EasyMock.replay(timeManager);

        AnimationManagerStateController controller = new AnimationManagerStateController(animationManager, timeManager);

        controller.activateState(TEST, null, null, doc.getDocumentElement());

        EasyMock.verify(animationManager, timeManager);
    }

    /**
     * Test
     * {@link AnimationManagerStateController#activateState(String, String, java.util.Collection, org.w3c.dom.Node)}
     * with a plan that has a non-standard loop duration.
     *
     * @throws ParserConfigurationException If the test fails.
     * @throws XPathExpressionException If the test fails.
     * @throws AnimationPlanModificationException Impossible.
     */
    @Test
    public void testActivateStateShortLoop()
        throws ParserConfigurationException, XPathExpressionException, AnimationPlanModificationException
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        TimeSpan loop = TimeSpan.get(cal.getTime(), new Seconds(Constants.SECONDS_PER_DAY - 1));

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node timeNode = doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME))
                .appendChild(StateXML.createElement(doc, TIME_TAG));

        Node animationNode = timeNode.appendChild(StateXML.createElement(doc, ANIMATION));
        animationNode.appendChild(StateXML.createElement(doc, "loop")).setTextContent(loop.toISO8601String());

        AnimationManager animationManager = EasyMock.createMock(AnimationManager.class);

        Capture<AnimationPlan> planCapture = EasyMock.newCapture();
        Capture<AnimationState> stateCapture = EasyMock.newCapture();
        animationManager.setPlan(EasyMock.capture(planCapture), EasyMock.capture(stateCapture), (Direction)EasyMock.eq(null),
                EasyMock.cmpEq(new Milliseconds(500)));
        EasyMock.replay(animationManager);

        TimeManager timeManager = EasyMock.createMock(TimeManager.class);
        EasyMock.replay(timeManager);

        AnimationManagerStateController controller = new AnimationManagerStateController(animationManager, timeManager);

        controller.activateState(TEST, null, null, doc.getDocumentElement());

        EasyMock.verify(animationManager);

        AnimationPlan capturedPlan = planCapture.getValue();
        AnimationState capturedState = stateCapture.getValue();

        Assert.assertEquals(AnimationState.Direction.FORWARD, capturedState.getDirection());
        Assert.assertFalse(capturedPlan instanceof ContinuousAnimationPlan);

        List<TimeSpan> expectedSequence = Collections.singletonList(TimeSpan.get(cal.getTime(), Days.ONE));
        Assert.assertTrue(capturedPlan.getAnimationSequence().equals(expectedSequence));
        Assert.assertEquals(expectedSequence.get(0), capturedPlan.getTimeSpanForState(capturedState));
    }

    /**
     * Test {@link AnimationManagerStateController#canActivateState(Node)}.
     *
     * @throws ParserConfigurationException If the test fails.
     * @throws XPathExpressionException If the test fails.
     * @throws AnimationPlanModificationException Impossible.
     */
    @Test
    public void testCanActivateStateDefault()
        throws ParserConfigurationException, XPathExpressionException, AnimationPlanModificationException
    {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node stateNode = doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME));
        Node timeNode = stateNode.appendChild(StateXML.createElement(doc, TIME_TAG));

        AnimationManager animationManager = EasyMock.createMock(AnimationManager.class);
        EasyMock.replay(animationManager);

        TimeManager timeManager = EasyMock.createMock(TimeManager.class);
        EasyMock.replay(timeManager);

        AnimationManagerStateController controller = new AnimationManagerStateController(animationManager, timeManager);

        Assert.assertFalse(controller.canActivateState(doc));

        timeNode.appendChild(StateXML.createElement(doc, ANIMATION));
        Assert.assertTrue(controller.canActivateState(doc));
    }

    /**
     * Test {@link AnimationManagerStateController#saveState(org.w3c.dom.Node)}
     * when no animation is active.
     *
     * @throws ParserConfigurationException If the test fails.
     * @throws XPathExpressionException If the test fails.
     */
    @Test
    public void testSaveNoAnimation() throws ParserConfigurationException, XPathExpressionException
    {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME));
        Node node = doc.getDocumentElement();
        StateXML.createChildNode(node, doc, node, "/" + ModuleStateController.STATE_QNAME + "/:time", TIME_TAG);

        AnimationPlan plan = null;
        AnimationManager animationManager = EasyMock.createMock(AnimationManager.class);

        EasyMock.reset(animationManager);
        EasyMock.expect(animationManager.getCurrentPlan()).andReturn(plan);
        EasyMock.replay(animationManager);

        TimeManager timeManager = EasyMock.createMock(TimeManager.class);
        EasyMock.replay(timeManager);

        AnimationManagerStateController controller = new AnimationManagerStateController(animationManager, timeManager);
        controller.saveState(node);

        XPath xpath = StateXML.newXPath();
        Assert.assertEquals("", xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:animation", doc));
    }

    /**
     * Test {@link AnimationManagerStateController#saveState(org.w3c.dom.Node)}
     * with a continuous plan.
     *
     * @throws ParserConfigurationException If the test fails.
     * @throws XPathExpressionException If the test fails.
     */
    @Test
    public void testSaveStateContinuous() throws ParserConfigurationException, XPathExpressionException
    {
        List<TimeSpan> sequence = getSequence();

        EndBehavior endBehavior = AnimationPlan.EndBehavior.WRAP;

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME));

        Duration activeWindowDuration = new Minutes(30);
        Duration advanceDuration = new Minutes(10);
        TimeSpan limitWindow = null;
        AnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, activeWindowDuration, advanceDuration, endBehavior,
                limitWindow);
        AnimationState animationState = new DefaultContinuousAnimationState(0,
                TimeSpan.get(sequence.get(1).getStart(), activeWindowDuration), Direction.FORWARD);

        AnimationManager animationManager = EasyMock.createMock(AnimationManager.class);

        EasyMock.reset(animationManager);
        EasyMock.expect(animationManager.getCurrentPlan()).andReturn(plan);
        EasyMock.expect(Boolean.valueOf(animationManager.isPlaying())).andReturn(Boolean.TRUE);
        EasyMock.expect(animationManager.getChangeRate()).andReturn(new Milliseconds(200));
        EasyMock.expect(animationManager.getAnimationState()).andReturn(animationState);
        EasyMock.replay(animationManager);

        TimeManager timeManager = EasyMock.createMock(TimeManager.class);
        EasyMock.replay(timeManager);

        AnimationManagerStateController controller = new AnimationManagerStateController(animationManager, timeManager);
        controller.saveState(doc.getDocumentElement());

        XPath xpath = StateXML.newXPath();
        Assert.assertEquals("PT10M", xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:advance", doc));
        Assert.assertEquals(sequence.get(0).toISO8601String(),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:sequence/:interval[1]", doc));
        Assert.assertEquals(sequence.get(1).toISO8601String(),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:sequence/:interval[2]", doc));
        Assert.assertEquals(sequence.get(2).toISO8601String(),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:sequence/:interval[3]", doc));
        Assert.assertEquals("taperEndTaperStart",
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:animation/:loopBehavior", doc));
        Assert.assertNull(
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:animation/:loop", doc, XPathConstants.NODE));
        Assert.assertEquals("200",
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:animation/:millisPerFrame", doc));
        Assert.assertEquals("Forward",
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:animation/:playState", doc));
    }

    /**
     * Test {@link AnimationManagerStateController#saveState(org.w3c.dom.Node)}
     * with a continuous plan with a limit interval.
     *
     * @throws ParserConfigurationException If the test fails.
     * @throws XPathExpressionException If the test fails.
     */
    @Test
    public void testSaveStateContinuousWithLimit() throws ParserConfigurationException, XPathExpressionException
    {
        List<TimeSpan> sequence = getSequence();

        EndBehavior endBehavior = AnimationPlan.EndBehavior.WRAP;

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME));

        Duration activeWindowDuration = new Minutes(30);
        Duration advanceDuration = new Minutes(10);
        TimeSpan limitWindow = sequence.get(1);
        AnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, activeWindowDuration, advanceDuration, endBehavior,
                limitWindow);
        AnimationState animationState = new DefaultContinuousAnimationState(0,
                TimeSpan.get(sequence.get(1).getStart(), activeWindowDuration), Direction.FORWARD);

        AnimationManager animationManager = EasyMock.createMock(AnimationManager.class);

        EasyMock.reset(animationManager);
        EasyMock.expect(animationManager.getCurrentPlan()).andReturn(plan);
        EasyMock.expect(Boolean.valueOf(animationManager.isPlaying())).andReturn(Boolean.TRUE);
        EasyMock.expect(animationManager.getChangeRate()).andReturn(new Milliseconds(200));
        EasyMock.expect(animationManager.getAnimationState()).andReturn(animationState);
        EasyMock.replay(animationManager);

        TimeManager timeManager = EasyMock.createMock(TimeManager.class);
        EasyMock.replay(timeManager);

        AnimationManagerStateController controller = new AnimationManagerStateController(animationManager, timeManager);
        controller.saveState(doc.getDocumentElement());

        XPath xpath = StateXML.newXPath();
        Assert.assertEquals("PT10M", xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:advance", doc));
        Assert.assertEquals(sequence.get(0).toISO8601String(),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:sequence/:interval[1]", doc));
        Assert.assertEquals(sequence.get(1).toISO8601String(),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:sequence/:interval[2]", doc));
        Assert.assertEquals(sequence.get(2).toISO8601String(),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:sequence/:interval[3]", doc));
        Assert.assertEquals("taperEndTaperStart",
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:animation/:loopBehavior", doc));
        Assert.assertEquals(limitWindow.toISO8601String(),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:animation/:loop", doc));
        Assert.assertEquals("200",
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:animation/:millisPerFrame", doc));
        Assert.assertEquals("Forward",
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:animation/:playState", doc));
    }

    /**
     * Test {@link AnimationManagerStateController#saveState(org.w3c.dom.Node)}
     * with a default plan.
     *
     * @throws ParserConfigurationException If the test fails.
     * @throws XPathExpressionException If the test fails.
     */
    @Test
    public void testSaveStateDefault() throws ParserConfigurationException, XPathExpressionException
    {
        List<TimeSpan> sequence = getSequence();

        EndBehavior endBehavior = AnimationPlan.EndBehavior.WRAP;
        AnimationPlan plan = new DefaultAnimationPlan(sequence, endBehavior);

        AnimationManager animationManager = EasyMock.createMock(AnimationManager.class);
        EasyMock.expect(animationManager.getCurrentPlan()).andReturn(plan);
        EasyMock.expect(Boolean.valueOf(animationManager.isPlaying())).andReturn(Boolean.FALSE);
        EasyMock.expect(animationManager.getChangeRate()).andReturn(new Milliseconds(200));
        EasyMock.replay(animationManager);

        TimeManager timeManager = EasyMock.createMock(TimeManager.class);
        EasyMock.replay(timeManager);

        AnimationManagerStateController controller = new AnimationManagerStateController(animationManager, timeManager);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        doc.appendChild(StateXML.createElement(doc, ModuleStateController.STATE_NAME));

        controller.saveState(doc.getDocumentElement());

        XPath xpath = StateXML.newXPath();
        Assert.assertEquals("PT1H", xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:advance", doc));
        Assert.assertEquals(sequence.get(0).toISO8601String(),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:sequence/:interval[1]", doc));
        Assert.assertEquals(sequence.get(1).toISO8601String(),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:sequence/:interval[2]", doc));
        Assert.assertEquals(sequence.get(2).toISO8601String(),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:sequence/:interval[3]", doc));
        Assert.assertEquals("taperEndTaperStart",
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:animation/:loopBehavior", doc));
        Assert.assertEquals(sequence.get(0).simpleUnion(sequence.get(2)).toISO8601String(),
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:animation/:loop", doc));
        Assert.assertEquals("200",
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:animation/:millisPerFrame", doc));
        Assert.assertEquals("Stop",
                xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:animation/:playState", doc));
    }

    /**
     * Tests V4 code.
     */
    @Test
    public void testV4()
    {
        TestAnimationManager animationManager = new TestAnimationManager();
        AnimationManagerStateController controller = new AnimationManagerStateController(animationManager, new TestTimeManager());

        animationManager
                .setPlan(new DefaultAnimationPlan(New.list(TimeSpan.get(100, 200), TimeSpan.get(200, 300)), EndBehavior.WRAP));
        animationManager.setChangeRate(null, new Milliseconds(10));
        animationManager.play(null, null);

        StateType state = new StateType();
        controller.saveState(state);

        animationManager.setPlan(null, null, null, null);

        Assert.assertTrue(controller.canActivateState(state));
        controller.activateState(null, null, null, state);

        Assert.assertTrue(animationManager.isPlaying());
        Assert.assertEquals(new Milliseconds(10), animationManager.getChangeRate());
        Assert.assertEquals(TimeSpan.get(100, 300), animationManager.getCurrentPlan().getLimitWindow());
        Assert.assertEquals(EndBehavior.WRAP, animationManager.getCurrentPlan().getEndBehavior());
        Assert.assertEquals(new Milliseconds(100), animationManager.getCurrentPlan().getAdvanceDuration());
        Assert.assertEquals(New.list(TimeSpan.get(100, 200), TimeSpan.get(200, 300)),
                animationManager.getCurrentPlan().getAnimationSequence());
    }

    /**
     * Tests V4 code with incomplete values.
     */
    @Test
    public void testV4Incomplete()
    {
        TestAnimationManager animationManager = new TestAnimationManager();
        AnimationManagerStateController controller = new AnimationManagerStateController(animationManager, new TestTimeManager());

        // Test completely empty
        StateType state = new StateType();
        Assert.assertFalse(controller.canActivateState(state));
        controller.activateState(null, null, null, state);

        // Test partially empty
        state.setTime(new TimeType());
        Assert.assertFalse(controller.canActivateState(state));
        controller.activateState(null, null, null, state);

        // Test less partially empty
        state.getTime().setAnimation(new TimeAnimationType());
        Assert.assertTrue(controller.canActivateState(state));
        controller.activateState(null, null, null, state);
        Assert.assertNull(animationManager.getCurrentPlan());
    }

    /**
     * Get a sequence for testing.
     *
     * @return The sequence.
     */
    private List<TimeSpan> getSequence()
    {
        List<TimeSpan> sequence = New.list();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        sequence.add(TimeSpan.get(cal.getTime(), Hours.ONE));
        cal.add(Calendar.HOUR_OF_DAY, 2);
        sequence.add(TimeSpan.get(cal.getTime(), Hours.ONE));
        cal.add(Calendar.HOUR_OF_DAY, 2);
        sequence.add(TimeSpan.get(cal.getTime(), Hours.ONE));
        return sequence;
    }
}

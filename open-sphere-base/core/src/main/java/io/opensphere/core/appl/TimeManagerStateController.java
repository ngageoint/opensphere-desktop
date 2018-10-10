package io.opensphere.core.appl;

import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bitsys.fade.mist.state.v4.HeldIntervalType;
import com.bitsys.fade.mist.state.v4.HeldIntervalsType;
import com.bitsys.fade.mist.state.v4.PlayStateType;
import com.bitsys.fade.mist.state.v4.StateType;
import com.bitsys.fade.mist.state.v4.TimeFadeType;
import com.bitsys.fade.mist.state.v4.TimeType;

import io.opensphere.core.AnimationManager;
import io.opensphere.core.TimeManager;
import io.opensphere.core.TimeManager.Fade;
import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.animation.AnimationPlan.EndBehavior;
import io.opensphere.core.animation.AnimationState.Direction;
import io.opensphere.core.animation.impl.DefaultAnimationPlan;
import io.opensphere.core.animation.impl.ExportAnimationState;
import io.opensphere.core.animation.impl.ExportAnimationState.PlayState;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.modulestate.AbstractModuleStateController;
import io.opensphere.core.modulestate.ModuleStateController;
import io.opensphere.core.modulestate.StateUtilities;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;

/**
 * A state controller for a {@link TimeManager}.
 */
@SuppressWarnings("PMD.GodClass")
public class TimeManagerStateController extends AbstractModuleStateController
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(TimeManagerStateController.class);

    /** The animation manager. */
    private final AnimationManager myAnimationManager;

    /** The time manager. */
    private final TimeManager myTimeManager;

    /**
     * Constructor.
     *
     * @param timeManager The time manager.
     * @param animationManager The animation manager.
     */
    public TimeManagerStateController(TimeManager timeManager, AnimationManager animationManager)
    {
        myTimeManager = Utilities.checkNull(timeManager, "timeManager");
        myAnimationManager = Utilities.checkNull(animationManager, "animationManager");
    }

    @Override
    public void activateState(String id, String description, Collection<? extends String> tags, Node element)
    {
        try
        {
            XPath xpath = StateXML.newXPath();

            String current = xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:current", element);
            ExportAnimationState animationState = StateXML.getStateBean(element, "/:time/:animation", ExportAnimationState.class);
            boolean animationSetTheTime = animationState != null
                    && (animationState.getPlayState() == PlayState.FORWARD || animationState.getPlayState() == PlayState.REVERSE);
            if (StringUtils.isNotEmpty(current) && !animationSetTheTime)
            {
                try
                {
                    TimeSpan span = TimeSpan.fromISO8601String(current);

                    resetPlanIfNecessary(span);

                    myTimeManager.setPrimaryActiveTimeSpan(span);
                }
                catch (ParseException e)
                {
                    LOGGER.error("Failed to parse state interval: " + e, e);
                }
            }

            NodeList heldNodes = (NodeList)xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:held", element,
                    XPathConstants.NODESET);
            for (int index = 0; index < heldNodes.getLength(); ++index)
            {
                Node heldNode = heldNodes.item(index);
                Node keyNode = (Node)xpath.evaluate(
                        "/" + ModuleStateController.STATE_QNAME + "/:time/:held[" + (index + 1) + "]/:key", heldNode,
                        XPathConstants.NODE);

                Collection<TimeSpan> intervals = New.collection();
                NodeList intervalNodes = (NodeList)xpath.evaluate(
                        "/" + ModuleStateController.STATE_QNAME + "/:time/:held[" + (index + 1) + "]/:interval", heldNode,
                        XPathConstants.NODESET);
                for (int index2 = 0; index2 < intervalNodes.getLength(); ++index2)
                {
                    String intervalText = intervalNodes.item(index2).getTextContent();
                    try
                    {
                        intervals.add(TimeSpan.fromISO8601String(intervalText));
                    }
                    catch (ParseException e)
                    {
                        LOGGER.warn("Could not parse interval text [" + intervalText + "]: " + e, e);
                    }
                }

                if (!intervals.isEmpty())
                {
                    myTimeManager.setSecondaryActiveTimeSpans(
                            keyNode == null ? TimeManager.WILDCARD_CONSTRAINT_KEY : keyNode.getTextContent(), intervals);
                }
            }

            String fadeIn = xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:fade/:in", element);
            String fadeOut = xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:fade/:out", element);
            if (StringUtils.isNotEmpty(fadeIn) || StringUtils.isNotEmpty(fadeOut))
            {
                try
                {
                    Duration in = StringUtils.isEmpty(fadeIn) ? Seconds.ZERO : Duration.fromISO8601String(fadeIn);
                    Duration out = StringUtils.isEmpty(fadeOut) ? Seconds.ZERO : Duration.fromISO8601String(fadeOut);
                    myTimeManager.setFade(new DefaultFade(in, out));
                }
                catch (ParseException e)
                {
                    LOGGER.error("Failed to parse state interval: " + e);
                }
            }
        }
        catch (XPathExpressionException e)
        {
            LOGGER.error(e, e);
        }
    }

    @Override
    public void activateState(String id, String description, Collection<? extends String> tags, StateType state)
    {
        TimeType time = state.getTime();
        if (time != null)
        {
            activateCurrentTime(time);
            activateHeldIntervals(time);
            activateFade(time);
        }
    }

    @Override
    public boolean canActivateState(Node node)
    {
        XPath xpath = StateXML.newXPath();

        try
        {
            String current = xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:current", node);
            String held = xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:held/:key", node);
            String fadeIn = xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:fade/:in", node);
            String fadeOut = xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:fade/:out", node);
            return StringUtils.isNotEmpty(current) || StringUtils.isNotEmpty(held) || StringUtils.isNotEmpty(fadeIn)
                    || StringUtils.isNotEmpty(fadeOut);
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
        return state.getTime() != null && (StringUtils.isNotEmpty(state.getTime().getCurrent())
                || state.getTime().getHeldIntervals() != null || state.getTime().getFade() != null);
    }

    @Override
    public boolean canSaveState()
    {
        return !myTimeManager.getPrimaryActiveTimeSpans().get(0).isZero() || myTimeManager.getFade() != null
                || !myTimeManager.getSecondaryActiveTimeSpans().isEmpty();
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
            Node timeNode = (Node)StateXML.newXPath().evaluate("/" + ModuleStateController.STATE_QNAME + "/:time", node,
                    XPathConstants.NODE);
            if (timeNode == null)
            {
                timeNode = StateXML.createElement(doc, "time");
                node.appendChild(timeNode);
            }

            saveCurrentState(timeNode);
            saveHeldState(timeNode);
            saveFadeState(timeNode);
        }
        catch (XPathExpressionException e)
        {
            LOGGER.error(e, e);
        }
    }

    @Override
    public void saveState(StateType state)
    {
        TimeType time = StateUtilities.getTime(state);
        time.setCurrent(getCurrent());
        time.setHeldIntervals(getHeldIntervals());
        time.setFade(getFade());
    }

    /**
     * Activates the current time.
     *
     * @param time the state time object
     */
    private void activateCurrentTime(TimeType time)
    {
        PlayStateType playState = time.getAnimation() != null ? time.getAnimation().getPlayState() : null;
        boolean animationSetTheTime = EqualsHelper.equalsAny(playState, PlayState.FORWARD, PlayState.REVERSE);
        if (StringUtils.isNotEmpty(time.getCurrent()) && !animationSetTheTime)
        {
            try
            {
                TimeSpan span = TimeSpan.fromISO8601String(time.getCurrent());

                resetPlanIfNecessary(span);

                myTimeManager.setPrimaryActiveTimeSpan(span);
            }
            catch (ParseException e)
            {
                LOGGER.error("Failed to parse state interval: " + e, e);
            }
        }
    }

    /**
     * Activates the held intervals.
     *
     * @param time the state time object
     */
    private void activateHeldIntervals(TimeType time)
    {
        if (time.getHeldIntervals() != null && time.getHeldIntervals().isSetHeld())
        {
            Map<String, List<HeldIntervalType>> partitionedIntervals = CollectionUtilities
                    .partition(time.getHeldIntervals().getHeld(), interval -> interval.getKey());
            for (Map.Entry<String, List<HeldIntervalType>> entry : partitionedIntervals.entrySet())
            {
                String key = entry.getKey();
                List<HeldIntervalType> heldIntervals = entry.getValue();

                Collection<TimeSpan> intervals = New.collection();
                for (HeldIntervalType heldInterval : heldIntervals)
                {
                    try
                    {
                        intervals.add(TimeSpan.fromISO8601String(heldInterval.getInterval()));
                    }
                    catch (ParseException e)
                    {
                        LOGGER.warn("Could not parse interval text [" + heldInterval.getInterval() + "]: " + e, e);
                    }
                }

                if (!intervals.isEmpty())
                {
                    Object constraintKey = key != null ? key : TimeManager.WILDCARD_CONSTRAINT_KEY;
                    myTimeManager.setSecondaryActiveTimeSpans(constraintKey, intervals);
                }
            }
        }
    }

    /**
     * Activates the fade setting.
     *
     * @param time the state time object
     */
    private void activateFade(TimeType time)
    {
        TimeFadeType fade = time.getFade();
        if (fade != null)
        {
            try
            {
                Duration in = fade.getIn() != null ? Duration.fromISO8601String(fade.getIn().toString()) : Seconds.ZERO;
                Duration out = fade.getOut() != null ? Duration.fromISO8601String(fade.getOut().toString()) : Seconds.ZERO;
                myTimeManager.setFade(new DefaultFade(in, out));
            }
            catch (ParseException e)
            {
                LOGGER.error(e, e);
            }
        }
    }

    /**
     * Gets the current time.
     *
     * @return the current time string
     */
    private String getCurrent()
    {
        TimeSpan current = myTimeManager.getPrimaryActiveTimeSpans().get(0);
        return !current.isZero() ? current.toISO8601String() : null;
    }

    /**
     * Gets the held intervals object.
     *
     * @return the held intervals object
     */
    private HeldIntervalsType getHeldIntervals()
    {
        HeldIntervalsType heldIntervals = null;
        Set<Entry<Object, Collection<? extends TimeSpan>>> spans = myTimeManager.getSecondaryActiveTimeSpans().entrySet();
        if (!spans.isEmpty())
        {
            heldIntervals = new HeldIntervalsType();
            for (Entry<Object, Collection<? extends TimeSpan>> entry : spans)
            {
                for (TimeSpan ts : entry.getValue())
                {
                    HeldIntervalType heldInterval = new HeldIntervalType();
                    if (!Utilities.sameInstance(TimeManager.WILDCARD_CONSTRAINT_KEY, entry.getKey()))
                    {
                        heldInterval.setKey(entry.getKey().toString());
                    }
                    heldInterval.setInterval(ts.toISO8601String());

                    heldIntervals.getHeld().add(heldInterval);
                }
            }
        }
        return heldIntervals;
    }

    /**
     * Gets the fade object.
     *
     * @return the fade object
     */
    private TimeFadeType getFade()
    {
        TimeFadeType fadeType = null;
        Fade fade = myTimeManager.getFade();
        if (fade != null)
        {
            fadeType = new TimeFadeType();
            try
            {
                DatatypeFactory factory = DatatypeFactory.newInstance();
                fadeType.setIn(factory.newDuration(fade.getFadeIn().toISO8601String()));
                fadeType.setOut(factory.newDuration(fade.getFadeOut().toISO8601String()));
            }
            catch (DatatypeConfigurationException e)
            {
                LOGGER.error(e);
            }
        }
        return fadeType;
    }

    /**
     * Reset the animation plan if it doesn't support the given time span.
     *
     * @param span The time span.
     */
    private void resetPlanIfNecessary(TimeSpan span)
    {
        AnimationPlan plan = myAnimationManager.getCurrentPlan();
        if (plan != null && !span.equals(plan.getTimeSpanForState(plan.findState(span, Direction.FORWARD))))
        {
            myAnimationManager.setPlan(new DefaultAnimationPlan(Collections.singletonList(span), EndBehavior.STOP));
        }
    }

    /**
     * Save the primary active time spans.
     *
     * @param timeNode The time node.
     * @throws XPathExpressionException If there is an XPath error.
     */
    private void saveCurrentState(Node timeNode) throws XPathExpressionException
    {
        TimeSpan current = myTimeManager.getPrimaryActiveTimeSpans().get(0);
        if (!current.isZero())
        {
            Node currentNode = (Node)StateXML.newXPath().evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:current",
                    timeNode, XPathConstants.NODE);
            if (currentNode == null)
            {
                currentNode = StateXML.createElement(timeNode.getOwnerDocument(), "current");
                timeNode.appendChild(currentNode);
            }

            currentNode.setTextContent(current.toISO8601String());
        }
    }

    /**
     * Save the fade state.
     *
     * @param timeNode The time node.
     *
     * @throws XPathExpressionException If there is an XPath error.
     */
    private void saveFadeState(Node timeNode) throws XPathExpressionException
    {
        Fade fade = myTimeManager.getFade();
        if (fade != null)
        {
            Node fadeNode = (Node)StateXML.newXPath().evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:fade", timeNode,
                    XPathConstants.NODE);
            if (fadeNode == null)
            {
                fadeNode = StateXML.createElement(timeNode.getOwnerDocument(), "fade");
                timeNode.appendChild(fadeNode);
            }

            Node fadeInNode = (Node)StateXML.newXPath().evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:fade/:in",
                    timeNode, XPathConstants.NODE);
            if (fadeInNode == null)
            {
                fadeInNode = StateXML.createElement(timeNode.getOwnerDocument(), "in");
                fadeNode.appendChild(fadeInNode);
            }

            fadeInNode.setTextContent(fade.getFadeIn().toISO8601String());

            Node fadeOutNode = (Node)StateXML.newXPath().evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:fade/:out",
                    timeNode, XPathConstants.NODE);
            if (fadeOutNode == null)
            {
                fadeOutNode = StateXML.createElement(timeNode.getOwnerDocument(), "out");
                fadeNode.appendChild(fadeOutNode);
            }

            fadeOutNode.setTextContent(fade.getFadeOut().toISO8601String());
            fadeInNode.setTextContent(fade.getFadeIn().toISO8601String());
        }
    }

    /**
     * Save the held state.
     *
     * @param timeNode The time node.
     */
    private void saveHeldState(Node timeNode)
    {
        for (Entry<Object, Collection<? extends TimeSpan>> entry : myTimeManager.getSecondaryActiveTimeSpans().entrySet())
        {
            Node heldNode = timeNode.appendChild(StateXML.createElement(timeNode.getOwnerDocument(), "held"));

            if (!Utilities.sameInstance(TimeManager.WILDCARD_CONSTRAINT_KEY, entry.getKey()))
            {
                heldNode.appendChild(StateXML.createElement(timeNode.getOwnerDocument(), "key"))
                .setTextContent(entry.getKey().toString());
            }

            for (TimeSpan ts : entry.getValue())
            {
                heldNode.appendChild(StateXML.createElement(timeNode.getOwnerDocument(), "interval"))
                .setTextContent(ts.toISO8601String());
            }
        }
    }
}

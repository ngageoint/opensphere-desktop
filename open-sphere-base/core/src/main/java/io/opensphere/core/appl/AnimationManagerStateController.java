package io.opensphere.core.appl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.LoopBehaviorType;
import com.bitsys.fade.mist.state.v4.PlayStateType;
import com.bitsys.fade.mist.state.v4.StateType;
import com.bitsys.fade.mist.state.v4.TimeAnimationType;
import com.bitsys.fade.mist.state.v4.TimeSequenceType;
import com.bitsys.fade.mist.state.v4.TimeType;

import io.opensphere.core.AnimationManager;
import io.opensphere.core.TimeManager;
import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.animation.AnimationPlan.EndBehavior;
import io.opensphere.core.animation.AnimationState;
import io.opensphere.core.animation.AnimationState.Direction;
import io.opensphere.core.animation.ContinuousAnimationPlan;
import io.opensphere.core.animation.impl.AnimationPlanFactory;
import io.opensphere.core.animation.impl.DefaultAnimationPlan;
import io.opensphere.core.animation.impl.DefaultContinuousAnimationPlan;
import io.opensphere.core.animation.impl.ExportAnimationState;
import io.opensphere.core.animation.impl.ExportAnimationState.LoopBehavior;
import io.opensphere.core.animation.impl.ExportAnimationState.PlayState;
import io.opensphere.core.animation.impl.ExportTimeState;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.modulestate.AbstractModuleStateController;
import io.opensphere.core.modulestate.ModuleStateController;
import io.opensphere.core.modulestate.StateUtilities;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.DurationUnitsProvider;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.units.duration.Weeks;
import io.opensphere.core.util.JAXBContextHelper;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.time.TimelineUtilities;

/**
 * A state controller for an {@link AnimationManager}.
 */
@SuppressWarnings("PMD.GodClass")
public class AnimationManagerStateController extends AbstractModuleStateController
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AnimationManagerStateController.class);

    /** The animation manager. */
    private final AnimationManager myAnimationManager;

    /** The time manager. */
    private final TimeManager myTimeManager;

    /**
     * Since we only support one span for continuous animation, combine the
     * sequence into a single span.
     *
     * @param sequence The original sequence.
     * @return The sequence as a single span. If the sequence is null or
     *         contains 0 or 1 spans the original sequence is returned,
     *         otherwise a new list containing a single span is returned.
     */
    protected static List<? extends TimeSpan> getCombinedSequence(List<? extends TimeSpan> sequence)
    {
        if (sequence != null && sequence.size() > 1)
        {
            long start = sequence.get(0).getStart();
            long end = sequence.get(sequence.size() - 1).getEnd();
            return Collections.singletonList(TimeSpan.get(start, end));
        }
        return sequence;
    }

    /**
     * Constructor.
     *
     * @param animationManager The animation manager.
     * @param timeManager The time manager.
     */
    public AnimationManagerStateController(AnimationManager animationManager, TimeManager timeManager)
    {
        myAnimationManager = Utilities.checkNull(animationManager, "animationManager");
        myTimeManager = Utilities.checkNull(timeManager, "timeManager");
    }

    @Override
    public void activateState(String id, String description, Collection<? extends String> tags, Node node)
    {
        try
        {
            XPath xpath = StateXML.newXPath();

            Node timeNode = (Node)xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time", node, XPathConstants.NODE);
            Node animationNode = (Node)xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:animation", node,
                    XPathConstants.NODE);

            if (timeNode == null || animationNode == null)
            {
                return;
            }

            if (checkNoAnimation(node))
            {
                myAnimationManager.abandonPlan();
                String evaluate = xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:current", timeNode);
                if (StringUtils.isEmpty(evaluate))
                {
                    myTimeManager.setPrimaryActiveTimeSpan(TimeSpan.ZERO);
                }
                return;
            }

            try
            {
                ExportTimeState timeState = XMLUtilities.readXMLObject(timeNode, ExportTimeState.class);
                TimeSpan activeSpan = timeState.getCurrent();
                Duration advanceDuration = timeState.getAdvanceDuration();
                ExportAnimationState animState = XMLUtilities.readXMLObject(animationNode, ExportAnimationState.class);
                TimeSpan loopSpan = animState.getLoopInterval();

                // Fix the sequence
                List<? extends TimeSpan> sequence = timeState.getSequence() != null ? timeState.getSequence()
                        : Collections.<TimeSpan>emptyList();

                // Fix the loop span
                if (loopSpan == null)
                {
                    loopSpan = activeSpan;
                }

                activeSpan = fixActiveSpan(activeSpan, sequence, loopSpan, advanceDuration);

                AnimationPlan plan = createAnimationPlan(sequence, loopSpan, activeSpan, advanceDuration);

                AnimationState animationState = plan.findState(activeSpan, AnimationState.Direction.FORWARD);
                PlayState playState = animState.getPlayState();
                Direction animationDirection = playState == null || playState == PlayState.STOP ? null
                        : playState == PlayState.REVERSE ? Direction.BACKWARD : Direction.FORWARD;
                myAnimationManager.setPlan(plan, animationState, animationDirection,
                        new Milliseconds(getMillisPerFrame(animState)));
            }
            catch (JAXBException e)
            {
                LOGGER.error("Failed to read animation state: " + e, e);
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
        if (time != null && time.getAnimation() != null)
        {
            if (time.getAnimation().isSetLoop() || time.getAnimation().isSetLoopBehavior()
                    || time.getAnimation().getMillisPerFrame() > 0 || time.getAnimation().isSetPlayState()
                    || time.isSetInterval())
            {
                TimeSpan activeSpan = StateUtilities.parseSpan(time.getCurrent());
                Duration advanceDuration = StateUtilities.parseDuration(time.getAdvance());
                TimeSpan loopSpan = StateUtilities.getLoopSpan(state.getTime());
                List<? extends TimeSpan> sequence = time.getPlayIntervals() != null && time.getPlayIntervals().isSetInterval()
                        ? time.getPlayIntervals().getInterval().stream().map(StateUtilities::parseSpan)
                                .collect(Collectors.toList())
                                : Collections.emptyList();
                                PlayState playState = time.getAnimation().getPlayState() == PlayStateType.PLAY ? PlayState.FORWARD
                                        : PlayState.STOP;

                                int millisPerFrame = time.getAnimation().getMillisPerFrame();
                                if (millisPerFrame <= 1)
                                {
                                    LOGGER.warn("Animation state millisPerFrame is " + millisPerFrame + "; setting to 500");
                                    millisPerFrame = 500;
                                }

                                // Fix the loop span
                                if (loopSpan == null)
                                {
                                    loopSpan = activeSpan;
                                }

                                activeSpan = fixActiveSpan(activeSpan, sequence, loopSpan, advanceDuration);

                                AnimationPlan plan = createAnimationPlan(sequence, loopSpan, activeSpan, advanceDuration);

                                AnimationState animationState = plan.findState(activeSpan, AnimationState.Direction.FORWARD);
                                Direction animationDirection = playState == PlayState.STOP ? null
                                        : playState == PlayState.REVERSE ? Direction.BACKWARD : Direction.FORWARD;
                                myAnimationManager.setPlan(plan, animationState, animationDirection, new Milliseconds(millisPerFrame));
            }
            else
            {
                myAnimationManager.abandonPlan();
                if (StringUtils.isEmpty(time.getCurrent()))
                {
                    myTimeManager.setPrimaryActiveTimeSpan(TimeSpan.ZERO);
                }
            }
        }
    }

    @Override
    public boolean canActivateState(Node node)
    {
        try
        {
            return (Node)StateXML.newXPath().evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:animation", node,
                    XPathConstants.NODE) != null;
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
        return state.getTime() != null && state.getTime().getAnimation() != null;
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
        try
        {
            AnimationPlan plan = myAnimationManager.getCurrentPlan();
            if (plan == null)
            {
                Document doc = node instanceof Document ? (Document)node : node.getOwnerDocument();
                Node timeNode = StateXML.createChildNode(node, doc, node, "/" + ModuleStateController.STATE_QNAME + "/:time",
                        "time");
                StateXML.createChildNode(node, doc, timeNode, "/" + ModuleStateController.STATE_QNAME + "/:time/:animation",
                        "animation");
                return;
            }

            ExportTimeState timeState = new ExportTimeState();

            ExportAnimationState animState = new ExportAnimationState();
            if (myAnimationManager.isPlaying())
            {
                if (myAnimationManager.getAnimationState().getDirection() == Direction.FORWARD)
                {
                    animState.setPlayState(PlayState.FORWARD);
                }
                else
                {
                    animState.setPlayState(PlayState.REVERSE);
                }
            }
            else
            {
                animState.setPlayState(PlayState.STOP);
            }

            animState.setLoopBehavior(LoopBehavior.TAPER_END_TAPER_START);
            animState.setMillisPerFrame(Milliseconds.get(myAnimationManager.getChangeRate()).intValue());

            if (plan instanceof ContinuousAnimationPlan)
            {
                ContinuousAnimationPlan cPlan = (ContinuousAnimationPlan)plan;
                timeState.setAdvanceDuration(cPlan.getAdvanceDuration());
                animState.setLoopInterval(cPlan.getLimitWindow().isTimeless() ? null : cPlan.getLimitWindow());
            }
            else
            {
                TimeSpan start = plan.getAnimationSequence().get(0);
                TimeSpan end = plan.getAnimationSequence().get(plan.getAnimationSequence().size() - 1);
                timeState.setAdvanceDuration(plan.getAnimationSequence().get(0).getDuration());
                animState.setLoopInterval(start.simpleUnion(end));
            }
            timeState.setSequence(plan.getAnimationSequence());

            try
            {
                JAXBContext context = JAXBContextHelper.getCachedContext(ExportTimeState.class);
                context.createMarshaller().marshal(timeState, node);

                XMLUtilities.mergeDuplicateElements(node.getOwnerDocument(), "time");
            }
            catch (JAXBException e)
            {
                LOGGER.error("Failed to marshal time state: " + e, e);
            }

            Node timeNode = (Node)StateXML.newXPath().evaluate("/" + ModuleStateController.STATE_QNAME + "/:time", node,
                    XPathConstants.NODE);

            try
            {
                JAXBContext context = JAXBContextHelper.getCachedContext(ExportAnimationState.class);
                context.createMarshaller().marshal(animState, timeNode);
            }
            catch (JAXBException e)
            {
                LOGGER.error("Failed to marshal animation state: " + e, e);
            }
        }
        catch (XPathExpressionException e)
        {
            LOGGER.error(e, e);
        }
    }

    @Override
    public void saveState(StateType state)
    {
        AnimationPlan plan = myAnimationManager.getCurrentPlan();
        if (plan != null)
        {
            TimeType time = StateUtilities.getTime(state);

            TimeAnimationType animation = new TimeAnimationType();
            animation.setPlayState(myAnimationManager.isPlaying() ? PlayStateType.PLAY : PlayStateType.STOP);
            animation.setLoopBehavior(LoopBehaviorType.TAPER_END_TAPER_START);
            animation.setMillisPerFrame(Milliseconds.get(myAnimationManager.getChangeRate()).intValue());
            if (plan instanceof ContinuousAnimationPlan)
            {
                ContinuousAnimationPlan cPlan = (ContinuousAnimationPlan)plan;
                time.setAdvance(cPlan.getAdvanceDuration().toISO8601String());
                animation.setLoop(cPlan.getLimitWindow().isTimeless() ? null : cPlan.getLimitWindow().toISO8601String());
            }
            else
            {
                TimeSpan start = plan.getAnimationSequence().get(0);
                TimeSpan end = plan.getAnimationSequence().get(plan.getAnimationSequence().size() - 1);
                time.setAdvance(plan.getAnimationSequence().get(0).getDuration().toISO8601String());
                animation.setLoop(start.simpleUnion(end).toISO8601String());
            }
            time.setAnimation(animation);

            TimeSequenceType playIntervals = new TimeSequenceType();
            playIntervals.getInterval()
            .addAll(plan.getAnimationSequence().stream().map(TimeSpan::toISO8601String).collect(Collectors.toList()));
            time.setPlayIntervals(playIntervals);
        }
    }

    /**
     * Expands and aligns the span to a day/week/month boundary if necessary.
     *
     * @param span The span.
     * @return The sequence to use.
     */
    private TimeSpan alignSpan(TimeSpan span)
    {
        TimeSpan alignedSpan = span;
        Duration dur = new DurationUnitsProvider().getLargestIntegerUnitType(span.getDuration());
        if (dur.doubleValue() != 1. && !(dur.compareTo(new Days(28)) >= 0 && dur.compareTo(new Days(31)) <= 0))
        {
            if (dur.isConvertibleTo(Days.class) && dur.compareTo(Days.ONE) < 0)
            {
                alignedSpan = TimelineUtilities.getThisDay(span.getStartDate());
            }
            else if (dur.isConvertibleTo(Weeks.class) && dur.compareTo(Weeks.ONE) < 0)
            {
                alignedSpan = TimelineUtilities.getThisWeek(span.getStartDate());
            }
            else
            {
                alignedSpan = TimelineUtilities.getThisMonth(span.getStartDate());
            }
        }
        return alignedSpan;
    }

    /**
     * Get the delay between animation frames.
     *
     * @param animState The animation state.
     * @return The number of milliseconds to spend on each frame.
     */
    protected int getMillisPerFrame(ExportAnimationState animState)
    {
        int millisPerFrame = animState.getMillisPerFrame();
        if (millisPerFrame <= 1)
        {
            LOGGER.warn("Animation state millisPerFrame is " + millisPerFrame + "; setting to 500");
            millisPerFrame = 500;
        }
        return millisPerFrame;
    }

    /**
     * Check to see if there is really no animation when activating a state.
     *
     * @param node A DOM node that contains the state of the module to be
     *            activated.
     * @return true when there is no animation and false when there is an
     *         animation.
     */
    private boolean checkNoAnimation(Node node)
    {
        try
        {
            XPath xpath = StateXML.newXPath();
            Node animNode = (Node)xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:time/:animation", node,
                    XPathConstants.NODE);
            if (!animNode.hasChildNodes())
            {
                return true;
            }
        }
        catch (XPathExpressionException e)
        {
            LOGGER.error(e, e);
        }

        return false;
    }

    /**
     * Creates an animation plan from the sequence if possible, otherwise from
     * the other parameters.
     *
     * @param sequence the sequence
     * @param loopSpan the loop span
     * @param activeSpan the active span
     * @param advanceDuration the advance duration
     * @return the animation plan
     */
    private AnimationPlan createAnimationPlan(List<? extends TimeSpan> sequence, TimeSpan loopSpan, TimeSpan activeSpan,
            Duration advanceDuration)
    {
        AnimationPlan plan;
        Duration activeDuration = activeSpan.getDuration();

        // Create the plan from the sequence
        if (!sequence.isEmpty())
        {
            /* This is a continuous plan if the active span does not equal one
             * of the intervals in the sequence or if the advance duration does
             * not equal the duration of elements in the sequence (which should
             * all be the same). */
            if (advanceDuration == null
                    || advanceDuration.equalsIgnoreUnits(sequence.get(0).getDuration()) && sequence.contains(activeSpan))
            {
                plan = new DefaultAnimationPlan(sequence, EndBehavior.WRAP);
            }
            else
            {
                plan = new DefaultContinuousAnimationPlan(getCombinedSequence(sequence), activeDuration, advanceDuration,
                        EndBehavior.WRAP, loopSpan);
            }
        }
        // Create the plan from the other parameters
        else
        {
            AnimationPlanFactory planFactory = new AnimationPlanFactory();
            List<TimeSpan> skippedSpans = Collections.<TimeSpan>emptyList();
            if (advanceDuration == null
                    || advanceDuration.equalsIgnoreUnits(activeDuration) && TimelineUtilities.isDayWeekMonth(activeSpan))
            {
                plan = planFactory.createDefaultAnimationPlan(loopSpan, alignSpan(activeSpan).getDuration(), skippedSpans);
            }
            else
            {
                plan = planFactory.createDefaultContinuousAnimationPlan(loopSpan, loopSpan, activeDuration, advanceDuration,
                        skippedSpans);
            }
        }

        return plan;
    }

    /**
     * Fixes the active span.
     *
     * @param activeSpan the active span
     * @param sequence the sequence
     * @param loopSpan the loop span
     * @param advanceDuration the advance duration
     * @return the fixed active span
     */
    private TimeSpan fixActiveSpan(TimeSpan activeSpan, List<? extends TimeSpan> sequence, TimeSpan loopSpan,
            Duration advanceDuration)
    {
        TimeSpan fixedSpan = activeSpan;

        // If no active span, set it to the best thing available
        if (activeSpan == null)
        {
            if (!sequence.isEmpty())
            {
                fixedSpan = sequence.get(0);
            }
            else if (advanceDuration != null)
            {
                fixedSpan = TimeSpan.get(loopSpan.getStart(), advanceDuration);
            }
            else
            {
                fixedSpan = loopSpan;
            }
        }

        // Make sure the active span is within the loop span
        if (!loopSpan.contains(fixedSpan))
        {
            fixedSpan = TimeSpan.get(loopSpan.getStart(), fixedSpan.getDuration());
        }

        return fixedSpan;
    }
}

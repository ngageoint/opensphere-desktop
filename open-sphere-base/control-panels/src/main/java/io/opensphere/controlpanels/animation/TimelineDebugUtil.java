package io.opensphere.controlpanels.animation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import io.opensphere.core.AnimationManager;
import io.opensphere.core.TimeManager.Fade;
import io.opensphere.core.Toolbox;
import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.animation.AnimationPlan.EndBehavior;
import io.opensphere.core.animation.AnimationPlanModificationException;
import io.opensphere.core.animation.AnimationState.Direction;
import io.opensphere.core.animation.impl.DefaultAnimationPlan;
import io.opensphere.core.animation.impl.DefaultContinuousAnimationPlan;
import io.opensphere.core.appl.DefaultFade;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Hours;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.time.TimelineUtilities;

/**
 * A utility class for helping to execute time browser debug actions from the
 * "Debug" menu.
 */
final class TimelineDebugUtil
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(TimelineDebugUtil.class);

    /**
     * Create the menu items which execute all of the supported animation
     * changes.
     *
     * @param toolbox The toolbox is used for access to the animation manager.
     * @return The menu which contains all off the menu items.
     */
    public static JMenu createTestMenuItems(Toolbox toolbox)
    {
        JMenu menu = new JMenu("Animation");

        addPlanPublishItems(toolbox, menu);
        addPlayItems(toolbox, menu);
        addMiscItems(toolbox, menu);

        return menu;
    }

    /**
     * Add miscellaneous animation settings menu items.
     *
     * @param toolbox The toolbox is used for access to the animation manager.
     * @param menu The menu which contains all off the menu items.
     */
    private static void addMiscItems(final Toolbox toolbox, JMenu menu)
    {
        JMenuItem fps = new JMenuItem("Set FPS");
        fps.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                AnimationManager animator = toolbox.getAnimationManager();
                AnimationPlan plan = animator.getCurrentPlan();
                String millisStr = JOptionPane.showInputDialog("Enter Duration in milliseconds : ");
                try
                {
                    int millis = Utilities.parseInt(millisStr, 1000);
                    animator.setChangeRate(plan, Duration.create(ChronoUnit.MILLIS, millis));
                }
                catch (AnimationPlanModificationException e)
                {
                    LOGGER.error("Failed to modify the animation plan." + e, e);
                }
            }
        });
        menu.add(fps);

        JMenuItem fadeMi = new JMenuItem("Set Fade Out");
        fadeMi.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                String timeString = JOptionPane.showInputDialog("Enter Fade Duration in hours : ");
                int time = Utilities.parseInt(timeString, 24);
                toolbox.getTimeManager()
                        .setFade(new DefaultFade(Duration.create(ChronoUnit.MILLIS, 0), Duration.create(ChronoUnit.HOURS, time)));
            }
        });
        menu.add(fadeMi);

        JMenuItem stepSize = new JMenuItem("Set Step Size");
        stepSize.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                // This is the advance duration and can only be set for
                // continuous plans
                AnimationPlan plan = toolbox.getAnimationManager().getCurrentPlan();
                if (plan instanceof DefaultContinuousAnimationPlan)
                {
                    DefaultContinuousAnimationPlan contPlan = (DefaultContinuousAnimationPlan)plan;

                    String timeString = JOptionPane.showInputDialog("Enter Advance Duration in hours : ");
                    int time = Utilities.parseInt(timeString, 24);

                    toolbox.getAnimationManager()
                            .setPlan(new DefaultContinuousAnimationPlan(contPlan.getAnimationSequence(),
                                    contPlan.getActiveWindowDuration(), new Hours(time), contPlan.getEndBehavior(),
                                    contPlan.getLimitWindow()));
                }
            }
        });
        menu.add(stepSize);
    }

    /**
     * Add menu items for publishing plans.
     *
     * @param toolbox The toolbox is used for access to the animation manager
     *            and time manager.
     * @param menu The menu which contains all off the menu items.
     */
    private static void addPlanPublishItems(final Toolbox toolbox, JMenu menu)
    {
        JMenuItem discretePlan = new JMenuItem("Publish a Discrete Plan");
        discretePlan.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                // Create and set the animation plan
                GregorianCalendar start = new GregorianCalendar(2013, 11, 5);
                GregorianCalendar end = new GregorianCalendar(2013, 11, 17);
                TimeSpan span = TimeSpan.get(start.getTime(), end.getTime());
                List<? extends TimeSpan> sequence = TimelineUtilities.getIntervalsForSpan(start.getTime(), span,
                        Calendar.DAY_OF_YEAR);

                EndBehavior endBehavior = EndBehavior.WRAP;

                DefaultAnimationPlan plan = new DefaultAnimationPlan(sequence, endBehavior);
                toolbox.getAnimationManager().setPlan(plan);

                // Set the active span
                GregorianCalendar activeStart = new GregorianCalendar(2013, 11, 12);
                GregorianCalendar activeEnd = new GregorianCalendar(2013, 11, 13);
                TimeSpan activeSpan = TimeSpan.get(activeStart.getTime(), activeEnd.getTime());
                toolbox.getTimeManager().setPrimaryActiveTimeSpan(activeSpan);
            }
        });
        menu.add(discretePlan);

        JMenuItem continuousPlan = new JMenuItem("Publish a Continuous Plan");
        continuousPlan.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                // Create and set the animation plan
                GregorianCalendar start = new GregorianCalendar(2013, 10, 29);
                GregorianCalendar end = new GregorianCalendar(2014, 0, 2);
                TimeSpan span = TimeSpan.get(start.getTime(), end.getTime());
                List<? extends TimeSpan> sequence = TimelineUtilities.getIntervalsForSpan(start.getTime(), span,
                        Calendar.DAY_OF_YEAR);

                Duration activeWindowDuration = Duration.create(ChronoUnit.HOURS, 18);
                Duration advanceDuration = Duration.create(ChronoUnit.HOURS, 12);
                EndBehavior endBehavior = EndBehavior.WRAP;

                GregorianCalendar limitStart = new GregorianCalendar(2013, 11, 6);
                GregorianCalendar limitEnd = new GregorianCalendar(2013, 11, 12);
                TimeSpan limitWindow = TimeSpan.get(limitStart.getTime(), limitEnd.getTime());
                DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, activeWindowDuration,
                        advanceDuration, endBehavior, limitWindow);

                toolbox.getAnimationManager().setPlan(plan);

                // Set the active span
                // Make this match the 18 hours we specified for the active
                // window duration.
                GregorianCalendar activeStart = new GregorianCalendar(2013, 11, 8, 3, 30);
                GregorianCalendar activeEnd = new GregorianCalendar(2013, 11, 8, 21, 30);
                TimeSpan activeSpan = TimeSpan.get(activeStart.getTime(), activeEnd.getTime());
                toolbox.getTimeManager().setPrimaryActiveTimeSpan(activeSpan);

                Fade fade = new DefaultFade(Duration.create(ChronoUnit.MILLIS, 0), Duration.create(ChronoUnit.HOURS, 2));
                toolbox.getTimeManager().setFade(fade);
            }
        });
        menu.add(continuousPlan);
    }

    /**
     * Add animation action menu items.
     *
     * @param toolbox The toolbox is used for access to the animation manager.
     * @param menu The menu which contains all off the menu items.
     */
    private static void addPlayItems(final Toolbox toolbox, JMenu menu)
    {
        JMenuItem run = new JMenuItem("Run Animation Once");
        run.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                animationRun(toolbox, Direction.FORWARD);
            }
        });
        menu.add(run);

        JMenuItem runBackward = new JMenuItem("Run Backward Once");
        runBackward.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                animationRun(toolbox, Direction.BACKWARD);
            }
        });
        menu.add(runBackward);

        JMenuItem play = new JMenuItem("Play Forward");
        play.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                play(toolbox, Direction.FORWARD);
            }
        });
        menu.add(play);

        JMenuItem playB = new JMenuItem("Play Backward");
        playB.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                play(toolbox, Direction.BACKWARD);
            }
        });
        menu.add(playB);

        JMenuItem stop = new JMenuItem("Stop");
        stop.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                AnimationManager animator = toolbox.getAnimationManager();
                AnimationPlan plan = animator.getCurrentPlan();
                try
                {
                    animator.pause(plan);
                }
                catch (AnimationPlanModificationException e)
                {
                    LOGGER.error("Failed to modify the animation plan." + e, e);
                }
            }
        });
        menu.add(stop);
    }

    /**
     * Run the animation once through.
     *
     * @param toolbox The toolbox is used for access to the animation manager.
     * @param direction The direction of play.
     */
    private static void animationRun(final Toolbox toolbox, final Direction direction)
    {
        ThreadUtilities.runBackground(new Runnable()
        {
            @Override
            public void run()
            {
                AnimationManager animator = toolbox.getAnimationManager();
                AnimationPlan plan = animator.getCurrentPlan();
                final List<? extends TimeSpan> spans = plan.getAnimationSequence();
                for (int i = 0; i < spans.size(); ++i)
                {
                    try
                    {
                        if (direction == Direction.FORWARD)
                        {
                            animator.stepForward(plan, false);
                        }
                        else
                        {
                            animator.stepBackward(plan, false);
                        }
                    }
                    catch (AnimationPlanModificationException e)
                    {
                        LOGGER.error("Failed to step animation plan. " + e, e);
                    }
                    ThreadUtilities.sleep(500);
                }
            }
        });
    }

    /**
     * Play the animation in the given direction.
     *
     * @param toolbox The toolbox is used for access to the animation manager.
     * @param playDirection The direction of play.
     */
    private static void play(Toolbox toolbox, Direction playDirection)
    {
        AnimationManager animator = toolbox.getAnimationManager();
        AnimationPlan plan = animator.getCurrentPlan();
        try
        {
            animator.play(plan, playDirection);
        }
        catch (AnimationPlanModificationException e)
        {
            LOGGER.error("Failed to modify the animation plan." + e, e);
        }
    }

    /** Disallow instantiation. */
    private TimelineDebugUtil()
    {
    }
}

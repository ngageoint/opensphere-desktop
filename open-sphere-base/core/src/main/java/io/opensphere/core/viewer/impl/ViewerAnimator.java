package io.opensphere.core.viewer.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import io.opensphere.core.math.RectangularCylinder;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.StrongChangeSupport;
import io.opensphere.core.util.concurrent.CommonTimer;
import io.opensphere.core.viewer.TrajectoryGenerator;
import io.opensphere.core.viewer.TrajectoryGenerator.TrajectorySegment;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeType;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.Viewer.TrajectoryGeneratorType;
import io.opensphere.core.viewer.Viewer.ViewerPosition;
import io.opensphere.core.viewer.impl.Viewer3D.ViewerPosition3D;

/**
 * Sends events to a viewer to gradually move it to a destination.
 */
public class ViewerAnimator
{
    /** The task used to generate the view moves. */
    private AnimatorTask myAnimatorTask;

    /** Change support. */
    private final ChangeSupport<Listener> myChangeSupport = StrongChangeSupport.create();

    /** The destination for the viewer. */
    private final ViewerPosition myDestination;

    /** The <tt>Future</tt> for the task. */
    private ScheduledFuture<?> myFuture;

    /** Reference to the viewer being animated. */
    private final DynamicViewer myViewer;

    /**
     * An observer on the viewer to cancel me if the view changes concurrently.
     */
    private final Viewer.Observer myViewerObserver = new Viewer.Observer()
    {
        /** The next expected step of the animation. */
        private int myExpectedStep = 1;

        @Override
        public void notifyViewChanged(ViewChangeType type)
        {
            if (type != ViewChangeType.VIEW_CHANGE)
            {
                return;
            }
            synchronized (ViewerAnimator.this)
            {
                if (myAnimatorTask != null)
                {
                    if (myViewer.getPosition().equals(myAnimatorTask.getTrajectory().get(myExpectedStep)))
                    {
                        ++myExpectedStep;
                        if (myExpectedStep >= myAnimatorTask.getTrajectory().size())
                        {
                            myExpectedStep = 1;
                        }
                    }
                    else
                    {
                        stop();
                    }
                }
            }
        }
    };

    /**
     * Construct the animator.
     *
     * @param viewer The viewer to manipulate.
     * @param points the points which will be used to determine the center of
     *            the viewer destination.
     * @param zoom When true, zoom the destination to fit the points.
     */
    public ViewerAnimator(DynamicViewer viewer, Collection<?> points, boolean zoom)
    {
        this(viewer, points, null, zoom);
    }

    /**
     * Construct the animator.
     *
     * @param viewer The viewer to manipulate.
     * @param points the points which will be used to determine the center of
     *            the viewer destination.
     * @param centroidHint Give a hint as to where the center of the view might
     *            go. This can be useful for when the points are very diverse
     *            geographically.
     * @param zoom When true, zoom the destination to fit the points.
     * @param <T> the type of points. These should be either
     *            {@link GeographicPosition} or {@link Vector3d}.
     */
    @SuppressWarnings("unchecked")
    public <T> ViewerAnimator(DynamicViewer viewer, Collection<T> points, T centroidHint, boolean zoom)
    {
        myViewer = viewer;
        if (points.isEmpty())
        {
            myDestination = myViewer.getPosition();
        }
        else
        {
            Object point = points.iterator().next();
            Collection<? extends Vector3d> modelPoints = null;
            Vector3d centroidModel = null;
            if (point instanceof GeographicPosition)
            {
                modelPoints = myViewer.getMapContext().getProjection()
                        .convertPositionsToModel((Collection<? extends GeographicPosition>)points, Vector3d.ORIGIN);
                if (centroidHint != null)
                {
                    centroidModel = myViewer.getMapContext().getProjection().convertToModel((GeographicPosition)centroidHint,
                            Vector3d.ORIGIN);
                }
            }
            else if (point instanceof Vector3d)
            {
                modelPoints = (Collection<? extends Vector3d>)points;
                centroidModel = (Vector3d)centroidHint;
            }
            if (modelPoints == null)
            {
                myDestination = myViewer.getPosition();
            }
            else
            {
                RectangularCylinder rectifiedBounds = myViewer.getRectifyBounds(modelPoints);
                if (zoom)
                {
                    myDestination = myViewer.getZoomToFitView(rectifiedBounds, centroidModel);
                }
                else
                {
                    myDestination = myViewer.getCenteredView(rectifiedBounds.getCenter(), centroidModel);
                }
            }
        }
    }

    /**
     * Construct the animator.
     *
     * @param viewer The viewer to manipulate.
     * @param destination The destination for the viewer in geographic
     *            coordinates.
     */
    public ViewerAnimator(DynamicViewer viewer, GeographicPosition destination)
    {
        myViewer = viewer;
        Vector3d target = viewer.getMapContext().getProjection().convertToModel(destination, Vector3d.ORIGIN).getNormalized();
        target = target.multiply(myViewer.getPosition().getLocation().getLength());
        myDestination = viewer.getRightedView(target);
    }

    /**
     * Construct the animator.
     *
     * @param viewer The viewer to manipulate.
     * @param destination The destination for the viewer in model coordinates.
     */
    public ViewerAnimator(DynamicViewer viewer, Vector3d destination)
    {
        myViewer = viewer;
        myDestination = viewer.getRightedView(destination);
    }

    /**
     * Construct the animator.
     *
     * @param viewer The viewer to manipulate.
     * @param destination The destination for the viewer.
     */
    public ViewerAnimator(DynamicViewer viewer, ViewerPosition destination)
    {
        myViewer = viewer;
        myDestination = destination;
    }

    /**
     * Add a listener to be notified when the animation is complete.
     *
     * @param listener The listener.
     */
    public void addListener(ViewerAnimator.Listener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Remove a listener.
     *
     * @param listener The listener.
     */
    public void removeListener(ViewerAnimator.Listener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /** Go directly to the destination without animating. */
    public synchronized void snapToPosition()
    {
        myViewer.setPosition(myDestination);
    }

    /**
     * Start the animation with the default trajectory generator. If an
     * animation has already started, it will be cancelled and replaced.
     */
    public synchronized void start()
    {
        start((TrajectoryGeneratorType)null);
    }

    /**
     * Start the animation with the default trajectory generator. If an
     * animation has already started, it will be cancelled and replaced.
     *
     * @param fps The desired speed of panning (frames per second).
     */
    public synchronized void start(int fps)
    {
        start((TrajectoryGeneratorType)null, fps);
    }

    /**
     * Start the animation. If an animation has already started, it will be
     * cancelled and replaced.
     *
     * @param type the type of trajectory to use for the animation. If null is
     *            given, the default arc type will be used.
     */
    public synchronized void start(TrajectoryGeneratorType type)
    {
        // Default is 100 frames per second (will eventually be a user
        // preference)
        final int framesPerSecond = 100;
        start(type, framesPerSecond);
    }

    /**
     * Start the animation. If an animation has already started, it will be
     * cancelled and replaced.
     *
     * @param type the type of trajectory to use for the animation. If null is
     *            given, the default arc type will be used.
     * @param fps The desired speed of panning (frames per second).
     */
    public synchronized void start(TrajectoryGeneratorType type, int fps)
    {
        if (myFuture != null)
        {
            myFuture.cancel(false);
        }
        TrajectoryGenerator arcGen = myViewer.getTrajectoryGenerator(type == null ? TrajectoryGeneratorType.ARC : type);
        List<TrajectorySegment> trajectorySegments = Collections
                .singletonList(new TrajectorySegment(myViewer.getPosition(), myDestination));
        List<ViewerPosition> trajectory = arcGen.generateTrajectory(trajectorySegments);
        GeographicPosition geoPosition;
        if (myDestination instanceof ViewerPosition3D)
        {
            geoPosition = ((ViewerPosition3D)myDestination).getGeoPosition();
        }
        else
        {
            geoPosition = myViewer.getMapContext().getProjection().convertToPosition(myDestination.getLocation(),
                    ReferenceLevel.ELLIPSOID);
        }
        myAnimatorTask = new AnimatorTask(trajectory, geoPosition);
        final long delayMilliseconds = 0L;
        final double oneThousand = 1000.0;
        // millisecond period = 1000/fps (Don't go slower than one frame a
        // second)
        final long periodMilliseconds = fps < 1 ? 1000 : (long)Math.ceil(oneThousand / fps);
        myFuture = CommonTimer.scheduleAtFixedRate(myAnimatorTask, delayMilliseconds, periodMilliseconds);
        myViewer.addObserver(myViewerObserver);
    }

    /** Stop the animation. */
    public synchronized void stop()
    {
        myViewer.removeObserver(myViewerObserver);
        if (myFuture != null)
        {
            myFuture.cancel(false);
            myFuture = null;
        }
        myChangeSupport.notifyListeners(listener -> listener.animationDone());
    }

    /** Listener for when the animation is done. */
    @FunctionalInterface
    public interface Listener
    {
        /** Method called when the viewer animation is done. */
        void animationDone();
    }

    /** Task for the animation. */
    private final class AnimatorTask implements Runnable
    {
        /**
         * The final position to be at when animation is done.
         */
        private final GeographicPosition myFinalPos;

        /** The step. */
        private int myStep = 1;

        /** The trajectory. */
        private final List<? extends ViewerPosition> myTrajectory;

        /**
         * Constructor.
         *
         * @param trajectory The viewer trajectory.
         * @param finalPos The final {@link GeographicPosition} to be at when
         *            animation is done.
         */
        public AnimatorTask(List<? extends ViewerPosition> trajectory, GeographicPosition finalPos)
        {
            myTrajectory = trajectory;
            if (finalPos != null)
            {
                Vector3d lastLocation = trajectory.get(trajectory.size() - 1).getLocation();
                GeographicPosition geoPos = myViewer.getMapContext().getProjection().convertToPosition(lastLocation,
                        ReferenceLevel.ELLIPSOID);
                myFinalPos = new GeographicPosition(LatLonAlt.createFromDegreesMeters(finalPos.getLatLonAlt().getLatD(),
                        finalPos.getLatLonAlt().getLonD(), geoPos.getAlt()));
            }
            else
            {
                myFinalPos = null;
            }
        }

        /**
         * Get the trajectory.
         *
         * @return The list of positions.
         */
        public List<? extends ViewerPosition> getTrajectory()
        {
            return myTrajectory;
        }

        @Override
        public synchronized void run()
        {
            myViewer.setPosition(myTrajectory.get(myStep));
            if (++myStep == myTrajectory.size())
            {
                /**
                 * Set the final position using a geographic position. Depending
                 * on how far we have traveled and how much we have zoomed in
                 * there can be inaccuracies with the projection and model
                 * coordinates.
                 */
                ViewerPosition3D position = (ViewerPosition3D)myViewer.getPosition();
                position.setGeoPosition(myFinalPos);
                myViewer.setPosition(position);
                stop();
            }
        }
    }
}

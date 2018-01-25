package io.opensphere.core.viewer;

import java.util.List;

import io.opensphere.core.viewer.Viewer.ViewerPosition;

/** An interface for generators for viewer Trajectories. */
public interface TrajectoryGenerator
{
    /**
     * Get a list of viewer positions leading from the start position to the end
     * position.
     *
     * @param trajectorySegments The segments for which to generate
     *            trajectories.
     * @return The trajectory in model coordinates.
     */
    List<ViewerPosition> generateTrajectory(List<TrajectorySegment> trajectorySegments);

    /** A segment of a viewer trajectory. */
    class TrajectorySegment
    {
        /** End of the segment. */
        private final ViewerPosition myEndPosition;

        /** Start of the segment. */
        private final ViewerPosition myStartPosition;

        /**
         * The number of steps in the segment. When this is -1, the generator
         * should do its best to generate a reasonable number of steps.
         */
        private final int mySteps;

        /**
         * Constructor. The number of steps will be determined by the generator.
         *
         * @param start Start of the segment.
         * @param end End of the segment.
         */
        public TrajectorySegment(ViewerPosition start, ViewerPosition end)
        {
            myStartPosition = start;
            myEndPosition = end;
            mySteps = -1;
        }

        /**
         * Constructor.
         *
         * @param start Start of the segment.
         * @param end End of the segment.
         * @param steps the number of steps in the segment.
         */
        public TrajectorySegment(ViewerPosition start, ViewerPosition end, int steps)
        {
            myStartPosition = start;
            myEndPosition = end;
            mySteps = steps;
        }

        /**
         * Get the endPosition.
         *
         * @return the endPosition
         */
        public ViewerPosition getEndPosition()
        {
            return myEndPosition;
        }

        /**
         * Get the startPosition.
         *
         * @return the startPosition
         */
        public ViewerPosition getStartPosition()
        {
            return myStartPosition;
        }

        /**
         * Get the steps.
         *
         * @return the steps
         */
        public int getSteps()
        {
            return mySteps;
        }
    }
}

package io.opensphere.core.pipeline.processor;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.opensphere.core.TimeManager;
import io.opensphere.core.geometry.ConstrainableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.util.collections.New;

/**
 * Filters out geometries with a {@link TimeConstraint} whose isMostRecent flag
 * is true. It will group the geometries by their group id and remove every
 * geometry in that group but the geometry that is closest to the end time of
 * the active span.
 */
public class MostRecentGeometryFilter implements Comparator<Geometry>, Serializable
{
    /**
     * serialization id.
     */
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(Geometry o1, Geometry o2)
    {
        ConstrainableGeometry geom1 = (ConstrainableGeometry)o1;
        ConstrainableGeometry geom2 = (ConstrainableGeometry)o2;

        TimeConstraint constraint1 = geom1.getConstraints().getTimeConstraint();
        TimeConstraint constraint2 = geom2.getConstraints().getTimeConstraint();

        return constraint1.getTimeSpan().compareTo(constraint2.getTimeSpan());
    }

    /**
     * Filters out grouped geometries but except for the geometry in that group
     * that is closest to the active spans end time.
     *
     * @param geometries The geometries to filter.
     * @param timeManager Used to see if the most recent constrained geometries
     *            are within the active span.
     * @param checkTimeConstraint True if the geometries should be checked to
     *            see if they fit in the active time window, false if it is
     *            already known that the geometries are in the active window.
     * @param <E> The element types.
     * @return Geometries without a most recent time constraint and the most
     *         recent geometries with a most recent time constraint.
     */
    public <E extends Geometry> List<E> filterMostRecent(Collection<? extends E> geometries, TimeManager timeManager,
            boolean checkTimeConstraint)
    {
        List<E> filtered = New.list();

        Map<Object, E> mostRecents = New.map();

        for (E geometry : geometries)
        {
            boolean addGeometry = true;
            if (geometry instanceof ConstrainableGeometry)
            {
                ConstrainableGeometry constrainGeom = (ConstrainableGeometry)geometry;
                Constraints constraints = constrainGeom.getConstraints();
                if (constraints != null)
                {
                    TimeConstraint mostRecentConstraint = constraints.getTimeConstraint();

                    if (mostRecentConstraint != null && mostRecentConstraint.isMostRecent())
                    {
                        if (!checkTimeConstraint || mostRecentConstraint.check(timeManager.getPrimaryActiveTimeSpans()))
                        {
                            Object groupId = mostRecentConstraint.getKey();

                            E mostRecent = mostRecents.get(groupId);

                            if (mostRecent == null || compare(geometry, mostRecent) > 0)
                            {
                                mostRecents.put(groupId, geometry);
                            }
                        }

                        addGeometry = false;
                    }
                }
            }

            if (addGeometry)
            {
                filtered.add(geometry);
            }
        }

        for (Entry<Object, E> entry : mostRecents.entrySet())
        {
            filtered.add(entry.getValue());
        }

        return filtered;
    }
}

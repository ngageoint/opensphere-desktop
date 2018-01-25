package io.opensphere.wfs.gml311;

import java.util.List;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.geom.AbstractMapGeometrySupport;
import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * GML SAX handler for features with multiple Geometries.
 */
public abstract class AbstractGmlMultiGeometryHandler extends AbstractGmlGeometryHandler
{
    /** List of child geometries (i.e. anything that's not the parent). */
    private final List<MapGeometrySupport> myChildGeometries = New.list();

    /** Holds the current collection state. */
    private State myCollectState = State.WAITING;

    /** Handler for the geometry currently processing. */
    private AbstractGmlGeometryHandler myCurrentGeomHandler;

    /**
     * Flag indicating whether collection involves single member at a time or
     * multiple members, one after another.
     */
    private boolean myIsMultiMember;

    /** Top-level (first-encountered) geometry. */
    private AbstractMapGeometrySupport myParentGeometry;

    /**
     * Instantiates a new SAX handler for GML Multi-Geometries.
     *
     * @param tagName the geometry tag name
     * @param isLatBeforeLon flag indicating position order in points
     */
    public AbstractGmlMultiGeometryHandler(String tagName, boolean isLatBeforeLon)
    {
        super(tagName, isLatBeforeLon);
    }

    @Override
    public AbstractMapGeometrySupport getGeometry()
    {
        if (myParentGeometry != null && !myChildGeometries.isEmpty())
        {
            for (MapGeometrySupport childMgs : myChildGeometries)
            {
                myParentGeometry.addChild(childMgs);
            }
        }
        return myParentGeometry;
    }

    /**
     * Gets the geometry handler for a specific GML geometry tag.
     *
     * @param tag the GML tag of the intended geometry
     * @param isLatBeforeLong flag usually required to instantiate specific
     *            geometry handlers
     * @return the appropriate geometry handler
     */
    protected abstract AbstractGmlGeometryHandler getGeometryHandler(String tag, boolean isLatBeforeLong);

    /**
     * Gets the GML tag name of the MultiGeometry's member types.<br>
     * This method handles the MultiGeomtry types that are returned in separate
     * open/close tags per member (e.g. "curveMember" instead of "curveMembers")
     *
     * @return the GML member tag name
     */
    protected abstract String getMemberName();

    /**
     * Gets the GML tag name of the MultiGeometry's members types.<br>
     * This method handles the MultiGeomtry types that are all condensed into a
     * single open/close tag (e.g. "curveMembers" instead of "curveMember")
     *
     * @return the GML member tag name
     */
    protected abstract String getMembersName();

    @Override
    public void handleClosingTag(String tag, String value)
    {
        switch (myCollectState)
        {
            case WAITING:
                break;
            case SEEK_GEOMETRY:
                break;
            case COLLECT_GEOMETRY:
                if (myCurrentGeomHandler.getTagName().equals(tag))
                {
                    if (myParentGeometry == null)
                    {
                        myParentGeometry = myCurrentGeomHandler.getGeometry();
                    }
                    else
                    {
                        myChildGeometries.add(myCurrentGeomHandler.getGeometry());
                    }

                    // If collecting multiple members, skip right back to
                    // SEEK_GEOMETRY
                    myCollectState = myIsMultiMember ? State.SEEK_GEOMETRY : State.WAITING;
                }
                else
                {
                    myCurrentGeomHandler.handleClosingTag(tag, value);
                }
                break;
            default:
                // Unknown state
                break;
        }
    }

    @Override
    public void handleOpeningTag(String tag)
    {
        switch (myCollectState)
        {
            case WAITING:
                if (getMemberName().equals(tag))
                {
                    myCollectState = State.SEEK_GEOMETRY;
                }
                else if (getMembersName().equals(tag))
                {
                    myCollectState = State.SEEK_GEOMETRY;
                    myIsMultiMember = true;
                }
                break;
            case SEEK_GEOMETRY:
                myCurrentGeomHandler = getGeometryHandler(tag, isLatBeforeLong());
                myCollectState = myCurrentGeomHandler != null ? State.COLLECT_GEOMETRY : State.WAITING;
                break;
            case COLLECT_GEOMETRY:
                myCurrentGeomHandler.handleOpeningTag(tag);
                break;
            default:
                // Unknown state
                break;
        }
    }

    /** Enum used to track the current multi-geometry parse State. */
    private enum State
    {
        /** State while processing a Geometry element. */
        COLLECT_GEOMETRY,

        /** After a member tag is encountered, but before the geometry tag. */
        SEEK_GEOMETRY,

        /** State while waiting for a multi-type member tag. */
        WAITING
    }
}

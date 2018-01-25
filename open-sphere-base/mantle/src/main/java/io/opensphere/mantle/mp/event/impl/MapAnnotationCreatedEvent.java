package io.opensphere.mantle.mp.event.impl;

import java.util.List;

import io.opensphere.core.event.AbstractSingleStateEvent;
import io.opensphere.mantle.mp.MapAnnotationPoint;

/**
 * These events will either confirm single annotation point creation or notify
 * listeners to create a group of annotations from a list of points.
 */
public class MapAnnotationCreatedEvent extends AbstractSingleStateEvent
{
    /**
     * The name of the folder to create the points in, or null if it should use
     * the top level folder.
     */
    private String myFolderName;

    /** The Point list. */
    private List<? extends MapAnnotationPoint> myPointList;

    /** The single point created flag. */
    private boolean mySinglePointCreated;

    /** The Source. */
    private final Object mySource;

    /**
     * Instantiates a new manual point event.
     *
     * @param source the source
     * @param success the success
     */
    public MapAnnotationCreatedEvent(Object source, boolean success)
    {
        mySource = source;
        mySinglePointCreated = success;
    }

    /**
     * Instantiates a new map annotation point created event.
     *
     * @param source the source
     * @param points the points
     */
    public MapAnnotationCreatedEvent(Object source, List<? extends MapAnnotationPoint> points)
    {
        this(source, points, null);
    }

    /**
     * Instantiates a new map annotation point created event.
     *
     * @param source the source
     * @param points the points
     * @param folderName The name of the folder this points should be contained
     *            in, or null if no folder.
     */
    public MapAnnotationCreatedEvent(Object source, List<? extends MapAnnotationPoint> points, String folderName)
    {
        mySource = source;
        myPointList = points;
        myFolderName = folderName;
    }

    @Override
    public String getDescription()
    {
        return "MapAnnotationPointCreatedEvent";
    }

    /**
     * Gets the folder name the points should be put into.
     *
     * @return The folder name, or null if no folder.
     */
    public String getFolderName()
    {
        return myFolderName;
    }

    /**
     * Gets the point list.
     *
     * @return the point list
     */
    public List<? extends MapAnnotationPoint> getPointList()
    {
        return myPointList;
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    public Object getSource()
    {
        return mySource;
    }

    /**
     * Checks if is single point created.
     *
     * @return true, if is single point created
     */
    public boolean isSinglePointCreated()
    {
        return mySinglePointCreated;
    }
}

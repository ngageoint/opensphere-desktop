package io.opensphere.myplaces.editor.controller;

import java.awt.Point;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.Toolbox;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.viewbookmark.ViewBookmark;
import io.opensphere.core.viewbookmark.util.ViewBookmarkUtil;
import io.opensphere.core.viewer.impl.ViewerAnimator;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.mp.MapAnnotationPoint;
import io.opensphere.myplaces.models.DataCouple;
import io.opensphere.myplaces.models.MyPlacesModel;

/**
 * The Class AnnotationPointsController.
 */
public class AnnotationEditController
{
    /** String constant. */
    public static final String OFF_GLOBE = "Off Globe";

    /** The Constant OUR_IGNORE_CHANGES_SOURCE. */
    protected static final String OUR_IGNORE_CHANGES_SOURCE = "IGNORE_CHANGES_SOURCE";

    /** The my annotations helper. */
    private final AnnotationsHelper myAnnotationsHelper;

    /** The picking mode boolean. */
    private boolean myIsPickingMode;

    /** The my toolbox. */
    private final Toolbox myToolbox;

    /**
     * The model.
     */
    private final MyPlacesModel myModel;

    /**
     * Instantiates a new annotation points controller.
     *
     * @param toolbox the toolbox
     * @param model The model.
     */
    public AnnotationEditController(Toolbox toolbox, MyPlacesModel model)
    {
        myToolbox = toolbox;
        myModel = model;
        myAnnotationsHelper = new AnnotationsHelper(myToolbox);
    }

    /**
     * Center the map on a point or its associated look angle.
     *
     * @param entry the point to center on
     */
    public void centerOnPoint(MapAnnotationPoint entry)
    {
        // If we have a valid associated view go there, if we fail to go there
        // or don't have an associated view go to the point the regular way.
        boolean wentToView = false;
        String associatedView = entry.getAssociatedViewName();
        if (!StringUtils.isBlank(associatedView))
        {
            ViewBookmark vbm = myToolbox.getMapManager().getViewBookmarkRegistry().getViewBookmarkByName(associatedView);
            if (vbm != null)
            {
                wentToView = true;
                ViewBookmarkUtil.gotoView(myToolbox, vbm);
            }
        }

        if (!wentToView)
        {
            ViewerAnimator viewerAnimator = new ViewerAnimator(myToolbox.getMapManager().getStandardViewer(),
                    new GeographicPosition(LatLonAlt.createFromDegreesMeters(entry.getLat(), entry.getLon(), entry.getAltitude(),
                            Altitude.ReferenceLevel.ELLIPSOID)));
            viewerAnimator.start();
        }
    }

    /**
     * Convert a point to a geographic position.
     *
     * @param point The point.
     * @return The geographic position, or {@code null} if there is no
     *         geographic position at this point.
     */
    public final GeographicPosition convertPointToGeographicPosition(Point point)
    {
        return convertPointToGeographicPosition(point, ReferenceLevel.TERRAIN);
    }
    /**
     *
     * Convert a point to a geographic position.
     *
     * @param point The point.
     * @param pReferenceLevel the reference level of the point to create.
     * @return The geographic position, or {@code null} if there is no
     *         geographic position at this point.
     */
    public final GeographicPosition convertPointToGeographicPosition(Point point, ReferenceLevel pReferenceLevel)
    {
        return myToolbox.getMapManager().convertToPosition(new Vector2i(point), pReferenceLevel);
    }


    /**
     * Delete point.
     *
     * @param key the placemark id
     */
    public void deletePoint(String key)
    {
        DataCouple couple = getDataType(key);
        if (couple.getDataGroup() != null)
        {
            couple.getDataGroup().removeMember(couple.getDataType(), false, this);
        }
    }

    /**
     * Gets the annotations helper.
     *
     * @return the annotations helper
     */
    public AnnotationsHelper getAnnotationsHelper()
    {
        return myAnnotationsHelper;
    }

    /**
     * Gets the data type.
     *
     * @param key The key.
     * @return The data type and its parent.
     */
    public DataCouple getDataType(String key)
    {
        DataGroupInfo dgi = myModel.getDataGroups();
        return getDataTypeAndParent(key, dgi);
    }

    /**
     * Gets the toolbox.
     *
     * @return the my toolbox
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Standard getter.
     *
     * @return True if in picking mode, false otherwise.
     */
    public final boolean isPickingMode()
    {
        return myIsPickingMode;
    }

    /**
     * Sets the picking mode.
     *
     * @param picking the new picking mode
     */
    public void setPickingMode(boolean picking)
    {
        myIsPickingMode = picking;
    }

    /**
     * Gets the data type and its parent, given a type key.
     *
     * @param key The key of the data type to retrieve.
     * @param dataGroup The data group to search.
     * @return The data type and its parent or null if one was not found.
     */
    private DataCouple getDataTypeAndParent(String key, DataGroupInfo dataGroup)
    {
        DataCouple couple = null;

        for (DataTypeInfo member : dataGroup.getMembers(false))
        {
            if (member.getTypeKey().equals(key))
            {
                couple = new DataCouple(member, dataGroup);
                break;
            }
        }

        if (couple == null)
        {
            for (DataGroupInfo child : dataGroup.getChildren())
            {
                couple = getDataTypeAndParent(key, child);
                if (couple != null)
                {
                    break;
                }
            }
        }

        return couple;
    }
}

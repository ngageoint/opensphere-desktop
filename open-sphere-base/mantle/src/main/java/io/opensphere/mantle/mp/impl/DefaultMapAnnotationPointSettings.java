package io.opensphere.mantle.mp.impl;

import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.mantle.mp.MapAnnotationPointChangeEvent;
import io.opensphere.mantle.mp.MapAnnotationPointSettings;
import io.opensphere.mantle.mp.MutableMapAnnotationPoint;
import io.opensphere.mantle.mp.MutableMapAnnotationPointSettings;

/**
 * The Class DefaultMapAnnotationPointSettings.
 */
public class DefaultMapAnnotationPointSettings implements MutableMapAnnotationPointSettings
{
    /** Boolean for annotation hide. */
    private boolean myAnnoHideFlag;

    /** Boolean for description. */
    private boolean myDescriptionFlag;

    /** Boolean for degrees minutes seconds lat/lon. */
    private boolean myDMSFlag;

    /** Boolean for color. */
    private boolean myDotOnFlag = true;

    /** Boolean for latitude/longitude. */
    private boolean myLatLonFlag;

    /** Boolean for MGRS. */
    private boolean myMGRSFlag;

    /** Boolean for altitude. */
    private boolean myAltitudeFlag;

    /** Flag used to enable / disable display of distance. */
    private boolean myDistanceFlag;

    /** Flag used to enable / disable display of duration. */
    private boolean myDurationFlag;

    /** Flag used to enable / disable display of heading. */
    private boolean myHeadingFlag;

    /** Flag used to enable / disable display of velocity. */
    private boolean myVelocityFlag;

    /** The owner. */
    private transient MutableMapAnnotationPoint myPoint;

    /** Boolean for title. */
    private boolean myTitleFlag = true;

    /** Boolean for field title. */
    private boolean myFieldTitleFlag = true;

    /**
     * Append a value to the given string builder.
     *
     * @param sb The StringBuilder.
     * @param flag The boolean flag.
     */
    private static void appendOneZeroFlagValue(StringBuilder sb, boolean flag)
    {
        sb.append(flag ? '1' : '0');
    }

    /**
     * Default constructor.
     */
    public DefaultMapAnnotationPointSettings()
    {
        /* intentionally blank */
    }

    /**
     * Copy constructor.
     *
     * @param other What to copy from.
     */
    public DefaultMapAnnotationPointSettings(MapAnnotationPointSettings other)
    {
        myTitleFlag = other.isTitle();
        myDescriptionFlag = other.isDesc();
        myLatLonFlag = other.isLatLon();
        myDMSFlag = other.isDms();
        myMGRSFlag = other.isMgrs();
        myAltitudeFlag = other.isAltitude();
        myDotOnFlag = other.isDotOn();
        myAnnoHideFlag = other.isAnnohide();
        myFieldTitleFlag = other.isFieldTitle();
        myDistanceFlag = other.isDistance();
        myDurationFlag = other.isDuration();
        myHeadingFlag = other.isHeading();
        myVelocityFlag = other.isVelocity();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        MutableMapAnnotationPointSettings other = (MutableMapAnnotationPointSettings)obj;
        //@formatter:off
        return myAnnoHideFlag == other.isAnnohide()
                && myDescriptionFlag == other.isDesc()
                && myDMSFlag == other.isDms()
                && myDotOnFlag == other.isDotOn()
                && myLatLonFlag == other.isLatLon()
                && myMGRSFlag == other.isMgrs()
                && myAltitudeFlag == other.isAltitude()
                && myTitleFlag == other.isTitle()
                && myFieldTitleFlag == other.isFieldTitle()
                && myDistanceFlag == other.isDistance()
                && myDurationFlag == other.isDuration()
                && myHeadingFlag == other.isHeading()
                && myVelocityFlag == other.isVelocity();
        //@formatter:on
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myAnnoHideFlag);
        result = prime * result + HashCodeHelper.getHashCode(myDescriptionFlag);
        result = prime * result + HashCodeHelper.getHashCode(myDMSFlag);
        result = prime * result + HashCodeHelper.getHashCode(myDotOnFlag);
        result = prime * result + HashCodeHelper.getHashCode(myLatLonFlag);
        result = prime * result + HashCodeHelper.getHashCode(myMGRSFlag);
        result = prime * result + HashCodeHelper.getHashCode(myAltitudeFlag);
        result = prime * result + HashCodeHelper.getHashCode(myTitleFlag);
        result = prime * result + HashCodeHelper.getHashCode(myFieldTitleFlag);
        result = prime * result + HashCodeHelper.getHashCode(myDistanceFlag);
        result = prime * result + HashCodeHelper.getHashCode(myDurationFlag);
        result = prime * result + HashCodeHelper.getHashCode(myHeadingFlag);
        result = prime * result + HashCodeHelper.getHashCode(myVelocityFlag);

        return result;
    }

    /**
     * Standard getter.
     *
     * @return True if hide annotation flag is set, false otherwise.
     */
    @Override
    public boolean isAnnohide()
    {
        return myAnnoHideFlag;
    }

    /**
     * Standard getter.
     *
     * @return True if description flag is set, false otherwise.
     */
    @Override
    public boolean isDesc()
    {
        return myDescriptionFlag;
    }

    /**
     * Standard getter.
     *
     * @return True if degree/min/sec flag is set, false otherwise.
     */
    @Override
    public boolean isDms()
    {
        return myDMSFlag;
    }

    /**
     * Standard getter.
     *
     * @return True if color flag is set, false otherwise.
     */
    @Override
    public boolean isDotOn()
    {
        return myDotOnFlag;
    }

    /**
     * Standard getter.
     *
     * @return True if lat/lon flag is set, false otherwise.
     */
    @Override
    public boolean isLatLon()
    {
        return myLatLonFlag;
    }

    /**
     * Standard getter.
     *
     * @return True if MGRS flag is set, false otherwise.
     */
    @Override
    public boolean isMgrs()
    {
        return myMGRSFlag;
    }

    @Override
    public boolean isAltitude()
    {
        return myAltitudeFlag;
    }

    /**
     * Standard getter.
     *
     * @return True if title flag is set, false otherwise.
     */
    @Override
    public boolean isTitle()
    {
        return myTitleFlag;
    }

    @Override
    public boolean isFieldTitle()
    {
        return myFieldTitleFlag;
    }

    /**
     * Gets the value of the {@link #myDurationFlag} field.
     *
     * @return the value stored in the {@link #myDurationFlag} field.
     */
    @Override
    public boolean isDuration()
    {
        return myDurationFlag;
    }

    /**
     * Gets the value of the {@link #myHeadingFlag} field.
     *
     * @return the value stored in the {@link #myHeadingFlag} field.
     */
    @Override
    public boolean isHeading()
    {
        return myHeadingFlag;
    }

    /**
     * Gets the value of the {@link #myVelocityFlag} field.
     *
     * @return the value stored in the {@link #myVelocityFlag} field.
     */
    @Override
    public boolean isVelocity()
    {
        return myVelocityFlag;
    }

    /**
     * Gets the value of the {@link #myDistanceFlag} field.
     *
     * @return the value stored in the {@link #myDistanceFlag} field.
     */
    @Override
    public boolean isDistance()
    {
        return myDistanceFlag;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.mp.MutableMapAnnotationPointSettings#setAnnohide(boolean, java.lang.Object)
     */
    @Override
    public void setAnnohide(boolean annohide, Object source)
    {
        if (annohide != myAnnoHideFlag)
        {
            myAnnoHideFlag = annohide;
            fireValueChanged(new MapAnnotationPointChangeEvent(myPoint, Boolean.valueOf(myAnnoHideFlag), source));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.mp.MutableMapAnnotationPointSettings#setDesc(boolean, java.lang.Object)
     */
    @Override
    public void setDesc(boolean desc, Object source)
    {
        if (desc != myDescriptionFlag)
        {
            myDescriptionFlag = desc;
            fireValueChanged(new MapAnnotationPointChangeEvent(myPoint, Boolean.valueOf(myDescriptionFlag), source));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.mp.MutableMapAnnotationPointSettings#setDms(boolean, java.lang.Object)
     */
    @Override
    public void setDms(boolean dms, Object source)
    {
        if (dms != myDMSFlag)
        {
            myDMSFlag = dms;
            fireValueChanged(new MapAnnotationPointChangeEvent(myPoint, Boolean.valueOf(myDMSFlag), source));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.mp.MutableMapAnnotationPointSettings#setDotOn(boolean, java.lang.Object)
     */
    @Override
    public void setDotOn(boolean doton, Object source)
    {
        if (doton != myDotOnFlag)
        {
            myDotOnFlag = doton;
            fireValueChanged(new MapAnnotationPointChangeEvent(myPoint, Boolean.valueOf(myDotOnFlag), source));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.mp.MutableMapAnnotationPointSettings#setDuration(boolean, java.lang.Object)
     */
    @Override
    public void setDuration(boolean durationFlag, Object source)
    {
        if (durationFlag != myDurationFlag)
        {
            myDurationFlag = durationFlag;
            fireValueChanged(new MapAnnotationPointChangeEvent(myPoint, Boolean.valueOf(myDurationFlag), source));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.mp.MutableMapAnnotationPointSettings#setDistance(boolean, java.lang.Object)
     */
    @Override
    public void setDistance(boolean distanceFlag, Object source)
    {
        if (distanceFlag != myDescriptionFlag)
        {
            myDistanceFlag = distanceFlag;
            fireValueChanged(new MapAnnotationPointChangeEvent(myPoint, Boolean.valueOf(myDescriptionFlag), source));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.mp.MutableMapAnnotationPointSettings#setVelocity(boolean, java.lang.Object)
     */
    @Override
    public void setVelocity(boolean velocityFlag, Object source)
    {
        if (velocityFlag != myVelocityFlag)
        {
            myVelocityFlag = velocityFlag;
            fireValueChanged(new MapAnnotationPointChangeEvent(myPoint, Boolean.valueOf(myVelocityFlag), source));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.mp.MutableMapAnnotationPointSettings#setHeading(boolean, java.lang.Object)
     */
    @Override
    public void setHeading(boolean headingFlag, Object source)
    {
        if (headingFlag != myHeadingFlag)
        {
            myHeadingFlag = headingFlag;
            fireValueChanged(new MapAnnotationPointChangeEvent(myPoint, Boolean.valueOf(myHeadingFlag), source));
        }
    }

    /**
     * Sets the equal to.
     *
     * @param other the other to set equal to
     * @param source the source of the change
     * @param event true to event, false not not event even if there are
     *            changes.
     */
    public void setEqualTo(MapAnnotationPointSettings other, Object source, boolean event)
    {
        if (!equals(other))
        {
            myTitleFlag = other.isTitle();
            myDescriptionFlag = other.isDesc();
            myLatLonFlag = other.isLatLon();
            myDMSFlag = other.isDms();
            myMGRSFlag = other.isMgrs();
            myAltitudeFlag = other.isAltitude();
            myDotOnFlag = other.isDotOn();
            myAnnoHideFlag = other.isAnnohide();
            myFieldTitleFlag = other.isFieldTitle();

            myHeadingFlag = other.isHeading();
            myVelocityFlag = other.isVelocity();
            myDurationFlag = other.isDuration();
            myDistanceFlag = other.isDistance();

            if (event)
            {
                fireValueChanged(new MapAnnotationPointChangeEvent(myPoint, this, source));
            }
        }
    }

    @Override
    public void setLatLon(boolean latlon, Object source)
    {
        if (latlon != myLatLonFlag)
        {
            myLatLonFlag = latlon;
            fireValueChanged(new MapAnnotationPointChangeEvent(myPoint, Boolean.valueOf(myLatLonFlag), source));
        }
    }

    @Override
    public void setMapAnnotationPoint(MutableMapAnnotationPoint mmap)
    {
        myPoint = mmap;
    }

    @Override
    public void setMgrs(boolean mgrs, Object source)
    {
        if (mgrs != myMGRSFlag)
        {
            myMGRSFlag = mgrs;
            fireValueChanged(new MapAnnotationPointChangeEvent(myPoint, Boolean.valueOf(myMGRSFlag), source));
        }
    }

    @Override
    public void setAltitude(boolean altitude, Object source)
    {
        if (altitude != myAltitudeFlag)
        {
            myAltitudeFlag = altitude;
            fireValueChanged(new MapAnnotationPointChangeEvent(myPoint, Boolean.valueOf(myAltitudeFlag), source));
        }
    }

    @Override
    public void setTitle(boolean title, Object source)
    {
        if (title != myTitleFlag)
        {
            myTitleFlag = title;
            fireValueChanged(new MapAnnotationPointChangeEvent(myPoint, Boolean.valueOf(myTitleFlag), source));
        }
    }

    @Override
    public void setFieldTitle(boolean title, Object source)
    {
        if (title != myFieldTitleFlag)
        {
            myFieldTitleFlag = title;
            fireValueChanged(new MapAnnotationPointChangeEvent(myPoint, Boolean.valueOf(myFieldTitleFlag), source));
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        appendOneZeroFlagValue(sb, myTitleFlag);
        appendOneZeroFlagValue(sb, myDescriptionFlag);
        appendOneZeroFlagValue(sb, myLatLonFlag);
        appendOneZeroFlagValue(sb, myDMSFlag);
        appendOneZeroFlagValue(sb, myMGRSFlag);
        appendOneZeroFlagValue(sb, myAltitudeFlag);
        appendOneZeroFlagValue(sb, myDotOnFlag);
        appendOneZeroFlagValue(sb, myAnnoHideFlag);
        appendOneZeroFlagValue(sb, myFieldTitleFlag);
        appendOneZeroFlagValue(sb, myDistanceFlag);
        appendOneZeroFlagValue(sb, myDurationFlag);
        appendOneZeroFlagValue(sb, myHeadingFlag);
        appendOneZeroFlagValue(sb, myVelocityFlag);
        return sb.toString();
    }

    /**
     * Fire value changed.
     *
     * @param evt the event to fire.
     */
    private void fireValueChanged(MapAnnotationPointChangeEvent evt)
    {
        if (myPoint != null)
        {
            myPoint.fireChangeEvent(evt);
        }
    }
}

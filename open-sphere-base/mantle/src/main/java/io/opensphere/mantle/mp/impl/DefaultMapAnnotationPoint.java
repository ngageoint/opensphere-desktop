package io.opensphere.mantle.mp.impl;

import java.awt.Color;
import java.awt.Font;
import java.util.concurrent.atomic.AtomicLong;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.mantle.mp.MapAnnotationPoint;
import io.opensphere.mantle.mp.MapAnnotationPointChangeEvent;
import io.opensphere.mantle.mp.MutableMapAnnotationPoint;
import io.opensphere.mantle.mp.MutableMapAnnotationPointGroup;
import io.opensphere.mantle.mp.MutableMapAnnotationPointSettings;
import io.opensphere.mantle.mp.event.impl.MapAnnotationPointMemberChangedEvent;

/**
 * The Class DefaultMapAnnotationPoint.
 */
@SuppressWarnings("PMD.GodClass")
public class DefaultMapAnnotationPoint implements MutableMapAnnotationPoint
{
    /** The default color. */
    public static final Color DEFAULT_COLOR = new Color(84, 84, 107, 190);

    /** The our id counter. */
    private static AtomicLong ourIdCounter = new AtomicLong(1000);

    /** The altitude of the point. */
    private double myAltitude;

    /** The annotation settings. */
    private final DefaultMapAnnotationPointSettings myAnnoSettings;

    /** The Associated view name. */
    private String myAssociatedViewName;

    /** The background color. */
    private Color myBackgroundColor = DEFAULT_COLOR;

    /** The color. */
    private Color myColor = DEFAULT_COLOR;

    /** The description. */
    private String myDescription = "";

    /** Whether the bubble is filled. */
    private boolean myFilled = true;

    /** The font color. */
    private Font myFont;

    /** The font color. */
    private Color myFontColor = Color.white;

    /** The font position. */
    private String myFontSize = "";

    /** The group. */
    private MutableMapAnnotationPointGroup myGroup;

    /**
     * True if this point has altitude, false if it should be clamped to ground.
     */
    private boolean myHasAltitude;

    /** The Id. */
    private final long myId;

    /** The latitude. */
    private double myLat;

    /** The longitude. */
    private double myLon;

    /** The MGRS coords. */
    private String myMGRS = "";

    /**
     * The time of the point or null if no time.
     */
    private TimeSpan myTime;

    /**
     * True if this point has a time component, false if it is timeless.
     */
    private boolean myTimeEnabled;

    /** The title. */
    private String myTitle = "";

    /** The visible. */
    private boolean myVisible;

    /**
     * The x coordinate offset of the call out for the map point with respect to
     * the anchor position.
     */
    private int myXOffset;

    /**
     * The y coordinate offset of the call out for the map point with respect to
     * the anchor position.
     */
    private int myYOffset;

    /**
     * Gets the next my places ID.
     *
     * @return the next ID
     */
    public static final long getNextId()
    {
        return ourIdCounter.incrementAndGet();
    }

    /**
     * Constructor.
     */
    public DefaultMapAnnotationPoint()
    {
        myId = getNextId();
        myAnnoSettings = new DefaultMapAnnotationPointSettings();
        myAnnoSettings.setMapAnnotationPoint(this);
    }

    /**
     * COPY CTOR.
     *
     * @param other the other to copy.
     */
    public DefaultMapAnnotationPoint(MapAnnotationPoint other)
    {
        myId = getNextId();
        myAnnoSettings = new DefaultMapAnnotationPointSettings(other.getAnnoSettings());
        myAnnoSettings.setMapAnnotationPoint(this);
        myColor = other.getColor();
        myFilled = other.isFilled();
        myFont = other.getFont();
        myFontColor = other.getFontColor();
        myDescription = other.getDescription();
        myFontSize = other.getFontSize();
        myBackgroundColor = other.getBackgroundColor();
        myLat = other.getLat();
        myLon = other.getLon();
        myAltitude = other.getAltitude();
        myMGRS = other.getMGRS();
        myTitle = other.getTitle() == null ? "" : other.getTitle();
        myAssociatedViewName = other.getAssociatedViewName() == null ? "" : other.getAssociatedViewName();
        myVisible = other.isVisible();
        myXOffset = other.getxOffset();
        myYOffset = other.getyOffset();
    }

    @Override
    public void fireChangeEvent(MapAnnotationPointChangeEvent e)
    {
        if (myGroup != null)
        {
            myGroup.fireGroupInfoChangeEvent(new MapAnnotationPointMemberChangedEvent(myGroup, this, e, e.getSource()));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.mp.MapAnnotationPoint#getAltitude()
     */
    @Override
    public double getAltitude()
    {
        return myAltitude;
    }

    @Override
    public MutableMapAnnotationPointSettings getAnnoSettings()
    {
        return myAnnoSettings;
    }

    @Override
    public String getAssociatedViewName()
    {
        return myAssociatedViewName;
    }

    @Override
    public Color getBackgroundColor()
    {
        return myBackgroundColor;
    }

    @Override
    public Color getColor()
    {
        return myColor;
    }

    @Override
    public String getDescription()
    {
        return myDescription;
    }

    @Override
    public Font getFont()
    {
        if (myFont == null)
        {
            try
            {
                float size = Float.parseFloat(myFontSize);
                myFont = DEFAULT_FONT.deriveFont(size);
            }
            catch (NumberFormatException e)
            {
                myFont = DEFAULT_FONT;
            }
        }
        return myFont;
    }

    @Override
    public Color getFontColor()
    {
        return myFontColor;
    }

    @Override
    public String getFontSize()
    {
        return myFontSize;
    }

    @Override
    public MutableMapAnnotationPointGroup getGroup()
    {
        return myGroup;
    }

    @Override
    public long getId()
    {
        return myId;
    }

    @Override
    public double getLat()
    {
        return myLat;
    }

    @Override
    public double getLon()
    {
        return myLon;
    }

    @Override
    public String getMGRS()
    {
        return myMGRS;
    }

    @Override
    public TimeSpan getTime()
    {
        return myTime;
    }

    @Override
    public String getTitle()
    {
        return myTitle;
    }

    @Override
    public int getxOffset()
    {
        return myXOffset;
    }

    @Override
    public int getyOffset()
    {
        return myYOffset;
    }

    @Override
    public boolean hasAltitude()
    {
        return myHasAltitude;
    }

    @Override
    public boolean isFilled()
    {
        return myFilled;
    }

    @Override
    public boolean isVisible()
    {
        return myVisible;
    }

    @Override
    public boolean isTimeEnabled()
    {
        return myTimeEnabled;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.mp.MutableMapAnnotationPoint#setAltitude(double,
     *      java.lang.Object)
     */
    @Override
    public void setAltitude(double pAltitude, Object pEventSource)
    {
        if (myAltitude != pAltitude)
        {
            myHasAltitude = true;
            myAltitude = pAltitude;
            fireChangeEvent(new MapAnnotationPointChangeEvent(this, Double.valueOf(myAltitude), pEventSource));
        }
    }

    @Override
    public void setAssociatedViewName(String pName, Object source)
    {
        String name = pName == null ? "" : pName;
        if (!EqualsHelper.equals(myAssociatedViewName, name))
        {
            myAssociatedViewName = name;
            fireChangeEvent(new MapAnnotationPointChangeEvent(this, myAssociatedViewName, source));
        }
    }

    @Override
    public void setBackgroundColor(Color color, Object source)
    {
        if (!EqualsHelper.equals(myBackgroundColor, color))
        {
            myBackgroundColor = color;
            fireChangeEvent(new MapAnnotationPointChangeEvent(this, myBackgroundColor, source));
        }
    }

    @Override
    public void setColor(Color shapeColor, Object source)
    {
        if (!EqualsHelper.equals(myColor, shapeColor))
        {
            myColor = shapeColor;
            fireChangeEvent(new MapAnnotationPointChangeEvent(this, shapeColor, source));
        }
    }

    @Override
    public void setDescription(String desc, Object source)
    {
        if (!EqualsHelper.equals(myDescription, desc))
        {
            myDescription = desc;
            fireChangeEvent(new MapAnnotationPointChangeEvent(this, myDescription, source));
        }
    }

    @Override
    public void setEqualTo(MapAnnotationPoint other, Object source)
    {
        if (!equals(other))
        {
            myAnnoSettings.setEqualTo(other.getAnnoSettings(), source, false);
            myAnnoSettings.setMapAnnotationPoint(this);
            myColor = other.getColor();
            myFilled = other.isFilled();
            myFont = other.getFont();
            myFontColor = other.getFontColor();
            myDescription = other.getDescription();
            myFontSize = other.getFontSize();
            myBackgroundColor = other.getBackgroundColor();
            myLat = other.getLat();
            myLon = other.getLon();
            myAltitude = other.getAltitude();
            myMGRS = other.getMGRS();
            myTitle = other.getTitle() == null ? "" : other.getTitle();
            myAssociatedViewName = other.getAssociatedViewName() == null ? "" : other.getAssociatedViewName();
            myVisible = other.isVisible();
            myXOffset = other.getxOffset();
            myYOffset = other.getyOffset();
            fireChangeEvent(new MapAnnotationPointChangeEvent(this, this, source));
        }
    }

    @Override
    public void setFilled(boolean filled, Object source)
    {
        if (myFilled != filled)
        {
            myFilled = filled;
            fireChangeEvent(new MapAnnotationPointChangeEvent(this, Boolean.valueOf(myFilled), source));
        }
    }

    @Override
    public void setFont(Font font, Object source)
    {
        if (!EqualsHelper.equals(myFont, font))
        {
            myFont = font;
            fireChangeEvent(new MapAnnotationPointChangeEvent(this, font, source));
        }
    }

    @Override
    public void setFontColor(Color color, Object source)
    {
        if (!EqualsHelper.equals(myFontColor, color))
        {
            myFontColor = color;
            fireChangeEvent(new MapAnnotationPointChangeEvent(this, myFontColor, source));
        }
    }

    @Override
    public void setFontSize(String fontSize, Object source)
    {
        if (!EqualsHelper.equals(myFontSize, fontSize))
        {
            myFontSize = fontSize;
            fireChangeEvent(new MapAnnotationPointChangeEvent(this, fontSize, source));
        }
    }

    @Override
    public void setGroup(MutableMapAnnotationPointGroup group)
    {
        myGroup = group;
    }

    @Override
    public void setLat(double lat, Object source)
    {
        if (myLat != lat)
        {
            myLat = lat;
            fireChangeEvent(new MapAnnotationPointChangeEvent(this, Double.valueOf(myLat), source));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.mp.MutableMapAnnotationPoint#setLon(double,
     *      java.lang.Object)
     */
    @Override
    public void setLon(double lon, Object source)
    {
        if (myLon != lon)
        {
            myLon = lon;
            fireChangeEvent(new MapAnnotationPointChangeEvent(this, Double.valueOf(myLon), source));
        }
    }

    @Override
    public void setMGRS(String mgrs, Object source)
    {
        if (!EqualsHelper.equals(myMGRS, mgrs))
        {
            myMGRS = mgrs;
            fireChangeEvent(new MapAnnotationPointChangeEvent(this, myMGRS, source));
        }
    }

    /**
     * Sets the time for the point.
     *
     * @param time The time for the point or null if there isn't one.
     */
    public void setTime(TimeSpan time)
    {
        myTime = time;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.mp.MutableMapAnnotationPoint#setTimeEnabled(boolean,
     *      Object)
     */
    @Override
    public void setTimeEnabled(boolean timeEnabled, Object source)
    {
        if (timeEnabled != myTimeEnabled)
        {
            myTimeEnabled = timeEnabled;
            fireChangeEvent(new MapAnnotationPointChangeEvent(this, Boolean.valueOf(myTimeEnabled), source));
        }
    }

    @Override
    public void setTitle(String pTitle, Object source)
    {
        String title = pTitle == null ? "" : pTitle;
        if (!EqualsHelper.equals(myTitle, title))
        {
            myTitle = title;
            fireChangeEvent(new MapAnnotationPointChangeEvent(this, myTitle, source));
        }
    }

    @Override
    public void setVisible(boolean visible, Object source)
    {
        if (myVisible != visible)
        {
            myVisible = visible;
            fireChangeEvent(new MapAnnotationPointChangeEvent(this, Boolean.valueOf(myVisible), source));
        }
    }

    @Override
    public void setxOffset(int xOffset, Object source)
    {
        if (myXOffset != xOffset)
        {
            myXOffset = xOffset;
            fireChangeEvent(new MapAnnotationPointChangeEvent(this, Integer.valueOf(xOffset), source));
        }
    }

    @Override
    public void setXYOffset(int xOffset, int yOffset, Object source)
    {
        boolean changed = false;
        if (myXOffset != xOffset)
        {
            myXOffset = xOffset;
            changed = true;
        }
        if (myYOffset != yOffset)
        {
            myYOffset = yOffset;
            changed = true;
        }
        if (changed)
        {
            fireChangeEvent(new MapAnnotationPointChangeEvent(this, Integer.valueOf(xOffset), source));
        }
    }

    @Override
    public void setyOffset(int yOffset, Object source)
    {
        if (myYOffset != yOffset)
        {
            myYOffset = yOffset;
            fireChangeEvent(new MapAnnotationPointChangeEvent(this, Integer.valueOf(myYOffset), source));
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append("MAPPOINT{TITLE[").append(getTitle()).append("], DESC[");
        sb.append(getDescription()).append("], LAT[");
        sb.append(getLat()).append("], LON[");
        sb.append(getLon()).append("]}");
        return sb.toString();
    }
}

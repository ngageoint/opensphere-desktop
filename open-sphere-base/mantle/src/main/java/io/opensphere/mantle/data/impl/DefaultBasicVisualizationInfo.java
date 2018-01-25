package io.opensphere.mantle.data.impl;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.StrongObservableValue;
import io.opensphere.core.util.lang.ToStringHelper;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.LoadsTo;

/**
 * The Class DefaultBasicVisualizationInfo.
 */
@ThreadSafe
public class DefaultBasicVisualizationInfo implements BasicVisualizationInfo
{
    /** The Constant ALL_TYPES. */
    public static final Set<LoadsTo> LOADS_TO_ALL_TYPES;

    /** The Constant BASE_AND_STATIC. */
    public static final Set<LoadsTo> LOADS_TO_BASE_AND_STATIC;

    /** The Constant BASE_AND_TIMELINE. */
    public static final Set<LoadsTo> LOADS_TO_BASE_AND_TIMELINE;

    /** The Constant BASE_ONLY. */
    public static final Set<LoadsTo> LOADS_TO_BASE_ONLY;

    /** The Constant STATIC_AND_TIMELINE. */
    public static final Set<LoadsTo> LOADS_TO_STATIC_AND_TIMELINE;

    /** The Constant STATIC_ONLY. */
    public static final Set<LoadsTo> LOADS_TO_STATIC_ONLY;

    /** The Constant TIMELINE_ONLY. */
    public static final Set<LoadsTo> LOADS_TO_TIMELINE_ONLY;

    /** The loads to. */
    private final ObservableValue<LoadsTo> myLoadsTo = new StrongObservableValue<>();

    /** The supported loads to types. */
    @GuardedBy("mySupportedLoadsToTypes")
    private final Set<LoadsTo> mySupportedLoadsToTypes;

    /** The default type color. */
    private volatile Color myDefaultTypeColor = Color.white;

    /** The type color. */
    private final ObservableValue<Color> myTypeColor = new StrongObservableValue<>();

    /** The uses data elements. */
    private volatile boolean myUsesDataElements;

    static
    {
        LOADS_TO_BASE_ONLY = Collections.unmodifiableSet(EnumSet.of(LoadsTo.BASE));
        LOADS_TO_STATIC_ONLY = Collections.unmodifiableSet(EnumSet.of(LoadsTo.STATIC));
        LOADS_TO_TIMELINE_ONLY = Collections.unmodifiableSet(EnumSet.of(LoadsTo.TIMELINE));
        LOADS_TO_BASE_AND_TIMELINE = Collections.unmodifiableSet(EnumSet.of(LoadsTo.BASE, LoadsTo.TIMELINE));
        LOADS_TO_BASE_AND_STATIC = Collections.unmodifiableSet(EnumSet.of(LoadsTo.BASE, LoadsTo.STATIC));
        LOADS_TO_STATIC_AND_TIMELINE = Collections.unmodifiableSet(EnumSet.of(LoadsTo.STATIC, LoadsTo.TIMELINE));
        LOADS_TO_ALL_TYPES = Collections.unmodifiableSet(EnumSet.of(LoadsTo.BASE, LoadsTo.STATIC, LoadsTo.TIMELINE));
    }

    /**
     * Instantiates a new default basic visualization info.
     *
     * @param defaultLoadsTo the loads to
     * @param defaultTypeColor the default type color
     * @param usesDataElements the uses data elements
     */
    public DefaultBasicVisualizationInfo(LoadsTo defaultLoadsTo, Color defaultTypeColor, boolean usesDataElements)
    {
        this(defaultLoadsTo, null, defaultTypeColor, usesDataElements);
    }

    /**
     * Instantiates a new default basic visualization info.
     *
     * @param defaultLoadsTo the default {@link LoadsTo} type.
     * @param supportedLoadsToTypes the complete set of {@link LoadsTo} types
     *            supported by this data type.
     * @param defaultTypeColor the default type color
     * @param usesDataElements the uses data elements
     */
    public DefaultBasicVisualizationInfo(LoadsTo defaultLoadsTo, Collection<LoadsTo> supportedLoadsToTypes,
            Color defaultTypeColor, boolean usesDataElements)
    {
        LoadsTo loadsTo = defaultLoadsTo == null ? LoadsTo.BASE : defaultLoadsTo;
        myLoadsTo.set(loadsTo);
        mySupportedLoadsToTypes = EnumSet.of(loadsTo);
        if (supportedLoadsToTypes != null && !supportedLoadsToTypes.isEmpty())
        {
            mySupportedLoadsToTypes.addAll(supportedLoadsToTypes);
        }

        if (defaultTypeColor != null)
        {
            myDefaultTypeColor = defaultTypeColor;
        }
        myTypeColor.set(new Color(myDefaultTypeColor.getRGB(), true));

        myUsesDataElements = usesDataElements;
    }

    @Override
    public Color getDefaultTypeColor()
    {
        return myDefaultTypeColor;
    }

    @Override
    public LoadsTo getLoadsTo()
    {
        return myLoadsTo.get();
    }

    @Override
    public Set<LoadsTo> getSupportedLoadsToTypes()
    {
        synchronized (mySupportedLoadsToTypes)
        {
            return Collections.unmodifiableSet(mySupportedLoadsToTypes);
        }
    }

    @Override
    public Color getTypeColor()
    {
        return myTypeColor.get();
    }

    @Override
    public int getTypeOpacity()
    {
        return myTypeColor.get().getAlpha();
    }

    @Override
    public void restoreDefaultColor(Object source)
    {
        setTypeColor(myDefaultTypeColor, source);
    }

    @Override
    public void setLoadsTo(LoadsTo l, Object source)
    {
        if (myLoadsTo.set(l))
        {
            synchronized (mySupportedLoadsToTypes)
            {
                mySupportedLoadsToTypes.add(l);
            }
        }
    }

    @Override
    public void setSupportedLoadsToTypes(Collection<LoadsTo> types)
    {
        synchronized (mySupportedLoadsToTypes)
        {
            mySupportedLoadsToTypes.clear();
            if (types != null)
            {
                mySupportedLoadsToTypes.addAll(types);
            }
        }
    }

    @Override
    public void setTypeColor(Color c, Object source)
    {
        myTypeColor.set(c);
    }

    @Override
    public void setTypeOpacity(int alpha, Object source)
    {
        Color typeColor = myTypeColor.get();
        int r = typeColor.getRed();
        int g = typeColor.getGreen();
        int b = typeColor.getBlue();
        setTypeColor(new Color(r, g, b, alpha), source);
    }

    @Override
    public void setUsesDataElements(boolean usesDataElements)
    {
        myUsesDataElements = usesDataElements;
    }

    @Override
    public boolean supportsLoadsTo(LoadsTo loadsTo)
    {
        if (myLoadsTo.get() == loadsTo)
        {
            return true;
        }
        synchronized (mySupportedLoadsToTypes)
        {
            return mySupportedLoadsToTypes.contains(loadsTo);
        }
    }

    @Override
    public boolean usesDataElements()
    {
        return myUsesDataElements;
    }

    @Override
    public ObservableValue<LoadsTo> loadsToProperty()
    {
        return myLoadsTo;
    }

    @Override
    public ObservableValue<Color> typeColorProperty()
    {
        return myTypeColor;
    }

    @Override
    public String toString()
    {
        return toStringMultiLine(1);
    }

    /**
     * Returns a multi-line string representation of the object.
     *
     * @param indentLevel the indent level (0-based)
     * @return the multi-line string
     */
    public String toStringMultiLine(int indentLevel)
    {
        ToStringHelper helper = new ToStringHelper(this, 256);
        helper.add("Loads To", myLoadsTo.get());
        synchronized (mySupportedLoadsToTypes)
        {
            helper.add("Supports", mySupportedLoadsToTypes);
        }
        helper.add("Type Color", ColorUtilities.convertToRGBAColorString(myTypeColor.get()));
        helper.add("Default Type Color", ColorUtilities.convertToRGBAColorString(myDefaultTypeColor));
        helper.add("Uses Data Elements", myUsesDataElements);
        return helper.toStringMultiLine(indentLevel);
    }

    /**
     * Allow derived types to set the default type color.
     *
     * @param c the new default type color
     */
    protected void setDefaultTypeColor(Color c)
    {
        myDefaultTypeColor = c == null ? Color.WHITE : c;
    }
}

package io.opensphere.mantle.data.geom.impl;

import java.awt.Color;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.lang.BitArrays;
import io.opensphere.mantle.data.geom.CallOutSupport;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.util.TimeSpanUtility;

/**
 * The Class SimpleLocationGeometrySupport.
 */
public abstract class AbstractSimpleGeometrySupport implements MapGeometrySupport
{
    /** Mask for Follow Terrain flag. */
    public static final byte FOLLOW_TERRAIN_MASK = 1;

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The value of the highest bit defined by this class. This is to be used as
     * an offset by subclasses that need to define their own bits.
     */
    protected static final byte HIGH_BIT = FOLLOW_TERRAIN_MASK;

    /**
     * Color of the support. Stored as an integer behind the scenes to save
     * memory as Color is larger
     */
    private int myColor = java.awt.Color.WHITE.getRGB();

    /** The my duration. */
    private long myEndTime = TimeSpanUtility.TIMELESS_END;

    /** Bit Field for flag storage. 8 bits max. */
    private byte myFlagField1;

    /** The my start time. */
    private long myStartTime = TimeSpanUtility.TIMELESS_START;

    /** Default constructor. */
    public AbstractSimpleGeometrySupport()
    {
        super();
    }
    
    /**
     * Copy constructor.
     *
     * @param source the source object from which to copy data.
     */
    public AbstractSimpleGeometrySupport(AbstractSimpleGeometrySupport source)
    {
        myColor = source.myColor;
        myEndTime = source.myEndTime;
        myFlagField1 = source.myFlagField1;
        myStartTime = source.myStartTime;
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
        AbstractSimpleGeometrySupport other = (AbstractSimpleGeometrySupport)obj;
        return myColor == other.myColor && myEndTime == other.myEndTime && getFlagField() == other.getFlagField()
                && myStartTime == other.myStartTime;
    }

    @Override
    public boolean followTerrain()
    {
        return isFlagSet(FOLLOW_TERRAIN_MASK);
    }

    @Override
    public CallOutSupport getCallOutSupport()
    {
        return null;
    }

    @Override
    public List<MapGeometrySupport> getChildren()
    {
        return null;
    }

    @Override
    @NonNull
    public Color getColor()
    {
        return new Color(myColor, true);
    }

    @Override
    public TimeSpan getTimeSpan()
    {
        return TimeSpanUtility.fromStartEnd(myStartTime, myEndTime);
    }

    @Override
    public String getToolTip()
    {
        return null;
    }

    @Override
    public boolean hasChildren()
    {
        return false;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + myColor;
        result = prime * result + (int)(myEndTime ^ myEndTime >>> 32);
        result = prime * result + getFlagField();
        result = prime * result + (int)(myStartTime ^ myStartTime >>> 32);
        return result;
    }

    /**
     * Checks to see if a flag is set in the internal bit field.
     *
     * @param mask - the mask to check
     * @return true if set, false if not
     */
    public synchronized boolean isFlagSet(byte mask)
    {
        return BitArrays.isFlagSet(mask, myFlagField1);
    }

    @Override
    public void setCallOutSupport(CallOutSupport cos)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setColor(Color c, Object source)
    {
        myColor = c == null ? 0 : c.getRGB();
    }

    /**
     * Sets (or un-sets) a flag in the internal bit field.
     *
     * @param mask - the mask to use
     * @param on - true to set on, false to set off
     * @return true if changed.
     */
    public synchronized boolean setFlag(byte mask, boolean on)
    {
        byte newBitField = BitArrays.setFlag(mask, on, myFlagField1);
        myFlagField1 = newBitField;
        return newBitField != myFlagField1;
    }

    @Override
    public void setFollowTerrain(boolean follow, Object source)
    {
        setFlag(FOLLOW_TERRAIN_MASK, follow);
    }

    @Override
    public void setTimeSpan(TimeSpan ts)
    {
        myStartTime = TimeSpanUtility.getWorkaroundStart(ts);
        myEndTime = TimeSpanUtility.getWorkaroundEnd(ts);
    }

    @Override
    public void setToolTip(String tip)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the flag field.
     *
     * @return the flag field
     */
    private synchronized byte getFlagField()
    {
        return myFlagField1;
    }
}

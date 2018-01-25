package io.opensphere.core.geometry.renderproperties;

import java.io.Serializable;

/** A model for line stipple configuration. */
@SuppressWarnings("PMD.AvoidUsingShortType")
public class StippleModelConfig implements Serializable
{
    /** A simple dotted line pattern. */
    public static final StippleModelConfig DASH_DASH_DOT;

    /** A simple dotted line pattern. */
    public static final StippleModelConfig DASH_DOT;

    /** A simple dashed line pattern. */
    public static final StippleModelConfig DASHED;

    /** A simple dashed line pattern. */
    public static final StippleModelConfig DASHED_2;

    /** A simple dotted line pattern. */
    public static final StippleModelConfig DOTTED;

    /** A simple dotted line pattern. */
    public static final StippleModelConfig DOTTED_2;

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * The multiplier for each bit in the pattern. For example, a pattern of
     * "1010" with a factor of "2" will give "11001100" in the actual line.
     */
    private final int myFactor;

    /** The pattern of bits ("1" is on "0" is off). */
    private final short myPattern;

    static
    {
        Builder builder = new Builder();
        builder.setBitPattern((short)0xAAAA);
        builder.setFactor(1);
        DOTTED = new StippleModelConfig(builder);

        builder.setBitPattern((short)0x0183);
        DOTTED_2 = new StippleModelConfig(builder);

        builder.setBitPattern((short)0xF8F8);
        DASHED = new StippleModelConfig(builder);

        builder.setBitPattern((short)0xF00F);
        DASHED_2 = new StippleModelConfig(builder);

        builder.setBitPattern((short)0x07C3);
        DASH_DOT = new StippleModelConfig(builder);

        builder.setBitPattern((short)0x1C47);
        DASH_DASH_DOT = new StippleModelConfig(builder);
    }

    /**
     * Constructor.
     *
     * @param builder Builder which contains my configuration settings.
     */
    public StippleModelConfig(Builder builder)
    {
        myFactor = builder.getFactor();
        myPattern = builder.getBitPattern();
    }

    /**
     * Create a builder that may be used to create a duplicate of this
     * configuration.
     *
     * @return A builder instance.
     */
    public Builder createBuilder()
    {
        Builder builder = new Builder();
        builder.setFactor(myFactor);
        builder.setBitPattern(myPattern);
        return builder;
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
        StippleModelConfig other = (StippleModelConfig)obj;
        return myFactor == other.myFactor && myPattern == other.myPattern;
    }

    /**
     * Get the factor.
     *
     * @return the factor
     */
    public int getFactor()
    {
        return myFactor;
    }

    /**
     * Get the pattern.
     *
     * @return the pattern
     */
    public short getPattern()
    {
        return myPattern;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + myFactor;
        result = prime * result + myPattern;
        return result;
    }

    /** Builder for setting the stipple model settings. */
    public static class Builder
    {
        /** The pattern of bits ("1" is on "0" is off). */
        private short myBitPattern;

        /**
         * The multiplier for each bit in the pattern. For example, a pattern of
         * "1010" with a factor of "2" will give "11001100" in the actual line.
         */
        private int myFactor;

        /**
         * Get the bitPattern.
         *
         * @return the bitPattern
         */
        public short getBitPattern()
        {
            return myBitPattern;
        }

        /**
         * Get the factor.
         *
         * @return the factor
         */
        public int getFactor()
        {
            return myFactor;
        }

        /**
         * Set the bitPattern.
         *
         * @param bitPattern the bitPattern to set
         */
        public void setBitPattern(short bitPattern)
        {
            myBitPattern = bitPattern;
        }

        /**
         * Set the factor.
         *
         * @param factor the factor to set
         */
        public void setFactor(int factor)
        {
            myFactor = factor;
        }
    }
}

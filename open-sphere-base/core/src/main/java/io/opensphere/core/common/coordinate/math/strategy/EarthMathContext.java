package io.opensphere.core.common.coordinate.math.strategy;

import io.opensphere.core.common.coordinate.math.EarthMath;

/**
 * This context completes the strategy pattern.
 */
public class EarthMathContext
{

    private static Vincenty myVincenty = new Vincenty();

    private static Haversine myHaversine = new Haversine();

    private EarthMath myEarthMathStrategy = null;

    /**
     * Defaults to the better model.
     */
    public EarthMathContext()
    {
        myEarthMathStrategy = new Vincenty();
    }

    /**
     * Takes the recommended strategy.
     *
     * @param inEarthMathStrategy
     */
    public EarthMathContext(EarthMath inEarthMathStrategy)
    {
        myEarthMathStrategy = inEarthMathStrategy;
    }

    /**
     * Returns the current strategy.
     *
     * @return
     */
    public EarthMath getEarthMathStrategy()
    {
        return myEarthMathStrategy;
    }

    public void setEarthMathStragey(EarthMath myEarthMathStragey)
    {
        myEarthMathStrategy = myEarthMathStragey;
    }

    // ********************* STATICS *************************************

    /**
     * Returns the static Vincenty strategy.
     *
     * @return
     */
    public static EarthMath getStaticVincenty()
    {
        return myVincenty;
    }

    /**
     * Returns the static Haversine strategy.
     *
     * @return
     */
    public static EarthMath getStaticHaversine()
    {
        return myHaversine;
    }

    /**
     * Returns the best strategy.
     *
     * @return
     */
    public static EarthMath getStaticBestEarthMathStrategy()
    {
        return myVincenty;
    }
}

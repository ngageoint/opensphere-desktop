package io.opensphere.core.units.angle;

import java.util.List;
import java.util.concurrent.locks.Lock;

import io.opensphere.core.units.AbstractUnitsProvider;
import io.opensphere.core.units.InvalidUnitsException;
import io.opensphere.core.util.lang.ImpossibleException;

/**
 * A units provider for angle units.
 */
public final class AngleUnitsProvider extends AbstractUnitsProvider<Angle>
{
    /**
     * Constructor. This initializes the units provider with the following
     * units:
     * <ul>
     * <li>decimal degrees</li>
     * <li>degrees minutes seconds</li>
     * </ul>
     * Additional units may be added with {@link #addUnits(Class)}; units may be
     * removed with {@link #removeUnits(Class)}.
     */
    public AngleUnitsProvider()
    {
        List<Class<? extends Angle>> units = getAvailableUnitsUnsync();
        units.add(DegreesMinutesSeconds.class);
        units.add(DegDecimalMin.class);
        units.add(DecimalDegrees.class);
        setPreferredUnits(units.get(0));
    }

    @Override
    public <T extends Angle> T convert(Class<T> desiredType, Angle source) throws InvalidUnitsException
    {
        return Angle.create(desiredType, source);
    }

    @Override
    public <T extends Angle> T fromUnitsAndMagnitude(Class<T> type, Number magnitude) throws InvalidUnitsException
    {
        return Angle.create(type, magnitude.doubleValue());
    }

    @Override
    public Class<? extends Angle> getDisplayClass(Angle value)
    {
        return value.getClass();
    }

    @Override
    public String getLongLabel(Class<? extends Angle> type, boolean plural) throws InvalidUnitsException
    {
        return Angle.getLongLabel(type);
    }

    @Override
    public String getSelectionLabel(Class<? extends Angle> type) throws InvalidUnitsException
    {
        return Angle.getSelectionLabel(type);
    }

    @Override
    public String getShortLabel(Class<? extends Angle> type, boolean plural) throws InvalidUnitsException
    {
        return Angle.getShortLabel(type);
    }

    @Override
    public Class<Angle> getSuperType()
    {
        return Angle.class;
    }

    @Override
    public Class<? extends Angle> getUnitsWithLongLabel(String label)
    {
        Lock lock = getLock();
        lock.lock();
        try
        {
            for (Class<? extends Angle> type : getAvailableUnitsUnsync())
            {
                try
                {
                    if (Angle.getLongLabel(type).equalsIgnoreCase(label) || Angle.getLongLabel(type).equalsIgnoreCase(label))
                    {
                        return type;
                    }
                }
                catch (InvalidUnitsException e)
                {
                    throw new ImpossibleException(e);
                }
            }
            return null;
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public String toShortLabelString(Angle obj)
    {
        return obj.toShortLabelString();
    }

    @Override
    protected Class<? extends Number> getValueType()
    {
        return Angle.VALUE_TYPE;
    }

    @Override
    protected void testUnits(Class<? extends Angle> type) throws InvalidUnitsException
    {
        Angle.create(type, 0.);
    }
}

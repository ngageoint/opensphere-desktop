package io.opensphere.core.units.angle;

import java.util.List;
import java.util.concurrent.locks.Lock;

import io.opensphere.core.units.AbstractUnitsProvider;
import io.opensphere.core.units.InvalidUnitsException;
import io.opensphere.core.util.lang.ImpossibleException;

/**
 * A units provider for angle units.
 */
public final class AngleUnitsProvider extends AbstractUnitsProvider<Coordinates>
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
        List<Class<? extends Coordinates>> units = getAvailableUnitsUnsync();
        units.add(DegreesMinutesSeconds.class);
        units.add(DegDecimalMin.class);
        units.add(DecimalDegrees.class);
        units.add(MGRS.class);
        setPreferredUnits(units.get(0));
    }

    @Override
    public <T extends Coordinates> T convert(Class<T> desiredType, Coordinates source) throws InvalidUnitsException
    {
        return Coordinates.create(desiredType, source);
    }

    @Override
    public <T extends Coordinates> T fromUnitsAndMagnitude(Class<T> type, Number magnitude) throws InvalidUnitsException
    {
        return Coordinates.create(type, magnitude.doubleValue());
    }

    @Override
    public Class<? extends Coordinates> getDisplayClass(Coordinates value)
    {
        return value.getClass();
    }

    @Override
    public String getLongLabel(Class<? extends Coordinates> type, boolean plural) throws InvalidUnitsException
    {
        return Coordinates.getLongLabel(type);
    }

    @Override
    public String getSelectionLabel(Class<? extends Coordinates> type) throws InvalidUnitsException
    {
        return Coordinates.getSelectionLabel(type);
    }

    @Override
    public String getShortLabel(Class<? extends Coordinates> type, boolean plural) throws InvalidUnitsException
    {
        return Coordinates.getShortLabel(type);
    }

    @Override
    public Class<Coordinates> getSuperType()
    {
        return Coordinates.class;
    }

    @Override
    public Class<? extends Coordinates> getUnitsWithLongLabel(String label)
    {
        Lock lock = getLock();
        lock.lock();
        try
        {
            for (Class<? extends Coordinates> type : getAvailableUnitsUnsync())
            {
                try
                {
                    if (Coordinates.getLongLabel(type).equalsIgnoreCase(label) || Coordinates.getLongLabel(type).equalsIgnoreCase(label))
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
    public String toShortLabelString(Coordinates obj)
    {
        return obj.toShortLabelString();
    }

    @Override
    protected Class<? extends Number> getValueType()
    {
        return Coordinates.VALUE_TYPE;
    }

    @Override
    protected void testUnits(Class<? extends Coordinates> type) throws InvalidUnitsException
    {
        Coordinates.create(type, 0.);
    }
}

package io.opensphere.core.units.length;

import java.util.List;

import io.opensphere.core.units.AbstractUnitsProvider;
import io.opensphere.core.units.InvalidUnitsException;

/**
 * A units provider for length units.
 */
public final class LengthUnitsProvider extends AbstractUnitsProvider<Length>
{
    /**
     * Constructor. This initializes the units provider with the following
     * units:
     * <ul>
     * <li>auto-scale metric</li>
     * <li>auto-scale imperial</li>
     * <li>auto-scale nautical</li>
     * <li>feet</li>
     * <li>inches</li>
     * <li>meters</li>
     * <li>kilometers</li>
     * <li>nautical miles</li>
     * <li>statute miles</li>
     * <li>yards</li>
     * </ul>
     * Additional units may be added with {@link #addUnits(Class)}; units may be
     * removed with {@link #removeUnits(Class)}.
     */
    public LengthUnitsProvider()
    {
        List<Class<? extends Length>> units = getAvailableUnitsUnsync();
        units.add(AutoscaleMetric.class);
        units.add(AutoscaleImperial.class);
        units.add(AutoscaleNautical.class);
        units.add(Feet.class);
        units.add(Inches.class);
        units.add(Meters.class);
        units.add(Kilometers.class);
        units.add(NauticalMiles.class);
        units.add(StatuteMiles.class);
        units.add(Yards.class);
        setPreferredUnits(units.get(0));
    }

    @Override
    public <T extends Length> T convert(Class<T> desiredType, Length source) throws InvalidUnitsException
    {
        return Length.create(desiredType, source);
    }

    @Override
    public <T extends Length> T fromUnitsAndMagnitude(Class<T> type, Number magnitude) throws InvalidUnitsException
    {
        return Length.create(type, magnitude.doubleValue());
    }

    @Override
    public Class<? extends Length> getDisplayClass(Length value)
    {
        return value.getDisplayClass();
    }

    @Override
    public String getLongLabel(Class<? extends Length> type, boolean plural) throws InvalidUnitsException
    {
        return Length.getLongLabel(type, plural);
    }

    @Override
    public String getSelectionLabel(Class<? extends Length> type) throws InvalidUnitsException
    {
        return Length.getSelectionLabel(type);
    }

    @Override
    public String getShortLabel(Class<? extends Length> type, boolean plural) throws InvalidUnitsException
    {
        return Length.getShortLabel(type, plural);
    }

    @Override
    public Class<Length> getSuperType()
    {
        return Length.class;
    }

    @Override
    public String toShortLabelString(Length obj)
    {
        return obj.toShortLabelString();
    }

    @Override
    protected Class<? extends Number> getValueType()
    {
        return Length.VALUE_TYPE;
    }

    @Override
    protected void testUnits(Class<? extends Length> type) throws InvalidUnitsException
    {
        Length.create(type, 0.);
        Length.create(type, new Meters(0.));
    }
}

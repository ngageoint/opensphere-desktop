package io.opensphere.core.common.util;

/**
 * Cheap class to handle Unit Conversions.
 * <p>
 * Conversion factors taken from:
 * http://www.taylormade.com.au/billspages/conversion_table.html
 */
public class UnitConversion
{
    /** .621371192237334 miles. */
    static final double KM_TO_MILE = .621371192237334;

    /** .53995680345572354211 nautical miles. */
    static final double KM_TO_NM = .53995680345572354211;

    /** 1093.6132983377079 yards. */
    static final double KM_TO_YARD = 1093.6132983377079;

    /** 3280.839895013123 feet. */
    static final double KM_TO_FEET = 3280.839895013123;

    /** 39370.078740157485 inches. */
    static final double KM_TO_INCH = 39370.078740157485;

    /** 1000 meters. */
    static final double KM_TO_METER = 1000;

    /** .8684229270767005 nautical miles. */
    static final double MI_TO_NM = .8684229270767005;

    /** 1760 yards. */
    static final double MI_TO_YARD = 1760;

    /** 5280 feet. */
    static final double MI_TO_FEET = 5280;

    /** 63360 inches. */
    static final double MI_TO_INCH = 63360;

    /** 1.609344 kilometers. */
    static final double MI_TO_KM = 1.609344;

    /** 1609.344 meters. */
    static final double MI_TO_METER = 1609.344;

    /** 1.1515126660303825 miles. */
    static final double NM_TO_MI = 1.1515126660303825;

    /** 2026.6622922134734 yards. */
    static final double NM_TO_YARD = 2026.6622922134734;

    /** 6079.98687664042 feet. */
    static final double NM_TO_FEET = 6079.98687664042;

    /** 72959.84251968504 inches. */
    static final double NM_TO_INCH = 72959.84251968504;

    /** 1.852 kilometers. */
    static final double NM_TO_KM = 1.852;

    /** 1852 meters. */
    static final double NM_TO_METER = 1852;

    /** .0006213711922373339 miles. */
    static final double METER_TO_MI = .0006213711922373339;

    /** .00053995680345572354 nautical miles. */
    static final double METER_TO_NM = .00053995680345572354;

    /** 1.0936132983377078 yards. */
    static final double METER_TO_YARD = 1.0936132983377078;

    /** 3.2808398950131235 feet. */
    public static final double METER_TO_FEET = 3.2808398950131235;

    /** 39.37007874015748 inches. */
    static final double METER_TO_INCH = 39.37007874015748;

    /** .001 kilometers. */
    static final double METER_TO_KM = .001;

    /** .000015782828282828283 miles. */
    static final double INCH_TO_MI = .000015782828282828283;

    /** .000013706169934922673 nautical miles. */
    static final double INCH_TO_NM = .000013706169934922673;

    /** .027777777777777776 yards. */
    static final double INCH_TO_YARD = .027777777777777776;

    /** .08333333333333333 feet. */
    static final double INCH_TO_FEET = .08333333333333333;

    /** .000025399999999999997 kilometers. */
    static final double INCH_TO_KM = .000025399999999999997;

    /** .0254 meters. */
    static final double INCH_TO_METER = .0254;

    /** .0001893939393939394 miles. */
    static final double FEET_TO_MILE = .0001893939393939394;

    /** .0001644740392190721 nautical miles. */
    static final double FEET_TO_NM = .0001644740392190721;

    /** .33333333333333337 yards. */
    static final double FEET_TO_YARD = .33333333333333337;

    /** 12 inches. */
    static final double FEET_TO_INCH = 12;

    /** .0003048 kilometers. */
    static final double FEET_TO_KM = .0003048;

    /** .3048 meters. */
    static final double FEET_TO_METER = .3048;

    /** .0005681818181818182 miles. */
    static final double YARD_TO_MI = .0005681818181818182;

    /** .0004934221176572162 nautical miles. */
    static final double YARD_TO_NM = .0004934221176572162;

    /** 3 feet. */
    static final double YARD_TO_FEET = 3;

    /** 36 inches. */
    static final double YARD_TO_INCH = 36;

    /** .0009144 yards. */
    static final double YARD_TO_KM = .0009144;

    /** .9144 meters. */
    static final double YARD_TO_METER = .9144;

    /**
     * @param km
     * @return the converted value
     */
    public static double convertKilometersToMiles(double km)
    {
        return km * KM_TO_MILE;
    }

    /**
     * @param km
     * @return the converted value
     */
    public static double convertKilometersToNauticalMiles(double km)
    {
        return km * KM_TO_NM;
    }

    /**
     * @param km
     * @return the converted value
     */
    public static double convertKilometersToYards(double km)
    {
        return km * KM_TO_YARD;
    }

    /**
     * @param km
     * @return the converted value
     */
    public static double convertKilometersToFeet(double km)
    {
        return km * KM_TO_FEET;
    }

    /**
     * @param km
     * @return the converted value
     */
    public static double convertKilometersToInches(double km)
    {
        return km * KM_TO_INCH;
    }

    /**
     * @param km
     * @return the converted value
     */
    public static double convertKilometersToMeters(double km)
    {
        return km * KM_TO_METER;
    }

    /**
     * @param m
     * @return the converted value
     */
    public static double convertMetersToMiles(double m)
    {
        return m * METER_TO_MI;
    }

    /**
     * @param m
     * @return the converted value
     */
    public static double convertMetersToNauticalMiles(double m)
    {
        return m * METER_TO_NM;
    }

    /**
     * @param m
     * @return the converted value
     */
    public static double convertMetersToYards(double m)
    {
        return m * METER_TO_YARD;
    }

    /**
     * @param m
     * @return the converted value
     */
    public static double convertMetersToFeet(double m)
    {
        return m * METER_TO_FEET;
    }

    /**
     * @param m
     * @return the converted value
     */
    public static double convertMetersToInches(double m)
    {
        return m * METER_TO_INCH;
    }

    /**
     * @param m
     * @return the converted value
     */
    public static double convertMetersToKilometers(double m)
    {
        return m * METER_TO_KM;
    }

    /**
     * @param nm
     * @return the converted value
     */
    public static double convertNauticalMilesToMiles(double nm)
    {
        return nm * NM_TO_MI;
    }

    /**
     * @param nm
     * @return the converted value
     */
    public static double convertNauticalMilesToYards(double nm)
    {
        return nm * NM_TO_YARD;
    }

    /**
     * @param nm
     * @return the converted value
     */
    public static double convertNauticalMilesToFeet(double nm)
    {
        return nm * NM_TO_FEET;
    }

    /**
     * @param nm
     * @return the converted value
     */
    public static double convertNauticalMilesToInches(double nm)
    {
        return nm * NM_TO_INCH;
    }

    /**
     * @param nm
     * @return the converted value
     */
    public static double convertNauticalMilesToKilometers(double nm)
    {
        return nm * NM_TO_KM;
    }

    /**
     * @param nm
     * @return the converted value
     */
    public static double convertNauticalMilesToMeters(double nm)
    {
        return nm * NM_TO_METER;
    }

    /**
     * @param mi
     * @return the converted value
     */
    public static double convertMilesToNauticalMiles(double mi)
    {
        return mi * MI_TO_NM;
    }

    /**
     * @param mi
     * @return the converted value
     */
    public static double convertMilesToYards(double mi)
    {
        return mi * MI_TO_YARD;
    }

    /**
     * @param mi
     * @return the converted value
     */
    public static double convertMilesToFeet(double mi)
    {
        return mi * MI_TO_FEET;
    }

    /**
     * @param mi
     * @return the converted value
     */
    public static double convertMilesToInches(double mi)
    {
        return mi * MI_TO_INCH;
    }

    /**
     * @param mi
     * @return the converted value
     */
    public static double convertMilesToKilometers(double mi)
    {
        return mi * MI_TO_KM;
    }

    /**
     * @param mi
     * @return the converted value
     */
    public static double convertMilesToMeters(double mi)
    {
        return mi * MI_TO_METER;
    }

    /**
     * @param yards
     * @return the converted value
     */
    public static double convertYardsToMiles(double yards)
    {
        return yards * YARD_TO_MI;
    }

    /**
     * @param yards
     * @return the converted value
     */
    public static double convertYardsToNauticalMiles(double yards)
    {
        return yards * YARD_TO_NM;
    }

    /**
     * @param yards
     * @return the converted value
     */
    public static double convertYardsToFeet(double yards)
    {
        return yards * YARD_TO_FEET;
    }

    /**
     * @param yards
     * @return the converted value
     */
    public static double convertYardsToInches(double yards)
    {
        return yards * YARD_TO_INCH;
    }

    /**
     * @param yards
     * @return the converted value
     */
    public static double convertYardsToKilometers(double yards)
    {
        return yards * YARD_TO_KM;
    }

    /**
     * @param yards
     * @return the converted value
     */
    public static double convertYardsToMeters(double yards)
    {
        return yards * YARD_TO_METER;
    }

    /**
     * @param feet
     * @return the converted value
     */
    public static double convertFeetToMiles(double feet)
    {
        return feet * FEET_TO_MILE;
    }

    /**
     * @param feet
     * @return the converted value
     */
    public static double convertFeetToNauticalMiles(double feet)
    {
        return feet * FEET_TO_NM;
    }

    /**
     * @param feet
     * @return the converted value
     */
    public static double convertFeetToYards(double feet)
    {
        return feet * FEET_TO_YARD;
    }

    /**
     * @param feet
     * @return the converted value
     */
    public static double convertFeetToInches(double feet)
    {
        return feet * FEET_TO_INCH;
    }

    /**
     * @param feet
     * @return the converted value
     */
    public static double convertFeetToKilometers(double feet)
    {
        return feet * FEET_TO_KM;
    }

    /**
     * @param feet
     * @return the converted value
     */
    public static double convertFeetToMeters(double feet)
    {
        return feet * FEET_TO_METER;
    }

    /**
     * @param in
     * @return the converted value
     */
    public static double convertInchesToMiles(double in)
    {
        return in * INCH_TO_MI;
    }

    /**
     * @param in
     * @return the converted value
     */
    public static double convertInchesToNauticalMiles(double in)
    {
        return in * INCH_TO_NM;
    }

    /**
     * @param in
     * @return the converted value
     */
    public static double convertInchesToYards(double in)
    {
        return in * INCH_TO_YARD;
    }

    /**
     * @param in
     * @return the converted value
     */
    public static double convertInchesToFeet(double in)
    {
        return in * INCH_TO_FEET;
    }

    /**
     * @param in
     * @return the converted value
     */
    public static double convertInchesToKilometers(double in)
    {
        return in * INCH_TO_KM;
    }

    /**
     * @param in
     * @return the converted value
     */
    public static double convertInchesToMeters(double in)
    {
        return in * INCH_TO_METER;
    }
}

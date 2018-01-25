package io.opensphere.core.common.util;

/**
 * Cheap class to handle Unit Conversions..
 */
public class UnitConversion
{

    /**
     * Conversion factors taken from:
     * http://www.taylormade.com.au/billspages/conversion_table.html
     */
    static final double KM_TO_MILE = .621371192237334;

    static final double KM_TO_NM = .53995680345572354211;

    static final double KM_TO_YARD = 1093.6132983377079;

    static final double KM_TO_FEET = 3280.839895013123;

    static final double KM_TO_INCH = 39370.078740157485;

    static final double KM_TO_METER = 1000;

    static final double MI_TO_NM = .8684229270767005;

    static final double MI_TO_YARD = 1760;

    static final double MI_TO_FEET = 5280;

    static final double MI_TO_INCH = 63360;

    static final double MI_TO_KM = 1.609344;

    static final double MI_TO_METER = 1609.344;

    static final double NM_TO_MI = 1.1515126660303825;

    static final double NM_TO_YARD = 2026.6622922134734;

    static final double NM_TO_FEET = 6079.98687664042;

    static final double NM_TO_INCH = 72959.84251968504;

    static final double NM_TO_KM = 1.852;

    static final double NM_TO_METER = 1852;

    static final double METER_TO_MI = .0006213711922373339;

    static final double METER_TO_NM = .00053995680345572354;

    static final double METER_TO_YARD = 1.0936132983377078;

    public static final double METER_TO_FEET = 3.2808398950131235;

    static final double METER_TO_INCH = 39.37007874015748;

    static final double METER_TO_KM = .001;

    static final double INCH_TO_MI = .000015782828282828283;

    static final double INCH_TO_NM = .000013706169934922673;

    static final double INCH_TO_YARD = .027777777777777776;

    static final double INCH_TO_FEET = .08333333333333333;

    static final double INCH_TO_KM = .000025399999999999997;

    static final double INCH_TO_METER = .0254;

    static final double FEET_TO_MILE = .0001893939393939394;

    static final double FEET_TO_NM = .0001644740392190721;

    static final double FEET_TO_YARD = .33333333333333337;

    static final double FEET_TO_INCH = 12;

    static final double FEET_TO_KM = .0003048;

    static final double FEET_TO_METER = .3048;

    static final double YARD_TO_MI = .0005681818181818182;

    static final double YARD_TO_NM = .0004934221176572162;

    static final double YARD_TO_FEET = 3;

    static final double YARD_TO_INCH = 36;

    static final double YARD_TO_KM = .0009144;

    static final double YARD_TO_METER = .9144;

    /**
     * @param km
     * @return
     */
    public static double convertKilometersToMiles(double km)
    {
        return km * KM_TO_MILE;
    }

    /**
     * @param km
     * @return
     */
    public static double convertKilometersToNauticalMiles(double km)
    {
        return km * KM_TO_NM;
    }

    /**
     * @param km
     * @return
     */
    public static double convertKilometersToYards(double km)
    {
        return km * KM_TO_YARD;
    }

    /**
     * @param km
     * @return
     */
    public static double convertKilometersToFeet(double km)
    {
        return km * KM_TO_FEET;
    }

    /**
     * @param km
     * @return
     */
    public static double convertKilometersToInches(double km)
    {
        return km * KM_TO_INCH;
    }

    /**
     * @param km
     * @return
     */
    public static double convertKilometersToMeters(double km)
    {
        return km * KM_TO_METER;
    }

    /**
     * @param m
     * @return
     */
    public static double convertMetersToMiles(double m)
    {
        return m * METER_TO_MI;
    }

    /**
     * @param m
     * @return
     */
    public static double convertMetersToNauticalMiles(double m)
    {
        return m * METER_TO_NM;
    }

    /**
     * @param m
     * @return
     */
    public static double convertMetersToYards(double m)
    {
        return m * METER_TO_YARD;
    }

    /**
     * @param m
     * @return
     */
    public static double convertMetersToFeet(double m)
    {
        return m * METER_TO_FEET;
    }

    /**
     * @param m
     * @return
     */
    public static double convertMetersToInches(double m)
    {
        return m * METER_TO_INCH;
    }

    /**
     * @param m
     * @return
     */
    public static double convertMetersToKilometers(double m)
    {
        return m * METER_TO_KM;
    }

    /**
     * @param nm
     * @return
     */
    public static double convertNauticalMilesToMiles(double nm)
    {
        return nm * NM_TO_MI;
    }

    /**
     * @param nm
     * @return
     */
    public static double convertNauticalMilesToYards(double nm)
    {
        return nm * NM_TO_YARD;
    }

    /**
     * @param nm
     * @return
     */
    public static double convertNauticalMilesToFeet(double nm)
    {
        return nm * NM_TO_FEET;
    }

    /**
     * @param nm
     * @return
     */
    public static double convertNauticalMilesToInches(double nm)
    {
        return nm * NM_TO_INCH;
    }

    /**
     * @param nm
     * @return
     */
    public static double convertNauticalMilesToKilometers(double nm)
    {
        return nm * NM_TO_KM;
    }

    /**
     * @param nm
     * @return
     */
    public static double convertNauticalMilesToMeters(double nm)
    {
        return nm * NM_TO_METER;
    }

    /**
     * @param mi
     * @return
     */
    public static double convertMilesToNauticalMiles(double mi)
    {
        return mi * MI_TO_NM;
    }

    /**
     * @param mi
     * @return
     */
    public static double convertMilesToYards(double mi)
    {
        return mi * MI_TO_YARD;
    }

    /**
     * @param mi
     * @return
     */
    public static double convertMilesToFeet(double mi)
    {
        return mi * MI_TO_FEET;
    }

    /**
     * @param mi
     * @return
     */
    public static double convertMilesToInches(double mi)
    {
        return mi * MI_TO_INCH;
    }

    /**
     * @param mi
     * @return
     */
    public static double convertMilesToKilometers(double mi)
    {
        return mi * MI_TO_KM;
    }

    /**
     * @param mi
     * @return
     */
    public static double convertMilesToMeters(double mi)
    {
        return mi * MI_TO_METER;
    }

    /**
     * @param yards
     * @return
     */
    public static double convertYardsToMiles(double yards)
    {
        return yards * YARD_TO_MI;
    }

    /**
     * @param yards
     * @return
     */
    public static double convertYardsToNauticalMiles(double yards)
    {
        return yards * YARD_TO_NM;
    }

    /**
     * @param yards
     * @return
     */
    public static double convertYardsToFeet(double yards)
    {
        return yards * YARD_TO_FEET;
    }

    /**
     * @param yards
     * @return
     */
    public static double convertYardsToInches(double yards)
    {
        return yards * YARD_TO_INCH;
    }

    /**
     * @param yards
     * @return
     */
    public static double convertYardsToKilometers(double yards)
    {
        return yards * YARD_TO_KM;
    }

    /**
     * @param yards
     * @return
     */
    public static double convertYardsToMeters(double yards)
    {
        return yards * YARD_TO_METER;
    }

    /**
     * @param feet
     * @return
     */
    public static double convertFeetToMiles(double feet)
    {
        return feet * FEET_TO_MILE;
    }

    /**
     * @param feet
     * @return
     */
    public static double convertFeetToNauticalMiles(double feet)
    {
        return feet * FEET_TO_NM;
    }

    /**
     * @param feet
     * @return
     */
    public static double convertFeetToYards(double feet)
    {
        return feet * FEET_TO_YARD;
    }

    /**
     * @param feet
     * @return
     */
    public static double convertFeetToInches(double feet)
    {
        return feet * FEET_TO_INCH;
    }

    /**
     * @param feet
     * @return
     */
    public static double convertFeetToKilometers(double feet)
    {
        return feet * FEET_TO_KM;
    }

    /**
     * @param feet
     * @return
     */
    public static double convertFeetToMeters(double feet)
    {
        return feet * FEET_TO_METER;
    }

    /**
     * @param inches
     * @return
     */
    public static double convertInchesToMiles(double in)
    {
        return in * INCH_TO_MI;
    }

    /**
     * @param inches
     * @return
     */
    public static double convertInchesToNauticalMiles(double in)
    {
        return in * INCH_TO_NM;
    }

    /**
     * @param inches
     * @return
     */
    public static double convertInchesToYards(double in)
    {
        return in * INCH_TO_YARD;
    }

    /**
     * @param inches
     * @return
     */
    public static double convertInchesToFeet(double in)
    {
        return in * INCH_TO_FEET;
    }

    /**
     * @param inches
     * @return
     */
    public static double convertInchesToKilometers(double in)
    {
        return in * INCH_TO_KM;
    }

    /**
     * @param inches
     * @return
     */
    public static double convertInchesToMeters(double in)
    {
        return in * INCH_TO_METER;
    }

    public static void main(String[] args)
    {
        System.out.println(UnitConversion.convertMilesToNauticalMiles(3));
    }
}

package io.opensphere.csvcommon.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import io.opensphere.core.mgrs.MGRSConverter;
import io.opensphere.core.mgrs.UTM;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LatLonAlt.CoordFormat;
import io.opensphere.core.model.LatLonAltParser;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Test utilities for CSV plugin.
 */
@SuppressWarnings("PMD.GodClass")
public final class CsvTestUtils
{
    /** The Constant NAME. */
    public static final String NAME = "Name";

    /**
     * Creates basic CSV data.
     *
     * @return the list of rows
     */
    public static List<List<String>> createBasicData()
    {
        List<List<String>> rows = New.list();

        rows.add(Arrays.asList(ColumnHeaders.TIME.toString(), ColumnHeaders.LAT.toString(), ColumnHeaders.LON.toString(),
                ColumnHeaders.NAME.toString(), ColumnHeaders.NAME2.toString()));

        int rowCount = 100;
        List<String> timeValues = createTimeValues(rowCount);
        List<String> latValues = createLatValues(rowCount);
        List<String> lonValues = createLonValues(rowCount);
        List<String> nameValues = createTextValues(rowCount, ColumnHeaders.NAME.toString());
//        List<String> nameValues = createTextValues(rowCount, new Function<Integer, String>()
//        {
//            @Override
//            public String apply(Integer rowIndex)
//            {
//                return rowIndex.intValue() % 3 == 0 ? "Name" + rowIndex.toString() : "";
//            }
//        });
        List<String> name2Values = createTextValues(rowCount, ColumnHeaders.NAME.toString());
        for (int i = 0; i < rowCount; i++)
        {
            rows.add(Arrays.asList(timeValues.get(i), latValues.get(i), lonValues.get(i), nameValues.get(i), name2Values.get(i)));
        }

        return rows;
    }

    /**
     * Creates a single column of data with or without a column name.
     *
     * @param columnName the column name
     * @param values the values
     * @return the list
     */
    public static List<List<String>> createSingleColumnData(String columnName, List<String> values)
    {
        List<List<String>> rows = New.list();
        if (columnName != null)
        {
            rows.add(Arrays.asList(columnName));
        }
        for (int i = 0; i < values.size(); i++)
        {
            rows.add(Arrays.asList(values.get(i)));
        }
        return rows;
    }

    /**
     * Creates the basic delimited cep data.
     *
     * @param delimiter the delimiter
     * @param quoteChar the quote char
     * @param sparseQuotes the sparse quotes
     * @return the list
     */
    public static List<String> createBasicDelimitedCEPData(String delimiter, Character quoteChar, boolean sparseQuotes)
    {
        return formatDelimited(createBasicCEPData(), delimiter, quoteChar, sparseQuotes);
    }

    /**
     * Creates basic delimited data.
     *
     * @param delimiter the delimiter
     * @return the list of rows
     */
    public static List<String> createBasicDelimitedData(String delimiter)
    {
        return formatDelimited(createBasicData(), delimiter, null, false);
    }

    /**
     * Creates the basic data with a single column.
     *
     * @param colName the column name, if null, just add the data
     * @param values the values
     * @param delimiter the delimiter
     * @param quoteChar the quote char
     * @return the list
     */
    public static List<String> createBasicSingleColumnData(String colName, List<String> values, String delimiter,
            Character quoteChar)
    {
        return formatDelimited(createSingleColumnData(colName, values), delimiter, quoteChar, false);
    }

    /**
     * Creates basic delimited data.
     *
     * @param delimiter the delimiter
     * @param quoteChar the quoteChar, or null
     * @param sparseQuotes When true, only quote the values sparsely.
     * @return the list of rows
     */
    public static List<String> createBasicDelimitedData(String delimiter, Character quoteChar, boolean sparseQuotes)
    {
        return formatDelimited(createBasicData(), delimiter, quoteChar, sparseQuotes);
    }

    /**
     * Creates the basic delimited data with ellipse columns.
     *
     * @param delimiter the delimiter
     * @param ellipseHeader the ellipse header
     * @return the list
     */
    public static List<String> createBasicDelimitedEllipseData(String delimiter, List<String> ellipseHeader)
    {
        return formatDelimited(createBasicEllipseData(ellipseHeader), delimiter, null, false);
    }

    /**
     * Creates the basic delimited line of bearing data.
     *
     * @param delimiter the delimiter
     * @param ellipseHeader the ellipse header
     * @return the list
     */
    public static List<String> createBasicDelimitedLobData(String delimiter, List<String> ellipseHeader)
    {
        return formatDelimited(createBasicLobData(ellipseHeader), delimiter, null, false);
    }

    /**
     * Creates basic fixed width data.
     *
     * @return the list of rows
     */
    public static List<String> createBasicFixedWidthData()
    {
        return formatFixed(createBasicData(), new int[] { 21, 20, 20, 7, 7 });
    }

    /**
     * Creates crap.
     *
     * @return the list of rows
     */
    public static List<String> createCrap()
    {
        List<String> rows = New.list();
        rows.add("=====================================");
        rows.add("= I like crap at the top of my file =");
        rows.add("=====================================");
        return rows;
    }

    /**
     * Creates hard data.
     *
     * @return the list of rows
     */
    public static List<String> createHardData()
    {
        List<String> rows = New.list();
        rows.addAll(createCrap());
        rows.add("1.1,2.2,3.3");
        rows.add("4.4,5.5,6.6");
        rows.add("7.7,8.8,9.9");
        rows.add("10.10,11.11,12.12");
        return rows;
    }

    /**
     * Creates the mgrs delimited data.
     *
     * @param delimiter the delimiter
     * @return the list
     */
    public static List<String> createMGRSDelimitedData(String delimiter)
    {
        return formatDelimited(createMGRSHeader(), delimiter, null, false);
    }

    /**
     * Creates the mgrs location data.
     *
     * @return the list
     */
    public static List<List<String>> createMGRSLocationData()
    {
        List<List<String>> rows = New.list();

        int rowCount = 100;
        List<String> timeValues = createTimeValues(rowCount);
        List<String> latValues = createLatValues(rowCount);
        List<String> lonValues = createLonValues(rowCount);
        List<String> mgrsValues = New.list();

        MGRSConverter converter = new MGRSConverter();
        for (int i = 0; i < latValues.size(); i++)
        {
            LatLonAlt lla = LatLonAlt.parse(latValues.get(i) + " " + lonValues.get(i));
            if (lla != null)
            {
                String mgrsStr = converter
                        .createString(new UTM(Double.parseDouble(latValues.get(i)), Double.parseDouble(lonValues.get(i))));
                mgrsValues.add(i, mgrsStr);
            }
        }
        List<String> nameValues = createTextValues(rowCount, NAME);
        List<String> name2Values = createTextValues(rowCount, NAME);
        for (int i = 0; i < rowCount; i++)
        {
            rows.add(Arrays.asList(timeValues.get(i), latValues.get(i), lonValues.get(i), mgrsValues.get(i), nameValues.get(i),
                    name2Values.get(i)));
        }

        return rows;
    }

    /**
     * Creates the multiple delimited formatted lat lon data.
     *
     * @param delimiter the delimiter
     * @return the list
     */
    public static List<String> createMultipleDelimitedFormattedLatLonData(String delimiter)
    {
        return formatDelimited(createMultipleLatLonHeaderWithFormats(), delimiter, null, false);
    }

    /**
     * Creates a list of DMS lat/lon rows based on the custom lists.
     *
     * @param delimiter the delimiter
     * @param quote character
     * @param lats the set of latitude values
     * @param lons the set of longitude values
     * @return the list
     */
    public static List<String> createMultipleDelimitedFormatDMSData(String delimiter, Character quote, List<String> lats,
            List<String> lons)
    {
        return formatDelimited(createMultipleFormatDMSValues(lats, lons), delimiter, quote, false);
    }

    /**
     * Creates multiple latitude/longitude data.
     *
     * @param delimiter the delimiter
     * @return the list
     */
    public static List<String> createMultipleDelimitedLatLonData(String delimiter)
    {
        return formatDelimited(createMultipleLatLonHeader(), delimiter, null, false);
    }

    /**
     * Creates the multiple delimited location data.
     *
     * @param delimiter the delimiter
     * @param quote the quote
     * @return the list
     */
    public static List<String> createMultipleDelimitedLocationData(String delimiter, Character quote)
    {
        return formatDelimited(createMultipleLocationHeader(), delimiter, quote, false);
    }

    /**
     * Creates the multiple location data.
     *
     * @return the list
     */
    public static List<List<String>> createMultipleLatLonData()
    {
        List<List<String>> rows = New.list();

        int rowCount = 100;
        List<String> timeValues = createTimeValues(rowCount);
        List<String> latValues = createLatValues(rowCount);
        List<String> lonValues = createLonValues(rowCount);
        List<String> bogusValues1 = createTextValues(rowCount, "Flatten");
        List<String> bogusValues2 = createTextValues(rowCount, "Belong");
        List<String> nameValues = createTextValues(rowCount, NAME);
        List<String> name2Values = createTextValues(rowCount, NAME);
        for (int i = 0; i < rowCount; i++)
        {
            rows.add(Arrays.asList(timeValues.get(i), latValues.get(i), lonValues.get(i), latValues.get(i), lonValues.get(i),
                    latValues.get(i), lonValues.get(i), latValues.get(i), lonValues.get(i), bogusValues1.get(i),
                    bogusValues2.get(i), latValues.get(i), lonValues.get(i), nameValues.get(i), name2Values.get(i)));
        }

        return rows;
    }

    /**
     * Creates the multiple lat lon data with multiple formats.
     *
     * @return the list
     */
    public static List<List<String>> createMultipleLatLonDataWithFormats()
    {
        List<List<String>> rows = New.list();

        int rowCount = 100;
        List<String> timeValues = createTimeValues(rowCount);
        List<String> latValues = createLatValues(rowCount);
        List<String> lonValues = createLonValues(rowCount);

        List<String> latDMSValues = New.list();
        List<String> lonDMSValues = New.list();
        for (int i = 0; i < latValues.size(); i++)
        {
            latDMSValues.add(i, LatLonAlt.latToDMSString(LatLonAltParser.parseLat(latValues.get(i), CoordFormat.DECIMAL), 3));
            lonDMSValues.add(i, LatLonAlt.lonToDMSString(LatLonAltParser.parseLon(lonValues.get(i), CoordFormat.DECIMAL), 3));
        }

        List<String> bogusValues1 = createTextValues(rowCount, "Flatten");
        List<String> bogusValues2 = createTextValues(rowCount, "Belong");
        List<String> nameValues = createTextValues(rowCount, NAME);
        List<String> name2Values = createTextValues(rowCount, NAME);
        for (int i = 0; i < rowCount; i++)
        {
            rows.add(
                    Arrays.asList(timeValues.get(i), latValues.get(i), lonValues.get(i), latDMSValues.get(i), lonDMSValues.get(i),
                            latValues.get(i), lonValues.get(i), latValues.get(i), lonValues.get(i), bogusValues1.get(i),
                            bogusValues2.get(i), latValues.get(i), lonValues.get(i), nameValues.get(i), name2Values.get(i)));
        }

        return rows;
    }

    /**
     * Creates the multiple location data.
     *
     * @return the list
     */
    public static List<List<String>> createMultipleLocationData()
    {
        List<List<String>> rows = New.list();

        int rowCount = 100;
        List<String> timeValues = createTimeValues(rowCount);
        List<String> latValues = createLatValues(rowCount);
        List<String> lonValues = createLonValues(rowCount);
        List<String> positionValues = New.list(rowCount);
        for (int i = 0; i < latValues.size(); i++)
        {
            String latDMS = LatLonAlt.latToDMSString(LatLonAltParser.parseLat(latValues.get(i), CoordFormat.DECIMAL), 3);
            String lonDMS = LatLonAlt.lonToDMSString(LatLonAltParser.parseLon(lonValues.get(i), CoordFormat.DECIMAL), 3);
            positionValues.add("(" + lonDMS + " " + latDMS + ")");
        }
        List<String> posValues = createTextValues(rowCount, "SPos");
        List<String> blockValues = createTextValues(rowCount, "Block");
        List<String> nameValues = createTextValues(rowCount, NAME);
        List<String> name2Values = createTextValues(rowCount, NAME);
        for (int i = 0; i < rowCount; i++)
        {
            rows.add(Arrays.asList(timeValues.get(i), latValues.get(i), lonValues.get(i), positionValues.get(i), posValues.get(i),
                    blockValues.get(i), nameValues.get(i), name2Values.get(i)));
        }

        return rows;
    }

    /**
     * Creates basic CSV data.
     *
     * @return the list of rows
     */
    public static List<List<String>> createMultipleTimesData()
    {
        List<List<String>> rows = New.list();

        int rowCount = 100;
        List<String> timeValues = createTimeValues(rowCount);
        List<String> latValues = createLatValues(rowCount);
        List<String> lonValues = createLonValues(rowCount);
        List<String> justTimeValues = createJustTimeValues(rowCount);
        List<String> nameValues = createTextValues(rowCount, NAME);
        List<String> name2Values = createTextValues(rowCount, NAME);
        List<String> dateValues = createDateValues(rowCount);
        List<String> textValues = createTextValues(rowCount, "Text");
        List<String> compositeDate = createDateValues(rowCount);
        List<String> compositeTime = createJustTimeValues(rowCount);

        for (int i = 0; i < rowCount; i++)
        {
            rows.add(Arrays.asList(timeValues.get(i), latValues.get(i), lonValues.get(i), justTimeValues.get(i),
                    nameValues.get(i), name2Values.get(i), dateValues.get(i), textValues.get(i), compositeDate.get(i),
                    compositeTime.get(i)));
        }

        return rows;
    }

    /**
     * Creates data that contains no locations.
     *
     * @param delimiter the delimiter
     * @return the list
     */
    public static List<String> createNoLocationData(String delimiter)
    {
        return formatDelimited(createNoLocationData(), delimiter, null, false);
    }

    /**
     * Creates data with missing rows and stuff.
     *
     * @return the list of rows
     */
    public static List<String> createSparseRowData()
    {
        List<String> rows = New.list();
        rows.add("ABC,DEF,GHI");
        rows.add("some crap");
        rows.add(",,");
        rows.add("1,2,3");
        rows.add("#,,");
        rows.add("4,5,6");
        rows.add(",,");
        rows.add("");
        return rows;
    }

    /**
     * Creates text values of the specified count.
     *
     * @param count the number of values to create
     * @param text the base text of the value
     * @return the list of values
     */
    public static List<String> createTextValues(int count, final String text)
    {
        return createTextValues(count, new Function<Integer, String>()
        {
            @Override
            public String apply(Integer rowIndex)
            {
                return text + rowIndex.toString();
            }
        });
    }

    /**
     * Creates the wkt delimited data.
     *
     * @param delimiter the delimiter
     * @return the list
     */
    public static List<String> createWktDelimitedData(String delimiter)
    {
        return formatDelimited(createWktHeader(), delimiter, null, false);
    }

    /**
     * Creates wkt location data as the point wkt type.
     *
     * @return the list
     */
    public static List<List<String>> createWktLocationData()
    {
        List<List<String>> rows = New.list();
        int rowCount = 100;
        List<String> timeValues = createTimeValues(rowCount);
        List<String> latValues = createLatValues(rowCount);
        List<String> lonValues = createLonValues(rowCount);
        List<String> wktPoints = New.list();
        for (int i = 0; i < latValues.size(); i++)
        {
            String wktPoint = "POINT (" + latValues.get(i) + " " + lonValues.get(i) + ")";
            wktPoints.add(wktPoint);
        }

        wktPoints.set(0, "POINT (60.205078125 30.9375)");
        wktPoints.set(1, "\"POLYGON ((-124 26, -65 26, -65 47, -124 47, -124 26))\"");
        wktPoints.set(2, "\"LINESTRING (36.6638888888889 34.6169444444444, 45.6638888888889 43.6169444444444, "
                + "44.6638888888889 32.6169444444444, 26.6638888888889 24.6169444444444)\"");
        wktPoints.set(3, "\"MULTIPOINT ((10 10), (20 20))\"");
        wktPoints.set(4, "\"MULTILINESTRING ((22 26, 25 27, 24 28),(12 10, 13 14, 15 16),(50 51, 52 53, 54 55))\"");
        wktPoints.set(5, "\"MULTIPOLYGON (((10 10, 10 20, 20 20, 20 15, 10 10)), ((60 60, 70 70, 80 60, 60 60 )))\"");
        wktPoints.set(6, "\"GEOMETRYCOLLECTION ( POINT (10 10), POINT (30 30), LINESTRING (15 15, 20 20) )\"");

        List<String> nameValues = createTextValues(rowCount, NAME);
        List<String> name2Values = createTextValues(rowCount, NAME);
        for (int i = 0; i < rowCount; i++)
        {
            rows.add(Arrays.asList(timeValues.get(i), latValues.get(i), lonValues.get(i), wktPoints.get(i), nameValues.get(i),
                    name2Values.get(i)));
        }

        return rows;
    }

    /**
     * Creates the basic cep data.
     *
     * @return the list
     */
    public static List<List<String>> createBasicCEPData()
    {
        List<List<String>> rows = New.list();

        rows.add(Arrays.asList(ColumnHeaders.TIME.toString(), ColumnHeaders.LAT.toString(), ColumnHeaders.LON.toString(),
                ColumnHeaders.CEP.toString(), ColumnHeaders.NAME.toString(), ColumnHeaders.NAME2.toString()));

        int rowCount = 100;
        List<String> timeValues = createTimeValues(rowCount);
        List<String> latValues = createLatValues(rowCount);
        List<String> lonValues = createLonValues(rowCount);
        List<String> cepValues = createCEPValues(rowCount);
        List<String> nameValues = createTextValues(rowCount, ColumnHeaders.NAME.toString());
//        List<String> nameValues = createTextValues(rowCount, new Function<Integer, String>()
//        {
//            @Override
//            public String apply(Integer rowIndex)
//            {
//                return rowIndex.intValue() % 3 == 0 ? "Name" + rowIndex.toString() : "";
//            }
//        });
        List<String> name2Values = createTextValues(rowCount, ColumnHeaders.NAME.toString());
        for (int i = 0; i < rowCount; i++)
        {
            rows.add(Arrays.asList(timeValues.get(i), latValues.get(i), lonValues.get(i), cepValues.get(i), nameValues.get(i),
                    name2Values.get(i)));
        }

        return rows;
    }

    /**
     * Creates the basic ellipse data.
     *
     * @param ellipseHeader the ellipse header
     * @return the list
     */
    public static List<List<String>> createBasicEllipseData(List<String> ellipseHeader)
    {
        List<List<String>> rows = New.list();

        List<String> header = Arrays.asList(ColumnHeaders.TIME.toString(), ColumnHeaders.LAT.toString(),
                ColumnHeaders.LON.toString(), ColumnHeaders.NAME.toString(), ColumnHeaders.NAME2.toString(), ellipseHeader.get(0),
                ellipseHeader.get(1), ellipseHeader.get(2));

        rows.add(header);

        int rowCount = 100;
        List<String> timeValues = createTimeValues(rowCount);
        List<String> latValues = createLatValues(rowCount);
        List<String> lonValues = createLonValues(rowCount);
        List<String> nameValues = createTextValues(rowCount, NAME);
        List<String> name2Values = createTextValues(rowCount, NAME);
        List<String> ellipseAxisValues = createEllipseAxisValues(rowCount);
        List<String> orientValues = createOrientationValues(rowCount);

        for (int i = 0; i < rowCount; i++)
        {
            rows.add(Arrays.asList(timeValues.get(i), latValues.get(i), lonValues.get(i), nameValues.get(i), name2Values.get(i),
                    ellipseAxisValues.get(i * 2), ellipseAxisValues.get(i * 2 + 1), orientValues.get(i)));
        }

        return rows;
    }

    /**
     * Creates the basic line of bearing data.
     *
     * @param lobHeader the lob header
     * @return the list
     */
    public static List<List<String>> createBasicLobData(List<String> lobHeader)
    {
        List<List<String>> rows = New.list();

        List<String> header = Arrays.asList(ColumnHeaders.TIME.toString(), ColumnHeaders.LAT.toString(),
                ColumnHeaders.LON.toString(), ColumnHeaders.NAME.toString(), ColumnHeaders.NAME2.toString(), lobHeader.get(0));

        rows.add(header);

        int rowCount = 100;
        List<String> timeValues = createTimeValues(rowCount);
        List<String> latValues = createLatValues(rowCount);
        List<String> lonValues = createLonValues(rowCount);
        List<String> nameValues = createTextValues(rowCount, NAME);
        List<String> name2Values = createTextValues(rowCount, NAME);
        List<String> lobValues = createOrientationValues(rowCount);

        for (int i = 0; i < rowCount; i++)
        {
            rows.add(Arrays.asList(timeValues.get(i), latValues.get(i), lonValues.get(i), nameValues.get(i), name2Values.get(i),
                    lobValues.get(i)));
        }

        return rows;
    }

    /**
     * Creates the cep values.
     *
     * @param count the count
     * @return the list
     */
    public static List<String> createCEPValues(int count)
    {
        List<String> values = New.list(count);
        double max = 100;
        double min = 5;
        Random r = new Random();
        double randomDouble = -1;
        for (int i = 0; i < count; i++)
        {
            randomDouble = r.nextDouble() * (max - min) + min;
            values.add(Double.toString(randomDouble));
        }
        return values;
    }

    /**
     * Creates time values of the specified count.
     *
     * @param count the number of values to create
     * @return the list of values
     */
    public static List<String> createDateValues(int count)
    {
        List<String> values = New.list(count);
        SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
        long currentValue;
        try
        {
            currentValue = format.parse("04-14-2014").getTime();
        }
        catch (ParseException e)
        {
            currentValue = 0;
        }
        double stepSize = 86400000;
        for (int i = 0; i < count; i++)
        {
            if (i % 10 == 0)
            {
                // Add some bad data.
                values.add("04142014");
            }
            else
            {
                values.add(format.format(new Date(currentValue)));
            }

            currentValue += stepSize;
        }
        return values;
    }

    /**
     * Creates the ellipse semi major and semi minor axis values.
     *
     * @param count the count
     * @return the list
     */
    public static List<String> createEllipseAxisValues(int count)
    {
        List<String> values = New.list(count);
        double currentValue = 1.;
        double stepSize = 1000. / (count - 1);
        for (int i = 0; i < count; i++)
        {
            values.add(String.valueOf(currentValue));
            values.add(String.valueOf(currentValue * .75));
            currentValue += stepSize;
        }
        return values;
    }

    /**
     * Creates time values of the specified count.
     *
     * @param count the number of values to create
     * @return the list of values
     */
    public static List<String> createJustTimeValues(int count)
    {
        List<String> values = New.list(count);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        long currentValue;
        try
        {
            currentValue = format.parse("22:00:00").getTime();
        }
        catch (ParseException e)
        {
            currentValue = 0;
        }
        double stepSize = 1000;
        for (int i = 0; i < count; i++)
        {
            if (i % 5 == 0)
            {
                // Add in some bad data.
                values.add("21:30");
            }
            else
            {
                values.add(format.format(new Date(currentValue)));
            }
            currentValue += stepSize;
        }
        return values;
    }

    /**
     * Creates latitude values of the specified count.
     *
     * @param count the number of values to create
     * @return the list of values
     */
    public static List<String> createLatValues(int count)
    {
        List<String> values = New.list(count);
        double currentValue = -90.;
        double stepSize = 180. / (count - 1);
        for (int i = 0; i < count; i++)
        {
            values.add(String.valueOf(currentValue));
            currentValue += stepSize;
        }
        return values;
    }

    /**
     * Creates longitude values of the specified count.
     *
     * @param count the number of values to create
     * @return the list of values
     */
    public static List<String> createLonValues(int count)
    {
        List<String> values = New.list(count);
        double currentValue = -180.;
        double stepSize = 360. / (count - 1);
        for (int i = 0; i < count; i++)
        {
            values.add(String.valueOf(currentValue));
            currentValue += stepSize;
        }
        return values;
    }

    /**
     * Creates a header with a MGRS column.
     *
     * @return the list
     */
    public static List<List<String>> createMGRSHeader()
    {
        List<List<String>> rows = New.list();
        rows.add(Arrays.asList(ColumnHeaders.TIME.toString(), ColumnHeaders.LAT.toString(), ColumnHeaders.LON.toString(),
                ColumnHeaders.MGRS.toString(), ColumnHeaders.NAME.toString(), ColumnHeaders.NAME2.toString()));
        rows.addAll(createMGRSLocationData());
        return rows;
    }

    /**
     * Creates a set of headers with multiple individual latitude and longitude
     * columns of different formats.
     *
     * @return the list
     */
    public static List<List<String>> createMultipleLatLonHeader()
    {
        List<List<String>> rows = New.list();
        rows.add(getMultipleLatLonHeader());
        rows.addAll(createMultipleLatLonData());
        return rows;
    }

    /**
     * Creates the multiple lat lon header with formats.
     *
     * @return the list
     */
    public static List<List<String>> createMultipleLatLonHeaderWithFormats()
    {
        List<List<String>> rows = New.list();
        rows.add(getMultipleLatLonHeader());
        rows.addAll(createMultipleLatLonDataWithFormats());
        return rows;
    }

    /**
     * Creates the multiple format dms values with a custom set of values.
     *
     * @param lonValues the lon values
     * @param latValues the lat values
     * @return the list
     */
    public static List<List<String>> createMultipleFormatDMSValues(List<String> latValues, List<String> lonValues)
    {
        List<List<String>> rows = New.list();
        rows.add(Arrays.asList(ColumnHeaders.LAT.toString(), ColumnHeaders.LON.toString()));

        for (int i = 0; i < lonValues.size(); i++)
        {
            rows.add(Arrays.asList(latValues.get(i), lonValues.get(i)));
        }
        return rows;
    }

    /**
     * Creates the multiple location header.
     *
     * @return the list
     */
    public static List<List<String>> createMultipleLocationHeader()
    {
        List<List<String>> rows = New.list();
        rows.add(Arrays.asList(ColumnHeaders.TIME.toString(), ColumnHeaders.LAT.toString(), ColumnHeaders.LON.toString(),
                ColumnHeaders.POSITION.toString(), ColumnHeaders.SPOS.toString(), ColumnHeaders.BLOCK.toString(),
                ColumnHeaders.NAME.toString(), ColumnHeaders.NAME2.toString()));
        rows.addAll(createMultipleLocationData());
        return rows;
    }

    /**
     * Creates data without a location.
     *
     * @return the list
     */
    public static List<List<String>> createNoLocationData()
    {
        List<List<String>> rows = New.list();

        rows.add(Arrays.asList(ColumnHeaders.TIME.toString(), ColumnHeaders.NAME.toString(), ColumnHeaders.NAME2.toString()));

        int rowCount = 100;
        List<String> timeValues = createTimeValues(rowCount);
        List<String> nameValues = createTextValues(rowCount, ColumnHeaders.NAME.toString());
        List<String> name2Values = createTextValues(rowCount, ColumnHeaders.NAME.toString());
        for (int i = 0; i < rowCount; i++)
        {
            rows.add(Arrays.asList(timeValues.get(i), nameValues.get(i), name2Values.get(i)));
        }

        return rows;
    }

    /**
     * Creates the ellipse orientation values.
     *
     * @param count the count
     * @return the list
     */
    public static List<String> createOrientationValues(int count)
    {
        List<String> values = New.list(count);
        double currentValue = 0.;
        double stepSize = 360. / (count - 1);
        for (int i = 0; i < count; i++)
        {
            values.add(String.valueOf(currentValue));
            currentValue += stepSize;
        }
        return values;
    }

    /**
     * Creates text values of the specified count.
     *
     * @param count the number of values to create
     * @param creator the function that creates the value from the row index
     * @return the list of values
     */
    private static List<String> createTextValues(int count, Function<Integer, String> creator)
    {
        List<String> values = New.list(count);
        for (int i = 0; i < count; i++)
        {
            values.add(creator.apply(Integer.valueOf(i)));
        }
        return values;
    }

    /**
     * Creates time values of the specified count.
     *
     * @param count the number of values to create
     * @return the list of values
     */
    public static List<String> createTimeValues(int count)
    {
        List<String> values = New.list(count);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        long currentValue;
        try
        {
            currentValue = format.parse("2014-04-14T22:00:00Z").getTime();
        }
        catch (ParseException e)
        {
            currentValue = 0;
        }
        double stepSize = 1000;
        for (int i = 0; i < count; i++)
        {
            values.add(format.format(new Date(currentValue)));
            currentValue += stepSize;
        }
        return values;
    }

    /**
     * Creates a header with a wkt column.
     *
     * @return the list
     */
    public static List<List<String>> createWktHeader()
    {
        List<List<String>> rows = New.list();
        rows.add(Arrays.asList(ColumnHeaders.TIME.toString(), ColumnHeaders.LAT.toString(), ColumnHeaders.LON.toString(),
                ColumnHeaders.GEOM.toString(), ColumnHeaders.NAME.toString(), ColumnHeaders.NAME2.toString()));
        rows.addAll(createWktLocationData());
        return rows;
    }

    /**
     * Formats CSV data as delimited.
     *
     * @param data the data
     * @param delimiter the delimiter
     * @param quoteChar the quoteChar, or null
     * @param sparseQuotes When true, only quote the values sparsely.
     * @return the list of rows as Strings
     */
    public static List<String> formatDelimited(List<List<String>> data, String delimiter, Character quoteChar,
            boolean sparseQuotes)
    {
        List<String> rows = New.list(data.size());
        for (List<String> row : data)
        {
            rows.add(StringUtilities.join(delimiter, quote(quoteChar, sparseQuotes, row)));
        }
        return rows;
    }

    /**
     * Formats CSV data as fixed width.
     *
     * @param data the data
     * @param columnWidths the column widths
     * @return the list of rows as Strings
     */
    public static List<String> formatFixed(List<List<String>> data, int[] columnWidths)
    {
        List<String> rows = New.list(data.size());
        for (List<String> row : data)
        {
            StringBuilder builder = new StringBuilder();
            int colCount = Math.min(columnWidths.length, row.size());
            for (int c = 0; c < colCount; c++)
            {
                builder.append(String.format("%-" + columnWidths[c] + "s", row.get(c)));
            }
            rows.add(builder.toString());
        }
        return rows;
    }

    /**
     * Gets the multiple lat lon header.
     *
     * @return the multiple lat lon header
     */
    public static List<String> getMultipleLatLonHeader()
    {
        return Arrays.asList(ColumnHeaders.TIME.toString(), ColumnHeaders.LAT.toString(), ColumnHeaders.LON.toString(),
                ColumnHeaders.LATITUDE.toString(), ColumnHeaders.LONGITUDE.toString(), ColumnHeaders.STATIONLAT.toString(),
                ColumnHeaders.STATIONLON.toString(), ColumnHeaders.STATIONLAT1.toString(), ColumnHeaders.STATIONLON1.toString(),
                ColumnHeaders.FLATTEN.toString(), ColumnHeaders.BELONG.toString(), ColumnHeaders.LAT_1.toString(),
                ColumnHeaders.LON_1.toString(), ColumnHeaders.NAME.toString(), ColumnHeaders.NAME2.toString());
    }

    /**
     * Quotes a collection of objects.
     *
     * @param quoteChar the quoteChar, or null
     * @param sparseQuotes When true, only quote the values sparsely.
     * @param objects the objects in the row
     * @return the quoted objects as Strings
     */
    private static Collection<String> quote(final Character quoteChar, final boolean sparseQuotes,
            Collection<? extends Object> objects)
    {
        final Random rand = new Random();
        Collection<String> quotedObjects = StreamUtilities.map(objects, new Function<Object, String>()
        {
            @SuppressWarnings("PMD.SimplifiedTernary")
            @Override
            public String apply(Object t)
            {
                if (t == null)
                {
                    return null;
                }

                boolean useQuote = sparseQuotes ? rand.nextInt(10) % 10 == 0 : true;
                if (quoteChar != null && useQuote)
                {
                    StringBuilder builder = new StringBuilder();
                    builder.append(quoteChar.charValue()).append(t.toString()).append(quoteChar.charValue());
                    return builder.toString();
                }
                else
                {
                    return t.toString();
                }
            }
        });
        return quotedObjects;
    }

    /**
     * Private constructor.
     */
    private CsvTestUtils()
    {
    }
}

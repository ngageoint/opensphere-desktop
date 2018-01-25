package io.opensphere.myplaces.util;

import java.util.Iterator;

import de.micromata.opengis.kml.v_2_2_0.Data;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.myplaces.constants.Constants;

/**
 * Utility class for ExtendedData.
 *
 */
public final class ExtendedDataUtils
{
    /**
     * Gets the boolean property value from extendedData.
     *
     * @param extendedData Contains the property.
     * @param propertyName The name of the property.
     * @param defaultValue The default value to return if the property was not
     *            found.
     * @return The property value.
     */
    public static boolean getBoolean(ExtendedData extendedData, String propertyName, boolean defaultValue)
    {
        boolean value = defaultValue;

        Data data = getData(extendedData, propertyName);

        if (data != null)
        {
            value = Boolean.parseBoolean(data.getValue());
        }

        return value;
    }

    /**
     * Gets the double property value from extendedData.
     *
     * @param extendedData Contains the property.
     * @param propertyName The name of the property.
     * @param defaultValue The default value to return if the property was not
     *            found.
     * @return The property value.
     */
    public static double getDouble(ExtendedData extendedData, String propertyName, double defaultValue)
    {
        double value = defaultValue;

        Data data = getData(extendedData, propertyName);

        if (data != null)
        {
            value = Double.parseDouble(data.getValue());
        }

        return value;
    }

    /**
     * Gets the int property value from extendedData.
     *
     * @param extendedData Contains the property.
     * @param propertyName The name of the property.
     * @param defaultValue The default value to return if the property was not
     *            found.
     * @return The property value.
     */
    public static int getInt(ExtendedData extendedData, String propertyName, int defaultValue)
    {
        int value = defaultValue;

        Data data = getData(extendedData, propertyName);

        if (data != null)
        {
            value = Integer.parseInt(data.getValue());
        }

        return value;
    }

    /**
     * Gets the string property value from extendedData.
     *
     * @param extendedData Contains the property.
     * @param propertyName The name of the property.
     * @return The property value.
     */
    public static String getString(ExtendedData extendedData, String propertyName)
    {
        String value = null;

        Data data = getData(extendedData, propertyName);

        if (data != null)
        {
            value = data.getValue();
        }

        return value;
    }

    /**
     * Gets the render type from the extended data of the placemark.
     *
     * @param extendedData The extended data containing render type.
     * @return The render type.
     */
    public static MapVisualizationType getVisualizationType(ExtendedData extendedData)
    {
        MapVisualizationType visType = MapVisualizationType.ANNOTATION_POINTS;
        if (extendedData != null)
        {
            for (Data data : extendedData.getData())
            {
                if (data.getName().equals(Constants.MAP_VISUALIZATION_ID))
                {
                    visType = MapVisualizationType.valueOf(data.getValue());
                    break;
                }
            }
        }
        return visType;
    }

    /**
     * Puts the boolean value in the extended data with the given property name.
     *
     * @param extendedData The extended data to contain the value.
     * @param propertyName The property name.
     * @param value The property value.
     */
    public static void putBoolean(ExtendedData extendedData, String propertyName, boolean value)
    {
        Data data = getData(extendedData, propertyName);

        if (data == null)
        {
            data = new Data(String.valueOf(value));
            data.setName(propertyName);
            extendedData.addToData(data);
        }
        else
        {
            data.setValue(String.valueOf(value));
        }
    }

    /**
     * Puts the double value in the extended data with the given property name.
     *
     * @param extendedData The extended data to contain the value.
     * @param propertyName The property name.
     * @param value The property value.
     */
    public static void putDouble(ExtendedData extendedData, String propertyName, double value)
    {
        Data data = getData(extendedData, propertyName);

        if (data == null)
        {
            data = new Data(String.valueOf(value));
            data.setName(propertyName);
            extendedData.addToData(data);
        }
        else
        {
            data.setValue(String.valueOf(value));
        }
    }

    /**
     * Puts the int value in the extended data with the given property name.
     *
     * @param extendedData The extended data to contain the value.
     * @param propertyName The property name.
     * @param value The property value.
     */
    public static void putInt(ExtendedData extendedData, String propertyName, int value)
    {
        Data data = getData(extendedData, propertyName);

        if (data == null)
        {
            data = new Data(String.valueOf(value));
            data.setName(propertyName);
            extendedData.addToData(data);
        }
        else
        {
            data.setValue(String.valueOf(value));
        }
    }

    /**
     * Puts the string value in the extended data with the given property name.
     *
     * @param extendedData The extended data to contain the value.
     * @param propertyName The property name.
     * @param value The property value.
     */
    public static void putString(ExtendedData extendedData, String propertyName, String value)
    {
        Data data = getData(extendedData, propertyName);

        if (data == null)
        {
            data = new Data(String.valueOf(value));
            data.setName(propertyName);
            extendedData.addToData(data);
        }
        else
        {
            data.setValue(String.valueOf(value));
        }
    }

    /**
     * Puts the visualization value in the extended data.
     *
     * @param extendedData The extended data to contain the value.
     * @param value The property value.
     */
    public static void putVisualizationType(ExtendedData extendedData, MapVisualizationType value)
    {
        Data data = getData(extendedData, Constants.MAP_VISUALIZATION_ID);

        if (data == null)
        {
            data = new Data(String.valueOf(value));
            data.setName(Constants.MAP_VISUALIZATION_ID);
            extendedData.addToData(data);
        }
        else
        {
            data.setValue(String.valueOf(value));
        }
    }

    /**
     * Remove a value from the extended data with the given property name.
     *
     * @param extendedData The extended data to contain the value.
     * @param propertyName The property name.
     */
    public static void removeData(ExtendedData extendedData, String propertyName)
    {
        if (extendedData != null)
        {
            for (Iterator<Data> iter = extendedData.getData().iterator(); iter.hasNext();)
            {
                if (iter.next().getName().equals(propertyName))
                {
                    iter.remove();
                    break;
                }
            }
        }
    }

    /**
     * Gets the data object within extendedData with the specified name.
     *
     * @param extendedData Contains the Data object.
     * @param propertyName The name of the Data object to return.
     * @return The data object or null if one was not found.
     */
    private static Data getData(ExtendedData extendedData, String propertyName)
    {
        Data returnData = null;
        if (extendedData != null)
        {
            for (Data data : extendedData.getData())
            {
                if (data.getName().equals(propertyName))
                {
                    returnData = data;
                    break;
                }
            }
        }
        return returnData;
    }

    /**
     * Utility class.
     */
    private ExtendedDataUtils()
    {
    }
}

package io.opensphere.mantle.data.geom.style.config.v1;

import java.awt.Color;
import java.text.ParseException;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.units.InvalidUnitsException;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.util.ListSupport;

/**
 * The Class StyleParameterConfig.
 */
@XmlRootElement(name = "StyleParameter")
@XmlAccessorType(XmlAccessType.FIELD)
public class StyleParameterConfig
{
    /** for parsing or writing multiple values as lists. */
    private static ListSupport listSupp = new ListSupport('\\', ';');

    /** The Constant LOGGER. */
    @XmlTransient
    private static final Logger LOGGER = Logger.getLogger(StyleParameterConfig.class);

    /** The Parameter key. */
    @XmlAttribute(name = "Key")
    private String myParameterKey;

    /** The Parameter value. */
    @XmlAttribute(name = "Value")
    private String myParameterValue;

    /** The Parameter value class. */
    @XmlAttribute(name = "ValueClass")
    private String myParameterValueClass;

    /**
     * Convert string to value.
     *
     * @param type the parameter value class
     * @param value the value
     * @return the object
     */
    public static Object convertStringToValue(String type, String value)
    {
        if ("NULL".equals(type) || StringUtils.isEmpty(value))
        {
            return null;
        }
        Class<?> aClass = null;
        try
        {
            aClass = Class.forName(type);
            if (aClass == String.class)
            {
                return value;
            }
            else if (aClass == Integer.class)
            {
                return Integer.valueOf(Integer.parseInt(value));
            }
            else if (aClass == Long.class)
            {
                return Long.valueOf(Long.parseLong(value));
            }
            else if (aClass == Short.class)
            {
                return Short.valueOf(Short.parseShort(value));
            }
            else if (aClass == Byte.class)
            {
                return Byte.valueOf(Byte.parseByte(value));
            }
            else if (aClass == Double.class)
            {
                return Double.valueOf(Double.parseDouble(value));
            }
            else if (aClass == Float.class)
            {
                return Float.valueOf(Float.parseFloat(value));
            }
            else if (aClass == Boolean.class)
            {
                return Boolean.valueOf(Boolean.parseBoolean(value));
            }
            else if (aClass == Color.class)
            {
                return new Color(Integer.parseInt(value), true);
            }
            else if (aClass.isEnum())
            {
                return parseEnum(value, aClass);
            }
            else if (List.class.isAssignableFrom(aClass))
            {
                return listSupp.parseList(value);
            }
            else if (Length.class.isAssignableFrom(aClass))
            {
                try
                {
                    return Length.parse(type, value);
                }
                catch (InvalidUnitsException | ParseException e)
                {
                    LOGGER.error("Failed to convert " + aClass + " Value \"" + value + "\" - " + e.getMessage());
                }
            }
            else
            {
                throw new UnsupportedClassConversionError("Could not convert value " + value + " to class " + type);
            }
        }
        catch (NumberFormatException e)
        {
            LOGGER.error("Failed to convert " + aClass + " Value \"" + value + "\" NumberFormatException.");
        }
        catch (ClassNotFoundException e)
        {
            throw new UnsupportedClassConversionError(
                    "Class " + type + " was not valid and could not be used to convert value [" + value + "].", e);
        }

        return null;
    }

    /**
     * Convert value to string.
     *
     * @param value the value
     * @return the string
     */
    @SuppressWarnings("unchecked")
    public static String convertValueToString(Object value)
    {
        if (value == null)
        {
            return null;
        }
        else if (value instanceof Color)
        {
            return Integer.toString(((Color)value).getRGB());
        }
        else if (value.getClass().isEnum())
        {
            return ((Enum<?>)value).name();
        }
        else if (value instanceof List)
        {
            return listSupp.writeList((List<String>)value);
        }
        else if (value instanceof Length)
        {
            return ((Length)value).getMagnitudeString();
        }
        else
        {
            return value.toString();
        }
    }

    /**
     * Parses the enum.
     *
     * @param s The string that can be an enum value.
     * @param c The enum type.
     * @return The enum or null if it could not be parsed.
     */
    private static Object parseEnum(String s, Class<?> c)
    {
        Object[] vals = c.getEnumConstants();
        for (Object v : vals)
        {
            if (((Enum<?>)v).name().equals(s))
            {
                return v;
            }
        }
        return null;
    }

    /**
     * Instantiates a new style parameter config.
     */
    public StyleParameterConfig()
    {
    }

    /**
     * Instantiates a new style parameter config.
     *
     * @param parameterKey the parameter key
     * @param parameterVauleClass the parameter vaule class
     * @param parameterValue the parameter value
     */
    public StyleParameterConfig(String parameterKey, String parameterVauleClass, String parameterValue)
    {
        myParameterKey = parameterKey;
        myParameterValue = parameterValue;
        myParameterValueClass = parameterVauleClass;
    }

    /**
     * Copy Constructor.
     *
     * @param other the other {@link StyleParameterConfig} to copy.
     */
    public StyleParameterConfig(StyleParameterConfig other)
    {
        Utilities.checkNull(other, "other");
        myParameterKey = other.myParameterKey;
        myParameterValue = other.myParameterValue;
        myParameterValueClass = other.myParameterValueClass;
    }

    /**
     * Instantiates a new style parameter config.
     *
     * @param parameter the parameter
     */
    public StyleParameterConfig(VisualizationStyleParameter parameter)
    {
        Utilities.checkNull(parameter, "parameter");
        myParameterKey = parameter.getKey();
        myParameterValueClass = parameter.getValue() == null ? "NULL" : parameter.getValue().getClass().getName();
        myParameterValue = convertValueToString(parameter.getValue());
    }

    @Override
    public boolean equals(Object obj)
    {
        boolean isEqual = false;

        if (obj instanceof StyleParameterConfig)
        {
            StyleParameterConfig other = (StyleParameterConfig)obj;
            isEqual = EqualsHelper.equals(other.myParameterKey, myParameterKey)
                    && EqualsHelper.equals(other.myParameterValue, myParameterValue)
                    && EqualsHelper.equals(other.myParameterValueClass, myParameterValueClass);
        }

        return isEqual;
    }

    /**
     * Gets the parameter key.
     *
     * @return the parameter key
     */
    public String getParameterKey()
    {
        return myParameterKey;
    }

    /**
     * Gets the parameter value.
     *
     * @return the parameter value
     */
    public String getParameterValue()
    {
        return myParameterValue;
    }

    /**
     * Gets the parameter value as object.
     *
     * @return the parameter value as object
     */
    public Object getParameterValueAsObject()
    {
        return convertStringToValue(myParameterValueClass, myParameterValue);
    }

    /**
     * Gets the parameter value class.
     *
     * @return the parameter value class
     */
    public String getParameterValueClass()
    {
        return myParameterValueClass;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myParameterKey == null ? 0 : myParameterKey.hashCode());
        result = prime * result + (myParameterValue == null ? 0 : myParameterValue.hashCode());
        result = prime * result + (myParameterValueClass == null ? 0 : myParameterValueClass.hashCode());
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append(getClass().getSimpleName()).append("  ParamKey[").append(myParameterKey).append("] ParamValueClass[")
                .append(myParameterValueClass).append("] Value[").append(myParameterValue).append(']');

        return sb.toString();
    }
}

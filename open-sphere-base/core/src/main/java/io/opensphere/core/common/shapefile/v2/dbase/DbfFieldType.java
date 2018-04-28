package io.opensphere.core.common.shapefile.v2.dbase;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Enumeration of the supported DBASE field types.
 */
public enum DbfFieldType
{

    FLOAT('F'), CHARACTER('C'), NUMBER('N'), LOGICAL('L'), DATE('D'), DOUBLE('O'), INTEGER('I');
    // Unsure if memo is needed
    // MEMO('M');

    /* This totally would have worked... CHARACTER('C', String.class,
     * String.class.getConstructor(String.class)), NUMBER('N',
     * NumberFormat.class, NumberFormat.class.getMethod("parse", String.class)),
     * LOGICAL('L', Boolean.class, Boolean.class.getConstructor(String.class)),
     * DATE('D', DateFormat.class, DateFormat.class.getMethod("parse",
     * String.class)), */

    /** The logger */
    private static Log LOGGER = LogFactory.getLog(DbfFieldType.class);

    private char value;

    private DbfFieldType(char value)
    {
        this.value = value;
    }

    /**
     * The value.
     *
     * @return the value
     */
    public char getValue()
    {
        return value;
    }

    /**
     * @throws ParseException
     * @throws IllegalAccessException
     * @throws InstantiationException
     *
     */
    public Object getDBFFieldActual(Object source) throws ParseException
    {
        Object returnObject = null;

        String sVal = source == null ? "" : source.toString().trim();
        switch (value)
        {
            case 'F':
            {
                if (sVal.equals(""))
                {
                    returnObject = Double.valueOf(0.);
                }
                else
                {
                    try
                    {
                        returnObject = Double.parseDouble(sVal);
                    }
                    catch (NumberFormatException e)
                    {
                        returnObject = Double.NaN;
                        LOGGER.error("Failed to parse float value [" + sVal + "] returning NaN");
                    }
                }
                break;
            }

            case 'N':
            {
                if (sVal.trim().equals(""))
                {
                    returnObject = Integer.valueOf(0);
                }
                else
                {
                    try
                    {
                        // If there is a period parse as a double.
                        if (sVal.indexOf('.') != -1)
                        {
                            returnObject = Double.parseDouble(sVal);
                        }
                        else
                        {
                            // First try integer, then long if integer fails to
                            // parse.
                            try
                            {
                                returnObject = Integer.parseInt(sVal);
                            }
                            catch (NumberFormatException e)
                            {
                                returnObject = Long.parseLong(sVal);
                            }
                            returnObject = NumberFormat.getNumberInstance().parse(sVal);
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        returnObject = Integer.valueOf(0);
                        LOGGER.error("Failed to parse number value [" + sVal + "] returning 0");
                    }
                }
                break;
            }
            case 'L':
            {
                returnObject = Boolean.valueOf("y".equalsIgnoreCase(sVal) || "t".equalsIgnoreCase(sVal));
                break;
            }
            case 'D':
            {
                returnObject = DateFormat.getDateInstance().parse(sVal, new ParsePosition(0));
                // If returnObject is null, fall through to simple "string" case
                if (returnObject != null)
                {
                    break;
                }
            }
            case 'C':
            default:
            {
                // String. Do Nothing.
                returnObject = source;
                break;
            }
        }

        return returnObject;
    }

    private static Map<Character, DbfFieldType> VALUE_DBFFIELDTYPE_MAP = null;

    /**
     * Get the <code>DbfFieldType</code> for a given char.
     *
     * @param char The type to get.
     * @return DbfFieldType
     */
    public static synchronized DbfFieldType getInstance(char type)
    {

        // Initialize the map if it hasn't be done.
        if (null == VALUE_DBFFIELDTYPE_MAP)
        {
            VALUE_DBFFIELDTYPE_MAP = new HashMap<Character, DbfFieldType>();
            for (DbfFieldType t : DbfFieldType.values())
            {
                VALUE_DBFFIELDTYPE_MAP.put(Character.valueOf(t.getValue()), t);
            }
        }

        return VALUE_DBFFIELDTYPE_MAP.get(type);
    }
}

package io.opensphere.core.common.configuration.date;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Configuration of a single project
 */
@XmlRootElement(name = "DateFormat")
@XmlAccessorType(XmlAccessType.FIELD)
public class DateFormat
{

    public enum Type
    {
        DATE, TIME, TIMESTAMP
    }

    public static final long START_TIME = System.currentTimeMillis();

    public static final DateFormat SAMPLE1 = new DateFormat(Type.TIMESTAMP, "yyyyMMddHHmmss", "\\d{14}");

    public static final DateFormat SAMPLE2 = new DateFormat(Type.TIMESTAMP, "yyyyMMddHHmmss'Z'", "\\d{14}Z");

    public static final DateFormat SAMPLE3 = new DateFormat(Type.TIMESTAMP, "yyyy-M-d'T'HH:mm:ss",
            "\\d{4}\\-\\d{1,2}\\-\\d{1,2}T\\d{2}\\:\\d{2}\\:\\d{2}");

    public static final DateFormat SAMPLE4 = new DateFormat(Type.TIMESTAMP, "yyyy-M-d'T'HH:mm:ss'Z'",
            "\\d{4}\\-\\d{1,2}\\-\\d{1,2}T\\d{2}\\:\\d{2}\\:\\d{2}Z");

    public static final DateFormat SAMPLE5 = new DateFormat(Type.TIMESTAMP, "yyyy-M-d'T'HH:mm:ss.SSS",
            "\\d{4}\\-\\d{1,2}\\-\\d{1,2}T\\d{2}\\:\\d{2}\\:\\d{2}\\.\\d+");

    public static final DateFormat SAMPLE6 = new DateFormat(Type.TIMESTAMP, "yyyy-M-d'T'HH:mm:ss.SSS'Z'",
            "\\d{4}\\-\\d{1,2}\\-\\d{1,2}T\\d{2}\\:\\d{2}\\:\\d{2}\\.\\d+Z");

    public static final DateFormat SAMPLE7 = new DateFormat(Type.TIMESTAMP, "yyyy-M-d HH:mm:ss",
            "\\d{4}\\-\\d{1,2}\\-\\d{1,2}\\s\\d{2}\\:\\d{2}\\:\\d{2}");

    public static final DateFormat SAMPLE33 = new DateFormat(Type.TIMESTAMP, "dd MMM yyyy HH:mm:ss",
            "\\d{2}\\s\\[a-zA-Z]{3}\\s\\d{4}\\s\\d{2}\\:\\d{2}\\:\\d{2}");

    public static final DateFormat SAMPLE34 = new DateFormat(Type.TIMESTAMP, "dd MMM HH:mm:ss",
            "\\d{2}\\s\\[a-zA-Z]{3}\\s\\d{2}\\:\\d{2}\\:\\d{2}");

    public static final DateFormat SAMPLE8 = new DateFormat(Type.TIMESTAMP, "yyyy-M-d HH:mm:ss'Z'",
            "\\d{4}\\-\\d{1,2}\\-\\d{1,2}\\s\\d{2}\\:\\d{2}\\:\\d{2}Z");

    public static final DateFormat SAMPLE9 = new DateFormat(Type.TIMESTAMP, "yyyy-M-d HH:mm:ss.SSS",
            "\\d{4}\\-\\d{1,2}\\-\\d{1,2}\\s\\d{2}\\:\\d{2}\\:\\d{2}\\.\\d+");

    public static final DateFormat SAMPLE10 = new DateFormat(Type.TIMESTAMP, "yyyy-M-d HH:mm:ss.SSS'Z'",
            "\\d{4}\\-\\d{1,2}\\-\\d{1,2}\\s\\d{2}\\:\\d{2}\\:\\d{2}\\.\\d+Z");

    public static final DateFormat SAMPLE11 = new DateFormat(Type.TIMESTAMP, "yyyy/M/d'T'HH:mm:ss",
            "\\d{4}\\/\\d{1,2}\\/\\d{1,2}T\\d{2}\\:\\d{2}\\:\\d{2}");

    public static final DateFormat SAMPLE12 = new DateFormat(Type.TIMESTAMP, "yyyy/M/d'T'HH:mm:ss'Z'",
            "\\d{4}\\/\\d{1,2}\\/\\d{1,2}T\\d{2}\\:\\d{2}\\:\\d{2}Z");

    public static final DateFormat SAMPLE13 = new DateFormat(Type.TIMESTAMP, "yyyy/M/d'T'HH:mm:ss.SSS",
            "\\d{4}\\/\\d{1,2}\\/\\d{1,2}T\\d{2}\\:\\d{2}\\:\\d{2}\\.\\d+");

    public static final DateFormat SAMPLE14 = new DateFormat(Type.TIMESTAMP, "yyyy/M/d'T'HH:mm:ss.SSS'Z'",
            "\\d{4}\\/\\d{1,2}\\/\\d{1,2}T\\d{2}\\:\\d{2}\\:\\d{2}\\.\\d+Z");

    public static final DateFormat SAMPLE15 = new DateFormat(Type.TIMESTAMP, "yyyy/M/d HH:mm:ss",
            "\\d{4}\\/\\d{1,2}\\/\\d{1,2}\\s\\d{2}\\:\\d{2}\\:\\d{2}");

    public static final DateFormat SAMPLE16 = new DateFormat(Type.TIMESTAMP, "yyyy/M/d HH:mm:ss'Z'",
            "\\d{4}\\/\\d{1,2}\\/\\d{1,2}\\s\\d{2}\\:\\d{2}\\:\\d{2}Z");

    public static final DateFormat SAMPLE17 = new DateFormat(Type.TIMESTAMP, "yyyy/M/d HH:mm:ss.SSS",
            "\\d{4}\\/\\d{1,2}\\/\\d{1,2}\\s\\d{2}\\:\\d{2}\\:\\d{2}\\.\\d+");

    public static final DateFormat SAMPLE18 = new DateFormat(Type.TIMESTAMP, "yyyy/M/d HH:mm:ss.SSS'Z'",
            "\\d{4}\\/\\d{1,2}\\/\\d{1,2}\\s\\d{2}\\:\\d{2}\\:\\d{2}\\.\\d+Z");

    public static final DateFormat SAMPLE19 = new DateFormat(Type.TIMESTAMP, "'td'M/d/yyyy HH:mm:ss",
            "td\\d{1,2}\\/\\d{1,2}\\/\\d{4}\\s\\d{2}\\:\\d{2}\\:\\d{2}");

    public static final DateFormat SAMPLE20 = new DateFormat(Type.TIMESTAMP, "'td'M/d/yyyy HH:mm:ss.SSS",
            "td\\d{1,2}\\/\\d{1,2}\\/\\d{4}\\s\\d{2}\\:\\d{2}\\:\\d{2}\\.\\d+");

    public static final DateFormat SAMPLE21 = new DateFormat(Type.TIMESTAMP, "M/d,HHmm'z'", "\\d{1,2}\\/\\d{1,2}\\,\\d{4}z");

    public static final DateFormat SAMPLE22 = new DateFormat(Type.DATE, "yyyy-M-d", "\\d{4}\\-\\d{1,2}\\-\\d{1,2}");

    public static final DateFormat SAMPLE32 = new DateFormat(Type.DATE, "dd MMM yyyy", "\\d{2}\\s\\[a-zA-Z]{3}\\s\\d{4}");

    public static final DateFormat SAMPLE23 = new DateFormat(Type.DATE, "yyyy/M/d", "\\d{4}\\/\\d{1,2}\\/\\d{1,2}");

    public static final DateFormat SAMPLE24 = new DateFormat(Type.DATE, "M-d-yyyy", "\\d{1,2}\\-\\d{1,2}\\-\\d{4}");

    public static final DateFormat SAMPLE25 = new DateFormat(Type.DATE, "M/d/yyyy", "\\d{1,2}\\/\\d{1,2}\\/\\d{4}");

    public static final DateFormat SAMPLE26 = new DateFormat(Type.DATE, "d-M-yyyy", "\\d{1,22}\\-\\d{1,2}\\-\\d{4}");

    public static final DateFormat SAMPLE27 = new DateFormat(Type.DATE, "d/M/yyyy", "\\d{1,2}\\/\\d{1,2}\\/\\d{4}");

    public static final DateFormat SAMPLE28 = new DateFormat(Type.TIME, "HH:mm:ss", "\\d{2}\\:\\d{2}\\:\\d{2}");

    public static final DateFormat SAMPLE29 = new DateFormat(Type.TIME, "HH:mm:ss.SSS", "\\d{2}\\:\\d{2}\\:\\d{2}\\.\\d+");

    public static final DateFormat SAMPLE30 = new DateFormat(Type.TIME, "HH:mm:ss'Z'", "\\d{2}\\:\\d{2}\\:\\d{2}Z");

    public static final DateFormat SAMPLE31 = new DateFormat(Type.TIME, "HH:mm:ss.SSS'Z'", "\\d{2}\\:\\d{2}\\:\\d{2}\\.\\d+Z");

    @XmlAttribute(name = "type")
    private String typeStr = Type.TIMESTAMP.toString();

    @XmlElement(name = "sdf", required = true)
    private String sdf;

    @XmlElement(name = "regex", required = false)
    private String regex;

    @XmlTransient
    private Pattern pattern;

    /**
     * Default constructor
     */
    public DateFormat()
    {
    }

    public DateFormat(Type type, String sdf)
    {
        this();
        setType(type);
        setSdf(sdf);
    }

    public DateFormat(Type type, String sdf, String regex)
    {
        this(type, sdf);
        setRegex(regex);
    }

    /**
     * Sets this {@link DateFormat} equal to another DateFormat
     *
     * @param other the other shape file source
     * @throws NullPointerException if other is null
     */
    public void setEqualTo(DateFormat other)
    {
        if (other == null)
        {
            throw new NullPointerException();
        }

        setType(other.getType());
        setSdf(other.sdf);
        setRegex(other.regex);
    }

    @Override
    public String toString()
    {
        return sdf;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (other != null && other instanceof DateFormat)
        {
            DateFormat that = (DateFormat)other;

            if (typeStr.equals(that.typeStr) && sdf.equals(that.sdf))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (regex == null ? 0 : regex.hashCode());
        result = prime * result + (sdf == null ? 0 : sdf.hashCode());
        result = prime * result + (typeStr == null ? 0 : typeStr.hashCode());
        return result;
    }

    public Type getType()
    {
        return Type.valueOf(typeStr);
    }

    public void setType(Type type)
    {
        typeStr = type.toString();
    }

    public String getSample()
    {
        Date aDate = new Date(START_TIME);
        return getFormat().format(aDate);
    }

    public String getSdf()
    {
        return sdf;
    }

    public void setSdf(String sdf)
    {
        this.sdf = sdf;
    }

    public String getRegex()
    {
        return regex;
    }

    public void setRegex(String regex)
    {
        this.regex = regex;
    }

    public SimpleDateFormat getFormat()
    {
        return new SimpleDateFormat(sdf);
    }

    public Pattern getPattern() throws PatternSyntaxException
    {
        if (regex != null)
        {
            pattern = Pattern.compile(regex);
        }

        return pattern;
    }

}

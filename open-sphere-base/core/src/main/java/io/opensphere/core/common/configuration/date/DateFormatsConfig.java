package io.opensphere.core.common.configuration.date;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.opensphere.core.common.util.JAXBContextHelper;

/**
 * The DateFormatsConfig represents a list of saved DateFormat *
 */
@XmlRootElement(name = "DateFormatsConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class DateFormatsConfig
{
    @XmlTransient
    private static Log myLogger = LogFactory.getLog(DateFormatsConfig.class);

    @XmlElement(name = "DateFormat")
    private final List<DateFormat> myFormats;

    public DateFormatsConfig()
    {
        myFormats = new ArrayList<>();
    }

    /**
     * @return the list of Formats
     */
    public List<DateFormat> getFormats()
    {
        return myFormats;
    }

    /**
     * Add a new format to the configuration.
     *
     * @param format The format to add
     * @return True if format was added, false otherwise.
     */
    public boolean addFormat(DateFormat format)
    {
        if (format == null)
        {
            throw new NullPointerException();
        }
        if (!myFormats.contains(format))
        {
            return myFormats.add(format);
        }
        return false;
    }

    /**
     * Removes a project from the list by name
     *
     * @param name
     */
    public boolean removeProject(DateFormat format)
    {
        return myFormats.remove(format);
    }

    /**
     * Clears and replaces all the formats in the config
     *
     * @param dataLayers
     */
    public void setFormats(Collection<DateFormat> formats)
    {
        myFormats.clear();
        myFormats.addAll(formats);
    }

    public void print()
    {
        try
        {
            JAXBContext ctx = JAXBContextHelper.getCachedContext(DateFormatsConfig.class);
            Marshaller m = ctx.createMarshaller();
            m.setProperty("jaxb.formatted.output", Boolean.TRUE);
            m.marshal(this, System.out);
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * looks through the available list of patterns and grabs patterns that
     * match the supplied time string
     *
     * @param sample, the sample time value
     * @return a list of the matched results, will not be null but may be empty
     *         if none is found.
     */
    public List<DateFormat> findMatchingPatterns(String sample)
    {
        return findMatchingPatterns(null, sample);
    }

    /**
     * looks through the available list of patterns and grabs patterns that
     * match the supplied time string
     *
     * @param matchList - the list to append matching patterns to if provided.
     * @param - the sample timestamp
     * @return the list of matching patterns( this will be the matchList) if
     *         provided or a new list if not, will never be null.
     */
    public List<DateFormat> findMatchingPatterns(List<DateFormat> matchList, String sample)
    {
        if (matchList == null)
        {
            matchList = new ArrayList<>();
        }

        for (int i = 0; i < myFormats.size(); i++)
        {
            if (myFormats.get(i).getPattern() != null)
            {
                if (myFormats.get(i).getPattern().matcher(sample).matches() && !matchList.contains(myFormats.get(i)))
                {
                    matchList.add(myFormats.get(i));
                }
            }
            else
            {
                SimpleDateFormat sdf = myFormats.get(i).getFormat();
                try
                {
                    sdf.parse(sample);
                    if (!matchList.contains(myFormats.get(i)))
                    {
                        matchList.add(myFormats.get(i));
                    }
                }
                catch (ParseException e)
                {
                    // Didn't match
                }
            }
        }
        return matchList;
    }

    /**
     * Looks through the available list of patterns and grabs a pattern that
     * matches. Favors a TIMESTAMP if haltOnTimeStamp is true.
     *
     * @param time
     * @param haltOnTimeStamp
     * @return
     */
    public DateFormat findMatchingPattern(String time, boolean haltOnTimeStamp)
    {
        DateFormat firstMatch = null;
        for (int i = 0; i < myFormats.size(); i++)
        {
            if (myFormats.get(i).getPattern() != null)
            {
                if (myFormats.get(i).getPattern().matcher(time).matches())
                {
                    if (firstMatch == null)
                    {
                        firstMatch = myFormats.get(i);
                    }

                    if (haltOnTimeStamp)
                    {
                        if (myFormats.get(i).getType() == DateFormat.Type.TIMESTAMP)
                        {
                            return myFormats.get(i);
                        }
                    }
                }
            }
        }
        return firstMatch;
    }

    /**
     * Reads a configuration file and appends all formats into this config.
     *
     * @param aFile
     */
    public void appendFormatsFromConfig(File aFile)
    {
        DateFormatsConfig dfc = DateFormatsConfig.loadPatternConfig(aFile);
        if (dfc != null && dfc.getFormats().size() > 0)
        {
            myFormats.addAll(dfc.getFormats());
        }
    }

    /**
     * Loads a DateFormatsConfig from a file
     *
     * @param aFile the file to load
     * @return a {@link DateFormatsConfig} or a new {@link DateFormatsConfig} if
     *         the file wasn't found
     */
    public static DateFormatsConfig loadPatternConfig(File aFile)
    {
        DateFormatsConfig dfc = null;
        try
        {
            if (aFile.exists())
            {
                dfc = loadPatternConfig(new FileInputStream(aFile));
            }
            else
            {
                myLogger.warn("DateFormat configuration file not found: " + aFile.getPath());
                dfc = new DateFormatsConfig();
            }
        }
        catch (IOException e)
        {
            myLogger.warn("Error date format files configuration", e);
            dfc = new DateFormatsConfig();
        }
        return dfc;
    }

    /**
     * Load pattern config.
     *
     * @param is the {@link InputStream}
     * @return the date formats config
     */
    public static DateFormatsConfig loadPatternConfig(InputStream is)
    {
        DateFormatsConfig dfc = null;
        try
        {

            JAXBContext ctx = JAXBContextHelper.getCachedContext(DateFormatsConfig.class);
            Unmarshaller um = ctx.createUnmarshaller();

            if (is.available() > 0)
            {
                dfc = (DateFormatsConfig)um.unmarshal(is);
            }
            else
            {
                myLogger.warn("DateFormat configuration file not found!");
                dfc = new DateFormatsConfig();
            }
        }
        catch (JAXBException e)
        {
            myLogger.warn("Error date format files configuration", e);
            dfc = new DateFormatsConfig();
        }
        catch (IOException e)
        {
            myLogger.warn("Error date format input stream configuration", e);
            dfc = new DateFormatsConfig();
        }
        return dfc;
    }

    /**
     * Saves a config to a file
     *
     * @param config
     * @param aFile
     * @return true if saved, false if some problem occurred
     */
    public static boolean saveConfig(DateFormatsConfig config, File aFile)
    {
        boolean saved = false;
        try
        {
            JAXBContext ctx = JAXBContextHelper.getCachedContext(DateFormatsConfig.class);
            Marshaller m = ctx.createMarshaller();
            myLogger.info("Saving new shape files configuration to " + aFile);
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(config, aFile);
            saved = true;
        }
        catch (JAXBException exc)
        {
            myLogger.warn("Error saving date format configuration", exc);
            saved = false;
        }
        return saved;
    }
}

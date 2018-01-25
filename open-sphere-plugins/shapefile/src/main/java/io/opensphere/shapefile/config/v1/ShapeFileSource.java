package io.opensphere.shapefile.config.v1;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.common.util.zip.Zip;
import io.opensphere.core.common.util.zip.Zip.ZipInputAdapter;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.datasources.DataSourceChangeEvent;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.SingleFileDataSource;
import io.opensphere.mantle.datasources.impl.AbstractDataSource;

/**
 * Configuration of a single shape file source.
 */
@XmlRootElement(name = "ShapeFileSource")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("PMD.GodClass")
public class ShapeFileSource extends AbstractDataSource implements SingleFileDataSource
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ShapeFileSource.class);

    /** The column. */
    @XmlElement(name = "Column", required = false)
    private String myColumn = "NONE";

    /** The column filter. */
    @XmlElement(name = "ColumnFilter", required = false)
    private Set<String> myColumnFilter = new HashSet<>();

    /** The column names. */
    @XmlElement(name = "ColumnNames", required = false)
    private List<String> myColumnNames = new ArrayList<>();

    /** The my data type info. */
    @XmlTransient
    private DataTypeInfo myDataTypeInfo;

    /** The data group info. */
    @XmlTransient
    private DataGroupInfo myDataGroupInfo;

    /** The date column. */
    @XmlElement(name = "dateColumn", required = false)
    private int myDateColumn = -1;

    /** The date format. */
    @XmlElement(name = "dateFormat", required = false)
    private DateFormat myDateFormat;

    /** The enabled. */
    @XmlAttribute(name = "enabled")
    private boolean myEnabled = true;

    /** The visibility flag. */
    @XmlAttribute(name = "visible")
    private boolean myIsVisible = true;

    /** The file paths to formats. */
    @XmlTransient
    private final Map<String, String> myFilePathsToFormats = new HashMap<>();

    /** The filled poly. */
    @XmlElement(name = "FilledPoly", required = false)
    private boolean myFilledPoly;

    /** The line poly. */
    @XmlElement(name = "LinePoly", required = false)
    private boolean myLinePoly = true;

    /** The lines follow terrain. */
    @XmlAttribute(name = "linesFollowTerrain", required = false)
    private boolean myLinesFollowTerrain = true;

    /** The line width. */
    @XmlElement(name = "LineWidth", required = false)
    private int myLineWidth = 1;

    /** The load error. */
    @XmlAttribute(name = "loadError", required = false)
    private boolean myLoadError;

    /** The loads to. */
    @XmlElement(name = "LoadsTo", required = false)
    private LoadsTo myLoadsTo = LoadsTo.BASE;

    /** The loads to base. */
    @XmlAttribute(name = "loadsToBase", required = false)
    private boolean myLoadsToBase = true;

    /** The lob column. */
    @XmlElement(name = "lobColumn", required = false)
    private int myLobColumn = -1;

    /** The max date. */
    @XmlElement(name = "maxDate", required = false)
    private String myMaxDate;

    /** The min date. */
    @XmlElement(name = "minDate", required = false)
    private String myMinDate;

    /** The name. */
    @XmlAttribute(name = "name")
    private String myName = "";

    /** The orient column. */
    @XmlElement(name = "orient", required = false)
    private int myOrientColumn = -1;

    /** The my participating. */
    @XmlTransient
    private boolean myParticipating;

    /** The shape color. */
    @XmlElement(name = "ShapeColor", required = false)
    private String myShapeColor = "255-0-0-255";

    /** The shape file absolute path. */
    @XmlElement(name = "ShapeFileAbsolutePath", required = true)
    private String myShapeFileAbsolutePath = "";

    /** The smaj column. */
    @XmlElement(name = "smajColumn", required = false)
    private int mySmajColumn = -1;

    /** The smin column. */
    @XmlElement(name = "sminColumn", required = false)
    private int mySminColumn = -1;

    /** The text color. */
    @XmlElement(name = "TextColor", required = false)
    private String myTextColor = "0-255-0-255";

    /** only applicable to CSV sources. */
    @XmlElement(name = "TextSize", required = false)
    private String myTextSize = "11";

    /** The time column. */
    @XmlElement(name = "timeColumn", required = false)
    private int myTimeColumn = -1;

    /** The time format. */
    @XmlElement(name = "timeFormat", required = false)
    private DateFormat myTimeFormat;

    /** The time sdf. */
    @XmlTransient
    private final SimpleDateFormat myTimeSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /** The uses timestamp. */
    @XmlAttribute(name = "usesTimestamp", required = false)
    private boolean myUsesTimestamp;

    /** The use time filter. */
    @XmlElement(name = "useTimeFilter", required = false)
    private boolean myUseTimeFilter;

    /** The From state source. */
    @XmlAttribute(name = "fromStateSource")
    private boolean myFromStateSource;

    /**
     * Default constructor.
     */
    public ShapeFileSource()
    {
        myColumnNames = new ArrayList<>();
    }

    /**
     * Instantiates a new shape file source.
     *
     * @param other the other
     */
    public ShapeFileSource(ShapeFileSource other)
    {
        this();
        setEqualTo(other);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        final ShapeFileSource other = (ShapeFileSource)obj;
        //@formatter:off
        return Objects.equals(myColumn, other.myColumn)
                && Objects.equals(myColumnFilter, other.myColumnFilter)
                && Objects.equals(myColumnNames, other.myColumnNames)
                && myDateColumn == other.myDateColumn
                && Objects.equals(myDateFormat, other.myDateFormat)
                && myEnabled == other.myEnabled
                && myIsVisible == other.myIsVisible
                && Objects.equals(myFilePathsToFormats, other.myFilePathsToFormats)
                && myFilledPoly == other.myFilledPoly
                && myLinePoly == other.myLinePoly
                && myLinesFollowTerrain == other.myLinesFollowTerrain
                && myLineWidth == other.myLineWidth
                && myLoadError == other.myLoadError
                && Objects.equals(myLoadsTo, other.myLoadsTo)
                && myLoadsToBase == other.myLoadsToBase
                && myLobColumn == other.myLobColumn
                && Objects.equals(myMaxDate, other.myMaxDate)
                && Objects.equals(myMinDate, other.myMinDate)
                && Objects.equals(myName, other.myName)
                && myOrientColumn == other.myOrientColumn
                && Objects.equals(myShapeColor, other.myShapeColor)
                && Objects.equals(myShapeFileAbsolutePath, other.myShapeFileAbsolutePath)
                && mySmajColumn == other.mySmajColumn
                && mySminColumn == other.mySminColumn
                && Objects.equals(myTextColor, other.myTextColor)
                && Objects.equals(myTextSize, other.myTextSize)
                && myTimeColumn == other.myTimeColumn
                && Objects.equals(myTimeFormat, other.myTimeFormat)
                && myUsesTimestamp == other.myUsesTimestamp
                && myUseTimeFilter == other.myUseTimeFilter;
        //@formatter:on
    }

    @Override
    public boolean exportsAsBundle()
    {
        return true;
    }

    @Override
    public void exportToFile(File selectedFile, Component parent, final ActionListener callback)
    {
        boolean success = true;

        final ShapeFileSource copySource = new ShapeFileSource(this);
        final File aFile = new File(getPath());
        copySource.setPath(aFile.getName());
        copySource.setActive(false);
        copySource.setBusy(false, null);
        copySource.setFrozen(false, null);
        ByteArrayOutputStream cfgXMLBAOS = null;

        try
        {
            cfgXMLBAOS = new ByteArrayOutputStream();
            XMLUtilities.writeXMLObject(copySource, cfgXMLBAOS);
        }
        catch (final JAXBException exc)
        {
            cfgXMLBAOS = null;
            success = false;
        }

        if (cfgXMLBAOS != null)
        {
            final ArrayList<ZipInputAdapter> inputAdapters = new ArrayList<>();
            inputAdapters
                    .add(new Zip.ZipByteArrayInputAdapter("source.opensphere3d", null, cfgXMLBAOS.toByteArray(), ZipEntry.DEFLATED));
            inputAdapters.add(new Zip.ZipFileInputAdapter("data", aFile, ZipEntry.DEFLATED));

            final String[] extensions = { "dbf", "sbn", "sbx", "shx", "txt", "shp.xml", "prj", "htm", "fix" };
            String baseFile = aFile.getAbsolutePath();
            if (baseFile.toLowerCase().endsWith(".shp"))
            {
                baseFile = baseFile.substring(0, baseFile.length() - 3);
            }
            for (final String ext : extensions)
            {
                File tFile = new File(baseFile + ext);
                if (!tFile.exists())
                {
                    tFile = new File(baseFile + ext.toUpperCase());
                }
                if (tFile.exists())
                {
                    inputAdapters.add(new Zip.ZipFileInputAdapter("data", tFile, ZipEntry.DEFLATED));
                }
            }

            try
            {
                Zip.zipfiles(selectedFile, inputAdapters, null, false);
            }
            catch (final IOException e)
            {
                success = false;
                if (!selectedFile.delete())
                {
                    LOGGER.warn("Failed to delete file : " + selectedFile.getAbsolutePath());
                }
            }
        }

        final String result = success ? EXPORT_SUCCESS : EXPORT_FAILED;
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                callback.actionPerformed(new ActionEvent(ShapeFileSource.this, 0, result));
            }
        });
    }

    /**
     * Generate type key.
     *
     * @return the string
     */
    public String generateTypeKey()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("SHP::");
        sb.append(myName);
        sb.append("::");
        sb.append(myShapeFileAbsolutePath);
        return sb.toString();
    }

    /**
     * Gets the column.
     *
     * @return the column
     */
    public String getColumn()
    {
        return myColumn;
    }

    /**
     * Gets the column filter.
     *
     * @return the column filter
     */
    public Set<String> getColumnFilter()
    {
        return myColumnFilter;
    }

    /**
     * Gets the column names.
     *
     * @return the column names
     */
    public List<String> getColumnNames()
    {
        return myColumnNames;
    }

    /**
     * Gets the data group info.
     *
     * @return the data group info
     */
    public DataGroupInfo getDataGroupInfo()
    {
        return myDataGroupInfo;
    }

    /**
     * Gets the data type info.
     *
     * @return the data type info
     */
    public DataTypeInfo getDataTypeInfo()
    {
        return myDataTypeInfo;
    }

    /**
     * Gets the date column.
     *
     * @return the date column
     */
    public int getDateColumn()
    {
        return myDateColumn;
    }

    /**
     * Gets the date format.
     *
     * @return the date format
     */
    public DateFormat getDateFormat()
    {
        return myDateFormat;
    }

    /**
     * Gets the line width.
     *
     * @return the line width
     */
    public int getLineWidth()
    {
        return myLineWidth;
    }

    /**
     * Gets the loads to.
     *
     * @return the loads to
     */
    public LoadsTo getLoadsTo()
    {
        return myLoadsTo;
    }

    /**
     * Gets the lob column.
     *
     * @return the lob column
     */
    public int getLobColumn()
    {
        return myLobColumn;
    }

    /**
     * Gets the max date.
     *
     * @return the max date
     */
    public Date getMaxDate()
    {
        Date dateToReturn;
        try
        {
            dateToReturn = myMaxDate == null ? null : myTimeSDF.parse(myMaxDate);
        }
        catch (final ParseException e)
        {
            dateToReturn = null;
        }
        return dateToReturn;
    }

    /**
     * Gets the min date.
     *
     * @return the min date
     */
    public Date getMinDate()
    {
        Date dateToReturn;
        try
        {
            dateToReturn = myMinDate == null ? null : myTimeSDF.parse(myMinDate);
        }
        catch (final ParseException e)
        {
            dateToReturn = null;
        }
        return dateToReturn;
    }

    @Override
    public String getName()
    {
        return myName;
    }

    /**
     * Gets the orient column.
     *
     * @return the orient column
     */
    public int getOrientColumn()
    {
        return myOrientColumn;
    }

    @Override
    public String getPath()
    {
        return getShapeFileAbsolutePath();
    }

    /**
     * Gets the path format map.
     *
     * @return the path format map
     */
    public Map<String, String> getPathFormatMap()
    {
        return myFilePathsToFormats;
    }

    /**
     * Gets the shape color.
     *
     * @return a Color object constructed from the shape color string
     */
    public Color getShapeColor()
    {
        return ColorUtilities.convertFromColorString(myShapeColor);
    }

    /**
     * Get the specified shapeColor. This will be a color name (one of the
     * static {@link java.awt.Color} fields), or a string in the format r-g-b-a.
     *
     * @return the shapeColor
     */
    public String getShapeColorString()
    {
        return myShapeColor;
    }

    /**
     * Gets the shape file absolute path.
     *
     * @return the shape file absolute path
     */
    public String getShapeFileAbsolutePath()
    {
        return myShapeFileAbsolutePath;
    }

    /**
     * Gets the smaj column.
     *
     * @return the smaj column
     */
    public int getSmajColumn()
    {
        return mySmajColumn;
    }

    /**
     * Gets the smin column.
     *
     * @return the smin column
     */
    public int getSminColumn()
    {
        return mySminColumn;
    }

    /**
     * Gets the text color.
     *
     * @return a Color object constructed from the text color string
     */
    public Color getTextColor()
    {
        return ColorUtilities.convertFromColorString(myTextColor);
    }

    /**
     * Get the specified textColor. This will be a color name (one of the static
     *
     * @return the textColor {@link java.awt.Color} fields), or a string in the
     *         format r-g-b-a.
     */
    public String getTextColorString()
    {
        return myTextColor;
    }

    /**
     * Gets the text size.
     *
     * @return the text size
     */
    public String getTextSize()
    {
        return myTextSize;
    }

    /**
     * Gets the time column.
     *
     * @return the time column
     */
    public int getTimeColumn()
    {
        return myTimeColumn;
    }

    /**
     * Gets the time format.
     *
     * @return the time format
     */
    public DateFormat getTimeFormat()
    {
        return myTimeFormat;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myColumn);
        result = prime * result + HashCodeHelper.getHashCode(myColumnFilter);
        result = prime * result + HashCodeHelper.getHashCode(myColumnNames);
        result = prime * result + HashCodeHelper.getHashCode(myDateColumn);
        result = prime * result + HashCodeHelper.getHashCode(myDateFormat);
        result = prime * result + HashCodeHelper.getHashCode(myEnabled);
        result = prime * result + HashCodeHelper.getHashCode(myIsVisible);
        result = prime * result + HashCodeHelper.getHashCode(myFilePathsToFormats);
        result = prime * result + HashCodeHelper.getHashCode(myFilledPoly);
        result = prime * result + HashCodeHelper.getHashCode(myLinePoly);
        result = prime * result + HashCodeHelper.getHashCode(myLinesFollowTerrain);
        result = prime * result + HashCodeHelper.getHashCode(myLineWidth);
        result = prime * result + HashCodeHelper.getHashCode(myLoadError);
        result = prime * result + HashCodeHelper.getHashCode(myLoadsTo);
        result = prime * result + HashCodeHelper.getHashCode(myLoadsToBase);
        result = prime * result + HashCodeHelper.getHashCode(myLobColumn);
        result = prime * result + HashCodeHelper.getHashCode(myMaxDate);
        result = prime * result + HashCodeHelper.getHashCode(myMinDate);
        result = prime * result + HashCodeHelper.getHashCode(myName);
        result = prime * result + HashCodeHelper.getHashCode(myOrientColumn);
        result = prime * result + HashCodeHelper.getHashCode(myShapeColor);
        result = prime * result + HashCodeHelper.getHashCode(myShapeFileAbsolutePath);
        result = prime * result + HashCodeHelper.getHashCode(mySmajColumn);
        result = prime * result + HashCodeHelper.getHashCode(mySminColumn);
        result = prime * result + HashCodeHelper.getHashCode(myTextColor);
        result = prime * result + HashCodeHelper.getHashCode(myTextSize);
        result = prime * result + HashCodeHelper.getHashCode(myTimeColumn);
        result = prime * result + HashCodeHelper.getHashCode(myTimeFormat);
        result = prime * result + HashCodeHelper.getHashCode(myUsesTimestamp);
        result = prime * result + HashCodeHelper.getHashCode(myUseTimeFilter);
        return result;
    }

    @Override
    public boolean isActive()
    {
        return isEnabled();
    }

    /**
     * Checks if is enabled.
     *
     * @return the enabled
     */
    public boolean isEnabled()
    {
        return myEnabled;
    }

    /**
     * Checks if is filled poly.
     *
     * @return true, if is filled poly
     */
    public boolean isFilledPoly()
    {
        return myFilledPoly;
    }

    /**
     * True if this is a state source.
     *
     * @return true, if is from state source
     */
    public boolean isFromStateSource()
    {
        return myFromStateSource;
    }

    /**
     * Checks if is line poly.
     *
     * @return true, if is line poly
     */
    public boolean isLinePoly()
    {
        return myLinePoly;
    }

    /**
     * Checks if is participating.
     *
     * @return true, if is participating
     */
    public boolean isParticipating()
    {
        return myParticipating;
    }

    /**
     * Checks if this source is visible or not.
     *
     * @return true, if is visible
     */
    public boolean isVisible()
    {
        return myIsVisible;
    }

    /**
     * Lines follow terrain.
     *
     * @return true, if successful
     */
    public boolean linesFollowTerrain()
    {
        return myLinesFollowTerrain;
    }

    @Override
    public boolean loadError()
    {
        return myLoadError;
    }

    @Override
    public void setActive(boolean active)
    {
        setEnabled(active);
    }

    /**
     * Sets the column.
     *
     * @param column the new column
     */
    public void setColumn(String column)
    {
        myColumn = column;
    }

    /**
     * Sets the column filter.
     *
     * @param pColumnFilter the new column filter
     */
    public void setColumnFilter(Set<String> pColumnFilter)
    {
        if (pColumnFilter == null)
        {
            throw new IllegalArgumentException();
        }

        myColumnFilter = pColumnFilter;
    }

    /**
     * Sets the column names.
     *
     * @param pColumns the new column names
     */
    public void setColumnNames(List<String> pColumns)
    {
        myColumnNames = pColumns;
    }

    /**
     * Sets the data group info.
     *
     * @param dgi the new data group info
     */
    public void setDataGroupInfo(DataGroupInfo dgi)
    {
        myDataGroupInfo = dgi;
    }

    /**
     * Sets the data type info.
     *
     * @param dti the new data type info
     */
    public void setDataTypeInfo(DataTypeInfo dti)
    {
        myDataTypeInfo = dti;
    }

    /**
     * Sets the date column.
     *
     * @param dateColumn the new date column
     */
    public void setDateColumn(int dateColumn)
    {
        myDateColumn = dateColumn;
    }

    /**
     * Sets the date format.
     *
     * @param dateFormat the new date format
     */
    public void setDateFormat(DateFormat dateFormat)
    {
        myDateFormat = dateFormat;
    }

    /**
     * Sets the enabled.
     *
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled)
    {
        myEnabled = enabled;
    }

    /**
     * Sets this {@link ShapeFileSource} equal to another ShapeFileSource.
     *
     * @param other the other shape file source
     */
    public final void setEqualTo(ShapeFileSource other)
    {
        myFilePathsToFormats.clear();
        for (final Entry<String, String> entry : other.myFilePathsToFormats.entrySet())
        {
            myFilePathsToFormats.put(entry.getKey(), entry.getValue());
        }

        myName = other.myName;
        myEnabled = other.myEnabled;
        myIsVisible = other.myIsVisible;
        myUsesTimestamp = other.myUsesTimestamp;
        myLinesFollowTerrain = other.myLinesFollowTerrain;
        myLoadError = other.myLoadError;
        myLoadsToBase = other.myLoadsToBase;
        myShapeFileAbsolutePath = other.myShapeFileAbsolutePath;
        myTextSize = other.myTextSize;
        myTextColor = other.myTextColor;
        myShapeColor = other.myShapeColor;
        myColumn = other.myColumn;
        myLineWidth = other.myLineWidth;
        myLobColumn = other.myLobColumn;
        mySmajColumn = other.mySmajColumn;
        mySminColumn = other.mySminColumn;
        myOrientColumn = other.myOrientColumn;
        myFilledPoly = other.myFilledPoly;
        myLinePoly = other.myLinePoly;
        myLoadsTo = other.myLoadsTo;
        myUseTimeFilter = other.myUseTimeFilter;
        myMinDate = other.myMinDate;
        myMaxDate = other.myMaxDate;

        if (other.myDateFormat != null)
        {
            myDateFormat = new DateFormat(other.myDateFormat.getType(), other.myDateFormat.getSdf());
        }
        else
        {
            myDateFormat = null;
        }

        myTimeColumn = other.myTimeColumn;

        if (other.myTimeFormat != null)
        {
            myTimeFormat = new DateFormat(other.myTimeFormat.getType(), other.myTimeFormat.getSdf());
        }
        else
        {
            myTimeFormat = null;
        }

        myColumnNames = new ArrayList<>();
        if (other.myColumnNames != null)
        {
            myColumnNames.addAll(other.myColumnNames);
        }

        myColumnFilter = new HashSet<>();
        if (other.myColumnFilter != null)
        {
            myColumnFilter.addAll(other.myColumnFilter);
        }
    }

    /**
     * Sets the filled poly.
     *
     * @param filledPoly the new filled poly
     */
    public void setFilledPoly(boolean filledPoly)
    {
        myFilledPoly = filledPoly;
    }

    /**
     * Marks this source as one that is associated with a saved state.
     *
     * @param fromStateSource the new from state source
     */
    public void setFromStateSource(boolean fromStateSource)
    {
        myFromStateSource = fromStateSource;
    }

    /**
     * Sets the line poly.
     *
     * @param linePoly the new line poly
     */
    public void setLinePoly(boolean linePoly)
    {
        myLinePoly = linePoly;
    }

    /**
     * Sets the lines follow terrain.
     *
     * @param followsTerrain the new lines follow terrain
     */
    public void setLinesFollowTerrain(boolean followsTerrain)
    {
        myLinesFollowTerrain = followsTerrain;
    }

    /**
     * Sets the line width.
     *
     * @param lineWidth the new line width
     */
    public void setLineWidth(int lineWidth)
    {
        myLineWidth = lineWidth;
    }

    @Override
    public void setLoadError(boolean error, Object source)
    {
        myLoadError = error;
        fireDataSourceChanged(new DataSourceChangeEvent(this, IDataSource.SOURCE_LOAD_ERROR_CHANGED, source));
    }

    /**
     * Sets the loads to.
     *
     * @param loadsTo the new loads to
     */
    public void setLoadsTo(LoadsTo loadsTo)
    {
        myLoadsTo = loadsTo;
    }

    /**
     * Sets the lob column.
     *
     * @param lobColumn the new lob column
     */
    public void setLobColumn(int lobColumn)
    {
        myLobColumn = lobColumn;
    }

    /**
     * Sets the max date.
     *
     * @param maxDate the new max date
     */
    public void setMaxDate(Date maxDate)
    {
        myMaxDate = maxDate == null ? null : myTimeSDF.format(maxDate);
    }

    /**
     * Sets the min date.
     *
     * @param minDate the new min date
     */
    public void setMinDate(Date minDate)
    {
        myMinDate = minDate == null ? null : myTimeSDF.format(minDate);
    }

    @Override
    public void setName(String name)
    {
        myName = name;
    }

    /**
     * Sets the orient column.
     *
     * @param orientColumn the new orient column
     */
    public void setOrientColumn(int orientColumn)
    {
        myOrientColumn = orientColumn;
    }

    /**
     * Sets the participating.
     *
     * @param participating the new participating
     */
    public void setParticipating(boolean participating)
    {
        myParticipating = participating;
    }

    @Override
    public void setPath(String path)
    {
        setShapeFileAbsolutePath(path);
    }

    /**
     * Sets the shape color.
     *
     * @param textColor the new shape color
     */
    public void setShapeColor(Color textColor)
    {
        myShapeColor = ColorUtilities.convertToRGBAColorString(textColor);
    }

    /**
     * Sets the shape color.
     *
     * @param shapeColor the textColor to set
     */
    public void setShapeColor(String shapeColor)
    {
        myShapeColor = shapeColor;
    }

    /**
     * Sets the shape file absolute path.
     *
     * @param shapeFileAbsolutePath the new shape file absolute path
     */
    public void setShapeFileAbsolutePath(String shapeFileAbsolutePath)
    {
        myShapeFileAbsolutePath = shapeFileAbsolutePath;
    }

    /**
     * Sets the smaj column.
     *
     * @param smajColumn the new smaj column
     */
    public void setSmajColumn(int smajColumn)
    {
        mySmajColumn = smajColumn;
    }

    /**
     * Sets the smin column.
     *
     * @param sminColumn the new smin column
     */
    public void setSminColumn(int sminColumn)
    {
        mySminColumn = sminColumn;
    }

    /**
     * Sets the text color.
     *
     * @param textColor the new text color
     */
    public void setTextColor(Color textColor)
    {
        myTextColor = ColorUtilities.convertToRGBAColorString(textColor);
    }

    /**
     * Sets the text color.
     *
     * @param textColor the textColor to set
     */
    public void setTextColor(String textColor)
    {
        myTextColor = textColor;
    }

    /**
     * Sets the text size.
     *
     * @param textSize the new text size
     */
    public void setTextSize(String textSize)
    {
        myTextSize = textSize;
    }

    /**
     * Sets the time column.
     *
     * @param timeColumn the new time column
     */
    public void setTimeColumn(int timeColumn)
    {
        myTimeColumn = timeColumn;
    }

    /**
     * Sets the time format.
     *
     * @param timeFormat the new time format
     */
    public void setTimeFormat(DateFormat timeFormat)
    {
        myTimeFormat = timeFormat;
    }

    /**
     * Sets the uses time filter.
     *
     * @param useTimeFilter the new uses time filter
     */
    public void setUsesTimeFilter(boolean useTimeFilter)
    {
        myUseTimeFilter = useTimeFilter;
    }

    /**
     * Sets the uses timestamp.
     *
     * @param usesTimestamp the new uses timestamp
     */
    public void setUsesTimestamp(boolean usesTimestamp)
    {
        myUsesTimestamp = usesTimestamp;
    }

    /**
     * Sets the visibility flag.
     *
     * @param visible the new visible
     */
    public void setVisible(boolean visible)
    {
        myIsVisible = visible;
    }

    @Override
    public boolean supportsFileExport()
    {
        return true;
    }

    @Override
    public void updateDataLocations(File destDataDir)
    {
        setPath(destDataDir.getAbsolutePath() + File.separator + getPath());
    }

    /**
     * Uses time filter.
     *
     * @return true, if successful
     */
    public boolean usesTimeFilter()
    {
        return myUseTimeFilter;
    }

    /**
     * Uses timestamp.
     *
     * @return true, if successful
     */
    public boolean usesTimestamp()
    {
        return myUsesTimestamp;
    }
}

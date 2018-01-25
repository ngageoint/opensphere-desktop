package io.opensphere.kml.export;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import de.micromata.opengis.kml.v_2_2_0.Data;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Icon;
import de.micromata.opengis.kml.v_2_2_0.IconStyle;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.TimeSpan;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.impl.specialkey.AltitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LongitudeKey;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;
import io.opensphere.mantle.util.TimeSpanUtility;

/**
 * The Class KML22Exporter.
 */
public class KML22Exporter implements KMLExporter
{
    /** Used to log any messages. */
    private static final Logger LOGGER = Logger.getLogger(KML22Exporter.class);

    /** The kml. */
    private Kml myKML;

    /**
     * Whether to export as a KMZ. Set to true if the icon file exists in the
     * preExportModel(ie. were not using a dot).
     */
    private boolean myExportAsKmz;

    /** The icon file url. */
    private String myIconFile;

    /** The icon scale. */
    private double myIconScale;

    /**
     * One-up counter to add to kml file names(so there are no duplicates) in
     * event of a nameless kml.
     */
    private transient int myMissingNameCounter = 1;

    @Override
    public void generateKMLDocument(DataTypeInfo dti, Collection<? extends DataElement> points, Collection<String> columnNames,
            SimpleDateFormat dateFormat, KMLExportOptionsModel preExportModel)
    {
        Document kmlDoc = new Document();
        kmlDoc.setName(preExportModel.getTitleText());
        kmlDoc.setVisibility(Boolean.TRUE);

        if (!preExportModel.isDot())
        {
            setIconProperties(preExportModel);
            kmlDoc.getStyleSelector().add(createIcon());
        }

        Folder folder = createFolder(dti, points, columnNames, dateFormat, preExportModel);
        kmlDoc.addToFeature(folder);

        myKML = new Kml();
        myKML.setFeature(kmlDoc);
    }

    @Override
    public File writeKMLToFile(File file) throws IOException
    {
        File outputFile = file;
        if (myExportAsKmz)
        {
            String fileName = file.getAbsolutePath();
            if (fileName.endsWith(".kml"))
            {
                int index = fileName.lastIndexOf(".kml");
                fileName = new StringBuilder(fileName).replace(index, index + 4, ".kmz").toString();
                outputFile = new File(fileName);
            }
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFile));
            writeKmlToStream(out, myKML);
            writeIconsToStream(out, new File(myIconFile));
            out.close();
        }
        else
        {
            writeKml(file);
        }
        return outputFile;
    }

    /**
     * Sets the icon properties.
     *
     * @param preExportModel the pre export model.
     */
    private void setIconProperties(KMLExportOptionsModel preExportModel)
    {
        myExportAsKmz = true;
        myIconFile = preExportModel.getIconFile();
        myIconScale = preExportModel.getIconScale();
    }

    /**
     * Creates the style element containing the icon.
     *
     * @return the style
     */
    private Style createIcon()
    {
        String name = new File(myIconFile).getName();
        Icon icon = new Icon();
        icon.withHref(name);

        IconStyle iconStyle = new IconStyle();
        iconStyle.setIcon(icon);
        iconStyle.setScale(myIconScale);

        Style style = new Style();
        style.setIconStyle(iconStyle);
        style.setId(name);
        return style;
    }

    /**
     * Write kml files to a (kmz) output stream.
     *
     * @param out the output stream
     * @param mainFile the main kml file
     * @param additionalKmlFiles other kml files to write to the archive
     * @throws IOException on error writing to file
     */
    private void writeKmlToStream(ZipOutputStream out, Kml mainFile, Kml... additionalKmlFiles) throws IOException
    {
        out.setComment("Created with OpenSphere KML 22 Exporter");
        addKmlFileToStream(out, mainFile, true);
        for (Kml kml : additionalKmlFiles)
        {
            addKmlFileToStream(out, kml, false);
        }
    }

    /**
     * Write image files to (kmz) output stream.
     *
     * @param out the output stream to write to
     * @param files the additional files to write
     * @throws IOException on error writing to file
     */
    private void writeIconsToStream(ZipOutputStream out, File... files) throws IOException
    {
        for (File file : files)
        {
            if (file.exists())
            {
                out.putNextEntry(new ZipEntry(file.getName()));
                BufferedImage image = ImageIO.read(file);
                String extension = StringUtilities.getLastSubstring(file.getName(), ".");
                ImageIO.write(image, extension, out);
            }
        }
    }

    /**
     * Adds kml file to output stream.
     *
     * @param out the output stream
     * @param file the kml document to be written
     * @param isMainFile true if called the main kml file, false if called with
     *            file being an additional kml file to write
     * @throws IOException on error writing to file
     */
    private void addKmlFileToStream(ZipOutputStream out, Kml file, boolean isMainFile) throws IOException
    {
        String fileName = null;
        if (file.getFeature() == null || file.getFeature().getName() == null || file.getFeature().getName().length() == 0)
        {
            fileName = "noFeatureNameSet" + myMissingNameCounter++ + ".kml";
        }
        else
        {
            fileName = file.getFeature().getName();
            if (!fileName.endsWith(".kml"))
            {
                fileName = fileName.concat(".kml");
            }
        }
        if (isMainFile)
        {
            fileName = "doc.kml";
        }
        out.putNextEntry(new ZipEntry(URLEncoder.encode(fileName, "UTF-8")));
        file.marshal(out);
        out.closeEntry();
    }

    /**
     * Write to kml file.
     *
     * @param outputFile the file to write to
     * @throws IOException on error writing to file
     */
    private void writeKml(File outputFile) throws IOException
    {
        try
        {
            XMLUtilities.writeXMLObject(myKML, outputFile);
            myKML = null;
        }
        catch (JAXBException e)
        {
            throw new IOException(e);
        }
    }

    /**
     * Creates the kml folder object with the given data elements.
     *
     * @param dti the data type info
     * @param points the data element points
     * @param columnNames the column names
     * @param dateFormat the format of the date field
     * @param preExportModel the model for the pre export options
     * @return the folder
     */
    private Folder createFolder(DataTypeInfo dti, Collection<? extends DataElement> points, Collection<String> columnNames,
            SimpleDateFormat dateFormat, KMLExportOptionsModel preExportModel)
    {
        // needed to create the proper JAXBElement objects with proper namespace
        String timeName = dti.getMetaDataInfo().getTimeKey();
        SimpleDateFormat kmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        Folder folder = new Folder();
        folder.setName("Data Folder");
        int count = 0;
        for (DataElement dPt : points)
        {
            count++;
            Placemark placemark = new Placemark();
            if (myExportAsKmz)
            {
                placemark.setStyleUrl("#" + new File(myIconFile).getName());
            }
            if (dPt.getTimeSpan() != null)
            {
                placemark.setTimePrimitive(createTimeSpan(kmlDateFormat, dPt));
            }
            placemark.setGeometry(KML22GeometryCreatorUtilities.createGeometry(dti, dPt));
            placemark.setId(Integer.toString(count));
            if (preExportModel.isMetadataField())
            {
                placemark.setName(getMetadataField(preExportModel, dPt));
            }
            else
            {
                // set name as prefix + one up counter
                placemark.setName(preExportModel.getRecordText() + Integer.toString(count));
            }

            if (!columnNames.isEmpty())
            {
                ExtendedData data = createExtendedData(dti, columnNames, dateFormat, timeName, dPt);
                placemark.setExtendedData(data);
            }
            folder.getFeature().add(placemark);
        }
        return folder;
    }

    /**
     * Creates the extended data.
     *
     * @param dti the data type info
     * @param columnNames the column names
     * @param dateFormat the date format
     * @param timeName the timeName
     * @param dPt the data element
     * @return the data
     */
    private ExtendedData createExtendedData(DataTypeInfo dti, Collection<String> columnNames, SimpleDateFormat dateFormat,
            String timeName, DataElement dPt)
    {
        ExtendedData ed = new ExtendedData();
        for (String curName : columnNames)
        {
            Object val = dPt.getMetaData().getValue(curName);

            if (val == null)
            {
                if (curName.equals(timeName))
                {
                    val = TimeSpanUtility.formatTimeSpanSingleTimeOnly(dateFormat, dPt.getTimeSpan());
                }
            }

            // Exclude these from the extended data so that after
            // importing, the resultant table
            // does not contain duplicate columns.
            SpecialKey sk = dti.getMetaDataInfo().getSpecialTypeForKey(curName);
            if (!(sk instanceof LatitudeKey) && !(sk instanceof LongitudeKey) && !(sk instanceof AltitudeKey)
                    && !(sk instanceof TimeKey))
            {
                Data dt = new Data(val == null ? "" : val.toString());
                dt.setName(curName);
                ed.getData().add(dt);
            }
        }
        return ed;
    }

    /**
     * Sets name to the value of the metadata field for this data element.
     *
     * @param preExportModel the model representing the export options
     * @param dPt the data element
     * @return the field
     */
    private String getMetadataField(KMLExportOptionsModel preExportModel, DataElement dPt)
    {
        Object metadata = dPt.getMetaData().getValue(preExportModel.getMetadataField());
        if (metadata == null)
        {
            LOGGER.warn("Empty metadata field: \"" + preExportModel.getMetadataField() + "\" used as KML Record name.");
            metadata = "";
        }
        return metadata.toString();
    }

    /**
     * Creates time span.
     *
     * @param kmlDateFormat the date format
     * @param dPt the data element
     * @return the time span as a JAXB element
     */
    private TimeSpan createTimeSpan(SimpleDateFormat kmlDateFormat, DataElement dPt)
    {
        TimeSpan timeSpan = new TimeSpan();
        timeSpan.setBegin(dPt.getTimeSpan().isUnboundedStart() ? null : kmlDateFormat.format(dPt.getTimeSpan().getStartDate()));
        timeSpan.setEnd(dPt.getTimeSpan().isUnboundedEnd() ? null : kmlDateFormat.format(dPt.getTimeSpan().getEndDate()));
        return timeSpan;
    }
}

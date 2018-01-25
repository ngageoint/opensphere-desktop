package io.opensphere.core.util;

import java.util.HashMap;
import java.util.Map;

import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * This class enumerates common MIME-types.
 */
public class MimeType
{
    /** The instantiated MIME types. */
    private static final Map<String, MimeType> ourMimeTypes = new HashMap<>();

    /** The map from display name to MIME type. */
    private static final Map<String, MimeType> ourDisplayNames = new HashMap<>();

    /** The MIME type for BIL files. */
    public static final MimeType BIL = new MimeType("BIL", "BIL Files", "image/bil", "bil");

    /** The MIME type for CSV files. */
    public static final MimeType CSV = new MimeType("CSV", "CSV Files", "text/csv", "csv");

    /** The MIME type for DDS files. */
    public static final MimeType DDS = new MimeType("DDS", "DDS Files", "image/dds", "dds");

    /** GeoTIFF MIME type. */
    public static final MimeType GEOTIFF = new MimeType("GeoTIFF", "GeoTIFF Files", "image/geotiff", "tif", "tiff");

    /** The MIME type for GIF files. */
    public static final MimeType GIF = new MimeType("GIF", "GIF Files", "image/gif", "gif");

    /** The MIME type for GML 2.1.2 files. */
    public static final MimeType GML_212 = new MimeType("GML 2.1.2", "GML 2.1.2 Files", "text/xml; subtype=gml/2.1.2", "xml");

    /** The MIME type for GML 3.1.1 files. */
    public static final MimeType GML_311 = new MimeType("GML 3.1.1", "GML 3.1.1 Files", "text/xml; subtype=gml/3.1.1", "xml");

    /** The MIME type for GeoPackage files. */
    public static final MimeType GPKG = new MimeType("GPKG", "GeoPackage Files", "application/gpkg", "gpkg");

    /** TIFF MIME type. */
    public static final MimeType TIFF = new MimeType("TIFF", "TIFF Files", "image/tiff", "tiff");

    /** The MIME type for HTML files. */
    public static final MimeType HTML = new MimeType("HTML", "HTML Files", "text/html", "html");

    /** The MIME type for JPEG files. */
    public static final MimeType JPEG = new MimeType("JPEG", "JPEG Files", "image/jpeg", "jpeg");

    /** The MIME type for JPG files. */
    public static final MimeType JPG = new MimeType("JPG", "JPG Files", "image/jpg", "jpg");

    /** The MIME type for KLV files. */
    public static final MimeType KLV = new MimeType("KLV", "KLV Files", "video/klv", "klv");

    /** The MIME type for KML files. */
    public static final MimeType KML = new MimeType("KML", "KML Files", "application/vnd.google-earth.kml+xml", "kml");

    /** The MIME type for KMZ files. */
    public static final MimeType KMZ = new MimeType("KMZ", "KMZ Files", "application/vnd.google-earth.kmz", "kmz");

    /** The MIME type for PNG files. */
    public static final MimeType PNG = new MimeType("PNG", "PNG Files", "image/png", "png");

    /** The MIME type for ESRI shape files. */
    public static final MimeType SHAPE = new MimeType("ESRI Shape File", "ESRI Shape Files", "application/zip; subtype=shape",
            "shp");

    /** The MIME type for XML files. */
    public static final MimeType XML = new MimeType("XML", "XML Files", "application/xml", "xml");

    /** The display name for the type. */
    private final String myDisplayName;

    /** The description for the type. */
    private final String myDescription;

    /** The file extension. */
    private final String[] myFileExtensions;

    /** The MIME type. */
    private final String myMimeType;

    /**
     * Get the {@link MimeType} for a given string MIME type.
     *
     * @param type The string.
     * @return The {@link MimeType}, or {@code null} if none was found.
     */
    public static MimeType getMimeType(String type)
    {
        return ourMimeTypes.get(type);
    }

    /**
     * Get the {@link MimeType} for a given string display name.
     *
     * @param type The string.
     * @return The {@link MimeType}, or {@code null} if none was found.
     */
    public static MimeType getMimeTypeFromDisplayName(String type)
    {
        return ourDisplayNames.get(type);
    }

    /**
     * Constructor.
     *
     * @param displayName The display name for the type.
     * @param description The description of the type.
     * @param mimeType The MIME type.
     * @param fileExtensions The file extensions for the type.
     */
    public MimeType(String displayName, String description, String mimeType, String... fileExtensions)
    {
        myDisplayName = displayName;
        myDescription = description;
        myMimeType = mimeType;
        myFileExtensions = fileExtensions.clone();

        ourMimeTypes.put(myMimeType, this);
        ourDisplayNames.put(myDisplayName, this);
    }

    /**
     * Get the description as a string.
     *
     * @return The description.
     */
    public String getDescription()
    {
        return myDescription;
    }

    /**
     * Gets the file extension.
     *
     * @return the file extension
     */
    public String[] getFileExtensions()
    {
        return myFileExtensions.clone();
    }

    /**
     * Gets the file name extension filter.
     *
     * @return the file name extension filter
     */
    public FileNameExtensionFilter getFileFilter()
    {
        return new FileNameExtensionFilter(myDisplayName, myFileExtensions);
    }

    /**
     * Get the MIME type as a string.
     *
     * @return The MIME type.
     */
    public String getMimeType()
    {
        return myMimeType;
    }

    @Override
    public String toString()
    {
        return myDisplayName;
    }
}

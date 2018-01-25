package io.opensphere.kml.common.model;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents a KML content type.
 */
public enum KMLContentType
{
    /** KML. */
    KML("application/vnd.google-earth.kml+xml", "kml"),

    /** KMZ. */
    KMZ("application/vnd.google-earth.kmz", "kmz"),

    /** UNKNOWN. */
    UNKNOWN("", "");

    /** The content type. */
    private final String myContentType;

    /** The file extension. */
    private final String myFileExtension;

    /**
     * Searches for the matching file extension.
     *
     * @param filename File name to check the extension of.
     * @return The content type or <code>UNKNOWN</code> if it cannot be found.
     */
    public static KMLContentType getKMLContentTypeForFilename(String filename)
    {
        KMLContentType contentType = UNKNOWN;
        if (!StringUtils.isBlank(filename))
        {
            String lowerCaseName = filename.toLowerCase();
            for (KMLContentType type : values())
            {
                if (lowerCaseName.endsWith(type.getFileExtension()))
                {
                    contentType = type;
                    break;
                }
            }
        }
        return contentType;
    }

    /**
     * Initializes the enum by setting it's content type and file extension.
     *
     * @param contentType The content type
     * @param fileExtension The file extension
     */
    KMLContentType(String contentType, String fileExtension)
    {
        myContentType = contentType;
        myFileExtension = fileExtension;
    }

    /**
     * Getter for contentType.
     *
     * @return the contentType
     */
    public String getContentType()
    {
        return myContentType;
    }

    /**
     * Getter for fileExtension.
     *
     * @return the fileExtension
     */
    public String getFileExtension()
    {
        return myFileExtension;
    }
}

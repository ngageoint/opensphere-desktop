package io.opensphere.kml.export;

import java.util.Observable;

/**
 * The model used by the pre export file dialog.
 */
public class KMLExportOptionsModel extends Observable
{
    /**
     * The KML document title property.
     */
    public static final String KML_TITLE = "kmlTitle";

    /**
     * The is metadata field property.
     */
    public static final String IS_METADATA_FIELD = "isMetadataField";

    /**
     * The kml record title property.
     */
    public static final String RECORD_TITLE = "recordTitle";

    /**
     * The metadata field property.
     */
    public static final String METADATA_FIELD = "metadataField";

    /**
     * The icon is dot property.
     */
    public static final String IS_DOT = "isDot";

    /**
     * The icon file property.
     */
    public static final String ICON_FILE = "iconFile";

    /** The title text for the entire KML file. */
    private String myKMLTitleText = "OpenSphere KML";

    /** The checkbox indicating one title for all records. */
    private boolean myIsMetadataField = true;

    /** The title text for each KML record. */
    private String myRecordTitleText = "KML Record";

    /** The metadata field radio button. */
    private String myMetadataField = "";

    /** The selector for which field in the metadata to use as the title. */
    private boolean myIsDot = true;

    /** The icon file button. */
    private String myIconFile = "";

    /** The icon scale. */
    private double myIconScale = 1.0;

    /**
     * Gets the title text for the entire KML file.
     *
     * @return the title.
     */
    public String getTitleText()
    {
        return myKMLTitleText;
    }

    /**
     * Whether the record title keys off a metadata field.
     *
     * @return true if keys off metadata field.
     */
    public boolean isMetadataField()
    {
        return myIsMetadataField;
    }

    /**
     * Gets the prefix text for each individual kml record.
     *
     * @return the text.
     */
    public String getRecordText()
    {
        return myRecordTitleText;
    }

    /**
     * Gets the metadata field to key off.
     *
     * @return the field.
     */
    public String getMetadataField()
    {
        return myMetadataField;
    }

    /**
     * Gets the url of the desired icon file.
     *
     * @return the url.
     */
    public String getIconFile()
    {
        return myIconFile;
    }

    /**
     * Gets the icon scale.
     *
     * @return the scale.
     */
    public double getIconScale()
    {
        return myIconScale;
    }

    /**
     * Whether the icon should be a dot.
     *
     * @return true if dot.
     */
    public boolean isDot()
    {
        return myIsDot;
    }

    /**
     * Sets the title text for the kml file.
     *
     * @param text the title text.
     */
    public void setKMLTitleText(String text)
    {
        boolean changed = !text.equals(myKMLTitleText);
        if (changed)
        {
            myKMLTitleText = text;
            setChanged();
            notifyObservers(KML_TITLE);
        }
    }

    /**
     * Sets whether or not to key off a metadata field.
     *
     * @param isField true if keying off a metadata field, false otherwise.
     */
    public void setIsMetadataField(boolean isField)
    {
        boolean changed = isField != myIsMetadataField;
        if (changed)
        {
            myIsMetadataField = isField;
            setChanged();
            notifyObservers(IS_METADATA_FIELD);
        }
    }

    /**
     * Sets the record title prefix text.
     *
     * @param text the prefix text.
     */
    public void setRecordTitleTextField(String text)
    {
        boolean changed = !text.equals(myRecordTitleText);
        if (changed)
        {
            myRecordTitleText = text;
            setChanged();
            notifyObservers(RECORD_TITLE);
        }
    }

    /**
     * Sets the metadata field to key off.
     *
     * @param field the field.
     */
    public void setMetadataField(String field)
    {
        boolean changed = !field.equals(myMetadataField) || myMetadataField.isEmpty() || field.isEmpty();
        if (changed)
        {
            myMetadataField = field;
            setChanged();
            notifyObservers(METADATA_FIELD);
        }
    }

    /**
     * Set whether the icon should be a dot.
     *
     * @param isDot set to true if is dot.
     */
    public void setIsDot(boolean isDot)
    {
        boolean changed = isDot != myIsDot;
        if (changed)
        {
            myIsDot = isDot;
            setChanged();
            notifyObservers(IS_DOT);
        }
    }

    /**
     * Sets the desired icon file.
     *
     * @param iconFile the url of the icon file.
     */
    public void setIconFile(String iconFile)
    {
        boolean changed = !iconFile.equals(myIconFile);
        if (changed)
        {
            myIconFile = iconFile;
            setChanged();
            notifyObservers(ICON_FILE);
        }
    }

    /**
     * Gets the icon scale.
     *
     * @param scale the scale
     */
    public void setIconScale(double scale)
    {
        myIconScale = scale;
    }
}

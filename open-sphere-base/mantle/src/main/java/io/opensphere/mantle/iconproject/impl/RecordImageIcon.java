package io.opensphere.mantle.iconproject.impl;

import java.awt.Image;

import javax.swing.ImageIcon;

import io.opensphere.mantle.icon.IconRecord;

/** An ImageIcon with associated IconRecord. */
public class RecordImageIcon extends ImageIcon
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The record. */
    private final IconRecord myRecord;

    /**
     * Constructor.
     *
     * @param image the image
     * @param record the icon record
     */
    public RecordImageIcon(Image image, IconRecord record)
    {
        super(image);
        myRecord = record;
    }

    /**
     * Gets the record.
     *
     * @return the record
     */
    public IconRecord getRecord()
    {
        return myRecord;
    }
}

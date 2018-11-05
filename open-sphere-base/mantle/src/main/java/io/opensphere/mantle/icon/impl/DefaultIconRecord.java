package io.opensphere.mantle.icon.impl;

import java.net.URL;
import java.util.Objects;

import io.opensphere.core.util.Utilities;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import javafx.scene.image.Image;

/**
 * The Class DefaultIconRecord.
 */
public class DefaultIconRecord implements IconRecord
{
    /** The Collection name. */
    private final String myCollectionNameString;

    /** The Id. */
    private final int myIconId;

    /** The Image provider. */
    private final URL myImageURLValue;

    /** A lazily instantiated image. */
    private Image myImage;

    /** The Source. */
    private final String mySourceKey;

    /** The Sub category. */
    private final String mySubCategoryValue;

    /**
     * Instantiates a new abstract icon record.
     *
     * @param id the id
     * @param ip the ip
     */
    public DefaultIconRecord(int id, IconProvider ip)
    {
        Utilities.checkNull(ip, "ip");
        myIconId = id;
        myImageURLValue = ip.getIconURL();
        myCollectionNameString = ip.getCollectionName() == null ? DEFAULT_COLLECTION : ip.getCollectionName();
        mySubCategoryValue = ip.getSubCategory();
        mySourceKey = ip.getSourceKey();
        myImage = new Image(myImageURLValue.toString());
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
        DefaultIconRecord other = (DefaultIconRecord)obj;
        String imageURLStr = myImageURLValue == null ? null : myImageURLValue.toString();
        String otherImageURLStr = other.myImageURLValue == null ? null : other.myImageURLValue.toString();
        return Objects.equals(myCollectionNameString, other.myCollectionNameString)
                && Objects.equals(imageURLStr, otherImageURLStr) && Objects.equals(mySourceKey, other.mySourceKey)
                && Objects.equals(mySubCategoryValue, other.mySubCategoryValue);
    }

    @Override
    public String getCollectionName()
    {
        return myCollectionNameString;
    }

    @Override
    public int getId()
    {
        return myIconId;
    }

    @Override
    public URL getImageURL()
    {
        return myImageURLValue;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconRecord#getImage()
     */
    @Override
    public Image getImage()
    {
        return myImage;
    }

    @Override
    public String getSourceKey()
    {
        return mySourceKey;
    }

    @Override
    public String getSubCategory()
    {
        return mySubCategoryValue;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myCollectionNameString == null ? 0 : myCollectionNameString.hashCode());
        result = prime * result + (myImageURLValue == null ? 0 : myImageURLValue.toString().hashCode());
        result = prime * result + (mySourceKey == null ? 0 : mySourceKey.hashCode());
        result = prime * result + (mySubCategoryValue == null ? 0 : mySubCategoryValue.hashCode());
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append("IconRecord: ID[").append(myIconId).append("] Collection[").append(myCollectionNameString).append("] SubCat[")
                .append(mySubCategoryValue).append("] Src[").append(mySourceKey).append("] URL[")
                .append(myImageURLValue == null ? "NULL" : myImageURLValue.toString()).append(']');
        return sb.toString();
    }
}

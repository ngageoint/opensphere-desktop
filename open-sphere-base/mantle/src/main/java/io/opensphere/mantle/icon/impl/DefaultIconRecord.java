package io.opensphere.mantle.icon.impl;

import java.net.URL;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;

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
        return EqualsHelper.equals(myCollectionNameString, other.myCollectionNameString)
                && EqualsHelper.equals(imageURLStr, otherImageURLStr) && EqualsHelper.equals(mySourceKey, other.mySourceKey)
                && EqualsHelper.equals(mySubCategoryValue, other.mySubCategoryValue);
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

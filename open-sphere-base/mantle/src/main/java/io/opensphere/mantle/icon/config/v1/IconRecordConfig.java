package io.opensphere.mantle.icon.config.v1;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;

/**
 * The Class IconRecordConfig.
 */
@XmlRootElement(name = "StyleParameter")
@XmlAccessorType(XmlAccessType.FIELD)
public class IconRecordConfig implements IconProvider
{
    /** The Constant LOGGER. */
    @XmlTransient
    private static final Logger LOGGER = Logger.getLogger(IconRecordConfig.class);

    /** The icon ID. */
    @XmlAttribute(name = "id")
    private long myId;

    /** The Parameter value class. */
    @XmlAttribute(name = "collectionName")
    private String myCollectionName;

    /** The Parameter key. */
    @XmlAttribute(name = "imageURL")
    private String myImageURLString;

    /** The Parameter value. */
    @XmlAttribute(name = "sourceKey")
    private String mySourceKey;

    /** The Parameter value. */
    @XmlAttribute(name = "subCategory")
    private String mySubCategory;

    /**
     * Instantiates a new icon record config.
     */
    public IconRecordConfig()
    {
    }

    /**
     * Instantiates a new icon record config.
     *
     * @param rec the rec
     */
    public IconRecordConfig(IconRecord rec)
    {
        myId = rec.idProperty().get();
        myImageURLString = rec.imageURLProperty() == null ? null : rec.imageURLProperty().toString();
        myCollectionName = rec.collectionNameProperty().get();
        mySubCategory = rec.subCategoryProperty().get();
        mySourceKey = rec.sourceKeyProperty().get();
    }

    /**
     * Gets the icon ID.
     *
     * @return the icon ID
     */
    public long getId()
    {
        return myId;
    }

    @Override
    public String getCollectionName()
    {
        return myCollectionName;
    }

    @Override
    public URL getIconURL()
    {
        URL aURL = null;
        if (myImageURLString != null)
        {
            try
            {
                aURL = new URL(myImageURLString);
            }
            catch (MalformedURLException e)
            {
                LOGGER.error("Failed to create URL from config value: [" + myImageURLString + "]");
            }
        }
        return aURL;
    }

    /**
     * Gets the image url string.
     *
     * @return the image url string
     */
    public String getImageURLString()
    {
        return myImageURLString;
    }

    @Override
    public String getSourceKey()
    {
        return mySourceKey;
    }

    @Override
    public String getSubCategory()
    {
        return mySubCategory;
    }

    /**
     * Sets the id.
     *
     * @param id the id
     */
    public void setId(int id)
    {
        myId = id;
    }

    /**
     * Sets the collection name.
     *
     * @param collectionName the new collection name
     */
    public void setCollectionName(String collectionName)
    {
        myCollectionName = collectionName;
    }

    /**
     * Sets the image url string.
     *
     * @param imageURL the new image url string
     */
    public void setImageURLString(String imageURL)
    {
        myImageURLString = imageURL;
    }

    /**
     * Sets the source key.
     *
     * @param sourceKey the new source key
     */
    public void setSourceKey(String sourceKey)
    {
        mySourceKey = sourceKey;
    }

    /**
     * Sets the sub category.
     *
     * @param subCategory the new sub category
     */
    public void setSubCategory(String subCategory)
    {
        mySubCategory = subCategory;
    }
}

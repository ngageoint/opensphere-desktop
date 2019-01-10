package io.opensphere.mantle.icon.config.v1;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import io.opensphere.core.util.io.IOUtilities;
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

    /** The attribute used to mark the record as a favorite. */
    @XmlAttribute(name = "favorite")
    private boolean myFavorite;

    /**
     * Instantiates a new icon record config.
     */
    public IconRecordConfig()
    {
        /* intentionally blank */
    }

    /**
     * Instantiates a new icon record config.
     *
     * @param rec the rec
     */
    public IconRecordConfig(IconRecord rec)
    {
        myId = rec.idProperty().get();
        myImageURLString = rec.imageURLProperty().get() == null ? null : rec.imageURLProperty().get().toString();
        myCollectionName = rec.collectionNameProperty().get();
        mySourceKey = rec.sourceKeyProperty().get();
        myFavorite = rec.favoriteProperty().get();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconProvider#getIconImageData()
     */
    @Override
    public InputStream getIconImageData() throws IOException
    {
        return IOUtilities.getInputStream(getIconURL());
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
                LOGGER.error("Failed to create URL from config value: [" + myImageURLString + "]", e);
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
     * Sets the value of the {@link #myFavorite} field.
     *
     * @param favorite the value to store in the {@link #myFavorite} field.
     */
    public void setFavorite(boolean favorite)
    {
        myFavorite = favorite;
    }

    /**
     * Gets the value of the {@link #myFavorite} field.
     *
     * @return the value stored in the {@link #myFavorite} field.
     */
    @Override
    public boolean isFavorite()
    {
        return myFavorite;
    }
}

package io.opensphere.mantle.icon.impl;

import java.net.URL;
import java.util.Objects;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.javafx.ConcurrentIntegerProperty;
import io.opensphere.core.util.javafx.ConcurrentLongProperty;
import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import io.opensphere.core.util.javafx.ConcurrentStringProperty;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

/**
 * The Class DefaultIconRecord.
 */
public class DefaultIconRecord implements IconRecord
{
    /** The list in which the tags are stored. */
    private final ObservableList<String> myTags = FXCollections.observableArrayList();

    /** The Collection name. */
    private final StringProperty myCollectionNameProperty = new ConcurrentStringProperty();

    /** The property in which the name of the icon is stored. */
    private final StringProperty myNameProperty = new ConcurrentStringProperty();

    /** The property in which the full name of the icon is stored. */
    private final StringProperty myFullNameProperty = new ConcurrentStringProperty();

    /** The property in which the file extension of the icon is stored. */
    private final StringProperty myExtensionProperty = new ConcurrentStringProperty();

    /** The property in which the description of the icon is stored. */
    private final StringProperty myDescriptionProperty = new ConcurrentStringProperty();

    /** The property in which the size (in bytes) of the icon is stored. */
    private final LongProperty mySizeProperty = new ConcurrentLongProperty();

    /** The property in which the height (in pixels) of the icon is stored. */
    private final IntegerProperty myHeightProperty = new ConcurrentIntegerProperty();

    /** The property in which the width (in pixels) of the icon is stored. */
    private final IntegerProperty myWidthProperty = new ConcurrentIntegerProperty();

    /** The property in which the ID of the icon is stored. */
    private final LongProperty myIdProperty = new ConcurrentLongProperty();

    /** The Image provider. */
    private final ObjectProperty<URL> myImageURLProperty = new ConcurrentObjectProperty<>();

    /** A lazily instantiated image. */
    private final ObjectProperty<Image> myImage = new ConcurrentObjectProperty<>();

    /** The Source. */
    private final StringProperty mySourceKeyProperty = new ConcurrentStringProperty();

    /** The Sub category. */
    private final StringProperty mySubCategoryProperty = new ConcurrentStringProperty();

    /**
     * Instantiates a new abstract icon record.
     *
     * @param id the id
     * @param ip the ip
     */
    public DefaultIconRecord(long id, IconProvider ip)
    {
        Utilities.checkNull(ip, "ip");
        myIdProperty.set(id);
        myImageURLProperty.set(ip.getIconURL());
        myCollectionNameProperty.set(ip.getCollectionName() == null ? DEFAULT_COLLECTION : ip.getCollectionName());
        mySubCategoryProperty.set(ip.getSubCategory());
        mySourceKeyProperty.set(ip.getSourceKey());
        myImage.set(new Image(myImageURLProperty.toString()));
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
        String imageURLStr = myImageURLProperty == null ? null : myImageURLProperty.toString();
        String otherImageURLStr = other.myImageURLProperty == null ? null : other.myImageURLProperty.toString();
        return Objects.equals(myCollectionNameProperty, other.myCollectionNameProperty)
                && Objects.equals(imageURLStr, otherImageURLStr) && Objects.equals(mySourceKeyProperty, other.mySourceKeyProperty)
                && Objects.equals(mySubCategoryProperty, other.mySubCategoryProperty);
    }

    @Override
    public StringProperty collectionNameProperty()
    {
        return myCollectionNameProperty;
    }

    @Override
    public LongProperty idProperty()
    {
        return myIdProperty;
    }

    @Override
    public ObjectProperty<URL> imageURLProperty()
    {
        return myImageURLProperty;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconRecord#imageProperty()
     */
    @Override
    public ObjectProperty<Image> imageProperty()
    {
        return myImage;
    }

    @Override
    public StringProperty sourceKeyProperty()
    {
        return mySourceKeyProperty;
    }

    @Override
    public StringProperty subCategoryProperty()
    {
        return mySubCategoryProperty;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myCollectionNameProperty == null ? 0 : myCollectionNameProperty.hashCode());
        result = prime * result + (myImageURLProperty == null ? 0 : myImageURLProperty.toString().hashCode());
        result = prime * result + (mySourceKeyProperty == null ? 0 : mySourceKeyProperty.hashCode());
        result = prime * result + (mySubCategoryProperty == null ? 0 : mySubCategoryProperty.hashCode());
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append("IconRecord: ID[").append(myIdProperty.get()).append("] Collection[").append(myCollectionNameProperty)
                .append("] SubCat[").append(mySubCategoryProperty).append("] Src[").append(mySourceKeyProperty).append("] URL[")
                .append(myImageURLProperty == null ? "NULL" : myImageURLProperty.toString()).append(']');
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconRecord#getTags()
     */
    @Override
    public ObservableList<String> getTags()
    {
        return myTags;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconRecord#nameProperty()
     */
    @Override
    public StringProperty nameProperty()
    {
        return myNameProperty;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconRecord#fullNameProperty()
     */
    @Override
    public StringProperty fullNameProperty()
    {
        return myFullNameProperty;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconRecord#extensionProperty()
     */
    @Override
    public StringProperty extensionProperty()
    {
        return myExtensionProperty;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconRecord#sizeProperty()
     */
    @Override
    public LongProperty sizeProperty()
    {
        return mySizeProperty;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconRecord#heightProperty()
     */
    @Override
    public IntegerProperty heightProperty()
    {
        return myHeightProperty;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconRecord#widthProperty()
     */
    @Override
    public IntegerProperty widthProperty()
    {
        return myWidthProperty;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconRecord#descriptionProperty()
     */
    @Override
    public StringProperty descriptionProperty()
    {
        return myDescriptionProperty;
    }
}

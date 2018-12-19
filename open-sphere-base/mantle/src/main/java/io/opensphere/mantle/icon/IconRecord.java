package io.opensphere.mantle.icon;

import java.net.URL;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

/**
 * The Interface IconRecord.
 */
public interface IconRecord
{
    /** The Constant DEFAULT_COLLECTION. */
    String DEFAULT_COLLECTION = "Default";

    /** The Constant FAVORITES_COLLECTION. */
    String FAVORITES_COLLECTION = "Favorites";

    /** The Constant USER_ADDED_COLLECTION. */
    String USER_ADDED_COLLECTION = "User Added";

    /**
     * Gets the property in which the name is maintained.
     *
     * @return the property in which the name is maintained.
     */
    StringProperty nameProperty();

    /**
     * Gets the property in which the name is maintained.
     *
     * @return the property in which the name is maintained.
     */
    StringProperty fullNameProperty();

    /**
     * Gets the property in which the full name is maintained.
     *
     * @return the property in which the full name is maintained.
     */
    StringProperty extensionProperty();

    /**
     * Gets the property in which the size (in bytes) is maintained.
     *
     * @return the property in which the size (in bytes) is maintained.
     */
    LongProperty sizeProperty();

    /**
     * Gets the property in which the height (in pixels) is maintained.
     *
     * @return the property in which the height (in pixels) is maintained.
     */
    IntegerProperty heightProperty();

    /**
     * Gets the property in which the width (in pixels) is maintained.
     *
     * @return the property in which the width (in pixels) is maintained.
     */
    IntegerProperty widthProperty();

    /**
     * Gets the property in which the description is maintained.
     *
     * @return the property in which the description is maintained.
     */
    StringProperty descriptionProperty();

    /**
     * Gets the collection name.
     *
     * @return the collection name
     */
    StringProperty collectionNameProperty();

    /**
     * Gets the list in which the collection of tags associated with the icon
     * are maintained.
     *
     * @return the list in which the collection of tags associated with the icon
     *         are maintained.
     */
    ObservableList<String> getTags();

    /**
     * Gets the id.
     *
     * @return the id
     */
    LongProperty idProperty();

    /**
     * Gets the image provider.
     *
     * @return the image provider
     */
    ObjectProperty<URL> imageURLProperty();

    /**
     * Gets the image in which the icon is rendered.
     *
     * @return the image in which the icon is rendered.
     */
    ObjectProperty<Image> imageProperty();

    /**
     * Gets the source key.
     *
     * @return the source key
     */
    StringProperty sourceKeyProperty();

    /**
     * Gets the sub category.
     *
     * @return the sub category
     */
    StringProperty subCategoryProperty();
}

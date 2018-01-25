package io.opensphere.wps.ui.detail;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

/**
 * A model in which WPS data is stored as properties for notification.
 */
public class WpsProcessModel
{
    /**
     * The property in which the data source provider is stored.
     */
    private final ObjectProperty<String> myProvider;

    /**
     * The property in which the data types are stored. This field reflects a
     * concatenated string value of the {@link #myTypeList} field.
     */
    private final ObjectProperty<String> myTypes;

    /**
     * The property in which a list of types is stored.
     */
    private final ObservableList<String> myTypeList;

    /**
     * The property in which the description field is stored.
     */
    private final ObjectProperty<String> myDescription;

    /**
     * The property in which a list of tags is stored.
     */
    private final ObservableList<String> myTagList;

    /**
     * The property in which the data tags stored. This field reflects a
     * concatenated string value of the {@link #myTagList} field.
     */
    private final ObjectProperty<String> myTags;

    /**
     * The property in which the URLs are stored.
     */
    private final ObjectProperty<String> myUrl;

    /**
     * Creates a new WPS Process model.
     */
    public WpsProcessModel()
    {
        myProvider = new SimpleObjectProperty<>();
        myDescription = new SimpleObjectProperty<>();
        myUrl = new SimpleObjectProperty<>();

        myTagList = FXCollections.observableArrayList();
        myTags = new SimpleObjectProperty<>();
        myTypes = new SimpleObjectProperty<>();
        myTypeList = FXCollections.observableArrayList();

        myTagList.addListener((javafx.collections.ListChangeListener.Change<? extends String> c) ->
        {
            StringBuilder builder = new StringBuilder();
            c.getList().forEach(t -> builder.append(",").append(t));
            if (builder.length() > 0)
            {
                builder.deleteCharAt(0);
            }
            myTags.setValue(builder.toString());
        });

        myTypeList.addListener((Change<? extends String> c) ->
        {
            StringBuilder builder = new StringBuilder();
            c.getList().forEach(t -> builder.append(",").append(t));
            if (builder.length() > 0)
            {
                builder.deleteCharAt(0);
            }
            myTypes.setValue(builder.toString());
        });
    }

    /**
     * Gets the value of the {@link #myDescription} field.
     *
     * @return the value stored in the {@link #myDescription} field.
     */
    public ObjectProperty<String> getDescription()
    {
        return myDescription;
    }

    /**
     * Gets the value of the {@link #myProvider} field.
     *
     * @return the value stored in the {@link #myProvider} field.
     */
    public ObjectProperty<String> getProvider()
    {
        return myProvider;
    }

    /**
     * Gets the value of the {@link #myTags} field.
     *
     * @return the value stored in the {@link #myTags} field.
     */
    public ObjectProperty<String> getTags()
    {
        return myTags;
    }

    /**
     * Gets the value of the {@link #myTagList} field.
     *
     * @return the value stored in the {@link #myTagList} field.
     */
    public ObservableList<String> getTagList()
    {
        return myTagList;
    }

    /**
     * Gets the value of the {@link #myTypes} field.
     *
     * @return the value stored in the {@link #myTypes} field.
     */
    public ObjectProperty<String> getTypes()
    {
        return myTypes;
    }

    /**
     * Gets the value of the {@link #myTypes} field.
     *
     * @return the value stored in the {@link #myTypes} field.
     */
    public ObservableList<String> getTypeList()
    {
        return myTypeList;
    }

    /**
     * Gets the value of the {@link #myUrl} field.
     *
     * @return the value stored in the {@link #myUrl} field.
     */
    public ObjectProperty<String> getUrl()
    {
        return myUrl;
    }
}

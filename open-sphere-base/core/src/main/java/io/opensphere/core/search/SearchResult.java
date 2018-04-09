package io.opensphere.core.search;

import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;

/**
 * Represents a single result from a search provider.
 */
public class SearchResult
{
    /**
     * A value between 0 and 1, 0 being less likely it matches the keyword term,
     * 1 being very likely it matches the keyword term.
     */
    private float myConfidence;

    /** A more detailed text of the result, if there is one. */
    private String myDescription;

    /**
     * The locations of the search result. One location means a point will be
     * drawn on the globe representing this result, multiple locations means a
     * polyine/polygon will be drawn on the globe representing this result.
     */
    private final List<LatLonAlt> myLocations = New.list();

    /**
     * The search type this result came from, i.e. Place Name, Layers, etc. This
     * is dependent upon the {@link SearchProvider} this result came from.
     */
    private String mySearchType;

    /** The text or name of the result. */
    private String myText;

    /** The property in which the focus of the element is maintained. */
    private final BooleanProperty myFocusedProperty = new SimpleBooleanProperty(false);

    /** The property in which the hovered state of the element is maintained. */
    private final BooleanProperty myHoveredProperty = new SimpleBooleanProperty(false);

    /** The property in which the selection state is maintained. */
    private final BooleanProperty mySelectedProperty = new SimpleBooleanProperty(false);

    /** Whether to have the framework create a geometry for the result. */
    private boolean myCreateGeometry = true;

    /**
     * Gets the value of the {@link #myFocusedProperty} field.
     *
     * @return the value stored in the {@link #myFocusedProperty} field.
     */
    public BooleanProperty focusedProperty()
    {
        return myFocusedProperty;
    }

    /**
     * Gets the value of the {@link #myHoveredProperty} field.
     *
     * @return the value stored in the {@link #myHoveredProperty} field.
     */
    public BooleanProperty hoveredProperty()
    {
        return myHoveredProperty;
    }

    /**
     * Gets the value of the {@link #mySelectedProperty} field.
     *
     * @return the value stored in the {@link #mySelectedProperty} field.
     */
    public BooleanProperty selectedProperty()
    {
        return mySelectedProperty;
    }

    /**
     * Gets a value between 0 and 1, 0 being less likely it matches the keyword
     * term, 1 being very likely it matches the keyword term.
     *
     * @return the confidence A value between 0 and 1, 0 being less likely it
     *         matches the keyword term, 1 being very likely it matches the
     *         keyword term.
     */
    public float getConfidence()
    {
        return myConfidence;
    }

    /**
     * Gets a more detailed text of the result, if there is one.
     *
     * @return the description.
     */
    public String getDescription()
    {
        return myDescription;
    }

    /**
     * The locations of the search result. One location means a point will be
     * drawn on the globe representing this result, multiple locations means a
     * polyline / polygon will be drawn on the globe representing this result.
     *
     * @return the locations.
     */
    public List<LatLonAlt> getLocations()
    {
        return myLocations;
    }

    /**
     * Gets the search type this result came from, i.e. Place Name, Layers, etc.
     * This is dependent upon the {@link SearchProvider} this result came from.
     *
     * @return the searchType.
     */
    public String getSearchType()
    {
        return mySearchType;
    }

    /**
     * Gets the text or name of the result.
     *
     * @return The text or name of the result.
     */
    public String getText()
    {
        return myText;
    }

    /**
     * Sets A value between 0 and 1, 0 being less likely it matches the keyword
     * term, 1 being very likely it matches the keyword term.
     *
     * @param confidence the confidence to set.
     */
    public void setConfidence(float confidence)
    {
        myConfidence = confidence;
    }

    /**
     * Sets a more detailed text of the result, if there is one.
     *
     * @param description the description to set.
     */
    public void setDescription(String description)
    {
        myDescription = description;
    }

    /**
     * Sets The search type this result came from, i.e. Place Name, Layers, etc.
     * This is dependent upon the {@link SearchProvider} this result came from.
     *
     * @param searchType the searchType to set.
     */
    public void setSearchType(String searchType)
    {
        mySearchType = searchType;
    }

    /**
     * Sets the text or name of the result.
     *
     * @param text the text to set
     */
    public void setText(String text)
    {
        myText = text;
    }

    /**
     * Gets whether to have the framework create a geometry for the result.
     *
     * @return whether to have the framework create a geometry for the result
     */
    public boolean isCreateGeometry()
    {
        return myCreateGeometry;
    }

    /**
     * Sets the whether to have the framework create a geometry for the result.
     *
     * @param createGeometry whether to have the framework create a geometry for
     *            the result
     */
    public void setCreateGeometry(boolean createGeometry)
    {
        myCreateGeometry = createGeometry;
    }
}

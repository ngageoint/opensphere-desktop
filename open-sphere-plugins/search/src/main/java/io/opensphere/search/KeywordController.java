package io.opensphere.search;

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.Toolbox;
import io.opensphere.core.mgrs.MGRSConverter;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.search.SearchRegistry;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.viewer.impl.ViewerAnimator;
import io.opensphere.search.model.SearchModel;

/** This class controls the "goto" dialog. */
public class KeywordController
{
    /** The current validated position. */
    private GeographicPosition myPosition;

    /** The search registry. */
    private final SearchRegistry mySearchRegistry;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * The transformer used to display the point and label (for lat/lon and
     * mgrs).
     */
    private final SearchTransformer myTransformer;

    /** The model in which data is stored. */
    private SearchModel myModel;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox.
     * @param transformer The transformer.
     */
    public KeywordController(Toolbox toolbox, SearchTransformer transformer)
    {
        myToolbox = toolbox;
        mySearchRegistry = toolbox.getSearchRegistry();
        myTransformer = transformer;
    }

    /**
     * Center on.
     *
     * @param coords the coords
     */
    public void centerOn(String coords)
    {
        performCenterOn(coords);
    }

    /**
     * Clear search results.
     */
    public void clearSearchResults()
    {
        FXUtilities.runOnFXThread(() -> myModel.getKeyword().set(null));
        mySearchRegistry.clearSearchResults();
    }

    /**
     * Standard accessor.
     *
     * @return The current geographic position (may be null).
     */
    public GeographicPosition getPosition()
    {
        return myPosition;
    }

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Removes the all goto geometries.
     */
    public void removeAllGotoGeometries()
    {
        myTransformer.removeAllGeometries();
    }

    /**
     * Use the search service and display the valid KML results.
     *
     * @param searchStr the search str
     */
    public void serviceSearch(String searchStr)
    {
        // TODO Do we need to URL encode the search string?
        if (!StringUtils.isBlank(searchStr))
        {
            mySearchRegistry.initiateSearch(searchStr);
        }
        FXUtilities.runOnFXThread(() -> myModel.getKeyword().set(searchStr));
        myPosition = null;
    }

    /**
     * Goes through and validates that the input is valid. There is a list here
     * and it first checks degrees-minutes-seconds lat/lon, MGRS, place names,
     * and then service query (when turned on).
     *
     * @param coords the coords
     * @return true, if successful
     */
    public boolean validate(String coords)
    {
        return validateDMS(coords) || validateMGRS(coords);
    }

    /**
     * Zoom to.
     *
     * @param positionStr the position string
     */
    public void zoomTo(String positionStr)
    {
        if (myPosition != null)
        {
            ViewerAnimator viewerAnimator = new ViewerAnimator(myToolbox.getMapManager().getStandardViewer(),
                    Collections.singleton(myPosition), true);
            viewerAnimator.start();
            myTransformer.addGeometries(myPosition, positionStr);
            myPosition = null;
        }
        else
        {
            serviceSearch(positionStr);
        }
    }

    /**
     * Helper method to do the actual work of center on.
     *
     * @param positionStr the position string
     */
    private void performCenterOn(String positionStr)
    {
        if (myPosition != null)
        {
            ViewerAnimator viewerAnimator = new ViewerAnimator(myToolbox.getMapManager().getStandardViewer(),
                    Collections.singleton(myPosition), false);
            viewerAnimator.start();
            myTransformer.addGeometries(myPosition, positionStr);
            myPosition = null;
        }
        else
        {
            serviceSearch(positionStr);
        }
    }

    /**
     * Validate the input field for latitude/longitude. This could be specified
     * as decimal degrees or as degrees minutes seconds (including degree and
     * tick marks) separated by space. Should also account for using 'W' rather
     * than negative longitude. If valid, sets the current position.
     *
     * @param coords the coords
     * @return True if the input is valid and parsible lat/lon, false otherwise.
     */
    private boolean validateDMS(String coords)
    {
        LatLonAlt lla = LatLonAlt.parse(coords);
        if (lla == null)
        {
            return false;
        }
        myPosition = new GeographicPosition(lla);
        return true;
    }

    /**
     * Validate the input field for MGRS. If valid, sets the current position.
     *
     * @param coords the coords
     * @return True if the input is a valid MGRS string, false otherwise.
     */
    private boolean validateMGRS(String coords)
    {
        MGRSConverter converter = new MGRSConverter();
        GeographicPosition geoPos = converter.convertToLatLon(coords);
        if (geoPos == null)
        {
            return false;
        }
        myPosition = geoPos;
        return true;
    }

    /**
     * Sets the value of the {@link #myModel} field.
     *
     * @param model the value to store in the {@link #myModel} field.
     */
    public void setModel(SearchModel model)
    {
        myModel = model;
    }
}

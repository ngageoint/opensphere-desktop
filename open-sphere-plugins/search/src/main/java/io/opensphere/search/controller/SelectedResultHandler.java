package io.opensphere.search.controller;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import com.google.common.collect.BiMap;

import io.opensphere.core.MapManager;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultMouseBinding;
import io.opensphere.core.control.DiscreteEventAdapter;
import io.opensphere.core.control.PickListener;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.search.SearchResult;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.Viewer2D;
import io.opensphere.core.viewer.impl.ViewerAnimator;
import io.opensphere.search.model.SearchModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Handles mouse over's and mouse clicks to and from the search result list and
 * the globe.
 */
public class SelectedResultHandler extends DiscreteEventAdapter implements PickListener
{
    /**
     * Creates a new {@link ViewerAnimator}.
     */
    private final ViewerAnimatorCreator myAnimatorCreator;

    /**
     * Used to listen for select and hover events from the globe.
     */
    private final ControlRegistry myControlRegistry;

    /**
     * The geometries and search results.
     */
    private final BiMap<SearchResult, Geometry> myGeometries;

    /**
     * The label geometries and search results.
     */
    private final BiMap<SearchResult, Geometry> myLabelGeometries;

    /**
     * Listens for the search UI hovering over search results.
     */
    private final ChangeListener<SearchResult> myHoverListener = this::hoverChanged;

    /**
     * Indicates if the hover or selection changes to the model were made by
     * this class.
     */
    private boolean myInitiatedEvent;

    /**
     * The last pick event.
     */
    private PickEvent myLastPickEvent;

    /**
     * Used to move the globe to a search result.
     */
    private final MapManager myMapManager;

    /**
     * The model used by the search.
     */
    private final SearchModel myModel;

    /**
     * Listens for the search UI selecting a search result.
     */
    private final ChangeListener<SearchResult> myFocusedListener = this::focusedChanged;

    /**
     * Listens for the search UI selecting a search result.
     */
    private final ChangeListener<SearchResult> mySelectionListener = this::selectionChanged;

    /**
     * Constructs a new selected result handler.
     *
     * @param model The model used by the search.
     * @param controlRegistry Used to listen for select and hover events from
     *            the globe.
     * @param mapManager Used to move the globe to a search result.
     * @param geometries The geometries and search results.
     * @param labelGeometries the map to store the published label geometries,
     *            mapped by their corresponding {@link SearchResult}.
     * @param animatorCreator Creates a new {@link ViewerAnimator}.
     */
    public SelectedResultHandler(SearchModel model, ControlRegistry controlRegistry, MapManager mapManager,
            BiMap<SearchResult, Geometry> geometries, BiMap<SearchResult, Geometry> labelGeometries,
            ViewerAnimatorCreator animatorCreator)
    {
        super("SearchResults", "Search Results Selection Handler", "Handles selection events for search results.");
        myModel = model;
        myControlRegistry = controlRegistry;
        myMapManager = mapManager;
        myGeometries = geometries;
        myLabelGeometries = labelGeometries;
        myAnimatorCreator = animatorCreator;
        myModel.getHoveredResult().addListener(myHoverListener);
        myModel.getFocusedResult().addListener(myFocusedListener);
        myModel.getSelectedResult().addListener(mySelectionListener);
        myControlRegistry.getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT).addPickListener(this);
        myControlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT).addListener(this,
                new DefaultMouseBinding(MouseEvent.MOUSE_CLICKED));
    }

    /**
     * Stops listening to events.
     */
    public void close()
    {
        myControlRegistry.getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT).removePickListener(this);
        myControlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT).removeListener(this);
        myModel.getHoveredResult().removeListener(myHoverListener);
        myModel.getSelectedResult().removeListener(mySelectionListener);
        myModel.getFocusedResult().removeListener(myFocusedListener);
    }

    @Override
    public void eventOccurred(InputEvent event)
    {
        if (event instanceof MouseEvent)
        {
            final MouseEvent mouseEvent = (MouseEvent)event;
            if (myLastPickEvent != null && mouseEvent.getID() == MouseEvent.MOUSE_CLICKED
                    && mouseEvent.getButton() == MouseEvent.BUTTON1)
            {
                SearchResult searchResult;
                if (myLastPickEvent.getPickedGeometry() instanceof LabelGeometry)
                {
                    searchResult = myLabelGeometries.inverse().get(myLastPickEvent.getPickedGeometry());
                }
                else
                {
                    searchResult = myGeometries.inverse().get(myLastPickEvent.getPickedGeometry());
                }
                if (searchResult != null)
                {
                    myInitiatedEvent = true;
                    ObjectProperty<SearchResult> resultProperty = mouseEvent.getClickCount() > 1
                            ? myModel.getDoubleSelectedResult() : myModel.getSelectedResult();
                    if (searchResult.equals(resultProperty.get()))
                    {
                        resultProperty.set(null);
                    }
                    else
                    {
                        resultProperty.set(searchResult);
                    }
                    myInitiatedEvent = false;
                }
            }
        }
    }

    @Override
    public void handlePickEvent(PickEvent evt)
    {
        Geometry picked = evt.getPickedGeometry();
        SearchResult searchResult;
        if (picked instanceof LabelGeometry)
        {
            searchResult = myLabelGeometries.inverse().get(picked);
        }
        else
        {
            searchResult = myGeometries.inverse().get(picked);
        }
        if (searchResult != null)
        {
            myLastPickEvent = evt;
            myInitiatedEvent = true;
            myModel.getHoveredResult().set(searchResult);
            myInitiatedEvent = false;
        }
        else if (myLastPickEvent != null)
        {
            myLastPickEvent = null;
            myModel.getHoveredResult().set(null);
        }
    }

    /**
     * Updates the colors for each geometry stored in the model, changing it
     * between the selected and unselected colors, based on the selected and
     * hover states in the model.
     */
    private void updateGeometryColors()
    {
        for (SearchResult searchResult : myModel.getAllResults())
        {
            updateColor(searchResult, myGeometries, Color.CYAN);
            updateColor(searchResult, myLabelGeometries, Color.WHITE);
        }

        updateColor(myModel.getSelectedResult().get(), myGeometries, Color.RED);
        updateColor(myModel.getSelectedResult().get(), myLabelGeometries, Color.RED);
        updateColor(myModel.getHoveredResult().get(), myGeometries, Color.RED);
        updateColor(myModel.getHoveredResult().get(), myLabelGeometries, Color.RED);
    }

    /**
     * If the supplied search result is not null, and it's corresponding
     * geometry is both not null and an instance of
     * {@link ColorRenderProperties}, change the item's color to the supplied
     * value.
     *
     * @param searchResult the item for which to change the geometry's color.
     * @param geometries the geometry map from which to extract data to update.
     * @param color the color to which to change the value.
     */
    private void updateColor(SearchResult searchResult, BiMap<SearchResult, Geometry> geometries, Color color)
    {
        if (searchResult != null)
        {
            Geometry geometry = geometries.get(searchResult);
            if (geometry != null && geometry.getRenderProperties() instanceof ColorRenderProperties)
            {
                ColorRenderProperties props = (ColorRenderProperties)geometry.getRenderProperties();
                props.setColor(color);
            }
        }
    }

    /**
     * Handles when the user is hovering over search results in the search ui.
     *
     * @param obs The observable value.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    private void hoverChanged(ObservableValue<? extends SearchResult> obs, SearchResult oldValue, SearchResult newValue)
    {
        updateGeometryColors();
    }

    /**
     * Handles when the user is selecting search results on the map.
     *
     * @param obs The observable value.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    private void selectionChanged(ObservableValue<? extends SearchResult> obs, SearchResult oldValue, SearchResult newValue)
    {
        updateGeometryColors();
    }

    /**
     * Handles when the user is selects a search results in the search ui.
     *
     * @param obs The observable value.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    private void focusedChanged(ObservableValue<? extends SearchResult> obs, SearchResult oldValue, SearchResult newValue)
    {
        if (!myInitiatedEvent)
        {
            if (!EqualsHelper.equals(myModel.getHoveredResult(), newValue))
            {
                updateGeometryColors();
            }

            if (newValue != null)
            {
                GeographicBoundingBox bounds = null;
                for (LatLonAlt location : newValue.getLocations())
                {
                    if (bounds == null)
                    {
                        bounds = new GeographicBoundingBox(location, location);
                    }
                    else
                    {
                        GeographicBoundingBox otherBounds = new GeographicBoundingBox(location, location);
                        bounds = GeographicBoundingBox.merge(bounds, otherBounds);
                    }
                }

                if (bounds != null)
                {
                    DynamicViewer view = myMapManager.getStandardViewer();

                    if (view instanceof Viewer2D)
                    {
                        System.out.println(bounds);
                        Vector3d dest = view.getMapContext().getProjection().convertToModel(bounds.getCenter(),
                                new Vector3d(0., 0., 1.));
                        view.setPosition(view.getCenteredView(dest));
                    }
                    else
                    {
                        ViewerAnimator animator;
                        if (bounds.getWidth() > 0.0 || bounds.getHeight() > 0.0)
                        {
                            animator = myAnimatorCreator.createAnimator(view, bounds.getVertices(), true);
                        }
                        else
                        {

                            animator = myAnimatorCreator.createAnimator(view, bounds.getCenter());
                        }

                        animator.start();
                    }
                }
            }
        }
    }
}

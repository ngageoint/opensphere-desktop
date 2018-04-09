package io.opensphere.wps.ui.detail.bbpicker;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Window;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.RegionEvent;
import io.opensphere.core.event.RegionEvent.RegionEventType;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.SelectionMode;
import io.opensphere.core.util.ThreadConfined;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.javafx.input.SimpleSkin;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.overlay.OverlayToolboxUtils;
import io.opensphere.overlay.SelectionModeController;
import io.opensphere.wps.util.WpsUtilities;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import jidefx.scene.control.validation.ValidationEvent;

/**
 * A "combo-box-like" component, in which a text field and a button are
 * displayed, and through which a bounding box may be drawn on the map.
 */
public class BoundingBoxPicker extends Control
{
    /** The default style. */
    public static final String DEFAULT_STYLE = "-fx-border-style: solid; -fx-border-radius: 3; -fx-border-color: -fx-outer-border;";

    /** The <code>Log</code> instance used for logging. */
    private static final Logger LOG = Logger.getLogger(BoundingBoxPicker.class);

    /**
     * The context for the bounding box. This context is used when usurping the
     * regular selection region bounding box.
     */
    private static final String WPS_USURPATION_CONTEXT = "WPS_USURPATION_CONTEXT";

    /** The toolbox through which application interaction is performed. */
    private final Toolbox myToolbox;

    /** The minimum number of required areas. */
    private final int myMinAreas;

    /** The maximum number of required areas. */
    private final int myMaxAreas;

    /** The Selection mode controller. */
    private final SelectionModeController mySelectionModeController;

    /** The list of bounding boxes. */
    private final ListView<BoundingBox<GeographicPosition>> myListView;

    /** The count label. */
    private final Label myCountLabel;

    /** The add button. */
    private final Button myAddButton;

    /** The node in which the components are rendered. */
    private final VBox myNode;

    /** The region event handler. */
    private final EventListener<? super RegionEvent> myMyRegionEventHandler;

    /**
     * A flag used to determine if the component is currently in map selection
     * mode.
     */
    @ThreadConfined("EDT")
    private boolean myBoundingBoxSelectionMode;

    /** List of visible dialogs. */
    @ThreadConfined("EDT")
    private List<Window> myVisibleDialogs;

    /** The bounding box selected by the user. */
    @ThreadConfined("EventManager-0")
    private BoundingBox<GeographicPosition> mySelectedBoundingBox;

    /** The value of the bounding box. */
    private final StringProperty myValue = new SimpleStringProperty(this, "value");

    /**
     * Creates a new bounding box picker.
     *
     * @param pToolbox the toolbox through which application interaction is
     *            performed.
     * @param minAreas the minimum number of required areas
     * @param maxAreas the maximum number of allowed areas
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public BoundingBoxPicker(Toolbox pToolbox, int minAreas, int maxAreas)
    {
        myToolbox = pToolbox;
        myMinAreas = minAreas;
        myMaxAreas = maxAreas;
        mySelectionModeController = OverlayToolboxUtils.getOverlayToolbox(myToolbox).getSelectionModeController();

        myListView = new ListView<>();
        myListView.setCellFactory(param -> new BboxListCell(myListView.getItems()));
        myListView.setPrefHeight(75);

        myCountLabel = new Label(getCountText());

        myAddButton = FXUtilities.newIconButton("Add from Map", IconType.PLUS, Color.GREEN);
        myAddButton.setTooltip(new Tooltip("Add an area from the map"));
        myAddButton.setOnAction(e -> EventQueueUtilities.invokeLater(() -> buttonClicked(e)));

        myNode = new VBox(5);
        myNode.setPadding(new Insets(5));
        myNode.getChildren().add(FXUtilities.newHBox(myCountLabel, FXUtilities.newHSpacer(), myAddButton));
        myNode.getChildren().add(myListView);
        getChildren().add(myNode);

        setStyle(DEFAULT_STYLE);

        myListView.getItems().addListener((ListChangeListener<BoundingBox<GeographicPosition>>)change -> handleItemsChange());

        myMyRegionEventHandler = event -> regionSelected(event);
        myToolbox.getEventManager().subscribe(RegionEvent.class, myMyRegionEventHandler);
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public StringProperty getValue()
    {
        return myValue;
    }

    /**
     * Fires a validation event.
     */
    public void fireValidationEvent()
    {
        Event.fireEvent(this, new ValidationEvent(isValid() ? ValidationEvent.VALIDATION_OK : ValidationEvent.VALIDATION_ERROR));
    }

    /**
     * Determines if the picker is in a valid state.
     *
     * @return whether the picker is in a valid state
     */
    public boolean isValid()
    {
        return MathUtil.between(myListView.getItems().size(), myMinAreas, myMaxAreas);
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.control.Control#createDefaultSkin()
     */
    @Override
    protected Skin<?> createDefaultSkin()
    {
        return new SimpleSkin(this, myNode);
    }

    /**
     * An event handler method invoked when the button is clicked.
     *
     * @param pEvent the event fired when the button was clicked.
     */
    protected void buttonClicked(ActionEvent pEvent)
    {
        if (!myBoundingBoxSelectionMode)
        {
            myBoundingBoxSelectionMode = true;
            SelectionMode mode = SelectionMode.BOUNDING_BOX;
            myToolbox.getUIRegistry().getRegionSelectionManager().usurpRegionContext(WPS_USURPATION_CONTEXT, mode);
            mySelectionModeController.setSelectionMode(mode);

            myVisibleDialogs = Arrays.stream(Window.getWindows()).filter(w -> w instanceof Dialog && w.isVisible())
                    .collect(Collectors.toList());
            myVisibleDialogs.forEach(w -> w.setVisible(false));
        }
        else
        {
            disableSelectionMode();
        }
    }

    /**
     * An event handler method called when a region selection is made.
     *
     * @param event the event fired when the region was selected.
     */
    protected void regionSelected(RegionEvent event)
    {
        GeographicBoundingBox region = event.getRegion();
        // Save off the region if provided
        if (region != null)
        {
            mySelectedBoundingBox = region;
        }

        BoundingBox<GeographicPosition> boundingBox = mySelectedBoundingBox;
        if (event.getType() == RegionEventType.REGION_COMPLETED && boundingBox != null)
        {
            LOG.info("Region Selected: " + boundingBox);
            mySelectedBoundingBox = null;

            Platform.runLater(() -> myListView.getItems().add(boundingBox));

            EventQueue.invokeLater(() ->
            {
                disableSelectionMode();

                if (CollectionUtilities.hasContent(myVisibleDialogs))
                {
                    myVisibleDialogs.forEach(w -> w.setVisible(true));
                    myVisibleDialogs = null;
                }
            });
        }
    }

    /**
     * Disables selection mode.
     */
    private void disableSelectionMode()
    {
        myBoundingBoxSelectionMode = false;
        mySelectionModeController.setSelectionMode(SelectionMode.NONE);
        myToolbox.getUIRegistry().getRegionSelectionManager().relinquishRegionContext(WPS_USURPATION_CONTEXT);
    }

    /**
     * Handles a change in the items.
     */
    private void handleItemsChange()
    {
        myValue.set(getBboxText());
        myCountLabel.setText(getCountText());
        myAddButton.setDisable(myListView.getItems().size() >= myMaxAreas);
        fireValidationEvent();
    }

    /**
     * Gets the value of the bounding box picker, as a String.
     *
     * @return the value of the bounding box picker, as a string.
     */
    private String getBboxText()
    {
        return myListView.getItems().stream().map(WpsUtilities::boundingBoxToString).collect(Collectors.joining(" "));
    }

    /**
     * Gets the count text.
     *
     * @return the count text
     */
    private String getCountText()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(myListView.getItems().size()).append(" of ").append(myMinAreas);
        if (myMinAreas != myMaxAreas)
        {
            sb.append('-').append(myMaxAreas);
        }
        sb.append(" Areas");
        return sb.toString();
    }

    /** Bounding box list cell. */
    private static class BboxListCell extends RemovableListCell<BoundingBox<GeographicPosition>>
    {
        /** The label. */
        private final Label myLabel;

        /** The decimal format. */
        private static final DecimalFormat FORMAT = new DecimalFormat("#.######");

        /**
         * Constructor.
         *
         * @param items the items model
         */
        public BboxListCell(ObservableList<BoundingBox<GeographicPosition>> items)
        {
            super(items);
            myLabel = new Label();
            getBox().getChildren().addAll(myLabel, FXUtilities.newHSpacer(), createRemoveButton());
        }

        @Override
        protected void updateItem(BoundingBox<GeographicPosition> item)
        {
            myLabel.setText(boundingBoxToDisplayString(item));
        }

        /**
         * Converts the bounding box to a display string.
         *
         * @param boundingBox The bounding box.
         * @return A display string.
         */
        private static String boundingBoxToDisplayString(BoundingBox<GeographicPosition> boundingBox)
        {
            LatLonAlt lowerLeft = boundingBox.getLowerLeft().getLatLonAlt();
            LatLonAlt upperRight = boundingBox.getUpperRight().getLatLonAlt();

            StringBuilder builder = new StringBuilder();
            builder.append(FORMAT.format(lowerLeft.getLonD())).append(',');
            builder.append(FORMAT.format(lowerLeft.getLatD())).append(" to ");
            builder.append(FORMAT.format(upperRight.getLonD())).append(',');
            builder.append(FORMAT.format(upperRight.getLatD()));
            return builder.toString();
        }
    }
}

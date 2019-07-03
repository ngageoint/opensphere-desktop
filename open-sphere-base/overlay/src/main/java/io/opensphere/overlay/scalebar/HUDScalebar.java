package io.opensphere.overlay.scalebar;

import java.awt.Color;
import java.awt.Font;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

import io.opensphere.core.MapManager;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.layout.GridLayout;
import io.opensphere.core.hud.framework.layout.GridLayoutConstraints;
import io.opensphere.core.hud.widget.TextLabel;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.units.InvalidUnitsException;
import io.opensphere.core.units.UnitsProvider;
import io.opensphere.core.units.UnitsProvider.UnitsChangeListener;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.Meters;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeListener;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.overlay.util.AbstractOverlayWindow;

/** The class for rendering of a HUD scale bar. */
public class HUDScalebar extends AbstractOverlayWindow
{
    /** My scale bar label. */
    private TextLabel myLabel;

    /** Listen to events from the main viewer. */
    private final ViewChangeListener myMainViewListener = new ViewChangeListener()
    {
        @Override
        public void viewChanged(final Viewer viewer, ViewChangeSupport.ViewChangeType type)
        {
            myViewChangeExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    showScale();
                }
            });
        }
    };

    /** My scale bar line. */
    private ScalebarLine myScalebarLine;

    /** The current units. */
    private Class<? extends Length> myUnits;

    /** Executor to handle view changes. */
    private final ProcrastinatingExecutor myViewChangeExecutor;

    /** Listener for units changes. */
    private final UnitsChangeListener<Length> myUnitsChangeListener = new UnitsChangeListener<Length>()
    {
      
        @Override
        public void preferredUnitsChanged(Class<? extends Length> type)
        {
            setUnits(type);
        }
    };

    /**
     * Get the largest scale less than the width where the scale is 1, 2 or 5 x
     * 10^n.
     *
     * @param widthSizeInModel The size of the width of this frame in in the
     *            model. This number should be pre-adjusted to the correct
     *            units.
     * @return largest usable scale.
     */
    private static double getScaleMeasure(double widthSizeInModel)
    {
        double exp = Math.pow(10, Math.floor(Math.log10(widthSizeInModel)));
        double val = (int)(widthSizeInModel / exp);
        val = val > 5 ? 5 : val > 2 ? 2 : 1;
        return val * exp;
    }

    /**
     * Constructor.
     *
     * @param hudTransformer The transformer.
     * @param executor Executor shared by HUD components.
     * @param size The bounding box we will use for size (but not location on
     *            screen).
     * @param location The predetermined location.
     * @param resize The resize behavior.
     */
    public HUDScalebar(TransformerHelper hudTransformer, ScheduledExecutorService executor, ScreenBoundingBox size,
            ToolLocation location, ResizeOption resize)
    {
        super(hudTransformer, size, location, resize, ZOrderRenderProperties.TOP_Z - 10);
        myViewChangeExecutor = new ProcrastinatingExecutor(executor, 1000);
        UnitsProvider<Length> unitsProvider = hudTransformer.getToolbox().getUnitsRegistry().getUnitsProvider(Length.class);
        unitsProvider.addListener(myUnitsChangeListener);
        myUnits = unitsProvider.getPreferredUnits();
    }

    @Override
    public void handleCleanupListeners()
    {
        super.handleCleanupListeners();
        getTransformer().getToolbox().getMapManager().getViewChangeSupport().removeViewChangeListener(myMainViewListener);
    }

    @Override
    public void init()
    {
        initBorder();

        // set the layout
        setLayout(new GridLayout(1, 1, this));

        // add scale line and label
        addScaleLine();
        addScaleLabel();

        getLayout().complete();

        // Display the scale bar
        showScale();

        // Register as a listener for view change events
        getTransformer().getToolbox().getMapManager().getViewChangeSupport().addViewChangeListener(myMainViewListener);
    }

    @Override
    public void repositionForInsets()
    {
    }

    /**
     * Change the units used.
     *
     * @param units The new units.
     */
    public synchronized void setUnits(Class<? extends Length> units)
    {
        myUnits = units;
        showScale();
    }

    /** Position the scale label within the frame. */
    private void addScaleLabel()
    {
        TextLabel.Builder builder = new TextLabel.Builder();
        builder.setColor(Color.GREEN);
        builder.setFont(Font.SANS_SERIF + " PLAIN 12");
        myLabel = new TextLabel(this, builder);

        myLabel.setVerticalAlignment(1f);
        myLabel.setHorizontalAlignment(.5f);
        GridLayoutConstraints constr = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(0, 0)));
        add(myLabel, constr);
    }

    /** Position the scale line within the frame. */
    private void addScaleLine()
    {
        myScalebarLine = new ScalebarLine(this);
        GridLayoutConstraints constr = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(0, 0)));
        add(myScalebarLine, constr);
    }

    /**
     * Gets the pair of locations to use for the ends of the scale. The
     * locations are at the vertical center of the view.
     *
     * @param scaleBarWidthPixels The scale bar width in pixels
     * @return The scale end positions
     */
    private Pair<GeographicPosition, GeographicPosition> getScaleEndPositions(double scaleBarWidthPixels)
    {
        MapManager mapManager = getTransformer().getToolbox().getMapManager();

        // Calculate the screen positions at the center of the view to use for
        // determining the distance
        double halfScaleBarWidthPixels = scaleBarWidthPixels / 2.;
        double halfViewPortWidthPixels = mapManager.getStandardViewer().getViewportWidth() / 2.;
        int halfViewPortHeightPixels = (int)Math.round(mapManager.getStandardViewer().getViewportHeight() / 2.);
        int leftScreenPos = (int)Math.round(halfViewPortWidthPixels - halfScaleBarWidthPixels);
        int rightScreenPos = (int)Math.round(halfViewPortWidthPixels + halfScaleBarWidthPixels);
        Vector2i leftScreenVector = new Vector2i(leftScreenPos, halfViewPortHeightPixels);
        Vector2i rightScreenVector = new Vector2i(rightScreenPos, halfViewPortHeightPixels);

        // Convert the screen positions to geographic positions
        GeographicPosition start = mapManager.convertToPosition(leftScreenVector, ReferenceLevel.TERRAIN);
        GeographicPosition end = mapManager.convertToPosition(rightScreenVector, ReferenceLevel.TERRAIN);

        return new Pair<GeographicPosition, GeographicPosition>(start, end);
    }

    /**
     * Calculates the distance between the two locations in the user's preferred
     * unit.
     *
     * @param start The start location
     * @param end The end location
     * @return The distance between the two locations in the user's preferred
     *         unit
     */
    private Length greatCircleDistance(GeographicPosition start, GeographicPosition end)
    {
        // Calculate the scale distance between the points in meters
        double metersDouble = GeographicBody3D.greatCircleDistanceM(start.getLatLonAlt(), end.getLatLonAlt(),
                WGS84EarthConstants.RADIUS_EQUATORIAL_M);
        Meters meters = new Meters(metersDouble);

        // Convert the length in meters to the user's preferred unit
        Length length;
        try
        {
            length = Length.create(myUnits, meters);
        }
        catch (InvalidUnitsException e)
        {
            length = meters;
        }
        return length;
    }

    /** (Re)Draw the scale bar and label based on the viewer position. */
    private synchronized void showScale()
    {
        double scaleBarWidthPixels = getDrawBounds().getWidth();

        // Get the locations of the scale ends (if it were at the view center)
        Pair<GeographicPosition, GeographicPosition> scaleEndPositions = getScaleEndPositions(scaleBarWidthPixels);

        String labelText;
        if (scaleEndPositions.getFirstObject() != null && scaleEndPositions.getSecondObject() != null)
        {
            // Calculate the scale length in the user's preferred unit
            Length length = greatCircleDistance(scaleEndPositions.getFirstObject(), scaleEndPositions.getSecondObject());

            // Scale the length and the scale bar down to the nearest round
            // number
            boolean doRounding = true;
            if (doRounding)
            {
                double scaledLength = getScaleMeasure(length.getDisplayMagnitude());
                scaleBarWidthPixels *= scaledLength / length.getDisplayMagnitude();
                length = Length.create(length.getDisplayClass(), scaledLength);
            }

            labelText = length.toShortLabelString(1, -3);
        }
        else
        {
            labelText = "N/A";
        }

        if ((int)scaleBarWidthPixels != (int)myScalebarLine.getPixelWidth())
        {
            // Update the scale bar
            myScalebarLine.drawScaleBarLines(scaleBarWidthPixels);
        }
        if (!labelText.equals(myLabel.getText()))
        {
            // Update the label
            myLabel.replaceText(labelText);
        }
    }
}

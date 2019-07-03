package io.opensphere.overlay;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.control.ui.ToolbarManager.ToolbarLocation;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.Window.ToolLocation;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.units.UnitsProvider;
import io.opensphere.core.units.UnitsProvider.UnitsChangeListener;
import io.opensphere.core.units.angle.Coordinates;
import io.opensphere.core.units.angle.DecimalDegrees;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeType;
import io.opensphere.core.viewer.Viewer;

/**
 * Transformer for the viewer position overlay.
 */
final class ViewerPositionTransformer extends AbstractOverlayTransformer
{
    /** The Altitude label. */
    private JTextField myAltitudeField;

    /** The current geographic position of the viewer. */
    private GeographicPosition myGeoPosition;

    /** The Heading label. */
    private JTextField myHeadingField;

    /** The screen position for the viewer label. */
    private ScreenPosition myLabelLocation;

    /** A listener for changes to the preferred length units. */
    private final UnitsChangeListener<Length> myLengthUnitsChangeListener = new UnitsChangeListener<Length>()
    {
        @Override
        public void preferredUnitsChanged(Class<? extends Length> type)
        {
            myPreferredLengthUnits = type;
            createViewPositionLabels();
        }
    };

    /** The Pitch label. */
    private JTextField myPitchField;

    /** The Pitch label. */
    private JLabel myPitchLabel;

    /** The currently preferred length units. */
    private volatile Class<? extends Length> myPreferredLengthUnits;

    /** The tool box used by plugins to interact with the rest of the system. */
    private final Toolbox myToolbox;

    /** The Transformer helper. */
    private final TransformerHelper myTransformerHelper;

    /** Flag indicating if the viewer position is to be displayed. */
    private boolean myViewerPositionEnabled;

    /** The Viewer position panel. */
    private JPanel myViewerPositionPanel;

    /** The Window. */
    private ViewerPositionWindow myVPWindow;

    /**
     * Constructor.
     *
     * @param toolbox The tool box used by plugins to interact with the rest of
     *            the system.
     * @param preferences The preferences
     */
    public ViewerPositionTransformer(Toolbox toolbox, Preferences preferences)
    {
        super((DataRegistry)null, preferences);
        myToolbox = toolbox;
        UnitsProvider<Length> lengthProvider = myToolbox.getUnitsRegistry().getUnitsProvider(Length.class);
        lengthProvider.addListener(myLengthUnitsChangeListener);
        myPreferredLengthUnits = lengthProvider.getPreferredUnits();
        myToolbox.getUIRegistry().getToolbarComponentRegistry().registerToolbarComponent(ToolbarLocation.SOUTH, "ViewerPosition",
                getViewerPositionPanel(), 300, SeparatorLocation.BOTH);

        myTransformerHelper = new TransformerHelper(this, toolbox);
    }

    /**
     * Enable the display of the viewer position.
     */
    public synchronized void enableViewerPosition()
    {
        myViewerPositionEnabled = true;
    }

    @Override
    public String getDescription()
    {
        return "Viewer Transformer";
    }

    /**
     * Update all of the labels for the new viewer position.
     *
     * @param view The current viewer position.
     * @param type The type of view change which has occurred.
     */
    public synchronized void handleViewChanged(Viewer view, ViewChangeType type)
    {
        if (myViewerPositionEnabled)
        {
            if (myLabelLocation == null || type == ViewChangeType.WINDOW_RESIZE)
            {
                int width = myToolbox.getMapManager().getScreenViewer().getViewportWidth();
                myLabelLocation = new ScreenPosition(width / 2, -5);
            }
            myGeoPosition = myToolbox.getMapManager().getProjection().convertToPosition(view.getPosition().getLocation(),
                    Altitude.ReferenceLevel.ELLIPSOID);

            createViewPositionLabels();
        }
    }

    @Override
    public void setVisible(boolean visible)
    {
        if (visible)
        {
            publishScreenPositionLabel();
            myVPWindow.display();
        }
        else
        {
            if (myVPWindow != null)
            {
                myVPWindow.closeWindow();
                myVPWindow = null;
            }
        }
        getPreferences().putBoolean("viewer.visibility", visible, this);
    }

    @Override
    public boolean isVisible()
    {
        return getPreferences().getBoolean("viewer.visibility", false);
    }

    /**
     * Create a text field with the given number of columns.
     *
     * @param columns the number of columns to use to calculate the preferred
     *            width; if columns is set to zero, the preferred width will be
     *            whatever naturally results from the component implementation.
     * @param bgColor the background color
     * @return the newly created text field.
     */
    private JTextField createTextField(int columns, Color bgColor)
    {
        JTextField field = new JTextField(columns);
        field.setSelectionColor(field.getBackground());
        field.setBackground(bgColor);
        field.setBorder(null);
        field.setFont(ourFont);
        field.setEditable(false);
        return field;
    }

    /** Creates the view position labels. */
    private void createViewPositionLabels()
    {
        if (isOpen())
        {
            if (myGeoPosition == null)
            {
                EventQueueUtilities.invokeLater(() -> setText("", "", ""));
            }
            else
            {
                final double precision = 1000.;

                double pitch = Math.toDegrees(myToolbox.getMapManager().getStandardViewer().getPitch());
                pitch = Math.round(pitch * precision) / precision;
                Coordinates pitchAngle = Coordinates.create(DecimalDegrees.class, pitch);
                final String pitchText = pitchAngle.toShortLabelString(10, 3);

                Length elevation = Length.create(myPreferredLengthUnits,
                        myGeoPosition.getLatLonAlt().getAltitude().getMagnitude());
                final String altText = elevation.toShortLabelString(10, 0);

                double heading = Math.toDegrees(myToolbox.getMapManager().getStandardViewer().getHeading());
                heading = Math.round(heading * precision) / precision;
                Coordinates headingAngle = Coordinates.create(DecimalDegrees.class, heading);
                final String headingText = headingAngle.toShortLabelString(9, 3);

                EventQueueUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        setText(altText, pitchText, headingText);
                        publishScreenPositionLabel();
                    }
                });
            }
        }
    }

    /**
     * Creates the window.
     */
    private void createWindow()
    {
        ScreenBoundingBox scalebarLocation = getInitialLocation(150, 60, "viewer.location");
        myVPWindow = new ViewerPositionWindow(myTransformerHelper, scalebarLocation, ToolLocation.SOUTH,
                ZOrderRenderProperties.TOP_Z - 10);
        Color blackOpaque = new Color(Color.BLACK.getRed(), Color.BLACK.getGreen(), Color.BLACK.getBlue(), 200);
        myVPWindow.setBackgroundColor(blackOpaque);
        myVPWindow.init();
        myVPWindow.setMoveListener(this::handleWindowMove);
    }

    /**
     * Handles window movement.
     *
     * @param location the upper left location
     */
    private void handleWindowMove(ScreenPosition location)
    {
        getPreferences().putString("viewer.location", location.toSimpleString(), this);
    }

    /**
     * Gets the viewer position panel.
     *
     * @return the viewer position panel
     */
    private JPanel getViewerPositionPanel()
    {
        if (myViewerPositionPanel == null)
        {
            myViewerPositionPanel = new JPanel(new GridBagLayout());
            myViewerPositionPanel.setOpaque(false);

            // Have to convert to awt Color to look right
            Color bgColor = new Color(myViewerPositionPanel.getBackground().getRGB(), true);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 15, 0, 0);
            myPitchLabel = new JLabel("Pitch:");
            myViewerPositionPanel.add(myPitchLabel, gbc);

            gbc.gridx = 1;
            gbc.insets = new Insets(0, 0, 0, 0);
            myPitchField = createTextField(5, bgColor);
            myViewerPositionPanel.add(myPitchField, gbc);

            gbc.gridx = 2;
            gbc.insets = new Insets(0, 15, 0, 0);
            myViewerPositionPanel.add(new JLabel("Alt:"), gbc);

            gbc.gridx = 3;
            gbc.insets = new Insets(0, 0, 0, 0);
            myAltitudeField = createTextField(8, bgColor);
            myViewerPositionPanel.add(myAltitudeField, gbc);

            gbc.gridx = 4;
            gbc.insets = new Insets(0, 15, 0, 0);
            myViewerPositionPanel.add(new JLabel("Heading:"), gbc);

            gbc.gridx = 5;
            gbc.insets = new Insets(0, 0, 0, 0);
            myHeadingField = createTextField(6, bgColor);
            myViewerPositionPanel.add(myHeadingField, gbc);
        }
        return myViewerPositionPanel;
    }

    /**
     * Publish a label which shows the position of the cursor within the canvas.
     */
    private synchronized void publishScreenPositionLabel()
    {
        if (myVPWindow == null)
        {
            createWindow();
        }
        myVPWindow.getPitchLabel().replaceText("Pitch: " + myPitchField.getText());
        myVPWindow.getHeadingLabel().replaceText("Heading: " + myHeadingField.getText());
        myVPWindow.getAltLabel().replaceText("Alt: " + myAltitudeField.getText());
    }

    /**
     * Set the labels to the given values.
     *
     * @param altitude The new altitude.
     * @param pitch The new pitch.
     * @param heading The new heading.
     */
    private void setText(final String altitude, final String pitch, final String heading)
    {
        myAltitudeField.setText(altitude);
        myPitchField.setText(pitch);
        myHeadingField.setText(heading);
    }
}

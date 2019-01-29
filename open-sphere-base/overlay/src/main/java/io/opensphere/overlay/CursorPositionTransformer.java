package io.opensphere.overlay;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultKeyPressedBinding;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.control.ui.ToolbarManager.ToolbarLocation;
import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultLabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.LabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.Window.ToolLocation;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.mgrs.MGRSConverter;
import io.opensphere.core.mgrs.UTM;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.projection.MutableGeographicProjection;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.terrain.util.AbsoluteElevationProvider;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * Transformer for the cursor position overlay.
 */
final class CursorPositionTransformer extends AbstractOverlayTransformer
{
    /** The initial screen position for the time display label. */
    public static final ScreenPosition ourLocation = new ScreenPosition(40, 40);

    /** An MGRS converter. */
    private static final MGRSConverter MGRS_CONVERTER = new MGRSConverter();

    /** The Window. */
    private CursorPositionWindow myCPWindow;

    /** The set of items that could be displayed as canvas overlays. */
    private final List<String> myDisplayableSet = New.list();

    /** The last mouse point. */
    private Point myLastPoint;

    /** Flag indicating if the cursor position is to be displayed. */
    private boolean myMousePositionEnabled;

    /** The label which tells the screen position of the cursor. */
    private LabelGeometry myScreenPositionLabel;

    /** The Show cursor screen position. */
    private boolean myShowCursorScreenPosition;

    /** The tool box used by plugins to interact with the rest of the system. */
    private final Toolbox myToolbox;

    /** The Transformer helper. */
    private final TransformerHelper myTransformerHelper;

    /** The cursor position panel. */
    private final CursorPositionPanel myCursorPositionPanel;

    /**
     * Constructor.
     *
     * @param toolbox The tool box used by plugins to interact with the rest of
     *            the system.
     * @param preferences The preferences
     */
    public CursorPositionTransformer(Toolbox toolbox, Preferences preferences)
    {
        super(null, preferences);

        myCursorPositionPanel = new CursorPositionPanel(ourFont, toolbox.getUnitsRegistry());

        myDisplayableSet.add("MGRS Label");
        myDisplayableSet.add("Latitude Label");
        myDisplayableSet.add("Longitude Label");
        myDisplayableSet.add("Altitude Label");

        myToolbox = toolbox;

        myCursorPositionPopupManager = new CursorPositionPopupManager(myToolbox.getUIRegistry().getMainFrameProvider(),
                toolbox.getUnitsRegistry());
        toolbox.getControlRegistry().getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT)
                .addListener(myCursorPositionPopupManager.getListener(), new DefaultKeyPressedBinding(KeyEvent.VK_PERIOD));

        myToolbox.getUIRegistry().getToolbarComponentRegistry().registerToolbarComponent(ToolbarLocation.SOUTH, "CursorPosition",
                myCursorPositionPanel, 10000, SeparatorLocation.NONE);

        EventQueueUtilities.runOnEDT(() -> myToolbox.getUIRegistry().getMenuBarRegistry()
                .getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.DEBUG_MENU).add(getDebugMenu()));
        myTransformerHelper = new TransformerHelper(this, toolbox);
    }

    /**
     * Remove the screen position label.
     */
    public void clear()
    {
        setPositionLabel(null);
    }

    /**
     * Enable the mouse position label.
     */
    public void enableMousePosition()
    {
        myMousePositionEnabled = true;
    }

    @Override
    public String getDescription()
    {
        return "Cursor Transformer";
    }

    /**
     * Publish a label which shows the position of the cursor within the canvas.
     */
    public synchronized void publishScreenPositionLabel()
    {
        if (myCPWindow == null)
        {
            createWindow();
        }
        myCPWindow.getMGRSLabel().replaceText("MGRS: " + myCursorPositionPanel.getMGRSText());
        myCPWindow.getLatLabel().replaceText("Lat: " + myCursorPositionPanel.getLatText());
        myCPWindow.getLonLabel().replaceText("Lon: " + myCursorPositionPanel.getLonText());
        myCPWindow.getAltLabel().replaceText("Alt: " + myCursorPositionPanel.getAltText());
    }

    /**
     * Reset the position label using the last mouse position if there is one.
     */
    public void resetPositionLabel()
    {
        if (myLastPoint != null)
        {
            setPositionLabel(myLastPoint);
        }
    }

    @Override
    public void setVisible(boolean visible)
    {
        if (visible)
        {
            publishScreenPositionLabel();
            myCPWindow.display();
        }
        else
        {
            if (myCPWindow != null)
            {
                myCPWindow.closeWindow();
                myCPWindow = null;
            }
        }
        getPreferences().putBoolean("cursor.visibility", visible, this);
    }

    @Override
    public boolean isVisible()
    {
        return getPreferences().getBoolean("cursor.visibility", false);
    }

    /**
     * Set the screen position label.
     *
     * @param text1 The text of the first row label.
     * @param latLonAlt the loc map
     * @param hasElevationProvider the has elevation provider
     */
    public void setLabels(String text1, LatLonAlt latLonAlt, boolean hasElevationProvider)
    {
        if (isOpen())
        {
            myCursorPositionPanel.setLabels(text1, latLonAlt, hasElevationProvider);
            myCursorPositionPopupManager.setLocation(latLonAlt, hasElevationProvider);
            publishScreenPositionLabel();
        }
    }

    /**
     * Set the position label.
     *
     * @param point The current cursor position.
     */
    public void setPositionLabel(Point point)
    {
        if (point != null)
        {
            myLastPoint = point;
        }
        if (myShowCursorScreenPosition && point != null)
        {
            publishScreenPosition(point);
        }

        if (!myMousePositionEnabled)
        {
            return;
        }

        boolean hasElevationProvider = false;
        GeographicPosition position = null;
        if (point != null)
        {
            position = myToolbox.getMapManager().convertToPosition(new Vector2i(point), Altitude.ReferenceLevel.ELLIPSOID);
        }
        if (position == null)
        {
            myCursorPositionPanel.clearText();
        }
        else
        {
            String mgrs = MGRS_CONVERTER.createString(new UTM(position));
            if (position.getLatLonAlt().getLatD() < 87)
            {
                Projection proj = myToolbox.getMapManager().getRawProjection();
                if (proj instanceof MutableGeographicProjection)
                {
                    GeographicBody3D body = ((MutableGeographicProjection)proj).getModel().getCelestialBody();
                    AbsoluteElevationProvider elevProv = body.getElevationManager().getProviderForPosition(position);
                    double elevationM;
                    if (elevProv != null)
                    {
                        hasElevationProvider = true;
                        elevationM = elevProv.getElevationM(position, true);
                        position = new GeographicPosition(LatLonAlt.createFromDegreesMeters(position.getLatLonAlt().getLatD(),
                                position.getLatLonAlt().getLonD(), elevationM, ReferenceLevel.ELLIPSOID));
                    }
                }
            }
            setLabels(mgrs, position.getLatLonAlt(), hasElevationProvider);
        }
    }

    /** Manager for the cursor position popup. */
    private final CursorPositionPopupManager myCursorPositionPopupManager;

    /**
     * Creates the window.
     */
    private void createWindow()
    {
        ScreenBoundingBox scalebarLocation = getInitialLocation(170, 80, "cursor.location");
        myCPWindow = new CursorPositionWindow(myTransformerHelper, scalebarLocation, ToolLocation.SOUTH,
                ZOrderRenderProperties.TOP_Z - 10);
        Color blackOpaque = new Color(Color.BLACK.getRed(), Color.BLACK.getGreen(), Color.BLACK.getBlue(), 200);
        myCPWindow.setBackgroundColor(blackOpaque);
        myCPWindow.init();
        myCPWindow.setMoveListener(this::handleWindowMove);
    }

    /**
     * Handles window movement.
     *
     * @param location the upper left location
     */
    private void handleWindowMove(ScreenPosition location)
    {
        getPreferences().putString("cursor.location", location.toSimpleString(), this);
    }

    /**
     * The the debug options for cursor labels.
     *
     * @return The menu which contains the debug options.
     */
    private JMenuItem getDebugMenu()
    {
        JMenu menu = new JMenu("Cursor");
        final JMenuItem cursorItem = new JCheckBoxMenuItem("Show cursor screen position");
        cursorItem.addActionListener(e ->
        {
            myShowCursorScreenPosition = cursorItem.isSelected();
            if (!myShowCursorScreenPosition && myScreenPositionLabel != null)
            {
                publishGeometries(Collections.<LabelGeometry>emptyList(), Collections.singletonList(myScreenPositionLabel));
            }
        });
        menu.add(cursorItem);

        return menu;
    }

    /**
     * Publish a label for the screen position of the cursor.
     *
     * @param point The current location of the cursor.
     */
    private void publishScreenPosition(Point point)
    {
        StringBuilder labelText = new StringBuilder();
        labelText.append("(" + point.x + ", " + point.y + ")");

        Collection<LabelGeometry> removes = myScreenPositionLabel == null ? Collections.<LabelGeometry>emptyList()
                : Collections.singletonList(myScreenPositionLabel);

        LabelGeometry.Builder<ScreenPosition> builder = new LabelGeometry.Builder<>();
        builder.setPosition(new ScreenPosition(20, 20));
        builder.setText(labelText.toString());
        builder.setRapidUpdate(true);

        LabelRenderProperties renderProperties = new DefaultLabelRenderProperties(ZOrderRenderProperties.TOP_Z, true, false);
        renderProperties.setColor(Color.GREEN);
        myScreenPositionLabel = new LabelGeometry(builder, renderProperties, null);

        publishGeometries(Collections.singletonList(myScreenPositionLabel), removes);
    }
}

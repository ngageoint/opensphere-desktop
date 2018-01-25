package io.opensphere.overlay;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Calendar;
import java.util.TimeZone;

import javax.swing.JLabel;
import javax.swing.JPanel;

import io.opensphere.core.TimeManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.control.ui.ToolbarManager.ToolbarLocation;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultLabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.LabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.Window.ToolLocation;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.preferences.Preferences;

/**
 * Transformer for the time display overlay.
 */
final class TimeDisplayTransformer extends AbstractOverlayTransformer
{
//    /** The time display label. */
//    private LabelGeometry myLabel;

    /** The initial screen position for the time display label. */
    public static final ScreenPosition ourLocation = new ScreenPosition(40, 15);

    /** Flag indicating if the time display is to be displayed. */
    private boolean myTimeDisplayEnabled;

    /** The time manager. */
    private final TimeManager myTimeManager;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /** The Time display panel. */
    private JPanel myTimeDisplayPanel;

    /** The Time display label. */
    private JLabel myTimeDisplayLabel;

    /** The label builder. */
    private final LabelGeometry.Builder<ScreenPosition> myBuilder;

    /** The label render properties. */
    private final LabelRenderProperties myRenderProperties;

    /** The Window. */
    private TimePositionWindow myTPWindow;

    /** The Transformer helper. */
    private final TransformerHelper myTransformerHelper;

    /**
     * Instantiates a new time display transformer.
     *
     * @param toolbox the toolbox
     * @param preferences The preferences
     */
    public TimeDisplayTransformer(Toolbox toolbox, Preferences preferences)
    {
        super((DataRegistry)null, preferences);
        myToolbox = toolbox;
        myTimeManager = toolbox.getTimeManager();
        myToolbox.getUIRegistry().getToolbarComponentRegistry().registerToolbarComponent(ToolbarLocation.SOUTH, "TimeDisplay",
                getTimeDisplayPanel(), 0, SeparatorLocation.RIGHT);

        myRenderProperties = new DefaultLabelRenderProperties(ZOrderRenderProperties.TOP_Z, true, false);
        myRenderProperties.setColor(Color.GREEN);
        myRenderProperties.setShadowColor(Color.RED);

        myBuilder = new LabelGeometry.Builder<ScreenPosition>();
        myBuilder.setHorizontalAlignment(0f);
        myBuilder.setVerticalAlignment(0f);
        String font = Font.MONOSPACED + " PLAIN 12";
        myBuilder.setFont(font);

        myTransformerHelper = new TransformerHelper(this, toolbox);
    }

    /**
     * Remove the screen position label.
     */
    public void clear()
    {
        if (isOpen())
        {
            getTimeDisplayLabel().setText("");
        }
    }

    /**
     * Enable the time display label.
     */
    public void enableTimeDisplay()
    {
        myTimeDisplayEnabled = true;
    }

    @Override
    public String getDescription()
    {
        return "Time Transformer";
    }

    /**
     * Gets the time display panel.
     *
     * @return the time display panel
     */
    public JPanel getTimeDisplayPanel()
    {
        if (myTimeDisplayPanel == null)
        {
            myTimeDisplayPanel = new JPanel(new GridBagLayout());
            myTimeDisplayPanel.setOpaque(false);
            // Make sure the size for this panel is assigned so the panel does
            // not change size when no time is active.
            myTimeDisplayPanel.setSize(325, 22);
            myTimeDisplayPanel.setPreferredSize(myTimeDisplayPanel.getSize());
            myTimeDisplayPanel.setMinimumSize(myTimeDisplayPanel.getSize());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 15, 0, 15);
            myTimeDisplayPanel.add(getTimeDisplayLabel(), gbc);
        }
        return myTimeDisplayPanel;
    }

    @Override
    public void open()
    {
        super.open();
        TimeSpan timeSpan = myTimeManager.getPrimaryActiveTimeSpans().getExtent();
        if (timeSpan != null && myTimeDisplayEnabled)
        {
            setTimeDisplayLabel();
        }
    }

    /**
     * Publish a label which shows the position of the cursor within the canvas.
     */
    public synchronized void publishScreenPositionLabel()
    {
        if (myTPWindow == null)
        {
            createWindow();
        }
        myTPWindow.getTimeLabel().replaceText(myTimeDisplayLabel.getText());
    }

    @Override
    public void setVisible(boolean visible)
    {
        if (visible)
        {
            publishScreenPositionLabel();
            myTPWindow.display();
        }
        else
        {
            if (myTPWindow != null)
            {
                myTPWindow.closeWindow();
                myTPWindow = null;
            }
        }
        getPreferences().putBoolean("time.visibility", visible, this);
    }

    @Override
    public boolean isVisible()
    {
        return getPreferences().getBoolean("time.visibility", false);
    }

    /**
     * Set the position label.
     */
    protected void setTimeDisplayLabel()
    {
        if (myTimeDisplayEnabled)
        {
            TimeSpan timeSpan = myTimeManager.getPrimaryActiveTimeSpans().getExtent();
            String text;
            if (timeSpan.equals(TimeSpan.ZERO))
            {
                text = "No time active";
                getTimeDisplayLabel().setText(text);
            }
            else
            {
                Calendar cal = Calendar.getInstance();
                text = timeSpan.isUnboundedStart() ? "beginning of time"
                        : timeSpan.getStartInstant().toDisplayString() + " to "
                                + (timeSpan.isUnboundedEnd() ? "end of time" : timeSpan.getEndInstant().toDisplayString()) + " "
                                + cal.getTimeZone().getDisplayName(cal.get(Calendar.DST_OFFSET) != 0, TimeZone.SHORT);
            }
//            String font = Font.decode(null).toString();
//            setLabel(text, font);
            getTimeDisplayLabel().setText(text);
            publishScreenPositionLabel();
        }
    }

    /**
     * Creates the window.
     */
    private void createWindow()
    {
        ScreenBoundingBox scalebarLocation = getInitialLocation(300, 20, "time.location");
        myTPWindow = new TimePositionWindow(myTransformerHelper, scalebarLocation, ToolLocation.SOUTH,
                ZOrderRenderProperties.TOP_Z - 10);
        Color blackOpaque = new Color(Color.BLACK.getRed(), Color.BLACK.getGreen(), Color.BLACK.getBlue(), 200);
        myTPWindow.setBackgroundColor(blackOpaque);
        myTPWindow.init();
        myTPWindow.setMoveListener(this::handleWindowMove);
    }

    /**
     * Handles window movement.
     *
     * @param location the upper left location
     */
    private void handleWindowMove(ScreenPosition location)
    {
        getPreferences().putString("time.location", location.toSimpleString(), this);
    }

    /**
     * Gets the time display label.
     *
     * @return the time display label
     */
    private JLabel getTimeDisplayLabel()
    {
        if (myTimeDisplayLabel == null)
        {
            myTimeDisplayLabel = new JLabel();
            myTimeDisplayLabel.setFont(ourFont);
        }
        return myTimeDisplayLabel;
    }
}

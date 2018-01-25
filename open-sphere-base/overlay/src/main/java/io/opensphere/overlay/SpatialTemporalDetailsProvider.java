package io.opensphere.overlay;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import io.opensphere.core.Toolbox;
import io.opensphere.core.options.impl.AbstractPreferencesOptionsProvider;
import io.opensphere.core.options.impl.OptionsPanel;
import io.opensphere.core.util.Showable;

/**
 * The Class SpatialTemporalDetailsProvider.
 */
public class SpatialTemporalDetailsProvider extends AbstractPreferencesOptionsProvider
{
    /** The topic for default settings. */
    public static final String DEFAULTS_TOPIC = "Info Overlays";

    /** The Cursor position transformer. */
    private final CursorPositionTransformer myCursorPositionTransformer;

    /** The Panel. */
    private JPanel myMainPanel;

    /** The Time display transformer. */
    private final TimeDisplayTransformer myTimeDisplayTransformer;

    /** The Viewer position transformer. */
    private final ViewerPositionTransformer myViewerPositionTransformer;

    /**
     * Instantiates a new spatial temporal details provider.
     *
     * @param toolbox the toolbox
     * @param defaultsTopic the defaults topic
     * @param cursorPositionTransformer the cursor position transformer
     * @param viewerPositionTransformer the viewer position transformer
     * @param timeDisplayTransformer the time display transformer
     */
    public SpatialTemporalDetailsProvider(Toolbox toolbox, String defaultsTopic,
            CursorPositionTransformer cursorPositionTransformer, ViewerPositionTransformer viewerPositionTransformer,
            TimeDisplayTransformer timeDisplayTransformer)
    {
        super(toolbox.getPreferencesRegistry(), DEFAULTS_TOPIC);
        myCursorPositionTransformer = cursorPositionTransformer;
        myViewerPositionTransformer = viewerPositionTransformer;
        myTimeDisplayTransformer = timeDisplayTransformer;
    }

    @Override
    public void applyChanges()
    {
    }

    @Override
    public JPanel getOptionsPanel()
    {
        if (myMainPanel == null)
        {
            myMainPanel = new OptionsPanel(createCheckboxPanel());
        }
        return myMainPanel;
    }

    @Override
    public void restoreDefaults()
    {
    }

    @Override
    public boolean usesApply()
    {
        return false;
    }

    @Override
    public boolean usesRestore()
    {
        return false;
    }

    /**
     * Creates the checkbox panel.
     *
     * @return the checkbox panel
     */
    private JPanel createCheckboxPanel()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(createCheckBox("Time", myTimeDisplayTransformer));
        panel.add(createCheckBox("View", myViewerPositionTransformer));
        panel.add(createCheckBox("Cursor", myCursorPositionTransformer));
        return panel;
    }

    /**
     * Creates a check box.
     *
     * @param text the text
     * @param showable the showable item being controlled
     * @return the check box
     */
    private JCheckBox createCheckBox(String text, Showable showable)
    {
        JCheckBox checkbox = new JCheckBox(text);
        checkbox.setToolTipText("Show / hide the " + text + " overlay");
        checkbox.setFocusPainted(false);
        checkbox.setSelected(showable.isVisible());
        checkbox.addActionListener(e -> showable.setVisible(((JCheckBox)e.getSource()).isSelected()));
        return checkbox;
    }
}

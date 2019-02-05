package io.opensphere.core.util.swing.tree;

import java.util.Enumeration;

import javax.swing.Icon;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import com.jidesoft.swing.TristateCheckBox;

/**
 * An extended checkbox that supports a button mode.
 */
public class TristateCheckBoxWithButtonModeSupport extends TristateCheckBox
{
    /** The logger used to capture output from this class. */
    private static final Logger LOG = Logger.getLogger(TristateCheckBoxWithButtonModeSupport.class);

    /**
     * The unique identifier used for serialization.
     */
    private static final long serialVersionUID = -562689637413729583L;

    /**
     * A flag used to track if the button is configured in button mode.
     */
    private boolean myButtonModeEnabled;

    /**
     * Creates an initially unselected check box.
     */
    public TristateCheckBoxWithButtonModeSupport()
    {
        super();
    }

    /**
     * Creates an initially unselected check box with the specified text.
     *
     * @param pText the text of the check box.
     */
    public TristateCheckBoxWithButtonModeSupport(String pText)
    {
        super(pText);
    }

    /**
     * Creates an initially unselected check box with the specified text and
     * icon.
     *
     * @param pText the text of the check box.
     * @param pIcon the Icon image to display
     */
    public TristateCheckBoxWithButtonModeSupport(String pText, Icon pIcon)
    {
        super(pText, pIcon);
    }

    protected void adjustMixedIcon()
    {
        setIcon(UIManager.getIcon("OSTristateCheckBox.icon"));
    }

    /**
     * Sets the value of the {@link #myButtonModeEnabled} field.
     *
     * @param pButtonModeEnabled the value to store in the
     *            {@link #myButtonModeEnabled} field.
     */
    public void setButtonModeEnabled(boolean pButtonModeEnabled)
    {
        myButtonModeEnabled = pButtonModeEnabled;
    }

    /**
     * Gets the value of the {@link #myButtonModeEnabled} field.
     *
     * @return the value stored in the {@link #myButtonModeEnabled} field.
     */
    public boolean isButtonModeEnabled()
    {
        return myButtonModeEnabled;
    }
}

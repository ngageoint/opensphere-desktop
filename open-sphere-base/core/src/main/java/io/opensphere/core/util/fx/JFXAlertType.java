package io.opensphere.core.util.fx;

import javafx.scene.control.ButtonType;

/**
 * An enumeration containing the available, pre-built alert types that the
 * {@link JFXAlert} class can use to pre-populate various properties.
 * <p>
 * This enum previously used locale-specific title strings, but now just uses
 * the English ones.
 */
public enum JFXAlertType
{
    /**
     * The NONE alert type has the effect of not setting any default properties
     * in the Alert.
     */
    NONE("", "", ""),

    /**
     * The INFORMATION alert type configures the Alert dialog to appear in a way
     * that suggests the content of the dialog is informing the user of a piece
     * of information. This includes an 'information' image, an appropriate
     * title and header, and just an OK button for the user to click on to
     * dismiss the dialog.
     */
    INFORMATION("Message", "Message", "information", ButtonType.OK),

    /**
     * The WARNING alert type configures the Alert dialog to appear in a way
     * that suggests the content of the dialog is warning the user about some
     * fact or action. This includes a 'warning' image, an appropriate title and
     * header, and just an OK button for the user to click on to dismiss the
     * dialog.
     */
    WARNING("Warning", "Warning", "warning", ButtonType.OK),

    /**
     * The CONFIRMATION alert type configures the Alert dialog to appear in a
     * way that suggests the content of the dialog is seeking confirmation from
     * the user. This includes a 'confirmation' image, an appropriate title and
     * header, and both OK and Cancel buttons for the user to click on to
     * dismiss the dialog.
     */
    CONFIRMATION("Confirmation", "Confirmation", "confirmation", ButtonType.OK, ButtonType.CANCEL),

    /**
     * The ERROR alert type configures the Alert dialog to appear in a way that
     * suggests that something has gone wrong. This includes an 'error' image,
     * an appropriate title and header, and just an OK button for the user to
     * click on to dismiss the dialog.
     */
    ERROR("Error", "Error", "error", ButtonType.OK);

    /** The default buttons shown for the {@link JFXAlertType}. */
    private final ButtonType[] myDefaultButtons;

    /** The default title shown for the {@link JFXAlertType}. */
    private final String myDefaultTitle;

    /** The default message shown for the {@link JFXAlertType}. */
    private final String myDefaultMessage;

    /** The style class associated with the alert type. */
    private final String myStyleClass;

    /**
     * Creates a new alert type, configured with the supplied parameters.
     *
     * @param defaultTitle The default title shown for the {@link JFXAlertType}.
     * @param defaultMessage The default message shown for the
     *            {@link JFXAlertType}.
     * @param styleClass The style class associated with the alert type.
     * @param defaultButtons The default buttons shown for the
     *            {@link JFXAlertType}.
     */
    private JFXAlertType(String defaultTitle, String defaultMessage, String styleClass, ButtonType... defaultButtons)
    {
        myDefaultTitle = defaultTitle;
        myDefaultMessage = defaultMessage;
        myStyleClass = styleClass;
        myDefaultButtons = defaultButtons;
    }

    /**
     * Gets the value of the {@link #myDefaultTitle} field.
     *
     * @return the value stored in the {@link #myDefaultTitle} field.
     */
    public String getDefaultTitle()
    {
        return myDefaultTitle;
    }

    /**
     * Gets the value of the {@link #myDefaultMessage} field.
     *
     * @return the value stored in the {@link #myDefaultMessage} field.
     */
    public String getDefaultMessage()
    {
        return myDefaultMessage;
    }

    /**
     * Gets the value of the {@link #myStyleClass} field.
     *
     * @return the value stored in the {@link #myStyleClass} field.
     */
    public String getStyleClass()
    {
        return myStyleClass;
    }

    /**
     * Gets the value of the {@link #myDefaultButtons} field.
     *
     * @return the value stored in the {@link #myDefaultButtons} field.
     */
    public ButtonType[] getDefaultButtons()
    {
        return myDefaultButtons;
    }
}

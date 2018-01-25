package io.opensphere.core.util.swing;

/**
 * Interface for a customizable buttons.
 */
public interface CustomizableButton
{
    /**
     * Sets whether to auto-resize when text/icon is changed.
     *
     * @param autoResize whether to auto-resize when text/icon is changed
     */
    void setAutoResize(boolean autoResize);

    /**
     * Sets the default icon.
     *
     * @param defaultIcon the default icon
     */
    void setIcon(String defaultIcon);

    /**
     * Sets whether the icon is to be painted.
     *
     * @param iconPainted whether the icon is to be painted
     */
    void setIconPainted(boolean iconPainted);

    /**
     * Sets the pressed icon.
     *
     * @param pressedIcon the pressed icon
     */
    void setPressedIcon(String pressedIcon);

    /**
     * Sets the rollover icon.
     *
     * @param rolloverIcon the rollover icon
     */
    void setRolloverIcon(String rolloverIcon);

    /**
     * Sets the selected icon.
     *
     * @param selectedIcon the selected icon
     */
    void setSelectedIcon(String selectedIcon);

    /**
     * Sets whether the text is to be painted.
     *
     * @param textPainted whether the text is to be painted
     */
    void setTextPainted(boolean textPainted);
}

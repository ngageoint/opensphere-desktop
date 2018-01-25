package io.opensphere.core.util.fx;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import io.opensphere.core.util.AwesomeIcon;

/**
 * A container in which the icons from the font-awesome package are presented
 * for use.
 */
public final class FxAwesomeIcons
{
    /**
     * Private constructor to prevent instantiation.
     */
    private FxAwesomeIcons()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not supported.");
    }

    /**
     * Creates a button using the named icon as its content.
     *
     * @param pIcon the name of the icon to use as the content of the button.
     * @return a new button using the named icon.
     */
    public static Button createIconButton(AwesomeIcon pIcon)
    {
        return createIconButton(pIcon, "", 12);
    }

    /**
     * Creates a button using the named icon and supplied text as its content.
     *
     * @param pIcon the name of the icon to use as the content of the button.
     * @param pText the text to use in the button, will be displayed after the
     *            icon.
     * @return a new button using the named icon.
     */
    public static Button createIconButton(AwesomeIcon pIcon, String pText)
    {
        return createIconButton(pIcon, pText, 12);
    }

    /**
     * Creates a button using the named icon as its content.
     *
     * @param pIcon the name of the icon to use as the content of the button.
     * @param pText the text to use in the button, will be displayed after the
     *            icon.
     * @param pSize the size of the icon in the button, expressed as a font
     *            size.
     * @return a new button using the named icon.
     */
    public static Button createIconButton(AwesomeIcon pIcon, String pText, int pSize)
    {
        return createIconButton(pIcon, "icons", pText, pSize);
    }

    /**
     * Creates a button using the named icon as its content.
     *
     * @param pIcon the name of the icon to use as the content of the button.
     * @param pStyleName the name of the style to apply to the button.
     * @param pText the text to use in the button, will be displayed after the
     *            icon.
     * @param pSize the size of the icon in the button, expressed as a font
     *            size.
     * @return a new button using the named icon.
     */
    public static Button createIconButton(AwesomeIcon pIcon, String pStyleName, String pText, int pSize)
    {
        Label icon = createIconLabel(pIcon, 12);
        icon.setStyle("-fx-font-size: " + pSize + "px;");

        Button button = new Button(pText);
        button.setGraphic(icon);
        button.getStyleClass().add(pStyleName);

        return button;
    }

    /**
     * Creates a {@link Label} containing the named icon, at the default font
     * size.
     *
     * @param pIcon the name of the icon to render in the label.
     * @return a new {@link Label} containing the named icon.
     */
    public static Label createIconLabel(AwesomeIcon pIcon)
    {
        return createIconLabel(pIcon, 12);
    }

    /**
     * Creates a {@link Label} containing the named icon, at the supplied font
     * size.
     *
     * @param pIcon the name of the icon to render in the label.
     * @param pSize the size of the icon in the Label, expressed as a font size.
     * @return a new {@link Label} containing the named icon.
     */
    public static Label createIconLabel(AwesomeIcon pIcon, int pSize)
    {
        Label label = new Label(pIcon.getFontCode());
        label.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        label.getStyleClass().add("icons");
        label.setStyle("-fx-font-size: " + pSize + "px;");
        return label;
    }

    /**
     * Creates a {@link Label} containing the named icon, at the supplied font
     * size using the supplied color.
     *
     * @param icon the name of the icon to render in the label.
     * @param color the color with which to render the icon.
     * @param size the size of the icon in the Label, expressed as a font size.
     * @return a new {@link Label} containing the named icon.
     */
    public static Label createIconLabel(AwesomeIcon icon, Color color, int size)
    {
        Label label = new Label(icon.getFontCode());
        label.setTextFill(color);
        label.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        label.getStyleClass().add("icons");
        label.setStyle("-fx-font-size: " + size + "px;");

        return label;
    }
}

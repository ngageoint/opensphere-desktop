package io.opensphere.core.util.fx;

import java.util.Arrays;
import java.util.stream.Collectors;

import io.opensphere.core.util.FontIconEnum;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * A container in which the icons from the font-awesome package are presented
 * for use.
 */
public final class FxIcons
{
    /**
     * Private constructor to prevent instantiation.
     */
    private FxIcons()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not supported.");
    }

    /**
     * Creates a button using the named icon as its content.
     *
     * @param pIcon the name of the icon to use as the content of the button.
     * @return a new button using the named icon.
     */
    public static Button createIconButton(final FontIconEnum pIcon)
    {
        return createIconButton(pIcon, "");
    }

    /**
     * Creates a button using the named icon and supplied text as its content.
     *
     * @param pIcon the name of the icon to use as the content of the button.
     * @param pText the text to use in the button, will be displayed after the
     *            icon.
     * @return a new button using the named icon.
     */
    public static Button createIconButton(final FontIconEnum pIcon, final String pText)
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
    public static Button createIconButton(final FontIconEnum pIcon, final String pText, final int pSize)
    {
        return createIconButton(pIcon, pText, pSize, "icons", "icons-" + pIcon.getFont().getFontName());
    }

    /**
     * Creates a button using the named icon as its content.
     *
     * @param pIcon the name of the icon to use as the content of the button.
     * @param pText the text to use in the button, will be displayed after the
     *            icon.
     * @param pSize the size of the icon in the button, expressed as a font
     *            size.
     * @param pStyleNames the names of the styles to apply to the button.
     * @return a new button using the named icon.
     */
    public static Button createIconButton(final FontIconEnum pIcon, final String pText, final int pSize,
            final String... pStyleNames)
    {
        final Label icon = createIconLabel(pIcon, 12);
        icon.setStyle("-fx-font-size: " + pSize + "px;");

        final Button button = new Button(pText);
        button.setGraphic(icon);
        button.getStyleClass().addAll(pStyleNames);

        return button;
    }

    /**
     * Creates a {@link Label} containing the named icon, at the default font
     * size.
     *
     * @param pIcon the name of the icon to render in the label.
     * @return a new {@link Label} containing the named icon.
     */
    public static Label createIconLabel(final FontIconEnum pIcon)
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
    public static Label createIconLabel(final FontIconEnum pIcon, final int pSize)
    {
        final Label label = new Label(pIcon.getFontCode());
        label.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        label.getStyleClass().addAll("icons", "icons-" + pIcon.getFont().getFontName());
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
    public static Label createIconLabel(final FontIconEnum icon, final Color color, final int size)
    {
        final Label label = new Label(icon.getFontCode());
        label.setTextFill(color);
        label.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        final String fontName = "icons-" + icon.getFont().getFontName().replaceAll("\\s", "-");
        label.getStyleClass().addAll("icons", fontName);
        final String style = "-fx-font-size: " + size + "px; -fx-text-fill: rgb(" + (int)(color.getRed() * 255) + ","
                + (int)(color.getGreen() * 255) + "," + (int)(color.getBlue() * 255) + ");";
        label.setStyle(style);

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
    public static Label createClearIcon(final FontIconEnum icon, final Color color, final int size)
    {
        final Label label = new Label(icon.getFontCode());
        label.setTextFill(color);
        label.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        final String fontName = "icons-" + icon.getFont().getFontName().replaceAll("\\s", "-");
        label.getStyleClass().addAll(fontName);
        final String style = "-fx-font-size: " + size + "px; -fx-text-fill: rgb(" + (int)(color.getRed() * 255) + ","
                + (int)(color.getGreen() * 255) + "," + (int)(color.getBlue() * 255) + ");";
        label.setStyle(style);

        return label;
    }

    /**
     * Creates a {@link Label} containing the named icon, at the supplied font
     * size using the supplied color.
     *
     * @param color the color with which to render the icon.
     * @param size the size of the icon in the Label, expressed as a font size.
     * @param icons the icons to render in the label.
     * @return a new {@link Label} containing the named icon.
     */
    public static Label createClearIcon(final Color color, final int size, final FontIconEnum... icons)
    {
        Label label;
        if (icons.length > 0)
        {
            label = new Label(Arrays.stream(icons).map(i -> i.getFontCode()).collect(Collectors.joining(" ")));
            label.setTextFill(color);
            label.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            final String fontName = "icons-" + icons[0].getFont().getFontName().replaceAll("\\s", "-");
            label.getStyleClass().addAll(fontName);
            final String style = "-fx-font-size: " + size + "px; -fx-text-fill: rgb(" + (int)(color.getRed() * 255) + ","
                    + (int)(color.getGreen() * 255) + "," + (int)(color.getBlue() * 255) + ");";
            label.setStyle(style);
        }
        else
        {
            label = new Label();
        }

        return label;
    }
}

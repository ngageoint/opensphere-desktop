package io.opensphere.core.util.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.UnexpectedEnumException;

/**
 * Icon utilities.
 */
public final class IconUtil
{
    /** Default icon foreground color. */
    public static final Color DEFAULT_ICON_FOREGROUND = new Color(97, 169, 220);

    /** Icon selection foreground. */
    public static final Color ICON_SELECTION_FOREGROUND = new Color(255, 244, 89);

    /** Icon type to icon map. */
    private static Map<IconType, String> ourTypeToResourceMap;

    static
    {
        Map<IconType, String> map = New.map();
        map.put(IconType.AIRPLANE, "/images/airplane.png");
        map.put(IconType.BINOCULARS, "/images/binoculars2.png");
        map.put(IconType.BUBBLE, "/images/bubble2.png");
        map.put(IconType.BUBBLE_FILLED, "/images/bubble.png");
        map.put(IconType.BULLS_EYE, "/images/bullseye.png");
        map.put(IconType.BUG, "/images/bug.png");
        map.put(IconType.CALENDAR, "/images/defaultCalendar.png");
        map.put(IconType.CAMERA, "/images/screencapture.png");
        map.put(IconType.CANCEL, "/images/cancel-circle.png");
        map.put(IconType.CLOCK, "/images/clock.png");
        map.put(IconType.CLOSE, "/images/close.png");
        map.put(IconType.COG, "/images/cog.png");
        map.put(IconType.COGS, "/images/cogs.png");
        map.put(IconType.COPY, "/images/copy.png");
        map.put(IconType.COLLAPSE, "/images/collapse.png");
        map.put(IconType.DELETE, "/images/remove.png");
        map.put(IconType.DISK, "/images/disk.png");
        map.put(IconType.DOWNLOAD, "/images/cloud-download.png");
        map.put(IconType.UPLOAD, "/images/cloud-upload.png");
        map.put(IconType.EDIT, "/images/pencil.png");
        map.put(IconType.EXPAND, "/images/expand3.png");
        map.put(IconType.EXPORT, "/images/download3.png");
        map.put(IconType.FILE, "/images/file.png");
        map.put(IconType.FILTER, "/images/filter2.png");
        map.put(IconType.LOCK, "/images/lock3.png");
        map.put(IconType.UNLOCK, "/images/unlocked2.png");
        map.put(IconType.MENU, "/images/menu-arrow.png");
        map.put(IconType.MOVE_UP, "/images/move-up.png");
        map.put(IconType.MOVE_DOWN, "/images/move-down.png");
        map.put(IconType.NEXT, "/images/next.png");
        map.put(IconType.PAUSE, "/images/pause2.png");
        map.put(IconType.PLAY, "/images/play3.png");
        map.put(IconType.PREVIOUS, "/images/previous.png");
        map.put(IconType.STOP, "/images/stop2.png");
        map.put(IconType.FIRST, "/images/first.png");
        map.put(IconType.LAST, "/images/last.png");
        map.put(IconType.FUTURE10, "/images/future10.png");
        map.put(IconType.FUTURE30, "/images/future30.png");
        map.put(IconType.HISTORY10, "/images/history10.png");
        map.put(IconType.HISTORY30, "/images/history30.png");
        map.put(IconType.IMPORT, "/images/upload3.png");
        map.put(IconType.NOTIFICATION, "/images/notification.png");
        map.put(IconType.OPEN, "/images/folder-open.png");
        map.put(IconType.PLUS, "/images/plus.png");
        map.put(IconType.QUESTION, "/images/question.png");
        map.put(IconType.RECORD, "/images/record.png");
        map.put(IconType.RELOAD, "/images/spinner6.png");
        map.put(IconType.SCREEN, "/images/screen.png");
        map.put(IconType.SCREEN_ON_EARTH, "/images/screenonearth.png");
        map.put(IconType.STAR, "/images/star3.png");
        map.put(IconType.STATS, "/images/stats.png");
        map.put(IconType.STACK, "/images/stack.png");
        map.put(IconType.STORAGE, "/images/storage.png");
        map.put(IconType.SNAPTO, "/images/arrows-in-box.png");
        map.put(IconType.TAG, "/images/tag.png");
        map.put(IconType.TRIANGLE, "/images/triangle.png");
        map.put(IconType.VIEW, "/images/file.png");
        map.put(IconType.AIRPLANE_ON_SCREEN_ON_EARTH, "/images/airplaneonscreenonearth.png");
        ourTypeToResourceMap = New.unmodifiableMap(map);
    }

    /**
     * Mixes a color into an icon using AlphaComposite. This is helpful if you
     * are using a base gray-scale icon and want to color it differently for
     * status purposes.
     *
     * Returns an new ImageIcon that matches the size and content of the base
     * icon with the specified color mixed in.
     *
     * @param baseIcon the base icon
     * @param mixColor the type color
     * @return the image icon
     */
    public static ImageIcon createIconWithMixInColor(ImageIcon baseIcon, Color mixColor)
    {
        if (baseIcon == null || mixColor == null)
        {
            return baseIcon;
        }

        BufferedImage bi = new BufferedImage(baseIcon.getIconWidth(), baseIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        ImageIcon mixedIcon = new ImageIcon();
        mixedIcon.setImage(bi);
        Graphics g = mixedIcon.getImage().getGraphics();
        g.setColor(mixColor);
        ((Graphics2D)g).setComposite(AlphaComposite.SrcOver);
        g.fillRect(0, 0, mixedIcon.getIconWidth(), mixedIcon.getIconHeight());
        ((Graphics2D)g).setComposite(AlphaComposite.DstIn);
        g.drawImage(baseIcon.getImage(), 0, 0, baseIcon.getImageObserver());
        return mixedIcon;
    }

    /**
     * Color the non-transparent pixels of the input icon using the default
     * color and apply the normal shading.
     *
     * @param iconType The icon type.
     * @return The colorized icon.
     * @throws IconRuntimeException If the icon could not be loaded.
     */
    public static Icon getNormalIcon(IconType iconType)
    {
        return getColorizedIcon(iconType, IconStyle.NORMAL, DEFAULT_ICON_FOREGROUND);
    }

    /**
     * Color the non-transparent pixels of the input icon using the default
     * color.
     *
     * @param iconType The icon type.
     * @param iconSize The icon size.
     * @return The colorized icon.
     * @throws IconRuntimeException If the icon could not be loaded.
     */
    public static Icon getColorizedIcon(IconType iconType, int iconSize)
    {
        return new ImageIcon(getColorizedIcon(iconType, DEFAULT_ICON_FOREGROUND).getImage().getScaledInstance(iconSize, iconSize,
                Image.SCALE_SMOOTH));
    }

    /**
     * Color the non-transparent pixels of the input icon using the given color.
     *
     * @param iconType The icon type.
     * @param color The color.
     * @return The colorized icon.
     * @throws IconRuntimeException If the icon could not be loaded.
     */
    public static ImageIcon getColorizedIcon(IconType iconType, Color color) throws IconRuntimeException
    {
        return getColorizedIcon(getIcon(iconType), color);
    }

    /**
     * Color the non-transparent pixels of the input icon using the default
     * color.
     *
     * @param icon The input icon.
     * @return The colorized icon.
     */
    public static ImageIcon getNormalIcon(ImageIcon icon)
    {
        return getColorizedIcon(icon, IconStyle.NORMAL, DEFAULT_ICON_FOREGROUND);
    }

    /**
     * Color the non-transparent pixels of the input icon using the given color.
     *
     * @param icon The input icon.
     * @param color The color.
     * @return The colorized icon.
     */
    public static ImageIcon getColorizedIcon(ImageIcon icon, Color color)
    {
        return getColorizedIcon(icon, IconStyle.FLAT, color);
    }

    /**
     * Color the non-transparent pixels of the input icon using the given color
     * across a gradient. The color will be adjusted according to the style.
     *
     * @param icon The input icon.
     * @param style The style to use for the icon.
     * @param color The color.
     * @return The colorized icon.
     */
    public static ImageIcon getColorizedIcon(ImageIcon icon, IconStyle style, Color color)
    {
        ImageIcon mixIcon = new ImageIcon();
        mixIcon.setImage(getColorizedImage(icon, style, color));
        return mixIcon;
    }

    /**
     * Get an image icon using a system resource and color the non-transparent
     * pixels using the default color.
     *
     * @param resource The name of the resource.
     * @return The image icon.
     * @throws IconRuntimeException If the resource could not be loaded.
     */
    public static ImageIcon getNormalIcon(String resource) throws IconRuntimeException
    {
        return getNormalIcon(getIcon(resource));
    }

    /**
     * Get an image icon using a system resource and color the non-transparent
     * pixels using the given color.
     *
     * @param resource The name of the resource.
     * @param color The color for the icon.
     * @return The image icon.
     * @throws IconRuntimeException If the resource could not be loaded.
     */
    public static ImageIcon getColorizedIcon(String resource, Color color) throws IconRuntimeException
    {
        return getColorizedIcon(getIcon(resource), color);
    }

    /**
     * Get an image icon using a system resource and color the non-transparent
     * pixels using the given color.
     *
     * @param resource The name of the resource.
     * @param color The color for the icon.
     * @param iconSize The size of the icon.
     * @return The image icon.
     * @throws IconRuntimeException If the resource could not be loaded.
     */
    public static ImageIcon getColorizedIcon(String resource, Color color, int iconSize) throws IconRuntimeException
    {
        return new ImageIcon(
                getColorizedIcon(getIcon(resource), color).getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
    }

    /**
     * Get an image icon using a system resource and color the non-transparent
     * pixels using the given color. The color will be adjusted according to the
     * style.
     *
     * @param resource The name of the resource.
     * @param style The style to use for the icon.
     * @param color The color.
     * @return The image icon.
     * @throws IconRuntimeException If the resource could not be loaded.
     */
    public static Icon getColorizedIcon(String resource, IconStyle style, Color color)
    {
        return getColorizedIcon(getIcon(resource), style, color);
    }

    /**
     * Get an image icon using an icon type and color the non-transparent pixels
     * using the given color. The color will be adjusted according to the style.
     *
     * @param iconType The icon type.
     * @param style The style to use for the icon.
     * @param color The color.
     * @return The image icon.
     * @throws IconRuntimeException If the resource could not be loaded.
     */
    public static ImageIcon getColorizedIcon(IconType iconType, IconStyle style, Color color)
    {
        return getColorizedIcon(getIcon(iconType), style, color);
    }

    /**
     * Get an image icon using an icon type and color the non-transparent pixels
     * using the given color. The color will be adjusted according to the style.
     *
     * @param iconType The icon type.
     * @param style The style to use for the icon.
     * @param color The color.
     * @param iconSize The icon size.
     * @return The image icon.
     * @throws IconRuntimeException If the resource could not be loaded.
     */
    public static Icon getColorizedIcon(IconType iconType, IconStyle style, Color color, int iconSize)
    {
        return new ImageIcon(
                getColorizedIcon(iconType, style, color).getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
    }

    /**
     * Color the non-transparent pixels of the input icon using the given color
     * across a gradient. The color will be adjusted according to the style.
     *
     * @param icon The input icon.
     * @param style The style to use for the icon.
     * @param color The color.
     * @return The colorized icon.
     */
    public static BufferedImage getColorizedImage(ImageIcon icon, IconStyle style, Color color)
    {
        Paint paint;
        final float upFactor = 1.6f;
        final float downFactor = .6f;
        switch (style)
        {
            case FLAT:
            {
                paint = color;
                break;
            }
            case NORMAL:
            {
                paint = new GradientPaint(0.0F, 0.0F, Color.WHITE, 0.0F, icon.getIconHeight() * downFactor, color);
                break;
            }
            case ROLLOVER:
            {
                float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
                hsb[1] = MathUtil.clamp(hsb[1] * upFactor, 0f, 1f);
                hsb[2] = MathUtil.clamp(hsb[2] * upFactor, 0f, 1f);
                paint = new GradientPaint(0.0F, 0.0F, Color.WHITE, 0.0F, icon.getIconHeight() * downFactor,
                        Color.getHSBColor(hsb[0], hsb[1], hsb[2]));
                break;
            }
            case PRESSED:
            {
                float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
                hsb[1] = MathUtil.clamp(hsb[1] * upFactor, 0f, 1f);
                hsb[2] = MathUtil.clamp(hsb[2] * downFactor, 0f, 1f);
                paint = new GradientPaint(0.0F, 0.0F, Color.WHITE, 0.0F, icon.getIconHeight() * downFactor,
                        Color.getHSBColor(hsb[0], hsb[1], hsb[2]));
                break;
            }
            default:
                throw new UnexpectedEnumException(style);
        }

        BufferedImage mixImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = mixImage.getGraphics();
        ((Graphics2D)graphics).setPaint(paint);
        ((Graphics2D)graphics).setComposite(AlphaComposite.SrcOver);
        graphics.fillRect(0, 0, mixImage.getWidth(), mixImage.getHeight());
        ((Graphics2D)graphics).setComposite(AlphaComposite.DstIn);
        graphics.drawImage(icon.getImage(), 0, 0, icon.getImageObserver());
        return mixImage;
    }

    /**
     * Get an image icon for the given icon type.
     *
     * @param iconType The icon type.
     * @return The image icon.
     * @throws IconRuntimeException If the resource could not be loaded.
     */
    public static ImageIcon getIcon(IconType iconType) throws IconRuntimeException
    {
        return getIcon(getResource(iconType));
    }

    /**
     * Get an image icon using a system resource.
     *
     * @param resource The name of the resource.
     * @return The image icon.
     * @throws IconRuntimeException If the resource could not be loaded.
     */
    public static ImageIcon getIcon(String resource) throws IconRuntimeException
    {
        try
        {
            // TODO we might consider caching these rather than reloading each
            // time.
            return new ImageIcon(ImageIO.read(IconUtil.class.getResource(resource)));
        }
        catch (IOException e)
        {
            throw new IconRuntimeException("Failed to load icon: " + resource, e);
        }
    }

    /**
     * Gets the resource name for the given icon type.
     *
     * @param iconType The icon type.
     * @return the resource name
     */
    public static String getResource(IconType iconType)
    {
        return ourTypeToResourceMap.get(iconType);
    }

    /**
     * Load an icon for the given icon type and derive icons in the styles
     * defined in {@link IconStyle}. Set the icons in the input button. Use the
     * default icon color.
     *
     * @param button The button whose icons are to be set.
     * @param iconType The icon type.
     * @throws IconRuntimeException If the resource cannot be loaded.
     */
    public static void setIcons(AbstractButton button, IconType iconType) throws IconRuntimeException
    {
        setIcons(button, getResource(iconType));
    }

    /**
     * Load an icon for the given icon type and derive icons in the styles
     * defined in {@link IconStyle}. Set the icons in the input button.
     *
     * @param button The button whose icons are to be set.
     * @param iconType The icon type.
     * @param normalColor The color to use for the normal style of the icon. The
     *            other icons will use colors based on this color.
     * @throws IconRuntimeException If the resource cannot be loaded.
     */
    public static void setIcons(AbstractButton button, IconType iconType, Color normalColor) throws IconRuntimeException
    {
        setIcons(button, getResource(iconType), normalColor);
    }

    /**
     * Load an icon for the given icon type and derive icons in the styles
     * defined in {@link IconStyle}. Set the icons in the input button.
     *
     * @param button The button whose icons are to be set.
     * @param iconType The icon type.
     * @param iconColor The icon color.
     * @param iconSize The icon size.
     */
    public static void setIcons(AbstractButton button, IconType iconType, Color iconColor, int iconSize)
    {
        ImageIcon icon = new ImageIcon(
                IconUtil.getIcon(iconType).getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
        setIcons(button, icon, iconColor, (Color)null);
    }

    /**
     * Load an icon for the given icon type and derive icons in the styles
     * defined in {@link IconStyle}. Set the icons in the input button.
     *
     * @param button The button whose icons are to be set.
     * @param iconType The icon type.
     * @param iconSize The icon size.
     */
    public static void setIcons(AbstractButton button, IconType iconType, int iconSize)
    {
        ImageIcon icon = new ImageIcon(
                IconUtil.getIcon(iconType).getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
        setIcons(button, icon);
    }

    /**
     * Color the non-transparent pixels of the input icon using the default
     * color across a gradient for the various icon styles. Set the icons in the
     * button.
     *
     * @param button The button whose icons are to be set.
     * @param icon The input icon.
     */
    public static void setIcons(AbstractButton button, ImageIcon icon)
    {
        setIcons(button, icon, DEFAULT_ICON_FOREGROUND, ICON_SELECTION_FOREGROUND);
    }

    /**
     * Color the non-transparent pixels of the input icon using the given color
     * across a gradient for the various icon styles. Set the icons in the
     * button.
     *
     * @param button The button whose icons are to be set.
     * @param icon The input icon.
     * @param normalColor The normal color.
     * @param selectColor The select color, or {@code null} to use color similar
     *            to the normal color.
     */
    public static void setIcons(AbstractButton button, ImageIcon icon, Color normalColor, Color selectColor)
    {
        button.setIcon(IconUtil.getColorizedIcon(icon, IconStyle.NORMAL, normalColor));
        button.setRolloverIcon(IconUtil.getColorizedIcon(icon, IconStyle.ROLLOVER, normalColor));
        if (selectColor == null)
        {
            button.setPressedIcon(IconUtil.getColorizedIcon(icon, IconStyle.PRESSED, normalColor));
        }
        else
        {
            button.setPressedIcon(IconUtil.getColorizedIcon(icon, IconStyle.NORMAL, selectColor));
        }

        button.setSelectedIcon(button.getPressedIcon());
    }

    /**
     * Load an icon at the given resource location and derive icons in the
     * styles defined in {@link IconStyle}. Set the icons in the input button.
     * Use the default icon color.
     *
     * @param button The button whose icons are to be set.
     * @param resource The resource location used to find the image for the
     *            icon.
     * @throws IconRuntimeException If the resource cannot be loaded.
     */
    public static void setIcons(AbstractButton button, String resource) throws IconRuntimeException
    {
        setIcons(button, resource, DEFAULT_ICON_FOREGROUND, ICON_SELECTION_FOREGROUND);
    }

    /**
     * Load an icon at the given resource location and derive icons in the
     * styles defined in {@link IconStyle}. Set the icons in the input button.
     *
     * @param button The button whose icons are to be set.
     * @param resource The resource location used to find the image for the
     *            icon.
     * @param normalColor The color to use for the normal style of the icon. The
     *            other icons will use colors based on this color.
     * @throws IconRuntimeException If the resource cannot be loaded.
     */
    public static void setIcons(AbstractButton button, String resource, Color normalColor) throws IconRuntimeException
    {
        setIcons(button, resource, normalColor, (Color)null);
    }

    /**
     * Load an icon at the given resource location and derive icons in the
     * styles defined in {@link IconStyle}. Set the icons in the input button.
     *
     * @param button The button whose icons are to be set.
     * @param resource The resource location used to find the image for the
     *            icon.
     * @param normalColor The color to use for the normal style of the icon. The
     *            rollover icon will use a color based on this color.
     * @param selectColor The color to use for the selected style of the icon.
     * @throws IconRuntimeException If the resource cannot be loaded.
     */
    public static void setIcons(AbstractButton button, String resource, Color normalColor, Color selectColor)
        throws IconRuntimeException
    {
        setIcons(button, getIcon(resource), normalColor, selectColor);
    }

    /** Disallow instantiation. */
    private IconUtil()
    {
    }

    /** Style indicating how a colorized icon should be colored. */
    public enum IconStyle
    {
        /** Flat (no gradient). */
        FLAT,

        /** The normal icon style. */
        NORMAL,

        /** The selected icon style. */
        PRESSED,

        /** The rollover icon style. */
        ROLLOVER,
    }

    /** Icon type. */
    public enum IconType
    {
        /** Airplane. */
        AIRPLANE,

        /** Icon of airplane on screen on the earth. */
        AIRPLANE_ON_SCREEN_ON_EARTH,

        /** Binoculars. */
        BINOCULARS,

        /** Bubble. */
        BUBBLE,

        /** Filled bubble. */
        BUBBLE_FILLED,

        /** Bulls eye. */
        BULLS_EYE,

        /** Bug. */
        BUG,

        /** Calendar. */
        CALENDAR,

        /** Camera. */
        CAMERA,

        /** Cancel. */
        CANCEL,

        /** Clock. */
        CLOCK,

        /** Close. */
        CLOSE,

        /** Cog. */
        COG,

        /** Cogs. */
        COGS,

        /** Collapse. */
        COLLAPSE,

        /** Copy. */
        COPY,

        /** Delete. */
        DELETE,

        /** Disk. */
        DISK,

        /** Download. */
        DOWNLOAD,

        /** Edit. */
        EDIT,

        /** Expand. */
        EXPAND,

        /** Export. */
        EXPORT,

        /** File. */
        FILE,

        /** Filter. */
        FILTER,

        /** Go to first step. */
        FIRST,

        /** Forward 10s. */
        FUTURE10,

        /** Forward 30s. */
        FUTURE30,

        /** Back 10s. */
        HISTORY10,

        /** Back 30s. */
        HISTORY30,

        /** Import. */
        IMPORT,

        /** Go to last step. */
        LAST,

        /** Lock. */
        LOCK,

        /** Menu. */
        MENU,

        /** Move Up. */
        MOVE_UP,

        /** Move Down. */
        MOVE_DOWN,

        /** Forward. */
        NEXT,

        /** Notification. */
        NOTIFICATION,

        /** Open. */
        OPEN,

        /** Pause. */
        PAUSE,

        /** Play. */
        PLAY,

        /** Plus. */
        PLUS,

        /** Backward. */
        PREVIOUS,

        /** Question. */
        QUESTION,

        /** Record. */
        RECORD,

        /** Reload. */
        RELOAD,

        /** A screen. */
        SCREEN,

        /** A screen on the earth. */
        SCREEN_ON_EARTH,

        /** Snap-to. */
        SNAPTO,

        /** Stack. */
        STACK,

        /** Star. */
        STAR,

        /** Stats. */
        STATS,

        /** Stop. */
        STOP,

        /** Storage. */
        STORAGE,

        /** Tag. */
        TAG,

        /** Triangle. */
        TRIANGLE,

        /** Unlock. */
        UNLOCK,

        /** Upload. */
        UPLOAD,

        /** View. */
        VIEW,

        ;
    }
}

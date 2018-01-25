package io.opensphere.laf.dark;

import java.awt.Color;
import java.awt.Font;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;

public class OSDarkLAFTheme extends DefaultMetalTheme
{
    public static final int DEFAULT_MENU_OPACITY = 155;

    public static final int DEFAULT_FRAME_OPACITY = 155;

    private ColorUIResource primary1 = new ColorUIResource(Color.decode("#8080A2"));

    private ColorUIResource primary2 = new ColorUIResource(Color.decode("#C2C2C2"));

    private ColorUIResource primary3 = new ColorUIResource(Color.decode("#CCCCCC"));

    private ColorUIResource secondary1 = new ColorUIResource(Color.decode("#8080A2"));

    private ColorUIResource secondary2 = new ColorUIResource(Color.gray);

    private ColorUIResource secondary3 = new ColorUIResource(Color.decode("#3F3F50"));

    private ColorUIResource black = new ColorUIResource(Color.decode("#FFFFFF"));

    private ColorUIResource white = new ColorUIResource(Color.decode("#999999"));

    private final FontUIResource font = new FontUIResource("SansSerif", Font.PLAIN, 12);

    private final FontUIResource fontBold = new FontUIResource("SansSerif", Font.BOLD, 12);

    private int menuOpacity = DEFAULT_MENU_OPACITY;

    private int frameOpacity = DEFAULT_FRAME_OPACITY;

    public OSDarkLAFTheme()
    {
        super();
    }

    public OSDarkLAFTheme(Color base)
    {
        super();
        setPrimary(base);
    }

    public OSDarkLAFTheme(Color primary, Color secondary)
    {
        super();
        setPrimary(primary);
        setSecondary(secondary);
    }

    @Override
    public String getName()
    {
        return "OpenSphere Dark Theme";
    }

    @Override
    protected ColorUIResource getPrimary1()
    {
        return primary1;
    }

    @Override
    protected ColorUIResource getPrimary2()
    {
        return primary2;
    }

    @Override
    protected ColorUIResource getPrimary3()
    {
        return primary3;
    }

    @Override
    protected ColorUIResource getSecondary1()
    {
        return secondary1;
    }

    @Override
    protected ColorUIResource getSecondary2()
    {
        return secondary2;
    }

    @Override
    protected ColorUIResource getSecondary3()
    {
        return secondary3;
    }

    @Override
    protected ColorUIResource getBlack()
    {
        return black;
    }

    @Override
    protected ColorUIResource getWhite()
    {
        return white;
    }

    public int getOpacity()
    {
        return getMenuOpacity();
    }

    public int getFrameOpacity()
    {
        return frameOpacity;
    }

    @Override
    public FontUIResource getControlTextFont()
    {
        return font;
    }

    @Override
    public FontUIResource getMenuTextFont()
    {
        return font;
    }

    @Override
    public FontUIResource getSubTextFont()
    {
        return font;
    }

    @Override
    public FontUIResource getSystemTextFont()
    {
        return fontBold;
    }

    @Override
    public FontUIResource getUserTextFont()
    {
        return font;
    }

    @Override
    public FontUIResource getWindowTitleFont()
    {
        return fontBold;
    }

    public void setPrimary(Color primary)
    {
        final int r = primary.getRed();
        final int g = primary.getGreen();
        final int b = primary.getBlue();
        primary1 = new ColorUIResource(new Color(r > 20 ? r - 20 : 0, g > 20 ? g - 20 : 0, b > 20 ? b - 20 : 0));
        primary2 = new ColorUIResource(new Color(r > 10 ? r - 10 : 0, g > 10 ? g - 10 : 0, b > 10 ? b - 10 : 0));
        primary3 = new ColorUIResource(primary);
    }

    public void setSecondary(Color secondary)
    {
        final int r = secondary.getRed();
        final int g = secondary.getGreen();
        final int b = secondary.getBlue();
        secondary1 = new ColorUIResource(new Color(r > 20 ? r - 20 : 0, g > 20 ? g - 20 : 0, b > 20 ? b - 20 : 0));
        secondary2 = new ColorUIResource(new Color(r > 10 ? r - 10 : 0, g > 10 ? g - 10 : 0, b > 10 ? b - 10 : 0));
        secondary3 = new ColorUIResource(secondary);
    }

    public void setPrimary1(Color c)
    {
        primary1 = new ColorUIResource(c);
    }

    public void setPrimary2(Color c)
    {
        primary2 = new ColorUIResource(c);
    }

    public void setPrimary3(Color c)
    {
        primary3 = new ColorUIResource(c);
    }

    public void setSecondary1(Color c)
    {
        secondary1 = new ColorUIResource(c);
    }

    public void setSecondary2(Color c)
    {
        secondary2 = new ColorUIResource(c);
    }

    public void setSecondary3(Color c)
    {
        secondary3 = new ColorUIResource(c);
    }

    public void setBlack(Color c)
    {
        black = new ColorUIResource(c);
    }

    public void setWhite(Color c)
    {
        white = new ColorUIResource(c);
    }

    public void setOpacity(int opacity)
    {
        setMenuOpacity(opacity);
    }

    public void setMenuOpacity(int opacity)
    {
        if (opacity < 0 || opacity > 255)
        {
            throw new NumberFormatException("MenuOpacity out of range [0,255]: " + opacity);
        }

        menuOpacity = opacity;
    }

    public int getMenuOpacity()
    {
        return menuOpacity;
    }

    public void setFrameOpacity(int val)
    {
        if (val < 0 || val > 255)
        {
            throw new NumberFormatException("MenuOpacity out of range [0,255]: " + val);
        }

        frameOpacity = val;
    }

    @Override
    public String toString()
    {
        final StringBuffer buff = new StringBuffer();
        buff.append("io.opensphere.laf.default_laf.p1=" + encode(primary1) + "\n");
        buff.append("io.opensphere.laf.default_laf.p2=" + encode(primary2) + "\n");
        buff.append("io.opensphere.laf.default_laf.p3=" + encode(primary3) + "\n");
        buff.append("io.opensphere.laf.default_laf.s1=" + encode(secondary1) + "\n");
        buff.append("io.opensphere.laf.default_laf.s2=" + encode(secondary2) + "\n");
        buff.append("io.opensphere.laf.default_laf.s3=" + encode(secondary3) + "\n");
        buff.append("io.opensphere.laf.default_laf.w=" + encode(white) + "\n");
        buff.append("io.opensphere.laf.default_laf.b=" + encode(black) + "\n");
        buff.append("io.opensphere.laf.default_laf.menuOpacity=" + menuOpacity + "\n");
        buff.append("io.opensphere.laf.default_laf.frameOpacity=" + frameOpacity + "\n");
        return buff.toString();
    }

    protected String encode(Color c)
    {
        final String r = Integer.toHexString(c.getRed()).toUpperCase();
        final String g = Integer.toHexString(c.getGreen()).toUpperCase();
        final String b = Integer.toHexString(c.getBlue()).toUpperCase();
        return "#" + (r.length() == 1 ? "0" + r : r) + (g.length() == 1 ? "0" + g : g) + (b.length() == 1 ? "0" + b : b);
    }
}

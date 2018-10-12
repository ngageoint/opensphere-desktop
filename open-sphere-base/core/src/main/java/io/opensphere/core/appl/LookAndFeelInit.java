package io.opensphere.core.appl;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

import io.opensphere.core.util.Colors;
import io.opensphere.laf.dark.OSDarkLAFTheme;
import io.opensphere.laf.dark.OpenSphereDarkLookAndFeel;

/**
 * Initialization class for the look and feel.
 */
public class LookAndFeelInit
{
    /**
     * Set the look and feel of the GUI.
     *
     * @throws UnsupportedLookAndFeelException If the look and feel is not supported.
     */
    public void setLookAndFeel() throws UnsupportedLookAndFeelException
    {
        if (!System.getProperties().containsKey("swing.defaultlaf"))
        {
            final OSDarkLAFTheme nt = new OSDarkLAFTheme()
            {
                @Override
                public ColorUIResource getControlDisabled()
                {
                    return new ColorUIResource(Color.LIGHT_GRAY);
                }
            };

            nt.setPrimary1(Colors.LF_PRIMARY1);
            nt.setPrimary2(Colors.LF_PRIMARY2);
            nt.setPrimary3(Colors.LF_PRIMARY3);
            nt.setSecondary1(Colors.LF_SECONDARY1);
            nt.setSecondary2(Colors.LF_SECONDARY2);
            nt.setSecondary3(Colors.LF_SECONDARY3);
            nt.setWhite(Colors.LF_WHITE);
            nt.setBlack(Colors.LF_BLACK);

            final int opacity = 155;
            nt.setMenuOpacity(opacity);
            nt.setFrameOpacity(opacity);

            //        UIManager.put("InternalFrame.border", new BorderUIResource(new OSDarkLAFInternalFrameBorder()));
            //        UIManager.put("InternalFrame.paletteBorder", new BorderUIResource(BorderFactory.createEmptyBorder()));

            final OpenSphereDarkLookAndFeel laf = new OpenSphereDarkLookAndFeel();
            OpenSphereDarkLookAndFeel.setCurrentTheme(nt);
            UIManager.setLookAndFeel(laf);

            UIManager.put("InternalFrame.titleFont", new FontUIResource(new JLabel().getFont()));
        }
    }
}

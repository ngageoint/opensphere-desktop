package io.opensphere.laf.dark;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.Kernel;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;

/**
 * The OpenSphere Dark Look and Feel is derived from ( or inspired by ) the
 * NimROD Look and Feel by Nilo J. Gonzalez. NimROD is licensed under LGPL. We
 * have re-written every class, and though most of the algorithms used to
 * compose the look and feel remain very similar, it has been re-written and
 * re-worked to provide a consistent look and feel for the OpenSphere tool, and
 * also to allow more customization of the UI implementation along with a
 * different theme.
 */
@SuppressWarnings("serial")
public class OpenSphereDarkLookAndFeel extends MetalLookAndFeel
{
    protected static MetalTheme theme;

    public OpenSphereDarkLookAndFeel()
    {
        super();

        final OSDarkLAFTheme nt = new OSDarkLAFTheme();
        setCurrentTheme(nt);

        float[] elements = new float[OSDarkLAFUtils.TITLE_SHADOW_THICKNESS * OSDarkLAFUtils.TITLE_SHADOW_THICKNESS];
        for (int i = 0; i < elements.length; i++)
        {
            elements[i] = 0.1f;
        }
        int mid = OSDarkLAFUtils.TITLE_SHADOW_THICKNESS / 2 + 1;
        elements[mid * mid] = .2f;

        OSDarkLAFUtils.titleShaodowKernel = new Kernel(OSDarkLAFUtils.TITLE_SHADOW_THICKNESS,
                OSDarkLAFUtils.TITLE_SHADOW_THICKNESS, elements);

        elements = new float[OSDarkLAFUtils.MENU_SHADOW_THICKNESS * OSDarkLAFUtils.MENU_SHADOW_THICKNESS];
        for (int i = 0; i < elements.length; i++)
        {
            elements[i] = 0.1f;
        }
        mid = OSDarkLAFUtils.MENU_SHADOW_THICKNESS / 2 + 1;
        elements[mid * mid] = .2f;

        OSDarkLAFUtils.menuShadowKernel = new Kernel(OSDarkLAFUtils.MENU_SHADOW_THICKNESS, OSDarkLAFUtils.MENU_SHADOW_THICKNESS,
                elements);
    }

    @Override
    public void initialize()
    {
        try
        {
            final LookAndFeel laf = (LookAndFeel)Class.forName(UIManager.getSystemLookAndFeelClassName()).getDeclaredConstructor().newInstance();
            final UIDefaults def = laf.getDefaults();
            final Enumeration<Object> keys = def.keys();
            String key;

            while (keys.hasMoreElements())
            {
                key = keys.nextElement().toString();

                if (key.contains("InputMap"))
                {
                    UIManager.getDefaults().put(key, def.get(key));
                }
            }
        }
        catch (final Exception ex)
        {
            // ex.printStackTrace();
        }
    }

    @Override
    public String getID()
    {
        return "OSDarkLAF";
    }

    @Override
    public String getName()
    {
        return "OSDarkLAF";
    }

    @Override
    public String getDescription()
    {
        return "OpenSphere Dark Look and Feel";
    }

    @Override
    public boolean isNativeLookAndFeel()
    {
        return false;
    }

    @Override
    public boolean isSupportedLookAndFeel()
    {
        return true;
    }

    @Override
    public boolean getSupportsWindowDecorations()
    {
        return false;
    }

    public static void setCurrentTheme(MetalTheme metTheme)
    {
        MetalLookAndFeel.setCurrentTheme(metTheme);
        theme = metTheme;
        OSDarkLAFUtils.rolloverColor = null;
    }

    @Override
    protected void initClassDefaults(UIDefaults defaults)
    {
        super.initClassDefaults(defaults);

        defaults.put("ButtonUI", "io.opensphere.laf.dark.OSDarkLAFButtonUI");
        defaults.put("ToggleButtonUI", "io.opensphere.laf.dark.OSDarkLAFToggleButtonUI");
        defaults.put("TextFieldUI", "io.opensphere.laf.dark.OSDarkLAFTextFieldUI");
        defaults.put("TextAreaUI", "io.opensphere.laf.dark.OSDarkLAFTextAreaUI");
        defaults.put("PasswordFieldUI", "io.opensphere.laf.dark.OSDarkLAFPasswordFieldUI");
        defaults.put("CheckBoxUI", "io.opensphere.laf.dark.OSDarkLAFCheckBoxUI");
        defaults.put("RadioButtonUI", "io.opensphere.laf.dark.OSDarkLAFRadioButtonUI");
        defaults.put("FormattedTextFieldUI", "io.opensphere.laf.dark.OSDarkLAFFormattedTextFieldUI");
        defaults.put("SliderUI", "io.opensphere.laf.dark.OSDarkLAFSliderUI");
        defaults.put("SpinnerUI", "io.opensphere.laf.dark.OSDarkLAFSpinnerUI");
        defaults.put("ListUI", "io.opensphere.laf.dark.OSDarkLAFListUI");
        defaults.put("ComboBoxUI", "io.opensphere.laf.dark.OSDarkLAFComboBoxUI");
        defaults.put("ScrollBarUI", "io.opensphere.laf.dark.OSDarkLAFScrollBarUI");
        defaults.put("ToolBarUI", "io.opensphere.laf.dark.OSDarkLAFToolBarUI");
        defaults.put("ProgressBarUI", "io.opensphere.laf.dark.OSDarkLAFProgressBarUI");
        defaults.put("ScrollPaneUI", "io.opensphere.laf.dark.OSDarkLAFScrollPaneUI");
        defaults.put("TabbedPaneUI", "io.opensphere.laf.dark.OSDarkLAFTabbedPaneUI");
        defaults.put("TableHeaderUI", "io.opensphere.laf.dark.OSDarkLAFTableHeaderUI");
        defaults.put("SplitPaneUI", "io.opensphere.laf.dark.OSDarkLAFSplitPaneUI");
        defaults.put("InternalFrameUI", "io.opensphere.laf.dark.OSDarkLAFInternalFrameUI");
        defaults.put("DesktopIconUI", "io.opensphere.laf.dark.OSDarkLAFDesktopIconUI");
        defaults.put("ToolTipUI", "io.opensphere.laf.dark.OSDarkLAFToolTipUI");
        defaults.put("MenuBarUI", "io.opensphere.laf.dark.OSDarkLAFMenuBarUI");
        defaults.put("MenuUI", "io.opensphere.laf.dark.OSDarkLAFMenuUI");
        defaults.put("SeparatorUI", "io.opensphere.laf.dark.OSDarkLAFSeparatorUI");
        defaults.put("PopupMenuUI", "io.opensphere.laf.dark.OSDarkLAFPopupMenuUI");
        defaults.put("PopupMenuSeparatorUI", "io.opensphere.laf.dark.OSDarkLAFPopupMenuSeparatorUI");
        defaults.put("MenuItemUI", "io.opensphere.laf.dark.OSDarkLAFMenuItemUI");
        defaults.put("CheckBoxMenuItemUI", "io.opensphere.laf.dark.OSDarkLAFCheckBoxMenuItemUI");
        defaults.put("RadioButtonMenuItemUI", "io.opensphere.laf.dark.OSDarkLAFRadioButtonMenuItemUI");
    }

    @Override
    protected void initSystemColorDefaults(UIDefaults def)
    {
        super.initSystemColorDefaults(def);
        def.put("textHighlight", getMenuSelectedBackground());
        def.put("textInactiveText", getInactiveSystemTextColor().darker());
    }

    @Override
    protected void initComponentDefaults(UIDefaults uiDefaults)
    {
        super.initComponentDefaults(uiDefaults);

        try
        {
            final ColorUIResource colorForeground = (ColorUIResource)uiDefaults.get("MenuItem.disabledForeground");
            final ColorUIResource colorBackground = (ColorUIResource)uiDefaults.get("MenuItem.foreground");

            final ColorUIResource col = OSDarkLAFUtils.getThirdColor(colorBackground, colorForeground);
            uiDefaults.put("MenuItem.disabledForeground", col);
            uiDefaults.put("Label.disabledForeground", col);
            uiDefaults.put("CheckBoxMenuItem.disabledForeground", col);
            uiDefaults.put("Menu.disabledForeground", col);
            uiDefaults.put("RadioButtonMenuItem.disabledForeground", col);
            uiDefaults.put("ComboBox.disabledForeground", col);
            uiDefaults.put("Button.disabledText", col);
            uiDefaults.put("ToggleButton.disabledText", col);
            uiDefaults.put("CheckBox.disabledText", col);
            uiDefaults.put("RadioButton.disabledText", col);

            final ColorUIResource col2 = OSDarkLAFUtils.getThirdColor(OpenSphereDarkLookAndFeel.getWhite(),
                    (Color)uiDefaults.get("TextField.inactiveBackground"));
            uiDefaults.put("TextField.inactiveBackground", col2);
        }
        catch (final Exception ex)
        {
            ex.printStackTrace();
        }

        uiDefaults.put("MenuBar.border", OSDarkLAFBorders.getMenuBarBorder());

        final Font fontMenu = ((Font)uiDefaults.get("Menu.font")).deriveFont(Font.BOLD);
        uiDefaults.put("MenuItem.acceleratorFont", fontMenu);
        uiDefaults.put("RadioButtonMenuItem.acceleratorFont", fontMenu);
        uiDefaults.put("CheckBoxMenuItem.acceleratorFont", fontMenu);

        final ColorUIResource colAcce = OSDarkLAFUtils.getThirdColor((ColorUIResource)uiDefaults.get("MenuItem.foreground"),
                (ColorUIResource)uiDefaults.get("MenuItem.acceleratorForeground"));

        uiDefaults.put("MenuItem.acceleratorForeground", colAcce);
        uiDefaults.put("RadioButtonMenuItem.acceleratorForeground", colAcce);
        uiDefaults.put("CheckBoxMenuItem.acceleratorForeground", colAcce);

        // For popupmenu's shadows
        uiDefaults.put("BorderPopupMenu.MenuShadowBottomIcon", OSDarkLAFUtils.loadImageResource("/icons/MenuShadowBottom.png"));
        uiDefaults.put("BorderPopupMenu.MenuShadowRight", OSDarkLAFUtils.loadImageResource("/icons/MenuShadowRight.png"));
        uiDefaults.put("BorderPopupMenu.MenuShadowTopLeft", OSDarkLAFUtils.loadImageResource("/icons/MenuShadowTopLeft.png"));
        uiDefaults.put("BorderPopupMenu.MenuShadowUp", OSDarkLAFUtils.loadImageResource("/icons/MenuShadowUp.png"));
        uiDefaults.put("BorderPopupMenu.MenuShadowTopRight", OSDarkLAFUtils.loadImageResource("/icons/MenuShadowTopRight.png"));

        // JTree Support
        uiDefaults.put("Tree.collapsedIcon", OSDarkLAFIconFactory.getTreeCollapsedIcon());
        uiDefaults.put("Tree.expandedIcon", OSDarkLAFIconFactory.getTreeExpandedIcon());
        uiDefaults.put("Tree.closedIcon", OSDarkLAFUtils.loadImageResource("/icons/ClosedFolder.png"));
        uiDefaults.put("Tree.openIcon", OSDarkLAFUtils.loadImageResource("/icons/OpenFolder.png"));
        uiDefaults.put("Tree.leafIcon", OSDarkLAFUtils.loadImageResource("/icons/TreeFileIcon.png"));
        uiDefaults.put("Tree.PickIcon", OSDarkLAFUtils.loadImageResource("/icons/TreeBubble.png"));

        // Dialogs and Files
        uiDefaults.put("FileView.directoryIcon", OSDarkLAFUtils.loadImageResource("/icons/DialogClosedFolder.png"));
        uiDefaults.put("FileView.fileIcon", OSDarkLAFUtils.loadImageResource("/icons/DialogFileIcon.png"));
        uiDefaults.put("FileView.floppyDriveIcon", OSDarkLAFUtils.loadImageResource("/icons/DialogFloppyIcon.png"));
        uiDefaults.put("FileView.hardDriveIcon", OSDarkLAFUtils.loadImageResource("/icons/DialogHDIcon.png"));
        uiDefaults.put("FileChooser.newFolderIcon", OSDarkLAFUtils.loadImageResource("/icons/DialogNewDir.png"));
        uiDefaults.put("FileChooser.homeFolderIcon", OSDarkLAFUtils.loadImageResource("/icons/DialogHome.png"));
        uiDefaults.put("FileChooser.upFolderIcon", OSDarkLAFUtils.loadImageResource("/icons/DialogDirUp.png"));
        uiDefaults.put("FileChooser.detailsViewIcon", OSDarkLAFUtils.loadImageResource("/icons/DialogDetails.png"));
        uiDefaults.put("FileChooser.listViewIcon", OSDarkLAFUtils.loadImageResource("/icons/DialogList.png"));

        // Check Boxes and Radio Buttons
        uiDefaults.put("CheckBoxMenuItem.checkIcon", OSDarkLAFIconFactory.getCheckBoxMenuItemIcon());
        uiDefaults.put("RadioButtonMenuItem.checkIcon", OSDarkLAFIconFactory.getRadioButtonMenuItemIcon());

        // Files and Combo Buttons
        uiDefaults.put("ComboBox.arrowIcon", OSDarkLAFUtils.loadImageResource("/icons/ComboButtonDown.png"));
        uiDefaults.put("ComboBox.buttonDownIcon", OSDarkLAFIconFactory.getComboArrowIcon());

        // Menu Icons
        uiDefaults.put("Menu.checkIcon", OSDarkLAFIconFactory.getBandaMenuItemIcon());
        uiDefaults.put("MenuItem.checkIcon", OSDarkLAFIconFactory.getBandaMenuItemIcon());
        uiDefaults.put("MenuCheckBox.iconBase", OSDarkLAFUtils.loadImageResource("/icons/MenuCheckBoxBase.png"));
        uiDefaults.put("MenuCheckBox.iconTick", OSDarkLAFUtils.loadImageResource("/icons/MenuCheckBoxTick.png"));
        uiDefaults.put("MenuRadioButton.iconBase", OSDarkLAFUtils.loadImageResource("/icons/MenuRadioBase.png"));
        uiDefaults.put("MenuRadioButton.iconTick", OSDarkLAFUtils.loadImageResource("/icons/MenuRadioTick.png"));
        uiDefaults.put("CheckBox.iconBase", OSDarkLAFUtils.loadImageResource("/icons/CheckBoxBase.png"));
        uiDefaults.put("CheckBox.iconTick", OSDarkLAFUtils.loadImageResource("/icons/CheckBoxTick.png"));
        uiDefaults.put("RadioButton.iconBase", OSDarkLAFUtils.loadImageResource("/icons/RadioButtonBase.png"));
        uiDefaults.put("RadioButton.iconTick", OSDarkLAFUtils.loadImageResource("/icons/RadioButtonTick.png"));

        // Border Icons
        uiDefaults.put("BorderGeneralTop", OSDarkLAFUtils.loadImageResource("/icons/BorderGeneralTop.png"));
        uiDefaults.put("BorderGeneralUpperRight", OSDarkLAFUtils.loadImageResource("/icons/BorderGeneralUpperRight.png"));
        uiDefaults.put("BorderGeneralRight", OSDarkLAFUtils.loadImageResource("/icons/BorderGeneralRight.png"));
        uiDefaults.put("BorderGeneralBottomRight", OSDarkLAFUtils.loadImageResource("/icons/BorderGeneralBottomRight.png"));
        uiDefaults.put("BorderGeneralBottom", OSDarkLAFUtils.loadImageResource("/icons/BorderGeneralBottom.png"));
        uiDefaults.put("BorderGeneralBottomLeft", OSDarkLAFUtils.loadImageResource("/icons/BorderGeneralBottomLeft.png"));
        uiDefaults.put("BorderGeneralLeft", OSDarkLAFUtils.loadImageResource("/icons/BorderGeneralLeft.png"));
        uiDefaults.put("BorderGeneralUpperLeft", OSDarkLAFUtils.loadImageResource("/icons/BorderGeneralUpperLeft.png"));

        // Standard Borders
        uiDefaults.put("List.border", OSDarkLAFBorders.getGenBorder());
        uiDefaults.put("ScrollPane.viewportBorder", OSDarkLAFBorders.getGenBorder());
        uiDefaults.put("Menu.border", OSDarkLAFBorders.getGenMenuBorder());
        uiDefaults.put("ToolBar.border", OSDarkLAFBorders.getToolBarBorder());
        uiDefaults.put("TextField.border", OSDarkLAFBorders.getTextFieldBorder());
        uiDefaults.put("TextArea.border", OSDarkLAFBorders.getTextFieldBorder());
        uiDefaults.put("FormattedTextField.border", OSDarkLAFBorders.getTextFieldBorder());
        uiDefaults.put("PasswordField.border", OSDarkLAFBorders.getTextFieldBorder());
        uiDefaults.put("ToolTip.border", OSDarkLAFBorders.getToolTipBorder());
        uiDefaults.put("Table.focusCellHighlightBorder", OSDarkLAFBorders.getCellFocusBorder());
        uiDefaults.put("ScrollPane.border", OSDarkLAFBorders.getScrollPaneBorder());

        // Tool Tips
        final ColorUIResource col2 = OSDarkLAFUtils.getThirdColor(OpenSphereDarkLookAndFeel.getFocusColor(),
                (Color)uiDefaults.get("TextField.inactiveBackground"));
        uiDefaults.put("ToolTip.background", col2);
        uiDefaults.put("ToolTip.font", uiDefaults.get("Menu.font"));

        // Spinners
        uiDefaults.put("Spinner.editorBorderPainted", Boolean.FALSE);
        uiDefaults.put("Spinner.border", OSDarkLAFBorders.getTextFieldBorder());
        uiDefaults.put("Spinner.arrowButtonBorder", BorderFactory.createEmptyBorder());
        uiDefaults.put("Spinner.nextIcon", OSDarkLAFIconFactory.getSpinnerNextIcon());
        uiDefaults.put("Spinner.previousIcon", OSDarkLAFIconFactory.getSpinnerPreviousIcon());

        // Icons and Dialogs
        uiDefaults.put("OptionPane.errorIcon", OSDarkLAFUtils.loadImageResource("/icons/Error.png"));
        uiDefaults.put("OptionPane.informationIcon", OSDarkLAFUtils.loadImageResource("/icons/Inform.png"));
        uiDefaults.put("OptionPane.warningIcon", OSDarkLAFUtils.loadImageResource("/icons/Warn.png"));
        uiDefaults.put("OptionPane.questionIcon", OSDarkLAFUtils.loadImageResource("/icons/Question.png"));

        // Slider Icons
        uiDefaults.put("Slider.horizontalThumbIcon", OSDarkLAFIconFactory.getSliderHorizontalIcon());
        uiDefaults.put("Slider.verticalThumbIcon", OSDarkLAFIconFactory.getSliderVerticalIcon());
        uiDefaults.put("Slider.horizontalThumbIconImage",
                OSDarkLAFUtils.loadImageResource("/icons/HorizontalThumbIconImage.png"));
        uiDefaults.put("Slider.verticalThumbIconImage", OSDarkLAFUtils.loadImageResource("/icons/VerticalThumbIconImage.png"));

        // Scrollbar Icons
        uiDefaults.put("ScrollBar.horizontalThumbIconImage",
                OSDarkLAFUtils.loadImageResource("/icons/HorizontalScrollIconImage.png"));
        uiDefaults.put("ScrollBar.verticalThumbIconImage",
                OSDarkLAFUtils.loadImageResource("/icons/VerticalScrollIconImage.png"));
        uiDefaults.put("ScrollBar.northButtonIconImage",
                OSDarkLAFUtils.loadImageResource("/icons/ScrollBarNorthButtonIconImage.png"));
        uiDefaults.put("ScrollBar.southButtonIconImage",
                OSDarkLAFUtils.loadImageResource("/icons/ScrollBarSouthButtonIconImage.png"));
        uiDefaults.put("ScrollBar.eastButtonIconImage",
                OSDarkLAFUtils.loadImageResource("/icons/ScrollBarEastButtonIconImage.png"));
        uiDefaults.put("ScrollBar.westButtonIconImage",
                OSDarkLAFUtils.loadImageResource("/icons/ScrollBarWestButtonIconImage.png"));
        uiDefaults.put("ScrollBar.northButtonIcon", OSDarkLAFIconFactory.getScrollBarNorthButtonIcon());
        uiDefaults.put("ScrollBar.southButtonIcon", OSDarkLAFIconFactory.getScrollBarSouthButtonIcon());
        uiDefaults.put("ScrollBar.eastButtonIcon", OSDarkLAFIconFactory.getScrollBarEastButtonIcon());
        uiDefaults.put("ScrollBar.westButtonIcon", OSDarkLAFIconFactory.getScrollBarWestButtonIcon());

        // Button Margins
        uiDefaults.put("Button.margin", new InsetsUIResource(5, 14, 5, 14));
        uiDefaults.put("ToggleButton.margin", new InsetsUIResource(5, 14, 5, 14));

        // Internal Frames and Icons
        uiDefaults.put("Desktop.background", uiDefaults.get("MenuItem.background"));
        uiDefaults.put("InternalFrame.border", OSDarkLAFBorders.getInternalFrameBorder());

        uiDefaults.put("InternalFrame.OSDarkLAFCloseIcon", OSDarkLAFUtils.loadImageResource("/icons/FrameClose.png"));
        uiDefaults.put("InternalFrame.OSDarkLAFCloseIconRoll", OSDarkLAFUtils.loadImageResource("/icons/FrameCloseRoll.png"));
        uiDefaults.put("InternalFrame.OSDarkLAFCloseIconPush", OSDarkLAFUtils.loadImageResource("/icons/FrameClosePush.png"));

        uiDefaults.put("InternalFrame.OSDarkLAFMaxIcon", OSDarkLAFUtils.loadImageResource("/icons/FrameMaximiza.png"));
        uiDefaults.put("InternalFrame.OSDarkLAFMaxIconRoll", OSDarkLAFUtils.loadImageResource("/icons/FrameMaximizaRoll.png"));
        uiDefaults.put("InternalFrame.OSDarkLAFMaxIconPush", OSDarkLAFUtils.loadImageResource("/icons/FrameMaximizaPush.png"));

        uiDefaults.put("InternalFrame.OSDarkLAFMinIcon", OSDarkLAFUtils.loadImageResource("/icons/FrameMinimiza.png"));
        uiDefaults.put("InternalFrame.OSDarkLAFMinIconRoll", OSDarkLAFUtils.loadImageResource("/icons/FrameMinimizaRoll.png"));
        uiDefaults.put("InternalFrame.OSDarkLAFMinIconPush", OSDarkLAFUtils.loadImageResource("/icons/FrameMinimizaPush.png"));

        uiDefaults.put("InternalFrame.OSDarkLAFResizeIcon", OSDarkLAFUtils.loadImageResource("/icons/FrameResize.png"));
        uiDefaults.put("InternalFrame.OSDarkLAFResizeIconRoll", OSDarkLAFUtils.loadImageResource("/icons/FrameResizeRoll.png"));
        uiDefaults.put("InternalFrame.OSDarkLAFResizeIconPush", OSDarkLAFUtils.loadImageResource("/icons/FrameResizePush.png"));

        uiDefaults.put("InternalFrame.closeIcon", OSDarkLAFIconFactory.getFrameCloseIcon());
        uiDefaults.put("InternalFrame.minimizeIcon", OSDarkLAFIconFactory.getFrameAltMaximizeIcon());
        uiDefaults.put("InternalFrame.maximizeIcon", OSDarkLAFIconFactory.getFrameMaxIcon());
        uiDefaults.put("InternalFrame.iconifyIcon", OSDarkLAFIconFactory.getFrameMinIcon());
        uiDefaults.put("InternalFrame.icon", OSDarkLAFUtils.loadImageResource("/icons/Frame.png"));
        uiDefaults.put("OSDarkLAFInternalFrameIconLit.width", Integer.valueOf(20));
        uiDefaults.put("OSDarkLAFInternalFrameIconLit.height", Integer.valueOf(20));

        final Font fontIcon = ((Font)uiDefaults.get("InternalFrame.titleFont")).deriveFont(Font.BOLD);
        uiDefaults.put("DesktopIcon.font", fontIcon);
        uiDefaults.put("OSDarkLAFDesktopIcon.width", Integer.valueOf(80));
        uiDefaults.put("OSDarkLAFDesktopIcon.height", Integer.valueOf(60));
        uiDefaults.put("OSDarkLAFDesktopIconBig.width", Integer.valueOf(48));
        uiDefaults.put("OSDarkLAFDesktopIconBig.height", Integer.valueOf(48));

        uiDefaults.put("InternalFrame.activeTitleBackground", getMenuSelectedBackground());
        uiDefaults.put("InternalFrame.activeTitleGradient", getMenuSelectedBackground().darker());
        uiDefaults.put("InternalFrame.inactiveTitleBackground", getMenuBackground().brighter());
        uiDefaults.put("InternalFrame.inactiveTitleGradient", getMenuBackground().darker());
    }
}

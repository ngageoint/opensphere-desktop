package io.opensphere.controlpanels.recording.gif;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.ScrollPaneLayout;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.NumberFormatter;

import org.apache.log4j.Logger;

import io.opensphere.controlpanels.recording.gif.AnimationOptions.ResizeOption;
import io.opensphere.core.capture.CaptureDialog;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.filesystem.MnemonicFileChooser;

/** Class that creates GIF file selector and presents GIF options. */
public class GifFileChooser extends MnemonicFileChooser
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(GifFileChooser.class);

    /** The system shadow color. */
    private static final Color ourSystem3dObjShadowColor = (Color)Toolkit.getDefaultToolkit()
            .getDesktopProperty("win.3d.shadowColor");

    /** The system foreground color. */
    private static final Color ourSystemTextFrgndColor = Color.WHITE;

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The current height of the window. */
    private final int myCurrentHeight;

    /** The current width of the window. */
    private final int myCurrentWidth;

    /** The panel of GIF options. */
    private final GIFOptions myOptionsPanel;

    /** The ui registry. */
    private final UIRegistry myUIRegistry;

    /**
     * Default constructor.
     *
     * @param uiRegistry The ui registry.
     * @param currentWidth The current width of the window.
     * @param currentHeight The current height of the window.
     * @param prefRegistry The preferences registry.
     */
    public GifFileChooser(UIRegistry uiRegistry, int currentWidth, int currentHeight, PreferencesRegistry prefRegistry)
    {
        super(prefRegistry, "GifFileChooser");
        myUIRegistry = uiRegistry;
        myCurrentWidth = currentWidth;
        myCurrentHeight = currentHeight;
        FileNameExtensionFilter filter = new FileNameExtensionFilter("GIF File (*.gif)", "gif");
        setFileFilter(filter);

        myOptionsPanel = new GIFOptions(this);
        setAccessory(myOptionsPanel);
    }

    /**
     * Display the GIF file saving dialog and options.
     *
     * @return The user selected options.
     */
    public AnimationOptions showFileDialog()
    {
        AnimationOptions options = null;

        int returnVal = showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            options = new AnimationOptions();

            File file = getSelectedFile();
            // Check that file ends with ".gif"
            String path = file.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".gif"))
            {
                path = file.getAbsolutePath() + ".gif";
                file = new File(path);
            }
            // Check that file doesn't already exist
            if (file.exists())
            {
                int choice = JOptionPane.showConfirmDialog(this, "Overwrite the file: " + path + "?", "Gif Recording",
                        JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.NO_OPTION)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Cancelling overwrite of existing GIF file");
                    }
                    return null;
                }
            }

            // Now determine what GIF options are selected.
            options.setFile(file);
            options.setResizeOption(myOptionsPanel.getResizeOption());
            options.setWidth(myOptionsPanel.getGIFWidth());
            options.setHeight(myOptionsPanel.getGIFHeight());
            options.setOriginalWidth(myCurrentWidth);
            options.setOriginalHeight(myCurrentHeight);
            options.setHiddenComponents(myOptionsPanel.getFramesToHide());
        }
        else if (returnVal == JFileChooser.CANCEL_OPTION)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Cancelling GIF animation file selection.");
            }
            return options;
        }

        return options;
    }

    /**
     * Panel for displaying frames that may or may not be included in GIF
     * images.
     */
    public static class FrameSelectorPanel extends JPanel
    {
        /** serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The panel that contains the frames to show/hide in GIF image. */
        private final JPanel mySelectableFramesPanel;

        /**
         * Constructor.
         *
         * @param uiRegistry The UI registry.
         * @param name The name of this frame.
         */
        public FrameSelectorPanel(UIRegistry uiRegistry, String name)
        {
            setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ourSystem3dObjShadowColor, 1), name,
                    TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12),
                    ourSystemTextFrgndColor));

            Predicate<String> selectedPredicate = new Predicate<String>()
            {
                @Override
                public boolean test(String text)
                {
                    return "Timeline".equals(text);
                }
            };
            mySelectableFramesPanel = CaptureDialog.showInternalFrames(uiRegistry.getComponentRegistry().getObjects(),
                    selectedPredicate);

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            setPreferredSize(new Dimension(230, 150));
            setMaximumSize(new Dimension(230, 1500));

            JLabel descriptionFirst = new JLabel("Select components to display");

            JLabel descriptionSecond = new JLabel("during animation");

            JScrollPane scrollPane = new JScrollPane(mySelectableFramesPanel);

            add(descriptionFirst);
            add(descriptionSecond);

            add(Box.createVerticalStrut(5));

            add(scrollPane);
        }

        /**
         * Get the list of frame names that are not selected.
         *
         * @return The list of names.
         */
        public List<String> getHiddenFrames()
        {
            List<String> frameNames = new ArrayList<>();

            Component[] childComponents = mySelectableFramesPanel.getComponents();
            for (Component component : childComponents)
            {
                if (component instanceof JCheckBox)
                {
                    JCheckBox checkBox = (JCheckBox)component;
                    if (!checkBox.isSelected())
                    {
                        frameNames.add(checkBox.getText());
                    }
                }
            }
            return frameNames;
        }
    }

    /** Helper class to display extra GIF options. */
    public class GIFOptions extends JScrollPane
    {
        /** serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The frame selector panel. */
        private final FrameSelectorPanel myFrameSelectorPanel;

        /** The size selector panel. */
        private final SizeSelectorPanel mySizeSelectorPanel;

        /**
         * Constructor.
         *
         * @param jfc The file chooser.
         */
        public GIFOptions(JFileChooser jfc)
        {
            mySizeSelectorPanel = new SizeSelectorPanel("GIF Size Selector");

            myFrameSelectorPanel = new FrameSelectorPanel(myUIRegistry, "Component Selector");

            setLayout(new ScrollPaneLayout());

            setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

            setPreferredSize(new Dimension(250, 370));

            Box vertBox = Box.createVerticalBox();

            Box sizeBox = Box.createHorizontalBox();
            sizeBox.add(mySizeSelectorPanel);
            sizeBox.add(Box.createHorizontalGlue());

            Box frameBox = Box.createHorizontalBox();
            frameBox.add(myFrameSelectorPanel);
            frameBox.add(Box.createHorizontalGlue());

            vertBox.add(sizeBox);
            vertBox.add(frameBox);

            setViewportView(vertBox);
        }

        /**
         * Get the list of frame names that should be hidden in GIF.
         *
         * @return List of names.
         */
        public List<String> getFramesToHide()
        {
            return myFrameSelectorPanel.getHiddenFrames();
        }

        /**
         * Get the user selected height.
         *
         * @return The height.
         */
        public int getGIFHeight()
        {
            return mySizeSelectorPanel.getGIFHeight();
        }

        /**
         * Get the user selected width.
         *
         * @return The width.
         */
        public int getGIFWidth()
        {
            return mySizeSelectorPanel.getGIFWidth();
        }

        /**
         * Get the resizeOption.
         *
         * @return the resizeOption
         */
        public ResizeOption getResizeOption()
        {
            return mySizeSelectorPanel.getResizeOption();
        }
    }

    /** Class that represents GIF size selection panel. */
    public class SizeSelectorPanel extends JPanel
    {
        /** serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /**
         * Selection for either resizing or compressing the image to match the
         * selected size.
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        private final JComboBox myCompressResizeSelector = new JComboBox(
                new ResizeOption[] { ResizeOption.RESIZE, ResizeOption.COMPRESS });

        /** The custom height field. */
        private JFormattedTextField myCustomHeightField;

        /** The custom width field. */
        private JFormattedTextField myCustomWidthField;

        /** The formatter for custom width/height input. */
        private final NumberFormatter myFormatter;

        /** The user selected height. */
        private int myHeight;

        /** The user selected width. */
        private int myWidth;

        /**
         * Constructor.
         *
         * @param name The name of the panel (to be displayed within border).
         */
        public SizeSelectorPanel(String name)
        {
            setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ourSystem3dObjShadowColor, 1), name,
                    TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12),
                    ourSystemTextFrgndColor));

            myFormatter = new NumberFormatter();
            myFormatter.setMinimum(Integer.valueOf(50));
            myFormatter.setMaximum(Integer.valueOf(2000));
            myFormatter.setValueClass(Integer.class);

            JRadioButton currentSizeButton = new JRadioButton("Current Size (" + myCurrentWidth + ", " + myCurrentHeight + ")");
            currentSizeButton.setPreferredSize(new Dimension(230, 25));
            currentSizeButton.addActionListener(createSizeButtonListener(false, myCurrentWidth, myCurrentHeight));

            JRadioButton powerPointSizeButton = new JRadioButton("PowerPoint (930x595)");
            powerPointSizeButton.setPreferredSize(new Dimension(230, 25));
            powerPointSizeButton.addActionListener(createSizeButtonListener(false, 930, 595));

            JRadioButton customSizeButton = new JRadioButton("Custom");
            customSizeButton.setPreferredSize(new Dimension(230, 25));
            customSizeButton.addActionListener(createSizeButtonListener(true, 800, 600));

            // Create the radio buttons
            ButtonGroup sizeGroup = new ButtonGroup();
            sizeGroup.add(currentSizeButton);
            sizeGroup.add(powerPointSizeButton);
            sizeGroup.add(customSizeButton);

            // Create Boxes that will hold radio buttons.
            Box screenSizeBox = createButtonBox(currentSizeButton);
            Box pageSizeBox = createButtonBox(powerPointSizeButton);
            Box customSizeBox = createButtonBox(customSizeButton);

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            setPreferredSize(new Dimension(230, 180));
            setMaximumSize(new Dimension(230, 1000));

            add(myCompressResizeSelector);
            add(screenSizeBox);
            add(pageSizeBox);
            add(customSizeBox);
            add(createCustomWidthBox());
            add(createCustomHeightBox());

            currentSizeButton.doClick();
        }

        /**
         * Get the user selected height.
         *
         * @return The height.
         */
        public int getGIFHeight()
        {
            if (myCustomHeightField.isEnabled())
            {
                try
                {
                    Integer value = (Integer)myFormatter.stringToValue(myCustomHeightField.getText());
                    return value.intValue();
                }
                catch (ParseException e)
                {
                    LOGGER.error("Unable to parse custom height value: " + e.getMessage());
                }
            }
            return myHeight;
        }

        /**
         * Get the user selected width.
         *
         * @return The width.
         */
        public int getGIFWidth()
        {
            if (myCustomWidthField.isEnabled())
            {
                try
                {
                    Integer value = (Integer)myFormatter.stringToValue(myCustomWidthField.getText());
                    return value.intValue();
                }
                catch (ParseException e)
                {
                    LOGGER.error("Unable to parse custom width value: " + e.getMessage());
                }
            }
            return myWidth;
        }

        /**
         * Get the resizeOption.
         *
         * @return the resizeOption
         */
        public ResizeOption getResizeOption()
        {
            return (ResizeOption)myCompressResizeSelector.getSelectedItem();
        }

        /**
         * Create a box to hold the button.
         *
         * @param button The button which goes in the box.
         * @return The newly created box.
         */
        private Box createButtonBox(JRadioButton button)
        {
            Box buttonBox = Box.createHorizontalBox();
            buttonBox.add(Box.createHorizontalStrut(10));
            buttonBox.add(button);
            buttonBox.add(Box.createHorizontalGlue());
            return buttonBox;
        }

        /**
         * Create the box containing the custom height input.
         *
         * @return A Box.
         */
        private Box createCustomHeightBox()
        {
            JLabel heightLabel = new JLabel("Height:");
            heightLabel.setForeground(ourSystemTextFrgndColor);
            heightLabel.setPreferredSize(new Dimension(50, 30));

            JLabel heightPixelLabel = new JLabel("pixel");
            heightPixelLabel.setForeground(ourSystemTextFrgndColor);

            myCustomHeightField = new JFormattedTextField(myFormatter);
            myCustomHeightField.setPreferredSize(new Dimension(100, 30));
            Integer defaultHeight = Integer.valueOf(600);
            myCustomHeightField.setValue(defaultHeight);
            myCustomHeightField.setCaretColor(ourSystemTextFrgndColor);
            myCustomHeightField.setForeground(ourSystemTextFrgndColor);

            Box heightBox = Box.createHorizontalBox();
            heightBox.setPreferredSize(new Dimension(230, 30));
            heightBox.setMaximumSize(new Dimension(230, 30));
            heightBox.add(Box.createHorizontalStrut(25));
            heightBox.add(heightLabel);
            heightBox.add(Box.createHorizontalStrut(3));
            heightBox.add(myCustomHeightField);
            heightBox.add(Box.createHorizontalStrut(3));
            heightBox.add(heightPixelLabel);
            heightBox.add(Box.createHorizontalGlue());

            return heightBox;
        }

        /**
         * Create the box containing the custom width input.
         *
         * @return A Box.
         */
        private Box createCustomWidthBox()
        {
            JLabel widthLabel = new JLabel("Width:");
            widthLabel.setForeground(ourSystemTextFrgndColor);
            widthLabel.setPreferredSize(new Dimension(50, 30));

            JLabel widthPixelLabel = new JLabel("pixel");
            widthPixelLabel.setForeground(ourSystemTextFrgndColor);

            myCustomWidthField = new JFormattedTextField(myFormatter);
            myCustomWidthField.setPreferredSize(new Dimension(100, 30));
            Integer defaultWidth = Integer.valueOf(800);
            myCustomWidthField.setValue(defaultWidth);
            myCustomWidthField.setCaretColor(ourSystemTextFrgndColor);
            myCustomWidthField.setForeground(ourSystemTextFrgndColor);

            Box widthBox = Box.createHorizontalBox();
            widthBox.setPreferredSize(new Dimension(230, 30));
            widthBox.setMaximumSize(new Dimension(230, 30));
            widthBox.add(Box.createHorizontalStrut(25));
            widthBox.add(widthLabel);
            widthBox.add(Box.createHorizontalStrut(3));
            widthBox.add(myCustomWidthField);
            widthBox.add(Box.createHorizontalStrut(3));
            widthBox.add(widthPixelLabel);
            widthBox.add(Box.createHorizontalGlue());

            return widthBox;
        }

        /**
         * Create a listener which sets the currently selected width and height
         * and whether it allows custom settings when the button is pressed.
         *
         * @param custom When true the custom width and height boxes are
         *            enabled.
         * @param width The initial default width for this button.
         * @param height The initial default height for this button.
         * @return the newly created listener.
         */
        private ActionListener createSizeButtonListener(final boolean custom, final int width, final int height)
        {
            return new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent event)
                {
                    myCustomWidthField.setEnabled(custom);
                    myCustomHeightField.setEnabled(custom);
                    myWidth = width;
                    myHeight = height;
                }
            };
        }
    }
}

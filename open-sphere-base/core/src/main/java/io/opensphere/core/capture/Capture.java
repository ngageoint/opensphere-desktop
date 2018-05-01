package io.opensphere.core.capture;

import java.awt.Frame;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.hud.awt.HUDFrame;
import io.opensphere.core.util.concurrent.CommonTimer;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * The dialog for capturing the current view of the application.
 */
public class Capture extends JFrame
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(Capture.class);

    /**
     * The unique identifier used for serialization operations.
     */
    private static final long serialVersionUID = -2402191272504048432L;

    /** The area where the current image is displayed. */
    private final ImageArea myImageArea = new ImageArea();

    /**
     * A list of HUD components that hidden before capture and should be made
     * visible again upon completion..
     */
    private final List<HUDFrame> myRedisplayAfterCapture = new ArrayList<>();

    /**
     * To support the display of images that can't be fully displayed without
     * scrolling, the ImageArea component is placed into a JScrollPane.
     */
    private final JScrollPane myScrollPane;

    /**
     * Toolbox containing the geometry registry, map manager, control registry,
     * etc.
     */
    private final Toolbox myToolbox;

    /**
     * Create screen capture GUI.
     *
     * @param toolbox toolbox containing the geometry registry, map manager,
     *            control registry, etc.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public Capture(Toolbox toolbox)
    {
        super("Screen Capture");
        myToolbox = toolbox;
        Frame mainFrame = myToolbox.getUIRegistry().getMainFrameProvider().get();
        setIconImage(myToolbox.getUIRegistry().getMainFrameProvider().get().getIconImage());

        // Create the application's menus.
        createMenus();

        // Install a scrollable ImageArea component.
        myScrollPane = new JScrollPane(myImageArea);
        getContentPane().add(myScrollPane);

        // Size main window to half the screen's size, and center window.
        setSize(mainFrame.getWidth() / 2, mainFrame.getHeight() / 2);
        setLocationRelativeTo(mainFrame);
    }

    /**
     * Helper method to create file menu options.
     *
     * @return The File menu.
     */
    protected JMenu createFileMenu()
    {
        JMenu menu = new JMenu("File");
        JMenuItem mi = new JMenuItem("Save As...");
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK));
        mi.addActionListener(e -> captureScreen());
        menu.add(mi);

        menu.addSeparator();

        mi = new JMenuItem("Exit");
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.ALT_DOWN_MASK));
        mi.addActionListener(e -> dispose());
        menu.add(mi);

        return menu;
    }

    /**
     * Saves the current screen to file, if the image area has an image.
     */
    protected void captureScreen()
    {
        // Disallow image saving if there is no image to save.
        if (myImageArea.getImage() == null)
        {
            LOGGER.error("No captured image.");
        }
        else
        {
            // Save image to file.
            ScreenCaptureWriter fileCreator = new ScreenCaptureWriter(myImageArea, myToolbox.getPreferencesRegistry());
            fileCreator.writeToFile();
        }
    }

    /**
     * Create our menus.
     */
    protected void createMenus()
    {
        JMenuBar mb = new JMenuBar();

        mb.add(createFileMenu());

        mb.add(createSnapshotMenu());

        // Install menus.
        setJMenuBar(mb);
    }

    /**
     * Create and add Snapshot menu item.
     *
     * @return The snapshot menu.
     */
    protected JMenu createSnapshotMenu()
    {
        JMenu menu = new JMenu("Snapshot");
        JMenuItem mi = new JMenuItem("New");
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK));
        mi.addActionListener(e -> initCapture());
        menu.add(mi);

        mi = new JMenuItem("Crop");
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.ALT_DOWN_MASK));
        mi.addActionListener(e -> cropSnapshot());
        menu.add(mi);

        mi = new JMenuItem("Clear");
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK));
        mi.addActionListener(e -> myImageArea.resetBackgroundColor());
        menu.add(mi);

        return menu;
    }

    /**
     * Crops the image area, and adjusts the scroll bars if the crop operation
     * succeeds.
     */
    protected void cropSnapshot()
    {
        // Crop ImageArea component and adjust the scroll bars if cropping
        // succeeds.
        if (myImageArea.crop())
        {
            myScrollPane.getHorizontalScrollBar().setValue(0);
            myScrollPane.getVerticalScrollBar().setValue(0);
        }
        else
        {
            LOGGER.error("Out of bounds.");
        }
    }

    /**
     * Perform a screen capture.
     */
    protected void doScreenCapture()
    {
        myToolbox.getFrameBufferCaptureManager().getCaptureProvider().captureSingleFrame((width, height,
                screenCapture) -> EventQueueUtilities.invokeLater(() -> performCapture(width, height, screenCapture)));
    }

    /**
     * Initialize screen capture.
     */
    protected void initCapture()
    {
        List<HUDFrame> allFrames = myToolbox.getUIRegistry().getComponentRegistry().getObjects();

        final List<HUDFrame> visibleFrames = CaptureDialog.getVisibleFrames(allFrames);

        // Just take screen capture if no extra components to select.
        if (visibleFrames.isEmpty())
        {
            startSnapshotTimer();
            return;
        }

        // The dialog that displays HUD components to allow the user to select
        // which ones will be included in the captured image.
        CaptureDialog hudSelectionDialog = new CaptureDialog(this);

        // Set the new list of hud frames each time so the dialog builds the
        // correct set of check boxes then show the dialog
        hudSelectionDialog.setVisibleFrames(visibleFrames);
        hudSelectionDialog.setVisible(true);

        if (hudSelectionDialog.wasOKSelected())
        {
            Set<String> doNotHideFrameList = hudSelectionDialog.getSelectedList();

            // Hide Capture's main window and the HUD picker dialog
            // so that it does not appear in the screen capture.
            hudSelectionDialog.setVisible(false);

            // Hide all shown hud components
            for (HUDFrame frame : visibleFrames)
            {
                if (frame.getTitle() == null || frame.getTitle().isEmpty() || !doNotHideFrameList.contains(frame.getTitle()))
                {
                    myRedisplayAfterCapture.add(frame);
                    frame.setVisible(false);
                }
            }

            startSnapshotTimer();
        }
    }

    /**
     * Restore the HUD components that were removed before the screen capture.
     */
    protected void putFramesBack()
    {
        for (HUDFrame frame : myRedisplayAfterCapture)
        {
            frame.setVisible(true);
        }
        myRedisplayAfterCapture.clear();
    }

    /**
     * Start the snapshot timer. The timer delays the snapshot long enough for
     * the HUD windows to finish hiding..
     */
    protected void startSnapshotTimer()
    {
        CommonTimer.schedule(this::doScreenCapture, 100);
    }

    /**
     * Copies the supplied array (containing data captured from an OpenGL screen
     * capture) to the {@link #myImageArea} as a screen capture. Used as a
     * lambda.
     *
     * @param width the width of the area to capture.
     * @param height the height of the area to capture.
     * @param screenCapture the data captured from the screen.
     */
    protected void performCapture(final int width, final int height, final byte[] screenCapture)
    {
        putFramesBack();

        // The y direction for the GL canvas is the opposite of Swing, so we
        // must flip the image.
        byte[] flip = new byte[screenCapture.length];
        int lineByteWidth = width * 3;
        for (int i = 0; i < height; ++i)
        {
            System.arraycopy(screenCapture, (height - i - 1) * lineByteWidth, flip, i * lineByteWidth, lineByteWidth);
        }

        BufferedImage biScreen = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        WritableRaster wr = biScreen.getRaster();
        wr.setDataElements(0, 0, width, height, flip);
        // Update ImageArea component with the new image, and adjust the scroll
        // bars.
        myImageArea.setImage(biScreen);

        myScrollPane.getHorizontalScrollBar().setValue(0);
        myScrollPane.getVerticalScrollBar().setValue(0);
        requestFocus();
    }
}

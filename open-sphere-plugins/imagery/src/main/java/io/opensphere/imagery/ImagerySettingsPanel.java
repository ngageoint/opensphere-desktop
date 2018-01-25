package io.opensphere.imagery;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

/**
 * The Class ImagerySetingsPanel.
 */
@SuppressWarnings("serial")
public class ImagerySettingsPanel extends JPanel
{
    /** The Constant BUTTON_HEIGHT. */
    private static final int BUTTON_HEIGHT = 25;

    /** The Constant BUTTON_WIDTH. */
    private static final int BUTTON_WIDTH = 150;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ImagerySettingsPanel.class);

    /** The Center on group button. */
    private JButton myCenterOnGroupButton;

    /** The Clear cache button. */
    private JButton myClearCacheButton;

    /** The Controller. */
    private final transient ImageryFileSourceController myController;

    /** The Data group. */
    private final transient ImageryDataGroupInfo myDataGroup;

    /** The Manage images button. */
    private JButton myManageImagesButton;

    /** The Show boundary check box. */
    private JCheckBox myShowBoundaryCheckBox;

    /** The Zoom to group button. */
    private JButton myZoomToGroupButton;

    /**
     * Instantiates a new imagery setings panel.
     *
     * @param controller the controller
     * @param idgi the idgi
     */
    public ImagerySettingsPanel(ImageryFileSourceController controller, ImageryDataGroupInfo idgi)
    {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        myController = controller;
        myDataGroup = idgi;

        setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 20));
        add(Box.createVerticalGlue());

        add(createCenteredBox(getBoundaryOnStartCheckBox()));
        add(Box.createVerticalStrut(5));
        add(createCenteredBox(getClearCacheButton()));
        add(Box.createVerticalStrut(5));
        add(createCenteredBox(getZoomToGroupButton()));
        add(Box.createVerticalStrut(5));
        add(createCenteredBox(getCenterOnGroupButton()));
        add(Box.createVerticalStrut(5));
        add(createCenteredBox(getManageImagesButton()));
        add(Box.createVerticalGlue());

        getBoundaryOnStartCheckBox()
                .setSelected(myDataGroup != null && myDataGroup.getImagerySourceGroup().isShowBoundaryOnLoad());
    }

    /**
     * Center on group.
     */
    private void centerOnGroup()
    {
        if (myDataGroup != null)
        {
            myDataGroup.getImagerySourceGroup().centerOnGroup(myController.getToolbox(), false);
        }
    }

    /**
     * Clear image cache.
     */
    private void clearImageCache()
    {
        if (myDataGroup != null)
        {
            if (myDataGroup.getImagerySourceGroup().getImageryEnvoy() != null)
            {
                myDataGroup.getImagerySourceGroup().getImageryEnvoy().clearImageCache();
            }
            else
            {
                myDataGroup.getImagerySourceGroup().cleanCache(myController.getToolbox());
                LOGGER.info("Cache clean is complete.");
            }
        }
    }

    /**
     * Creates the centered box.
     *
     * @param c the c
     * @return the box
     */
    private Box createCenteredBox(Component c)
    {
        Box aBox = Box.createHorizontalBox();
        aBox.add(Box.createHorizontalGlue());
        aBox.add(c);
        aBox.add(Box.createHorizontalGlue());
        return aBox;
    }

    /**
     * Gets the boundary on start check box.
     *
     * @return the boundary on start check box
     */
    private JCheckBox getBoundaryOnStartCheckBox()
    {
        if (myShowBoundaryCheckBox == null)
        {
            myShowBoundaryCheckBox = new JCheckBox("Boundary On Start", false);
            myShowBoundaryCheckBox.setFocusPainted(false);
            myShowBoundaryCheckBox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    myDataGroup.getImagerySourceGroup().setShowBoundaryOnLoad(getBoundaryOnStartCheckBox().isSelected());
                    myController.saveConfigState();
                }
            });
        }
        return myShowBoundaryCheckBox;
    }

    /**
     * Gets the center on group button.
     *
     * @return the center on group button
     */
    private Component getCenterOnGroupButton()
    {
        if (myCenterOnGroupButton == null)
        {
            myCenterOnGroupButton = new JButton("Center On Group");
            myCenterOnGroupButton.setMinimumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
            myCenterOnGroupButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
            myCenterOnGroupButton.setFocusPainted(false);
            myCenterOnGroupButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    centerOnGroup();
                }
            });
        }
        return myCenterOnGroupButton;
    }

    /**
     * Gets the clear cache button.
     *
     * @return the clear cache button
     */
    private Component getClearCacheButton()
    {
        if (myClearCacheButton == null)
        {
            myClearCacheButton = new JButton("Clear Cache");
            myClearCacheButton.setMinimumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
            myClearCacheButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
            myClearCacheButton.setFocusPainted(false);
            myClearCacheButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    clearImageCache();
                }
            });
        }
        return myClearCacheButton;
    }

    /**
     * Gets the manage images button.
     *
     * @return the manage images button
     */
    private JButton getManageImagesButton()
    {
        if (myManageImagesButton == null)
        {
            myManageImagesButton = new JButton("Manage Images");
            myManageImagesButton.setMinimumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
            myManageImagesButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
            myManageImagesButton.setFocusPainted(false);
            myManageImagesButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    manageImages();
                }
            });
        }
        return myManageImagesButton;
    }

    /**
     * Gets the zoom to group button.
     *
     * @return the zoom to group button
     */
    private Component getZoomToGroupButton()
    {
        if (myZoomToGroupButton == null)
        {
            myZoomToGroupButton = new JButton("Zoom To Group");
            myZoomToGroupButton.setMinimumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
            myZoomToGroupButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
            myZoomToGroupButton.setFocusPainted(false);
            myZoomToGroupButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    zoomToGroup();
                }
            });
        }
        return myZoomToGroupButton;
    }

    /**
     * Manage images.
     */
    private void manageImages()
    {
        JDialog jd = new JDialog(myController.getToolbox().getUIRegistry().getMainFrameProvider().get(),
                "Imagery Group Image Manager", ModalityType.APPLICATION_MODAL);
        ImageryGroupImageManagerPanel panel = new ImageryGroupImageManagerPanel(this, myController, jd);
        panel.setFromSource(myDataGroup.getImagerySourceGroup());
        jd.setSize(600, 540);
        jd.setMinimumSize(new Dimension(600, 520));
        jd.setContentPane(panel);
        jd.setLocationRelativeTo(myController.getToolbox().getUIRegistry().getMainFrameProvider().get());
        jd.setVisible(true);
    }

    /**
     * Zoom to group.
     */
    private void zoomToGroup()
    {
        if (myDataGroup != null)
        {
            myDataGroup.getImagerySourceGroup().zoomToGroup(myController.getToolbox());
        }
    }
}

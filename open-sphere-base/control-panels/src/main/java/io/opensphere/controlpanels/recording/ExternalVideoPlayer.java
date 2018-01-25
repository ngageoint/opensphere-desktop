package io.opensphere.controlpanels.recording;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JFrame;

import io.opensphere.core.util.swing.EventQueueUtilities;

/** A video player which opens in a separate frame. */
public class ExternalVideoPlayer extends AbstractVideoPlayer
{
    /** The latest image to be rendered. */
    private BufferedImage myLatestImage;

    /** The frame which contains the video. */
    private JFrame myVideoScreen;

    @Override
    void closeMedium()
    {
        if (myVideoScreen != null)
        {
            myVideoScreen.dispose();
            myVideoScreen = null;
        }
    }

    @Override
    void initializeMedium(final int width, final int height)
    {
        if (myVideoScreen == null)
        {
            myVideoScreen = new JFrame();
            myVideoScreen.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            JComponent pane = new JComponent()
            {
                /** Serial version UID. */
                private static final long serialVersionUID = 1L;

                @Override
                protected void paintComponent(Graphics g)
                {
                    super.paintComponent(g);

                    ((Graphics2D)g).drawImage(myLatestImage, 0, 0, this);
                }
            };
            pane.setPreferredSize(new Dimension(width, height));

            myVideoScreen.getContentPane().add(pane);
            myVideoScreen.pack();
            myVideoScreen.setVisible(true);
            myVideoScreen.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
    }

    @Override
    void updateMedium(final BufferedImage image)
    {
        EventQueueUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                myLatestImage = image;
                myVideoScreen.repaint();
            }
        });
    }
}

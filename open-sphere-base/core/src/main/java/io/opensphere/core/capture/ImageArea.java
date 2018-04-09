package io.opensphere.core.capture;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;

import javax.swing.JPanel;

/**
 * This class defines a specialized panel for displaying a captured image.
 */
public class ImageArea extends JPanel
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * The Background color. Preserve the background color to clear the panel
     * when necessary.
     */
    private final Color myBackgroundColor;

    /**
     * Stroke-defined outline of selection rectangle.
     */
    private final BasicStroke myBasicStroke;

    /**
     * Latest mouse X coordinate during drag operation.
     */
    private int myDestX;

    /**
     * Latest mouse Y coordinate during drag operation.
     */
    private int myDestY;

    /**
     * A gradient paint is used to create a distinctive-looking selection
     * rectangle outline.
     */
    private final GradientPaint myGradientPaint;

    /**
     * Displayed image's Image object, which is actually a BufferedImage.
     */
    private Image myImage;

    /**
     * Location and extents of selection rectangle.
     */
    private final Rectangle myRectSelection;

    /**
     * X Mouse coordinate when mouse button pressed.
     */
    private int mySrcX;

    /**
     * Y Mouse coordinate when mouse button pressed.
     */
    private int mySrcY;

    /**
     * Construct an ImageArea component.
     */
    public ImageArea()
    {
        myBackgroundColor = getBackground();

        // Create a selection Rectangle. It's better to create one Rectangle
        // here than a Rectangle each time paintComponent() is called, to reduce
        // unnecessary object creation.
        myRectSelection = new Rectangle();

        // Define the stroke for drawing selection rectangle outline.
        myBasicStroke = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] { 12, 12 }, 0);

        // Define the gradient paint for coloring selection rectangle outline.
        myGradientPaint = new GradientPaint(0.0f, 0.0f, Color.red, 1.0f, 1.0f, Color.white, true);

        // Install a mouse listener that sets things up for a selection drag.
        MouseListener ml;
        ml = new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                // When you start Capture, there is no captured image.
                // Therefore, it makes no sense to try and select a subimage.
                // This is the reason for the if (image == null) test.
                if (myImage == null)
                {
                    return;
                }

                myDestX = mySrcX = e.getX();
                myDestY = mySrcY = e.getY();

                repaint();
            }
        };
        addMouseListener(ml);

        // Install a mouse motion listener to update the selection rectangle
        // during drag operations.

        MouseMotionListener mml;
        mml = new MouseMotionAdapter()
        {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                // When you start Capture, there is no captured image.
                // Therefore, it makes no sense to try and select a
                // subimage. This is the reason for the if (image == null)
                // test.
                if (myImage == null)
                {
                    return;
                }

                myDestX = e.getX();
                myDestY = e.getY();

                repaint();
            }
        };
        addMouseMotionListener(mml);
    }

    /**
     * Crop the image to the dimensions of the selection rectangle.
     *
     * @return true if cropping succeeded
     */

    public boolean crop()
    {
        // There is nothing to crop if the selection rectangle is only a single
        // point.
        if (mySrcX == myDestX && mySrcY == myDestY)
        {
            return true;
        }

        // Assume success.
        boolean succeeded = true;

        /* Compute upper-left and lower-right coordinates for selection
         * rectangle corners. */
        int x1 = mySrcX < myDestX ? mySrcX : myDestX;
        int y1 = mySrcY < myDestY ? mySrcY : myDestY;

        int x2 = mySrcX > myDestX ? mySrcX : myDestX;
        int y2 = mySrcY > myDestY ? mySrcY : myDestY;

        // Compute width and height of selection rectangle.
        int width = x2 - x1 + 1;
        int height = y2 - y1 + 1;

        // Create a buffer to hold cropped image.
        BufferedImage biCrop = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = biCrop.createGraphics();

        // Perform the crop operation.
        try
        {
            BufferedImage bi = (BufferedImage)myImage;
            BufferedImage bi2 = bi.getSubimage(x1, y1, width, height);
            g2d.drawImage(bi2, null, 0, 0);
        }
        catch (RasterFormatException e)
        {
            succeeded = false;
        }

        g2d.dispose();

        if (succeeded)
        {
            // Implicitly remove selection rectangle.
            setImage(biCrop);
        }
        else
        {
            // Prepare to remove selection rectangle.
            mySrcX = myDestX;
            mySrcY = myDestY;

            // Explicitly remove selection rectangle.
            repaint();
        }
        return succeeded;
    }

    /**
     * Return the current image.
     *
     * @return Image reference to current image
     */
    public Image getImage()
    {
        return myImage;
    }

    /**
     * Repaint the ImageArea with the current image's pixels.
     *
     * @param g graphics context
     */
    @Override
    public void paintComponent(Graphics g)
    {
        // Repaint the component's background.
        super.paintComponent(g);

        // If an image has been defined, draw that image using the Component
        // layer of this ImageArea object as the ImageObserver.
        if (myImage != null)
        {
            g.drawImage(myImage, 0, 0, this);
        }

        // Draw the selection rectangle if present.
        if (mySrcX != myDestX || mySrcY != myDestY)
        {
            // Compute upper-left and lower-right coordinates for selection
            // rectangle corners.
            int x1 = mySrcX < myDestX ? mySrcX : myDestX;
            int y1 = mySrcY < myDestY ? mySrcY : myDestY;

            int x2 = mySrcX > myDestX ? mySrcX : myDestX;
            int y2 = mySrcY > myDestY ? mySrcY : myDestY;

            // Establish selection rectangle origin.
            myRectSelection.x = x1;
            myRectSelection.y = y1;

            // Establish selection rectangle extents.
            myRectSelection.width = x2 - x1 + 1;
            myRectSelection.height = y2 - y1 + 1;

            // Draw selection rectangle.
            Graphics2D g2d = (Graphics2D)g;
            g2d.setStroke(myBasicStroke);
            g2d.setPaint(myGradientPaint);
            g2d.draw(myRectSelection);
        }
    }

    /**
     * Reset the background color.
     */
    public void resetBackgroundColor()
    {
        setImage(null);
        setPreferredSize(new Dimension(0, 0));
        setBackground(myBackgroundColor);
    }

    /**
     * Establish a new image and update the display.
     *
     * @param image new image's Image reference
     */
    public void setImage(Image image)
    {
        // Save the image for later repaint.
        myImage = image;

        if (myImage != null)
        {
            // Set this panel's preferred size to the image's size, to influence
            // the display of scrollbars.
            setPreferredSize(new Dimension(image.getWidth(this), image.getHeight(this)));

            // Prepare to remove any selection rectangle.
            mySrcX = myDestX;
            mySrcY = myDestY;
        }

        // Update the image displayed on the panel.
        revalidate();
        repaint();
    }
}

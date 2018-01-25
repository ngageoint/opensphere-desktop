package io.opensphere.core.util.image;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXImagePanel;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntProcedure;
import io.opensphere.core.util.lang.Pair;

/**
 * A panel which previews a single image at a time, but allows scrolling through
 * the images. When scrolling will wrap bottom to top and top to bottom.
 */
public class ImagePreviewPanel extends JPanel
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ImagePreviewPanel.class);

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * The container for the controls which allow switching the currently viewed
     * image.
     */
    private final JComponent myControls;

    /** The panel which shows the image currently being previewed. */
    private final JXImagePanel myCurrentImage = new JXImagePanel();

    /** The index of the image currently being previewed. */
    private int myCurrentIndex = -1;

    /** Label for the name or ID of the image. */
    private final JLabel myImageLabel = new JLabel();

    /**
     * The map of image index to the associated image. These images are kept
     * unscaled to allow the panel to be resized and retain as much accuracy as
     * possible.
     */
    private final TIntObjectMap<Pair<String, BufferedImage>> myImages = new TIntObjectHashMap<>();

    /** A label to indicate which image is currently being previewed. */
    private final JLabel myIndexLabel = new JLabel();

    /**
     * A lock to manage concurrent changes to which image is being previewed.
     */
    private final Lock myIndexLock = new ReentrantLock();

    /** The largest index for which I have an image. */
    private int myMaxIndex;

    /** The smallest index for which I have an image. */
    private int myMinIndex;

    /**
     * Constructor.
     *
     * @param width The desired width of the preview panel.
     * @param height The desired height of the preview panel.
     * @param usesLabels When true display a label under each image.
     */
    public ImagePreviewPanel(int width, int height, boolean usesLabels)
    {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(width, height));
        setMinimumSize(new Dimension(width, height));
        add(getImageContainer(usesLabels), BorderLayout.CENTER);
        myControls = createControls();
        add(myControls, BorderLayout.SOUTH);
        myControls.setVisible(false);
    }

    /**
     * Add an image which can be previewed.
     *
     * @param index The index of the image.
     * @param image The image. This image will be resized to fit the current
     *            panel size.
     * @param makeCurrent When true make this image the current image being
     *            previewed.
     */
    public void addImageAt(int index, BufferedImage image, boolean makeCurrent)
    {
        addImageAt(index, null, image, makeCurrent);
    }

    /**
     * Add an image which can be previewed.
     *
     * @param index The index of the image.
     * @param label The label associated with the image, if any.
     * @param image The image. This image will be resized to fit the current
     *            panel size.
     * @param makeCurrent When true make this image the current image being
     *            previewed.
     */
    public void addImageAt(int index, String label, BufferedImage image, boolean makeCurrent)
    {
        myIndexLock.lock();
        try
        {
            myMinIndex = Math.min(myMinIndex, index);
            myMaxIndex = Math.max(myMaxIndex, index);
            myImages.put(index, new Pair<String, BufferedImage>(label, image));
            if (makeCurrent || myCurrentIndex == -1)
            {
                myControls.setVisible(true);
                myCurrentIndex = index;
                myImageLabel.setText(label);
                Image scaled = ImageUtil.scaleDownImage(image, myCurrentImage.getHeight(), myCurrentImage.getWidth());
                myCurrentImage.setImage(scaled);
            }
            else if (myCurrentIndex == index)
            {
                myImageLabel.setText(label);
            }

            setIndexLabel();
        }
        finally
        {
            myIndexLock.unlock();
        }
    }

    /**
     * Map the image at the given index to the new index. If there is already an
     * image at the new index, it will be replaced.
     *
     * @param originalIndex the index at which the image currently resides.
     * @param newIndex The index at which the image will reside.
     */
    public void remapIndex(int originalIndex, int newIndex)
    {
        myIndexLock.lock();
        try
        {
            Pair<String, BufferedImage> image = myImages.remove(originalIndex);
            if (image != null)
            {
                myImages.put(newIndex, image);
                if (myCurrentIndex == originalIndex)
                {
                    myCurrentIndex = newIndex;
                }

                determineIndexBounds();
                setIndexLabel();
            }
            else
            {
                LOGGER.warn("Failed to map image at index " + originalIndex + " to " + newIndex
                        + " because no image exists at that index.");
            }
        }
        finally
        {
            myIndexLock.unlock();
        }
    }

    /**
     * Remove the image at the specified index.
     *
     * @param index The index at which the image should be removed.
     * @param shiftIndices When true, move all of the indices greater than the
     *            given index down by exactly one. If there are gaps in the
     *            indices, the gaps will remain in place.
     */
    public void removeImageAt(int index, boolean shiftIndices)
    {
        myIndexLock.lock();
        try
        {
            if (myImages.remove(index) != null)
            {
                if (myImages.isEmpty())
                {
                    myImageLabel.setText(null);
                    myCurrentImage.setImage(null);
                    myCurrentIndex = -1;
                    myMaxIndex = 0;
                    myMinIndex = 0;
                    myIndexLabel.setText(null);
                    myControls.setVisible(false);
                }
                else
                {
                    if (myCurrentIndex == index)
                    {
                        setToNextImage(myCurrentIndex != myMaxIndex);
                    }

                    if (shiftIndices)
                    {
                        for (int i = index; i < myMaxIndex; ++i)
                        {
                            remapIndex(i + 1, i);
                        }
                    }
                }
            }
            else
            {
                LOGGER.warn("Failed to remove image at " + index + ". No such image.");
            }
        }
        finally
        {
            myIndexLock.unlock();
        }
    }

    @Override
    public void setBounds(int x, int y, int width, int height)
    {
        myIndexLock.lock();
        try
        {
            super.setBounds(x, y, width, height);
            Pair<String, BufferedImage> image = myImages.get(myCurrentIndex);
            if (image != null)
            {
                myImageLabel.setText(image.getFirstObject());
                Image scaled = ImageUtil.scaleDownImage(image.getSecondObject(), myCurrentImage.getHeight(),
                        myCurrentImage.getWidth());
                myCurrentImage.setImage(scaled);
            }
        }
        finally
        {
            myIndexLock.unlock();
        }
    }

    /**
     * Create the component which contains the controls for changing which image
     * is being previewed.
     *
     * @return The controls for the preview panel.
     */
    private JComponent createControls()
    {
        Box controls = Box.createHorizontalBox();

        controls.add(Box.createHorizontalGlue());

        JButton decrement = new JButton("<<");
        decrement.setFocusPainted(false);
        decrement.setBorder(null);
        decrement.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setToNextImage(false);
            }
        });
        controls.add(decrement);

        controls.add(Box.createHorizontalStrut(3));
        controls.add(myIndexLabel);
        controls.add(Box.createHorizontalStrut(3));

        JButton increment = new JButton(">>");
        increment.setFocusPainted(false);
        increment.setBorder(null);
        increment.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setToNextImage(true);
            }
        });
        controls.add(increment);

        controls.add(Box.createHorizontalGlue());

        Box container = Box.createVerticalBox();
        container.add(Box.createVerticalStrut(5));
        container.add(controls);
        return container;
    }

    /** Determine the minimum and maximum indices. */
    private void determineIndexBounds()
    {
        myMaxIndex = 0;
        myMinIndex = 0;
        myImages.forEachKey(new TIntProcedure()
        {
            @Override
            public boolean execute(int value)
            {
                myMaxIndex = Math.max(myMaxIndex, value);
                myMinIndex = Math.min(myMinIndex, value);
                return true;
            }
        });
    }

    /**
     * Get the container which houses the image.
     *
     * @param usesLabels When true include a label below the image.
     * @return The newly created container.
     */
    private JComponent getImageContainer(boolean usesLabels)
    {
        Box box = Box.createVerticalBox();
        box.add(myCurrentImage);
        if (usesLabels)
        {
            Box labelBox = Box.createHorizontalBox();
            labelBox.add(Box.createHorizontalGlue());
            labelBox.add(myImageLabel);
            labelBox.add(Box.createHorizontalGlue());
            box.add(labelBox);
        }
        return box;
    }

    /**
     * Update the label to correctly identify the index of the currently viewed
     * image.
     */
    private void setIndexLabel()
    {
        int imageCount = 1;
        for (int i = myMinIndex; i < myMaxIndex; ++i)
        {
            if (myImages.get(i) != null)
            {
                if (myCurrentIndex == i)
                {
                    break;
                }
                ++imageCount;
            }
        }

        StringBuilder labelText = new StringBuilder();
        labelText.append(imageCount).append(" of ").append(myImages.size());
        myIndexLabel.setText(labelText.toString());
    }

    /**
     * Changed the previewed image to be the next image by either incrementing
     * or decrementing. This wraps top to bottom and bottom to top.
     *
     * @param increment When true, change to the next image in the positive
     *            direction. Otherwise, change to the next image in the negative
     *            direction.
     */
    private void setToNextImage(boolean increment)
    {
        myIndexLock.lock();
        try
        {
            if (increment ? myCurrentIndex < myMaxIndex : myCurrentIndex > myMinIndex)
            {
                int searchIndex = increment ? myCurrentIndex + 1 : myCurrentIndex - 1;
                while (increment ? searchIndex <= myMaxIndex : searchIndex >= myMinIndex)
                {
                    Pair<String, BufferedImage> image = myImages.get(searchIndex);
                    if (image != null)
                    {
                        myImageLabel.setText(image.getFirstObject());
                        Image scaled = ImageUtil.scaleDownImage(image.getSecondObject(), myCurrentImage.getHeight(),
                                myCurrentImage.getWidth());
                        myCurrentImage.setImage(scaled);
                        myCurrentIndex = searchIndex;
                        setIndexLabel();
                        break;
                    }
                    searchIndex = increment ? ++searchIndex : --searchIndex;
                }
            }
            else
            {
                int index = increment ? myMinIndex : myMaxIndex;
                Pair<String, BufferedImage> image = myImages.get(index);
                if (image != null)
                {
                    myImageLabel.setText(image.getFirstObject());
                    Image scaled = ImageUtil.scaleDownImage(image.getSecondObject(), myCurrentImage.getHeight(),
                            myCurrentImage.getWidth());
                    myCurrentImage.setImage(scaled);
                    myCurrentIndex = index;
                    setIndexLabel();
                }
            }
        }
        finally
        {
            myIndexLock.unlock();
        }
    }
}

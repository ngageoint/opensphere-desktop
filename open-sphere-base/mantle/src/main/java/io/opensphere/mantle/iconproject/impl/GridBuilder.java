package io.opensphere.mantle.iconproject.impl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.image.ImageUtil;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.impl.gui.IconChooserPanel.RecButton;
import io.opensphere.mantle.icon.impl.gui.IconChooserPanel.RecordImageIcon;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class GridBuilder
{
    private AnchorPane myGridPanel = new AnchorPane();

    private class BuildIconGridWorker implements Runnable
    {
        /** The Can interrupt. */
        private final boolean myCanInterrupt;

        /** The Interrupted. */
        private boolean myInterrupted;

        /** The Rec list. */
        private final List<IconRecord> myRecList;

        private Object myIconRegistry;

        /**
         * Instantiates a new builds the icon grid worker.
         *
         * @param recList the rec list
         * @param canCancel the can cancel
         */
        public BuildIconGridWorker(List<IconRecord> recList, boolean canCancel)
        {
            myRecList = recList;
            myCanInterrupt = canCancel;
        }

        @Override
        public void run()
        {
            int tileWidth = 100;
            int borderSize = 6;
            int iconWidth = tileWidth - borderSize;
            double width = myGridPanel.getWidth();
            if (width < 0 || width > 5000)
            {
                width = 400;
            }

            double height = myGridPanel.getHeight();
            if (height < 0 || height > 5000)
            {
                height = 400;
            }
            int numIconRowsInView = (int)Math.ceil((double)height / (double)tileWidth);
            GridPane grid = new GridPane();
            if (!isInterrupted() && !myRecList.isEmpty())
            {
                List<RecordImageIcon> imIcList = buildImageList(iconWidth);

                if (!isInterrupted())
                {
                    double numIconsPerRow = tileWidth > width ? 1 : (int)Math.floor((double)width / (double)tileWidth);
                    double numRows = (int)Math.ceil((double)imIcList.size() / (double)numIconsPerRow);

                    grid = new GridPane();
                    grid.setLayoutY((double)numRows < numIconRowsInView ? numIconRowsInView : numRows);
                    grid.setLayoutX(numIconsPerRow);
                    Dimension size = new Dimension(iconWidth, iconWidth);

                    for (int i = 0; i < imIcList.size() && !isInterrupted(); i++)
                    {
                        RecordImageIcon rec = imIcList.get(i);
                        AnchorPane imageBT = buildRecButton(size, rec);
                        grid.getChildren().add(imageBT);
                    }
                    double blankGridLements = numIconsPerRow * numIconRowsInView - imIcList.size();
                    for (int i = 0; i < blankGridLements; i++)
                    {
                        grid.getChildren().add(new HBox());
                    }
                }
            }
            if (!isInterrupted())
            {
                final GridPane fGrid = grid;
                EventQueueUtilities.runOnEDT(() ->
                {

                    myGridPanel.getChildren().clear();
                    ;
                    myGridPanel.getChildren().add(new GridPane());
                    myGridPanel.requestLayout();

                });
            }
        }

        /**
         * Checks if is interrupted.
         *
         * @return true, if is interrupted
         */
        boolean isInterrupted()
        {
            if (!myInterrupted && myCanInterrupt)
            {
                myInterrupted = Thread.interrupted();
            }
            return myInterrupted;
        }

        /**
         * Builds the image list.
         *
         * @param iconWidth the icon width
         * @return the list
         */
        private List<RecordImageIcon> buildImageList(int iconWidth)
        {
            List<RecordImageIcon> icons = New.list(myRecList.size());
            TIntList brokenIconIds = new TIntArrayList();
            for (IconRecord record : myRecList)
            {
                RecordImageIcon icon = loadImage(record, iconWidth);
                if (icon != null)
                {
                    icons.add(icon);
                }
                else
                {
                    brokenIconIds.add(record.getId());
                }

                if (isInterrupted())
                {
                    break;
                }
            }

            if (!brokenIconIds.isEmpty())
            {
                myIconRegistry.removeIcons(brokenIconIds, this);
            }

            return icons;
        }

        /**
         * Builds the rec button.
         *
         * @param size the size
         * @param rec the rec
         * @return the rec button
         */
        private AnchorPane buildRecButton(Dimension size, RecordImageIcon rec)
        {
            AnchorPane recBTPanel = new AnchorPane();
            Button imageBT = new Button(rec, myIconPopupMenu);


            JLabel nameLB = new JLabel(rec.getRecord().getName());
            recBTPanel.add(nameLB, BorderLayout.SOUTH);

            String urlStr = rec.getRecord().getImageURL().toString();
            imageBT.setToolTipText(urlStr);
            recBTPanel.add(imageBT, BorderLayout.CENTER);
            return recBTPanel;
        }

        /**
         * Loads an image.
         *
         * @param record the icon record
         * @param iconWidth the icon width
         * @return the record image icon, or null if it couldn't be loaded
         */
        private RecordImageIcon loadImage(IconRecord record, int iconWidth)
        {
            BufferedImage image;
            try
            {
                image = ImageIO.read(record.getImageURL());
            }
            catch (IOException e)
            {
                image = null;
            }

            RecordImageIcon icon = null;
            if (image != null)
            {
                Image scaledImage = ImageUtil.scaleDownImage(image, iconWidth, iconWidth - 20);
                icon = new RecordImageIcon(scaledImage, record);
            }
            return icon;
        }
    }

}

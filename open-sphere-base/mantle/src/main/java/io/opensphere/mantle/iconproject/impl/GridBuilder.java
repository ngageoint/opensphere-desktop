package io.opensphere.mantle.iconproject.impl;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.image.ImageUtil;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.impl.gui.IconChooserPanel.RecordImageIcon;

/**
 * The Class GridBuilder Builds the main icon grid.
 *
 */
public class GridBuilder extends GridPane// AnchorPane//implements Runnable
{
//    /** The Grid Panel anchor Pane. */
//    private final AnchorPane myGridPanel = new AnchorPane();
    /** The Toolbox. */
    private final Toolbox myToolbox;

//    /** The Icon registry. */
//    private final IconRegistry myIconRegistry;
//
//    /** The Selected records. */
//    private final Set<IconRecord> mySelectedRecords;

    /** the testing button. */
    Button tester;

    /** The Resize timer. */
    // private final Timer myResizeTimer;


    /** The list of icon records. */
    List<IconRecord> myRecList;

    /**
     * The Class GridBuilder.
     *
     * @param tb the toolbox
     */
    public GridBuilder(Toolbox tb, List<IconRecord> rl)//possibly pass allowable grid with in but idk how that'd work
    {
        myToolbox = tb;
        myRecList = rl;

        int tileWidth = 100;
        int borderSize = 6;
        int iconWidth = tileWidth - borderSize;
        int width = 300;
        int height = 400;


        int numIconRowsInView = (int)Math.ceil((double)height / (double)tileWidth);

        if (!isInterrupted()) //&& !myRecList.isEmpty())
        {
            // List<RecordImageIcon> imIcList = buildImageList(iconWidth);
            // make imageview instead
            List<ImageView> imIcList = buildImageList(iconWidth);

            if (!isInterrupted())
            {
                int numIconsPerRow = tileWidth > width ? 1 : (int)Math.floor((double)width / (double)tileWidth);
                int numRows = (int)Math.ceil((double)imIcList.size() / (double)numIconsPerRow);
//                grid = new JPanel(new GridLayout(numRows < numIconRowsInView ? numIconRowsInView : numRows, numIconsPerRow,
//                        borderSize, borderSize));
                if (numRows < numIconRowsInView)
                {
                    for (int row = 0; row < numRows; row++)
                    {
                        RowConstraints rc = new RowConstraints();
                        rc.setFillHeight(true);
                        rc.setVgrow(Priority.ALWAYS);
                        getRowConstraints().add(rc);
                    }
                }
                else
                {
                    System.out.println("haven't figured out what to do yet lol");
                }
                //Dimension size = new Dimension(iconWidth, iconWidth);

                for (int i = 0; i < imIcList.size() && !isInterrupted(); i++)
                {
                    ImageView rec = imIcList.get(i);
                    add(createButton(rec), i % numRows, i / numRows);
//                    RecordImageIcon rec = imIcList.get(i);
//                    JPanel imageBT = buildRecButton(size, rec);
//                    grid.add(imageBT);
                }
                int blankGridLements = numIconsPerRow * numIconRowsInView - imIcList.size();
                for (int i = 0; i < blankGridLements; i++)
                {
//                    grid.add(new JPanel());
                    System.out.println("Not sure whats happenening hereeeE");
                    //possibly adding the blank space when needed?????
                }
            }
        }





        /*setMinSize(150, 150);
        int numRows = 4; //need math for this based on # of images and size
        int numCols = 3; //^^^^^^ same

        //System.out.println("grid width: " + getWidth());

        Image image = new Image(getClass().getResourceAsStream("Guitar.png"));
        ImageView imageV = new ImageView(image);
        imageV.setPreserveRatio(true);
        imageV.setFitWidth(150);
        Image adidas = new Image(getClass().getResourceAsStream("Adidas.png"));
        ImageView adidasV = new ImageView(adidas);
        adidasV.setPreserveRatio(true);
        adidasV.setFitWidth(150);

        for (int row = 0; row < numRows; row++)
        {
            RowConstraints rc = new RowConstraints();
            rc.setFillHeight(true);
            rc.setVgrow(Priority.ALWAYS);
            getRowConstraints().add(rc);
        }
        for (int col = 0; col < numCols; col++)
        {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setFillWidth(true);
            cc.setHgrow(Priority.ALWAYS);
            getColumnConstraints().add(cc);
        }
        for (int i = 0; i < 9; i++)
        {
            Button button = createButton(Integer.toString(i + 1));
            add(button, i % 3, i / 3);
        }
        add(createButton(adidasV), 0, 3);
        add(createButton("0"), 1, 3);
        add(createButton("*", imageV), 2, 3);
        //add(createButton(adidasV), 5, 3);
*/
//        myIconRegistry = MantleToolboxUtils.getMantleToolbox(myToolbox).getIconRegistry();
//        mySelectedRecords = New.set();

        //System.out.println("Image width:    " + image.getWidth());

        // imageV.setFitHeight(150);
        /* System.out.println("Initial fit height:    " +
         * imageV.getFitHeight()); while (imageV.getFitHeight() > 200) {
         * imageV.setFitHeight(imageV.getFitHeight()/1.5);
         * System.out.println("Changed fit height:    " +
         * imageV.getFitHeight()); } System.out.println("Initial fit width:    "
         * + imageV.getFitWidth()); while (imageV.getFitWidth() > 200) {
         * imageV.setFitWidth(imageV.getFitWidth()/2);
         * System.out.println("Changed fit width:    " + imageV.getFitWidth());
         * } */

        /*getColumnConstraints().add(new ColumnConstraints(150));
        getRowConstraints().add(new RowConstraints(100));

        tester = new Button("", imageV);
        tester.setMaxSize(150., 100.);

        add(tester, 0, 0);
        add(new Button("Ze button"), 2, 2);*/

        // setFillWidth(tester, true);
        // setFillHeight(tester, true);
        // getChildren().add(tester);
        // System.out.println("width: " + getWidth());
        // System.out.println("height: " + getHeight());
        /* setWidth(20); setHeight(20);
         *
         * getChildren().addAll(tester); */

        // myResizeTimer = new Timer(200, e -> valueChanged(null));
        // myResizeTimer.setRepeats(false);
        // myLoader = new Thread(new BuildIconGridWorker(recList, canCancel));
        // myLoader.start();
    }

    /**
     * Builds the image list.
     *
     * @param iconWidth the icon width
     * @return the list
     */
    private List<ImageView> buildImageList(int iconWidth)
    {
        List<RecordImageIcon> icons = New.list(myRecList.size());
        TIntList brokenIconIds = new TIntArrayList();
        for (IconRecord record : myRecList)
        {
            Image icon = loadImage(record, iconWidth);
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

    //TEMP
    boolean isInterrupted()
        {
            return false;
        }

    /**
     * Loads an image.
     *
     * @param record the icon record
     * @param iconWidth the icon width
     * @return the record image icon, or null if it couldn't be loaded
     */
    private Image loadImage(IconRecord record, int iconWidth)
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

        Image icon = null;
        if (image != null)
        {
//            Image scaledImage = ImageUtil.scaleDownImage(image, iconWidth, iconWidth - 20);
//            icon = new RecordImageIcon(scaledImage, record);
        }
        return icon;
    }

    /** The create a button with just text method.
     *
     * @param text the text for the button
     * @return button the finished button
     */
    private Button createButton(String text)
    {
        Button button = new Button(text);
        button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        button.setOnAction(e -> System.out.println(text));
        return button;
    }

    /** The create a button with both text and an image method.
    *
    * @param text the text for the button
    * @param imageV the image to put in the button
    * @return button the finished button
    */
    private Button createButton(String text, ImageView imageV)
    {
        Button button = new Button(text, imageV);
        button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        button.setOnAction(e -> System.out.println(text));
        return button;
    }

    /** The create a button with just an image method.
    *
    * @param imageV the image to put in the button
    * @return button the finished button
    */
    private Button createButton(ImageView imageV)
    {
        Button button = new Button("", imageV);
        button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        button.setOnAction(e -> System.out.println("blank"));
        return button;
    }

    // myIconRegistry.addIcon(new DefaultIconProvider(result.toURI().toURL(),
    // collectionName, subCatName, "User"), this);
    // IconChooserPanel line 373
}

package io.opensphere.mantle.iconproject.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.jdesktop.swingx.JXImagePanel;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.filesystem.MnemonicFileChooser;
import io.opensphere.core.util.image.ImageUtil;
import io.opensphere.mantle.iconproject.model.PanelModel;
import javafx.embed.swing.SwingNode;
import javafx.scene.layout.AnchorPane;

public class LoadPane extends JPanel
{
    /** The serial ID. */
    private static final long serialVersionUID = -5913383302540771819L;
    
    /** The Sub Category currently selected. */
    private String mySubCatName;
    
    /** The current UI model. */
    private PanelModel myPanelModel;
    
    /** The Category currently selected. */
    private String myCollectionName;

    /**
     * Load from file.
     *
     * @param PanelModel the model used.
     */
    private AnchorPane createLoadPane()
    {
        AnchorPane LoadInterface = new AnchorPane();
        LoadInterface.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
                + "-fx-border-radius: 5;" + "-fx-border-color: yellow;");
        final SwingNode SwingWrapper = new SwingNode();
        createAndSetSwingContent(SwingWrapper);
        LoadInterface.getChildren().add(SwingWrapper);
        //FileChooser test = new FileChooser();
        //test.showOpenDialog(getParent().getScene().getWindow());
        return LoadInterface;
    }

    private void createAndSetSwingContent(SwingNode swingNode)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                JPanel LoadInterFace = new LoadPane(myPanelModel);
                swingNode.setContent(LoadInterFace);
            }
        });
    }
    
    public LoadPane(PanelModel thePanelModel)
    {
        myPanelModel = thePanelModel;
    //    String collectionName = myPanelModel.getCollectionName().get();
    //    String subCollectionName = myPanelModel.getSubCollectionName().get();
        

        File result = ImageUtil.showImageFileChooser("Choose Icon File", this,
                myPanelModel.getToolBox().getPreferencesRegistry());
        /* if (result != null) { try { myPanelModel.getMyIconRegistry()
         * .addIcon(new DefaultIconProvider(result.toURI().toURL(),
         * myCollectionName, mySubCatName, "User"), this);
         * refreshFromRegistry(myCollectionName); } catch (MalformedURLException
         * e) { JOptionPane.showMessageDialog(this, "Failed to load image: " +
         * result.getAbsolutePath(), "Image Load Error",
         * JOptionPane.ERROR_MESSAGE); } } */
        
        MnemonicFileChooser chooser = new MnemonicFileChooser(myPanelModel.getToolBox().getPreferencesRegistry(), "ImageUtil");

        String title = ("Choose Icon File");
        chooser.setDialogTitle(title == null ? "Choose Image File" : title);
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBorder(BorderFactory.createTitledBorder("Image Preview:"));
        
        final JXImagePanel imagePreviewPanel = new JXImagePanel();
        imagePreviewPanel.setMinimumSize(new Dimension(180, 180));
        imagePreviewPanel.setPreferredSize(new Dimension(180, 180));
        imagePanel.add(imagePreviewPanel, BorderLayout.SOUTH);

        chooser.setAccessory(imagePanel);
        chooser.setFileFilter(new FileFilter()
        {
            @Override
            public boolean accept(File f)
            {
                String fName = f.getName().toLowerCase();
                return f.isDirectory() || fName.endsWith(".jpeg") || fName.endsWith(".jpg") || fName.endsWith(".gif")
                        || fName.endsWith(".png") || fName.endsWith(".svg") || fName.endsWith(".bmp");
            }

            @Override
            public String getDescription()
            {
                return "Image Files[*.jpeg,*.jpg,*.gif,*.png,*.svg,*.bmp]";
            }
        });
        
    }
    
    public static File showImageFileChooser(String title, PreferencesRegistry prefsRegistry)
    {
        File chosenFile = null;

        MnemonicFileChooser chooser = new MnemonicFileChooser(prefsRegistry, "ImageUtil");

        chooser.setDialogTitle(title == null ? "Choose Image File" : title);
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBorder(BorderFactory.createTitledBorder("Image Preview:"));
        
        final JXImagePanel imagePreviewPanel = new JXImagePanel();
        imagePreviewPanel.setMinimumSize(new Dimension(180, 180));
        imagePreviewPanel.setPreferredSize(new Dimension(180, 180));
        imagePanel.add(imagePreviewPanel, BorderLayout.SOUTH);

        chooser.setAccessory(imagePanel);
        chooser.setFileFilter(new FileFilter()
        {
            @Override
            public boolean accept(File f)
            {
                String fName = f.getName().toLowerCase();
                return f.isDirectory() || fName.endsWith(".jpeg") || fName.endsWith(".jpg") || fName.endsWith(".gif")
                        || fName.endsWith(".png") || fName.endsWith(".svg") || fName.endsWith(".bmp");
            }

            @Override
            public String getDescription()
            {
                return "Image Files[*.jpeg,*.jpg,*.gif,*.png,*.svg,*.bmp]";
            }
        });

        chooser.addPropertyChangeListener(new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName()))
                {
                    String filePath = evt.getNewValue() == null ? null : evt.getNewValue().toString();
                    if (filePath != null)
                    {
                        try
                        {
                            BufferedImage buff = ImageIO.read(new File(filePath));
                            Image scale = ImageUtil.scaleDownImage(buff, imagePreviewPanel.getHeight(),
                                    imagePreviewPanel.getWidth());
                            imagePreviewPanel.setImage(scale);
                        }
                        catch (IOException e)
                        {
                            imagePreviewPanel.setImage(ImageUtil.NO_IMAGE);
                        }
                    }
                    else
                    {
                        imagePreviewPanel.setImage(ImageUtil.NO_IMAGE);
                    }
                }
            }
        });
            chosenFile = chooser.getSelectedFile();
            
        return chosenFile;
    }

}


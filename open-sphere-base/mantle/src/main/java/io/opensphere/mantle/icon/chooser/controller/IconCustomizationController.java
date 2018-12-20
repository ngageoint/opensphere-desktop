/**
 *
 */
package io.opensphere.mantle.icon.chooser.controller;

import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.chooser.model.CustomizationModel;
import io.opensphere.mantle.icon.chooser.model.IconModel;
import io.opensphere.mantle.icon.chooser.view.IconDetail;
import io.opensphere.mantle.icon.chooser.view.IconView;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * A controller used to modify the customization state of a given icon.
 */
public class IconCustomizationController
{
    /** The {@link Logger} used to capture output from this class. */
    private static final Logger LOG = Logger.getLogger(IconCustomizationController.class);

    /** The model in which customization operations are stored. */
    private CustomizationModel myCustomizationModel;

    /** The icon model in which the chooser is backed. */
    private final IconModel myModel;

    /** The detail panel on which the selected icon information is displayed. */
    private final IconDetail myDetailPanel;

    /**
     * Creates a new controller with the supplied model and view.
     *
     * @param model the model in which the chooser's state is maintained.
     * @param iconView the view in which all icon operations are managed.
     */
    public IconCustomizationController(IconModel model, IconView iconView)
    {
        myModel = model;
        myDetailPanel = iconView.getDetailPanel();
        myCustomizationModel = model.getCustomizationModel();

        myModel.selectedRecordProperty().addListener((obs, ov, nv) ->
        {
            myCustomizationModel.nameProperty().set(nv.nameProperty().get());
            myCustomizationModel.sourceProperty().set(nv.collectionNameProperty().get());
            myCustomizationModel.tagsProperty().set(nv.getTags().stream().collect(Collectors.joining(",")));
            myCustomizationModel.getTransformModel().resetAllToDefault();
            myDetailPanel.redrawPreview(nv);
        });
    }

    private void saveCanvas(Node canvasNode)
    {
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        WritableImage image = canvasNode.snapshot(parameters, null);

        BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(image, null);
        BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), bufImageARGB.getHeight(),
                Transparency.TRANSLUCENT);

        Graphics2D graphics = bufImageRGB.createGraphics();
        graphics.drawImage(bufImageARGB, 0, 0, null);
        try
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufImageRGB, "png", outputStream);

            URL imageURL = myModel.getIconRegistry().getIconCache().cacheIcon(outputStream.toByteArray(),
                    myCustomizationModel.nameProperty().get(), true);
            IconProvider provider = new DefaultIconProvider(imageURL, myCustomizationModel.sourceProperty().get(), "User");
            myModel.getIconRegistry().addIcon(provider, this);
        }
        catch (IOException e)
        {
            LOG.error("Failed to write image.", e);
        }
    }

}

package io.opensphere.mantle.icon.impl;

import java.io.File;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.util.net.UrlUtilities;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconSource;
import javafx.scene.Node;
import javafx.stage.FileChooser;

/**
 *
 */
public class SingleFileIconSource implements IconSource<FileIconSourceModel>
{
    /** The model to which the user interface is bound. */
    private final FileIconSourceModel myModel = new FileIconSourceModel();

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconSource#getName()
     */
    @Override
    public String getName()
    {
        return "single icon";
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconSource#getIconProviders()
     */
    @Override
    public List<IconProvider> getIconProviders()
    {
        return Collections
                .singletonList(new DefaultIconProvider(UrlUtilities.toURLNew(myModel.fileProperty().get().getAbsolutePath()),
                        IconRecord.USER_ADDED_COLLECTION, null, null));
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconSource#getModel()
     */
    @Override
    public FileIconSourceModel getModel()
    {
        return myModel;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconSource#getUserInput(Node)
     */
    @Override
    public void getUserInput(Node parent)
    {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.jpg", "*.jpeg", "*.jpe", "*.jfif", "*.jif", "*.JPG", "*.JPEG",
                        "*.JPE", "*.JFIF", "*.JIF", "*.png", "*.PNG", "*.gif", "*.GIF", "*.ico", "*.ICO", "*.bmp", "*.BMP"),
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg", "*.jpeg", "*.jpe", "*.jfif", "*.jif", "*.JPG", "*.JPEG", "*.JPE",
                        "*.JFIF", "*.JIF"),
                new FileChooser.ExtensionFilter("PNG", "*.png", "*.PNG"),
                new FileChooser.ExtensionFilter("GIF", "*.gif", "*.GIF"),
                new FileChooser.ExtensionFilter("ICO", "*.ico", "*.ICO"),
                new FileChooser.ExtensionFilter("BMP", "*.bmp", "*.BMP"));
        chooser.setTitle("Select an Icon");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = chooser.showOpenDialog(parent.getScene().getWindow());
        if (file != null)
        {
            myModel.fileProperty().set(file);
        }
    }
}

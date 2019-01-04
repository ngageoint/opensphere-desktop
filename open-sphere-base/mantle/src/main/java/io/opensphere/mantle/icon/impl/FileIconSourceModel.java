package io.opensphere.mantle.icon.impl;

import java.io.File;

import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import io.opensphere.mantle.icon.IconSourceModel;
import javafx.beans.property.ObjectProperty;

/**
 * The source model for a file icon source.
 */
public class FileIconSourceModel implements IconSourceModel
{
    /**
     * The property in which the file is bound.
     */
    private final ObjectProperty<File> myFileProperty = new ConcurrentObjectProperty<>();

    /**
     * Gets the value of the {@link #myFileProperty} field.
     *
     * @return the value stored in the {@link #myFileProperty} field.
     */
    public ObjectProperty<File> fileProperty()
    {
        return myFileProperty;
    }
}

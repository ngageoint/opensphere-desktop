package io.opensphere.server.display;

import java.awt.Component;
import java.util.List;

import io.opensphere.core.util.Validatable;
import io.opensphere.mantle.datasources.IDataSource;

/**
 * Interface for Editors that produce Server Sources.
 */
public interface ServerSourceEditor extends Validatable
{
    /**
     * Accept (apply) changes.
     *
     * @return whether to allow the accept to complete
     */
    boolean accept();

    /**
     * Gets the changed data source.
     *
     * @return the data source
     */
    IDataSource getChangedSource();

    /**
     * Gets a component (usually a JPanel) used to edit data sources.
     *
     * @return the source editor
     */
    Component getEditor();

    /**
     * Returns whether the source being edited is new.
     *
     * @return whether it's a new source
     */
    boolean isNewSource();

    /**
     * Opens the given data source, or creates a new one if it's null.
     *
     * @param source The data source
     * @param isNew Whether the source is new
     * @param otherSources The other data sources
     */
    void openSource(IDataSource source, boolean isNew, List<IDataSource> otherSources);
}

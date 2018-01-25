package io.opensphere.controlpanels.styles.controller;

import java.util.List;

/**
 * Interface to a class that provides the different columns that can be labeled.
 */
public interface LabelOptionsController
{
    /**
     * Gets the available columns that can be used in a label.
     *
     * @return The list of columns.
     */
    List<String> getColumns();
}

package io.opensphere.featureactions.editor.ui;

import javafx.scene.control.Accordion;
import javafx.scene.control.Button;

/**
 * Interface to the editor object.
 */
public interface SimpleFeatureActionEditor
{
    /**
     * Gets the accordion view used to display all the feature action groups.
     *
     * @return The accordion view.
     */
    Accordion getAccordion();

    /**
     * Gets the add button.
     *
     * @return The add button.
     */
    Button getAddButton();

    /**
     * Gets the export button.
     *
     * @return The export button.
     */
    Button getExportButton();

    /**
     * Gets the import button.
     *
     * @return The export button.
     */
    Button getImportButton();

    /**
     * Sets the saving listener.
     *
     * @param runnable The save listener.
     */
    void setSaveListener(Runnable runnable);
}

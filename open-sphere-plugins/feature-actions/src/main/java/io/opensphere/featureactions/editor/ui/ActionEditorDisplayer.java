package io.opensphere.featureactions.editor.ui;

import java.awt.Window;

import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Interface to an object that knows how to display a
 * {@link SimpleFeatureActionEditor} so the user can edit the feature actions
 * for a given layer.
 */
public interface ActionEditorDisplayer
{
    /**
     * Displays the {@link SimpleFeatureActionEditor} to the user so they can
     * edit feature actions.
     *
     * @param owner The window owner.
     * @param layer The layer.
     */
    void displaySimpleEditor(Window owner, DataTypeInfo layer);
}

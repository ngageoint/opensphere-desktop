package io.opensphere.controlpanels.layers.layerpopout;

import java.awt.Container;

import io.opensphere.controlpanels.layers.activedata.controller.ActiveDataDataLayerController;

/**
 * Interfaces to an object that can create ActiveData views.
 */
@FunctionalInterface
public interface ActiveDataViewCreator
{
    /**
     * Creates the ActiveDataView using the specified controller.
     *
     * @param controller The controller to use for the ActiveDataView.
     * @return A newly create ActiveDataView.
     */
    Container createActiveDataView(ActiveDataDataLayerController controller);
}

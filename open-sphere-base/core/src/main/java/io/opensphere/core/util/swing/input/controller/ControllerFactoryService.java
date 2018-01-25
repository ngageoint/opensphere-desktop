package io.opensphere.core.util.swing.input.controller;

import javax.swing.JComponent;

import io.opensphere.core.util.ObservableValueService;
import io.opensphere.core.util.swing.input.model.ViewModel;

/**
 * Controller factory service. In order for the controllers (and views) to be
 * active, {@link #open()} must be called. In order to clean up,
 * {@link #close()} should be called when the view is no longer needed.
 */
public class ControllerFactoryService extends ObservableValueService
{
    /**
     * Creates and returns a view component for the given model. It will use the
     * preferredComponent for the view if possible, otherwise
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param <U> The view type
     * @param model The model
     * @param preferredComponent The preferred component type
     * @return The view component
     */
    public <U extends JComponent> U createComponent(ViewModel<?> model, Class<U> preferredComponent)
    {
        return createComponent(model, preferredComponent, null);
    }

    /**
     * Creates and returns a view component for the given model. It will use the
     * preferredComponent for the view if possible, otherwise
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param <T> The model data type
     * @param <U> The view type
     * @param model The model
     * @param preferredComponent The preferred component type
     * @param viewSettings The view settings
     * @return The view component
     */
    public <T, U extends JComponent> U createComponent(ViewModel<T> model, Class<U> preferredComponent,
            ViewSettings<T> viewSettings)
    {
        U component = null;

        AbstractController<T, ? extends ViewModel<T>, U> controller = ControllerFactory.createControllerDetached(model,
                preferredComponent, viewSettings);
        if (controller != null)
        {
            component = controller.getView();
            addService(controller);
        }

        return component;
    }
}

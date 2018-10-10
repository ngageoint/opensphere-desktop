package io.opensphere.core.util.swing.input.controller;

import java.awt.Component;
import java.util.Collection;
import java.util.function.Function;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.core.util.swing.IconToggleButton;
import io.opensphere.core.util.swing.RadioButtonPanel;
import io.opensphere.core.util.swing.input.model.BooleanModel;
import io.opensphere.core.util.swing.input.model.ChoiceModel;
import io.opensphere.core.util.swing.input.model.ColorModel;
import io.opensphere.core.util.swing.input.model.DoubleModel;
import io.opensphere.core.util.swing.input.model.GhostTextModel;
import io.opensphere.core.util.swing.input.model.IntegerModel;
import io.opensphere.core.util.swing.input.model.PropertyChangeEvent;
import io.opensphere.core.util.swing.input.model.TextModel;
import io.opensphere.core.util.swing.input.model.TimeInstantModel;
import io.opensphere.core.util.swing.input.model.ViewModel;
import io.opensphere.core.util.swing.input.view.SliderPanel;

/**
 * Controller factory.
 */
public final class ControllerFactory
{
    /**
     * Creates a JComboBox for a ChoiceModel.
     *
     * @param <T> The type of the options
     * @param model The model
     * @param preferredComponent The preferred component type
     * @return The view component
     */
    @SuppressWarnings("unchecked")
    public static <T> JComboBox<T> createComponent(ChoiceModel<T> model,
            @SuppressWarnings("rawtypes") Class<JComboBox> preferredComponent)
    {
        return createComponent(model, preferredComponent, null);
    }

    /**
     * Creates and returns the default view component for the given model.
     *
     * @param model The model
     * @return The view component
     */
    public static JComponent createComponent(ViewModel<?> model)
    {
        return createComponent(model, null, null);
    }

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
    public static <U extends JComponent> U createComponent(ViewModel<?> model, Class<U> preferredComponent)
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
    public static <T, U extends JComponent> U createComponent(ViewModel<T> model, Class<U> preferredComponent,
            ViewSettings<T> viewSettings)
    {
        U component = null;

        AbstractController<T, ? extends ViewModel<T>, U> controller = createController(model, preferredComponent, viewSettings);
        if (controller != null)
        {
            component = controller.getView();
        }

        return component;
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
     * @param controllers Collection in which to put the controller for the view
     *            component
     * @return The view component
     */
    public static <T, U extends JComponent> U createComponent(ViewModel<T> model, Class<U> preferredComponent,
            ViewSettings<T> viewSettings, Collection<? super AbstractController<T, ? extends ViewModel<T>, U>> controllers)
    {
        U component = null;

        AbstractController<T, ? extends ViewModel<T>, U> controller = createController(model, preferredComponent, viewSettings);
        if (controller != null)
        {
            component = controller.getView();
            controllers.add(controller);
        }

        return component;
    }

    /**
     * Creates the appropriate controller for the given model and preferred
     * component.
     *
     * @param <T> The model data type
     * @param <U> The preferred view type
     * @param model The model
     * @param preferredComponent The preferred component type
     * @param viewSettings The view settings
     * @return The controller
     */
    public static <T, U extends JComponent> AbstractController<T, ? extends ViewModel<T>, U> createController(ViewModel<T> model,
            Class<U> preferredComponent, ViewSettings<T> viewSettings)
    {
        AbstractController<T, ? extends ViewModel<T>, U> castController = createControllerDetached(model, preferredComponent,
                viewSettings);

        // Initialize the controller
        if (castController != null)
        {
            castController.open();
        }

        return castController;
    }

    /**
     * Creates a label for the given model and view.
     *
     * @param model the model
     * @param component the view component
     * @return the label
     */
    public static JLabel createLabel(final ViewModel<?> model, Component component)
    {
        final JLabel label = new JLabel(model.getName() + ":");
        model.addPropertyChangeListener(e ->
        {
            if (e.getProperty() == PropertyChangeEvent.Property.NAME_AND_DESCRIPTION)
            {
                label.setText(model.getName() + ":");
            }
        });
        label.setLabelFor(component);
        label.setEnabled(component.isEnabled());
        return label;
    }

    /**
     * Gets a label and component for the given controller.
     *
     * @param controller the controller
     * @return the array of label and component
     */
    public static JComponent[] createLabelAndComponent(AbstractController<?, ?, ?> controller)
    {
        JComponent[] components = new JComponent[2];
        components[1] = controller.getView();
        components[0] = createLabel(controller.getModel(), components[1]);
        return components;
    }

    /**
     * Creates a label and component for the given model.
     *
     * @param model the model
     * @return the array of label and component
     */
    public static JComponent[] createLabelAndComponent(ViewModel<?> model)
    {
        return createLabelAndComponent(model, null);
    }

    /**
     * Creates a label and component for the given model.
     *
     * @param model the model
     * @param preferredComponent The preferred component type
     * @return the array of label and component
     */
    public static JComponent[] createLabelAndComponent(ViewModel<?> model, Class<? extends JComponent> preferredComponent)
    {
        JComponent[] components = new JComponent[2];
        components[1] = createComponent(model, preferredComponent);
        components[0] = createLabel(model, components[1]);
        return components;
    }

    /**
     * Creates the appropriate controller for the given model and preferred
     * component. The controller returned has not been opened.
     *
     * @param <T> The model data type
     * @param <U> The preferred view type
     * @param model The model
     * @param preferredComponent The preferred component type
     * @param viewSettings The view settings
     * @return The controller
     */
    static <U extends JComponent, T> AbstractController<T, ? extends ViewModel<T>, U> createControllerDetached(ViewModel<T> model,
            Class<U> preferredComponent, ViewSettings<T> viewSettings)
    {
        AbstractController<?, ? extends ViewModel<?>, ? extends JComponent> controller = null;

//        if (model instanceof DateModel)
//        {
//            controller = new DateController((DateModel)model);
//        }
        if (model instanceof TextModel)
        {
            if (model instanceof GhostTextModel)
            {
                controller = new GhostTextController((GhostTextModel)model);
            }
            else
            {
                controller = new TextController((TextModel)model);
            }
        }
        else if (model instanceof BooleanModel)
        {
            if (preferredComponent != null)
            {
                if (JToggleButton.class.isAssignableFrom(preferredComponent))
                {
                    controller = new BooleanToggleButtonController((BooleanModel)model);
                }
                else if (IconButton.class.isAssignableFrom(preferredComponent))
                {
                    controller = new IconButtonController((BooleanModel)model);
                }
                else
                {
                    controller = new BooleanCheckBoxController((BooleanModel)model);
                }
            }
            else
            {
                controller = new BooleanCheckBoxController((BooleanModel)model);
            }
        }
        else if (model instanceof DoubleModel)
        {
            controller = new DoubleTextFieldController((DoubleModel)model);
        }
        else if (model instanceof IntegerModel)
        {
            controller = createIntegerController((IntegerModel)model, preferredComponent);
        }
        else if (model instanceof ChoiceModel)
        {
            controller = createChoiceController((ChoiceModel<T>)model, preferredComponent, viewSettings);
        }
        else if (model instanceof ColorModel)
        {
            controller = new ColorButtonController((ColorModel)model);
        }
        else if (model instanceof TimeInstantModel)
        {
            controller = new TimeInstantTextFieldController((TimeInstantModel)model);
        }

        if (controller != null && preferredComponent != null
                && !preferredComponent.isAssignableFrom(controller.getView().getClass()))
        {
            throw new IllegalArgumentException("The preferredComponent " + preferredComponent + " is not supported.");
        }

        @SuppressWarnings("unchecked")
        AbstractController<T, ? extends ViewModel<T>, U> castController = (AbstractController<T, ? extends ViewModel<T>, U>)controller;

        if (castController != null)
        {
            castController.setViewSettings(viewSettings);
        }

        return castController;
    }

    /**
     * Creates the appropriate controller for the given model and preferred
     * component.
     *
     * @param <T> The model data type
     * @param model The model
     * @param preferredComponent The preferred component type
     * @param viewSettings The view settings
     * @return The controller
     */
    private static <T> AbstractController<T, ? extends ChoiceModel<T>, ? extends JComponent> createChoiceController(
            ChoiceModel<T> model, Class<? extends JComponent> preferredComponent, final ViewSettings<T> viewSettings)
    {
        AbstractController<T, ? extends ChoiceModel<T>, ? extends JComponent> controller;
        if (preferredComponent == RadioButtonPanel.class)
        {
            Function<T, AbstractButton> buttonProvider = viewSettings == null || viewSettings.getIconProvider() == null ? null
                    : option ->
                    {
                        IconToggleButton button = new IconToggleButton();
                        ImageIcon icon = viewSettings.getIconProvider().apply(option);
                        if (icon != null)
                        {
                            IconUtil.setIcons(button, icon, IconUtil.DEFAULT_ICON_FOREGROUND,
                                    IconUtil.ICON_SELECTION_FOREGROUND);
                        }
                        button.setToolTipText(option.toString());
                        return button;
                    };
            controller = new ChoiceRadioButtonController<>(model, viewSettings, buttonProvider);
        }
        else
        {
            controller = new ChoiceComboBoxController<>(model, viewSettings);
        }
        return controller;
    }

    /**
     * Creates the appropriate controller for the given model and preferred
     * component.
     *
     * @param model The model
     * @param preferredComponent The preferred component type
     * @return The controller
     */
    private static AbstractController<Integer, ? extends IntegerModel, ? extends JComponent> createIntegerController(
            IntegerModel model, Class<? extends JComponent> preferredComponent)
    {
        AbstractController<Integer, ? extends IntegerModel, ? extends JComponent> controller;
        if (preferredComponent == JTextField.class)
        {
            controller = new IntegerTextFieldController(model);
        }
        else if (preferredComponent == JSpinner.class)
        {
            controller = new IntegerSpinnerController(model);
        }
        else if (preferredComponent == JSlider.class)
        {
            controller = new IntegerSliderController(model);
        }
        else if (preferredComponent == SliderPanel.class)
        {
            controller = new IntegerSliderPanelController(model);
        }
        else
        {
            boolean isLargeRange = model.getMax() - model.getMin() > 100;
            if (isLargeRange)
            {
                controller = new IntegerTextFieldController(model);
            }
            else
            {
                controller = new IntegerSpinnerController(model);
            }
        }
        return controller;
    }

    /**
     * Private constructor.
     */
    private ControllerFactory()
    {
    }
}

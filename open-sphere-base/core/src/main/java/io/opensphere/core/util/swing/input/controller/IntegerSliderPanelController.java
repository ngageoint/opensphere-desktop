package io.opensphere.core.util.swing.input.controller;

import javax.swing.JSlider;
import javax.swing.JTextField;

import io.opensphere.core.util.swing.input.model.IntegerModel;
import io.opensphere.core.util.swing.input.model.ViewModel;
import io.opensphere.core.util.swing.input.view.SliderPanel;

/**
 * A controller using an Integer model and SliderPanel view.
 */
public class IntegerSliderPanelController extends AbstractController<Integer, IntegerModel, SliderPanel>
{
    /** The slider controller. */
    private final AbstractController<Integer, ? extends ViewModel<Integer>, JSlider> mySliderController;

    /** The text field controller. */
    private final AbstractController<Integer, ? extends ViewModel<Integer>, JTextField> myTextFieldController;

    /**
     * Constructor.
     *
     * @param model The model
     */
    public IntegerSliderPanelController(IntegerModel model)
    {
        super(model, new SliderPanel());
        mySliderController = ControllerFactory.createController(model, JSlider.class, null);
        myTextFieldController = ControllerFactory.createController(model, JTextField.class, null);
    }

    @Override
    public void open()
    {
        getView().buildUI(mySliderController.getView(), myTextFieldController.getView());
        super.open();
    }

    @Override
    public void close()
    {
        super.close();
        mySliderController.close();
        myTextFieldController.close();
    }

    @Override
    protected void updateModel()
    {
        // This is handled by the controllers for the individual components
    }

    @Override
    protected void updateViewValue()
    {
        // This is handled by the controllers for the individual components
    }
}

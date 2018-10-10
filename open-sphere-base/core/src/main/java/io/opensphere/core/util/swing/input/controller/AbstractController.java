package io.opensphere.core.util.swing.input.controller;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.swing.input.model.PropertyChangeEvent;
import io.opensphere.core.util.swing.input.model.PropertyChangeListener;
import io.opensphere.core.util.swing.input.model.ViewModel;

/**
 * Abstract controller.
 *
 * @param <T> The type of the value in the view model
 * @param <M> The model type
 * @param <V> The view type
 */
public abstract class AbstractController<T, M extends ViewModel<T>, V extends JComponent> implements Service
{
    /** The model. */
    private final M myModel;

    /** The view. */
    private final V myView;

    /** The view settings. */
    private ViewSettings<T> myViewSettings;

    /** The default background. */
    private Color myDefaultBackground;

    /** The error background. */
    private Color myErrorBackground;

    /** The label for the component. */
    private JLabel myLabel;

    /** The value change listener. */
    private ChangeListener<T> myChangeListener;

    /** The property change listener. */
    private PropertyChangeListener myPropertyChangeListener;

    /** Whether the model listener is enabled. */
    private boolean myIsModelListenerEnabled = true;

    /** Whether the view listener is enabled. */
    private boolean myIsViewListenerEnabled = true;

    /**
     * Constructor.
     *
     * @param model The model
     * @param view The view
     */
    public AbstractController(M model, V view)
    {
        myModel = Utilities.checkNull(model, "model");
        myView = Utilities.checkNull(view, "view");
    }

    @Override
    public void open()
    {
        assert EventQueue.isDispatchThread();

        if (myViewSettings != null && myViewSettings.getBackground() != null)
        {
            myView.setBackground(myViewSettings.getBackground());
        }
        setDefaultBackground(myView.getBackground());

        handleModelChange();
        updateViewEnabled();
        updateViewVisible();

        myChangeListener = (observable, oldValue, newValue) ->
        {
            assert EventQueue.isDispatchThread();
            handleModelChange();
        };
        myModel.addListener(myChangeListener);

        myPropertyChangeListener = e ->
        {
            assert EventQueue.isDispatchThread();
            if (e.getProperty() == PropertyChangeEvent.Property.ENABLED)
            {
                updateViewEnabled();
            }
            else if (e.getProperty() == PropertyChangeEvent.Property.VISIBLE)
            {
                updateViewVisible();
            }
            else if (e.getProperty() == PropertyChangeEvent.Property.OPTIONS)
            {
                updateViewOptions();
            }
            else if (e.getProperty() == PropertyChangeEvent.Property.VALIDATION_CRITERIA)
            {
                updateViewLookAndFeel();
            }
            else if (e.getProperty() == PropertyChangeEvent.Property.VIEW_PARAMETERS)
            {
                updateViewParameters();
            }
            else if (e.getProperty() == PropertyChangeEvent.Property.NAME_AND_DESCRIPTION)
            {
                // This will also update the tool tip.
                updateViewLookAndFeel();
            }
        };
        myModel.addPropertyChangeListener(myPropertyChangeListener);
    }

    @Override
    public void close()
    {
        myModel.removeListener(myChangeListener);
        myModel.removePropertyChangeListener(myPropertyChangeListener);
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public JLabel getLabel()
    {
        if (myLabel == null)
        {
            myLabel = new JLabel(myModel.getName() + ":");
            myLabel.setEnabled(myModel.isEnabled());
            myLabel.setLabelFor(myView);
        }
        return myLabel;
    }

    /**
     * Gets the model.
     *
     * @return the model
     */
    public M getModel()
    {
        return myModel;
    }

    /**
     * Gets the view.
     *
     * @return the view
     */
    public V getView()
    {
        return myView;
    }

    /**
     * Sets the view settings.
     *
     * @param viewSettings The view settings
     */
    public void setViewSettings(ViewSettings<T> viewSettings)
    {
        myViewSettings = viewSettings;
    }

    /**
     * Gets the view settings.
     *
     * @return the view settings
     */
    public ViewSettings<T> getViewSettings()
    {
        return myViewSettings;
    }

    /**
     * Gets the default background.
     *
     * @return the default background
     */
    protected Color getDefaultBackground()
    {
        return myDefaultBackground;
    }

    /**
     * Gets the error background.
     *
     * @return the error background
     */
    protected Color getErrorBackground()
    {
        return myErrorBackground;
    }

    /**
     * Handles a view change by updating the model if allowed.
     */
    protected void handleViewChange()
    {
        if (myIsViewListenerEnabled)
        {
            myIsModelListenerEnabled = false;
            updateModel();
            myModel.setValidating(true);
            myIsModelListenerEnabled = true;
        }
    }

    /**
     * Sets the default background.
     *
     * @param defaultBackground the new default background
     */
    protected void setDefaultBackground(Color defaultBackground)
    {
        myDefaultBackground = defaultBackground;
        int red = Math.min(255, myDefaultBackground.getRed() + 40);
        myErrorBackground = new Color(red, myDefaultBackground.getGreen(), myDefaultBackground.getBlue());
    }

    /**
     * Gets called when the model changes in order to update any view settings
     * (that don't directly affect the model value).
     */
    protected void updateViewLookAndFeel()
    {
        if (myModel.getValidationStatus() != ValidationStatus.VALID)
        {
            myView.setToolTipText(myModel.getErrorMessage());
            myView.setBackground(myErrorBackground);
        }
        else
        {
            myView.setToolTipText(myModel.getDescription());
            myView.setBackground(myDefaultBackground);
        }
    }

    /**
     * Updates the model (from the view).
     */
    protected abstract void updateModel();

    /**
     * Updates the view options.
     */
    protected void updateViewOptions()
    {
    }

    /**
     * Updates the view parameters.
     */
    protected void updateViewParameters()
    {
    }

    /**
     * Updates the view value.
     */
    protected abstract void updateViewValue();

    /**
     * Handles a model change by updating the view if allowed.
     */
    private void handleModelChange()
    {
        if (myIsModelListenerEnabled)
        {
            myIsViewListenerEnabled = false;
            updateViewValue();
            myIsViewListenerEnabled = true;
        }

        updateViewLookAndFeel();
    }

    /**
     * Sets the view's enabled state to that of the model.
     */
    private void updateViewEnabled()
    {
        if (myView instanceof JTextField)
        {
            ((JTextField)myView).setEditable(myModel.isEnabled());
        }
        else
        {
            myView.setEnabled(myModel.isEnabled());
        }
        Object labeledBy = myView.getClientProperty("labeledBy");
        if (labeledBy instanceof JComponent)
        {
            ((JComponent)labeledBy).setEnabled(myModel.isEnabled());
        }
    }

    /**
     * Sets the view's visible state to that of the model.
     */
    private void updateViewVisible()
    {
        myView.setVisible(myModel.isVisible());
        //        Object labeledBy = myView.getClientProperty("labeledBy");
        //        if (labeledBy instanceof JComponent)
        //        {
        //            ((JComponent)labeledBy).setVisible(myModel.isVisible());
        //        }
    }
}

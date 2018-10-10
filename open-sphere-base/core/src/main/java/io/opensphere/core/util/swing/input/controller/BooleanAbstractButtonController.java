package io.opensphere.core.util.swing.input.controller;

import java.awt.Color;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;

import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconStyle;
import io.opensphere.core.util.swing.input.model.BooleanModel;
import io.opensphere.core.util.swing.input.model.PropertyChangeEvent;
import io.opensphere.core.util.swing.input.model.PropertyChangeListener;

/**
 * A controller using an Boolean model and JToggleButton view.
 */
public abstract class BooleanAbstractButtonController extends AbstractController<Boolean, BooleanModel, AbstractButton>
{
    /** The action listener. */
    private ActionListener myActionListener;

    /**
     * Constructor.
     *
     * @param model The model
     * @param view The view
     */
    public BooleanAbstractButtonController(BooleanModel model, AbstractButton view)
    {
        super(model, view);

        model.addPropertyChangeListener(new PropertyChangeListener()
        {
            @Override
            public void stateChanged(PropertyChangeEvent e)
            {
                if (e.getProperty() == PropertyChangeEvent.Property.NAME_AND_DESCRIPTION && getView().getIcon() == null)
                {
                    getView().setText(getModel().getName());
                }
            }
        });
    }

    @Override
    public void close()
    {
        super.close();
        if (myActionListener != null)
        {
            getView().removeActionListener(myActionListener);
        }
    }

    @Override
    public void open()
    {
        super.open();
        myActionListener = e -> handleViewChange();
        getView().addActionListener(myActionListener);

        ViewSettings<Boolean> viewSettings = getViewSettings();
        if (viewSettings != null && viewSettings.getIconProvider() != null)
        {
            AbstractButton button = getView();

            Color foreground = viewSettings.getForeground();
            if (foreground == null)
            {
                foreground = IconUtil.DEFAULT_ICON_FOREGROUND;
            }

            ImageIcon icon = viewSettings.getIconProvider().apply(Boolean.FALSE);
            button.setIcon(IconUtil.getColorizedIcon(icon, IconStyle.NORMAL, foreground));
            button.setRolloverIcon(IconUtil.getColorizedIcon(icon, IconStyle.ROLLOVER, foreground));

            ImageIcon selectedIcon = viewSettings.getIconProvider().apply(Boolean.TRUE);
            if (selectedIcon.equals(icon))
            {
                Color selectionForeground = viewSettings.getSelectionForeground();
                if (selectionForeground == null)
                {
                    selectionForeground = IconUtil.ICON_SELECTION_FOREGROUND;
                }
                selectedIcon = IconUtil.getColorizedIcon(icon, IconStyle.NORMAL, selectionForeground);
            }
            button.setPressedIcon(selectedIcon);
            button.setSelectedIcon(selectedIcon);
        }
        else
        {
            getView().setText(getModel().getName());
        }
    }

    @Override
    protected void updateModel()
    {
        getModel().set(Boolean.valueOf(getView().isSelected()));
    }

    @Override
    protected void updateViewValue()
    {
        getView().setSelected(getModel().get() != null && getModel().get().booleanValue());
    }
}

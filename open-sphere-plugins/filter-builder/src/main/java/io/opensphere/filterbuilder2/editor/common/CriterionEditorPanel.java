package io.opensphere.filterbuilder2.editor.common;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;

import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.input.controller.AbstractController;
import io.opensphere.core.util.swing.input.controller.ControllerFactory;
import io.opensphere.core.util.swing.input.model.PropertyChangeEvent;
import io.opensphere.core.util.swing.input.model.PropertyChangeEvent.Property;
import io.opensphere.core.util.swing.input.model.PropertyChangeListener;
import io.opensphere.core.util.swing.input.model.ViewModel;
import io.opensphere.filterbuilder2.common.Constants;
import io.opensphere.filterbuilder2.editor.model.CriterionModel;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;

/**
 * The panel for editing a single criterion.
 */
public class CriterionEditorPanel extends GridBagPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The height used for all components so that they match. */
    private static int ourComponentHeight;

    /** The model. */
    private final CriterionModel myModel;

    /** The number of columns for text fields. */
    private final int myTextColumns;

    /** The key listener. */
    private KeyListener myKeyListener;

    /** The field controller. */
    private AbstractController<?, ? extends ViewModel<?>, ? extends JComponent> myFieldController;

    /** The operator controller. */
    private AbstractController<?, ? extends ViewModel<?>, ? extends JComponent> myOperatorController;

    /** The value controller. */
    private AbstractController<?, ? extends ViewModel<?>, ? extends JComponent> myValueController;

    /** The max value controller. */
    private AbstractController<?, ? extends ViewModel<?>, ? extends JComponent> myMaxValueController;

    /** Whether the current field is a time field. */
    private boolean myIsTimeField;

    /**
     * Sets up the combo box.
     *
     * @param component the component
     * @param maxWidth the maximum width
     * @param hasScrollbar if the component should get a scrollbar
     * @return the modified component
     */
    private static JComponent setupComboBox(JComponent component, int maxWidth, boolean hasScrollbar)
    {
        if (component instanceof JComboBox)
        {
            JComboBox<?> comboBox = (JComboBox<?>)component;
            if (hasScrollbar)
            {
                comboBox.setMaximumRowCount(10);
            }
            else
            {
                comboBox.setMaximumRowCount(comboBox.getItemCount());
            }
            comboBox.setRenderer(setupPaddingRenderer(3, 0, 3, 0));
            ourComponentHeight = comboBox.getPreferredSize().height;
        }

        Dimension size = component.getPreferredSize();
        if (size.width > maxWidth)
        {
            size.width = maxWidth;
            component.setPreferredSize(size);
        }

        return component;
    }

    /**
     * Set up the padding renderer for extra space between list items.
     *
     * @param top the top padding in pixels
     * @param left the left padding in pixels
     * @param bottom the bottom padding in pixels
     * @param right the right padding in pixels
     * @return the padding renderer
     */
    private static DefaultListCellRenderer setupPaddingRenderer(int top, int left, int bottom, int right)
    {
        return new DefaultListCellRenderer()
        {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus)
            {
                JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
                return label;
            }
        };
    }

    /**
     * Constructor.
     *
     * @param model the model
     * @param textColumns the number of columns for text fields
     */
    public CriterionEditorPanel(CriterionModel model, int textColumns)
    {
        super();
        myModel = model;
        myTextColumns = textColumns;

        buildPanel();

        myIsTimeField = model.getSpecialType() instanceof TimeKey;
        model.getField().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                boolean isTimeField = myModel.getSpecialType() instanceof TimeKey;
                if (myIsTimeField != isTimeField)
                {
                    myIsTimeField = isTimeField;
                    rebuild();
                }
            }
        });

        model.getCriterionMaxValue().addPropertyChangeListener(new PropertyChangeListener()
        {
            @Override
            public void stateChanged(PropertyChangeEvent e)
            {
                if (e.getProperty() == Property.VISIBLE)
                {
                    revalidate();
                }
            }
        });
    }

    @Override
    public void addNotify()
    {
        super.addNotify();
        myValueController.getView().requestFocusInWindow();
    }

    /**
     * Closes the controllers in the panel.
     */
    public void close()
    {
        if (myFieldController != null)
        {
            myFieldController.close();
        }
        if (myOperatorController != null)
        {
            myOperatorController.close();
        }
        if (myValueController != null)
        {
            myValueController.close();
        }
        if (myMaxValueController != null)
        {
            myMaxValueController.close();
        }
    }

    /**
     * Rebuilds the panel.
     */
    public void rebuild()
    {
        close();
        removeAll();
        buildPanel();
        revalidate();
    }

    /**
     * Sets the key listener.
     *
     * @param keyListener the new key listener
     */
    public void setKeyListener(KeyListener keyListener)
    {
        myKeyListener = keyListener;
    }

    /**
     * Builds the panel.
     */
    private void buildPanel()
    {
        myFieldController = ControllerFactory.createController(myModel.getField(), null, null);
        if (myFieldController != null)
        {
            fillNone();
            setInsets(Constants.INSET, Constants.INSET, Constants.INSET, Constants.INSET);
            add(setupComboBox(myFieldController.getView(), 180, true));
        }

        myOperatorController = ControllerFactory.createController(myModel.getOperator(), null, null);
        if (myOperatorController != null)
        {
            setInsets(Constants.INSET, 0, Constants.INSET, Constants.INSET);
            add(setupComboBox(myOperatorController.getView(), 180, false));
        }

        myValueController = ControllerFactory.createController(myModel.getCriterionValue(), null, null);
        if (myValueController != null)
        {
            fillHorizontal();
            add(setupTextField(myValueController.getView(), myTextColumns));
        }

        myMaxValueController = ControllerFactory.createController(myModel.getCriterionMaxValue(), null, null);
        if (myMaxValueController != null)
        {
            fillHorizontal();
            add(setupTextField(myMaxValueController.getView(), myTextColumns));
        }

        fillHorizontal();
        setWeightx(.00001);
        add(Box.createHorizontalGlue(), getGBC());
    }

    /**
     * Sets up the text field.
     *
     * @param component the component
     * @param textColumns the number of columns for the text field
     * @return the modified component
     */
    private JComponent setupTextField(JComponent component, int textColumns)
    {
        if (component instanceof JTextField)
        {
            ((JTextField)component).setColumns(textColumns);
        }
        component.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    if (myKeyListener != null)
                    {
                        myKeyListener.keyPressed(e);
                    }
                    e.consume();
                }
            }
        });
        component.setPreferredSize(new Dimension(component.getPreferredSize().width, ourComponentHeight));
        return component;
    }
}

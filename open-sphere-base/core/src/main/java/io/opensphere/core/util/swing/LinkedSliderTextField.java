package io.opensphere.core.util.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.WeakChangeSupport;

/**
 * This class consists of a JSlider and a text field. The two components are
 * linked so that changes in one are reflected in the other. A value change
 * event is fired when the slider or field values change.
 */
@SuppressWarnings("PMD.GodClass")
public final class LinkedSliderTextField extends AbstractHUDPanel
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The Change support for SliderFieldControl changed events. */
    private final transient ChangeSupport<ActionListener> myChangeSupport = new WeakChangeSupport<>();

    /** Filter for arbitrating input to the text field. */
    private final transient DocumentFilter myDocumentFilter = new DocumentFilter()
    {
        /**
         * This is used to skip updating the text to "0" when we have just set
         * it to space. This is necessary because setting the text to space,
         * causes the slider value to be set to 0.
         */
        private boolean mySetToSpace;

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException
        {
            if (!myTextHasFocus)
            {
                fb.remove(offset, length);
                return;
            }
            String existing = fb.getDocument().getText(0, fb.getDocument().getLength());
            String candidate = existing.substring(0, offset) + existing.substring(offset + length, existing.length());

            if (validateText(candidate))
            {
                fb.remove(offset, length);
                mySetToSpace = candidate.isEmpty();
                int sliderVal = mySetToSpace ? 0 : Integer.parseInt(candidate);
                updateSlider(sliderVal);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException
        {
            if (!myTextHasFocus)
            {
                fb.replace(offset, length, text, attrs);
                return;
            }

            String existing = fb.getDocument().getText(0, fb.getDocument().getLength());
            StringBuilder builder = new StringBuilder(existing.substring(0, offset));
            builder.append(text).append(existing.substring(offset + length, existing.length()));
            String candidate = builder.toString();

            if (mySetToSpace && "0".equals(candidate))
            {
                mySetToSpace = false;
                return;
            }
            if (!candidate.equals(existing) && validateText(candidate))
            {
                fb.replace(offset, length, text, attrs);
                mySetToSpace = candidate.isEmpty();
                int sliderVal = mySetToSpace ? 0 : Integer.parseInt(candidate);
                updateSlider(sliderVal);
            }
        }

        private void updateSlider(final int value)
        {
            EventQueueUtilities.invokeLater(() -> mySlider.setValue(value));
        }

        private boolean validateText(String text)
        {
            if (text.isEmpty())
            {
                return true;
            }
            try
            {
                int value = Integer.parseInt(text);
                return value >= mySlider.getMinimum() && value <= mySlider.getMaximum();
            }
            catch (NumberFormatException e)
            {
                return false;
            }
        }
    };

    /** The label. */
    private final JLabel myLabel;

    /** The parameters for the size of the slider. */
    private final transient PanelSizeParameters myPanelParams;

    /** The Slider. */
    private JSlider mySlider;

    /** The Field. */
    private JTextField myTextField;

    /** When true the text field has the focus. */
    private boolean myTextHasFocus;

    /**
     * Instantiates a new slider field panel.
     *
     * @param sliderLabel the slider label
     * @param min the min slider value
     * @param max the max slider value
     * @param initialValue the slider start value
     * @param params the parameters
     */
    public LinkedSliderTextField(String sliderLabel, int min, int max, int initialValue, PanelSizeParameters params)
    {
        super();
        myPanelParams = params;
        createSlider(min, max, initialValue);
        createTextField(Integer.toString(initialValue));

        setLayout(new GridBagLayout());
        setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        myLabel = new JLabel(sliderLabel);
        add(myLabel, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.insets = new Insets(3, 0, 0, 0);
        gbc.weightx = 1.0;

        add(mySlider, gbc);

        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.weightx = 0;
        add(myTextField, gbc);
    }

    /**
     * Adds the slider field change listener.
     *
     * @param listener the listener
     */
    public void addSliderFieldChangeListener(ActionListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Get the slider.
     *
     * @return the slider
     */
    public JSlider getSlider()
    {
        return mySlider;
    }

    /**
     * Standard accessor.
     *
     * @return The slider value.
     */
    public int getSliderValue()
    {
        return mySlider.getValue();
    }

    /**
     * Get the value of the slider.
     *
     * @return the current value of the slider.
     */
    public int getValue()
    {
        return mySlider.getValue();
    }

    /**
     * Removes the histogram change listener.
     *
     * @param listener the listener
     */
    public void removeSliderFieldChangeListener(ActionListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        myLabel.setEnabled(enabled);
        mySlider.setEnabled(enabled);
        myTextField.setEnabled(enabled);
    }

    /**
     * Sets the value of both the slider and the text field.
     *
     * @param value the new value
     */
    public void setValues(int value)
    {
        mySlider.setValue(value);
        myTextField.setText(Integer.toString(value));
    }

    /**
     * Create the slider.
     *
     * @param min the minimum slider value.
     * @param max the maximum slider value.
     * @param initialValue The value to which to set the slider initially.
     */
    private void createSlider(int min, int max, int initialValue)
    {
        mySlider = new JSlider(SwingConstants.HORIZONTAL, min, max, initialValue);
        if (myPanelParams.getSliderWidth() > 0)
        {
            mySlider.setSize(myPanelParams.getSliderWidth(), 20);
            mySlider.setPreferredSize(mySlider.getSize());
            mySlider.setMinimumSize(mySlider.getSize());
        }

        mySlider.setOpaque(false);
        mySlider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                makeTextMatchSlider();
                myChangeSupport.notifyListeners(new Callback<ActionListener>()
                {
                    @Override
                    public void notify(ActionListener listener)
                    {
                        listener.actionPerformed(
                                new ActionEvent(LinkedSliderTextField.this, mySlider.getValue(), "Value Updated"));
                    }
                });
            }
        });

        mySlider.addMouseWheelListener(new MouseWheelListener()
        {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e)
            {
                moveSlider(-e.getWheelRotation());
            }
        });
    }

    /**
     * Create the text field.
     *
     * @param initialValue The value to which to set the text initially.
     */
    private void createTextField(String initialValue)
    {
        myTextField = new JTextField();
        myTextField.setSize(myPanelParams.getFieldWidth(), myPanelParams.getFieldHeight());
        myTextField.setPreferredSize(myTextField.getSize());
        myTextField.setMinimumSize(myTextField.getSize());
        myTextField.setOpaque(false);
        myTextField.setBackground(ourComponentBackgroundColor);
        myTextField.setEditable(true);
        myTextField.setText(initialValue);
        myTextField.addMouseWheelListener(e -> moveSlider(-e.getWheelRotation()));

        ((AbstractDocument)myTextField.getDocument()).setDocumentFilter(myDocumentFilter);

        myTextField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent evt)
            {
                int keyCode = evt.getKeyCode();
                if (keyCode == KeyEvent.VK_UP)
                {
                    moveSlider(1);
                }
                else if (keyCode == KeyEvent.VK_DOWN)
                {
                    moveSlider(-1);
                }
            }
        });

        FocusListener focusListener = new FocusListener()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                myTextHasFocus = true;
                EventQueueUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        myTextField.selectAll();
                    }
                });
            }

            @Override
            public void focusLost(FocusEvent e)
            {
                myTextHasFocus = false;
                if (myTextField.getText().isEmpty())
                {
                    myTextField.setText("0");
                }
            }
        };

        myTextField.addFocusListener(focusListener);
    }

    /** Set the text so that it matches the current value of the slider. */
    private void makeTextMatchSlider()
    {
        final String text = Integer.toString(mySlider.getValue());
        if (!text.equals(myTextField.getText()))
        {
            EventQueueUtilities.invokeLater(() -> myTextField.setText(text));
        }
    }

    /**
     * Move the slider by the given amount.
     *
     * @param movement the amount to move the slider.
     */
    private void moveSlider(int movement)
    {
        int currentValue = mySlider.getValue();
        int newValue = Math.max(mySlider.getMinimum(), Math.min(currentValue + movement, mySlider.getMaximum()));
        if (newValue != currentValue)
        {
            mySlider.setValue(newValue);
        }
    }

    /**
     * Parameters for the sizing of this panel.
     */
    public static class PanelSizeParameters
    {
        /** The Field height. */
        private final int myFieldHeight;

        /** The Field width. */
        private final int myFieldWidth;

        /** The Slider width. */
        private final int mySliderWidth;

        /**
         * Instantiates a new slider field panel parameters.
         *
         * @param fieldWidth the field width
         * @param fieldHeight the field height
         * @param sliderWidth the slider width
         */
        public PanelSizeParameters(int fieldWidth, int fieldHeight, int sliderWidth)
        {
            myFieldWidth = fieldWidth;
            myFieldHeight = fieldHeight;
            mySliderWidth = sliderWidth;
        }

        /**
         * Gets the field height.
         *
         * @return the field height
         */
        public int getFieldHeight()
        {
            return myFieldHeight;
        }

        /**
         * Gets the field width.
         *
         * @return the field width
         */
        public int getFieldWidth()
        {
            return myFieldWidth;
        }

        /**
         * Gets the slider width.
         *
         * @return the slider width
         */
        public int getSliderWidth()
        {
            return mySliderWidth;
        }
    }
}

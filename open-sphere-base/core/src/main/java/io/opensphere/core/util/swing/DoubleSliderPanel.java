package io.opensphere.core.util.swing;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.text.MaskFormatter;

import org.apache.log4j.Logger;

/**
 * The Class DoubleSliderPanel. This class is a double implementation of a
 * JSlider. It consists of a JSlider and a JTextField and map integer slider
 * positions to double values and vice versa.
 */
public final class DoubleSliderPanel extends AbstractHUDPanel
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DoubleSliderPanel.class);

    /** The Text field. */
    private JFormattedTextField myTextField;

    /** The Slider. */
    private JSlider mySlider;

    /** The Slider width. */
    private final int mySliderWidth;

    /** The Slider label. */
    private final String mySliderLabel;

    /** The Slider min. */
    private int mySliderMin;

    /** The Slider max. */
    private final int mySliderMax;

    /** The Slider rel min. */
    private final double mySliderRelMin;

    /** The Slider rel max. */
    private final double mySliderRelMax;

    /** The Num dec places. */
    private final int myNumDecPlaces;

    /** The Num digits. */
    private final int myNumDigits;

    /** The Formatter string. */
    private final String myFormatterString;

    /** The Dec format. */
    private final DecimalFormat myDecFormat;

    /**
     * Test main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args)
    {
        JFrame aFrame = new JFrame();
        aFrame.add(new DoubleSliderPanel("Opacity:", 0, 1, 3, 150));
        aFrame.pack();
        aFrame.setVisible(true);
    }

    /**
     * Instantiates a new double slider panel.
     *
     * @param label the label
     * @param min the minimum slider value
     * @param max the maximum slider value
     * @param numDecPlaces the number of decimal places for the sliders value
     * @param sliderWidth the slider width
     */
    public DoubleSliderPanel(String label, double min, double max, int numDecPlaces, int sliderWidth)
    {
        super();
        setLayout(new GridBagLayout());
        mySliderLabel = label;
        mySliderRelMin = min;
        mySliderRelMax = max;
        myNumDecPlaces = numDecPlaces;
        mySliderWidth = sliderWidth;
        mySliderMax = (int)((mySliderRelMax - mySliderRelMin) * Math.pow(10, myNumDecPlaces));
        BigDecimal bd = BigDecimal.valueOf(max);
        myNumDigits = Integer.toString(bd.intValue()).length();

        StringBuilder sb = new StringBuilder();
        // Create a format string to match digits and decimal places
        for (int i = 0; i < myNumDigits; i++)
        {
            sb.append('#');
        }
        sb.append('.');
        for (int i = 0; i < myNumDecPlaces; i++)
        {
            sb.append('#');
        }
        myFormatterString = sb.toString();

        myDecFormat = new DecimalFormat(myFormatterString);
        myDecFormat.setMinimumFractionDigits(myNumDecPlaces);

        initialize();
    }

    /**
     * Decrements the slider value which in turn will change the text field
     * value as well.
     */
    public void decrement()
    {
        int value = getSlider().getValue();
        value -= 1;

        if (value < mySliderMin)
        {
            value = mySliderMin;
        }
        getSlider().setValue(value);
    }

    /**
     * Gets the current value from the text field.
     *
     * @return the double value
     */
    public double getDoubleValue()
    {
        return Double.parseDouble(getTextField().getText());
    }

    /**
     * Increments the slider value which in turn will change the text field
     * value as well.
     */
    public void increment()
    {
        int value = getSlider().getValue();
        value += 1;

        if (value > mySliderMax)
        {
            value = mySliderMax;
        }
        getSlider().setValue(value);
    }

    /**
     * Creates the formatter.
     *
     * @param s the string to create the formatter from
     * @return the mask formatter
     */
    private MaskFormatter createFormatter(String s)
    {
        MaskFormatter formatter = null;
        try
        {
            formatter = new MaskFormatter(s);
        }
        catch (java.text.ParseException exc)
        {
            LOGGER.error("formatter is bad: " + exc.getMessage());
        }
        return formatter;
    }

    /**
     * Increments or decrements when mouse wheel is rolled.
     *
     * @param evt the mouse wheel event
     */
    private void doMouseWheelAction(MouseWheelEvent evt)
    {
        int wheelRotation = -1 * evt.getWheelRotation();

        if (wheelRotation > 0)
        {
            increment();
        }
        else
        {
            decrement();
        }
    }

    /**
     * Gets the slider.
     *
     * @return the slider
     */
    private JSlider getSlider()
    {
        if (mySlider == null)
        {
            mySlider = new JSlider(0, mySliderMax, mySliderMax);
            mySlider.setSize(mySliderWidth, 20);
            mySlider.setPreferredSize(mySlider.getSize());
            mySlider.setMinimumSize(mySlider.getSize());
            setTextFieldDisplay(mySliderMax);
            mySlider.addChangeListener(evt ->
            {
                if (evt.getSource() instanceof JSlider && !mySlider.getValueIsAdjusting())
                {
                    int val = getSlider().getValue();
                    setTextFieldDisplay(val);
                }
            });
            mySlider.addMouseWheelListener(e -> doMouseWheelAction(e));
        }
        return mySlider;
    }

    /**
     * Gets the text field.
     *
     * @return the text field
     */
    private JFormattedTextField getTextField()
    {
        if (myTextField == null)
        {
            myTextField = new JFormattedTextField(createFormatter(myFormatterString));
            myTextField.setColumns(myNumDigits + myNumDecPlaces);
            myTextField.setOpaque(false);
            myTextField.setBackground(ourComponentBackgroundColor);
            myTextField.addMouseWheelListener(evt -> doMouseWheelAction(evt));
            myTextField.addKeyListener(new KeyListener()
            {
                @Override
                public void keyPressed(KeyEvent e)
                {
                    keyPressedAction(e);
                }

                @Override
                public void keyReleased(KeyEvent e)
                {
                }

                @Override
                public void keyTyped(KeyEvent e)
                {
                }
            });
            myTextField.addFocusListener(new FocusListener()
            {
                @Override
                public void focusGained(FocusEvent e)
                {
                    selectItLater(e.getComponent());
                }

                @Override
                public void focusLost(FocusEvent e)
                {
                }
            });
        }
        return myTextField;
    }

    /**
     * Handles values in the text field when the 'enter' key is pressed. If an
     * invalid value is entered, sets the slider to max value.
     */
    private void handleEnter()
    {
        double fieldVal = -1;
        try
        {
            fieldVal = Double.parseDouble(getTextField().getText());
        }
        catch (NumberFormatException e)
        {
            fieldVal = mySliderRelMax;
        }

        int sliderPos = (int)((fieldVal - mySliderRelMin) * Math.pow(10, myNumDecPlaces));
        if (sliderPos >= mySliderMin && sliderPos <= mySliderMax)
        {
            getSlider().setValue(sliderPos);
        }
    }

    /**
     * Initialize.
     */
    private void initialize()
    {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel(mySliderLabel), gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.insets = new Insets(3, 0, 0, 0);
        gbc.weightx = 1.0;
        add(getSlider(), gbc);

        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.weightx = 0;
        add(getTextField(), gbc);
    }

    /**
     * Key pressed action.
     *
     * @param keyevent the keyevent
     */
    private void keyPressedAction(KeyEvent keyevent)
    {
        int keyCode = keyevent.getKeyCode();
        if (keyCode == KeyEvent.VK_ENTER)
        {
            handleEnter();
        }
        else if (keyCode == KeyEvent.VK_UP)
        {
            increment();
        }
        else if (keyCode == KeyEvent.VK_DOWN)
        {
            decrement();
        }
    }

    /**
     * Select it later.
     *
     * @param component the component
     */
    private void selectItLater(final Component component)
    {
        if (component instanceof JTextField)
        {
            final JTextField ftf = (JTextField)component;
            EventQueueUtilities.invokeLater(() -> ftf.selectAll());
        }
    }

    /**
     * Sets the text field display value.
     *
     * @param sliderPos the current position of the slider
     */
    private void setTextFieldDisplay(int sliderPos)
    {
        double val = sliderPos / Math.pow(10, myNumDecPlaces) + mySliderRelMin;
        getTextField().setText(myDecFormat.format(val));
    }
}

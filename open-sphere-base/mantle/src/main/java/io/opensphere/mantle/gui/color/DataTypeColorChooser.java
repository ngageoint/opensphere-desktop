package io.opensphere.mantle.gui.color;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.bric.swing.ColorPicker;

import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.ObservableValueListenerHandle;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.ColorCircleIcon;
import io.opensphere.core.util.swing.ColorIcon;
import io.opensphere.core.util.swing.OptionDialog;
import io.opensphere.core.util.swing.SmallColorPalette;
import io.opensphere.core.util.swing.SplitButton;
import io.opensphere.core.util.swing.input.model.ColorModel;

/**
 * UI Component for setting the color for a data type.
 */
public final class DataTypeColorChooser extends SplitButton implements Service
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The parent. */
    private final Frame myParent;

    /** The model. */
    private final ColorModel myModel;

    /** Handle to the model listener. */
    private final ObservableValueListenerHandle<Color> myModelListenerHandle;

    /** The icon. */
    private final ColorCircleIcon myIcon;

    /** The default color. */
    private Color myDefaultColor;

    /**
     * Constructor.
     *
     * @param parent the parent
     * @param model the model
     */
    public DataTypeColorChooser(Frame parent, ColorModel model)
    {
        super(null, null);
        myParent = parent;
        myModel = model;
        myModelListenerHandle = new ObservableValueListenerHandle<>(model, new ChangeListener<Color>()
        {
            @Override
            public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue)
            {
                handleModelChange();
            }
        });
        myIcon = new ColorCircleIcon(model.get(), 12);

        setIcon(myIcon);
        add(createQuickSelectionPanel());
        setToolTipText("Set Layer Color");
        addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                showPicker();
            }
        });
    }

    @Override
    public void open()
    {
        myModelListenerHandle.open();
    }

    @Override
    public void close()
    {
        myModelListenerHandle.close();
    }

    /**
     * Sets the default color.
     *
     * @param defaultColor the default color
     */
    public void setDefaultColor(Color defaultColor)
    {
        myDefaultColor = defaultColor;
    }

    /**
     * Creates the quick selection panel.
     *
     * @return the quick selection panel
     */
    private JPanel createQuickSelectionPanel()
    {
        Consumer<Color> modelUpdator = new Consumer<>()
        {
            @Override
            public void accept(Color color)
            {
                myModel.set(color);
            }
        };

        JPanel panel = new JPanel(new BorderLayout());
        Box buttonBox = Box.createHorizontalBox();
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(createRestoreDefaultButton(modelUpdator, new Insets(2, 2, 2, 2)));
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.setBorder(BorderFactory.createEmptyBorder(4, 3, 2, 3));
        panel.add(buttonBox, BorderLayout.SOUTH);
        panel.add(createPalettePanel(modelUpdator), BorderLayout.CENTER);
        return panel;
    }

    /**
     * Creates the restore default button.
     *
     * @param consumer the color consumer
     * @param margin the margin
     * @return the restore default button
     */
    private JButton createRestoreDefaultButton(final Consumer<Color> consumer, Insets margin)
    {
        JButton button = new JButton("Layer Default");
        button.setFocusPainted(false);
        button.setToolTipText("Reset to default color");
        button.setMargin(margin);
        button.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (myDefaultColor != null)
                {
                    consumer.accept(myDefaultColor);
                }
            }
        });
        return button;
    }

    /**
     * Creates the color palette panel.
     *
     * @param consumer the color consumer
     * @return the color palette panel
     */
    private SmallColorPalette createPalettePanel(final Consumer<Color> consumer)
    {
        SmallColorPalette panel = new SmallColorPalette(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                JButton button = (JButton)evt.getSource();
                if (button.getIcon() instanceof ColorIcon)
                {
                    consumer.accept(((ColorIcon)button.getIcon()).getColor());
                }
            }
        });
        return panel;
    }

    /**
     * Handles a change in the color model.
     */
    private void handleModelChange()
    {
        myIcon.setColor(myModel.get());
        repaint();
    }

    /** Display a color picker to allow selection of a new color. */
    private void showPicker()
    {
        final ColorPicker picker = new ColorPicker(true, true);
        picker.setColor(myIcon.getColor());

        Consumer<Color> pickerUpdator = new Consumer<>()
        {
            @Override
            public void accept(Color color)
            {
                picker.setColor(color);
            }
        };

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(createPalettePanel(pickerUpdator));
        bottomPanel.add(Box.createHorizontalStrut(4));
        bottomPanel.add(createRestoreDefaultButton(pickerUpdator, ButtonPanel.INSETS_MEDIUM));
        bottomPanel.add(Box.createHorizontalStrut(170));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(picker, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        OptionDialog dialog = new OptionDialog(myParent, panel, "Select Layer Color");
        dialog.buildAndShow();
        if (dialog.getSelection() == JOptionPane.OK_OPTION)
        {
            myModel.set(picker.getColor());
        }
    }
}

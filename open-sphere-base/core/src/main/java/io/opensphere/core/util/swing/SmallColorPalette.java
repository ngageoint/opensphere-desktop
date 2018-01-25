package io.opensphere.core.util.swing;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import io.opensphere.core.util.collections.New;

/**
 * A panel with a small assortment of color buttons.
 */
public class SmallColorPalette extends JPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The colors. */
    private final List<Color> myColors = New.list();

    /**
     * Instantiates a new small color palette.
     *
     * @param listener the listener
     */
    public SmallColorPalette(ActionListener listener)
    {
        super(new GridBagLayout());

        myColors.add(Color.RED);
        myColors.add(Color.ORANGE);
        myColors.add(Color.YELLOW);
        myColors.add(Color.GREEN);
        myColors.add(Color.CYAN);
        myColors.add(Color.BLUE);
        myColors.add(Color.MAGENTA);
        myColors.add(Color.PINK);
        myColors.add(Color.WHITE);
        myColors.add(Color.LIGHT_GRAY);
        myColors.add(Color.DARK_GRAY);
        myColors.add(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;

        for (int i = 0; i < myColors.size(); i++)
        {
            if (i > 0)
            {
                if (i % 6 == 0)
                {
                    gbc.gridx = 0;
                    gbc.gridy++;
                }
                else
                {
                    gbc.gridx++;
                }
            }

            add(createColorButton(listener, myColors.get(i)), gbc);
        }
    }

    /**
     * Creates a color button.
     *
     * @param listener the listener
     * @param color the color
     * @return the color button
     */
    private JButton createColorButton(ActionListener listener, Color color)
    {
        JButton colorButton = new JButton();
        colorButton.setSize(18, 18);
        colorButton.setMinimumSize(colorButton.getSize());
        colorButton.setPreferredSize(colorButton.getSize());
        ColorIcon icon = new ColorIcon(color, 13, 13);
        colorButton.setIcon(icon);
        colorButton.setFocusPainted(false);
        colorButton.setBorder(null);
        colorButton.setMargin(new Insets(1, 1, 1, 1));
        colorButton.addActionListener(listener);
        return colorButton;
    }
}

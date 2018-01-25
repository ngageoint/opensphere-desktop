package io.opensphere.core.util.swing;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.UIManager;

import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.input.model.BooleanModel;
import io.opensphere.core.util.swing.input.model.ChoiceModel;
import io.opensphere.core.util.swing.input.model.ColorModel;

/**
 * The Class TextStyleModel.
 */
public class TextStyleModel implements Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The font. */
    private final ChoiceModel<FontWrapper> myFont;

    /** The font size. */
    private final ChoiceModel<Integer> myFontSize;

    /** Whether the font is bold. */
    private final BooleanModel myIsBold = new BooleanModel();

    /** Whether the font is italic. */
    private final BooleanModel myIsItalic = new BooleanModel();

    /** The font color. */
    private final ColorModel myFontColor = new ColorModel();

    /**
     * Returns a new font with the given style change applied.
     *
     * @param font The font
     * @param style The style to apply
     * @param isAdd True to add the style, false to remove it
     * @return The new font
     */
    public static Font changeStyle(Font font, int style, boolean isAdd)
    {
        return font.deriveFont(isAdd ? font.getStyle() | style : font.getStyle() & ~style);
    }

    /**
     * Count if true.
     *
     * @param value the value
     * @return the 1 if true 0 if false.
     */
    public static int countIfTrue(Boolean value)
    {
        return value != null && value.booleanValue() ? 1 : 0;
    }

    /**
     * Constructor.
     */
    public TextStyleModel()
    {
        // Get the available fonts
        List<FontWrapper> fontWrappers = new ArrayList<>();
        for (String family : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())
        {
            Font font = new Font(family, Font.PLAIN, 12);
            if (font.canDisplay('a'))
            {
                fontWrappers.add(new FontWrapper(font));
            }
        }

        myFont = new ChoiceModel<>(fontWrappers.toArray(new FontWrapper[fontWrappers.size()]));
        myFontSize = new ChoiceModel<Integer>(new Integer[] { Integer.valueOf(6), Integer.valueOf(8), Integer.valueOf(10),
            Integer.valueOf(12), Integer.valueOf(14), Integer.valueOf(18), Integer.valueOf(24), Integer.valueOf(32),
            Integer.valueOf(48), Integer.valueOf(64), });
        myFont.setNameAndDescription("Font", "The font of the text");
        myFontSize.setNameAndDescription("Size", "The font size of the text");
        myIsBold.setNameAndDescription("Bold", "Whether the font is bold");
        myIsItalic.setNameAndDescription("Italic", "Whether the font is italic");
        myFontColor.setNameAndDescription("Color", "The font color of the text");
    }

    /**
     * Add a change listener to all of my wrapped models.
     *
     * @param changeListener The change listener.
     */
    public void addChangeListener(ChangeListener<Object> changeListener)
    {
        myFont.addListener(changeListener);
        myFontColor.addListener(changeListener);
        myFontSize.addListener(changeListener);
        myIsBold.addListener(changeListener);
        myIsItalic.addListener(changeListener);
    }

    /**
     * Getter for bold.
     *
     * @return the bold
     */
    public BooleanModel getBold()
    {
        return myIsBold;
    }

    /**
     * Getter for font.
     *
     * @return the font
     */
    public ChoiceModel<FontWrapper> getFont()
    {
        return myFont;
    }

    /**
     * Getter for fontColor.
     *
     * @return the fontColor
     */
    public ColorModel getFontColor()
    {
        return myFontColor;
    }

    /**
     * Getter for fontSize.
     *
     * @return the fontSize
     */
    public ChoiceModel<Integer> getFontSize()
    {
        return myFontSize;
    }

    /**
     * Getter for italic.
     *
     * @return the italic
     */
    public BooleanModel getItalic()
    {
        return myIsItalic;
    }

    /**
     * Creates a Font from the user's selections.
     *
     * @return The selected Font
     */
    public Font getSelectedFont()
    {
        Font font = null;
        if (myFont.get() != null && myFontSize.get() != null && myIsBold.get() != null && myIsItalic.get() != null)
        {
            float size = myFontSize.get().floatValue();
            int style = 0;
            if (myIsBold.get().booleanValue())
            {
                style += Font.BOLD;
            }
            if (myIsItalic.get().booleanValue())
            {
                style += Font.ITALIC;
            }
            font = myFont.get().getFont().deriveFont(style, size);
        }
        return font;
    }

    /**
     * Remove a change listener from all of my wrapped models.
     *
     * @param changeListener The change listener.
     */
    public void removeChangeListener(ChangeListener<Object> changeListener)
    {
        myFont.removeListener(changeListener);
        myFontColor.removeListener(changeListener);
        myFontSize.removeListener(changeListener);
        myIsBold.removeListener(changeListener);
        myIsItalic.removeListener(changeListener);
    }

    /**
     * Set the models to the system defaults.
     */
    public void setDefaults()
    {
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        setFromFont(font);
        myFontColor.set(UIManager.getColor("Label.foreground"));
    }

    /**
     * Set all my models using the given font.
     *
     * @param font The font.
     */
    public void setFromFont(Font font)
    {
        myFont.set(new FontWrapper(font.deriveFont(Font.PLAIN)));

        // Ensure the size from the font is available in the choice model.
        Integer size = Integer.valueOf(font.getSize());
        List<Integer> sizeOptions = myFontSize.getOptions();
        if (!sizeOptions.contains(size))
        {
            sizeOptions = New.list(sizeOptions);
            sizeOptions.add(size);
            Collections.sort(sizeOptions);
            myFontSize.setOptions(New.array(sizeOptions, Integer.class));
        }
        myFontSize.set(size);

        myIsItalic.set(Boolean.valueOf(font.isItalic()));
        myIsBold.set(Boolean.valueOf(font.isBold()));
    }
}

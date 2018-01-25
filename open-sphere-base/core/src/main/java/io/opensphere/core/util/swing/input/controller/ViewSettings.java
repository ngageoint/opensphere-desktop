package io.opensphere.core.util.swing.input.controller;

import java.awt.Color;
import java.util.function.Function;

import javax.swing.ImageIcon;

/**
 * Smörgåsbord of view settings. Follows the builder pattern.
 *
 * @param <T> The type of the model
 */
public class ViewSettings<T>
{
    /** The background. */
    private Color myBackground;

    /** The foreground. */
    private Color myForeground;

    /** The icon provider. */
    private Function<? super T, ImageIcon> myIconProvider;

    /**
     * Options to use in the view (used to override what's in a ChoiceModel).
     */
    private T[] myOptions;

    /** The selection foreground. */
    private Color mySelectionForeground;

    /**
     * Gets the background.
     *
     * @return the background
     */
    public Color getBackground()
    {
        return myBackground;
    }

    /**
     * Get the foreground.
     *
     * @return The foreground.
     */
    public Color getForeground()
    {
        return myForeground;
    }

    /**
     * Gets the icon provider.
     *
     * @return the icon provider
     */
    public Function<? super T, ImageIcon> getIconProvider()
    {
        return myIconProvider;
    }

    /**
     * Gets the options.
     *
     * @return the options
     */
    public T[] getOptions()
    {
        return myOptions != null ? myOptions.clone() : null;
    }

    /**
     * Get the selection foreground.
     *
     * @return The selection foreground.
     */
    public Color getSelectionForeground()
    {
        return mySelectionForeground;
    }

    /**
     * Sets the background.
     *
     * @param background the new background
     * @return this
     */
    public ViewSettings<T> setBackground(Color background)
    {
        myBackground = background;
        return this;
    }

    /**
     * Set the foreground.
     *
     * @param foreground The foreground.
     * @return this
     */
    public ViewSettings<T> setForeground(Color foreground)
    {
        myForeground = foreground;
        return this;
    }

    /**
     * Sets the icon provider.
     *
     * @param iconProvider the icon provider
     * @return this
     */
    public ViewSettings<T> setIconProvider(Function<? super T, ImageIcon> iconProvider)
    {
        myIconProvider = iconProvider;
        return this;
    }

    /**
     * Sets the options.
     *
     * @param options the options
     * @return this
     */
    public ViewSettings<T> setOptions(T[] options)
    {
        myOptions = options.clone();
        return this;
    }

    /**
     * Set the selection foreground.
     *
     * @param selectionForeground The selection foreground.
     * @return this
     */
    public ViewSettings<T> setSelectionForeground(Color selectionForeground)
    {
        mySelectionForeground = selectionForeground;
        return this;
    }
}

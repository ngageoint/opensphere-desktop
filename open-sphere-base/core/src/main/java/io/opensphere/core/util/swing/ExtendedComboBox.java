package io.opensphere.core.util.swing;

import java.awt.Dimension;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

/**
 * A combo box that shows its pop menu past the width of the combo box.
 *
 * @param <E> The type of the items in the combo box.
 */
public class ExtendedComboBox<E> extends JComboBox<E>
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * The width of the popup menu.
     */
    private int myPopupWidth;

    /**
     * Default constructor.
     *
     * @param popupWidth The width of the popup.
     */
    public ExtendedComboBox(int popupWidth)
    {
        super();
        setUI(new ExtendedComboBoxUI(this));
        myPopupWidth = popupWidth;
    }

    /**
     * Constructs the combo box with the given model.
     *
     * @param aModel The combo box model.
     * @param popupWidth The width of the popup.
     */
    public ExtendedComboBox(ComboBoxModel<E> aModel, int popupWidth)
    {
        super(aModel);
        setUI(new ExtendedComboBoxUI(this));
        myPopupWidth = popupWidth;
    }

    /**
     * Constructs the combo box with the specified items.
     *
     * @param items The items to display in the pop up menu.
     * @param popupWidth The width of the popup.
     */
    public ExtendedComboBox(E[] items, int popupWidth)
    {
        super(items);
        setUI(new ExtendedComboBoxUI(this));
        myPopupWidth = popupWidth;
    }

    /**
     * Sets the popup width.
     *
     * @param width The width of the popup.
     */
    public void setPopupWidth(int width)
    {
        myPopupWidth = width;
    }

    /**
     * Gets the popup size.
     *
     * @return The size of the popup.
     */
    public Dimension getPopupSize()
    {
        return new Dimension(myPopupWidth, getSize().height);
    }
}

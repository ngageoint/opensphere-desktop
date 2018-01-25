package io.opensphere.core.util.swing.tags;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.MouseInputAdapter;

import io.opensphere.core.util.collections.New;

/**
 * A field in which a pre-defined set of tags may be chosen by the user with a
 * popup menu.
 */
public class JTagField extends JPanel
{
    /** The unique identifier used for serialization. */
    private static final long serialVersionUID = 6039752385919432581L;

    /** The values selected by the user. */
    private final List<String> myOrderedSelections;

    /** A dictionary of tags selected by the user. */
    private final Map<String, JTag> myValues;

    /** A dictionary of menu items. */
    private final Map<String, JMenuItem> myMenuItems;

    /** The popup menu shown when the user clicks the component. */
    private final JPopupMenu myPopupMenu;

    /** The choices available to the user. */
    private final List<String> myChoices;

    /** The set of change listeners registered for notification. */
    private final Set<Consumer<ChangeEvent>> myChangeListeners;

    /** The color to use for tags within the field. */
    private Color myTagColor;

    /**
     * Creates a new tag field using the supplied list of choices.
     *
     * @param choices the choices with which to populate the tag field.
     */
    public JTagField(List<String> choices)
    {
        this(choices, JTag.DEFAULT_COLOR);
    }

    /**
     * Creates a new tag field using the supplied list of choices and the
     * supplied tag color.
     *
     * @param choices the choices with which to populate the tag field.
     * @param tagColor the color to use for tags within the field.
     */
    public JTagField(List<String> choices, Color tagColor)
    {
        super(new WrappedFlowLayout(WrappedFlowLayout.LEFT, 5, 1));
        myChoices = choices;
        myTagColor = tagColor;
        setPreferredSize(new Dimension(500, 28));
        setMinimumSize(new Dimension(25, 28));
        setBorder(new RoundedBorder(Color.GRAY));
        getInsets().set(0, 0, 0, 0);
        myChangeListeners = New.set();
        myValues = New.map();
        myOrderedSelections = New.list();
        myMenuItems = New.map();
        myPopupMenu = new JPopupMenu();

        choices.forEach(value -> createMenuItem(value));
        rebuildPopup();

        addMouseListener(new MouseInputAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (isEnabled())
                {
                    myPopupMenu.show(JTagField.this, e.getX(), getHeight());
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled)
    {
        for (JTag selectedTag : myValues.values())
        {
            selectedTag.setEnabled(enabled);
        }

        super.setEnabled(enabled);
    }

    /**
     * Sets the value of the {@link #myTagColor} field.
     *
     * @param tagColor the value to store in the {@link #myTagColor} field.
     */
    public void setTagColor(Color tagColor)
    {
        myTagColor = tagColor;
    }

    /**
     * Registers the supplied listener to receive change events.
     *
     * @param listener the listener to register.
     */
    public void addChangeListener(Consumer<ChangeEvent> listener)
    {
        myChangeListeners.add(listener);
    }

    /**
     * De-registers the supplied listener from receiving change events.
     *
     * @param listener the listener to de-register.
     */
    public void removeChangeListener(Consumer<ChangeEvent> listener)
    {
        myChangeListeners.remove(listener);
    }

    /**
     * Notifies all registered listeners that a change to the selected data set
     * has taken place.
     */
    protected void notifyChange()
    {
        ChangeEvent event = new ChangeEvent(this);
        myChangeListeners.forEach(listener -> listener.accept(event));
    }

    /**
     * Sets the collection of available choices to the supplied {@link List}.
     * This method has side-effects, and modifies the menu items, selected
     * values, and ordered selection collections. The popup menu is rebuilt to
     * reflect the new collection of available choices. The collection of
     * selected values ({@link #myValues}) is modified to retain only those
     * items which are valid in the new collection of available choices, and the
     * ordered collection of selected values ({@link #myOrderedSelections}) is
     * likewise modified to retain only the new valid choices.
     *
     * @param choices the collection of available choices.
     */
    public void setChoices(List<String> choices)
    {
        myChoices.clear();
        myChoices.addAll(choices);

        myMenuItems.clear();
        myChoices.forEach(value -> createMenuItem(value));

        // remove all invalid choices from the selected values:
        myValues.keySet().retainAll(myChoices);
        myOrderedSelections.retainAll(myChoices);

        rebuildPopup();
    }

    /**
     * Gets a list of the items selected by the user.
     *
     * @return the set of items selected by the user.
     */
    public List<String> getSelectedItems()
    {
        return myOrderedSelections;
    }

    /**
     * Creates a menu item, and adds it to the {@link #myMenuItems} map. The
     * item is not added to the popup menu.
     *
     * @param value the value for which to create the menu item.
     */
    protected void createMenuItem(String value)
    {
        JMenuItem menuItem = new StayOpenMenuItem(value);
        menuItem.addActionListener(e -> add(value));
        myMenuItems.put(value, menuItem);
    }

    /**
     * Adds the supplied item to the tag field if it is not already present, and
     * if it is a valid choice within the tag set. This method has a side-effect
     * of rebuilding the popup menu to reflect the unchosen options.
     *
     * @param text the text to add to the tag field.
     */
    public void add(String text)
    {
        if (!myValues.containsKey(text) && myChoices.contains(text))
        {
            myOrderedSelections.add(text);
            myValues.put(text, new JTag(text, this::remove, myTagColor));
            add(myValues.get(text));
            rebuildPopup();
            revalidate();
            repaint();
            notifyChange();
        }
    }

    /**
     * Removes the named item from the tag list if it is included in the
     * selection set. If the tag is not included, no action is taken. This
     * method has a side-effect of rebuilding the popup menu to reflect the
     * unchosen options.
     *
     * @param text the text to remove from the tag field.
     */
    public void remove(String text)
    {
        if (myValues.containsKey(text))
        {
            myOrderedSelections.remove(text);
            remove(myValues.remove(text));
            rebuildPopup();
            revalidate();
            repaint();
            notifyChange();
        }
    }

    /**
     * Rebuilds the popup menu with the items available for selection. Already
     * selected items are not included in the popup. Order is preserved from the
     * original collection of choices.
     */
    public void rebuildPopup()
    {
        myPopupMenu.removeAll();
        myChoices.stream().filter(p -> !myValues.containsKey(p)).forEach(value -> myPopupMenu.add(myMenuItems.get(value)));
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g)
    {
        Insets insets = getInsets();
        int x = insets.left;
        int y = insets.top;
        int width = getWidth() - insets.left - insets.right;
        int height = getHeight() - insets.top - insets.bottom;

        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ((Graphics2D)g).setColor(getBackground());
        ((Graphics2D)g).fillRoundRect(x, y, width, height, 12, 12);

        paintChildren(g);
        paintBorder(g);
    }

    public static void main(String[] args)
    {
        JFrame frame = new JFrame("demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTagField tagField = new JTagField(Arrays.asList("One", "Two", "Item Three", "4"));
        tagField.setBackground(Color.BLUE);
        tagField.addChangeListener(JTagField::changed);

        frame.getContentPane().add(tagField, BorderLayout.CENTER);
        JTextField comp = new JTextField(100);
        frame.getContentPane().add(comp, BorderLayout.SOUTH);

        JButton button = new JButton("Get Results");
        button.addActionListener(e -> comp.setText(tagField.getSelectedItems().toString()));
        frame.getContentPane().add(button, BorderLayout.EAST);

        frame.pack();
        frame.setVisible(true);
    }

    public static void changed(ChangeEvent event)
    {
        System.out.println("List changed: " + ((JTagField)event.getSource()).getSelectedItems());
    }
}

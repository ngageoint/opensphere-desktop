package io.opensphere.mantle.data.geom.style.impl.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.apache.log4j.Logger;

import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.core.util.swing.ListComboBoxModel;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;
import io.opensphere.mantle.data.geom.util.ListSupport;

/**
 * An editor that supports editing a list of String values. A finite Collection
 * of allowed values is specified, and then presented to the user as the
 * options. This class also enforces a configurable maximum number of values for
 * which the default is five.
 */
@SuppressWarnings("unchecked")
public class MultiComboEditor extends AbstractStyleParameterEditorPanel
{
    /** Version. */
    private static final long serialVersionUID = 1L;

    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(MultiComboEditor.class);

    /** Combo box background. */
    private static final String COMBOBOX_BACKGROUND = "COMBOBOX_BACKGROUND";

    /** Helps with separating checkbox and dropdown values */
    private ListSupport listSupp = new ListSupport('\\', ',');

    /** Max number of boxes. */
    private int maxBoxes = 5;

    /** List of rows. */
    private final List<ComboRow> rowList = new LinkedList<>();

    /** Ignore edits. */
    private boolean ignoreChange;

    /** List of options. */
    private List<OptionProxy<String>> optList;

    /** Map of option values to themselves. */
    private final Map<String, OptionProxy<String>> optMap = new HashMap<>();

    /** Background color for combo box. */
    private Color boxBgColor;

    /** The root panel in which the components are rendered. */
    private final JPanel mainPanel = new JPanel();
    
    /** The root panel's layout. */
    private final GroupLayout myLayout;

    /** A panel in which the additional combo boxes are displayed. */
    private final JPanel addPanel = createAddPanel();

    /**
     * Instantiate.
     *
     * @param label a bad idea
     * @param style a glorified map of String to editable value
     * @param key a String key that maps to the value to be edited
     */
    public MultiComboEditor(PanelBuilder label, MutableVisualizationStyle style, String key)
    {
        myLayout = new GroupLayout(mainPanel);
        myLayout.setAutoCreateGaps(false);
        myLayout.setAutoCreateContainerGaps(false);
        mainPanel.setLayout(myLayout);

        setStuff(label, style, key);

        Object bg = myPanelBuilder.getOtherParameter(COMBOBOX_BACKGROUND);
        if (bg instanceof Color)
        {
            boxBgColor = (Color)bg;
        }
    }

    /**
     * Specify the maximum number of values allowed by this editor. The default
     * is five.
     *
     * @param n the maximum number of combo-boxes
     */
    public void setMaxBoxes(int n)
    {
        maxBoxes = n;
    }

    /**
     * Provide a ListSupport object for parsing and writing a list as a String.
     * This allows the creator of the editor to control the format of its
     * input/output.
     *
     * @param ls support for list formatting
     */
    public void setListSupport(ListSupport ls)
    {
        listSupp = ls;
    }

    /**
     * Create and layout subcomponents for this editor. Currently selected
     * values are loaded into the editor before it is presented to the user. The
     * superclass setup method is also called before any other work is done.
     *
     * @param num true if and only if the values are numeric
     * @param opts a Collection of options to be presented in the drop-down
     */
    public void setup(boolean num, Collection<String> opts)
    {
        setupAspep();
        setOptions(num, opts);

        if (myControlPanel instanceof JScrollPane)
        {
            ((JScrollPane)myControlPanel).setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            ((JScrollPane)myControlPanel).setViewportView(mainPanel);
            myControlPanel.setBorder(null);
        }
        else
        {        
            GridBagLayout layout = new GridBagLayout();
            
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.insets = new Insets(0, 15, 0, 0);
            constraints.anchor = GridBagConstraints.WEST;
            constraints.gridx = 0;
            constraints.gridy = 0;
            myControlPanel.setLayout(layout);
            
            constraints.gridy++;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.weightx = 1.0;
            layout.setConstraints(mainPanel, constraints);
            myControlPanel.add(mainPanel);
        }
        
        myControlPanel.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                resizeControl();
            }
        });
        
        add(addPanel, BorderLayout.SOUTH);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.impl.ui.AbstractStyleParameterEditorPanel#getMinimumControlPanelWidth()
     */
    @Override
    protected int getMinimumControlPanelWidth()
    {
        return 5;
    }

    /**
     * Sets the options.
     *
     * @param numeric whether the options are numeric
     * @param opts the options
     */
    public void setOptions(boolean numeric, Collection<String> opts)
    {
        optList = sortOptions(opts, numeric);

        // stash in the map
        optMap.clear();
        optList.stream().forEach(p -> optMap.put(p.val, p));

        // load up the values
        rowList.clear();
        Object obj = getParamValue();
        try
        {
            List<String> val = null;
            if (obj instanceof List)
            {
                val = (List<String>)obj;
            }
            else
            {
                val = singleton((String)obj);
            }
            selectValues(val, true);
        }
        catch (RuntimeException eek)
        {
            LOG.error("Runtime exception encountered.", eek);
        }

        layoutGui();
    }

    /** Lays out the UI. */
    private void layoutGui()
    {
        addPanel.setVisible(false);
        mainPanel.removeAll();
        
        GroupLayout.SequentialGroup hGroup = myLayout.createSequentialGroup();
        GroupLayout.SequentialGroup vGroup = myLayout.createSequentialGroup();
        
        GroupLayout.ParallelGroup hName = myLayout.createParallelGroup();
        GroupLayout.ParallelGroup hCombo = myLayout.createParallelGroup();
        GroupLayout.ParallelGroup hUp = myLayout.createParallelGroup();
        GroupLayout.ParallelGroup hDown = myLayout.createParallelGroup();
        GroupLayout.ParallelGroup hDel = myLayout.createParallelGroup();
        
        rowList.stream().forEach(r -> 
        {
            vGroup.addGroup(myLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(r.nameCheck)
                    .addComponent(r.combo)
                    .addComponent(r.up)
                    .addComponent(r.down)
                    .addComponent(r.del));
            
            hName.addComponent(r.nameCheck);
            hCombo.addComponent(r.combo);
            hUp.addComponent(r.up);
            hDown.addComponent(r.down);
            hDel.addComponent(r.del);
        });
        
        hGroup.addGroup(hName).addGroup(hCombo).addGroup(hUp).addGroup(hDown).addGroup(hDel);
        
        myLayout.setHorizontalGroup(hGroup);
        myLayout.setVerticalGroup(vGroup);

        if (rowList.size() < maxBoxes)
        {
            addPanel.setVisible(true);
        }
        
        revalidate();
        repaint();
        
        resizeControl();
    }
    
    /**
     * Updates the control panel's minimum and preferred size, then repaints.
     */
    private void resizeControl()
    {
        int addHeight = 5;
        if (myControlPanel instanceof JScrollPane)
        {
            addHeight += ((JScrollPane)myControlPanel).getHorizontalScrollBar().isVisible() ? 20 : 0;
        }
        Dimension size = new Dimension(myControlPanel.getPreferredSize().width, mainPanel.getPreferredSize().height + addHeight);
        myControlPanel.setMinimumSize(size);
        myControlPanel.setPreferredSize(size);
        
        repaint();
    }

    /**
     * Selects values in the editor.
     * <p>
     * Only call on AWT thread when visible
     *
     * @param vals the values to select
     * @param internal if it's user-activated or not
     */
    @SuppressWarnings("null")
    private void selectValues(List<String> vals, boolean internal)
    {
        int count = 0;
        if (vals != null)
        {
            count = Math.min(maxBoxes, vals.size());
        }
        // adjust the number of JComboBoxes
        ignoreChange = internal;
        while (rowList.size() < count)
        {
            addRow();
        }
        boolean removed = false;
        while (rowList.size() > count)
        {
            rowList.remove(rowList.size() - 1);
            removed = true;
        }
        // assign selections for the rest of them
        for (int i = 0; i < count; i++)
        {
            rowList.get(i).setStringValue(vals.get(i));
        }
        if (removed)
        {
            layoutGui();
        }
        ignoreChange = false;
        showMessage(vals);
    }

    /**
     * Executes action when value changes.
     */
    private void valueChanged()
    {
        if (ignoreChange)
        {
            return;
        }
        // collect up the selected values, ignoring nulls
        List<String> vals = map(rowList, b -> b.getStringValue());
        if (vals.isEmpty())
        {
            vals = null;
        }
        setParamValue(vals);
        showMessage(vals);
    }

    /**
     * Produce a new list of mapped values, with nulls removed.
     *
     * @param orig the original list
     * @param f a mapping function
     * @return the new list
     */
    private static <S, T> List<T> map(List<S> orig, Function<S, T> f)
    {
        List<T> vals = new LinkedList<>();
        orig.stream().map(f).filter(x -> x != null).forEach(x -> vals.add(x));
        return vals;
    }

    @Override
    public void update()
    {
        EventQueueUtilities.runOnEDT(() -> selectValues((List<String>)getParamValue(), false));
    }

    /**
     * Sorts options.
     *
     * @param opts the options to sort
     * @param numeric numeric sort if true, lexical if false
     * @return a sorted list of option proxies
     */
    private List<OptionProxy<String>> sortOptions(Collection<String> opts, boolean numeric)
    {
        List<OptionProxy<String>> ret = new LinkedList<>();
        opts.stream().forEach(t -> ret.add(new OptionProxy<>(t)));
        if (numeric)
        {
            Collections.sort(ret, (left, right) -> left.numCompare(right));
        }
        else
        {
            Collections.sort(ret, (left, right) -> left.lexCompare(right));
        }
        return ret;
    }

    /**
     * Creates the add label panel.
     *
     * @return the panel
     */
    private JPanel createAddPanel()
    {
        JTextArea label = new JTextArea();
        label.setBorder(null);
        label.setEditable(false);
        label.setLineWrap(true);
        label.setWrapStyleWord(true);
        label.setFont(label.getFont().deriveFont(Font.ITALIC));
        label.setText(String.format("Click the plus button to add labels to a maximum of %d.", Integer.valueOf(maxBoxes)));
        
        JButton button = new IconButton(IconType.PLUS, Color.GREEN);
        button.setToolTipText("Add a label");
        button.addActionListener(e -> addRow());

        JPanel p = new JPanel();
        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(gb);

        c.gridwidth = GridBagConstraints.RELATIVE;
        c.gridheight = 2;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        gb.setConstraints(label, c);
        p.add(label);
        
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        gb.setConstraints(button, c);
        p.add(button);
        
        return p;
    }

    /**
     * Adds a row to the editor.
     */
    private void addRow()
    {
        ComboRow row = new ComboRow();
        row.nameCheck.addActionListener(e -> valueChanged());
        row.combo.setModel(new ListComboBoxModel<>(optList));
        row.combo.addActionListener(e -> valueChanged());
        row.combo.setBackground(boxBgColor);
        row.up.addActionListener(e -> moveUp(row));
        row.down.addActionListener(e -> moveDown(row));
        row.del.addActionListener(e -> deleteRow(row));

        rowList.add(row);
        checkMovability();
        layoutGui();
        valueChanged();
    }

    /**
     * Checks if a row can be moved.
     */
    private void checkMovability()
    {
        int k = 0;
        for (ComboRow row : rowList)
        {
            row.up.setEnabled(k > 0);
            row.down.setEnabled(k < rowList.size() - 1);
            k++;
        }
    }

    /**
     * Moves a row up one space.
     *
     * @param row the row to move
     */
    private void moveUp(ComboRow row)
    {
        int k = rowList.indexOf(row);
        if (k < 1)
        {
            return;
        }
        rowList.remove(k);
        rowList.add(k - 1, row);
        checkMovability();
        layoutGui();
        valueChanged();
    }

    /**
     * Moves a row down one space.
     *
     * @param row the row to move
     */
    private void moveDown(ComboRow row)
    {
        int k = rowList.indexOf(row);
        if (k == rowList.size() - 1)
        {
            return;
        }
        rowList.remove(k);
        rowList.add(k + 1, row);
        checkMovability();
        layoutGui();
        valueChanged();
    }

    /**
     * Deletes a row.
     *
     * @param row the row to delete
     */
    private void deleteRow(ComboRow row)
    {
        rowList.remove(row);
        checkMovability();
        layoutGui();
        valueChanged();
    }

    /**
     * Gets the selected item in a combo box.
     *
     * @param box the box
     * @return the selected item
     */
    private static String getVal(JComboBox<OptionProxy<String>> box)
    {
        Object selectedItem = box.getSelectedItem();
        return selectedItem != null ? ((OptionProxy<String>)selectedItem).val : null;
    }

    /**
     * This simple class unites the components associated with one displayed row
     * within the containing editor class. It also includes some convenience
     * methods.
     */
    private class ComboRow
    {
        /** check box (current use: include column names). */
        public JCheckBox nameCheck = new JCheckBox();

        /** value selector. */
        public JComboBox<OptionProxy<String>> combo = new JComboBox<>();

        /** move-up button. */
        public JButton up = new IconButton(IconType.MOVE_UP);

        /** move-down button. */
        public JButton down = new IconButton(IconType.MOVE_DOWN);

        /** delete button. */
        public JButton del = new IconButton(IconType.CLOSE, Color.RED);

        /**
         * Install the specified value for this row. The value should be a
         * formatted list where the first element is a boolean and is attributed
         * to the checkbox and the second element is a general String and is
         * attributed to the combo-box.
         *
         * @param v the formatted list of fields
         */
        public void setStringValue(String v)
        {
            List<String> fields = listSupp.parseList(v);
            boolean check = false;
            String sel = null;
            if (fields.size() > 1)
            {
                check = Boolean.parseBoolean(fields.get(0));
                sel = fields.get(1);
            }
            else if (fields.size() == 1)
            {
                sel = fields.get(0);
            }
            nameCheck.setSelected(check);
            combo.setSelectedItem(optMap.get(sel));
        }

        /**
         * Obtain a formatted list representing the current state of this row.
         * The values are, in order, a boolean value for the checkbox and the
         * selection from the combo-box.
         *
         * @return the formatted list of fields
         */
        public String getStringValue()
        {
            String sel = getVal(combo);
            if (sel == null)
            {
                return null;
            }

            List<String> fields = new LinkedList<>();
            fields.add(Boolean.toString(nameCheck.isSelected()));
            fields.add(sel);
            return listSupp.writeList(fields);
        }
    }

    /**
     * A null-tolerant wrapper for drop-down options. The toString method
     * returns "NONE" in case the embedded reference is null; otherwise, it
     * delegates to value's toString method. This class also implements
     * comparison methods useful for numerical and lexical ordering.
     *
     * @param <T> the type of wrapped object
     */
    private static class OptionProxy<T>
    {
        /** the embedded reference. */
        private final T val;

        /**
         * Like, ya know, totally construct it.
         *
         * @param t the embedded option value
         */
        public OptionProxy(T t)
        {
            val = t;
        }

        @Override
        public String toString()
        {
            if (val == null)
            {
                return "NONE";
            }
            return val.toString();
        }

        /**
         * Compare this OptionProxy to another according to lexical order. As is
         * common practice in Java, return a negative number if this one is less
         * than the other, a positive number if it is greater, or zero if they
         * are equivalent. Note: this operation is not null-tolerant.
         *
         * @param p the other OptionProxy
         * @return an integer result (see above)
         */
        public int lexCompare(OptionProxy<T> p)
        {
            return val.toString().compareTo(p.val.toString());
        }

        /**
         * Compare this OptionProxy to another according to numerical order, if
         * possible. If the embedded values are Numbers or are Strings that can
         * be parsed as such, then they are compared as floating-point numerical
         * values. If that comparison fails for any reason, then lexical
         * comparison of String values is used as a fallback. Note: this
         * operation is not null-tolerant.
         *
         * @param p the other OptionProxy
         * @return an integer result (see above)
         */
        public int numCompare(OptionProxy<T> p)
        {
            try
            {
                return Double.compare(doubleVal(), p.doubleVal());
            }
            catch (RuntimeException eek)
            {
                return lexCompare(p);
            }
        }

        /**
         * Get the numerical value, if possible.
         *
         * @return the value as a double
         */
        private double doubleVal()
        {
            if (val instanceof Number)
            {
                return ((Number)val).doubleValue();
            }
            return Double.parseDouble(val.toString());
        }
    }

    /**
     * Construct a mutable List containing the specified element. If a null
     * given, then the result is an empty List.
     *
     * @param e the sole element of the resulting list or null
     * @param <E> the element type
     * @return a mutable list containing at most one element
     */
    private static <E> List<E> singleton(E e)
    {
        LinkedList<E> ret = new LinkedList<>();
        addNonNull(ret, e);
        return ret;
    }

    /**
     * Add an element to a list, ignoring null values.
     *
     * @param list the List to be extended, maybe
     * @param e the element to add, if it is not null
     * @param <E> the element type
     */
    private static <E> void addNonNull(List<E> list, E e)
    {
        if (e != null)
        {
            list.add(e);
        }
    }
}

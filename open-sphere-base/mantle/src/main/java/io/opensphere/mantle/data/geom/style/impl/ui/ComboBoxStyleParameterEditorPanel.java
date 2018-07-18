package io.opensphere.mantle.data.geom.style.impl.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.ListComboBoxModel;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;

/**
 * The Class AbstractStyleParameterEditorPanel.
 */
public class ComboBoxStyleParameterEditorPanel extends AbstractStyleParameterEditorPanel implements ActionListener
{
    /** The Constant COMBOBOX_BACKGROUND. */
    public static final String COMBOBOX_BACKGROUND = "COMBOBOX_BACKGROUND";

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Choice to proxy map. */
    private final Map<Object, OptionProxy<Object>> myChoiceToProxyMap;

    /** The Slider. */
    private final JComboBox<OptionProxy<Object>> myComboBox;

    /** The Combo box panel. */
    protected final JComponent myComboBoxPanel;

    /** The Options. */
    @SuppressWarnings("PMD.SingularField")
    private final List<OptionProxy<Object>> myOptions;

    /**
     * Instantiates a new abstract style parameter editor panel.
     *
     * @param label the label
     * @param style the style
     * @param paramKey the param key
     * @param previewable the previewable
     * @param numericChoices the numeric choices
     * @param noneOption the none option
     * @param options the options
     */
    public ComboBoxStyleParameterEditorPanel(PanelBuilder label, MutableVisualizationStyle style, String paramKey,
            boolean previewable, boolean numericChoices, boolean noneOption, Collection<? extends Object> options)
    {
        super(label, style, paramKey);

        myChoiceToProxyMap = New.map();
        myOptions = convertAndSortList(myChoiceToProxyMap, options, numericChoices, noneOption);

        myComboBox = new JComboBox<>(new ListComboBoxModel<>(myOptions));
        myComboBox.setSelectedItem(myChoiceToProxyMap.get(getParamValue()));
        Color cbBackground = getComboboxBackgroundFromBuilder(label);
        if (cbBackground != null)
        {
            myComboBox.setBackground(cbBackground);
        }

        myComboBoxPanel = Box.createHorizontalBox();
        myComboBoxPanel.add(myComboBox);
        myComboBoxPanel.add(Box.createHorizontalGlue());

        myControlPanel.setLayout(new BorderLayout());
        myControlPanel.add(myComboBoxPanel, BorderLayout.CENTER);

        myComboBox.addActionListener(this);

        showMessage(getParamValue());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e == null)
        {
            System.out.println("Null Action Event!");
        }
        if (myComboBox == null)
        {
            System.out.println("Null Combo box!");
        }
        else if (myComboBox.getSelectedItem() == null)
        {
            System.out.println("Null Selected Item!");
        }
        if (e.getSource() == myComboBox && !myComboBox.getSelectedItem().equals(getParamValue()))
        {
            Object option = ((OptionProxy<Object>)myComboBox.getSelectedItem()).getOption();
            setParamValue(option);
            showMessage(option);
        }
    }

    /**
     * Gets the combobox background from builder.
     *
     * @param pb the pb
     * @return the combobox background from builder
     */
    public final Color getComboboxBackgroundFromBuilder(PanelBuilder pb)
    {
        Object val = pb.getOtherParameter(COMBOBOX_BACKGROUND);
        return val instanceof Color ? (Color)val : null;
    }

    @Override
    public void update()
    {
        final Object pVal = getParamValue();
        final Object val = myChoiceToProxyMap.get(pVal);
        if (!Objects.equals(val, myComboBox.getSelectedItem()))
        {
            EventQueueUtilities.runOnEDT(() ->
            {
                myComboBox.setSelectedItem(val);
                showMessage(pVal);
            });
        }
    }

    /**
     * Convert and sort list.
     *
     * @param choiceToProxyMap the choice to proxy map
     * @param choiceList the choice list
     * @param numericChoices the numeric choices
     * @param noneOption the none option
     * @return the list
     */
    protected final List<OptionProxy<Object>> convertAndSortList(Map<Object, OptionProxy<Object>> choiceToProxyMap,
            Collection<? extends Object> choiceList, final boolean numericChoices, boolean noneOption)
    {
        List<OptionProxy<Object>> result = New.list(choiceList.size());
        for (Object o : choiceList)
        {
            OptionProxy<Object> proxy = new OptionProxy<>(o);
            choiceToProxyMap.put(o, proxy);
            result.add(proxy);
        }
        Collections.sort(result, new Comparator<Object>()
        {
            @Override
            public int compare(Object o1, Object o2)
            {
                if (numericChoices)
                {
                    try
                    {
                        double v1 = o1 instanceof Number ? ((Number)o1).doubleValue() : Double.parseDouble(o1.toString());
                        double v2 = o2 instanceof Number ? ((Number)o2).doubleValue() : Double.parseDouble(o2.toString());
                        return Double.compare(v1, v2);
                    }
                    catch (NumberFormatException e)
                    {
                        return o1.toString().compareTo(o2.toString());
                    }
                }
                return o1.toString().compareTo(o2.toString());
            }
        });

        if (noneOption)
        {
            OptionProxy<Object> nullProxy = new OptionProxy<>(null);
            choiceToProxyMap.put(null, nullProxy);
            result.add(0, nullProxy);
        }

        return result;
    }

    /**
     * The Class OptionProxy.
     *
     * @param <T> the generic type
     */
    public static class OptionProxy<T extends Object>
    {
        /** The Object. */
        private final T myObject;

        /**
         * Instantiates a new option proxy.
         *
         * @param obj the obj
         */
        public OptionProxy(T obj)
        {
            myObject = obj;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            OptionProxy other = (OptionProxy)obj;
            return Objects.equals(myObject, other.myObject);
        }

        /**
         * Gets the object.
         *
         * @return the object
         */
        public T getOption()
        {
            return myObject;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + (myObject == null ? 0 : myObject.hashCode());
            return result;
        }

        @Override
        public String toString()
        {
            if (myObject == null)
            {
                return "NONE";
            }
            else
            {
                return myObject.toString();
            }
        }
    }
}

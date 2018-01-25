package io.opensphere.mantle.data.geom.style.impl.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.ListComboBoxModel;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;

/**
 * The Class AbstractStyleParameterEditorPanel.
 */
public class MultiComboBoxStyleTwoParameterEditorPanel extends ComboBoxStyleParameterEditorPanel
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Choice to proxy map. */
    private final Map<Object, OptionProxy<Object>> mySecondChoiceToProxyMap;

    /** The Slider. */
    private final JComboBox<OptionProxy<Object>> mySecondComboBox;

    /** The Options. */
    @SuppressWarnings("PMD.SingularField")
    private final List<OptionProxy<Object>> mySecondOptions;

    /** The Second parameter. */
    private final String mySecondParameterKey;

    /**
     * Instantiates a new abstract style parameter editor panel.
     *
     * @param label the label
     * @param style the style
     * @param paramKey1 the param key1
     * @param paramKey2 the param key2
     * @param previewable the previewable
     * @param options1 the options1
     * @param opt1Numeric the opt1 numeric
     * @param opt1ShowNone the opt1 show none
     * @param options2 the options2
     * @param opt2Numeric the opt2 numeric
     * @param opt2ShowNone the opt2 show none
     * @param labelOpt2 the label opt2
     */
    public MultiComboBoxStyleTwoParameterEditorPanel(PanelBuilder label, MutableVisualizationStyle style, String paramKey1,
            String paramKey2, boolean previewable, Collection<? extends Object> options1, boolean opt1Numeric,
            boolean opt1ShowNone, Collection<? extends Object> options2, boolean opt2Numeric, boolean opt2ShowNone,
            String labelOpt2)
    {
        super(label, style, paramKey1, previewable, opt1Numeric, opt1ShowNone, options1);

        mySecondParameterKey = paramKey2;
        mySecondChoiceToProxyMap = New.map();
        mySecondOptions = convertAndSortList(mySecondChoiceToProxyMap, options2, opt2Numeric, opt2ShowNone);
        mySecondComboBox = new JComboBox<>(new ListComboBoxModel<>(mySecondOptions));
        mySecondComboBox.setSelectedItem(mySecondChoiceToProxyMap.get(getSecondParameterValue()));

        Color cbBackground = getComboboxBackgroundFromBuilder(label);
        if (cbBackground != null)
        {
            mySecondComboBox.setBackground(cbBackground);
        }
//        Dimension d = mySecondComboBox.getPreferredSize();
//        mySecondComboBox.setPreferredSize(new Dimension(d.width - 50, d.height));
//        mySecondComboBox.setMaximumSize(new Dimension(d.width - 50, 30));
//        getComboBoxPanel().add(Box.createHorizontalStrut(5));
//        getComboBoxPanel().add(mySecondComboBox);
//        if (labelOpt2 != null)
//        {
//            getComboBoxPanel().add(Box.createHorizontalStrut(5));
//            getComboBoxPanel().add(new JLabel(labelOpt2));
//        }

        mySecondComboBox.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        super.actionPerformed(e);
        if (e.getSource() == mySecondComboBox)
        {
            @SuppressWarnings("unchecked")
            Object opt = ((OptionProxy<Object>)mySecondComboBox.getSelectedItem()).getOption();
            if (!EqualsHelper.equals(opt, getSecondParameterValue()))
            {
                myStyle.setParameter(mySecondParameterKey, opt, this);
            }
        }
    }

    /**
     * Update.
     */
    @Override
    public final void update()
    {
        super.update();

        Object val = mySecondChoiceToProxyMap.get(getSecondParameterValue());
        if (!val.equals(mySecondComboBox.getSelectedItem()))
        {
            final Object fVal = val;
            EventQueueUtilities.runOnEDT(() -> mySecondComboBox.setSelectedItem(fVal));
        }
    }

    /**
     * Adds the other components.
     *
     * @param cbPanel the cb panel
     */
    @Override
    protected void addOtherComponents(JPanel cbPanel)
    {
        cbPanel.add(Box.createHorizontalStrut(5));
        cbPanel.add(mySecondComboBox);
    }

    /**
     * Gets the parameter value.
     *
     * @return the parameter value
     */
    private Object getSecondParameterValue()
    {
        return myStyle.getStyleParameter(mySecondParameterKey).getValue();
    }
}

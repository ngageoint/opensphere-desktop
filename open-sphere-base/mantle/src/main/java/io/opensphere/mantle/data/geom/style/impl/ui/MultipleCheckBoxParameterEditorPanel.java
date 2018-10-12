package io.opensphere.mantle.data.geom.style.impl.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Nulls;
import io.opensphere.core.util.lang.NumberUtilities;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;

/**
 * A multiple check box AbstractStyleParameterEditorPanel.
 */
public class MultipleCheckBoxParameterEditorPanel extends AbstractStyleParameterEditorPanel implements ActionListener
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The map of check box to parameter key. */
    private final Map<AbstractButton, String> myCheckBoxMap = New.map();

    /**
     * Constructor.
     *
     * @param label the {@link PanelBuilder}
     * @param style the style
     * @param paramKeys the param keys
     */
    public MultipleCheckBoxParameterEditorPanel(PanelBuilder label, MutableVisualizationStyle style, String... paramKeys)
    {
        super(label, style, null);

        GridBagPanel panel = new GridBagPanel();
        int firstIndent = NumberUtilities.parseInt((String)label.getOtherParameter("firstIndent"), 4);
        for (String paramKey : paramKeys)
        {
            VisualizationStyleParameter styleParameter = myStyle.getStyleParameter(paramKey);
            Boolean value = (Boolean)styleParameter.getValue();
            JCheckBox checkBox = new JCheckBox(Nulls.STRING, value.booleanValue());
            int indent = myCheckBoxMap.isEmpty() ? firstIndent : 4;
            panel.setInsets(0, 0, 0, indent).add(new JLabel(styleParameter.getName()));
            panel.setInsets(0, 0, 0, 9).add(checkBox);

            myCheckBoxMap.put(checkBox, paramKey);
        }
        panel.fillHorizontalSpace();

        myControlPanel.setLayout(new BorderLayout());
        myControlPanel.add(panel, BorderLayout.CENTER);

        update();

        myCheckBoxMap.keySet().forEach(c -> c.addActionListener(this));
    }

    @Override
    public final void update()
    {
        assert EventQueue.isDispatchThread();

        for (Map.Entry<AbstractButton, String> entry : myCheckBoxMap.entrySet())
        {
            AbstractButton checkBox = entry.getKey();
            String paramKey = entry.getValue();

            Boolean value = (Boolean)myStyle.getStyleParameter(paramKey).getValue();
            checkBox.setSelected(value.booleanValue());
        }
    }

    /**
     * Called when one of the check boxes is selected.
     *
     * @param e the event
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        AbstractButton checkBox = (AbstractButton)e.getSource();
        String paramKey = myCheckBoxMap.get(checkBox);
        myStyle.setParameter(paramKey, Boolean.valueOf(checkBox.isSelected()), this);
    }
}

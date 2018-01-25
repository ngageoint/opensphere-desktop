package io.opensphere.hud.dashboard.widget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import io.opensphere.core.Toolbox;
import io.opensphere.core.metrics.MetricsProvider;
import io.opensphere.core.metrics.NumberMetricsProvider;
import io.opensphere.core.util.lang.EqualsHelper;

/**
 * The Class DefaultLabelValueDashboardWidget.
 */
public class DefaultLabelValueDashboardWidget extends DashboardWidget
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Label panel. */
    @SuppressWarnings("PMD.SingularField")
    private final JPanel myLabelPanel;

    /** The Value panel. */
    @SuppressWarnings("PMD.SingularField")
    private final JPanel myValuePanel;

    /** The Label. */
    @SuppressWarnings("PMD.SingularField")
    private final JLabel myLabel;

    /** The Value. */
    private final JLabel myValue;

    /** The Decorator. */
    private NumericWidgetDecorator myDecorator;

    /** The Original label color. */
    private final Color myOriginalLabelColor;

    /**
     * Instantiates a new default label value dash board widget.
     *
     * @param provider the provider
     * @param toolbox The system toolbox
     */
    public DefaultLabelValueDashboardWidget(MetricsProvider provider, Toolbox toolbox)
    {
        super(provider);

        myLabelPanel = new JPanel(new BorderLayout());
        myLabelPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 5));
        myLabelPanel.setPreferredSize(new Dimension(120, 20));
        myLabelPanel.setMaximumSize(new Dimension(1000, 20));
        myLabelPanel.setMinimumSize(new Dimension(120, 20));
        myLabel = new JLabel(provider.getLabel() == null ? "" : provider.getLabel());
        myLabelPanel.add(myLabel, BorderLayout.CENTER);
        myLabel.setFont(myLabel.getFont().deriveFont(Font.BOLD));
        myOriginalLabelColor = myLabel.getForeground();

        myValue = new JLabel(provider.getValue());
        myValuePanel = new JPanel(new BorderLayout());
//        myValue.setBorder(BorderFactory.createLineBorder(Color.red));
        myValuePanel.add(myValue, BorderLayout.CENTER);

        if (provider instanceof NumberMetricsProvider)
        {
            myDecorator = new NumericWidgetDecorator((NumberMetricsProvider)provider,
                    toolbox.getPreferencesRegistry().getPreferences(NumericWidgetDecorator.class));
            myDecorator.setMinimumSize(new Dimension(50, 20));
            myDecorator.setPreferredSize(new Dimension(50, 20));
            myValuePanel.add(myDecorator, BorderLayout.EAST);
        }

        setPreferredSize(new Dimension(200, 20));
        setMaximumSize(new Dimension(1000, 20));
        setMinimumSize(new Dimension(100, 20));

        add(myLabelPanel, BorderLayout.WEST);
        add(myValuePanel, BorderLayout.CENTER);
    }

    @Override
    public void updateFromProvider(MetricsProvider provider)
    {
        String value = provider.getValue();
        if (!value.equals(myValue.getText()))
        {
            myValue.setText(value);
        }
        if (!EqualsHelper.equals(myValue.getForeground(), provider.getColor()))
        {
            myValue.setForeground(provider.getColor() == null ? myOriginalLabelColor : provider.getColor());
        }
        if (myDecorator != null && provider instanceof NumberMetricsProvider)
        {
            myDecorator.addValue((NumberMetricsProvider)provider);
        }
    }
}

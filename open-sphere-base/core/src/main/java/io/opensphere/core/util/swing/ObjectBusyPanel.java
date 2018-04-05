package io.opensphere.core.util.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXPanel;

/**
 * The Class ObjectBusyPanel.
 */
public abstract class ObjectBusyPanel extends JXPanel
{
    /** The serialVersionUID constant. */
    private static final long serialVersionUID = 1L;

    /** The Busy label. */
    private JXBusyLabel myBusyLabel;

    /** The Busy panel. */
    private JPanel myBusyPanel;

    /** The Message. */
    private JLabel myMessage;

    /** The Component. */
    private Component myComponent;

    /** The GridBagConstraints. */
    private final GridBagConstraints myGbc;

    /**
     * Instantiates a new object busy panel.
     */
    public ObjectBusyPanel()
    {
        this(0);
    }

    /**
     * Instantiates a new object busy panel.
     *
     * @param comp the component
     */
    public ObjectBusyPanel(Component comp)
    {
        this();
        setComponentInternal(comp);
    }

    /**
     * Instantiates a new object busy panel.
     *
     * @param comp the comp
     * @param delayBeforeBusyShowsMS the delay before busy shows ms
     */
    public ObjectBusyPanel(Component comp, int delayBeforeBusyShowsMS)
    {
        this(delayBeforeBusyShowsMS);
        setComponentInternal(comp);
    }

    /**
     * Instantiates a new object busy panel.
     *
     * @param delayBeforeBusyShowsMS the delay before busy shows ms
     */
    public ObjectBusyPanel(int delayBeforeBusyShowsMS)
    {
        super(new GridBagLayout());

        myGbc = new GridBagConstraints();
        myGbc.gridx = 0;
        myGbc.gridy = 0;
        myGbc.anchor = GridBagConstraints.CENTER;
        myGbc.fill = GridBagConstraints.BOTH;
        myGbc.weightx = 1.0;
        myGbc.weighty = 1.0;
    }

    /**
     * Starts/stops the busy label.
     *
     * @param isBusy the new busy
     */
    public void setBusy(boolean isBusy)
    {
        setBusy(isBusy, null);
    }

    /**
     * Starts/stops the busy label.
     *
     * @param isBusy the new busy
     * @param busyText the busy text
     */
    public void setBusy(boolean isBusy, String busyText)
    {
        assert EventQueue.isDispatchThread();

        getBusyLabel().setBusy(isBusy);
        myMessage.setText(StringUtils.isBlank(busyText) ? "" : busyText);
        myMessage.setVisible(!StringUtils.isBlank(myMessage.getText()));
        setEnable(!isBusy);

        if (isBusy)
        {
            setComponentZOrder(getBusyPanel(), 0);
            setComponentZOrder(myComponent, 1);
        }
        else
        {
            setComponentZOrder(getBusyPanel(), 1);
            setComponentZOrder(myComponent, 0);
        }
    }

    /**
     * Sets the enable.
     *
     * @param enable the new enable
     */
    public abstract void setEnable(boolean enable);

    /**
     * Sets the component.
     *
     * @param comp the new component
     */
    public void setPanel(Component comp)
    {
        setComponentInternal(comp);
    }

    /**
     * Gets the busy label.
     *
     * @return the busy label
     */
    private JXBusyLabel getBusyLabel()
    {
        if (myBusyLabel == null)
        {
            myBusyLabel = new JXBusyLabel(new Dimension(100, 100));
            myBusyLabel.getBusyPainter().setBaseColor(Color.CYAN);
            myBusyLabel.getBusyPainter().setPoints(15);
            myBusyLabel.getBusyPainter().setTrailLength(5);
        }
        return myBusyLabel;
    }

    /**
     * Gets the busy panel.
     *
     * @return the busy panel
     */
    private JPanel getBusyPanel()
    {
        if (myBusyPanel == null)
        {
            myBusyPanel = new JPanel(new GridBagLayout());
            myBusyPanel.setOpaque(false);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;

            myMessage = new JLabel("");
            myMessage.setForeground(Color.CYAN);
            myMessage.setHorizontalTextPosition(SwingConstants.CENTER);
            myMessage.setHorizontalAlignment(SwingConstants.CENTER);
            myMessage.setFont(myMessage.getFont().deriveFont(Font.BOLD, myMessage.getFont().getSize() + 3));

            JPanel messagePanel = new JPanel(new BorderLayout());
            messagePanel.setBackground(new Color(0, 0, 0, 200));
            messagePanel.add(myMessage, BorderLayout.CENTER);

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.setOpaque(false);
            centerPanel.add(getBusyLabel(), BorderLayout.CENTER);
            centerPanel.add(messagePanel, BorderLayout.SOUTH);

            myBusyPanel.add(centerPanel, gbc);
        }
        return myBusyPanel;
    }

    /**
     * Sets the component.
     *
     * @param comp the new component
     */
    private void setComponentInternal(Component comp)
    {
        myComponent = comp;
        if (getComponentCount() > 0)
        {
            removeAll();
        }
        add(myComponent, myGbc);
        add(getBusyPanel(), myGbc);
    }
}

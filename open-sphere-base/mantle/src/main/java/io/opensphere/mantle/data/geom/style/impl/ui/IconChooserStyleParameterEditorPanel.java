package io.opensphere.mantle.data.geom.style.impl.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import io.opensphere.core.util.image.ImageUtil;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.iconproject.view.IconProjDialog;
import javafx.scene.control.ButtonBar.ButtonData;

/**
 * The Class IconChooserStyleParameterEditorPanel.
 */
public class IconChooserStyleParameterEditorPanel extends AbstractStyleParameterEditorPanel implements ActionListener
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Button. */
    private final JButton myButton;

    /**
     * Load icon.
     *
     * @param iconWidth the icon width
     * @param iconURL the icon url
     * @return the buffered image
     */
    private static Image loadIcon(int iconWidth, URL iconURL)
    {
        BufferedImage icon = null;
        try
        {
            icon = ImageIO.read(iconURL);
        }
        catch (IOException e)
        {
            icon = ImageUtil.BAD_URL_IMAGE;
        }

        if (icon == null)
        {
            icon = ImageUtil.BROKEN_IMAGE;
        }

        return ImageUtil.scaleDownImage(icon, iconWidth, iconWidth);
    }

    /**
     * Instantiates a new IconChooserStyleParameterEditorPanel.
     *
     * @param label the label
     * @param style the style
     * @param paramKey the param key
     * @param previewable the previewable
     */
    public IconChooserStyleParameterEditorPanel(PanelBuilder label, MutableVisualizationStyle style, String paramKey,
            boolean previewable)
    {
        super(label, style, paramKey);
        myButton = new JButton();
        Dimension d = new Dimension(60, 60);
        myButton.setMinimumSize(d);
        myButton.setPreferredSize(d);
        myButton.setFocusable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(myButton);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(new JLabel("Select Icon"));
        myControlPanel.setLayout(new BorderLayout());
        myControlPanel.add(panel, BorderLayout.CENTER);
        int panelHeight = getPanelHeightFromBuilder();
        myControlPanel.setMinimumSize(new Dimension(80, panelHeight));
        myControlPanel.setPreferredSize(new Dimension(80, panelHeight));
        myControlPanel.setMaximumSize(new Dimension(1200, panelHeight));
        myButton.addActionListener(this);
        update();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == myButton)
        {
            IconProjDialog fileDialog = new IconProjDialog(myStyle.getToolbox().getUIRegistry().getMainFrameProvider().get(),
                    myStyle.getToolbox(), true, false);
            fileDialog.setVisible(true);

            if (!(fileDialog.getResponse() == null || fileDialog.getResponse().equals(ButtonData.CANCEL_CLOSE)))
            {
                IconRecord rec = fileDialog.getMyPanelModel().getSelectedRecord().get();
                setParamValue(rec.getImageURL().toString());
            }
        }
    }

    /** Update. */
    @Override
    public final void update()
    {
        final String val = getParameterValue();
        if (val != null)
        {
            EventQueueUtilities.runOnEDT(() ->
            {
                Image image;
                try
                {
                    image = loadIcon(56, new URL(val));
                }
                catch (MalformedURLException e)
                {
                    image = ImageUtil.BROKEN_IMAGE;
                }
                myButton.setIcon(new ImageIcon(image));
            });
        }
    }

    /**
     * Gets the parameter value.
     *
     * @return the parameter value
     */
    private String getParameterValue()
    {
        Object value = getParamValue();
        return value instanceof String ? (String)value : null;
    }
}

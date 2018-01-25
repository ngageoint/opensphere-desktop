package io.opensphere.mantle.data.geom.style.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXImagePanel;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * The Class VisualizationStyleDataTypePanel.
 */
public class VisualizationStyleDataTypePanel extends JPanel implements StyleDataTypeTreeListener
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(VisualizationStyleDataTypePanel.class);

    /** The our default selected icon. */
    private static final ImageIcon ourDefaultSelectedIcon;

    /** The our type checked and selected icon. */
    private static final ImageIcon ourTypeCheckedAndSelectedIcon;

    /** The our type selected not checked icon. */
    private static final ImageIcon ourTypeSelectedNotCheckedIcon;

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Center panel. */
    private final JPanel myCenterPanel;

    /** The Current data type. */
    private DataTypeNodeUserObject myCurrentDataType;

    /** The Data type tree panel. */
    @SuppressWarnings("PMD.SingularField")
    private final VisualizationStyleDataTypeTreePanel myDataTypeTreePanel;

    /** The type of data this panel will hold. */
    private final VisualizationStyleGroup myGroupType;

    /** The Node key to style edit panel map. */
    private final Map<String, StyleEditPanel> myNodeKeyToStyleEditPanelMap;

    /** The Style manager controller. */
    private final StyleManagerController myStyleManagerController;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    static
    {
        ImageIcon temp = null;
        try
        {
            temp = new ImageIcon(
                    ImageIO.read(VisualizationStyleDataTypePanel.class.getResource("/images/VisStyleDefaultSelected.png")));
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to load image.", e);
        }
        ourDefaultSelectedIcon = temp;

        try
        {
            temp = new ImageIcon(
                    ImageIO.read(VisualizationStyleDataTypePanel.class.getResource("/images/VisStyleTypeSelected.png")));
        }
        catch (IOException e)
        {
            temp = null;
            LOGGER.error("Failed to load image.", e);
        }
        ourTypeCheckedAndSelectedIcon = temp;

        try
        {
            temp = new ImageIcon(
                    ImageIO.read(VisualizationStyleDataTypePanel.class.getResource("/images/VisStyleTypeLabelSelected.png")));
        }
        catch (IOException e)
        {
            temp = null;
            LOGGER.error("Failed to load image.", e);
        }
        ourTypeSelectedNotCheckedIcon = temp;
    }

    /**
     * Instantiates a new visualization style data type panel.
     *
     * @param tb the toolbox through which application state is accessed.
     * @param styleManagerController the style manager controller
     * @param groupType the type of data this panel is configured to contain.
     */
    public VisualizationStyleDataTypePanel(Toolbox tb, StyleManagerController styleManagerController, VisualizationStyleGroup groupType)
    {
        super(new BorderLayout());
        myCenterPanel = new JPanel(new BorderLayout());
        myGroupType = groupType;
        myToolbox = tb;
        myStyleManagerController = styleManagerController;
        myDataTypeTreePanel = new VisualizationStyleDataTypeTreePanel(myToolbox, myStyleManagerController, this, myGroupType);
        myDataTypeTreePanel.setMinimumSize(new Dimension(180, 10));
        myDataTypeTreePanel.setPreferredSize(new Dimension(180, 10));
        add(myDataTypeTreePanel, BorderLayout.WEST);
        add(myCenterPanel, BorderLayout.CENTER);
        myNodeKeyToStyleEditPanelMap = New.map();
        rebuildEditorPanel();
    }

    @Override
    public void dataTypeSelected(DataTypeNodeUserObject type)
    {
        synchronized (myNodeKeyToStyleEditPanelMap)
        {
            myCurrentDataType = type;
        }
        rebuildEditorPanel();
    }

    @Override
    public void forceRebuild()
    {
        rebuildEditorPanel();
    }

    @Override
    public void noDataTypeSelected()
    {
        synchronized (myNodeKeyToStyleEditPanelMap)
        {
            myCurrentDataType = null;
        }
        rebuildEditorPanel();
    }

    /**
     * Switch to data type.
     *
     * @param event the event
     * @return true, if successful
     */
    public boolean switchToDataType(ShowTypeVisualizationStyleEvent event)
    {
        return myDataTypeTreePanel.switchToDataType(event);
    }

    /**
     * Creates the image panel.
     *
     * @param image the image
     * @return the jX image panel
     */
    private JXImagePanel createImagePanel(ImageIcon image)
    {
        JXImagePanel ip1 = new JXImagePanel();
        ip1.setBorder(BorderFactory.createEtchedBorder());
        ip1.setImage(image.getImage());
        ip1.setMaximumSize(new Dimension(image.getImage().getWidth(null) + 2, image.getImage().getHeight(null) + 2));
        return ip1;
    }

    /**
     * Creates the label panel.
     *
     * @param text the text
     * @param fontStyle the font style
     * @param fontSizeAdder the font size adder
     * @return the j panel
     */
    private JPanel createLabelPanel(String text, int fontStyle, int fontSizeAdder)
    {
        JPanel p = new JPanel(new BorderLayout());
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(fontStyle, label.getFont().getSize() + fontSizeAdder));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        p.add(label, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(1000, 30));
        p.setMinimumSize(new Dimension(100, 30));
        p.setPreferredSize(new Dimension(100, 30));
        return p;
    }

    /**
     * Creates the text area.
     *
     * @param text the text
     * @return the j text area
     */
    private JPanel createTextArea(String text)
    {
        JPanel p = new JPanel(new BorderLayout());
        JTextArea jta = new JTextArea();
        jta.setEditable(false);
        jta.setBackground(myCenterPanel.getBackground());
        jta.setBorder(BorderFactory.createEmptyBorder());
        jta.setText(text);
//        jta.setMaximumSize(new Dimension(300, 50));
//        jta.setMinimumSize(new Dimension(100, 50));
//        jta.setPreferredSize(new Dimension(300, 50));

        p.add(jta, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(400, 50));
        p.setMinimumSize(new Dimension(100, 50));
        p.setPreferredSize(new Dimension(100, 50));
//        p.setBorder(BorderFactory.createLineBorder(Color.white));
        jta.setWrapStyleWord(true);
        jta.setLineWrap(true);
        return p;
    }

    /**
     * Rebuild editor panel.
     */
    private void rebuildEditorPanel()
    {
        EventQueueUtilities.runOnEDT(() -> rebuildEditorPanelInternal());
    }

    /**
     * Rebuild editor panel internal.
     */
    private void rebuildEditorPanelInternal()
    {
        synchronized (myNodeKeyToStyleEditPanelMap)
        {
            myCenterPanel.removeAll();
            if (myCurrentDataType == null)
            {
                JPanel noEditPanel = createLabelPanel("No Type Selected", Font.BOLD, 2);
                noEditPanel.setBorder(BorderFactory.createEtchedBorder());

                JPanel p = new JPanel();
                p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
                p.add(noEditPanel);
                p.add(Box.createVerticalStrut(20));
                p.add(createLabelPanel("How to use the type tree", Font.BOLD, 2));

                JPanel jta = createTextArea("If a data type is not checked it is using the default settings.  "
                        + "You can change the default settings by selecting the default name in the tree.");
                p.add(jta);
                p.add(createImagePanel(ourDefaultSelectedIcon));
                p.add(Box.createVerticalStrut(5));

                jta = createTextArea("To enable a custom visualization style for a type, select its check box.\n"
                        + "The type name will become selected automatically and you can then edit the custom style.");
                p.add(jta);
                p.add(createImagePanel(ourTypeCheckedAndSelectedIcon));
                p.add(Box.createVerticalStrut(5));

                jta = createTextArea("To edit a custom style without changing the check box, select the name\n"
                        + " and the type's custom editor will then appear.");
                p.add(jta);

                p.add(createImagePanel(ourTypeSelectedNotCheckedIcon));

                p.add(Box.createVerticalGlue());
                p.add(new JPanel());

                myCenterPanel.add(p, BorderLayout.CENTER);
            }
            else
            {
//                StyleEditPanel editPanel = myNodeKeyToStyleEditPanelMap.get(myCurrentDataType.getNodeKey());
//                if (editPanel == null)
//                {
//                    editPanel = new StyleEditPanel(myToolbox, myCurrentDataType);
//                    myNodeKeyToStyleEditPanelMap.put(myCurrentDataType.getNodeKey(), editPanel);
//                }

                StyleEditPanel editPanel = new StyleEditPanel(myToolbox, myStyleManagerController, myCurrentDataType,
                        myGroupType);
                myCenterPanel.add(editPanel, BorderLayout.CENTER);
            }

            revalidate();
            repaint();
        }
    }
}

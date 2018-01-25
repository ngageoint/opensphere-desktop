/**
 *
 */
package io.opensphere.core.common.collapsablepanel;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 *
 *
 * BIT Systems, Inc <br/>
 * Jan 18, 2012
 * </dl>
 */

public class CollapsiblePanel extends JPanel implements ActionListener
{
    /**
    *
    */
    private static final long serialVersionUID = -536039897699807671L;

    public static final int DEFAULT_ICON_SIZE = 11;

    public static final int DEFAULT_INDENT = 0;

    public static final int DEFAULT_BUTTON_SPACING = 4;

    public static final int MINIMUM_BUTTON_SIZE = 9;

    public static final Border DEFAULT_HEADER_BORDER = BorderFactory
            .createLineBorder(UIManager.getColor("Panel.background").darker().darker(), 1);

    public static final Border DEF_COMPONENT_BORDER = BorderFactory
            .createLineBorder(UIManager.getColor("Panel.background").darker().darker(), 1);

    public static final String DEF_TITLE = "Collapsable Panel";

    public static final int BUTTON_ALIGNMENT_LEFT = 1;

    public static final int BUTTON_ALIGNMENT_RIGHT = 2;

    private static final Insets DEFAULT_INSETS = new Insets(0, 0, 0, 0);

    private GridBagConstraints componentPanelGbc;

    private GridBagConstraints headerPanelGbc;

    private String title;

    private JComponent myComponent;

    private boolean expanded;

    private boolean showHeaderBorder;

    private boolean showComponentBorder;

    private int componentIndent = DEFAULT_INDENT;

    private int buttonAlignment;

    private ImageIcon expandedIcon;

    private ImageIcon collapsedIcon;

    private JButton button;

    private int buttonSize = DEFAULT_ICON_SIZE;

    private int buttonSpacing = DEFAULT_BUTTON_SPACING;

    private JPanel headerPanel;

    private JPanel componentPanel;

    private JLabel label;

    private Border headerBorder;

    private Border componentBorder;

    private boolean usingDefaultExpandIcon;

    private boolean usingDefaultCollapseIcon;

    public CollapsiblePanel()
    {
        this(null, null);
    }

    /**
    *
    *
    */
    public CollapsiblePanel(String pLabel, JComponent pComponent)
    {
        this(pLabel, pComponent, DEFAULT_INDENT, true, BUTTON_ALIGNMENT_LEFT);
    }

    public CollapsiblePanel(String pLabel, JComponent pComponent, int pComponentIndent)
    {
        this(pLabel, pComponent, pComponentIndent, true, BUTTON_ALIGNMENT_LEFT);
    }

    public CollapsiblePanel(String pLabel, JComponent pComponent, boolean pExpanded)
    {
        this(pLabel, pComponent, DEFAULT_INDENT, pExpanded, BUTTON_ALIGNMENT_LEFT);
    }

    public CollapsiblePanel(String pLabel, JComponent pComponent, int pComponentIndent, boolean isExpanded, int pButtonAlignment)
    {
        super(new GridBagLayout());
        title = pLabel == null ? DEF_TITLE : pLabel;
        myComponent = pComponent == null ? new JLabel("My Component") : pComponent;
        componentIndent = pComponentIndent;
        expanded = isExpanded;
        buttonAlignment = pButtonAlignment;

        init();
    }

    /**
    *
    *
    */
    private void init()
    {
        headerBorder = BorderFactory.createLineBorder(getBackground().darker().darker(), 1);
        componentBorder = BorderFactory.createLineBorder(getBackground().darker().darker(), 1);
//       setBorder(BorderFactory.createLineBorder(Color.green));
        setBorder(BorderFactory.createEmptyBorder());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 0);

        add(getHeaderPanel(), gbc);
        gbc.gridy++;
        add(getComponentPanel(), gbc);

    }

    /**
    *
    *
    */
    private void setButtonIcon()
    {
        if (expanded)
        {
            getButton().setIcon(getExpandedIcon());
        }
        else
        {
            getButton().setIcon(getCollapsedIcon());
        }

    }

    /**
     *
     *
     * @return
     */
    private JButton getButton()
    {
        if (button == null)
        {
            button = new JButton();
            button.addActionListener(this);
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setMargin(new Insets(0, 0, 0, 0));
            button.setOpaque(false);
            button.setBackground(new Color(128, 128, 128, 0));
            button.setFocusPainted(false);
        }

        return button;
    }

    private JPanel getHeaderPanel()
    {
        if (headerPanel == null)
        {
            headerPanel = new JPanel(new GridBagLayout());
            headerPanel.setBorder(BorderFactory.createEmptyBorder());

        }

        return headerPanel;
    }

    private void updateHeaderPanel()
    {
        int buttonPos;
        int spacerPos;
        int labelPos;

        if (buttonAlignment == BUTTON_ALIGNMENT_LEFT)
        {
            buttonPos = 0;
            labelPos = 1;
            spacerPos = 2;
        }
        else
        {
            buttonPos = 2;
            spacerPos = 1;
            labelPos = 0;
        }

        getHeaderPanel().removeAll();
        getHeaderPanelGbc().gridy = 0;
        getHeaderPanelGbc().insets = DEFAULT_INSETS;

        getHeaderPanelGbc().gridx = buttonPos;
        getHeaderPanel().add(getButton(), getHeaderPanelGbc());

        getHeaderPanelGbc().gridx = labelPos;
        getHeaderPanelGbc().insets = new Insets(0, getButtonSpacing(), 0, 0);
        getHeaderPanel().add(getLabel(), getHeaderPanelGbc());

        getHeaderPanelGbc().gridx = spacerPos;
        HorizontalSpacerForGridbag hs = new HorizontalSpacerForGridbag(getHeaderPanelGbc().gridx, 0);
        headerPanel.add(hs, hs.getGBConst());

        getHeaderPanel().revalidate();
    }

    private GridBagConstraints getHeaderPanelGbc()
    {
        if (headerPanelGbc == null)
        {
            headerPanelGbc = new GridBagConstraints();
            headerPanelGbc.gridx = 0;
            headerPanelGbc.gridy = 0;
            headerPanelGbc.weightx = 0.0;
            headerPanelGbc.weighty = 0.0;
            headerPanelGbc.fill = GridBagConstraints.NONE;
            headerPanelGbc.anchor = GridBagConstraints.WEST;
            headerPanelGbc.insets = new Insets(0, 0, 0, 0);
        }
        return headerPanelGbc;
    }

    /**
     *
     *
     * @return
     */
    private JLabel getLabel()
    {
        if (label == null)
        {
            label = new JLabel(getTitle());
        }

        return label;
    }

    private JPanel getComponentPanel()
    {
        if (componentPanel == null)
        {

            componentPanel = new JPanel(new GridBagLayout());
            componentPanel.setBorder(BorderFactory.createEmptyBorder());
        }

        return componentPanel;
    }

    /**
    *
    *
    */
    private GridBagConstraints getCompPanelGbc()
    {
        if (componentPanelGbc == null)
        {
            componentPanelGbc = new GridBagConstraints();
            componentPanelGbc.gridx = 0;
            componentPanelGbc.gridy = 0;
            componentPanelGbc.weightx = 1.0;
            componentPanelGbc.weighty = 0.0;
            componentPanelGbc.fill = GridBagConstraints.HORIZONTAL;
            componentPanelGbc.anchor = GridBagConstraints.WEST;
            componentPanelGbc.insets = new Insets(0, componentIndent, 0, 0);
        }
        return componentPanelGbc;
    }

    private void updateComponentPanel()
    {
        getComponentPanel().removeAll();
        getCompPanelGbc().gridx = 0;
        getCompPanelGbc().gridy = 0;

        getComponentPanel().add(getMyComponent(), getCompPanelGbc());
        getComponentPanel().revalidate();
    }

    /**
     *
     *
     * @return
     */
    private JComponent getMyComponent()
    {
        if (myComponent == null)
        {
            myComponent = new JLabel("Place Holder Component");
        }

        return myComponent;
    }

    public JComponent getComponent()
    {
        return getMyComponent();
    }

    public void setComponent(JComponent pComponent)
    {
        myComponent = pComponent;
        updateComponentPanel();
    }

    /**
     *
     *
     * @return
     */
    public String getTitle()
    {
        if (title == null)
        {
            title = "Collapsable";
        }

        return title;
    }

    public void setTitle(String pTitle)
    {
        title = pTitle;
        getLabel().setText(title);

    }

    public ImageIcon getExpandedIcon()
    {
        if (expandedIcon == null)
        {
            expandedIcon = getDefaultExpandedIcon(buttonSize);
            usingDefaultExpandIcon = true;
        }

        return expandedIcon;
    }

    public ImageIcon getDefaultExpandedIcon(int pSize)
    {
        return Icons.createBoxedMinusIcon(pSize);
    }

    public ImageIcon getDefaultCollapsedIcon(int pSize)
    {
        return Icons.createBoxedPlusIcon(pSize);
    }

    public ImageIcon getCollapsedIcon()
    {
        if (collapsedIcon == null)
        {
            collapsedIcon = getDefaultCollapsedIcon(buttonSize);
            usingDefaultCollapseIcon = true;
        }

        return collapsedIcon;
    }

    public void setExpandedIcon(ImageIcon pIcon)
    {
        expandedIcon = pIcon;
        setButtonIcon();
    }

    public void setCollapsedIcon(ImageIcon pIcon)
    {
        collapsedIcon = pIcon;
        setButtonIcon();
    }

    public void setShowHeaderBorder(boolean b)
    {
        showHeaderBorder = b;
        if (b)
        {
            getHeaderPanel().setBorder(headerBorder);
        }
        else
        {
            getHeaderPanel().setBorder(BorderFactory.createEmptyBorder());
        }
    }

    public boolean getShowHeaderBorder()
    {
        return showHeaderBorder;
    }

    public boolean getShowComponentBorder()
    {
        return showComponentBorder;
    }

    public void setShowComponentBorder(boolean b)
    {
        showComponentBorder = b;
        if (b)
        {
            getComponentPanel().setBorder(componentBorder);
        }
        else
        {
            getComponentPanel().setBorder(BorderFactory.createEmptyBorder());
        }
    }

    public void setExpanded(boolean b)
    {
        expanded = b;
        setButtonIcon();
        getComponentPanel().setVisible(b);
    }

    public void setButtonAlignment(int pAlignment)
    {
        if (pAlignment != BUTTON_ALIGNMENT_LEFT && pAlignment != BUTTON_ALIGNMENT_RIGHT)
        {
            throw new IllegalArgumentException("invalid button alignment value: " + pAlignment);
        }
        buttonAlignment = pAlignment;
        updateHeaderPanel();
    }

    public void setButtonSize(int pSize)
    {
        buttonSize = pSize < MINIMUM_BUTTON_SIZE ? MINIMUM_BUTTON_SIZE : pSize;

        if (usingDefaultCollapseIcon || usingDefaultExpandIcon)
        {
            if (usingDefaultCollapseIcon)
            {
                collapsedIcon = getDefaultCollapsedIcon(buttonSize);
            }
            if (usingDefaultExpandIcon)
            {
                expandedIcon = getDefaultExpandedIcon(buttonSize);
            }
            setButtonIcon();
        }
        getButton().setSize(buttonSize, buttonSize);
        getButton().setPreferredSize(getButton().getSize());
        updateHeaderPanel();
    }

    public int getButtonSize()
    {
        return buttonSize;
    }

    public void setComponentIndent(int pIndent)
    {
        componentIndent = pIndent;
        getCompPanelGbc().insets.left = componentIndent;

        updateComponentPanel();
    }

    public void setHeaderBorder(Border pBorder)
    {
        headerBorder = pBorder == null ? DEFAULT_HEADER_BORDER : pBorder;
        if (getShowHeaderBorder())
        {
            getHeaderPanel().setBorder(headerBorder);
        }
    }

    /**
     *
     *
     * @return the buttonSpacing
     */
    public int getButtonSpacing()
    {
        return buttonSpacing;
    }

    /**
     *
     *
     * @param buttonSpacing the buttonSpacing to set
     */
    public void setButtonSpacing(int pButtonSpacing)
    {
        buttonSpacing = pButtonSpacing;
        updateHeaderPanel();
    }

    public Border getHeaderBorder()
    {
        return headerBorder;
    }

    public void setComponentBorder(Border pBorder)
    {
        componentBorder = pBorder == null ? DEFAULT_HEADER_BORDER : pBorder;
        if (getShowComponentBorder())
        {
            getComponentPanel().setBorder(componentBorder);
        }
    }

    public Border getComponentBorder()
    {
        return componentBorder;
    }

    /* (non-Javadoc)
     *
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent) */
    @Override
    public void actionPerformed(ActionEvent pE)
    {
        if (pE.getSource() == getButton())
        {
            setExpanded(!expanded);
        }

    }

    /* (non-Javadoc)
     *
     * @see javax.swing.JComponent#addNotify() */
    @Override
    public void addNotify()
    {
        super.addNotify();
        updateHeaderPanel();
        updateComponentPanel();
        setExpanded(expanded);
    }

}

/**
 *
 */

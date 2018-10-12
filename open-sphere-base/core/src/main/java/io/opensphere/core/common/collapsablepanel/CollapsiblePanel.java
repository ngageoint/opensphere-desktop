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
    /** Version ID. */
    private static final long serialVersionUID = -536039897699807671L;

    /** Default icon size. */
    public static final int DEFAULT_ICON_SIZE = 11;

    /** Default left indent. */
    public static final int DEFAULT_INDENT = 0;

    /** Default button spacing. */
    public static final int DEFAULT_BUTTON_SPACING = 4;

    /** Minimum button size: 9px. */
    public static final int MINIMUM_BUTTON_SIZE = 9;

    /** Default header border. */
    public static final Border DEFAULT_HEADER_BORDER = BorderFactory
            .createLineBorder(UIManager.getColor("Panel.background").darker().darker(), 1);

    /** Default component border. */
    public static final Border DEF_COMPONENT_BORDER = BorderFactory
            .createLineBorder(UIManager.getColor("Panel.background").darker().darker(), 1);

    /** Default title. */
    public static final String DEF_TITLE = "Collapsable Panel";

    /** Left alignment. */
    public static final int BUTTON_ALIGNMENT_LEFT = 1;

    /** Right alignment. */
    public static final int BUTTON_ALIGNMENT_RIGHT = 2;

    /** Default insets. */
    private static final Insets DEFAULT_INSETS = new Insets(0, 0, 0, 0);

    /** Component constraints. */
    private GridBagConstraints componentPanelGbc;

    /** Header constraints. */
    private GridBagConstraints headerPanelGbc;

    /** Panel title. */
    private String title;

    /** Inner component. */
    private JComponent myComponent;

    /** Expanded or Collapsed. */
    private boolean expanded;

    /** Header border visible. */
    private boolean showHeaderBorder;

    /** Component border visible. */
    private boolean showComponentBorder;

    /** Component left indent. */
    private int componentIndent = DEFAULT_INDENT;

    /** Button alignment. */
    private int buttonAlignment;

    /** Expanded icon. */
    private ImageIcon expandedIcon;

    /** Collapsed icon. */
    private ImageIcon collapsedIcon;

    /** Expand/collapse button. */
    private JButton button;

    /** Button size. */
    private int buttonSize = DEFAULT_ICON_SIZE;

    /** Button spacing. */
    private int buttonSpacing = DEFAULT_BUTTON_SPACING;

    /** Header panel. */
    private JPanel headerPanel;

    /** Component panel. */
    private JPanel componentPanel;

    /** Title label. */
    private JLabel label;

    /** Header border. */
    private Border headerBorder;

    /** Component border. */
    private Border componentBorder;

    /** Using default icon or not. */
    private boolean usingDefaultExpandIcon;

    /** Using default icon or not. */
    private boolean usingDefaultCollapseIcon;

    /**
     * Default constructor.
     * <p>
     * Uses default values for everything.
     */
    public CollapsiblePanel()
    {
        this(null, null);
    }

    /**
     * Constructor.
     * <p>
     * Uses default values for indentation, expansion status, & button
     * alignment.
     *
     * @param pLabel the panel label
     * @param pComponent the component
     */
    public CollapsiblePanel(String pLabel, JComponent pComponent)
    {
        this(pLabel, pComponent, DEFAULT_INDENT, true, BUTTON_ALIGNMENT_LEFT);
    }

    /**
     * Constructor.
     * <p>
     * Uses default values for expansion status & button alignment.
     *
     * @param pLabel the panel label
     * @param pComponent the component
     * @param pComponentIndent the component indentation
     */
    public CollapsiblePanel(String pLabel, JComponent pComponent, int pComponentIndent)
    {
        this(pLabel, pComponent, pComponentIndent, true, BUTTON_ALIGNMENT_LEFT);
    }

    /**
     * Constructor.
     * <p>
     * Uses default values for indentation & button alignment.
     *
     * @param pLabel the panel label
     * @param pComponent the component
     * @param pExpanded whether or not the panel is expanded
     */
    public CollapsiblePanel(String pLabel, JComponent pComponent, boolean pExpanded)
    {
        this(pLabel, pComponent, DEFAULT_INDENT, pExpanded, BUTTON_ALIGNMENT_LEFT);
    }

    /**
     * Constructor.
     *
     * @param pLabel
     * @param pComponent
     * @param pComponentIndent
     * @param isExpanded
     * @param pButtonAlignment
     */
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
     * Sets button icon to expanded or collapsed, depending on state.
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
     * Creates or returns button.
     *
     * @return the button
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

    /**
     * Creates or returns header panel.
     *
     * @return the panel
     */
    private JPanel getHeaderPanel()
    {
        if (headerPanel == null)
        {
            headerPanel = new JPanel(new GridBagLayout());
            headerPanel.setBorder(BorderFactory.createEmptyBorder());

        }

        return headerPanel;
    }

    /**
     * Updates the header panel.
     */
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

    /**
     * Gets or creates the header GridBagConstraints.
     *
     * @return headerPanelGbc
     */
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
     * Gets or creates the label/
     *
     * @return label
     */
    private JLabel getLabel()
    {
        if (label == null)
        {
            label = new JLabel(getTitle());
        }

        return label;
    }

    /**
     * Gets or creates the component panel.
     *
     * @return the panel
     */
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
     * Gets or creates the component GridBagConstraints.
     *
     * @return componentPanelGbc
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

    /**
     * Updates the component panel.
     */
    private void updateComponentPanel()
    {
        getComponentPanel().removeAll();
        getCompPanelGbc().gridx = 0;
        getCompPanelGbc().gridy = 0;

        getComponentPanel().add(getMyComponent(), getCompPanelGbc());
        getComponentPanel().revalidate();
    }

    /**
     * Gets or creates my component.
     *
     * @return myComponent
     */
    private JComponent getMyComponent()
    {
        if (myComponent == null)
        {
            myComponent = new JLabel("Place Holder Component");
        }

        return myComponent;
    }

    /**
     * @return myComponent
     */
    public JComponent getComponent()
    {
        return getMyComponent();
    }

    /**
     * Sets myComponent.
     *
     * @param pComponent the component
     */
    public void setComponent(JComponent pComponent)
    {
        myComponent = pComponent;
        updateComponentPanel();
    }

    /**
     * Gets or creates the title string.
     *
     * @return the title
     */
    public String getTitle()
    {
        if (title == null)
        {
            title = "Collapsable";
        }

        return title;
    }

    /**
     * Sets the title.
     *
     * @param pTitle the new title
     */
    public void setTitle(String pTitle)
    {
        title = pTitle;
        getLabel().setText(title);
    }

    /**
     * Gets or creates the expanded icon.
     *
     * @return the icon
     */
    public ImageIcon getExpandedIcon()
    {
        if (expandedIcon == null)
        {
            expandedIcon = getDefaultExpandedIcon(buttonSize);
            usingDefaultExpandIcon = true;
        }

        return expandedIcon;
    }

    /**
     * Gets the default expanded icon.
     *
     * @param pSize the size of the icon
     * @return the icon
     */
    public ImageIcon getDefaultExpandedIcon(int pSize)
    {
        return Icons.createBoxedMinusIcon(pSize);
    }

    /**
     * Gets the default collapsed icon.
     *
     * @param pSize the size of the icon
     * @return the icon
     */
    public ImageIcon getDefaultCollapsedIcon(int pSize)
    {
        return Icons.createBoxedPlusIcon(pSize);
    }

    /**
     * Gets or creates the collapsed icon.
     *
     * @return the icon
     */
    public ImageIcon getCollapsedIcon()
    {
        if (collapsedIcon == null)
        {
            collapsedIcon = getDefaultCollapsedIcon(buttonSize);
            usingDefaultCollapseIcon = true;
        }

        return collapsedIcon;
    }

    /**
     * Sets the expanded icon.
     *
     * @param pIcon the new icon
     */
    public void setExpandedIcon(ImageIcon pIcon)
    {
        expandedIcon = pIcon;
        setButtonIcon();
    }

    /**
     * Sets the collapsed icon.
     *
     * @param pIcon the new icon
     */
    public void setCollapsedIcon(ImageIcon pIcon)
    {
        collapsedIcon = pIcon;
        setButtonIcon();
    }

    /**
     * Show or hide the header border.
     *
     * @param b true to show the border, false to hide it
     */
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

    /**
     * isShowHeaderBorder
     *
     * @return showHeaderBorder
     */
    public boolean getShowHeaderBorder()
    {
        return showHeaderBorder;
    }

    /**
     * isShowComponentBorder
     *
     * @return showComponentBorder
     */
    public boolean getShowComponentBorder()
    {
        return showComponentBorder;
    }

    /**
     * Show or hide the component border.
     *
     * @param b true to show the border, false to hide it
     */
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

    /**
     * Expand or collapse the panel.
     *
     * @param b true to expand, false to collapse
     */
    public void setExpanded(boolean b)
    {
        expanded = b;
        setButtonIcon();
        getComponentPanel().setVisible(b);
    }

    /**
     * Sets the button alignment.
     *
     * @param pAlignment BUTTON_ALIGNMENT_LEFT or BUTTON_ALIGNMENT_RIGHT
     */
    public void setButtonAlignment(int pAlignment)
    {
        if (pAlignment != BUTTON_ALIGNMENT_LEFT && pAlignment != BUTTON_ALIGNMENT_RIGHT)
        {
            throw new IllegalArgumentException("invalid button alignment value: " + pAlignment);
        }
        buttonAlignment = pAlignment;
        updateHeaderPanel();
    }

    /**
     * Sets the button size, floored to MINIMUM_BUTTON_SIZE.
     *
     * @param pSize the size
     */
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

    /**
     * Gets the button size.
     *
     * @return the size
     */
    public int getButtonSize()
    {
        return buttonSize;
    }

    /**
     * Sets the component indent.
     *
     * @param pIndent the indent
     */
    public void setComponentIndent(int pIndent)
    {
        componentIndent = pIndent;
        getCompPanelGbc().insets.left = componentIndent;

        updateComponentPanel();
    }

    /**
     * Sets the header border.
     *
     * @param pBorder the border
     */
    public void setHeaderBorder(Border pBorder)
    {
        headerBorder = pBorder == null ? DEFAULT_HEADER_BORDER : pBorder;
        if (getShowHeaderBorder())
        {
            getHeaderPanel().setBorder(headerBorder);
        }
    }

    /**
     * Gets the button spacing.
     *
     * @return buttonSpacing
     */
    public int getButtonSpacing()
    {
        return buttonSpacing;
    }

    /**
     * Sets the button spacing.
     *
     * @param pButtonSpacing the buttonSpacing to set
     */
    public void setButtonSpacing(int pButtonSpacing)
    {
        buttonSpacing = pButtonSpacing;
        updateHeaderPanel();
    }

    /**
     * Gets the header border.
     *
     * @return the border
     */
    public Border getHeaderBorder()
    {
        return headerBorder;
    }

    /**
     * Sets the component border.
     *
     * @param pBorder the border
     */
    public void setComponentBorder(Border pBorder)
    {
        componentBorder = pBorder == null ? DEFAULT_HEADER_BORDER : pBorder;
        if (getShowComponentBorder())
        {
            getComponentPanel().setBorder(componentBorder);
        }
    }

    /**
     * Gets the component border.
     *
     * @return the border
     */
    public Border getComponentBorder()
    {
        return componentBorder;
    }

    @Override
    public void actionPerformed(ActionEvent pE)
    {
        if (pE.getSource() == getButton())
        {
            setExpanded(!expanded);
        }

    }

    @Override
    public void addNotify()
    {
        super.addNotify();
        updateHeaderPanel();
        updateComponentPanel();
        setExpanded(expanded);
    }

}

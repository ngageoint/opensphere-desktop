package io.opensphere.filterbuilder2.editor.advanced;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import io.opensphere.core.datafilter.DataFilterOperators.Logical;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.core.util.swing.input.controller.ControllerFactory;
import io.opensphere.core.util.swing.input.model.ViewModel;
import io.opensphere.filterbuilder2.common.Constants;
import io.opensphere.filterbuilder2.editor.model.CriterionModel;
import io.opensphere.filterbuilder2.editor.model.GroupModel;

/**
 * Filter tree cell panel.
 */
public class FilterTreeCellPanel extends GridBagPanel
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The edit button. */
    private final IconButton myButtonEdit;

    /** The remove criterion button. */
    private final IconButton myButtonRemoveCriterion;

    /** The remove group button. */
    private final IconButton myButtonRemoveGroup;

    /** Icon used to show non-leaf nodes that aren't expanded. */
    private final transient Icon myIconClosed;

    /** Icon used to show leaf nodes. */
    private final transient Icon myIconLeaf;

    /** Icon used to show non-leaf nodes that are expanded. */
    private final transient Icon myIconOpen;

    /** The field label. */
    private final JLabel myLabelField = new JLabel();

    /** The icon label. */
    private final JLabel myLabelIcon = new JLabel();

    /** The max value label. */
    private final JLabel myLabelMaxValue = new JLabel();

    /** The operator label. */
    private final JLabel myLabelOperator = new JLabel();

    /** The value label. */
    private final JLabel myLabelValue = new JLabel();

    /** The toolbox. */
    private final transient PreferencesRegistry myPreferencesRegistry;

    /** The user object. */
    private Object myUserObject;

    /** The model to view map. */
    private final transient Map<ViewModel<?>, JComboBox<Logical>> myModelToViewMap;

    /**
     * Constructor.
     *
     * @param prefs the system registry for user preferences
     */
    public FilterTreeCellPanel(PreferencesRegistry prefs)
    {
        super();
        setBackground(Constants.TRANSPARENT_COLOR);
        setOpaque(false);
        anchorWest().fillNone();
        myPreferencesRegistry = prefs;
        myLabelOperator.setFont(myLabelOperator.getFont().deriveFont(Font.BOLD));
        myButtonEdit = buildEditButton();
        myButtonRemoveGroup = buildRemoveGroupButton();
        myButtonRemoveCriterion = buildRemoveCriterionButton();
        myIconLeaf = UIManager.getIcon("Tree.leafIcon");
        myIconClosed = UIManager.getIcon("Tree.closedIcon");
        myIconOpen = UIManager.getIcon("Tree.openIcon");
        myModelToViewMap = new HashMap<>();
    }

    /**
     * Builds the panel.
     *
     * @param tree the tree
     * @param value the value
     * @param expanded whether the cell is expanded
     * @param row the row
     * @param isEditing whether the cell is being edited
     * @return the user object
     */
    public Object buildPanel(JTree tree, Object value, boolean expanded, int row, boolean isEditing)
    {
        myUserObject = FilterTreeUtilities.getUserObject(value);

        boolean isHover = false;
        if (tree instanceof FilterTree)
        {
            FilterTree filterTree = (FilterTree)tree;
            isHover = filterTree.getMouseOverRow() == row;
        }

        removeAll();
        setInsets(0, 0, 0, Constants.DOUBLE_INSET);

        setIcon(expanded, myUserObject instanceof CriterionModel);

        if (myUserObject instanceof CriterionModel)
        {
            CriterionModel criterion = (CriterionModel)myUserObject;

            myLabelField.setText(StringUtilities.concat("'", criterion.getField().get(), "'"));
            add(myLabelField);
            myLabelOperator.setText(criterion.getOperator().get().toString());
            add(myLabelOperator);
            FilterTreeUtilities.setText(myLabelValue, criterion.getCriterionValue());
            add(myLabelValue);
            if (criterion.getCriterionMaxValue().isVisible())
            {
                FilterTreeUtilities.setText(myLabelMaxValue, criterion.getCriterionMaxValue());
                add(myLabelMaxValue);
            }

            fillHorizontalSpace().fillNone();
            if (isHover)
            {
                setInsets(0, 0, 0, Constants.INSET);
                add(myButtonEdit);
                add(myButtonRemoveCriterion);
            }
        }
        else if (myUserObject instanceof GroupModel)
        {
            GroupModel group = (GroupModel)myUserObject;

            JComboBox<Logical> view = myModelToViewMap.get(group.getOperator());
            if (view == null)
            {
                view = ControllerFactory.createComponent(group.getOperator(), JComboBox.class);
                @SuppressWarnings("rawtypes")
                final ListCellRenderer defaultRenderer = view.getRenderer();
                view.setRenderer(new ListCellRenderer<Logical>()
                {
                    @SuppressWarnings("unchecked")
                    @Override
                    public Component getListCellRendererComponent(JList<? extends Logical> list, Logical val, int index,
                            boolean sel, boolean cellHasFocus)
                    {
                        return defaultRenderer.getListCellRendererComponent(list, val.getLogicText(), index, sel, cellHasFocus);
                    }
                });
                myModelToViewMap.put(group.getOperator(), view);
            }
            add(view);

            fillHorizontalSpace().fillNone();
            if (isHover && group.getParent() != null)
            {
                setInsets(0, 0, 0, Constants.INSET);
                add(myButtonRemoveGroup);
            }
        }

        if (!isEditing)
        {
            JTree.DropLocation dropLocation = tree.getDropLocation();
            boolean isDrop = dropLocation != null && dropLocation.getChildIndex() == -1
                    && tree.getRowForPath(dropLocation.getPath()) == row;
            setBackground(isDrop ? Constants.HOVER_COLOR : Constants.TRANSPARENT_COLOR);
            setOpaque(isDrop);
        }

        return myUserObject;
    }

    @Override
    public Dimension getPreferredSize()
    {
        Dimension preferredSize = super.getPreferredSize();
        if (preferredSize.height < 22)
        {
            preferredSize.height = 22;
        }
        return preferredSize;
    }

    /**
     * Builds the edit button.
     *
     * @return the edit button
     */
    private IconButton buildEditButton()
    {
        IconButton button = new IconButton(IconType.EDIT);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setToolTipText("Edit the " + Constants.EXPRESSION.toLowerCase());
        button.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CriterionModel criterion = (CriterionModel)myUserObject;
                CriterionEditorDialog.showDialog(FilterTreeCellPanel.this.getParent(), criterion);
            }
        });
        return button;
    }

    /**
     * Builds the remove criterion button.
     *
     * @return the remove criterion button
     */
    private IconButton buildRemoveCriterionButton()
    {
        IconButton button = new IconButton(IconType.CLOSE, Color.RED);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setToolTipText("Remove the " + Constants.EXPRESSION.toLowerCase());
        button.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CriterionModel criterion = (CriterionModel)myUserObject;
                criterion.getParent().removeCriterion(criterion);
            }
        });
        return button;
    }

    /**
     * Builds the remove group button.
     *
     * @return the remove group button
     */
    private IconButton buildRemoveGroupButton()
    {
        IconButton button = new IconButton(IconType.CLOSE, Color.RED);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setToolTipText("Remove the group");
        button.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                GroupModel group = (GroupModel)myUserObject;
                FilterTreeUtilities.removeItems(Collections.singletonList(group), myPreferencesRegistry,
                        FilterTreeCellPanel.this.getParent());
            }
        });
        return button;
    }

    /**
     * Sets the icon.
     *
     * @param expanded whether the cell is expanded
     * @param leaf whether the cell is a leaf
     */
    private void setIcon(boolean expanded, boolean leaf)
    {
        Icon icon;
        if (leaf)
        {
            icon = myIconLeaf;
        }
        else if (expanded)
        {
            icon = myIconOpen;
        }
        else
        {
            icon = myIconClosed;
        }
        myLabelIcon.setIcon(icon);
        add(myLabelIcon);
    }
}

package io.opensphere.filterbuilder2.editor.advanced;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.filterbuilder2.common.Constants;
import io.opensphere.filterbuilder2.editor.model.FilterModel;

/**
 * The advanced filter editor panel.
 */
public class AdvancedEditorPanel extends JPanel
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The filter tree. */
    private final FilterTree myFilterTree;

    /** The model. */
    private final FilterModel myModel;

    /**
     * Constructor.
     *
     * @param prefs the system registry for user preferences
     * @param model the model
     */
    public AdvancedEditorPanel(PreferencesRegistry prefs, FilterModel model)
    {
        super(new BorderLayout());
        setName("Advanced");
        myModel = model;
        myFilterTree = new FilterTree(prefs, myModel);
        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
    }

    /**
     * Builds the center panel.
     *
     * @return the center panel
     */
    private JScrollPane buildCenterPanel()
    {
        JScrollPane scrollPane = new JScrollPane(myFilterTree);
        // Set this to prevent sizing issues with the renderer when the
        // scrollbar comes and goes
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(Constants.EDITOR_SCROLLPANE_SIZE);
        return scrollPane;
    }

    /**
     * Builds the top panel.
     *
     * @return the top panel
     */
    private JPanel buildTopPanel()
    {
        IconButton addCriterionButton = new IconButton("Add " + Constants.EXPRESSION, IconType.PLUS);
        addCriterionButton.setToolTipText("Add a filter " + Constants.EXPRESSION.toLowerCase());
        addCriterionButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                myFilterTree.addNewCriterion();
            }
        });

        IconButton addGroupButton = new IconButton("Add Group", IconType.PLUS);
        addGroupButton.setToolTipText("Add a filter group");
        addGroupButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                myFilterTree.addNewGroup();
            }
        });

        final IconButton removeButton = new IconButton(IconType.CLOSE, Color.RED);
        removeButton.setEnabled(false);
        removeButton.setToolTipText("Remove the selected items");
        removeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                myFilterTree.removeSelected();
            }
        });

        myFilterTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener()
        {
            @Override
            public void valueChanged(TreeSelectionEvent e)
            {
                int[] selectionRows = myFilterTree.getSelectionModel().getSelectionRows();
                removeButton.setEnabled(selectionRows != null && selectionRows.length > 0 && selectionRows[0] != 0);
            }
        });

        GridBagPanel panel = new GridBagPanel();
        panel.anchorWest();
        panel.setInsets(3, 0, Constants.DOUBLE_INSET + 3, Constants.INSET);
        panel.add(addCriterionButton);
        panel.add(addGroupButton);
        panel.add(removeButton);
        panel.fillHorizontalSpace();
        return panel;
    }
}

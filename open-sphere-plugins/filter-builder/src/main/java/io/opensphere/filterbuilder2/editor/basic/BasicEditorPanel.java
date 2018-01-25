package io.opensphere.filterbuilder2.editor.basic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import io.opensphere.core.datafilter.DataFilterOperators.Logical;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.core.util.swing.RadioButtonPanel;
import io.opensphere.core.util.swing.input.controller.ControllerFactory;
import io.opensphere.core.util.swing.input.controller.ViewSettings;
import io.opensphere.core.util.swing.input.model.ChoiceModel;
import io.opensphere.filterbuilder.filter.v1.Group;
import io.opensphere.filterbuilder2.common.Constants;
import io.opensphere.filterbuilder2.editor.model.FilterModel;

/**
 * The basic filter editor panel.
 */
public class BasicEditorPanel extends JPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The model. */
    private final FilterModel myModel;

    /** The criteria panel. */
    private CriteriaEditorPanel myCriteriaPanel;

    /** Whether the current filter is accepted. */
    private boolean myIsFilterAccepted;

    /** Whether to ignore events. */
    private boolean myIgnoreEvents;

    /**
     * Returns whether this panel accepts the given filter model.
     *
     * @param model the filter model
     * @return whether the panel accepts the model
     */
    public static boolean acceptsFilter(FilterModel model)
    {
        return model.getGroup().getGroups().isEmpty() && model.getGroup().getOperator().get() != Logical.NOT;
    }

    /**
     * Constructor.
     *
     * @param model the model
     */
    public BasicEditorPanel(FilterModel model)
    {
        super(new BorderLayout());
        setName("Basic");
        myModel = model;
        myIsFilterAccepted = acceptsFilter(myModel);
        buildPanel();

        myModel.getGroup().addListener(new io.opensphere.core.util.ChangeListener<Group>()
        {
            @Override
            public void changed(ObservableValue<? extends Group> observable, Group oldValue, Group newValue)
            {
                if (myIgnoreEvents)
                {
                    return;
                }

                // Rebuild this panel if the filter is not accepted
                boolean accepts = acceptsFilter(myModel);
                if (myIsFilterAccepted != accepts)
                {
                    myIsFilterAccepted = accepts;
                    removeAll();
                    buildPanel();
                }

                // Rebuild the criteria panel if necessary
                if (myIsFilterAccepted)
                {
                    // A criterion was added/removed, or the And/Or selection
                    // was changed
                    if ("criteriaListChange".equals(myModel.getGroup().getAction())
                            || myModel.getGroup().getChangedSource() instanceof ChoiceModel)
                    {
                        myCriteriaPanel.rebuildPanel();
                    }
                    // Something else changed
                    else
                    {
                        validate();
                        repaint();
                    }
                }
            }
        });
    }

    /**
     * Builds the center panel.
     *
     * @return the center panel
     */
    private JScrollPane buildCenterPanel()
    {
        myCriteriaPanel = new CriteriaEditorPanel(myModel);
        JScrollPane scrollPane = new JScrollPane(myCriteriaPanel);
        scrollPane.setPreferredSize(Constants.EDITOR_SCROLLPANE_SIZE);
        return scrollPane;
    }

    /**
     * Builds the panel.
     */
    private void buildPanel()
    {
        myIgnoreEvents = true;
        if (myIsFilterAccepted)
        {
            JScrollPane centerPanel = buildCenterPanel();
            add(buildTopPanel(), BorderLayout.NORTH);
            add(centerPanel, BorderLayout.CENTER);
        }
        else
        {
            add(buildUnhandledPanel(), BorderLayout.CENTER);
        }
        myIgnoreEvents = false;
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
                myCriteriaPanel.addNewCriterion();
            }
        });

        GridBagPanel panel = new GridBagPanel();
        panel.anchorWest();
        panel.setInsets(0, 0, Constants.DOUBLE_INSET, 0);
        panel.add(addCriterionButton);
        panel.add(Box.createHorizontalStrut(60));
        panel.add(new JLabel("Match:"));
        ViewSettings<Logical> viewSettings = new ViewSettings<Logical>().setOptions(new Logical[] { Logical.AND, Logical.OR });
        panel.add(ControllerFactory.createComponent(myModel.getGroup().getOperator(), RadioButtonPanel.class, viewSettings));
        panel.fillHorizontalSpace();
        return panel;
    }

    /**
     * Builds an unhandled panel.
     *
     * @return an unhandled panel
     */
    private JPanel buildUnhandledPanel()
    {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("The filter is too complex to display in basic mode. Please go to the Advanced tab.");
        label.setForeground(Color.ORANGE);
        panel.add(label);
        return panel;
    }
}

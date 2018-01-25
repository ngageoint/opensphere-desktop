package io.opensphere.filterbuilder2.editor.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;

import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.filterbuilder2.common.Constants;
import io.opensphere.filterbuilder2.editor.common.CriterionEditorPanel;
import io.opensphere.filterbuilder2.editor.model.CriterionModel;
import io.opensphere.filterbuilder2.editor.model.FilterModel;

/**
 * The inner editor panel with all the individual criteria.
 */
public class CriteriaEditorPanel extends GridBagPanel implements KeyListener
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The model. */
    private final FilterModel myModel;

    /**
     * Constructor.
     *
     * @param model the model
     */
    public CriteriaEditorPanel(FilterModel model)
    {
        super();
        myModel = model;

        // Add a row if this is a new/empty filter
        if (myModel.getGroup().getCriteria().isEmpty())
        {
            myModel.getGroup().addNewCriterion();
        }

        buildPanel();
    }

    /**
     * Adds a new criterion.
     */
    public void addNewCriterion()
    {
        myModel.getGroup().addNewCriterion();
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
        {
            addNewCriterion();
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    /**
     * Rebuilds the panel.
     */
    public void rebuildPanel()
    {
        for (Component comp : getComponents())
        {
            if (comp instanceof CriterionEditorPanel)
            {
                CriterionEditorPanel panel = (CriterionEditorPanel)comp;
                panel.close();
            }
        }

        removeAll();
        buildPanel();
        validate();
        repaint();
    }

    /**
     * Adds a criterion row.
     *
     * @param criterion the criterion model
     */
    private void addCriterionRow(final CriterionModel criterion)
    {
        IconButton removeButton = new IconButton(IconType.CLOSE, Color.RED);
        removeButton.setToolTipText("Remove the " + Constants.EXPRESSION.toLowerCase());
        removeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                removeCriterion(criterion);
            }
        });

        setInsets(0, 0, 0, 0);
        fillHorizontal();
        CriterionEditorPanel criterionPanel = new CriterionEditorPanel(criterion, 8);
        criterionPanel.setKeyListener(this);
        add(criterionPanel);
        incrementGridx();

        setInsets(Constants.INSET, Constants.INSET, Constants.INSET, Constants.INSET);
        fillNone();
        add(removeButton);
        incrementGridx();

        setGridx(0);
        incrementGridy();
    }

    /**
     * Adds a logic operator row.
     */
    private void addLogicOperator()
    {
        setInsets(0, 0, 0, 0);
        fillNone();
        add(new JLabel(myModel.getGroup().getOperator().get().getLogicText()));
        incrementGridx();

        setGridx(0);
        incrementGridy();
    }

    /**
     * Builds the panel.
     */
    private void buildPanel()
    {
        init0();
        boolean isFirst = true;
        for (CriterionModel criterion : myModel.getGroup().getCriteria())
        {
            if (!isFirst)
            {
                addLogicOperator();
            }

            addCriterionRow(criterion);
            isFirst = false;
        }
        fillVerticalSpace();
    }

    /**
     * Removes a criterion.
     *
     * @param criterion The criterion to remove
     */
    private void removeCriterion(CriterionModel criterion)
    {
        myModel.getGroup().removeCriterion(criterion);
    }
}

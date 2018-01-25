package io.opensphere.imagery.histogram;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import io.opensphere.core.util.collections.New;

/**
 * The Class HistoCurveEditor.
 */
public class HistoCurveEditor extends JFrame
{
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Colors. */
    private final List<Color> myColors = New.unmodifiableList(Color.red, Color.green, Color.blue);

    /** The Display panels. */
    private final List<HistoManipulationDisplayPanel> myDisplayPanels;

    /**
     * Instantiates a new histo curve editor.
     *
     * @param bandNum the band num
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public HistoCurveEditor(int bandNum)
    {
        setLayout(new BorderLayout());

        JToolBar toolBar = buildToolBar();

        JPanel histoPanels = new JPanel();
        histoPanels.setLayout(new BoxLayout(histoPanels, BoxLayout.Y_AXIS));
        this.add(toolBar, BorderLayout.NORTH);
        this.add(histoPanels, BorderLayout.CENTER);

        myDisplayPanels = new ArrayList<>();
        for (int i = 0; i < bandNum; i++)
        {
            HistoManipulationDisplayPanel dispPanel = new HistoManipulationDisplayPanel(myColors.get(i));
            myDisplayPanels.add(dispPanel);
            histoPanels.add(dispPanel);
        }

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
    }

    /**
     * Sets the all histos to mode.
     *
     * @param modifyMode the new all histos to mode
     */
    protected void setAllHistosToMode(int modifyMode)
    {
        for (HistoManipulationDisplayPanel histo : myDisplayPanels)
        {
            histo.setMouseMode(modifyMode);
        }
    }

    /**
     * Builds the tool bar.
     *
     * @return the j tool bar
     */
    private JToolBar buildToolBar()
    {
        JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
        toolBar.setLayout(new BorderLayout());

        ImageIcon cursorIcon = new ImageIcon(HistoCurveEditor.class.getResource("/images/histoCursor.png"));
        ImageIcon addPointIcon = new ImageIcon(HistoCurveEditor.class.getResource("/images/histoAddPoint.png"));
        ImageIcon deletePointIcon = new ImageIcon(HistoCurveEditor.class.getResource("/images/histoDeletePoint.png"));

        final JToggleButton modifyPointsButton = new JToggleButton(cursorIcon);
        final JToggleButton addPointsButton = new JToggleButton(addPointIcon);
        final JToggleButton deletePointsButton = new JToggleButton(deletePointIcon);

        modifyPointsButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setAllHistosToMode(HistoManipulationDisplayPanel.MODIFY_MODE);
            }
        });

        addPointsButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setAllHistosToMode(HistoManipulationDisplayPanel.INSERT_MODE);
            }
        });

        deletePointsButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setAllHistosToMode(HistoManipulationDisplayPanel.DELETE_MODE);
            }
        });

        modifyPointsButton.setPreferredSize(new Dimension(32, 32));
        addPointsButton.setPreferredSize(new Dimension(32, 32));
        deletePointsButton.setPreferredSize(new Dimension(32, 32));

        modifyPointsButton.setToolTipText("Modify color points");
        addPointsButton.setToolTipText("Add color points");
        deletePointsButton.setToolTipText("Delete color points");

        modifyPointsButton.setSelected(true);

        JPanel tempPanel = new JPanel();
        tempPanel.setLayout(new FlowLayout());
        tempPanel.add(modifyPointsButton);
        tempPanel.add(addPointsButton);
        tempPanel.add(deletePointsButton);
        toolBar.add(tempPanel, BorderLayout.WEST);

        ButtonGroup pointButtonsGrp = new ButtonGroup();
        pointButtonsGrp.add(modifyPointsButton);
        pointButtonsGrp.add(addPointsButton);
        pointButtonsGrp.add(deletePointsButton);

        return toolBar;
    }
}

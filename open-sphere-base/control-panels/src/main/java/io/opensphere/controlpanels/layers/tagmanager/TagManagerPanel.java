package io.opensphere.controlpanels.layers.tagmanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import io.opensphere.controlpanels.layers.tagmanager.TagManagerController.TagManagerControllerListener;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class ActiveGroupBookmarkPanel.
 */
public class TagManagerPanel extends JPanel implements TagManagerControllerListener
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The add button. */
    private JButton myAddButton;

    /** The controller. */
    private final transient TagManagerController myController;

    /** The delete button. */
    private JButton myDeleteButton;

    /** The DGI. */
    private final transient DataGroupInfo myDGI;

    /** The DTI. */
    private final transient DataTypeInfo myDTI;

    /** The tags list. */
    private JList<String> myTagList;

    /** The tag scroll pane. */
    private JScrollPane myTagScrollPane;

    /** The tags panel. */
    private Box myTagsPanel;

    /** The toolbox. */
    private final transient Toolbox myToolbox;

    /**
     * Instantiates a new tag manager for a {@link DataGroupInfo}.
     *
     * @param tb the {@link Toolbox}
     * @param dgi the {@link DataGroupInfo}
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public TagManagerPanel(Toolbox tb, DataGroupInfo dgi)
    {
        this(tb, dgi, null);
    }

    /**
     * Instantiates a new active layer book mark panel.
     *
     * @param tb the {@link Toolbox}
     * @param dgi the dgi
     * @param dti the dti
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public TagManagerPanel(Toolbox tb, DataGroupInfo dgi, DataTypeInfo dti)
    {
        super();
        myToolbox = tb;
        myDTI = dti;
        myDGI = dgi;
        myController = dgi != null ? new TagManagerController(myToolbox, dgi) : new TagManagerController(tb, dti);

        setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        myController.addListener(this);
        setLayout(new BorderLayout());

        Box labelBox = Box.createVerticalBox();
        JLabel name = new JLabel(getTagItemName());
        name.setFont(name.getFont().deriveFont(Font.BOLD, name.getFont().getSize() + 2));
        Box nameBox = Box.createHorizontalBox();
        nameBox.add(Box.createHorizontalGlue());
        nameBox.add(name);
        nameBox.add(Box.createHorizontalGlue());
        labelBox.add(nameBox);
        labelBox.add(Box.createVerticalStrut(5));

        Box topBox = Box.createHorizontalBox();
        topBox.add(getTagsPanel());
        add(labelBox, BorderLayout.NORTH);
        add(topBox, BorderLayout.CENTER);
        tagsChanged();
        getDeleteTagButton().setEnabled(false);
        setPreferredSize(new Dimension(350, 300));
        setMinimumSize(new Dimension(350, 300));
    }

    /**
     * Instantiates a new tag manager for a {@link DataTypeInfo}.
     *
     * @param tb the {@link Toolbox}
     * @param dti the {@link DataTypeInfo}
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public TagManagerPanel(Toolbox tb, DataTypeInfo dti)
    {
        this(tb, null, dti);
    }

    /**
     * Gets the adds the book mark button.
     *
     * @return the adds the book mark button
     */
    public JButton getAddBookmarkButton()
    {
        if (myAddButton == null)
        {
            myAddButton = new JButton();
            IconUtil.setIcons(myAddButton, IconType.PLUS, Color.GREEN);
            myAddButton.setToolTipText("Create a new tag.");
            myAddButton.setMargin(new Insets(3, 3, 3, 3));
            myAddButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (myDGI != null)
                    {
                        TagUtility.showTagDataGroupDialog(TagManagerPanel.this, myDGI, myController);
                    }
                    else
                    {
                        TagUtility.showTagDataTypeDialog(TagManagerPanel.this, myDTI, myController);
                    }
                }
            });
        }
        return myAddButton;
    }

    /**
     * Gets the book mark scroll pane.
     *
     * @return the book mark scroll pane
     */
    public JScrollPane getBookmarkScrollPane()
    {
        if (myTagScrollPane == null)
        {
            myTagScrollPane = new JScrollPane(getTagList());
        }
        return myTagScrollPane;
    }

    /**
     * Gets the delete book mark button.
     *
     * @return the delete book mark button
     */
    public JButton getDeleteTagButton()
    {
        if (myDeleteButton == null)
        {
            myDeleteButton = new JButton();
            myDeleteButton.setToolTipText("Delete the selected tags(s)");
            try
            {
                ImageIcon normal = new ImageIcon(ImageIO.read(TagManagerPanel.class.getResource("/images/minus_big.png")));
                ImageIcon over = new ImageIcon(ImageIO.read(TagManagerPanel.class.getResource("/images/minus_big_over.png")));
                ImageIcon press = new ImageIcon(ImageIO.read(TagManagerPanel.class.getResource("/images/minus_big_press.png")));
                myDeleteButton.setIcon(normal);
                myDeleteButton.setRolloverIcon(over);
                myDeleteButton.setPressedIcon(press);
                myDeleteButton.setMargin(new Insets(3, 3, 2, 3));
            }
            catch (IOException e)
            {
                myDeleteButton.setText("Delete");
            }
            myDeleteButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    List<String> values = getTagList().getSelectedValuesList();
                    for (String tagName : values)
                    {
                        myController.deleteTag(tagName);
                    }
                }
            });
        }
        return myDeleteButton;
    }

    /**
     * Gets the book mark list.
     *
     * @return the book mark list
     */
    public JList<String> getTagList()
    {
        if (myTagList == null)
        {
            myTagList = new JList<>();
            myTagList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
            {
                @Override
                public void valueChanged(ListSelectionEvent e)
                {
                    if (!e.getValueIsAdjusting())
                    {
                        int[] selectedIndexes = myTagList.getSelectedIndices();
                        int count = selectedIndexes.length;
                        getDeleteTagButton().setEnabled(count >= 1);
                    }
                }
            });
        }
        return myTagList;
    }

    /**
     * Gets the tags panel.
     *
     * @return the tags panel
     */
    public Box getTagsPanel()
    {
        if (myTagsPanel == null)
        {
            myTagsPanel = Box.createVerticalBox();
            myTagsPanel.setBorder(new TitledBorder("Tags"));
            myTagsPanel.setMinimumSize(new Dimension(0, 250));
            myTagsPanel.setPreferredSize(new Dimension(0, 250));
            myTagsPanel.add(Box.createVerticalStrut(3));
            myTagsPanel.add(getBookmarkScrollPane());
            myTagsPanel.add(Box.createVerticalStrut(3));
            Box buttonBox = Box.createHorizontalBox();
            buttonBox.add(Box.createHorizontalStrut(4));
            buttonBox.add(getAddBookmarkButton());
            buttonBox.add(Box.createHorizontalStrut(4));
            buttonBox.add(getDeleteTagButton());
            buttonBox.add(Box.createHorizontalGlue());
            myTagsPanel.add(buttonBox);
        }
        return myTagsPanel;
    }

    @Override
    public void tagsChanged()
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                Object lastSelectedBookmark = null;
                List<String> values = getTagList().getSelectedValuesList();
                if (values.size() == 1)
                {
                    lastSelectedBookmark = values.get(0);
                }

                List<String> setNames = myController.getTags();
                DefaultListModel<String> model = new DefaultListModel<>();
                if (setNames != null && !setNames.isEmpty())
                {
                    model.ensureCapacity(setNames.size());
                    for (String name : setNames)
                    {
                        model.addElement(name);
                    }
                }
                getTagList().setModel(model);

                if (lastSelectedBookmark != null)
                {
                    getTagList().setSelectedValue(lastSelectedBookmark, true);
                }
            }
        });
    }

    /**
     * Gets the tag item name.
     *
     * @return the tag item name
     */
    private String getTagItemName()
    {
        if (myDGI != null)
        {
            return myDGI.getDisplayName();
        }
        else if (myDTI != null)
        {
            return myDTI.getDisplayName();
        }
        else
        {
            return "UNKNOWN";
        }
    }
}

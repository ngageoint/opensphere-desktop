package io.opensphere.search;

import java.awt.BorderLayout;

import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import io.opensphere.core.Toolbox;
import io.opensphere.core.hud.awt.AbstractInternalFrame;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.swing.AbstractHUDPanel;
import io.opensphere.search.model.SearchModel;
import io.opensphere.search.view.SearchDialogPanel;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

/** A HUD-dialog in which search results are rendered. */
public class SearchResultHudDialog extends AbstractInternalFrame
{
    /** The title of the frame in which the search results are presented. */
    public static final String TITLE = "Search Results";

    /** The internal frame border width. */
    private static final int FRAME_BORDER_WIDTH = 30;

    /** The unique identifier used for serialization. */
    private static final long serialVersionUID = 5814849968949784006L;

    /** The panel in which the results are rendered. */
    private final SearchDialogPanel myResultPanel;

    /** The container in which the results are rendered. */
    private final JFXPanel myResultPanelContainer;

    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /**
     * Creates a new search result dialog, bound to the supplied toolbox.
     *
     * @param toolbox The toolbox through which application state is accessed.
     * @param searchModel The main search model.
     */
    public SearchResultHudDialog(Toolbox toolbox, SearchModel searchModel)
    {
        super();
        myToolbox = toolbox;

        setTitle(TITLE);
        setOpaque(false);
        setIconifiable(false);
        setClosable(true);
        setResizable(true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        myResultPanel = new SearchDialogPanel(toolbox, searchModel);
        myResultPanelContainer = new JFXPanel();
        Scene scene = new Scene(myResultPanel, 500, 800);
        myResultPanelContainer.setScene(FXUtilities.addDesktopStyle(scene));
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        AbstractHUDPanel mainPanel = new AbstractHUDPanel(myToolbox.getPreferencesRegistry());
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(myResultPanelContainer, BorderLayout.CENTER);

        setContentPane(mainPanel);

        addInternalFrameListener(new InternalFrameAdapter()
        {
            @Override
            public void internalFrameActivated(InternalFrameEvent e)
            {
                myResultPanel.setDialogVisible(true);
            }

            @Override
            public void internalFrameClosing(InternalFrameEvent e)
            {
                myResultPanel.setDialogVisible(false);
            }
        });
    }

    /**
     * Gets the result model into which results will be populated.
     *
     * @return the result model into which results will be populated.
     * @see io.opensphere.search.view.SearchDialogPanel#getModel()
     */
    public SearchModel getModel()
    {
        return myResultPanel.getModel();
    }

    /**
     * Sizes and positions the frame to the default location.
     *
     * @param performSearch Indicates if the dialog should initiate a search (if
     *            the keyword hasn't changed). True if the dialog is being shown
     *            by the What's Here button, false if the dialog is being shown
     *            because the user initiated a keyword search.
     */
    public void resizeAndPositionToDefault(boolean performSearch)
    {
        if (performSearch)
        {
            myResultPanel.performSearch();
        }
        int width = 150;
        int height = 850;
        int xposition = 0;
        int yposition = 0;
        if (myResultPanelContainer.getPreferredSize() != null)
        {
            width = myResultPanelContainer.getPreferredSize().width + FRAME_BORDER_WIDTH;
        }
        if (getParent() != null)
        {
            height = getParent().getHeight() - 187;
            xposition = getParent().getWidth() - getWidth();
        }
        setSize(width, height);
        setLocation(xposition, yposition);
        validate();
    }
}

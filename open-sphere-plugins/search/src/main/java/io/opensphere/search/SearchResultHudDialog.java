package io.opensphere.search;

import java.awt.BorderLayout;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import io.opensphere.core.Toolbox;
import io.opensphere.core.hud.awt.AbstractInternalFrame;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.swing.AbstractHUDPanel;
import io.opensphere.search.model.SearchModel;
import io.opensphere.search.view.SearchDialogPanel;

/** A HUD-dialog in which search results are rendered. */
public class SearchResultHudDialog extends AbstractInternalFrame
{
    /** The height offset used to avoid the timeline. */
    private static final int HEIGHT_OFFSET = 187;

    /** The default height of the HUD window. */
    private static final int DEFAULT_HEIGHT = 850;

    /** The default width of the HUD window. */
    private static final int DEFAULT_WIDTH = 550;

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

        // set up default height and width:
        int width = DEFAULT_WIDTH;
        int height = DEFAULT_HEIGHT;
        int xposition = 0;
        int yposition = 0;
        if (getParent() != null)
        {
            height = getParent().getHeight() - HEIGHT_OFFSET;
            xposition = getParent().getWidth() - getWidth();
        }
        setSize(width, height);
        setLocation(xposition, yposition);

        myResultPanel = new SearchDialogPanel(toolbox, searchModel);
        myResultPanelContainer = new JFXPanel();

        Scene scene = FXUtilities.addDesktopStyle(new Scene(myResultPanel, DEFAULT_WIDTH, DEFAULT_HEIGHT));
        FXUtilities.runOnFXThreadAndWait(() -> myResultPanelContainer.setScene(scene));

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
                // VORTEX-5538 Temporarily disable search notification dialog
                // until recommended layers is added back
//                myResultPanel.setDialogVisible(true);
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
        int width = DEFAULT_WIDTH;
        int height = DEFAULT_HEIGHT;
        int xposition = 0;
        int yposition = 0;
        if (myResultPanelContainer.getPreferredSize() != null)
        {
            width = myResultPanelContainer.getPreferredSize().width + FRAME_BORDER_WIDTH;
        }
        if (getParent() != null)
        {
            height = getParent().getHeight() - HEIGHT_OFFSET;
            xposition = getParent().getWidth() - getWidth();
        }
        setSize(width, height);
        setLocation(xposition, yposition);
        validate();
    }
}

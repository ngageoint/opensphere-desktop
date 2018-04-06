package io.opensphere.search;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.api.adapter.AbstractHUDFrameMenuItemPlugin;
import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.control.ui.ToolbarManager.ToolbarLocation;
import io.opensphere.core.event.ApplicationLifecycleEvent;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.hud.awt.AbstractInternalFrame;
import io.opensphere.core.hud.awt.HUDJInternalFrame;
import io.opensphere.core.search.ResultsSearchProvider;
import io.opensphere.core.search.SearchProvider;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.search.model.SearchModel;
import javafx.application.Platform;

/** Class for the "Goto" hud frame. */
public class SearchPlugin extends AbstractHUDFrameMenuItemPlugin
{
    /** The toolbox. */
    private Toolbox myToolbox;

    /** The transformer that displays point on the map. */
    private SearchTransformer myTransformer;

    /** The dialog in which results are presented to the user. */
    private SearchResultHudDialog myResultsDialog;

    /** The search model. */
    private final SearchModel myModel = new SearchModel();

    /** The plugin init listener. */
    private final EventListener<ApplicationLifecycleEvent> myPluginInitListener = this::handleApplicationLifecycleEvent;

    /** The provider under which search provider configurations are grouped. */
    private SearchOptionsProvider mySearchOptionsProvider;

    /** Creates a new instance of the search plugin. */
    public SearchPlugin()
    {
        super(SearchResultHudDialog.TITLE, false, true);
    }

    @Override
    public void close()
    {
        super.close();
        myTransformer.removeAllGeometries();
        myToolbox.getUIRegistry().getOptionsRegistry().removeOptionsProvider(mySearchOptionsProvider);
        myToolbox.getEventManager().unsubscribe(ApplicationLifecycleEvent.class, myPluginInitListener);
    }

    @Override
    public Collection<? extends Transformer> getTransformers()
    {
        return Collections.singleton(myTransformer);
    }

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        super.initialize(plugindata, toolbox);
        myToolbox = toolbox;

        mySearchOptionsProvider = new SearchOptionsProvider(toolbox.getPreferencesRegistry());
        toolbox.getUIRegistry().getOptionsRegistry().addOptionsProvider(mySearchOptionsProvider);

        myTransformer = new SearchTransformer(myToolbox);

        toolbox.getEventManager().subscribe(ApplicationLifecycleEvent.class, myPluginInitListener);

        final SearchPlugin instance = this;
        EventQueueUtilities.runOnEDTAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                SearchResultHudDialog dialog = (SearchResultHudDialog)createInternalFrame(toolbox);

                KeywordController controller = new KeywordController(myToolbox, myTransformer);
                controller.setModel(dialog.getModel());
                SearchPanel mainPanel = new SearchPanel(controller, myResultsDialog, instance.getHUDFrameSupplier(), myModel);

                myToolbox.getUIRegistry().getToolbarComponentRegistry().registerToolbarComponent(ToolbarLocation.NORTH,
                        "SearchGoto", mainPanel, 10000, SeparatorLocation.RIGHT);
            }
        });
    }

    /**
     * Gets a supplier to provide the internal HUD Frame.
     *
     * @return a supplier to provide the internal HUD Frame.
     */
    protected Supplier<HUDJInternalFrame> getHUDFrameSupplier()
    {
        return this::getHUDFrame;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.api.adapter.AbstractHUDFrameMenuItemPlugin#createInternalFrame(io.opensphere.core.Toolbox)
     */
    @Override
    protected AbstractInternalFrame createInternalFrame(Toolbox toolbox)
    {
        assert SwingUtilities.isEventDispatchThread();
        if (myResultsDialog == null)
        {
            myResultsDialog = new SearchResultHudDialog(toolbox, myModel);
        }
        return myResultsDialog;
    }

    /**
     * Handles a {@link ApplicationLifecycleEvent}.
     *
     * @param event the event
     */
    private void handleApplicationLifecycleEvent(ApplicationLifecycleEvent event)
    {
        if (event.getStage() == ApplicationLifecycleEvent.Stage.PLUGINS_INITIALIZED)
        {
            populateSearchTypes();
        }
    }

    /**
     * Populates the search types available for the user.
     */
    private void populateSearchTypes()
    {
        Map<String, Map<String, SearchProvider>> providers = myToolbox.getSearchRegistry().getProviders();
        Set<String> searchTypes = New.set();
        for (Entry<String, Map<String, SearchProvider>> entry : providers.entrySet())
        {
            for (Entry<String, SearchProvider> sameTypeProviders : entry.getValue().entrySet())
            {
                if (sameTypeProviders.getValue() instanceof ResultsSearchProvider)
                {
                    searchTypes.add(sameTypeProviders.getValue().getType());
                }
            }
        }

        List<String> allSearchTypes = New.list(searchTypes);
        Collections.sort(allSearchTypes);

        Platform.runLater(() ->
        {
            myModel.getSelectedSearchTypes().addAll(allSearchTypes);
            myModel.getSearchTypes().addAll(allSearchTypes);
        });
    }
}

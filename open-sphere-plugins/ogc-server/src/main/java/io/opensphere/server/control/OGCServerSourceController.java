package io.opensphere.server.control;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.server.config.v1.OGCServerConfig;
import io.opensphere.server.control.OGCServerHandler.ServerHandlerLoadListener;
import io.opensphere.server.customization.DefaultCustomization;
import io.opensphere.server.customization.GeoServerCustomization;
import io.opensphere.server.display.DefaultServerSourceEditor;
import io.opensphere.server.display.ServerSourceEditor;
import io.opensphere.server.display.ServiceValidator;
import io.opensphere.server.display.SingleUrlServerSourceEditor;
import io.opensphere.server.display.model.GeoServerSourceModel;
import io.opensphere.server.display.model.OGCServerSourceModel;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.state.activate.serversource.ServerSourceFilterer;
import io.opensphere.server.state.activate.serversource.ServerSourceProvider;
import io.opensphere.server.state.activate.serversource.genericserver.GenericServerSourceFilterer;
import io.opensphere.server.state.activate.serversource.genericserver.GenericServerSourceProvider;
import io.opensphere.server.toolbox.ServerSourceController;
import io.opensphere.server.toolbox.ServerSourceControllerManager;
import io.opensphere.server.toolbox.ServerToolbox;
import io.opensphere.server.toolbox.ServerToolboxUtils;
import io.opensphere.server.toolbox.StateServerSourceController;

/**
 * The OGC server data source controller.
 */
public class OGCServerSourceController extends AbstractServerSourceController
        implements ServerHandlerLoadListener, StateServerSourceController
{
    /** Server handler reference. */
    private OGCServerHandler myServerHandler;

    /** Flag that gets set when the controller is initialized. */
    private boolean myControllerInitialized;

    @Override
    public void activateSource(IDataSource source)
    {
        OGCServerSource aSource = (OGCServerSource)source;
        if (!aSource.isLoaded())
        {
            myServerHandler.activateSource(aSource);
        }
    }

    @Override
    public IDataSource createNewSource(String typeName)
    {
        OGCServerSource serverSource = new OGCServerSource();
        serverSource.setName("");
        serverSource.setServerType(typeName);
        return serverSource;
    }

    @Override
    public void deactivateSource(IDataSource source)
    {
        myServerHandler.deactivateSource(source);
    }

    @Override
    public int getOrdinal()
    {
        return 1;
    }

    @Override
    public ServerSourceFilterer getServerSourceFilterer()
    {
        return new GenericServerSourceFilterer(this);
    }

    @Override
    public String getSourceDescription(IDataSource source)
    {
        StringBuilder description = new StringBuilder("<html>");
        if (source instanceof OGCServerSource)
        {
            OGCServerSource ogcSource = (OGCServerSource)source;

            ServerToolbox serverToolbox = ServerToolboxUtils.getServerToolbox(getToolbox());

            description.append(serverToolbox.getServerLabelGenerator().buildLabelFromType(ogcSource.getServerType())).append(": ")
                    .append(ogcSource.getName());
            if (!StringUtils.isBlank(ogcSource.getWMSServerURL()))
            {
                description.append("<br>&nbsp WMS: ").append(ogcSource.getWMSServerURL());
            }
            if (!StringUtils.isBlank(ogcSource.getWFSServerURL()))
            {
                description.append("<br>&nbsp WFS: ").append(ogcSource.getWFSServerURL());
            }
            if (!StringUtils.isBlank(ogcSource.getWPSServerURL()))
            {
                description.append("<br>&nbsp WPS: ").append(ogcSource.getWPSServerURL());
            }
        }
        description.append("</html>");
        return description.toString();
    }

    @Override
    public ServerSourceProvider getStateServerProvider()
    {
        return new GenericServerSourceProvider(getToolbox());
    }

    @Override
    public String getTypeName(IDataSource source)
    {
        String typeName = null;
        if (source instanceof OGCServerSource)
        {
            typeName = ((OGCServerSource)source).getServerType();
        }
        return typeName;
    }

    @Override
    public void loadEnded(IDataSource source, boolean success, String error)
    {
        updateSource(source);
    }

    @Override
    public void loadStarted(IDataSource source)
    {
    }

    @Override
    public void open(final Toolbox toolbox, Class<?> prefsTopic)
    {
        super.open(toolbox, prefsTopic);
        configureServerTypes(toolbox);

        Preferences preferences = toolbox.getPreferencesRegistry().getPreferences(prefsTopic);
        myServerHandler = new OGCServerHandler(toolbox);
        myServerHandler.addLoadListener(this);
        setConfig(preferences.getJAXBObject(OGCServerConfig.class, getPrefsKey(), new OGCServerConfig()));

        if (!myControllerInitialized)
        {
            initialize();
            myControllerInitialized = true;
        }
    }

    /**
     * Configures the set of supported server types.
     *
     * @param toolbox the toolbox with which the service validator is
     *            initialized.
     */
    protected void configureServerTypes(final Toolbox toolbox)
    {
        final ServiceValidator<OGCServerSource> validator = new ServiceValidatorImpl(toolbox);

        addServerType(new DefaultCustomization(), new SingleCallable<ServerSourceEditor>()
        {
            @Override
            protected ServerSourceEditor callOnce()
            {
                return new DefaultServerSourceEditor<>(new OGCServerSourceModel(), validator);
            }
        });
        addServerType(new GeoServerCustomization(), new SingleCallable<ServerSourceEditor>()
        {
            @Override
            protected ServerSourceEditor callOnce()
            {
                return new SingleUrlServerSourceEditor(new GeoServerSourceModel(), validator);
            }
        });
        // addServerType(new ArcGisCustomization(), new
        // SingleCallable<ServerSourceEditor>()
        // {
        // @Override
        // protected ServerSourceEditor callOnce()
        // {
        // return new DefaultServerSourceEditor<OGCServerSource>(new
        // ArcGisServerSourceModel(), validator);
        // }
        // });
    }

    /**
     * Gets a list of all server sources.
     *
     * @return list of sources from all {@link ServerSourceController}s
     */
    protected List<IDataSource> getFullSourceList()
    {
        List<IDataSource> sourceList = New.list();
        ServerSourceControllerManager ctrlMgr = ServerToolboxUtils.getServerSourceControllerManager(getToolbox());
        if (ctrlMgr != null)
        {
            for (ServerSourceController ctrl : ctrlMgr.getControllers())
            {
                sourceList.addAll(ctrl.getSourceList());
            }
        }
        return sourceList;
    }
}

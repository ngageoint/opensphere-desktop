package io.opensphere.myplaces.controllers;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.lang3.StringUtils;

import de.micromata.opengis.kml.v_2_2_0.Data;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.viewbookmark.ViewBookmark;
import io.opensphere.core.viewbookmark.util.ViewBookmarkUtil;
import io.opensphere.mantle.data.CategoryContextKey;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.util.impl.DataTypeActionUtils;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.dataaccess.MyPlacesDataAccessor;
import io.opensphere.myplaces.importer.MyPlacesMasterImporter;
import io.opensphere.myplaces.migration.Migrator;
import io.opensphere.myplaces.models.MyPlacesDataGroupInfo;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.models.MyPlacesModel;
import io.opensphere.myplaces.renderer.MyPlacesRenderer;
import io.opensphere.myplaces.specific.PlaceTypeController;
import io.opensphere.myplaces.specific.PointGoer;
import io.opensphere.myplaces.specific.factory.TypeControllerFactory;

/**
 * The main controller for the My Places plugin.
 */
public class MyPlacesController implements Observer, Runnable, PointGoer, Service
{
    /**
     * The time wait to check for changes.
     */
    private static final int WAIT_TIME = 1000;

    /**
     * The context menu provider for categories.
     */
    private final CategoryContextMenuProvider myCategoryMenuProvider;

    /**
     * Loads and saves the my places data.
     */
    private final MyPlacesDataAccessor myDataAccessor;

    /**
     * The controller for the data groups or active layer view portion of my
     * places.
     */
    private final MyPlacesDataGroupController myDataGroupController;

    /**
     * Executes saves in the background.
     */
    private ProcrastinatingExecutor myExecutor;

    /**
     * Migrates the old data to the new my places data.
     */
    private final Migrator myMigrator;

    /**
     * The my places model.
     */
    private final MyPlacesModel myModel;

    /**
     * Renderers the my places data.
     */
    private MyPlacesRenderer myRenderer;

    /**
     * The context menu provider for single group selection.
     */
    private SingleGroupContextMenuProvider mySingleGroupMenuProvider;

    /**
     * Adds my places to the timeline.
     */
    private MyPlacesTimelineController myTimelineController;

    /**
     * The toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a new MyPlacesController.
     *
     * @param toolbox The toolbox.
     */
    public MyPlacesController(Toolbox toolbox)
    {
        myModel = new MyPlacesModel();
        myToolbox = toolbox;
        myDataAccessor = new MyPlacesDataAccessor(myToolbox);
        myMigrator = new Migrator(myToolbox, myDataAccessor);
        myDataGroupController = new MyPlacesDataGroupController(myToolbox, myModel);
        mySingleGroupMenuProvider = new SingleGroupContextMenuProvider(myToolbox, myModel, this);
        myCategoryMenuProvider = new CategoryContextMenuProvider(myToolbox, myModel);
        myToolbox.getImporterRegistry().addImporter(new MyPlacesMasterImporter(myToolbox, myModel));
    }

    /**
     * Closes open resources.
     */
    @Override
    public void close()
    {
        myTimelineController.close();
        for (PlaceTypeController controller : TypeControllerFactory.getInstance().getControllers())
        {
            controller.close();
        }

        myExecutor.shutdown();
        myDataAccessor.saveMyPlacesSynchronized(myModel.getMyPlaces());
        myDataGroupController.close();

        ContextActionManager contextActionManager = myToolbox.getUIRegistry().getContextActionManager();
        contextActionManager.deregisterContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT,
                DataGroupInfo.DataGroupContextKey.class, mySingleGroupMenuProvider);
        contextActionManager.deregisterContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT, CategoryContextKey.class,
                myCategoryMenuProvider);
    }

    /**
     * Gets the list of transformers used to render.
     *
     * @return The list of transformers.
     */
    public List<Transformer> getTransformers()
    {
        return myRenderer.getTransformers();
    }

    @Override
    public void gotoPoint(DataTypeInfo dataType)
    {
        if (dataType != null)
        {
            boolean wentToView = false;
            String associatedView = dataType.getAssociatedView();

            if (!StringUtils.isBlank(associatedView))
            {
                ViewBookmark vbm = myToolbox.getMapManager().getViewBookmarkRegistry().getViewBookmarkByName(associatedView);
                if (vbm != null)
                {
                    wentToView = true;
                    ViewBookmarkUtil.gotoView(myToolbox, vbm);
                }
            }

            if (!wentToView)
            {
                DataTypeActionUtils.gotoDataType(dataType, myToolbox.getMapManager().getStandardViewer());
            }
        }
    }

    /**
     * Initialized my places.
     */
    @Override
    public void open()
    {
        myExecutor = new ProcrastinatingExecutor("My Places Change", WAIT_TIME);

        myMigrator.migrateAllIfNeeded();
        Kml kml = myDataAccessor.loadMyPlaces();
        myModel.setMyPlaces(kml);

        myDataGroupController.loadDataGroups();

        myRenderer = new MyPlacesRenderer(myToolbox, myModel);
        myTimelineController = new MyPlacesTimelineController(myToolbox.getUIRegistry().getTimelineRegistry(),
                myToolbox.getOrderManagerRegistry(), myModel);
        myModel.addObserver(this);

        for (PlaceTypeController controller : TypeControllerFactory.getInstance().getControllers())
        {
            controller.initialize(myToolbox, myModel);
        }

        setLocationInformation();

        ContextActionManager contextActionManager = myToolbox.getUIRegistry().getContextActionManager();
        contextActionManager.registerContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT,
                DataGroupInfo.DataGroupContextKey.class, mySingleGroupMenuProvider);
        contextActionManager.registerContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT, CategoryContextKey.class,
                myCategoryMenuProvider);
    }

    @Override
    public void run()
    {
        myDataAccessor.saveMyPlaces(myModel.getMyPlaces());
    }

    @Override
    public void update(Observable o, Object arg)
    {
        myExecutor.execute(this);
        setLocationInformation();
        myRenderer.renderMyPlaces();
        myTimelineController.updateTimeline();
    }

    /**
     * Gets the associated view name for a give placemark.
     *
     * @param dataType The data type to set the associated view for.
     */
    private void setAssociatedView(MyPlacesDataTypeInfo dataType)
    {
        Placemark placemark = dataType.getKmlPlacemark();
        if (placemark != null)
        {
            ExtendedData extendedData = placemark.getExtendedData();
            if (extendedData != null)
            {
                List<Data> datas = extendedData.getData();
                for (Data data : datas)
                {
                    if (Constants.ASSOCIATED_VIEW_ID.equals(data.getName()))
                    {
                        dataType.setAssociatedView(data.getValue());
                        break;
                    }
                }
            }
        }
    }

    /**
     * Sets all location information for each data type where applicable.
     */
    private void setLocationInformation()
    {
        MyPlacesDataGroupInfo dataGroup = myModel.getDataGroups();
        for (DataTypeInfo dataType : dataGroup.getMembers(true))
        {
            if (dataType instanceof MyPlacesDataTypeInfo)
            {
                MyPlacesDataTypeInfo placesDataType = (MyPlacesDataTypeInfo)dataType;
                setAssociatedView(placesDataType);

                MapVisualizationInfo visInfo = placesDataType.getMapVisualizationInfo();
                if (visInfo != null)
                {
                    MapVisualizationType visType = visInfo.getVisualizationType();
                    PlaceTypeController typeController = TypeControllerFactory.getInstance().getController(visType);

                    if (typeController != null)
                    {
                        typeController.setLocationInformation(placesDataType);
                    }
                }
            }
        }
    }
}

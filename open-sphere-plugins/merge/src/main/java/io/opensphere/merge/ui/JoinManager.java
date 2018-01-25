package io.opensphere.merge.ui;

import java.util.LinkedList;
import java.util.List;

import io.opensphere.core.Notify;
import io.opensphere.core.Notify.Method;
import io.opensphere.core.Toolbox;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.ActivationState;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.merge.algorithm.EnvSupport;
import io.opensphere.merge.algorithm.JoinData;
import io.opensphere.merge.algorithm.JoinInfo;
import io.opensphere.merge.algorithm.Util;
import io.opensphere.merge.controller.ColumnMappingSupport;
import io.opensphere.merge.controller.DataRegistryUtils;
import io.opensphere.merge.controller.MergeDataElementProvider;
import io.opensphere.merge.model.JoinModel;
import io.opensphere.merge.model.MergePrefs;
import io.opensphere.merge.model.MergedDataRow;

/**
 * Shared tools for managing layers formed by joining other layers. The task of
 * working with Mantle's annoying interfaces is mostly delegated to the resident
 * DataUtil instance. Persistence of data within the DataRegistry is handled
 * here, with assistance from DataRegistryUtils.
 */
public class JoinManager
{
    /** The system Toolbox. */
    private Toolbox tools;

    /** One of many Mantle tools. */
    private DataGroupController groupCtrl;

    /** Supporting tools for performing join operations. */
    private EnvSupport systemSupport;

    /** For handling the Mantle. */
    private final DataUtil joinLayers = new DataUtil();

    /**
     * Receive a reference to the system Toolbox and perform some activities
     * that require the Toolbox.
     *
     * @param tb the Toolbox
     */
    public void setTools(Toolbox tb)
    {
        tools = tb;
        MantleToolbox mtb = tools.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        groupCtrl = mtb.getDataGroupController();
        systemSupport = new ColumnMappingSupport(mtb.getDataElementLookupUtils(),
                tools.getDataFilterRegistry().getColumnMappingController());
        joinLayers.setTools(tools);
        joinLayers.setupRoot("Joined");
    }

    /**
     * Perform the join operation as instructed by the user.
     *
     * @param m a model containing the join parameters
     */
    public void handleJoin(JoinModel m)
    {
        // perform the join
        JoinData jd = calcJoin(m);
        // install the new layer
        installJoin(m.getJoinName(), jd);
    }

    /**
     * Perform the join from a saved configuration.
     *
     * @param j the join config
     */
    public void handleJoin(MergePrefs.Join j)
    {
        // perform the join
        JoinData jd = calcJoin(j);
        // install the new layer
        installJoin(j.name, jd);
    }

    /**
     * Get the results of a join operation and install them within Mantle and
     * the DataRegistry.
     *
     * @param typeId the name of the layer
     * @param jd the result of the join
     */
    private void installJoin(String typeId, JoinData jd)
    {
        // check for error conditions here?
        String errorMsg = jd.getErrorMessage();
        if (errorMsg != null)
        {
            Notify.info(errorMsg, Method.POPUP);
            return;
        }

        List<MergedDataRow> data = jd.getAllData();
        if (data == null || data.isEmpty())
        {
            Notify.info("Operation produced no data.", Method.POPUP);
            return;
        }

        // deposit in the local cache (delete the old stuff, if any)
        // Note: for performance, we might prefer to delete after inserting,
        // but we can't use the category because then it would delete
        // everything, including what was just inserted
        DataRegistryUtils.delete(tools, typeId);
        DataRegistryUtils.deposit(tools, typeId, data);

        // should be created as part of the join operation
        MetaDataInfo meta = Util.getMeta(jd.getColumnDefs());
        // register the layer first--can't create DataElements without it
        DataTypeInfo dti = joinLayers.registerType(typeId, meta);
        // get the records from the join and wrap as DataElement instances
        joinLayers.repopulateType(typeId, getElements(data, dti));
        joinLayers.setActivationListener(typeId, ActivationStateListener.forState(() -> load(dti), ActivationState.ACTIVE));
        joinLayers.setDeleteListener(typeId, () -> delete(typeId));
    }

    /**
     * Convert MergedDataRow instances to DataElement for the specified layer.
     *
     * @param rows instances of MergeDataRow
     * @param dti the layer
     * @return instances of DataElement
     */
    private List<DataElement> getElements(List<MergedDataRow> rows, DataTypeInfo dti)
    {
        List<DataElement> elts = new LinkedList<>();
        for (MergedDataRow mdr : rows)
        {
            elts.add(MergeDataElementProvider.createElt(dti, mdr));
        }
        return elts;
    }

    /**
     * Recover records from the DataRegistry and reintroduce them to Mantle.
     *
     * @param type the layer
     */
    private void load(DataTypeInfo type)
    {
        String typeId = type.getTypeKey();
        List<MergedDataRow> recs = DataRegistryUtils.query(tools, typeId);
        if (recs == null || recs.isEmpty())
        {
            return;
        }
        joinLayers.repopulateType(typeId, getElements(recs, type));
    }

    /**
     * Remove records associated with the given layer from the DataRegistry.
     *
     * @param layerId the layer ID
     */
    private void delete(String layerId)
    {
        DataRegistryUtils.delete(tools, layerId);
    }

    /**
     * Perform a join operation from the JoinGui.
     *
     * @param m the join parameters
     * @return data records resulting from the join operation
     */
    private JoinData calcJoin(JoinModel m)
    {
        JoinData jd = new JoinData();
        jd.setSupp(systemSupport);
        jd.setUseExact(m.isUseExact());
        for (JoinModel.Rec r : m.getParams())
        {
            jd.getSrc().add(new JoinInfo(r.type, r.column));
        }
        jd.join();
        return jd;
    }

    /**
     * Perform a join operation from a saved join config.
     *
     * @param j the join parameters
     * @return data records resulting from the join operation
     */
    private JoinData calcJoin(MergePrefs.Join j)
    {
        JoinData jd = new JoinData();
        jd.setSupp(systemSupport);
        jd.setUseExact(j.useExact);
        for (MergePrefs.LayerParam lp : j.params)
        {
            DataTypeInfo type = groupCtrl.findMemberById(lp.typeKey);
            jd.getSrc().add(new JoinInfo(type, lp.column));
        }
        jd.join();
        return jd;
    }
}

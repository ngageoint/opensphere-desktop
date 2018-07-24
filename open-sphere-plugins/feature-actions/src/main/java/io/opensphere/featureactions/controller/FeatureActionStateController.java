package io.opensphere.featureactions.controller;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bitsys.fade.mist.state.v4.ActionArrayType;
import com.bitsys.fade.mist.state.v4.FeatureActionArrayType;
import com.bitsys.fade.mist.state.v4.FeatureActionType;
import com.bitsys.fade.mist.state.v4.FeatureActionTypeHint;
import com.bitsys.fade.mist.state.v4.FeatureStyleActionType;
import com.bitsys.fade.mist.state.v4.ObjectFactory;
import com.bitsys.fade.mist.state.v4.ShapeType;
import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.controlpanels.styles.model.StyleOptions;
import io.opensphere.core.modulestate.AbstractLayerStateController;
import io.opensphere.core.modulestate.StateUtilities;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.featureactions.model.Action;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.featureactions.model.StyleAction;
import io.opensphere.featureactions.registry.FeatureActionsRegistry;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.filterbuilder.impl.WFS100FilterToDataFilterConverter;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.mdfilter.FilterException;
import io.opensphere.mantle.data.element.mdfilter.FilterToWFS100Converter;
import javafx.collections.ObservableList;
import net.opengis.ogc._100t.FilterType;

/**
 * A state controller for {@link FeatureAction}.
 */
public class FeatureActionStateController extends AbstractLayerStateController<Pair<FeatureAction, String>>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(FeatureActionStateController.class);

    /** The registry for feature actions. */
    private final FeatureActionsRegistry myRegistry;

    /** The data group controller to access layers. */
    private final DataGroupController myDataGroupController;

    /**
     * Constructor.
     *
     * @param actionRegistry The feature action registry.
     * @param dataGroupController The data group controller.
     */
    public FeatureActionStateController(FeatureActionsRegistry actionRegistry, DataGroupController dataGroupController)
    {
        myRegistry = actionRegistry;
        myDataGroupController = dataGroupController;
    }

    @Override
    public void activateState(String id, String description, Collection<? extends String> tags, Node node)
    {
        try
        {
            NodeList featureActionNodes = StateXML.getChildNodes(node, FeatureActionStateConstants.FEATURE_ACTION_PATH);
            for (int i = 0; i < featureActionNodes.getLength(); i++)
            {
                FeatureAction newFeatureAction = new FeatureAction();
                Node featureActionNode = featureActionNodes.item(i);
                try
                {
                    newFeatureAction = XMLUtilities.readXMLObject(featureActionNode, FeatureAction.class);
                }
                catch (JAXBException e)
                {
                    LOGGER.error(e.getMessage(), e);
                }
                String layerPath = StateXML.newXPath().evaluate(FeatureActionStateConstants.TYPE_PATH, featureActionNode);
                myRegistry.add(layerPath, Collections.singleton(newFeatureAction), this);
                addResource(id, new Pair<>(newFeatureAction, layerPath));
            }
        }
        catch (XPathExpressionException e)
        {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void activateState(String id, String description, Collection<? extends String> tags, StateType state)
            throws InterruptedException
    {
        FeatureActionArrayType featureActionList = StateUtilities.getFeatureActions(state);
        for (FeatureActionType featureActionType : featureActionList.getFeatureAction())
        {
            FeatureAction newFeatureAction = new FeatureAction();
            newFeatureAction.setName(featureActionType.getTitle() + " - " + id);
            newFeatureAction.setEnabled(featureActionType.isActive());
            newFeatureAction.setGroupName(featureActionType.getDescription());
            WFS100FilterToDataFilterConverter filterConverter = new WFS100FilterToDataFilterConverter();
            newFeatureAction.setFilter(filterConverter.apply(featureActionType.getFilter()));
            StyleAction style = new StyleAction();
            FeatureStyleActionType act = (FeatureStyleActionType)featureActionType.getActions().getAction().get(0).getValue();
            style.getStyleOptions().setColor(ColorUtilities.convertFromHexString(act.getColor(), 1, 2, 3, 0));
            style.getStyleOptions().setSize(act.getSize().intValue());
            newFeatureAction.getActions().add(style);
            myRegistry.add(featureActionType.getType(), Collections.singleton(newFeatureAction), this);
            addResource(id, new Pair<>(newFeatureAction, featureActionType.getType()));
        }
    }

    @Override
    public boolean canActivateState(Node node)
    {
        return StateXML.anyMatch(node, "/:state/:featureActions/:featureAction");
    }

    @Override
    public boolean canActivateState(StateType state)
    {
        return !StateUtilities.getFeatureActions(state).getFeatureAction().isEmpty();
    }

    @Override
    public boolean canSaveState()
    {
        Set<DataTypeInfo> dataTypeSet = getFeatureLayers();
        for (DataTypeInfo dataType : dataTypeSet)
        {
            if (!myRegistry.get(dataType.getTypeKey()).isEmpty())
            {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void deactivate(Pair<FeatureAction, String> resource)
    {
        myRegistry.remove(resource.getSecondObject(), Collections.singleton(resource.getFirstObject()), this);
    }

    @Override
    public boolean isSaveStateByDefault()
    {
        return true;
    }

    @Override
    public void saveState(Node node)
    {
        Document doc = node instanceof Document ? (Document)node : node.getOwnerDocument();
        Set<DataTypeInfo> dataTypeSet = getFeatureLayers();
        for (DataTypeInfo dataType : dataTypeSet)
        {
            List<FeatureAction> actionList = myRegistry.get(dataType.getTypeKey());
            for (FeatureAction currentAction : actionList)
            {
                try
                {
                    Node baseNode = StateXML.createChildNode(node, doc, node, FeatureActionStateConstants.BASE_PATH,
                            "featureActions");
                    Node featureActionNode = StateXML.createChildNode(baseNode, doc, baseNode,
                            FeatureActionStateConstants.FEATURE_ACTION_PATH, "featureAction");
                    XMLUtilities.marshalJAXBObjectToElement(currentAction, featureActionNode);
                }
                catch (XPathExpressionException | JAXBException e)
                {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void saveState(StateType state)
    {
        FeatureActionArrayType featureActionArrayType = StateUtilities.getFeatureActions(state);

        Set<DataTypeInfo> dataTypeSet = getFeatureLayers();
        for (DataTypeInfo dataType : dataTypeSet)
        {
            List<FeatureAction> actionList = myRegistry.get(dataType.getTypeKey());
            for (FeatureAction currentAction : actionList)
            {
                FeatureActionType featureActionType = new FeatureActionType();
                featureActionType.setActive(currentAction.isEnabled());
                featureActionType.setTitle(currentAction.getName());
                featureActionType.setDescription(currentAction.getGroupName());
                featureActionType.setType(dataType.getTypeKey());
                featureActionType.setTypeHint(FeatureActionTypeHint.FILTERABLE);

                // Filter
                Filter featureFilter = currentAction.getFilter();
                FilterType filterType = new FilterType();
                filterType.setActive(featureFilter.isActive());
                filterType.setTitle(featureFilter.getName());
                filterType.setDescription(featureFilter.getFilterDescription());
                filterType.setId(Integer.toHexString(filterType.hashCode()));
                filterType.setFilterType("single");
                filterType.setMatch(featureFilter.getMatch());
                filterType.setType(featureFilter.getTypeKey());
                try
                {
                    filterType.setLogicOps(FilterToWFS100Converter.convert(featureFilter));
                }
                catch (FilterException e)
                {
                    LOGGER.error("Unable to create an OGCFilter", e);
                }
                featureActionType.setFilter(filterType);

                // Actions
                ActionArrayType actionArray = featureActionType.getActions() == null
                        ? new ActionArrayType() : featureActionType.getActions();
                FeatureStyleActionType featureStyleAction = new FeatureStyleActionType();
                ObservableList<Action> actions = currentAction.getActions();
                if (actions.get(0) instanceof StyleAction)
                {
                    StyleOptions styleOptions = ((StyleAction)actions.get(0)).getStyleOptions();
                    featureStyleAction.setColor(ColorUtilities.convertToHexString(styleOptions.getColor(), 1, 2, 3, 0));
                    featureStyleAction.setSize(BigDecimal.valueOf(styleOptions.getSize()));
                    featureStyleAction.setShape(ShapeType.DEFAULT);
                    featureStyleAction.setCenterShape(ShapeType.POINT);
                }
                actionArray.getAction().add(new ObjectFactory().createFeatureStyleAction(featureStyleAction));
                featureActionType.setActions(actionArray);
                featureActionArrayType.getFeatureAction().add(featureActionType);
            }
        }
    }

    /**
     * Generates a list of layers that could potentially have feature actions.
     *
     * @return The list of layers.
     */
    private Set<DataTypeInfo> getFeatureLayers()
    {
        Set<DataTypeInfo> layerList = myDataGroupController.getActiveMembers(false);
        layerList.removeIf(e -> !(e.getSourcePrefix().equals("CSV") || e.getSourcePrefix().equals("Merged")
                    || e.getSourcePrefix().equals("Joined")));
        return layerList;
    }
}

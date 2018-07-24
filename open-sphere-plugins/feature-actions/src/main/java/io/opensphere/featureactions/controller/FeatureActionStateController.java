package io.opensphere.featureactions.controller;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.ActionArrayType;
import com.bitsys.fade.mist.state.v4.FeatureActionArrayType;
import com.bitsys.fade.mist.state.v4.FeatureActionType;
import com.bitsys.fade.mist.state.v4.FeatureActionTypeHint;
import com.bitsys.fade.mist.state.v4.FeatureStyleActionType;
import com.bitsys.fade.mist.state.v4.ObjectFactory;
import com.bitsys.fade.mist.state.v4.ShapeType;
import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.controlpanels.styles.model.StyleOptions;
import io.opensphere.core.export.ExportException;
import io.opensphere.core.modulestate.AbstractLayerStateController;
import io.opensphere.core.modulestate.ModuleStateController;
import io.opensphere.core.modulestate.StateUtilities;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.featureactions.model.Action;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.featureactions.model.StyleAction;
import io.opensphere.featureactions.registry.FeatureActionsRegistry;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.filterbuilder.impl.WFS100FilterToDataFilterConverter;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.mdfilter.CustomBinaryLogicOpType;
import io.opensphere.mantle.data.element.mdfilter.DataFilterExporter;
import io.opensphere.mantle.data.element.mdfilter.FilterException;
import io.opensphere.mantle.data.element.mdfilter.FilterToWFS100Converter;
import io.opensphere.mantle.data.element.mdfilter.FilterToWFS110Converter;
import javafx.collections.ObservableList;
import net.opengis.ogc._110.LogicOpsType;

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
     * Constructor for the FeatureActionsStateController.
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
        return true;
    }

    @Override
    public boolean canActivateState(StateType state)
    {
        return state.getFeatureActions().isSetFeatureAction();
    }

    @Override
    public boolean canSaveState()
    {
        return true;
    }

    @Override
    protected void deactivate(Pair<FeatureAction, String> resource)
    {
        myRegistry.remove(resource.getSecondObject(), Collections.singleton(resource.getFirstObject()), this);
    }

//    @Override
//    public void deactivateState(String id, Node node) throws InterruptedException
//    {
//    }

//    @Override
//    public void deactivateState(String id, StateType state) throws InterruptedException
//    {
//        FeatureActionArrayType featureActionArray = StateUtilities.getFeatureActions(state);
//        for (FeatureActionType featureActionType : featureActionArray.getFeatureAction())
//        {
//            List<FeatureAction> removeList = New.list();
//            for (FeatureAction fa : myRegistry.get(featureActionType.getType()))
//            {
//                if (fa.getName().equals(featureActionType.getTitle() + " - " + id))
//                {
//                    removeList.add(fa);
//                }
//            }
//            myRegistry.remove(featureActionType.getType(), removeList, this);
//        }
//    }

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
                    Node baseNode = StateXML.createChildNode(node, doc, node, "/" + ModuleStateController.STATE_QNAME + "/:featureActions",
                            "featureActions");
                    Node featureActionNode = StateXML.createChildNode(baseNode, doc, baseNode,
                            "/" + ModuleStateController.STATE_QNAME + "/:featureActions/:featureAction", "featureAction");
                    ((Element)featureActionNode).setAttribute("active", StringUtilities.toString(currentAction.isEnabled()));
                    ((Element)featureActionNode).setAttribute("title", currentAction.getName());
                    ((Element)featureActionNode).setAttribute("description", currentAction.getGroupName());
                    ((Element)featureActionNode).setAttribute("type", dataType.getTypeKey());
                    ((Element)featureActionNode).setAttribute("typeHint", FeatureActionTypeHint.FILTERABLE.toString());

                    // Filter
                    Filter featureFilter = currentAction.getFilter();
                    net.opengis.ogc._100t.FilterType filterType = new net.opengis.ogc._100t.FilterType();
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
                    XMLUtilities.marshalJAXBObjectToElement(filterType, featureActionNode);

                    //Actions
                    Node actionsNode = StateXML.createChildNode(featureActionNode, doc, featureActionNode,
                            "/" + ModuleStateController.STATE_QNAME + "/:featureActions/:featureAction/:actions", "actions");
                    Node featureStyleNode = StateXML.createChildNode(actionsNode, doc, actionsNode,
                            "/" + ModuleStateController.STATE_QNAME + "/:featureActions/:featureAction/:actions/:featureStyleAction",
                            "featureStyleAction");
                    ObservableList<Action> actions = currentAction.getActions();
                    if (actions.get(0) instanceof StyleAction)
                    {
                        StyleOptions styleOptions = ((StyleAction)actions.get(0)).getStyleOptions();
                        Node colorNode = createElement(featureStyleNode, "color");
                        colorNode.setTextContent((ColorUtilities.convertToHexString(styleOptions.getColor(), 1, 2, 3, 0)));
                        Node sizeNode = createElement(featureStyleNode, "size");
                        sizeNode.setTextContent(StringUtilities.toString(styleOptions.getSize()));
                        Node shapeNode = createElement(featureStyleNode, "shape");
                        shapeNode.setTextContent(ShapeType.DEFAULT.toString());
                        Node centerNode = createElement(featureStyleNode, "centerShape");
                        centerNode.setTextContent(ShapeType.POINT.toString());
                    }
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
                net.opengis.ogc._100t.FilterType filterType = new net.opengis.ogc._100t.FilterType();
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
        Set<DataTypeInfo> returnList  = New.set();
        for (DataTypeInfo typeInfo : layerList)
        {
            if (typeInfo.getTypeKey().startsWith("CSV"))
            {
                returnList.add(typeInfo);
            }
        }
        return returnList;
    }

    /**
     * Creates a new element attached to the specified parent node.
     *
     * @param parent The parent node for the element.
     * @param childName The name of the new element.
     * @return The new element.
     */
    private Node createElement(Node parent, String childName)
    {
        return parent.appendChild(XMLUtilities.getDocument(parent).createElement(childName));
    }
}

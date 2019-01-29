package io.opensphere.controlpanels.animation.state;

import java.util.Collection;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.StateType;
import com.bitsys.fade.mist.state.v4.TimeType;

import io.opensphere.controlpanels.animation.controller.AnimationController;
import io.opensphere.core.modulestate.AbstractModuleStateController;
import io.opensphere.core.modulestate.ModuleStateController;
import io.opensphere.core.modulestate.StateUtilities;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;

/**
 * Saves/Activates the live mode state of the timeline.
 */
public class LiveStateController extends AbstractModuleStateController
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(LiveStateController.class);

    /**
     * The animation controller.
     */
    private final AnimationController myController;

    /**
     * The map of previous live mode when deactivated.
     */
    private final Map<String, Boolean> myPreviousLive = New.map();

    /**
     * Constructs a new live state controller.
     *
     * @param controller The animation controller.
     */
    public LiveStateController(AnimationController controller)
    {
        myController = controller;
    }

    @Override
    public void activateState(String id, String description, Collection<? extends String> tags, Node node)
    {
        Node timeNode;
        try
        {
            timeNode = (Node)StateXML.newXPath().evaluate("/" + ModuleStateController.STATE_QNAME + "/:time", node,
                    XPathConstants.NODE);

            LiveState liveState = XMLUtilities.readXMLObject(timeNode, LiveState.class);
            myPreviousLive.put(id, myController.isLive());
            myController.setLive(liveState.isLive());
        }
        catch (XPathExpressionException | JAXBException e)
        {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void activateState(String id, String description, Collection<? extends String> tags, StateType state)
    {
        if (canActivateState(state))
        {
            myPreviousLive.put(id, myController.isLive());
            boolean live = Boolean.TRUE.equals(state.getTime().isLive());
            myController.setLive(live);
        }
    }

    @Override
    public boolean canActivateState(Node node)
    {
        boolean canActivate = false;
        try
        {
            canActivate = (Node)StateXML.newXPath().evaluate("/" + ModuleStateController.STATE_QNAME + "/:time", node,
                    XPathConstants.NODE) != null;
        }
        catch (XPathExpressionException e)
        {
            LOGGER.error(e.getMessage(), e);
        }

        return canActivate;
    }

    @Override
    public boolean canActivateState(StateType state)
    {
        return state.getTime() != null;
    }

    @Override
    public boolean canSaveState()
    {
        return true;
    }

    @Override
    public void deactivateState(String id, Node node)
    {
        boolean live = Boolean.TRUE.equals(myPreviousLive.remove(id));
        myController.setLive(live);
    }

    @Override
    public void deactivateState(String id, StateType state)
    {
        deactivateState(id, (Node)null);
    }

    @Override
    public boolean isSaveStateByDefault()
    {
        return false;
    }

    @Override
    public void saveState(Node node)
    {
        try
        {
            Node stateNode = (Node)StateXML.newXPath().evaluate("/" + ModuleStateController.STATE_QNAME, node,
                    XPathConstants.NODE);
            if (stateNode != null)
            {
                boolean isLive = Boolean.TRUE.equals(myController.isLive());
                LiveState state = new LiveState();
                state.setLive(isLive);

                XMLUtilities.marshalJAXBObjectToElement(state, stateNode);

                XMLUtilities.mergeDuplicateElements(stateNode.getOwnerDocument(), "time");
            }
        }
        catch (JAXBException | XPathExpressionException e)
        {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void saveState(StateType state)
    {
        TimeType time = StateUtilities.getTime(state);
        time.setLive(myController.isLive());
    }
}

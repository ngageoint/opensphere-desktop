package io.opensphere.mantle.data.util;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.element.event.DataElementAltitudeChangeEvent;
import io.opensphere.mantle.data.element.event.DataElementColorChangeEvent;
import io.opensphere.mantle.data.element.event.DataElementLOBVsibilityChangeEvent;
import io.opensphere.mantle.data.element.event.DataElementSelectionChangeEvent;
import io.opensphere.mantle.data.element.event.DataElementVisibilityChangeEvent;
import io.opensphere.mantle.data.element.event.consolidated.AbstractConsolidatedDataElementChangeEvent;
import io.opensphere.mantle.data.element.event.consolidators.DataElementAltitudeChangeConsolidator;
import io.opensphere.mantle.data.element.event.consolidators.DataElementColorChangeConsolidator;
import io.opensphere.mantle.data.element.event.consolidators.DataElementLOBVisibilityChangeConsolidator;
import io.opensphere.mantle.data.element.event.consolidators.DataElementSelectionChangeConsolidator;
import io.opensphere.mantle.data.element.event.consolidators.DataElementVisibilityChangeConsolidator;

/**
 * Adjusts a VisualizationState to match a given state.
 */
public class AdjustToMatchSelectedFields implements VisualizationStateAdjustmentVisitor
{
    /** The adjust altitude. */
    private final boolean myAdjustAltitude;

    /** The adjust color. */
    private final boolean myAdjustColor;

    /** The adjust lob visible. */
    private final boolean myAdjustLobVisible;

    /** The adjust selection. */
    private final boolean myAdjustSelection;

    /** The adjust visibility. */
    private final boolean myAdjustVisibility;

    /** The consolidator. */
    private final DataElementAltitudeChangeConsolidator myAltConsolidator;

    /** The color consolidator. */
    private final DataElementColorChangeConsolidator myColorConsolidator;

    /** The lob consolidator. */
    private final DataElementLOBVisibilityChangeConsolidator myLobConsolidator;

    // /** The adjust highlight. */
    // private final boolean myAdjustHighlight;

    /** The sel consolidator. */
    private final DataElementSelectionChangeConsolidator mySelConsolidator;

    /** The state to match. */
    private final VisualizationState myStateToMatch;

    /** The vis consolidator. */
    private final DataElementVisibilityChangeConsolidator myVisConsolidator;

    /**
     * Instantiates a new adjust to match all fields.
     *
     * @param stateToMatch the state to match
     */
    public AdjustToMatchSelectedFields(VisualizationState stateToMatch)
    {
        this(stateToMatch, true, true, true, true, true);
    }

    /**
     * Instantiates a new adjust to match.
     *
     * @param stateToMatch the state to match
     * @param matchVisibility the match visibility
     * @param matchSelection the match selection
     * @param matchLobVisible the match lob visible
     * @param matchColor the match color
     * @param matchAltitude the match altitude
     */
    public AdjustToMatchSelectedFields(VisualizationState stateToMatch, boolean matchVisibility, boolean matchSelection,
            boolean matchLobVisible, boolean matchColor, boolean matchAltitude)
    {
        myAltConsolidator = new DataElementAltitudeChangeConsolidator();
        myVisConsolidator = new DataElementVisibilityChangeConsolidator();
        mySelConsolidator = new DataElementSelectionChangeConsolidator();
        myLobConsolidator = new DataElementLOBVisibilityChangeConsolidator();
        myColorConsolidator = new DataElementColorChangeConsolidator();

        myStateToMatch = stateToMatch;
        myAdjustAltitude = matchAltitude;
        myAdjustSelection = matchSelection;
        myAdjustColor = matchColor;
        myAdjustLobVisible = matchLobVisible;
        myAdjustVisibility = matchVisibility;
        // myAdjustHighlight = matchHighlight;
    }

    @Override
    public void adjustState(VisualizationState stateToAdjust, long id, String dtKey, Object source)
    {
        if (!stateToAdjust.equals(myStateToMatch))
        {
            if (myAdjustVisibility && stateToAdjust.setVisible(myStateToMatch.isVisible()))
            {
                myVisConsolidator.addEvent(new DataElementVisibilityChangeEvent(id, dtKey, stateToAdjust.isVisible(), source));
            }

            if (myAdjustAltitude && stateToAdjust.setAltitudeAdjust(myStateToMatch.getAltitudeAdjust()))
            {
                myAltConsolidator
                        .addEvent(new DataElementAltitudeChangeEvent(id, dtKey, stateToAdjust.getAltitudeAdjust(), source));
            }

            if (myAdjustColor && stateToAdjust.setColor(myStateToMatch.getColor()))
            {
                myColorConsolidator.addEvent(new DataElementColorChangeEvent(id, dtKey, stateToAdjust.getColor(), source));
            }

            // if (myAdjustHighlight &&
            // stateToAdjust.setHighlighted(myStateToMatch.isHighlighted()))
            // {
            // eventsToReturn.add(new DataElementHighlightChangeEvent(id,
            // dtKey, stateToAdjust.isHighlighted(), source));
            // }

            if (myAdjustLobVisible && stateToAdjust.setLobVisible(myStateToMatch.isLobVisible()))
            {
                myLobConsolidator
                        .addEvent(new DataElementLOBVsibilityChangeEvent(id, dtKey, stateToAdjust.isLobVisible(), source));
            }

            if (myAdjustSelection && stateToAdjust.setVisible(myStateToMatch.isSelected()))
            {
                mySelConsolidator.addEvent(new DataElementSelectionChangeEvent(id, dtKey, stateToAdjust.isSelected(), source));
            }
        }
    }

    @Override
    public List<AbstractConsolidatedDataElementChangeEvent> getEvents()
    {
        List<AbstractConsolidatedDataElementChangeEvent> eventList = new ArrayList<>();

        if (myAdjustVisibility && myVisConsolidator.hadEvents())
        {
            eventList.add((AbstractConsolidatedDataElementChangeEvent)myVisConsolidator.createConsolidatedEvent());
        }

        if (myAdjustAltitude && myAltConsolidator.hadEvents())
        {
            eventList.add((AbstractConsolidatedDataElementChangeEvent)myAltConsolidator.createConsolidatedEvent());
        }

        if (myAdjustColor && myColorConsolidator.hadEvents())
        {
            eventList.add((AbstractConsolidatedDataElementChangeEvent)myColorConsolidator.createConsolidatedEvent());
        }

        if (myAdjustLobVisible && myLobConsolidator.hadEvents())
        {
            eventList.add((AbstractConsolidatedDataElementChangeEvent)myLobConsolidator.createConsolidatedEvent());
        }
        if (myAdjustSelection && mySelConsolidator.hadEvents())
        {
            eventList.add((AbstractConsolidatedDataElementChangeEvent)mySelConsolidator.createConsolidatedEvent());
        }
        return eventList;
    }

    @Override
    public boolean wereAdjustmentsMade()
    {
        return myVisConsolidator.hadEvents() || myAltConsolidator.hadEvents() || myColorConsolidator.hadEvents()
                || myLobConsolidator.hadEvents() || mySelConsolidator.hadEvents();
    }
}

package io.opensphere.subterrain.xraygoggles.model;

import static org.junit.Assert.assertEquals;

import java.util.Observer;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ScreenPosition;

/**
 * Unit test for {@link XrayGogglesModel}.
 */
public class XrayGogglesModelTest
{
    /**
     * Tests setting the geographic positions.
     */
    @Test
    public void testSetGeoPosition()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer observer = createObserver(support, XrayGogglesModel.GEO_POSITION);

        support.replayAll();

        GeographicPosition upperLeft = new GeographicPosition(LatLonAlt.createFromDegrees(11, 10));
        GeographicPosition upperRight = new GeographicPosition(LatLonAlt.createFromDegrees(11, 11));
        GeographicPosition lowerLeft = new GeographicPosition(LatLonAlt.createFromDegrees(10, 10));
        GeographicPosition lowerRight = new GeographicPosition(LatLonAlt.createFromDegrees(10, 11));
        GeographicPosition center = new GeographicPosition(LatLonAlt.createFromDegrees(10.5, 10.5));

        XrayGogglesModel model = new XrayGogglesModel();
        model.addObserver(observer);

        model.setGeoPosition(upperLeft, upperRight, lowerLeft, lowerRight, center);
        assertEquals(upperLeft, model.getUpperLeftGeo());
        assertEquals(upperRight, model.getUpperRightGeo());
        assertEquals(lowerLeft, model.getLowerLeftGeo());
        assertEquals(lowerRight, model.getLowerRightGeo());
        assertEquals(center, model.getCenterGeo());

        support.verifyAll();
    }

    /**
     * Tests setting the geographic positions.
     */
    @Test
    public void testClone()
    {
        GeographicPosition upperLeftGeo = new GeographicPosition(LatLonAlt.createFromDegrees(11, 10));
        GeographicPosition upperRightGeo = new GeographicPosition(LatLonAlt.createFromDegrees(11, 11));
        GeographicPosition lowerLeftGeo = new GeographicPosition(LatLonAlt.createFromDegrees(10, 10));
        GeographicPosition lowerRightGeo = new GeographicPosition(LatLonAlt.createFromDegrees(10, 11));
        GeographicPosition center = new GeographicPosition(LatLonAlt.createFromDegrees(10.5, 10.5));

        ScreenPosition upperLeft = new ScreenPosition(10, 11);
        ScreenPosition upperRight = new ScreenPosition(11, 11);
        ScreenPosition lowerLeft = new ScreenPosition(10, 10);
        ScreenPosition lowerRight = new ScreenPosition(11, 10);

        XrayGogglesModel model = new XrayGogglesModel();

        model.setGeoPosition(upperLeftGeo, upperRightGeo, lowerLeftGeo, lowerRightGeo, center);
        model.setScreenPosition(upperLeft, upperRight, lowerLeft, lowerRight);
        model = model.clone();

        assertEquals(upperLeftGeo.asVector2d(), model.getUpperLeftGeo().asVector2d());
        assertEquals(upperRightGeo.asVector2d(), model.getUpperRightGeo().asVector2d());
        assertEquals(lowerLeftGeo.asVector2d(), model.getLowerLeftGeo().asVector2d());
        assertEquals(lowerRightGeo.asVector2d(), model.getLowerRightGeo().asVector2d());
        assertEquals(center.asVector2d(), model.getCenterGeo().asVector2d());
        assertEquals(upperLeft.asVector2d(), model.getUpperLeft().asVector2d());
        assertEquals(upperRight.asVector2d(), model.getUpperRight().asVector2d());
        assertEquals(lowerLeft.asVector2d(), model.getLowerLeft().asVector2d());
        assertEquals(lowerRight.asVector2d(), model.getLowerRight().asVector2d());
    }

    /**
     * Tests setting the screen positions.
     */
    @Test
    public void testSetScreenPosition()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer observer = createObserver(support, XrayGogglesModel.SCREEN_POSITION);

        support.replayAll();

        ScreenPosition upperLeft = new ScreenPosition(10, 11);
        ScreenPosition upperRight = new ScreenPosition(11, 11);
        ScreenPosition lowerLeft = new ScreenPosition(10, 10);
        ScreenPosition lowerRight = new ScreenPosition(11, 10);

        XrayGogglesModel model = new XrayGogglesModel();
        model.addObserver(observer);

        model.setScreenPosition(upperLeft, upperRight, lowerLeft, lowerRight);
        assertEquals(upperLeft, model.getUpperLeft());
        assertEquals(upperRight, model.getUpperRight());
        assertEquals(lowerLeft, model.getLowerLeft());
        assertEquals(lowerRight, model.getLowerRight());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link Observer}.
     *
     * @param support Used to create the mock.
     * @param expectedProperty The expected property in the update event.
     * @return The mocked observer.
     */
    private Observer createObserver(EasyMockSupport support, String expectedProperty)
    {
        Observer observer = support.createMock(Observer.class);
        observer.update(EasyMock.isA(XrayGogglesModel.class), EasyMock.cmpEq(expectedProperty));

        return observer;
    }
}

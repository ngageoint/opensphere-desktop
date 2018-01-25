package io.opensphere.feedback;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import javax.swing.JMenuItem;

import org.junit.Before;
import org.junit.Test;

import io.opensphere.core.StatisticsManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.geometry.RenderingCapabilities;
import io.opensphere.core.orwell.ApplicationStatistics;
import io.opensphere.core.orwell.GraphicsStatistics;
import io.opensphere.core.orwell.SessionStatistics;
import io.opensphere.core.orwell.SystemStatistics;
import io.opensphere.core.orwell.UserStatistics;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.swing.SplitButton;

/**
 * Test class used to exercise the {@link FeedbackManager}.
 */
public class FeedbackManagerTest
{
    /**
     * The object on which tests are performed.
     */
    private FeedbackManager myTestObject;

    /**
     * Creates the resources needed to execute the tests.
     *
     * @throws java.lang.Exception if the resources cannot be initialized.
     */
    @Before
    public void setUp() throws Exception
    {
        myTestObject = new FeedbackManager();
    }

    /**
     * Test method to verify that there are no private methods in the
     * {@link FeedbackManager} class.
     */
    @Test
    public void testNonPrivateMethods()
    {
        Method[] declaredMethods = FeedbackManager.class.getDeclaredMethods();

        for (Method method : declaredMethods)
        {
            if (!method.getName().startsWith("$") && !method.getName().startsWith("lambda$"))
            {
                assertFalse(method.getName() + " is private. No private methods are permitted.",
                        Modifier.isPrivate(method.getModifiers()));
            }
        }
    }

    /**
     * Test method for
     * {@link FeedbackManager#addMenuItems(io.opensphere.core.Toolbox, io.opensphere.core.util.swing.SplitButton)}
     * .
     */
    @Test
    public void testAddMenuItems()
    {
        SplitButton button = new SplitButton(null, null);
        int initialComponentCount = button.getMenu().getComponentCount();
        Set<Component> initialComponents = new HashSet<>();
        initialComponents.addAll(Arrays.asList(button.getMenu().getComponents()));

        Toolbox mockToolbox = createStrictMock(Toolbox.class);
        PreferencesRegistry mockPreferencesRegistry = createStrictMock(PreferencesRegistry.class);
        Preferences mockPreferences = createStrictMock(Preferences.class);

        expect(mockToolbox.getPreferencesRegistry()).andReturn(mockPreferencesRegistry);
        expect(mockPreferencesRegistry.getPreferences(FeedbackManager.class)).andReturn(mockPreferences);

        String provideFeedback = "ProvideFeedback";
        expect(mockPreferences.getString(FeedbackManager.PROVIDE_FEEDBACK, null)).andReturn(provideFeedback);

        String reportBug = "ReportBug";
        expect(mockPreferences.getString(FeedbackManager.REPORT_BUG, null)).andReturn(reportBug);

        String requestFeature = "requestFeature";
        expect(mockPreferences.getString(FeedbackManager.REQUEST_FEATURE, null)).andReturn(requestFeature);

        replay(mockToolbox, mockPreferencesRegistry, mockPreferences);

        myTestObject.addMenuItems(mockToolbox, button);

        verify(mockToolbox, mockPreferencesRegistry, mockPreferences);

        assertEquals(3, button.getMenu().getComponentCount() - initialComponentCount);
        Component[] components = button.getMenu().getComponents();

        for (Component component : components)
        {
            if (!initialComponents.contains(component))
            {
                assertTrue(component instanceof JMenuItem);
            }
        }
    }

    /**
     * Test method for
     * {@link FeedbackManager#addMenuItems(io.opensphere.core.Toolbox, io.opensphere.core.util.swing.SplitButton)}
     * .
     */
    @Test
    public void testAddMenuItemsNoPrefs()
    {
        SplitButton button = new SplitButton(null, null);
        int initialComponentCount = button.getMenu().getComponentCount();

        Toolbox mockToolbox = createStrictMock(Toolbox.class);
        PreferencesRegistry mockPreferencesRegistry = createStrictMock(PreferencesRegistry.class);
        Preferences mockPreferences = createStrictMock(Preferences.class);

        expect(mockToolbox.getPreferencesRegistry()).andReturn(mockPreferencesRegistry);
        expect(mockPreferencesRegistry.getPreferences(FeedbackManager.class)).andReturn(mockPreferences);

        expect(mockPreferences.getString(FeedbackManager.PROVIDE_FEEDBACK, null)).andReturn(null);
        expect(mockPreferences.getString(FeedbackManager.REPORT_BUG, null)).andReturn(null);
        expect(mockPreferences.getString(FeedbackManager.REQUEST_FEATURE, null)).andReturn(null);

        replay(mockToolbox, mockPreferencesRegistry, mockPreferences);

        myTestObject.addMenuItems(mockToolbox, button);

        verify(mockToolbox, mockPreferencesRegistry, mockPreferences);

        assertEquals(initialComponentCount, button.getMenu().getComponentCount());
    }

    /**
     * Test method for {@link FeedbackManager#openURL(String)} and
     * {@link FeedbackManager#transmit(String)} .
     *
     * @throws MalformedURLException if the test cannot be performed.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testOpenURL() throws MalformedURLException
    {
        SplitButton button = new SplitButton(null, null);

        Toolbox mockToolbox = createStrictMock(Toolbox.class);
        GeometryRegistry mockGeometryRegistry = createStrictMock(GeometryRegistry.class);
        RenderingCapabilities mockRenderingCapabilities = createStrictMock(RenderingCapabilities.class);
        PreferencesRegistry mockPreferencesRegistry = createStrictMock(PreferencesRegistry.class);
        Preferences mockPreferences = createStrictMock(Preferences.class);
        UIRegistry mockUIRegistry = createStrictMock(UIRegistry.class);
        Supplier mockSupplier = createStrictMock(Supplier.class);
        StatisticsManager mockStatisticsManager = createStrictMock(StatisticsManager.class);

        SystemStatistics systemStatistics = new SystemStatistics();
        GraphicsStatistics graphicsStatistics = new GraphicsStatistics();
        ApplicationStatistics applicationStatistics = new ApplicationStatistics();
        SessionStatistics sessionStatistics = new SessionStatistics();
        UserStatistics userStatistics = new UserStatistics();

        expect(mockStatisticsManager.getSystemStatistics()).andReturn(systemStatistics).anyTimes();
        expect(mockStatisticsManager.getGraphicsStatistics()).andReturn(graphicsStatistics).anyTimes();
        expect(mockStatisticsManager.getApplicationStatistics()).andReturn(applicationStatistics).anyTimes();
        expect(mockStatisticsManager.getSessionStatistics()).andReturn(sessionStatistics).anyTimes();
        expect(mockStatisticsManager.getUserStatistics()).andReturn(userStatistics).anyTimes();

        expect(mockToolbox.getPreferencesRegistry()).andReturn(mockPreferencesRegistry);
        expect(mockPreferencesRegistry.getPreferences(FeedbackManager.class)).andReturn(mockPreferences);

        expect(mockPreferences.getString(FeedbackManager.PROVIDE_FEEDBACK, null)).andReturn(null);
        expect(mockPreferences.getString(FeedbackManager.REPORT_BUG, null)).andReturn(null);
        expect(mockPreferences.getString(FeedbackManager.REQUEST_FEATURE, null)).andReturn(null);

        expect(mockUIRegistry.getMainFrameProvider()).andReturn(mockSupplier);
        expect(mockSupplier.get()).andReturn(null);
        expect(mockToolbox.getStatisticsManager()).andReturn(mockStatisticsManager).anyTimes();
        expect(mockToolbox.getUIRegistry()).andReturn(mockUIRegistry);

        replay(mockToolbox, mockUIRegistry, mockSupplier, mockPreferencesRegistry, mockPreferences, mockGeometryRegistry,
                mockRenderingCapabilities, mockStatisticsManager);

        myTestObject.addMenuItems(mockToolbox, button);

        myTestObject.transmit("http://127.0.0.1?test=data");

        verify(mockToolbox, mockUIRegistry, mockSupplier, mockPreferencesRegistry, mockPreferences, mockGeometryRegistry,
                mockRenderingCapabilities);
    }
}

package io.opensphere.controlpanels.layers.availabledata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.swing.ImageIcon;

import org.easymock.EasyMock;
import org.junit.Test;

import io.opensphere.core.util.swing.QuadStateIconButton;
import io.opensphere.core.util.swing.tree.ButtonModelPayload;
import io.opensphere.core.util.swing.tree.TreeTableTreeNode;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.impl.GroupByNodeUserObject;

/**
 * Tests the {@link RefreshTreeCellRenderer}.
 */
public class RefreshTreeCellRendererTest
{
    /**
     * Tests the update method.
     */
    @Test
    public void testUpdate()
    {
        GroupByNodeUserObject payload = new GroupByNodeUserObject(new RefreshableGroupMock());
        ButtonModelPayload buttonPayload = new ButtonModelPayload(payload, "", "");
        TreeTableTreeNode node = new TreeTableTreeNode(null, buttonPayload);

        RefreshTreeCellRenderer renderer = new RefreshTreeCellRenderer();
        QuadStateIconButton button = new QuadStateIconButton((ImageIcon)null, (ImageIcon)null, (ImageIcon)null, (ImageIcon)null);
        renderer.update(button, node);

        assertFalse(button.isHidden());
    }

    /**
     * Tests the update with a normal data group.
     */
    @Test
    public void testUpdateNonRefreshableGroup()
    {
        GroupByNodeUserObject payload = new GroupByNodeUserObject(EasyMock.createMock(DataGroupInfo.class));
        ButtonModelPayload buttonPayload = new ButtonModelPayload(payload, "", "");
        TreeTableTreeNode node = new TreeTableTreeNode(null, buttonPayload);

        RefreshTreeCellRenderer renderer = new RefreshTreeCellRenderer();
        QuadStateIconButton button = new QuadStateIconButton((ImageIcon)null, (ImageIcon)null, (ImageIcon)null, (ImageIcon)null);
        renderer.update(button, node);

        assertTrue(button.isHidden());
    }

    /**
     * Tests the update method with no payload.
     */
    @Test
    public void testUpdateNoPayload()
    {
        ButtonModelPayload buttonPayload = new ButtonModelPayload(null, "", "");
        TreeTableTreeNode node = new TreeTableTreeNode(null, buttonPayload);

        RefreshTreeCellRenderer renderer = new RefreshTreeCellRenderer();
        QuadStateIconButton button = new QuadStateIconButton((ImageIcon)null, (ImageIcon)null, (ImageIcon)null, (ImageIcon)null);
        renderer.update(button, node);

        assertTrue(button.isHidden());
    }

    /**
     * Tests the update with a null data group.
     */
    @Test
    public void testUpdateNullGroup()
    {
        GroupByNodeUserObject payload = new GroupByNodeUserObject("label");
        ButtonModelPayload buttonPayload = new ButtonModelPayload(payload, "", "");
        TreeTableTreeNode node = new TreeTableTreeNode(null, buttonPayload);

        RefreshTreeCellRenderer renderer = new RefreshTreeCellRenderer();
        QuadStateIconButton button = new QuadStateIconButton((ImageIcon)null, (ImageIcon)null, (ImageIcon)null, (ImageIcon)null);
        renderer.update(button, node);

        assertTrue(button.isHidden());
    }

    /**
     * Tests the update method with null payload.
     */
    @Test
    public void testUpdatePayloadNull()
    {
        TreeTableTreeNode node = new TreeTableTreeNode(null, null);

        RefreshTreeCellRenderer renderer = new RefreshTreeCellRenderer();
        QuadStateIconButton button = new QuadStateIconButton((ImageIcon)null, (ImageIcon)null, (ImageIcon)null, (ImageIcon)null);
        renderer.update(button, node);

        assertTrue(button.isHidden());
    }
}

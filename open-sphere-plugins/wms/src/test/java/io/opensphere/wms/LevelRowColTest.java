package io.opensphere.wms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test for {@link LevelRowCol}.
 */
public class LevelRowColTest
{
    /** Level value for testing. */
    private static final int LEVEL = 1;

    /** Row value for testing. */
    private static final int ROW = 4;

    /** Column value for testing. */
    private static final int COL = 26;

    /** An instance for testing. */
    private static final LevelRowCol LRC = new LevelRowCol(LEVEL, ROW, COL);

    /** Test for {@link LevelRowCol#equals(Object)}. */
    @Test
    public void testEqualsObject()
    {
        assertTrue(new LevelRowCol(4, 5, 6).equals(new LevelRowCol(4, 5, 6)));
        assertFalse(new LevelRowCol(4, 5, 6).equals(new LevelRowCol(3, 5, 6)));
        assertFalse(new LevelRowCol(4, 5, 6).equals(new LevelRowCol(4, 4, 6)));
        assertFalse(new LevelRowCol(4, 5, 6).equals(new LevelRowCol(4, 5, 7)));
    }

    /** Test for {@link LevelRowCol#getCol()}.. */
    @Test
    public void testGetCol()
    {
        assertEquals(COL, LRC.getCol());
    }

    /** Test for {@link LevelRowCol#getLevel()}. */
    @Test
    public void testGetLevel()
    {
        assertEquals(LEVEL, LRC.getLevel());
    }

    /** Test for {@link LevelRowCol#getRow()}. */
    @Test
    public void testGetRow()
    {
        assertEquals(ROW, LRC.getRow());
    }

    /** Test for {@link LevelRowCol#hashCode()}. */
    @Test
    public void testHashCode()
    {
        assertTrue(new LevelRowCol(4, 5, 6).hashCode() == new LevelRowCol(4, 5, 6).hashCode());
        assertFalse(new LevelRowCol(4, 5, 6).hashCode() == new LevelRowCol(3, 5, 6).hashCode());
        assertFalse(new LevelRowCol(4, 5, 6).hashCode() == new LevelRowCol(4, 4, 6).hashCode());
        assertFalse(new LevelRowCol(4, 5, 6).hashCode() == new LevelRowCol(4, 5, 7).hashCode());
    }

    /** Test for {@link LevelRowCol#toString()}. */
    @Test
    public void testToString()
    {
        assertFalse(LRC.toString().isEmpty());
    }
}

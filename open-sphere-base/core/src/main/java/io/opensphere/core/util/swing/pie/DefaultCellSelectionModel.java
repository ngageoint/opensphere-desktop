package io.opensphere.core.util.swing.pie;

import java.awt.Point;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.StrongChangeSupport;
import io.opensphere.core.util.collections.New;

/**
 * The default CellSelectionModel.
 */
@SuppressWarnings("PMD.GodClass")
public class DefaultCellSelectionModel implements CellSelectionModel
{
    /** The slice count. */
    private int mySliceCount;

    /** The ring count. */
    private int myRingCount;

    /** The selected cells. */
    private final Set<Point> mySelectedCells = New.set();

    /** The selection mode. */
    private SelectionMode mySelectionMode = SelectionMode.CELL;

    /** The anchor cell. */
    private Point myAnchorCell;

    /** The mouse-over cell. */
    private Point myMouseOverCell;

    /**
     * The previous final cell.
     */
    private Point myPreviousFinalCell;

    /** The change support. */
    private final transient ChangeSupport<ChangeListener> myChangeSupport = new StrongChangeSupport<>();

    /**
     * Gets all the cells in the bounding box of the given cells.
     *
     * @param cells the cells
     * @return All the cells in the bounding box
     */
    private static Collection<Point> getAllCells(Collection<Point> cells)
    {
        // Get the bounding box
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Point cell : cells)
        {
            if (cell.x < minX)
            {
                minX = cell.x;
            }
            if (cell.x > maxX)
            {
                maxX = cell.x;
            }
            if (cell.y < minY)
            {
                minY = cell.y;
            }
            if (cell.y > maxY)
            {
                maxY = cell.y;
            }
        }

        // Create the list of all cells in the bounding box
        int xCount = maxX - minX + 1;
        int yCount = maxY - minY + 1;
        Collection<Point> allCells = New.list(xCount * yCount);
        for (int y = minY; y <= maxY; y++)
        {
            for (int x = minX; x <= maxX; x++)
            {
                allCells.add(new Point(x, y));
            }
        }
        return allCells;
    }

    /**
     * Constructor.
     *
     * @param sliceCount the slice count
     * @param ringCount the ring count
     */
    public DefaultCellSelectionModel(int sliceCount, int ringCount)
    {
        super();
        mySliceCount = sliceCount;
        myRingCount = ringCount;
    }

    @Override
    public void addChangeListener(ChangeListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public void addSelectedCell(Point cell, Object source)
    {
        mySelectedCells.add(cell);
        fireStateChanged(ChangeType.SELECTION, source);
    }

    @Override
    public Point getMouseOverCell()
    {
        return myMouseOverCell;
    }

    @Override
    public Collection<Point> getSelectedCells()
    {
        Collection<Point> selectedCells;
        if (mySelectionMode == SelectionMode.CELL)
        {
            selectedCells = New.list(mySelectedCells);
        }
        else if (mySelectionMode == SelectionMode.SLICE)
        {
            TIntSet selectedSlices = new TIntHashSet();
            for (Point selectedCell : mySelectedCells)
            {
                selectedSlices.add(selectedCell.x);
            }
            TIntList sortedSlices = new TIntArrayList(selectedSlices);
            sortedSlices.sort();

            selectedCells = New.list(sortedSlices.size() * myRingCount);
            for (TIntIterator iter = sortedSlices.iterator(); iter.hasNext();)
            {
                int slice = iter.next();
                for (int ring = 0; ring < myRingCount; ring++)
                {
                    selectedCells.add(new Point(slice, ring));
                }
            }
        }
        else
        {
            TIntSet selectedRings = new TIntHashSet();
            for (Point selectedCell : mySelectedCells)
            {
                selectedRings.add(selectedCell.y);
            }
            TIntList sortedRings = new TIntArrayList(selectedRings);
            sortedRings.sort();

            selectedCells = New.list(sortedRings.size() * mySliceCount);
            for (TIntIterator iter = sortedRings.iterator(); iter.hasNext();)
            {
                int ring = iter.next();
                for (int slice = 0; slice < mySliceCount; slice++)
                {
                    selectedCells.add(new Point(slice, ring));
                }
            }
        }
        return selectedCells;
    }

    @Override
    public boolean isCellSelected(Point cell)
    {
        boolean isCellSelected = false;
        if (mySelectionMode == SelectionMode.CELL)
        {
            isCellSelected = mySelectedCells.contains(cell);
        }
        else if (mySelectionMode == SelectionMode.SLICE)
        {
            for (Point selectedCell : mySelectedCells)
            {
                if (selectedCell.x == cell.x)
                {
                    isCellSelected = true;
                    break;
                }
            }
        }
        else
        {
            for (Point selectedCell : mySelectedCells)
            {
                if (selectedCell.y == cell.y)
                {
                    isCellSelected = true;
                    break;
                }
            }
        }
        return isCellSelected;
    }

    @Override
    public boolean isMouseOverCell(Point cell)
    {
        boolean isMouseOverCell = false;
        if (myMouseOverCell != null)
        {
            if (mySelectionMode == SelectionMode.CELL)
            {
                isMouseOverCell = cell.equals(myMouseOverCell);
            }
            else if (mySelectionMode == SelectionMode.SLICE)
            {
                isMouseOverCell = myMouseOverCell.x == cell.x;
            }
            else
            {
                isMouseOverCell = myMouseOverCell.y == cell.y;
            }
        }
        return isMouseOverCell;
    }

    @Override
    public void removeChangeListener(ChangeListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    @Override
    public void removeSelectedCell(Point cell, Object source)
    {
        mySelectedCells.remove(cell);
        fireStateChanged(ChangeType.SELECTION, source);
    }

    @Override
    public void setAnchorCell(Point cell)
    {
        myAnchorCell = cell;
        myPreviousFinalCell = null;
    }

    @Override
    public void setFinalCell(Point cell, boolean keepSelected, Object source)
    {
        if (myAnchorCell != null)
        {
            Collection<Point> allCells = getAllCells(New.list(myAnchorCell, cell));

            // Unselect everything in the previous bounding box.
            if (keepSelected)
            {
                if (myPreviousFinalCell == null)
                {
                    myPreviousFinalCell = cell;
                }

                List<Point> previousBoundingBox = New.list(myAnchorCell, myPreviousFinalCell);
                Collection<Point> previousPoints = getAllCells(previousBoundingBox);
                mySelectedCells.removeAll(previousPoints);

                allCells.addAll(mySelectedCells);
            }

            myPreviousFinalCell = cell;
            setSelectedCells(allCells, source);
        }
        else
        {
            if (!keepSelected)
            {
                setSelectedCell(cell, source);
            }
            else
            {
                addSelectedCell(cell, source);
            }
        }
    }

    @Override
    public void setMouseOverCell(Point cell, Object source)
    {
        if (!Objects.equals(myMouseOverCell, cell))
        {
            myMouseOverCell = cell;
            fireStateChanged(ChangeType.MOUSE_OVER, source);
        }
    }

    @Override
    public void setSelectedCell(Point cell, Object source)
    {
        mySelectedCells.clear();
        mySelectedCells.add(cell);
        fireStateChanged(ChangeType.SELECTION, source);
    }

    @Override
    public void setSelectedCells(Collection<? extends Point> cells, Object source)
    {
        mySelectedCells.clear();
        mySelectedCells.addAll(cells);
        fireStateChanged(ChangeType.SELECTION, source);
    }

    @Override
    public void setSelectionMode(SelectionMode selectionMode, Object source)
    {
        mySelectionMode = selectionMode;
        fireStateChanged(ChangeType.SELECTION, source);
    }

    /**
     * Updates the slice and ring counts, and clears the model.
     *
     * @param sliceCount the slice count
     * @param ringCount the ring count
     */
    public void updateCounts(int sliceCount, int ringCount)
    {
        mySliceCount = sliceCount;
        myRingCount = ringCount;
        mySelectedCells.clear();
        myAnchorCell = null;
        myMouseOverCell = null;
        // Fire event here?
    }

    /**
     * Fires a state change.
     *
     * @param changeType the change type
     * @param source the source of the change
     */
    protected void fireStateChanged(final ChangeType changeType, final Object source)
    {
        myChangeSupport.notifyListeners(new Callback<ChangeListener>()
        {
            @Override
            public void notify(ChangeListener listener)
            {
                listener.stateChanged(changeType, source);
            }
        });
    }
}

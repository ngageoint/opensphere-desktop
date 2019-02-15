package io.opensphere.featureactions.editor.ui;

import java.util.List;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.editor.model.SimpleFeatureActionGroup;
import io.opensphere.featureactions.editor.model.SimpleFeatureActions;

/**
 * Handles the dragging and dropping of items from list views to other list
 * views.
 */
public class DragDropHandler
{
    /**
     * The index of the item being dragged.
     */
    private static int myDragIndex = -1;

    /**
     * The row being dragged.
     */
    private SimpleFeatureAction myDragAction;

    /**
     * The group being dragged.
     */
    private SimpleFeatureActionGroup myDragGroup;

    private ListCell<SimpleFeatureAction> myDropTarget;
    
    /**
     * The list the drag item is coming from.
     */
    private List<SimpleFeatureAction> myListSource;

    /**
     * The main accordion.
     */
    private final SimpleFeatureActions myMainModel;

    /**
     * Constructs a new drag and drop handler.
     *
     * @param mainModel The main model containing all groups and actions.
     */
    public DragDropHandler(SimpleFeatureActions mainModel)
    {
        myMainModel = mainModel;
    }

    /**
     * Starts listening to drag and drop of the passed in cell.
     *
     * @param group The group this cell belongs too.
     * @param cell The cell to potentially handle drag and drop for.
     */
    public void handleNewCell(SimpleFeatureActionGroup group, ListCell<SimpleFeatureAction> cell)
    {
        cell.setOnDragDetected(event ->
        {
            if (!cell.isEmpty())
            {
                Dragboard db = cell.startDragAndDrop(TransferMode.COPY_OR_MOVE);
                ClipboardContent cc = new ClipboardContent();
                cc.putString(cell.getItem().toString());
                db.setContent(cc);
                myDragIndex = cell.getIndex();
                myListSource = group.getActions();
                myDragAction = cell.getItem();
            }
        });

        cell.setOnDragOver(event ->
        {
            Dragboard db = event.getDragboard();
            if (db.hasString())
            {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                myDropTarget = cell;
            }
        });

        cell.setOnDragDropped(event ->
        {
            Dragboard db = event.getDragboard();
            if (db.hasString() && myDragAction != null)
            {
                SimpleFeatureAction action = myDragAction;
                SimpleFeatureAction cellAction = cell.getItem();
                
                group.getActions().remove(myDragIndex);
                
                if (cellAction == null)
                {
                	group.getActions().add(action);
                }
                else
                {
                	int featureIndex = group.getActions().indexOf(cellAction);
                	featureIndex += featureIndex < myDragIndex ? 0 : 1;

	                group.getActions().add(featureIndex, action);
                }
                
                event.setDropCompleted(true);
                
                myDragAction = null;
                myDropTarget = null;
            }
            else
            {
                event.setDropCompleted(false);
            }
        });
    }
    
//    public void handleListView(SimpleFeatureActionGroup group, ListView<SimpleFeatureAction> view)
//    {
//    	view.setOnDragDropped(event ->
//    	{
//    		Dragboard db = event.getDragboard();
//    		if (db.hasString() && myDragAction != null)
//    		{
//    			SimpleFeatureAction action = myDragAction;
//                group.getActions().remove(myDragIndex);
//                if (myDropTarget != null)
//                {
//                	group.getActions().add(myDropTarget.getIndex()+1, action);
//                }
//                else
//                {                	
//                	group.getActions().add(action);
//                }
//                event.setDropCompleted(true);
//                myDragAction = null;
//                myDropTarget = null;
//    		}
//    		else
//    		{
//    			event.setDropCompleted(false);
//    		}
//    	});
//    }

    /**
     * Handles dragging and dropping actions to other groups.
     *
     * @param pane The titled pane representing a feature action group.
     * @param group The group the pane represents.
     */
    public void handlePane(FeatureActionTitledPane pane, SimpleFeatureActionGroup group)
    {
        pane.setOnDragDetected(event ->
        {
            Dragboard db = pane.startDragAndDrop(TransferMode.COPY_OR_MOVE);
            ClipboardContent cc = new ClipboardContent();
            cc.putString(pane.toString());
            db.setContent(cc);
            myDragIndex = myMainModel.getFeatureGroups().indexOf(group);
            myListSource = group.getActions();
            myDragGroup = group;
        });

        pane.setOnDragOver(event ->
        {
            Dragboard db = event.getDragboard();
            if (db.hasString())
            {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
        });

        pane.setOnDragDropped(event ->
        {
            Dragboard db = event.getDragboard();
            if (db.hasString())
            {
                if (myDragAction != null)
                {
                    SimpleFeatureAction action = myDragAction;
                    myListSource.remove(myDragIndex);
                    group.getActions().add(action);
                    event.setDropCompleted(true);
                    myDragAction = null;
                    myDropTarget = null;
                }
                else if (myDragGroup != null)
                {
                    int newIndex = myMainModel.getFeatureGroups().indexOf(group);
                    myMainModel.getFeatureGroups().remove(myDragIndex);
                    myMainModel.getFeatureGroups().add(newIndex, myDragGroup);
                    event.setDropCompleted(true);
                    myDragGroup = null;
                }
            }
            else
            {
                event.setDropCompleted(false);
            }
        });
    }
}

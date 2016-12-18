package table.attributeValues;

import com.sun.javafx.scene.control.skin.VirtualFlow;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.DragEvent;

@SuppressWarnings("restriction")
public class UNUSED_AutoscrollTreeView<T> extends TreeView<T>
{
    private enum ScrollDirection
    {
        None, Top, Bottom
    }
     private static final double _milliSecondToSecondFactor = 1.0d / 1000.0d;

     // ---------------------------------------------------------------------------------------------
     /**
     * the time interval in milliseconds in which the scroll is performed
     */
    private final LongProperty _checkInterval = new SimpleLongProperty(50);
    /**
     * the actual scroll speed when being in the scroll areas
     */
    private final DoubleProperty _scrollSpeed = new SimpleDoubleProperty(1.0);
    /**
     * the scroll speed increment per second the user remain in the scroll area
     */
    private final DoubleProperty _scrollSpeedIncrementPerSecond = new SimpleDoubleProperty(0.0);
    /**
     * distance from the top, which defines the area which will start a scroll in the -y axis
     */
    private final DoubleProperty _dragIdentifierTop = new SimpleDoubleProperty();
    /**
     * distance from the bottom, which defines the area which will start a scroll in the +y axis
     */
    private final DoubleProperty _dragIdentifierBottom = new SimpleDoubleProperty();
    /**
     * time at which the user entered the any scroll area
     */
    private long _initialDragTime = -1;
    /**
     * last time the interval was checked
     */
    private long _lastCheck = -1;

    // ---------------------------------------------------------------------------------------------
   //  Constructors

    public UNUSED_AutoscrollTreeView()
    {
        super();
        addEventHandlers();
    }

    public UNUSED_AutoscrollTreeView(TreeItem<T> root)
    {
        super(root);
        addEventHandlers();
    }
   // ---------------------------------------------------------------------------------------------
     public final void setCheckInterval(long value)    {        _checkInterval.set(value);    }
    public final long getCheckInterval()    {        return _checkInterval.get();    }
    public final LongProperty checkIntervalProperty()    {        return _checkInterval;    }
 
    public final void setScrollSpeed(double value)    {        _scrollSpeed.set(value);    }
    public final double getScrollSpeed()    {        return _scrollSpeed.get();    }
    public final DoubleProperty scrollSpeedProperty()    {        return _scrollSpeed;    }
 
    public final void setScrollSpeedIncrementPerSecond(double value)    {        _scrollSpeedIncrementPerSecond.set(value);    }
    public final double getScrollSpeedIncrementPerSecond()    {        return _scrollSpeedIncrementPerSecond.get();    }
    public final DoubleProperty scrollSpeedIncrementPerSecondProperty()    {        return _scrollSpeedIncrementPerSecond;    }

    public final void setDragIdentiferTop(double value)    {        _dragIdentifierTop.set(value);    }
    public final double getDragIdentifierTop()    {        return _dragIdentifierTop.get();    }
    public final DoubleProperty dragIdentifierTopProperty()    {        return _dragIdentifierTop;    }

    public final void setDragIdentiferBottom(double value)    {        _dragIdentifierBottom.set(value);    }
    public final double getDragIdentifierBottom()    {        return _dragIdentifierBottom.get();    }
    public final DoubleProperty dragIdentifierBottomProperty()    {        return _dragIdentifierBottom;    }

    // ---------------------------------------------------------------------------------------------
    //  Events

    private void onDragEvent(DragEvent event)
    {
        // -----only apply when there is a drag event in progress
        if(event.getEventType().equals(DragEvent.DRAG_OVER))
        {
            if(_lastCheck == -1 || System.currentTimeMillis() - _lastCheck > _checkInterval.get())
            {
                ScrollDirection direction = ScrollDirection.None;
                if(event.getY() <= _dragIdentifierTop.get())
                    direction = ScrollDirection.Top;
                else if(event.getY() >= getHeight() - _dragIdentifierBottom.get())
                    direction = ScrollDirection.Bottom;

                if(direction != ScrollDirection.None)
                {
                    double additionalScrollSpeed = 0;
                    if(_initialDragTime > 0)
                        additionalScrollSpeed = _scrollSpeedIncrementPerSecond.get() * (System.currentTimeMillis() - _initialDragTime) * _milliSecondToSecondFactor;
                    else
                        _initialDragTime = System.currentTimeMillis();

                    if(direction == ScrollDirection.Bottom)
                        scrollY(_scrollSpeed.get() + additionalScrollSpeed);
                    else
                        scrollY(-(_scrollSpeed.get() + additionalScrollSpeed));
                }
                else   _initialDragTime = -1;

                _lastCheck = System.currentTimeMillis();
            }
        }
        else
        {
            _initialDragTime = -1;
            _lastCheck = -1;
        }
    }

    // ---------------------------------------------------------------------------------------------
    //  Private
    /**
     * adds the necessary event filters
     */
    private void addEventHandlers()
    {
        addEventHandler(DragEvent.DRAG_OVER, event -> onDragEvent(event));
        addEventHandler(DragEvent.DRAG_EXITED, event -> onDragEvent(event));
        addEventHandler(DragEvent.DRAG_DROPPED, event -> onDragEvent(event));
        addEventHandler(DragEvent.DRAG_DONE, event -> onDragEvent(event));
//        setOnDragDetected(event -> startDrag(event)); 
    }

//private void startDrag(MouseEvent e)
//{
//	Dragboard db = startDragAndDrop(TransferMode.COPY);
//	ClipboardContent content = new ClipboardContent();
//	int idx = getSelectionModel().getSelectedIndex();
//	String av = getTreeItem(idx).getValue().toString();
//	content.put(new DataFormat("TreeTableItem"), av);
//	db.setContent(content);
//	e.consume();
//
//}
    private void scrollY(double offset)
    {
        VirtualFlow<?> flow = ((VirtualFlow<?>) lookup("VirtualFlow"));
        flow.adjustPixels(offset);
    }

}
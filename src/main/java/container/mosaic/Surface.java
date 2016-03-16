package container.mosaic;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;


public interface Surface<T> {
	public void cursorLeft();//37
	public void cursorUp();//38
	public void cursorRight();//39
	public void cursorDown();//40
	public String getCursor();
	public void setCursor(String id);
	
	/**
	 * Returns a flag indicating that a recent Move/Drop operation
	 * produced the necessary conditions for a valid drop. This is
	 * in turn queried by the {@link MosaicEngine} to issue commands
	 * relevant to commiting a drop.
	 * 
	 * @return	true if valid drop exists, false if not.
	 */
	public boolean hasValidDrop();
	
	/**
	 * Sets the flag indicating whether a recent Move/Drop operation
	 * produced a valid drop condition.
	 * 
	 * @param b		the flag to set.
	 */
	public void setHasValidDrop(boolean b);
	
	/**
	 * Adds an implementation of the {@link RelativePosition} interface. 
	 * Only to be used with the serialization mechanism
	 * 
	 * @param relativePosition	an implementation of the {@link RelativePosition} interface.
	 * @return	this {@code Surface}
	 */
	public Surface<T> add(String id, T t);
	
	/**
	 * Removes the Object <T> from this Surface.
	 * 
	 * @param t	the Object to remove
	 */
	public void requestRemove(T t);
	
	/**
	 * Removes the Object that was added with the specified id, from this Surface.
	 * <p>
	 * The {@link MosaicEngine} will respond to this request via the {@link SurfaceListener}
	 * interface's methods to instruct the client as to what exact changes need to be made,
	 * this allows the client to be totally passive with regard to necessary changes to affect
	 * the change in its layout.
	 * 
	 * @param id	the id of the Object to remove.
	 */
	public void requestRemove(String id);
	
	/**
	 * Begins an "Add" operation which will be "answered" as a response
	 * through the {@link SurfaceListener} interface. 
	 * <p>
	 * The {@link MosaicEngine} will respond to this request via the {@link SurfaceListener}
	 * interface's methods to instruct the client as to what exact changes need to be made,
	 * this allows the client to be totally passive with regard to necessary changes to affect
	 * the change in its layout.
	 */
	public void requestAdd(T source, String sourceID, T target, Position p);
	
	/**
	 * Moves the specified source object to the specified {@link Position} relative to
	 * the specified target - if such a move is possible given possible sizing constraints
	 * registered on the target object.
	 * <p>
	 * The {@link MosaicEngine} will respond to this request via the {@link SurfaceListener}
	 * interface's methods to instruct the client as to what exact changes need to be made,
	 * this allows the client to be totally passive with regard to necessary changes to affect
	 * the change in its layout.
	 * 
	 * @param source		the source object to be moved.
	 * @param target		the target object indicating the move resulting area.
	 * @param p				the position relative to the "target" where the source will be moved,
	 * 						if possible.
	 * @see #requestMoveBegin(Object)
	 * @see #requestMoveTest(Object, Object, Position)
	 * @see #requestMoveCommit(Object, Object, Position)
	 * @see #requestMoveReject(Object)
	 * @see #requestMoveCancel(Object)
	 */
	public void requestMove(T source, T target, Position p);
	
	/**
	 * Stage one of an incremental programmatic move of an object from one location in 
	 * the layout to another. This begins a transaction of sorts which must either end 
	 * with a call to {@link #requestMoveCommit(Object, String)} or {@link #requestMoveReject(Object, String)}.
	 * The layout engine will allow no other mutative effects until either of the above
	 * two methods are called, signalling the end of the move operation.
	 * <p>
	 * First,  {@link ChangeType#MOVE_BEGIN} will be sent to the client which then can adjust the 
	 * presentation of the moved object to be viewed as a "ghost" object.
	 * <p>
	 * Next, this specific phase will detach the object to be moved from its surrounding layout
	 * and signal the layout to be altered via the {@link SurfaceListener} interface to
	 * adjust surrounding objects within the layout to affect the specified object's removal.
	 * A specific {@link ChangeType} will be issued called {@link ChangeType#ANIMATE_RESIZE_RELOCATE}
	 * for all objects which this removal will affect, giving the client the opportunity
	 * to animate the positional changes incurred during this phase.
	 * 
	 * @param source	the object to be moved.
	 * @see #requestMove(Object, Object, Position)
	 * @see #requestMoveTest(Object, Object, Position)
	 * @see #requestMoveCommit(Object, Object, Position)
	 * @see #requestMoveReject(Object)
	 * @see #requestMoveCancel(Object)
	 */
	public void requestMoveBegin(T source);
	/**
	 * Stage two of an incremental programmatic move of an object from one location in
	 * the layout to another. This will signal the engine to respond with a test drop 
	 * location and size for the source object and a test drop mutation for the target
	 * object, where the target object "yields" a space that represents the resultant
	 * layout configuration should the drop be committed.
	 * 
	 * @param source	the object being moved.
	 * @param target	the object sharing its boundaries to provide a drop location
	 * @param p			the quadrant of the target object specifying the drop location.
	 * @see #requestMove(Object, Object, Position)
	 * @see #requestMoveBegin(Object)
	 * @see #requestMoveCommit(Object, Object, Position)
	 * @see #requestMoveReject(Object) 
	 * @see #requestMoveCancel(Object)
	 */
	public void requestMoveTest(T source, T target, Position p);
	/**
	 * One of two possible last stages of the move operation. This indicates that the 
	 * move operation should conclude with the object specified to be position at the
	 * last location tested.
	 * <p>
	 * The engine will respond through the {@link SurfaceListener} interface with location
	 * instructions for the source object and any other objects necessary. The last 
	 * instruction will be a {@link ChangeType#MOVE_END} which should signal the client
	 * to restore/remove any ghosting effects on the object representing the moved object.
	 * 
	 * @param source	the object being moved.
	 * @param target TODO
	 * @param position TODO
	 * @see #requestMove(Object, Object, Position)
	 * @see #requestMoveBegin(Object)
	 * @see #requestMoveTest(Object, Object, Position)
	 * @see #requestMoveReject(Object)
	 * @see #requestMoveCancel(Object)
	 */
	public void requestMoveCommit(T source, T target, Position position);
	
	/**
	 * An interim reset of a "placement" in a test position where the underlying 
	 * object no longer "yields" its occupied space to a ghost image, or temporarily
	 * moved object. The move source object still appears in the same place but 
	 * the underlying target object no longer yields its representative move area.
	 * <p>
	 * This method returns the layout to the same state it would be in if the cursor
	 * had been moved to its current location and {@link #requestMoveBegin(Object)}
	 * had been called.
	 * 
	 * @param source
	 * @see #requestMove(Object, Object, Position)
	 * @see #requestMoveBegin(Object)
	 * @see #requestMoveTest(Object, Object, Position)
	 * @see #requestMoveCommit(Object, Object, Position)
	 * @see #requestMoveCancel(Object)
	 */
	public void requestMoveReject(T source);
	
	/**
	 * One of two possible last stages of the move operation. This indicates that the 
	 * move operation should conclude with the object specified being moved back to its
	 * original position and any other objects affected by operations during the "move
	 * operation" to be restored to their original location/dimensions.
	 * <p>
	 * The engine will respond through the {@link SurfaceListener} interface with location
	 * instructions for the source object and any other objects necessary. The last 
	 * instruction will be a {@link ChangeType#MOVE_END} which should signal the client
	 * to restore/remove any ghosting effects on the object representing the moved object.
	 * 
	 * @param source	the object being moved.
	 * @see #requestMove(Object, Object, Position)
	 * @see #requestMoveBegin(Object)
	 * @see #requestMoveTest(Object, Object, Position)
	 * @see #requestMoveCommit(Object, Object, Position)
	 * @see #requestMoveReject(Object)
	 */
	public void requestMoveCancel(T source);
	
	/**
	 * Returns a flag indicating that an uninterruptible operation is underway.
	 * 
	 * @return	true if locked, false if not.
	 */
	public boolean isLocked();
	
	/**
	 * Adds the specified {@link List} of objects to the objects this engine will manage.
	 * <p>
	 * This method and {@link Surface#add(String, Object)} are to be used <em> only </em> with serialization mechanism.
	 * Those two methods are safe to call when a {@link Layout} specification is expected to be de-serialized into this
	 * surface.
	 * <p>
	 * The number of objects in the List of T's passed in <em>must</em> be equal to
	 * the number of {@link Layout} cell definitions in the previously set during the call to
	 * {@link #addAll(List)}, such that there is a 1-to-1 correspondence.
	 * <p>
	 * This method waits until the user calls {@link #requestLayout()} before attempting
	 * to determine any layout positions or dimension of the added objects. see {@link
	 * #add(Object, double, double, double, double)} for a more thorough description of
	 * the problem this solves.
	 * 
	 * @param 			tList				the list of T to add to this Surface.
	 * @param			idList					the list of ids corresponding to the T entries in "tList"
	 * @return			this Surface.
	 * @throws IllegalArgumentException 	when tList is not the same size as the size of
	 * 		the model definitions previously set during the call to {@link #addAll(List)}
	 * 		<em>-or-</em> not the same as the number of ids.
	 */
	public Surface<T> addAll(List<T> tList, List<String> idList);
	
	/**
	 * Adds a {@link LayoutImpl} object to this {@code Surface}. This Surface's
	 * resulting Layout will be a Union of both the current Layout set on this
	 * Surface and the one specified.
	 * 
	 * @param layout	the added {@link Layout}
	 * @return		this {@code Surface}
	 */
	public Surface<T> addLayoutContents(Layout layout);
	
	/**
	 * Sets the specified {@link Layout} object on this {@code Surface}. As opposed
	 * to the {@link #addLayoutContents(Layout)} method, this method totally replaces the existing
	 * Layout set on this Surface.
	 * 
	 * @param l		the Layout to set
	 * @return		this {@code Surface}.
	 */
	public Surface<T> setLayout(Layout l);
	
	/**
	 * Returns the current {@link Layout} currently set on this {@code Surface}
	 * 
	 * @return	the current {@link Layout} currently set on this {@code Surface}
	 */
	public Layout getLayout();
	
	/**
	 * Returns a flag indicating whether the current state is before the first layout cycle.
	 * @return
	 */
	public boolean getIsInit();
	
	/**
	 * 
	 * @param id
	 * @param t
	 * @param percentX
	 * @param percentY
	 * @param percentWidth
	 * @param percentHeight
	 * @param minW
	 * @param maxW
	 * @param minH
	 * @param maxH
	 * @return
	 */
	public Surface<T> addRelative(String id, T t, double percentX, double percentY, double percentWidth, double percentHeight,
		double minW, double maxW, double minH, double maxH);
	
	/**
	 * 
	 * @param id
	 * @param t
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param minW
	 * @param maxW
	 * @param minH
	 * @param maxH
	 * @return
	 */
	public Surface<T> addAbsolute(String id, T t, double x, double y, double width, double height,
		double minW, double maxW, double minH, double maxH);
	
	/**
	 * Adds a {@link SurfaceListener} which listens to the engine
	 * responses for this {@link Surface} and modifies its
	 * components in response to the engine's directives.
	 * 
	 * @param listener	the {@link SurfaceListener}
	 * 
	 * @return	this {@code Surface}
	 */
	public Surface<T> addChangeListener(SurfaceListener<T> listener);
	
	/**
	 * Removes the specified {@link SurfaceListener} from the list of listeners
	 * receiving notifications regarding this {@link Surface}
	 * 
	 * @param listener	the {@link SurfaceListener} to remove.
	 */
	public void removeChangeListener(SurfaceListener<T> listener);
	
	/**
	 * Rectangle containing the bounds of this surface.
	 * Non-zero x/y values must be accompanied by corresponding call to
	 * {@link #setUseSurfaceOffset(boolean)}, which must be called with
	 * a "true" setting before setting a non-zero origin.
	 * 
	 * @param r	Rectangle containing the bounds of this surface.
	 */
	public void setArea(Rectangle2D.Double r);
	
	/**
	 * Returns the Rectangle containing the bounds of this surface.
	 * 
	 * @return	Rectangle containing the bounds of this surface.
	 * @see #setArea(java.awt.geom.Rectangle2D.Double)
	 */
	public Rectangle2D.Double getArea();
	
	/**
	 * Returns a flag indicating whether the offset should
	 * be used.
	 * @param b	
	 * 
	 * @return			this {@code Surface}.
	 */
	public Surface<T> setUseSurfaceOffset(boolean b);
	
	/**
	 * Returns a flag indicating whether the engine is
	 * set to use offsets when returning locations of 
	 * objects it manages.
	 * 
	 * @return 	true if yes, false if no
	 */
	public boolean getUseSurfaceOffset();
	
	/**
	 * Distance within which a snap to a relevant divider's location or screen
	 * edge location will occur.
	 * 
	 * @param distance		the distance to a given snap location
	 * @return			this {@code Surface}.
	 */
	public Surface<T> setSnapDistance(double distance);
	
	/**
	 * Returns the distance within which a snap to a relevant divider's location or screen
	 * edge location will occur.
	 * 
	 * @return	the snap distance
	 */
	public double getSnapDistance();

	/**
	 * Sets the offset added to the location calculations and returned
	 * {@link Rectangle2D} objects by the {@link SurfaceListener} interface.
	 * 
	 * @param offset	the distance (x,y) from the origin (0,0).
	 * @return			this {@code Surface}.
	 */
	public Surface<T> setSurfaceOffset(Point2D.Double offset);
	
	/**
	 * Returns the offset added to the location calculations and returned
	 * {@link Rectangle2D} objects by the {@link SurfaceListener} interface. 
	 * 
	 * @return	the offset added to the location calculations
	 */
	public Point2D.Double getSurfaceOffset();

	/**
	 * Sets the flag indicating whether results returned
	 * from the engine should be resolved to integer
	 * precision - used for those GUI toolkits which don't
	 * use double precision for layout.
	 * 
	 * @param b
	 * @return			this {@code Surface}.
	 */
	public Surface<T> setUseIntegerPrecision(boolean b);
	
	/**
	 * Returns a boolean flag indicating that all calculations of location 
	 * and size will be to integer precision as opposed to double precision.
	 * 
	 * @return	true, if useIntegerPrecision is set to true, false if not.
	 */
	public boolean getUseIntegerPrecision();

	/**
	 * Sets the radius which forms a circle around a given corner,
	 * within which a corner drag gesture will be recognized.
	 * 
	 * @param radius	the distance from the corner.
	 * @return			this {@code Surface}.
	 */
	public Surface<T> setCornerClickRadius(double radius);
	
	/**
	 * Returns the radius which forms a circle around a given corner,
	 * within which a corner drag gesture will be recognized. 
	 * 
	 * @return	corner click radius recognition distance.
	 */
	public double getCornerClickRadius();

	/**
	 * Sets the size of all dividers in their static (unchanging) dimension.
	 * @param size	the size of all dividers.
	 * @return		this {@code Surface}.
	 */
	public Surface<T> setDividerSize(double size);
	
	/**
	 * Returns the configured divider size.
	 * 
	 * @return	the configured divider size.
	 */
	public double getDividerSize();
	
	/**
	 * This method is to be called by the client code everytime a layout
	 * is requested. Most often it should simply be called from overridden
	 * client code that gets called by the client framework so that calls to layout
	 * can take place as if they were being issued to a "layout manager" in 
	 * the native code. An example of this, is the "doLayout" method in a Swing
	 * Component, the "layout" method of a Pivot skin, or "changed" method of a "BoundsProperty"
	 * ChangeListener in JavaFX.
	 */
	public void requestLayout();
	
	/**
	 * Notifies this Surface that changes to its underlying layout have occurred.
	 * 
	 * @param changeType 	the {@link ChangeType} which describes the action to be taken.
	 * @param t				the <T> or layed out object type
	 * @param id			the id of the object being layed out.
	 * @param previous		the previous rectangle describing the object's bounds.
	 * @param current		the current rectangle describing the object's new bounds.
	 */
	public void notifyChange(ChangeType changeType, T t, String id, Rectangle2D.Double previous, Rectangle2D.Double current);
	
	/**
	 * User must call this method to receive layout events relevant to mouse drags
	 * such as {@link Divider} dragging and node resizing etc.
	 * 
	 * @param x		the x location of the user's mouse press.
	 * @param y		the y location of the user's mouse press.
	 */
	public void mousePressed(double x, double y);
	
	/**
	 * User must call this method to receive layout events relevant to mouse drags
	 * such as {@link Divider} dragging and node resizing etc.
	 * 
	 * @param x		the x location of the user's mouse drag.
	 * @param y		the y location of the user's mouse drag.
	 */
	public void mouseDragged(double x, double y);
	
	/**
	 * User must call this method to receive layout events relevant to mouse releases.
	 */
	public void mouseReleased();
	
	/**
	 * Returns a serialized form of this {@code Surface} in
	 * JSON format.
	 * 
	 * @return	this Surface in serialized form
	 */
	public String serialize();
	
	/**
	 * Returns a {@code Surface} implementation from a JSON String.
	 * 
	 * @param jsonSurface
	 * @return	the deserialized {@code Surface}
	 */
	public Surface<T> deSerialize(String jsonSurface);

}

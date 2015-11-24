package container.mosaic;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonAutoDetect
public class SurfaceImpl<T> extends SurfacePriviledged<T> {
	private enum AddMode { UNSET, RELATIVE, ABSOLUTE };
	
	
	/** Flag indicating first layout run */
    private boolean isInit = true;
    
    /** Enabled flag */
    @JsonProperty
    private boolean isEnabled = true;
    
    /** Flag indicating whether a drag operation is currently ocurring */
    private boolean isDragging;
    
    /** the size opposite to its direction */
    @JsonProperty
    private double dividerSize = 3;
    
    /** Flag for offset values being added to x, y */
    @JsonProperty
    private boolean useSurfaceOffset;
    
    /** Flag for indicating results are integer precision */
    @JsonProperty
    private boolean useIntegerPrecision;
    
    /** Flag indicating that this {@code SurfaceImpl} was created from serialization */
    public boolean constitutedFromSerialization;
    
    /** Flag indicating that this {@code SurfaceImpl} is undergoing a transactive operation */
    @JsonIgnore
    private boolean locked;
    
    /** Flag indicating that a recent move operation produced a valid drop condition */
    @JsonIgnore
    private boolean hasValidDrop;
    
    /** the minimum distance from another divider before it snaps */
    @JsonProperty
    private double snapDistance = 15;
    
    /** max distance from a corner for drag recognition */
    @JsonProperty
    private double cornerClickRadius = 3;
    
    /** the minimum distance from the outer edge of the surface before adding a new divider */
    @JsonProperty
	private Point2D.Double boundaryDividerCondition = new Point2D.Double(20, 20);
	
	/** List of {@link SurfaceListener}s. */
	private ChangeListenerList listeners = new ChangeListenerList();
	
	/** Offset point from (0,0) */
	@JsonProperty
    private Point2D.Double surfaceOffset = new Point2D.Double(0, 0);
    
    /** Rectangle containing the bounds of this surface. */
	@JsonSerialize(using = AreaSerializer.class)
	@JsonDeserialize(using = AreaDeserializer.class)
    private Rectangle2D.Double area = new Rectangle2D.Double(0, 0, 0, 0);
    
    /** The current engine this Surface is added to */
    private MosaicEngineImpl<T> engine;
    
    /** Used internally to track adding pre/post init */
    private AddMode addMode = AddMode.UNSET;
    
    /** The surface's input manager used for mouse input handling */
    private MosaicEngineImpl<T>.InputManager inputManager;
    
    /** Used by the engine to temporarily store its search results for overlapping dividers */
    private Map<Divider<T>, List<Divider<T>>> tempOverlapSearchMap = new HashMap<Divider<T>, List<Divider<T>>>();
       
    /** Used for persistence handling and temporary object storage prior to addition to engine */
    @JsonProperty
    private LayoutImpl<T> layout;
    
    /** Used for saving of state for animated rollback if needed */
    private LayoutImpl<T> layoutCopy;
    
    /** Used for saving the state during move - after removal of move node */
    private LayoutImpl<T> interimLayoutSnapshot;
    
    @JsonIgnore
    private String nodeCursor;
    
    /**
	 * Stores change listeners and notifies them when a 
	 * change has occured to an object being managed by this
	 * engine.
	 */
	@SuppressWarnings("serial")
    private class ChangeListenerList extends ArrayList<SurfaceListener<T>> implements SurfaceListener<T> {
	    public void changed(ChangeType changeType, T t, String id, Rectangle2D oldRectangle , Rectangle2D newRectangle) {
	    	if(!isEnabled) return;
	    	
	        for(SurfaceListener<T> listener : this) {
	            listener.changed(changeType, t, id, oldRectangle, newRectangle);
	        }
	    }
	}
	
	/**
	 * Constructs a new {@code SurfaceImpl}
	 */
	SurfaceImpl() {}
	
	/**
	 * Returns the root {@link Node} understood to be the {@code Node}
	 * at location (x=0, y=0). Called by the {@link MosaicEngineImpl} to
	 * obtain a reference to the origin Node.
	 * 
	 * @return	the root {@link Node}
	 */
	Node<T> getRoot() {
		return layout.getRoot();
	}
	
	/**
	 * Called from the engine remove logic to set the root node if
	 * the previous root node has been removed.
	 * 
	 * @param n 	the new root
	 */
	void setRoot(Node<T> n) {
		layout.setRoot(n);
	}
	
	/**
	 * Returns this {@code Surface}'s {@link PathIterator}.
	 * 
	 * @return	this {@code Surface}'s {@link PathIterator}.
	 */
	PathIterator<T> getPathIterator() {
		return layout.getPathIterator();
	}
	
	/**
	 * Signals the {@link Layout} to recreate its string representation in
	 * preparation for serialization.
	 * 
	 * @param isRelative	whether this Layout's current spec is relative or not.
	 */
	public void updateLayoutSerializables(boolean isRelative) { //RETURN TO PRIVATE AFTER TESTING!
		layout.setRelative(isRelative);
		layout.clearSerializableDefinitions();
		
		for(Node<T> node : getNodeList()) {
			String id = node.stringID;
			layout.replaceOrAddCell(id, node.r.x, node.r.y, node.r.width, node.r.height, 
				node.getMinWidth(), node.getMaxWidth(), node.getMinHeight(), node.getMaxHeight(),
					node.getHorizontalWeight(), node.getVerticalWeight());
		}
		
		for(Divider<T> div : getVerticalDividers()) {
			layout.replaceOrAddDivider("" + div.stringID, true, div.r.x, div.r.y, div.r.width, div.r.height, 
				div.prevNodesSerial(), div.nextNodesSerial(), div.leadingJoinsSerial(), div.trailingJoinsSerial());
		}
		
		for(Divider<T> div : getHorizontalDividers()) {
			layout.replaceOrAddDivider("" + div.stringID, false, div.r.x, div.r.y, div.r.width, div.r.height, 
				div.prevNodesSerial(), div.nextNodesSerial(), div.leadingJoinsSerial(), div.trailingJoinsSerial());
		}
	}
	
	/**
	 * Creates a copy that can be used for partial or total recreation
	 * of a layout as it exists at the time of this method being called.
	 */
	void snapshotLayout() {
		this.layoutCopy = new LayoutImpl<T>(layout);
	}
	
	LayoutImpl<T> getSnapshot() {
		return this.layoutCopy;
	}
	
	void revertLayout() {
		this.layout.clearAll();
		this.layout = new LayoutImpl<T>(layoutCopy);
	}
	
	/**
	 * Returns a serialized form of this {@code Surface} in
	 * JSON format.
	 * 
	 * @return	this Surface in serialized form
	 */
	public String serialize() {
		updateLayoutSerializables(false);
		
		String json = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			json = mapper.writeValueAsString(this);
			System.out.println(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(this));
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return json;
	}
	
	/**
	 * Returns a {@code Surface} implementation from a JSON String.
	 * 
	 * @param jsonSurface
	 * @return	the deserialized {@code Surface}
	 */
	@SuppressWarnings("unchecked")
	public SurfaceImpl<T> deSerialize(String jsonSurface) {
		ObjectMapper om = new ObjectMapper();
		SurfaceImpl<T> si = null;
		try {
			si = (SurfaceImpl<T>)om.readValue(jsonSurface, SurfaceImpl.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		si.createNodesFromSerialized();
		si.createDividersFromSerialized();
		
		si.constitutedFromSerialization = true;
		si.setIsInit(false);
		
		si.getPathIterator().assemblePaths(si.getRoot());
		
		return si;
	}
	
	/**
	 * Returns a deep copy snapshot of this {@code Surface}'s 
	 * Layout in its current state.
	 * 
	 * @return a deep copy of this {@link LayoutImpl}
	 */
	public LayoutImpl<T> copyLayout() {
		long start = System.nanoTime();
		LayoutImpl<T> copy = new LayoutImpl<T>(layout);
		long finish = System.nanoTime() - start;
		System.out.println("copy layout: execution time: " + ((double)finish / 1000000000.0d) + "  secs.");
		return copy;
	}
	
	/**
	 * Delegates to the {@link Layout} to retrieve the {@link Node}
	 * specified by "id".
	 * 
	 * @param id	the String id of the retrieved {@link Node}
	 * @return	the {@link Node} specified by "id".
	 */
	Node<T> getNode(String id) {
		return layout.getNode(id);
	}
	
	/**
	 * Delegates to the {@link Layout} to retrieve the {@link Divider}
	 * specified by "id".
	 * 
	 * @param id	the String id of the retrieved {@link Divider}
	 * @param isVertical	the flag indicating type of {@link Divider}
	 * @return	the {@link Divider} specified by "id".
	 */
	Divider<T> getDivider(String id, boolean isVertical) {
		return layout.getDivider(id, isVertical);
	}
	
	/**
	 * Ratio of Surface size to the size of a hit or miss tolerance/distance
	 * when estimating dividers which Nodes belong attached to.
	 * 
	 * @return
	 */
	double getToleranceRatio() {
		return MosaicEngineImpl.DIVIDER_TOLERANCE_RATIO;
	}
	
	/**
	 * Called by client code to begin a layout cycle.
	 */
	@Override
	public void requestLayout() {
		if(engine == null) {
			throw new IllegalStateException("This surface has not been added to a MosaicEngine");
		}
		
		engine.requestLayout(this);
	}
	
	/**
	 * Requests removal of the Object t from this Surface's {@link Layout}.
	 * 
	 * Actual removal should only take place as a response to the {@link SurfaceListener} 
	 * interface message.
	 * @param t		the <T> to remove.
	 */
	public void requestRemove(T t) {
		requestRemove(layout.get(t));
	}
	
	/**
	 * Removes the Object identified by the specified id from this Surface's {@link Layout}.
	 */
	public void requestRemove(String id) {
		Node<T> node = layout.getNode(id);
		if(node == null) {
			return;
		}
		
		List<Node<T>> affectedNodes = engine.requestRemoveElement(this, node);
		
		if(!affectedNodes.isEmpty()) {
			engine.adjustWeights(this);
			
			//Signal the client to remove the object associated with the removed "node".
			node.force(this, /* isMoveOperation() ? ChangeType.REMOVE_RETAIN : */  ChangeType.REMOVE_DISCARD);
			
			for(Node<T> n : affectedNodes) {
				n.set(this, ChangeType.RESIZE_RELOCATE);
			}
			
			removeNodeReferences(node);
			
			getPathIterator().assemblePaths(layout.getRoot());
		}else{
			//////////  VETO REMOVE /////////
		}
	}
	
	/**
	 * Called to validate add params.
	 * 
	 * @param source		the object being added
	 * @param sourceID		the id of the object being added
	 * @param target		the target whose area will be shared by the added object
	 * @param p				the enum {@link Position} determining the add location.
	 */
	private void validateAddParams(T source, String sourceID, T target, Position p) {
		if(source == null || sourceID == null || sourceID.length() < 1 || target == null || p == null) {
			String message = null;
			
			if(source == null) {
				message = "Attempt to add a null source object";
			}else if(sourceID == null) {
				message = "Attempt to assign an invalid id to added source " + source + ",  sourceID = " + sourceID;
			}else if(target == null) {
				message = "Attempt to add a source object to an invalid target";
			}else if(p == null) {
				message = "Attempt to add a source object to an invalid target location p = " + p;
			}
			throw new IllegalArgumentException(message);
		}
	}
	
	/**
	 * Returns a flag indicating that a recent Move/Drop operation
	 * produced the necessary conditions for a valid drop. This is
	 * in turn queried by the {@link MosaicEngine} to issue commands
	 * relevant to commiting a drop.
	 * 
	 * @return	true if valid drop exists, false if not.
	 */
	@JsonIgnore
	@Override
	public boolean hasValidDrop() {
		return hasValidDrop;
	}
	
	/**
	 * Sets the flag indicating whether a recent Move/Drop operation
	 * produced a valid drop condition.
	 * 
	 * @param b		the flag to set.
	 */
	@Override
	public void setHasValidDrop(boolean b) {
		this.hasValidDrop = b;
	}
	
	void snapshotInterimLayout() {
		this.interimLayoutSnapshot = new LayoutImpl<T>(layout);
	}
	
	LayoutImpl<T> getInterimSnapshot() {
		return interimLayoutSnapshot;
	}
	
	/**
	 * Begins an "Add" operation which will be "answered" as a response
	 * through the {@link SurfaceListener} interface. 
	 * <p>
	 * The {@link MosaicEngine} will respond to this request via the {@link SurfaceListener}
	 * interface's methods to instruct the client as to what exact changes need to be made,
	 * this allows the client to be totally passive with regard to necessary changes to affect
	 * the change in its layout.
	 */
	@Override
	public void requestAdd(T source, String sourceID, T target, Position p) {
		validateAddParams(source, sourceID, target, p);
		
		Node<T> newNode = new Node<T>(source, sourceID, 0, 0, 0, 0, 0, Double.MAX_VALUE, 0, Double.MAX_VALUE, 0, 0);
		engine.requestAddElement(this, newNode, getNode(layout.get(target)), p);
	}

	/**
	 * Moves the specified source object to the specified {@link Position} relative to
	 * the specified target - if such a move is possible given possible sizing constraints
	 * registered on the target object.
	 * <p>
	 * The {@link MosaicEngine} will respond to this request via the {@link SurfaceListener}
	 * interface's methods to instruct the client as to what exact changes need to be made,
	 * this allows the client to be totally passive with regard to necessary changes to affect
	 * the change in its layout.
	 * <p>
	 * <em><b>NOTE: It should be noted that none of the requestMove...() methods are absolutely
	 * 			necessary in order to get object moving effects when the client is relaying
	 * 			its mouse events to this surface as it should be for normal operation. These
	 * 			methods are provided for more minute control and any other client effects that
	 * 			may be required, and are used by this package's testing infrastructure as well.
	 * </b></em>
	 * 
	 * @param source		the source object to be moved.
	 * @param target		the target object indicating the move resulting area.
	 * @param p				the position relative to the "target" where the source will be moved,
	 * 						if possible.
	 * @see #requestMoveBegin(Object)
	 * @see #requestMoveTest(Object, Object, Position)
	 * @see #requestMoveCommit(Object, Object, Position)
	 * @see #requestMoveReject(Object)
	 * @see #requestMoveCancel()
	 */
	@Override
	public void requestMove(T source, T target, Position p) {
		//here
	}
	
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
	 * @see #requestMoveCancel()
	 */
	public void requestMoveBegin(T source) {
		if(isLocked()) return;
		setLocked(true);
		
		engine.beginDropElement(this, layout.getNode(layout.get(source)));
		if(layoutCopy.get(source).equals(layoutCopy.getRoot().stringID)) {
			nodeCursor = interimLayoutSnapshot.getRoot().stringID;
		}
	}

	/**
	 * Stage two of an incremental programmatic move of an object from one location in
	 * the layout to another. This will signal the engine to respond with a test drop 
	 * location and size for the source object and a test drop mutation for the target
	 * object.
	 * 
	 * @param source	the object being moved.
	 * @param target	the object sharing its boundaries to provide a drop location
	 * @param p			the quadrant of the target object specifying the drop location.
	 * @see #requestMove(Object, Object, Position)
	 * @see #requestMoveBegin(Object)
	 * @see #requestMoveCommit(Object, Object, Position)
	 * @see #requestMoveReject(Object)
	 * @see #requestMoveCancel()
	 */
	@Override
	public void requestMoveTest(T source, T target, Position p) {
		if(! isLocked()) return;
		
		engine.testDropElement(this, interimLayoutSnapshot, 
			layoutCopy.getNode(layoutCopy.get(source)), layout.getNode(layout.get(target)), p);
	}

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
	 * @see #requestMove(Object, Object, Position)
	 * @see #requestMoveBegin(Object)
	 * @see #requestMoveTest(Object, Object, Position)
	 * @see #requestMoveReject(Object)
	 * @see #requestMoveCancel(Object)
	 */
	@Override
	public void requestMoveCommit(T source, T target, Position position) {
		setLocked(false);
		// TODO Auto-generated method stub
	}

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
	 * @see #requestMoveCancel()
	 */
	@Override
	public void requestMoveReject(T source) {
		// TODO Auto-generated method stub
		
	}
	
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
	@Override
	public  void requestMoveCancel(T source) {
		engine.cancelDropElement(this, layoutCopy.getNode(layoutCopy.get(source)));
		setLocked(false);
	}

	/**
	 * Returns a flag indicating that an uninterruptible operation is underway.
	 * 
	 * @return	true if locked, false if not.
	 */
	@Override
	@JsonIgnore
	public boolean isLocked() {
		return locked;
	}
	
	/**
	 * Sets the locked flag on this {@link Surface} to indicate that an uniterruptible
	 * mutation is underway
	 */
	@Override
	@JsonIgnore
	void setLocked(boolean b) {
		this.locked = b;
	}

	/**
	 * Called by the MosaicEngine to set itself as a reference once this 
	 * {@code Surface} is added to the engine.
	 * 
	 * @param engine
	 */
	@Override
	void setEngine(MosaicEngine<T> engine) {
		if(!constitutedFromSerialization) {
			validateLayout();
			createNodeList();
		}
		
		this.engine = (MosaicEngineImpl<T>)engine; 
		
		this.inputManager = ((MosaicEngineImpl<T>)engine).new InputManager(this);
	}
	
	/**
	 * Generates {@link Node}s using the Layout object's layout spec.
	 */
	private void createNodeList() {
		List<Node<T>> nodeList = getNodeList();
		for(String id : layout.stringKeySet()) {
			String[] spec = layout.getCell(id).split(LayoutConstants.CELL_PTRN);
			Node<T> node = null;
			if(spec.length > LayoutConstants.MAX_H) {
				node = new Node<T>(layout.get(id), id, 
						Double.parseDouble(spec[LayoutConstants.X]),
						Double.parseDouble(spec[LayoutConstants.Y]),
						Double.parseDouble(spec[LayoutConstants.W]),
						Double.parseDouble(spec[LayoutConstants.H]),
						Double.parseDouble(spec[LayoutConstants.MIN_W]),
						Double.parseDouble(spec[LayoutConstants.MAX_W]),
						Double.parseDouble(spec[LayoutConstants.MIN_H]),
						Double.parseDouble(spec[LayoutConstants.MAX_H]));
			} else {
				node = new Node<T>(layout.get(id), id, 
					Double.parseDouble(spec[LayoutConstants.X]),
					Double.parseDouble(spec[LayoutConstants.Y]),
					Double.parseDouble(spec[LayoutConstants.W]),
					Double.parseDouble(spec[LayoutConstants.H]));
			}
			if(node.percentX == 0 && node.percentY == 0 && node.r.x == 0 && node.r.y == 0) {
				layout.setRoot(node);
			}
			
			nodeList.add(node);
		}
	}
	
	private void createNodesFromSerialized() {
		List<Node<T>> nodeList = getNodeList();
		for(String id : layout.stringKeySet()) {
			String[] spec = layout.getCell(id).split(LayoutConstants.CELL_PTRN);
			Node<T> node = null;
			node = new Node<T>(layout.get(id), id, 
				Double.parseDouble(spec[LayoutConstants.X]),
				Double.parseDouble(spec[LayoutConstants.Y]),
				Double.parseDouble(spec[LayoutConstants.W]),
				Double.parseDouble(spec[LayoutConstants.H]),
				Double.parseDouble(spec[LayoutConstants.MIN_W]),
				Double.parseDouble(spec[LayoutConstants.MAX_W]),
				Double.parseDouble(spec[LayoutConstants.MIN_H]),
				Double.parseDouble(spec[LayoutConstants.MAX_H]),
				Double.parseDouble(spec[LayoutConstants.H_WT]),
				Double.parseDouble(spec[LayoutConstants.V_WT]));
			
			if(node.percentX == 0 && node.percentY == 0 && node.r.x == 0 && node.r.y == 0) {
				layout.setRoot(node);
			}
			nodeList.add(node);
		}
		
		List<Node<T>> targetLessNodes = createTargetlessNodes();
		nodeList.addAll(targetLessNodes);
	}
	
	private List<Node<T>> createTargetlessNodes() {
		List<Node<T>> nodeList = getNodeList();
		List<Node<T>> retList = new ArrayList<Node<T>>();
		for(String cell : layout.getCells()) {
			Node<T> node = createSerializedNode(cell);
			if(!nodeList.contains(node)) {
				retList.add(node);
			}
		}
		return retList;
	}
	
	private Node<T> createSerializedNode(String layoutSpec) {
		String[] spec = layoutSpec.split(LayoutConstants.CELL_PTRN);
		String id = layout.parse(layoutSpec, LayoutConstants.ID);
		Node<T> node = new Node<T>(layout.get(id), id, 
			Double.parseDouble(spec[LayoutConstants.X]),
			Double.parseDouble(spec[LayoutConstants.Y]),
			Double.parseDouble(spec[LayoutConstants.W]),
			Double.parseDouble(spec[LayoutConstants.H]),
			Double.parseDouble(spec[LayoutConstants.MIN_W]),
			Double.parseDouble(spec[LayoutConstants.MAX_W]),
			Double.parseDouble(spec[LayoutConstants.MIN_H]),
			Double.parseDouble(spec[LayoutConstants.MAX_H]),
			Double.parseDouble(spec[LayoutConstants.H_WT]),
			Double.parseDouble(spec[LayoutConstants.V_WT]));
		
		if(node.percentX == 0 && node.percentY == 0 && node.r.x == 0 && node.r.y == 0) {
			layout.setRoot(node);
		}
		
		return node;
	}
	
	private void createDividersFromSerialized() {
		List<Divider<T>> horizontalDividers = getHorizontalDividers();
		List<Divider<T>> verticalDividers = getVerticalDividers();
		for(String divStr : layout.getSerializedHorizontalDividers()) {
			Divider<T> d = createDividerFromSerialized(divStr, false);
			horizontalDividers.add(d);
		}
		
		for(String divStr : layout.getSerializedVerticalDividers()) {
			Divider<T> d = createDividerFromSerialized(divStr, true);
			verticalDividers.add(d);
		}
		
		for(String divStr : layout.getSerializedHorizontalDividers()) {
			createSerializedJoins(getDivider(layout.parse(divStr, LayoutConstants.ID), false), divStr, false);
		}
		
		for(String divStr : layout.getSerializedVerticalDividers()) {
			createSerializedJoins(getDivider(layout.parse(divStr, LayoutConstants.ID), true), divStr, true);
		}
	}
	
	private Divider<T> createDividerFromSerialized(String divStr, boolean isVertical) {
		String[] spec = divStr.split(LayoutConstants.CELL_PTRN);
		Divider<T> d = new Divider<T>();
		d.dividerSize = dividerSize;
		d.isVertical = isVertical;
		d.setId(Integer.parseInt(spec[LayoutConstants.ID]));
		
		Rectangle2D.Double r = new Rectangle2D.Double(
			Double.parseDouble(spec[LayoutConstants.X]),
			Double.parseDouble(spec[LayoutConstants.Y]),
			Double.parseDouble(spec[LayoutConstants.W]),
			Double.parseDouble(spec[LayoutConstants.H]));
		d.r = r;
		
		String[] prevNodeIds = spec[LayoutConstants.PRV_N].split(LayoutConstants.SUB_PTRN);
		for(String nodeId : prevNodeIds) {
			if(nodeId.equals(Divider.EMPTY)) continue;
			
			Node<T> node = layout.getNode(nodeId);
			d.addPrevious(node);
			if(d.isVertical) {
				node.nextVertical = d;
			}else{
				node.nextHorizontal = d;
			}
		}
		String[] nextNodeIds = spec[LayoutConstants.NXT_N].split(LayoutConstants.SUB_PTRN);
		for(String nodeId : nextNodeIds) {
			if(nodeId.equals(Divider.EMPTY)) continue;
			Node<T> node = getNode(nodeId);
			d.addNext(node);
			if(d.isVertical) {
				node.prevVertical = d;
			}else{
				node.prevHorizontal = d;
			}
		}
		
		return d;
	}
	
	private void createSerializedJoins(Divider<T> d, String divStr, boolean isVertical) {
		String[] spec = divStr.split(LayoutConstants.CELL_PTRN);
		String[] leadingJoinIds = spec[LayoutConstants.LED_J].split(LayoutConstants.SUB_PTRN);
		for(String divId : leadingJoinIds) {
			if(divId.equals(Divider.EMPTY)) continue;
			Divider<T> div = getDivider(divId, !isVertical);
			d.leadingJoins.add(div);
			div.trailingJoin = d;
		}
		
		String[] trailingJoinIds = spec[LayoutConstants.TRL_J].split(LayoutConstants.SUB_PTRN);
		for(String divId : trailingJoinIds) {
			if(divId.equals(Divider.EMPTY)) continue;
			Divider<T> div = getDivider(divId, !isVertical);
			d.trailingJoins.add(div);
			div.leadingJoin = d;
		}
	}
	
	/**
	 * Checks the list of layout specs in the Layout object against the
	 * number of T ids set to make sure they are the same and that all
	 * T ids have corresponding layout specs.
	 */
	private void validateLayout() {
		List<String> missingIds = new ArrayList<String>();
		for(String id : layout.stringKeySet()) {
			if(layout.getCell(id) == null) {
				missingIds.add(id);
			}
		}
		
		if(missingIds.size() > 0) {
			throw new IllegalStateException("No layout spec found for ids: " + missingIds);
		}
		
		//Check layout's with no objects/ID's to match		
		for(String cell : layout.getCells()) {
			String id = layout.parse(cell, LayoutConstants.ID);
			if(layout.get(id) == null) {
				missingIds.add(id);
			}
		}
		
		if(missingIds.size() > 0) {
			throw new IllegalStateException("No <T> or ID found for ids: " + missingIds);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Surface<T> add(String id, T t) {
		if(id == null) {
			throw new IllegalArgumentException("Can't objects with a null id.");
		}
		
		layout.put(id, t);
		
		Node<T> node = getNodeWithID(id);
		if(node != null) {
			//Set the target object on the node
			node.setTarget(t);
		}else{
			String spec = layout.getCell(id);
			if(spec != null) {
				node = createSerializedNode(spec);
				node.setTarget(t);
				getNodeList().add(node);
			}
		}
		
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Surface<T> addAll(List<T> tList, List<String> idList) {
		if(tList.size() != idList.size()) {
			throw new IllegalArgumentException("Object list size != id list size.");
		}
		
		int i = 0;
		for(String id : idList) {
			T t = tList.get(i);
			layout.put(id, t);
			
			Node<T> node = getNodeWithID(id);
			if(node != null) {
				//Set the target object on the node
				node.setTarget(t);
			}else{
				String spec = layout.getCell(id);
				if(spec != null) {
					node = createSerializedNode(spec);
					node.setTarget(t);
					getNodeList().add(node);
				}
			}
		}
		
		return this;
	}
	
	private Node<T> getNodeWithID(String id) {
		for(Node<T> n : getNodeList()) {
			if(n.stringID.equals(id)) {
				return n;
			}
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Surface<T> addRelative(String id, T t, double percentX, double percentY, double percentWidth, double percentHeight,
		double minW, double maxW, double minH, double maxH) {
		if(isInit && addMode == AddMode.ABSOLUTE) {
			throw new IllegalArgumentException("Cannot add relative once an absolute specification has been added.");
		}
		
		if(addMode == AddMode.UNSET) {
			addMode = AddMode.RELATIVE;
		}
		
		if(layout == null) {
			layout = new LayoutImpl<T>(true);
		}
		
		layout.addCell(id, percentX, percentY, percentWidth, percentHeight, minW, maxW, minH, maxH);
		
		layout.put(id, t);
		
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Surface<T> addAbsolute(String id, T t, double x, double y, double width, double height, 
		double minW, double maxW, double minH, double maxH) {
		if(isInit && addMode == AddMode.RELATIVE) {
			throw new IllegalArgumentException("Cannot add absolute once a relative specification has been added.");
		}
		
		if(addMode == AddMode.UNSET) {
			addMode = AddMode.ABSOLUTE;
		}
		
		if(layout == null) {
			layout = new LayoutImpl<T>(false);
		}
		
		layout.addCell(id, x, y, width, height, minW, maxW, minH, maxH);
		
		layout.put(id, t);
		
		return this;
	}
	
	/**
	 * Adds a {@link LayoutImpl} object to this {@code Surface}. This Surface's
	 * resulting Layout will be a Union of both the current Layout set on this
	 * Surface and the one specified.
	 * 
	 * @param layout	the added {@link Layout}
	 * @return		this {@code Surface}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Surface<T> addLayoutContents(Layout layout) {
		if(!layout.isRelative()) {
			if(addMode == AddMode.RELATIVE) {
				throw new IllegalArgumentException("Cannot add an absolute layout after a relative spec has been added.");
			}
			
			if(addMode == AddMode.UNSET) {
				addMode = AddMode.ABSOLUTE;
			}
		}else{
			if(addMode == AddMode.ABSOLUTE) {
				throw new IllegalArgumentException("Cannot add a relative layout after an absolute spec has been added.");
			}
			
			if(addMode == AddMode.UNSET) {
				addMode = AddMode.RELATIVE;
			}
		}
		
		if(this.layout == null) {
			this.layout = (LayoutImpl<T>)layout;
		}else{
			this.layout.add(layout);
		}
		return this;
	}
	
	/**
	 * Called by the engine to remove Node references and linked infrastructure.
	 * @param node	the node being removed.
	 */
	void removeNodeReferences(Node<T> node) {
		layout.remove(node.stringID);
		node.disconnectFromDividers();
	}
	
	/**
	 * Called by the engine to temporarily store its search results for overlapping dividers
	 * @param d		the Divider acting as key
	 * @param l		the list of Divider search results.
	 */
	void storeSearchResults(Divider<T> d, List<Divider<T>> l) {
		tempOverlapSearchMap.put(d, l);
	}
	
	/**
	 * Called by the engine to retrieve temporary search results previously stored here.
	 *  
	 * @param key	the Divider acting as key
	 * @return		the list of Divider search results.
	 */
	List<Divider<T>> getSearchResults(Divider<T> key) {
		return tempOverlapSearchMap.get(key);
	}
	
	/**
	 * Clears the previous cylce's search results.
	 */
	void clearSearchResults() {
		tempOverlapSearchMap.clear();
	}
	
	/**
	 * Sets the specified {@link Layout} object on this {@code Surface}. As opposed
	 * to the {@link #addLayoutContents(Layout)} method, this method totally replaces the existing
	 * Layout set on this Surface.
	 * 
	 * @param l		the Layout to set
	 * @return		this {@code Surface}.
	 */
	@SuppressWarnings("unchecked")
	@Override
	@JsonIgnore
	public Surface<T> setLayout(Layout layout) {
		this.layout = (LayoutImpl<T>)layout;
		return this;
	}
	
	/**
	 * Returns the current {@link Layout} currently set on this {@code Surface}
	 * 
	 * @return	the current {@link Layout} currently set on this {@code Surface}
	 */
	@JsonIgnore
	public LayoutImpl<T> getLayout() {
		return this.layout;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Surface<T> addChangeListener(SurfaceListener<T> listener) {
	    listeners.add(listener);
	    return this;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeChangeListener(SurfaceListener<T> listener) {
	    listeners.remove(listener);
	}
	
	/**
	 * Notifies this Surface that changes to its underlying layout have occurred.
	 * 
	 * @param changeType	the {@link ChangeType} describing the action for the client to take.
	 * @param t				the <T> or layed out object type
	 * @param id			the id of the object being layed out.
	 * @param previous		the previous rectangle describing the object's bounds.
	 * @param current		the current rectangle describing the object's new bounds.
	 */
	public void notifyChange(ChangeType changeType, T t, String id, Rectangle2D.Double previous, Rectangle2D.Double current) {
		listeners.changed(changeType, t, id, previous, current);
	}
	
	/**
	 * Do any initial initialization here. This should be called by the
	 * {@link MosaicEngine} when or before executing the first layout. 
	 */
	void init() {
		//Initialize the smallest node size/boundaryDividerCondition
		for(Node<T> n : getNodeList()) {
			double w = n.percentWidth * area.width;
			if(w < area.width) {
				area.width = w; 
			}
			
			double h = n.percentHeight * area.height;
			if(h < area.height) {
				area.height = h;
			}
		}
	}
	
	/**
	 * Returns the list of {@link Node}s.
	 * 
	 * @return	the canonical list of {@link Node}s.
	 */
	List<Node<T>> getNodeList() {
		return layout.getNodeList();
	}
	
	/**
	 * Called by the {@link MosaicEngine} during initial layout when
	 * constructing new Dividers, to determine when a Node is not far
	 * enough from the edge of the surface to create a new Divider.
	 * 
	 * @return	the configured boundary divider condition.
	 */
	Point2D.Double getBoundaryDividerCondition() {
		return boundaryDividerCondition;
	}
	
	/**
	 * Returns the list of horizontal {@link Divider}s.
	 * 
	 * @return	the list of horizontal {@link Divider}s.
	 */
	List<Divider<T>> getHorizontalDividers() {
		return layout.getHorizontalDividers();
	}
	
	/**
	 * Returns the list of vertical {@link Divider}s.
	 * 
	 * @return	the list of vertical {@link Divider}s.
	 */
	List<Divider<T>> getVerticalDividers() {
		return layout.getVerticalDividers();
	}
	
	/**
	 * Sets the init flag indicating first layout run.
	 * 
	 * @param b	true if is first layout run or before, false if not
	 */
	void setIsInit(boolean b) {
		this.isInit = b;
	}
	
	/**
	 * Returns the init flag
	 * 
	 * @return	the init flag
	 * @see #setIsInit(boolean)
	 */
	@Override
	public boolean getIsInit() {
		return this.isInit;
	}
	
	/**
	 * Sets the flag indicating whether a drag operation is occurring.
	 * 	
	 * @param b	the flag indicating whether a drag operation is occurring.
	 */
	void setIsDragging(boolean b) {
		this.isDragging = b;
	}
	
	/**
	 * Sets the flag indicating whether a drag operation is occurring.
	 * 	
	 * @param b	the flag indicating whether a drag operation is occurring.
	 */
	boolean getIsDragging() {
		return this.isDragging;
	}
	
	/**
	 * Rectangle containing the bounds of this surface.
	 * @param r	Rectangle containing the bounds of this surface.
	 */
	@Override
	public void setArea(Rectangle2D.Double r) {
		this.area = r;
	}
	
	/**
	 * Returns the Rectangle containing the bounds of this surface.
	 * 
	 * @return	Rectangle containing the bounds of this surface.
	 * @see #setArea(java.awt.geom.Rectangle2D.Double)
	 */
	@Override
	public Rectangle2D.Double getArea() {
		return area;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Surface<T> setUseSurfaceOffset(boolean b) {
		this.useSurfaceOffset = b;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getUseSurfaceOffset() {
		return useSurfaceOffset;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Surface<T> setSurfaceOffset(Point2D.Double offset) {
		this.surfaceOffset = offset;
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point2D.Double getSurfaceOffset() {
		return this.surfaceOffset;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Surface<T> setSnapDistance(double distance) {
		this.snapDistance = distance;
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getSnapDistance() {
		return this.snapDistance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Surface<T> setUseIntegerPrecision(boolean b) {
		this.useIntegerPrecision = b;
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getUseIntegerPrecision() {
		return useIntegerPrecision;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Surface<T> setCornerClickRadius(double radius) {
		this.cornerClickRadius = radius;
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getCornerClickRadius() {
		return this.cornerClickRadius;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Surface<T> setDividerSize(double size) {
		this.dividerSize = size;
//		this.layoutVisitor.setDividerSize(size);
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getDividerSize() {
		return this.dividerSize;
	}
	
	
	public void cursorLeft() {//37
		if(nodeCursor == null) {
			nodeCursor = getRoot().stringID;
		}
		
		Node<T> currNode = interimLayoutSnapshot.getNode(nodeCursor);
		if(currNode != null && currNode.prevVertical != null) {
			nodeCursor = currNode.prevVertical.previousNodes().get(0).stringID;
		}
	}
	public void cursorUp() {//38
		if(nodeCursor == null) {
			nodeCursor = getRoot().stringID;
		}
		Node<T> currNode = interimLayoutSnapshot.getNode(nodeCursor);
		if(currNode != null && currNode.prevHorizontal != null) {
			nodeCursor = currNode.prevHorizontal.previousNodes().get(0).stringID;
		}
	}
	public void cursorRight() {//39
		if(nodeCursor == null) {
			nodeCursor = getRoot().stringID;
		}
		Node<T> currNode = interimLayoutSnapshot.getNode(nodeCursor);
		if(currNode != null && currNode.nextVertical != null) {
			nodeCursor = currNode.nextVertical.nextNodes().get(0).stringID;
		}
	}
	public void cursorDown() {//40
		if(nodeCursor == null) {
			nodeCursor = getRoot().stringID;
		}
		Node<T> currNode = interimLayoutSnapshot.getNode(nodeCursor);
		if(currNode != null && currNode.nextHorizontal != null) {
			nodeCursor = currNode.nextHorizontal.nextNodes().get(0).stringID;
		}
	}
	
	@JsonIgnore
	public String getCursor() {
		if(nodeCursor == null) {
			nodeCursor = getRoot().stringID;
		}
		return nodeCursor;
	}
	
	@JsonIgnore
	public void setCursor(String id) {
		this.nodeCursor = id;
	}
	
	/**
	 * User must call this method to receive layout events relevant to mouse drags
	 * such as {@link Divider} dragging and node resizing etc.
	 * 
	 * @param x		the x location of the user's mouse press.
	 * @param y		the y location of the user's mouse press.
	 */
	@Override
	public void mousePressed(double x, double y) {
		if(inputManager == null) {
			throw new IllegalStateException("Surface.mousePressed cannot be called before " +
				"adding the surface to a MosaicEngine");
		}
		List<Element<T>> elems = inputManager.findElements(x, y);
		inputManager.selectElement(elems, x, y);
	}
	
	/**
	 * User must call this method to receive layout events relevant to mouse drags
	 * such as {@link Divider} dragging and node resizing etc.
	 * 
	 * @param x		the x location of the user's mouse drag.
	 * @param y		the y location of the user's mouse drag.
	 */
	@Override
	public void mouseDragged(double x, double y) {
		isDragging = true;
		inputManager.dragElement(this, x, y);
	}
	
	/**
	 * User must call this method to receive layout events relevant to mouse releases.
	 */
	@Override
	public void mouseReleased() {
		isDragging = false;
		inputManager.releaseElement(this);
	}
	
}

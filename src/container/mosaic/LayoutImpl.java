package container.mosaic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Implements {@link Layout} to bulk-add specifications to a given {@link Surface}.
 * <p>
 * A Layout is considered relative if it is initialized using relative
 * constraints such as percentages of the overall area, or cardinal 
 * location specifiers (not yet implemented) such as NORTH, SOUTH, etc.
 * <p>
 * Conversely, an absolute layout specifies the location and sizes of
 * each "cell" specifically by pixel.
 * 
 * @author David Ray
 *
 */
public class LayoutImpl<T> implements Layout {
	
	
	@JsonProperty
	private boolean isRelative;
	
	@JsonProperty
	private List<String> cells = new ArrayList<String>();
	
	@JsonProperty
	private List<String> vDividers = new ArrayList<String>();
	
	@JsonProperty
	private List<String> hDividers = new ArrayList<String>();
	
	/** List of all horizontal Dividers */
    private List<Divider<T>> horizontalDividers = new ArrayList<Divider<T>>();
	
	/** List of all vertical Dividers */
    private List<Divider<T>> verticalDividers = new ArrayList<Divider<T>>();
	
	/** The list of Nodes */
	private List<Node<T>> nodeList = new ArrayList<Node<T>>();
	
	private Map<String, T> objectIDs = new LinkedHashMap<String, T>();
	private Map<T, String> typeObjectIDs = new LinkedHashMap<T, String>();
	
	/** Assembles/contains {@link PathIterator.Path} objects */ 
	private PathIterator<T> pathIterator;
	
	/** The root node */
	@JsonIgnore
	private Node<T> root;
	
	
	/**
	 * Used by JSON serialization mechanism
	 */
	@SuppressWarnings("unused")
	private LayoutImpl() {}
	
	
	/**
	 * Constructs a new {@link LayoutImpl} in the "mode"
	 * specified by the parameter isRelative which provides a hint 
	 * internally as to the type of measurements it can expect.
	 * <p>
	 * A Layout is considered relative if it is initialized using relative
	 * constraints such as percentages of the overall area, or cardinal 
	 * location specifiers (not yet implemented) such as NORTH, SOUTH, etc.
	 * <p>
	 * Conversely, an absolute layout specifies the location and sizes of
	 * each "cell" specifically by pixel.
	 * 
	 * @param isRelative	true if relative, false if absolute
	 */
	public LayoutImpl(boolean isRelative) {
		this.isRelative = isRelative;
	}
	
	/**
	 * Copy constructor. This constructor returns an identical
	 * copy of the specified {@link Layout}.
	 * 
	 * @param other {@link Layout} to copy
	 */
	public LayoutImpl(LayoutImpl<T> other) {
		this.isRelative = other.isRelative;
		this.objectIDs.putAll(other.objectIDs);
		this.typeObjectIDs.putAll(other.typeObjectIDs);
		
		for(Node<T> n : other.nodeList) {
			Node<T> newNode = new Node<T>(n);
			this.nodeList.add(newNode);
			if(other.root.stringID.equals(newNode.stringID)) {
				this.root = newNode;
			}
		}
		
		for(Divider<T> otherDivider : other.horizontalDividers) {
			Divider<T> thisDivider = new Divider<T>(otherDivider);
			for(Node<T> n : otherDivider.prevNodes) {
				Node<T> local = getNode(n.stringID);
				local.nextHorizontal = thisDivider;
				thisDivider.addPrevious(local);
			}
			for(Node<T> n : otherDivider.nextNodes) {
				Node<T> local = getNode(n.stringID);
				local.prevHorizontal = thisDivider;
				thisDivider.addNext(getNode(n.stringID));
			}
			this.horizontalDividers.add(thisDivider);
		}
		
		for(Divider<T> otherDivider : other.verticalDividers) {
			Divider<T> thisDivider = new Divider<T>(otherDivider);
			for(Node<T> n : otherDivider.prevNodes) {
				Node<T> local = getNode(n.stringID);
				local.nextVertical = thisDivider;
				thisDivider.addPrevious(getNode(n.stringID));
			}
			for(Node<T> n : otherDivider.nextNodes) {
				Node<T> local = getNode(n.stringID);
				local.prevVertical = thisDivider;
				thisDivider.addNext(getNode(n.stringID));
			}
			this.verticalDividers.add(thisDivider);
		}
		
		//Hookup Perpendicular Joins
		List<Divider<T>> aggregateList = new ArrayList<Divider<T>>(other.horizontalDividers);
		aggregateList.addAll(other.verticalDividers);
		for(Divider<T> d : aggregateList) {
			Divider<T> local = getDivider(d.stringID, d.isVertical);
			for(Divider<T> lj : d.leadingJoins) {
				Divider<T> localDivider = getDivider(lj.stringID, lj.isVertical);
				local.leadingJoins.add(localDivider);
				localDivider.trailingJoin = local;
			}
			for(Divider<T> tj : d.trailingJoins) {
				Divider<T> localDivider = getDivider(tj.stringID, tj.isVertical);
				local.trailingJoins.add(localDivider);
				localDivider.leadingJoin = local;
			}
		}
		
		//Copy the PathIterator
		this.pathIterator = new PathIterator<T>(this, other.pathIterator);
	}
	
	List<Node<T>> getNodes(List<String> ids) {
		Set<Node<T>> nodes = new HashSet<Node<T>>();
		for(String id : ids) {
			nodes.add(getNode(id));
		}
		return new ArrayList<Node<T>>(nodes);
	}
	
	List<Divider<T>> getDividers(List<String> ids, boolean isVertical) {
		Set<Divider<T>> divs = new HashSet<Divider<T>>();
		for(String id : ids) {
			divs.add(getDivider(id, isVertical));
		}
		return new ArrayList<Divider<T>>(divs);
	}
	
	/**
	 * Returns the root {@link Node} understood to be the {@code Node}
	 * at location (x=0, y=0). Called by the {@link MosaicEngineImpl} to
	 * obtain a reference to the origin Node.
	 * 
	 * @return	the root {@link Node}
	 */
	Node<T> getRoot() {
		return root;
	}
	
	/**
	 * Called from the engine remove logic to set the root node if
	 * the previous root node has been removed.
	 * 
	 * @param n 	the new root
	 */
	void setRoot(Node<T> n) {
		this.root = n;
	}
	
	
	/**
	 * Returns the {@link Node} whose string id matches
	 * the id specified.
	 * 
	 * @param id	the String id of the Node to return.
	 * @return		the {@link Node} whose string id matches
	 * 				the id specified.
	 */
	Node<T> getNode(String id) {
		for(Node<T> n : getNodeList()) {
			if(n.stringID.equals(id)) {
				return n;
			}
		}
		return null;
	}
	
	/**
	 * Returns the {@link Divider} whose String id matches
	 * the id specified, and whose orientation is as specified.
	 * 
	 * @param id			the String id of the Divider to return.
	 * @param isVertical	the orientation of the Divider to return.
	 * @return				the {@link Divider} whose String id matches
	 * 						the id specified
	 */
	Divider<T> getDivider(String id, boolean isVertical) {
		List<Divider<T>> searchList = isVertical ? getVerticalDividers() : getHorizontalDividers();
		for(Divider<T> d : searchList) {
			if(d.stringID.equals(id)) return d;
		}
		return null;
	}
	
	/**
	 * Returns the {@link PathIterator} specific to this {@code Layout}
	 * 
	 * @return		this {@code Layout}'s {@link PathIterator}.
	 */
	PathIterator<T> getPathIterator() {
		if(pathIterator == null) {
			pathIterator = new PathIterator<T>();
		}
		return pathIterator;
	}
	
	/**
	 * Returns a flag indicating whether this Layout is relative or not.
	 */
	@Override
	@JsonIgnore
	public boolean isRelative() {
		return this.isRelative;
	}
	
	@JsonIgnore
	void setRelative(boolean b) {
		this.isRelative = b;
	}
	
	/**
	 * Adds the mapping from the String "id" to the <T> specified
	 * 
	 * @param id	the Object id
	 * @param t		the <T>
	 */
	void put(String id, T t) {
		objectIDs.put(id, t);
		typeObjectIDs.put(t, id);
	}
	
	/**
	 * Returns the <T> object mapped to the specified key "id"
	 * or null.
	 * 
	 * @param id		the id key
	 * @return	<T>
	 */
	T get(String id) {
		return objectIDs.get(id);
	}
	
	/**
	 * Returns the String "id" mapped to the object <T>
	 * @param t		the type object 
	 * @return		the String "id"
	 */
	String get(T t) {
		return typeObjectIDs.get(t);
	}
	
	/**
	 * Removes all references to the objects and Nodes keyed
	 * by "id".
	 * 
	 * @param id
	 */
	void remove(String id) {
		typeObjectIDs.remove(objectIDs.remove(id));
		nodeList.remove(getNode(id));
		removeCell(id);
	}
	
	/**
	 * Returns the {@link Set} of String "ids"
	 * @return	the {@link Set} of String "ids"
	 */
	Set<String> stringKeySet() {
		return objectIDs.keySet();
	}
	
	/**
	 * Returns the {@link Set} of Objects <T>s
	 * @return	the {@link Set} of Objects <T>s
	 */
	Set<T> objectKeySet() {
		return typeObjectIDs.keySet();
	}
	
	/**
	 * Returns the list of {@link Node}s.
	 * 
	 * @return	the canonical list of {@link Node}s.
	 */
	List<Node<T>> getNodeList() {
		return nodeList;
	}
	
	/**
	 * Returns the list of horizontal {@link Divider}s.
	 * 
	 * @return	the list of horizontal {@link Divider}s.
	 */
	List<Divider<T>> getHorizontalDividers() {
		return horizontalDividers;
	}
	
	/**
	 * Returns the list of vertical {@link Divider}s.
	 * 
	 * @return	the list of vertical {@link Divider}s.
	 */
	List<Divider<T>> getVerticalDividers() {
		return verticalDividers;
	}
	
	/**
	 * Adds <em>ONLY</em> the cell definition strings contained in the
	 * specified {@link Layout} to this Layout's cell definitions so that
	 * this Layout's cell list results in a union between the specified layout's
	 * cells and this one's.
	 * 
	 * @param layout 	the {@link Layout} to merge cells with.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Layout add(Layout layout) {
		if((layout.isRelative() && !isRelative) || (!layout.isRelative() && isRelative)) {
			throw new IllegalArgumentException("Cannot add relative and absolute layout elements");
		}
		
		cells.addAll(((LayoutImpl<T>)layout).cells);
		return this;
	}
	
	@Override
	public Layout addCell(String id, double x, double y, double width, double height) {
		return addCell(id, x, y, width, height, 0, Double.MAX_VALUE, 0, Double.MAX_VALUE);
	}
	
	@Override
	public Layout addCell(String id, double x, double y, double width, double height,
		double minW, double maxW, double minH, double maxH) {
		
		if(checkIsAdded(id)) {
			throw new IllegalArgumentException("Attempt to add duplicate cell: " + id);
		}
		
		if(id == null) {
			throw new IllegalArgumentException("Can't add layout specification with an null id.");
		}
		
		if((x > 1 || y > 1 || width > 1 || height > 1) && isRelative) {
			throw new IllegalArgumentException("Relative locations represent percentages and must be between 0 and 1.");
		}
		
		if(checkIsAdded(id)) {
			throw new IllegalArgumentException("Cannot add a specification with the same id twice [" + id + "]");
		}
		
		cells.add(new StringBuilder(id).append(",").append(x).append(",").append(y).append(",").append(width).
			append(",").append(height).append(",").append(minW).append(",").append(maxW).append(",").append(minH).
			append(",").append(maxH).toString());
		
		return this;
	}
	
	Layout addCell(String id, double x, double y, double width, double height,
		double minW, double maxW, double minH, double maxH, double hWeight, double vWeight) {
		
		if(checkIsAdded(id)) {
			throw new IllegalArgumentException("Attempt to add duplicate cell: " + id);
		}
		
		if(id == null) {
			throw new IllegalArgumentException("Can't add layout specification with an null id.");
		}
		
		if((x > 1 || y > 1 || width > 1 || height > 1) && isRelative) {
			throw new IllegalArgumentException("Relative locations represent percentages and must be between 0 and 1.");
		}
		
		if(checkIsAdded(id)) {
			throw new IllegalArgumentException("Cannot add a specification with the same id twice [" + id + "]");
		}
		
		cells.add(new StringBuilder(id).append(",").append(x).append(",").append(y).append(",").append(width).
			append(",").append(height).append(",").append(minW).append(",").append(maxW).append(",").append(minH).
			append(",").append(maxH).append(",").append(hWeight).append(",").append(vWeight).toString());
		
		return this;
	}
	
	/**
	 * Removes the specified cell definition designated by "id".
	 * 
	 * @param id	the id of the 
	 * @return
	 */
	public boolean removeCell(String id) {
		for(ListIterator<String> li = cells.listIterator();li.hasNext();) {
			if(li.next().split(LayoutConstants.CELL_PTRN)[LayoutConstants.ID].equals(id)) {
				li.remove();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param id
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
	public boolean replaceOrAddCell(String id, double x, double y, double width, double height,
		double minW, double maxW, double minH, double maxH, double hWeight, double vWeight) {
		
		boolean cellPreExisted = removeCell(id);
		addCell(id, x, y, width, height, minW, maxW, minH, maxH, hWeight, vWeight);
		return cellPreExisted;
	}
	
	void addDivider(String id, boolean isVertical, double x, double y, double width , double height,
		String prevNodes, String nextNodes, String leadingJoins, String trailingJoins) {
		
		String dividerStr = new StringBuilder(id).append(",").append(x).append(",").append(y).append(",").append(width).
			append(",").append(height).append(",").append(prevNodes).append(",").append(nextNodes).append(",").
				append(leadingJoins).append(",").append(trailingJoins).toString();
		if(isVertical) {
			vDividers.add(dividerStr);
		}else{
			hDividers.add(dividerStr);
		}
	}
	
	boolean removeDivider(String id, boolean isVertical) {
		if(isVertical) {
			for(ListIterator<String> li = vDividers.listIterator();li.hasNext();) {
				if(li.next().split(LayoutConstants.CELL_PTRN)[LayoutConstants.ID].equals(id)) {
					li.remove();
					return true;
				}
			}
		}else{
			for(ListIterator<String> li = hDividers.listIterator();li.hasNext();) {
				if(li.next().split(LayoutConstants.CELL_PTRN)[LayoutConstants.ID].equals(id)) {
					li.remove();
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean replaceOrAddDivider(String id, boolean isVertical, double x, double y, double width , double height,
		String prevNodes, String nextNodes, String leadingJoins, String trailingJoins) {
		
		boolean divPreExisted = removeDivider(id, isVertical);
		addDivider(id, isVertical, x, y, width, height, prevNodes, nextNodes, leadingJoins, trailingJoins);
		return divPreExisted;
	}
	
	/**
	 * Returns the cell specification for the specified id if any 
	 * exists.
	 * 
	 * @param id	the id correlated to the returned specification string.
	 * @return		the comma-separated string representing the correlated layout.
	 */
	public String getCell(String id) {
		return getCellWithMatchingParameter(LayoutConstants.ID, id);
	}

	/**
	 * Returns the list of vertical {@link Divider}s represented in
	 * serializable (String) format.
	 * @return	 the list of {@link Divider} strings
	 */
	List<String> getSerializedVerticalDividers() {
		return vDividers;
	}
	
	/**
	 * Returns the list of horizontal {@link Divider}s represented in
	 * serializable (String) format.
	 * @return	 the list of {@link Divider} strings
	 */
	List<String> getSerializedHorizontalDividers() {
		return hDividers;
	}
	
	/**
	 * Returns true if the specified id exists in this {@link Layout} already.
	 * 
	 * @param id	the id to check for
	 * @return		true if already added, false if not.
	 */
	private boolean checkIsAdded(String id) {
		for(String s : cells) {
			if(s.split(LayoutConstants.CELL_PTRN)[LayoutConstants.ID].trim().equals(id)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the cell specification string for the parameter at the specified index
	 * which matches the given "param" argument.
	 *  
	 * @param index		one of the final index integer constants
	 * @param param		the parameter to match
	 * @return	the matching cell specification string
	 */
	String getCellWithMatchingParameter(int index, String param) {
		for(String s : cells) {
			if(s.split("[\\s,\\,]")[index].trim().equals(param)) {
				return s;
			}
		}
		return null;
	}
	
	/**
	 * Utility method to parse a cell definition for a 
	 * particular value.
	 * 
	 * @param cellStr				the codified string expressing cell contents.
	 * @param layoutConstant		the index in {@link LayoutConstants} of interest.
	 * @return	a particular value.
	 */
	String parse(String cellStr, int layoutConstant) {
		return cellStr.split(LayoutConstants.CELL_PTRN)[layoutConstant];
	}
	
	/**
	 * 
	 * @return
	 */
	List<String> getCells() {
		return cells;
	}
	
	void clearAll() {
		clearSerializableDefinitions();
		nodeList.clear();
		horizontalDividers.clear();
		verticalDividers.clear();
		pathIterator.clearAll();
		pathIterator = null;
	}
	
	/**
	 * Clears out any cell/Divider definitions.
	 */
	void clearSerializableDefinitions() {
		this.cells.clear();
		this.hDividers.clear();
		this.vDividers.clear();
	}
}

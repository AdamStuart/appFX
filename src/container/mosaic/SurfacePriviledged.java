package container.mosaic;

import java.awt.geom.Point2D;
import java.util.List;


abstract class SurfacePriviledged<T> implements Surface<T> {
	/**
	 * Sets the "locked" flag on this {@link SurfacePriviledged} in order to 
	 * indicate to clients that the current operation underway doesn't allow
	 * another operation until it is finished. Indicates that this {@link Surface} 
	 * is in the middle of a "transaction" or series of actions that cannot be
	 * interrupted nor can the {@link Surface} be interacted with until it's 
	 * current operations are complete.
	 * 
	 * @param b
	 * @see #setLocked(boolean)
	 * @see Surface#isLocked()
	 */
	abstract void setLocked(boolean b);
	/**
	 * Creates a copy that can be used for partial or total recreation
	 * of a layout as it exists at the time of this method being called.
	 */
	abstract void snapshotLayout();
	abstract LayoutImpl<T> getSnapshot();
	abstract void revertLayout();
	abstract void snapshotInterimLayout();
	abstract LayoutImpl<T> getInterimSnapshot();
	/**
	 * Sets the flag indicating whether a drag operation is occurring.
	 * 	
	 * @param b	the flag indicating whether a drag operation is occurring.
	 */
	abstract boolean getIsDragging();
	/**
	 * Called by the {@link MosaicEngine} during initial layout when
	 * constructing new Dividers, to determine when a Node is not far
	 * enough from the edge of the surface to create a new Divider.
	 * 
	 * @return	the configured boundary divider condition.
	 */
	abstract Point2D.Double getBoundaryDividerCondition();
	/**
	 * Returns the root {@link Node} understood to be the {@code Node}
	 * at location (x=0, y=0). Called by the {@link MosaicEngineImpl} to
	 * obtain a reference to the origin Node.
	 * 
	 * @return	the root {@link Node}
	 */
	abstract Node<T> getRoot();
	/**
	 * Called from the engine remove logic to set the root node if
	 * the previous root node has been removed.
	 * 
	 * @param n 	the new root
	 */
	abstract void setRoot(Node<T> n);
	/**
	 * Delegates to the {@link Layout} to retrieve the {@link Node}
	 * specified by "id".
	 * 
	 * @param id	the String id of the retrieved {@link Node}
	 * @return	the {@link Node} specified by "id".
	 */
	abstract Node<T> getNode(String id);
	/**
	 * Returns the list of {@link Node}s.
	 * 
	 * @return	the canonical list of {@link Node}s.
	 */
	abstract List<Node<T>> getNodeList();
	/**
	 * Returns the list of horizontal {@link Divider}s.
	 * 
	 * @return	the list of horizontal {@link Divider}s.
	 */
	abstract List<Divider<T>> getHorizontalDividers();
	/**
	 * Returns the list of vertical {@link Divider}s.
	 * 
	 * @return	the list of vertical {@link Divider}s.
	 */
	abstract List<Divider<T>> getVerticalDividers();
	/**
	 * Called by the engine to retrieve temporary search results previously stored here.
	 *  
	 * @param key	the Divider acting as key
	 * @return		the list of Divider search results.
	 */
	abstract List<Divider<T> >getSearchResults(Divider<T> d);
	/**
	 * Returns this {@code Surface}'s {@link PathIterator}.
	 * 
	 * @return	this {@code Surface}'s {@link PathIterator}.
	 */
	abstract PathIterator<T> getPathIterator();
	/**
	 * Called by the MosaicEngine to set itself as a reference once this 
	 * {@code Surface} is added to the engine.
	 * 
	 * @param engine
	 */
	abstract void setEngine(MosaicEngine<T> engine);
	/**
	 * Sets the init flag indicating first layout run.
	 * 
	 * @param b	true if is first layout run or before, false if not
	 */
	abstract void setIsInit(boolean b);
	/**
	 * Clears the previous cylce's search results.
	 */
	abstract void clearSearchResults();
	/**
	 * Called by the engine to temporarily store its search results for overlapping dividers
	 * @param d		the Divider acting as key
	 * @param l		the list of Divider search results.
	 */
	abstract void storeSearchResults(Divider<T> d, List<Divider<T>> l);
	/**
	 * Called by the engine to remove Node references and linked infrastructure.
	 * @param node	the node being removed.
	 */
	abstract void removeNodeReferences(Node<T> node);
	/**
	 * Signals the {@link Layout} to recreate its string representation in
	 * preparation for serialization.
	 * 
	 * @param isRelative	whether this Layout's current spec is relative or not.
	 */
	abstract void updateLayoutSerializables(boolean isRelative);
}

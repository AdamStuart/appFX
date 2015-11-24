package container.mosaic;

/**
 * Enum which accompanies all change notifications for the {@link MosaicEngine}
 * and describes the action to be taken by the user as a result of said change.
 * 
 * @author David Ray
 *
 */
public enum ChangeType {
	/** 
	 * Indicates that the specified object be resized and/or relocated 
	 * as distinguished from ANIMATE_RESIZE_RELOCATE which indicates that
	 * the transition from start to end bounds be animated.
	 */
	RESIZE_RELOCATE,
	/** 
	 * Indicates that the specified object be removed from its container
	 * but held onto possibly for later re-addition to the container such
	 * as in the case of a drag/move.
	 */
	REMOVE_RETAIN,
	/**
	 * Indicates that the specified object be removed from its container
	 * and that no further action will be needed, and may be discarded upon
	 * user descretion.
	 */
	REMOVE_DISCARD,
	/**
	 * Indicates that the specified object be resized and/or relocated
	 * and that the transition should be animated.
	 */
	ANIMATE_RESIZE_RELOCATE,
	
	ADD_TEST,
	ADD_COMMIT,
	ADD_REJECT,
	
	MOVE_BEGIN,
	MOVE_END,
	
	/**
	 * Refers to actions taken upon the "ghost" drag image or object representing
	 * the object being moved. The engine provides location and size related
	 * information dependant upon the original object size and requirements of
	 * any "drop location" it detects the target is over.
	 */
	RESIZE_DRAG_TARGET,
	/**
	 * Refers to actions taken upon the "ghost" drag image or object representing
	 * the object being moved. The engine provides location and size related
	 * information dependant upon the original object size and requirements of
	 * any "drop location" it detects the target is over.
	 */
	RELOCATE_DRAG_TARGET,
	/**
	 * Cursor change for cursor over a vertical divider
	 */
	CURSOR_OVER_DIVIDER_V,
	/**
	 * Cursor change for cursor grab of a vertical divider
	 */
	CURSOR_GRAB_DIVIDER_V,
	/**
	 * Cursor change for cursor over a horizontal divider
	 */
	CURSOR_OVER_DIVIDER_H,
	/**
	 * Cursor change for cursor grab of a horizontal divider
	 */
	CURSOR_GRAB_DIVIDER_H,
	/**
	 * Cursor change for cursor over the NW corner of an object
	 */
	CURSOR_OVER_CORNER_NW,
	/**
	 * Cursor change for cursor grab of the NW corner of an object
	 */
	CURSOR_GRAB_CORNER_NW,
	/**
	 * Cursor change for cursor over the NE corner of an object
	 */
	CURSOR_OVER_CORNER_NE,
	/**
	 * Cursor change for cursor grab of the NE corner of an object
	 */
	CURSOR_GRAB_CORNER_NE,
	/**
	 * Cursor change for cursor over the SW corner of an object
	 */
	CURSOR_OVER_CORNER_SW,
	/**
	 * Cursor change for cursor grab of the SW corner of an object
	 */
	CURSOR_GRAB_CORNER_SW,
	/**
	 * Cursor change for cursor over the SE corner of an object
	 */
	CURSOR_OVER_CORNER_SE,
	/**
	 * Cursor change for cursor grab of the SE corner of an object
	 */
	CURSOR_GRAB_CORNER_SE;
	
}

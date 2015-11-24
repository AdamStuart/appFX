package container.mosaic.refimpl.pivot;

import org.apache.pivot.wtk.Component;

/**
 * Implemented by clients interested in being notified when a component is added
 * to the Pivot framework. This is implemented by the {@link MosaicPane} in order
 * to relay the addXXX call to the layout engine.
 * 
 * @author David Ray
 */
public interface MosaicPaneListener {
	/**
	 * 
	 * @param added				the object being added
	 * @param id				the (optional) id to be used internally and for lookups.
	 * @param percentX			the percentage of the overall width, the x position is located at.
	 * @param percentY			the percentage of the overall height, the y position is located at.
	 * @param percentWidth		the percentage of the overall width the object should occupy.
	 * @param percentHeight		the percentage of the overall height the object should occupy.
	 */
	public void componentAdded(Component added, String id, double percentX, double percentY, double percentWidth, double percentHeight);
	
	public void serializedComponentAdded(Component added, String id);
}

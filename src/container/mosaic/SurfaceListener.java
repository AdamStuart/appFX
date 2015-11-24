package container.mosaic;

import java.awt.geom.Rectangle2D;

/**
 * Used in conjunction with the {@link MosaicEngineImpl} to notify clients when
 * the engine has determined that the bounds of a particular object T need to
 * be reset. This interface should be implemented by clients to receive notification
 * of size and location changes to the specified T previously added to the engine.
 * However, adding the actual object to be laid out is not absolutely necessary, T 
 * <i>could</i> be a key into a map of objects, etc. 
 * 
 * Because the {@link MosaicEngineImpl} is intended for use by any GUI framework, it cannot
 * know of any particular object type which can be laid out - therefore it cannot directly
 * change the size/location of an object because it isn't aware of any framework object's
 * particular interface. 
 * 
 * The engine is implemented using generics however, so that methods on the type T can
 * be called directly by client code in order to mutate their bounds without casting.
 * 
 * 
 * @author David Ray
 *
 * @param <T>	t 		the Type object being added.
 */
public interface SurfaceListener<T> {
	/**
	 * Method for handling the various types of user actions which can
	 * occur on a given {@link Surface}. The types are identified by the
	 * {@link ChangeType} passed in to this method, along with the corresponding
	 * ui element, the surface id and the old and new sizes - which should encompass
	 * all needed information required to make changes to a given ui container.
	 * 
	 * @param changeType		the type of change this event is notifying the listener of.
	 * @param uiElement			the Object of type &lt;T&gt; which requires updating
	 * @param id				the id of the ui element
	 * @param oldRectangle		the previous location and dimensions
	 * @param newRectangle		the new location and dimensions
	 */
    public void changed(ChangeType changeType, T uiElement, String id, Rectangle2D oldRectangle, Rectangle2D newRectangle);
}

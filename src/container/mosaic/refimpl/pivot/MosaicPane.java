package container.mosaic.refimpl.pivot;

import java.awt.geom.Rectangle2D;

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Mouse.Button;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.WTKListenerList;
import org.apache.pivot.wtk.effects.DropShadowDecorator;
import org.apache.pivot.wtk.effects.FadeDecorator;

import container.mosaic.ChangeType;
import container.mosaic.MosaicEngine;
import container.mosaic.MosaicEngineBuilder;
import container.mosaic.MosaicSurfaceBuilder;
import container.mosaic.Surface;
import container.mosaic.SurfaceListener;

/**
 * An implementation of a Pivot container which is 
 * set up to use in conjunction with the {@link MosaicEngineImpl}
 * 
 * @author David Ray
 */
public class MosaicPane extends Container implements MosaicPaneListener {
	
	private MosaicEngine<Component> layoutEngine;
	private Surface<Component> surface;
	private SurfaceListener<Component> listener;
	
	private FadeDecorator ghoster = new FadeDecorator(0.5f);
	private DropShadowDecorator shadower = new DropShadowDecorator(10, 10, 10);
	
	private MosaicPaneListenerList mosaicPaneListeners = new MosaicPaneListenerList();
	
	/**
	 * Constructs a new MosaicPane
	 */
	public MosaicPane() {
		this(null, null);
	}
	
	/**
	 * Constructs a new MosaicPane using the specified
	 * {@link MosaicEngine} and {@link Surface}. 
	 * 
	 *  This constructor is used to construct a pane containing the same engine
	 *  and surface definition as a previously configured pane for copying,
	 *  serialization etc.
	 * 
	 * @param engine	the layout engine
	 * @param surface 	the pre-configured surface
	 */
	public <T> MosaicPane(MosaicEngine<Component> engine, Surface<Component> surface) {
		Theme.getTheme().set(MosaicPane.class, MosaicPaneSkin.class); 
		installSkin(MosaicPane.class);
		
		if(engine == null || surface == null) {
			this.layoutEngine = new MosaicEngineBuilder<Component>().build();
			
			MosaicSurfaceBuilder<Component> builder = new MosaicSurfaceBuilder<Component>();
			this.surface = builder
				.useIntegerPrecision(true)
				.dividerSize(10)
				.cornerClickRadius(3)
				.build();
		}else{
			this.layoutEngine = engine;
			this.surface = surface;
		}
		
		listener = new SurfaceListener<Component> () {
		    public void changed(ChangeType changeType, Component c, String id, Rectangle2D r1, Rectangle2D r2) {
		    	switch(changeType) {
			    	case REMOVE_DISCARD: {
			    		remove(c);
			    		break;
			    	}
			    	case RESIZE_RELOCATE: {
			    		c.setLocation((int)r2.getX(), (int)r2.getY());
				        c.setSize((int)r2.getWidth(), (int)r2.getHeight());
				        
				        break;
			    	}
			    	case ADD_COMMIT: {
			    		add(c);
			    		c.setLocation((int)r2.getX(), (int)r2.getY());
				        c.setSize((int)r2.getWidth(), (int)r2.getHeight());
				        break;
			    	}
			    	case MOVE_BEGIN: {
			    		if(c.getDecorators().getLength() < 1) {
				    		c.getDecorators().add(ghoster);
				    		shadower.setShadowOpacity(0.3f);
				    		c.getDecorators().add(shadower);
			    		}
			    		c.setLocation((int)r2.getX(), (int)r2.getY());
				       
			    		//Move to top z-order
			    		if(indexOf(c) >= 0) {
			    			move(indexOf(c), getLength() - 1);
			    		}
				        break;
			    	}
			    	case RELOCATE_DRAG_TARGET: {
			    		c.setLocation((int)r2.getX(), (int)r2.getY());
			    		break;
			    	}
			    	case RESIZE_DRAG_TARGET: {
			    		c.setSize((int)r2.getWidth(), (int)r2.getHeight());
				        break;
			    	}
			    	case MOVE_END: {
			    		c.getDecorators().remove(ghoster);
			    		c.getDecorators().remove(shadower);
			    		repaint(c.getBounds());
			    		break;
			    	}
			    	case ANIMATE_RESIZE_RELOCATE: {
			    		c.setLocation((int)r2.getX(), (int)r2.getY());
				        c.setSize((int)r2.getWidth(), (int)r2.getHeight());
				        repaint();
				        break;
			    	}
				default:
					break;
		    	}
		    }
		};
		
		this.surface.addChangeListener(listener);
		
		getMosaicPaneListeners().add(this);
		getContainerMouseListeners().add(new ContainerMouseListener.Adapter() {
			@Override
			public boolean mouseDown(Container arg0, Button arg1, int x, int y) {
				if(arg1.equals(Mouse.Button.RIGHT)) {
					Component c = arg0.getComponentAt(x, y);
					MosaicPane.this.surface.requestRemove(c);
					return false;
				}
				
				requestFocus();
				
				MosaicPane.this.surface.mousePressed(x, y);
				return false;
			}

			@Override
			public boolean mouseUp(Container arg0, Button arg1, int arg2, int arg3) {
				MosaicPane.this.surface.mouseReleased();
				System.out.println("mouse released");
				return false;
			}
			
			@Override
			public boolean mouseMove(Container container, int x, int y) {
				if(!Mouse.isPressed(Mouse.Button.LEFT)) return false;
				
				MosaicPane.this.surface.mouseDragged(x, y);
				return false;
			}
		});
	}
	
	@Override
	public void layout() {
		super.layout();
		surface.setArea(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
		surface.requestLayout();
	}
	
	/**
	 * Implementation of the {@link MosaicPaneListener} interface which relays calls
	 * to add the specified component to the underlying engine.
	 * 
	 * @param added				the object to be laid out or key to such.
	 * @param id				the id to be used in reference to the added object
	 * @param percentX			the percentage of the overall width, the x position is located at.
	 * @param percentY			the percentage of the overall height, the y position is located at.
	 * @param percentWidth		the percentage of the overall width the object should occupy.
	 * @param percentHeight		the percentage of the overall height the object should occupy.  
	 */
	@Override
	public void componentAdded(Component added, String id, double percentX, double percentY, double percentWidth, double percentHeight) {
		invalidate();
		
		surface.addRelative(id, added, percentX, percentY, percentWidth, percentHeight, 0, Double.MAX_VALUE, 0, Double.MAX_VALUE);
	}
	@Override
	public void serializedComponentAdded(Component added, String id) {
		invalidate();
		
		surface.add(id, added);
	}
	
	@Override
	public boolean isFocusable() {
		return true;
	}
	
	/**
	 * List of {@link MosaicPaneListener}s which are notified when
	 * components are added.
	 */
	private static class MosaicPaneListenerList extends WTKListenerList<MosaicPaneListener> implements MosaicPaneListener {
		@Override
		public void componentAdded(Component added, String id, double percentX, double percentY, double percentWidth, double percentHeight) {
			for(MosaicPaneListener listener : this) {
				listener.componentAdded(added, id, percentX, percentY, percentWidth, percentHeight);
			}
		}
		@Override
		public void serializedComponentAdded(Component added, String id) {
			for(MosaicPaneListener listener : this) {
				listener.serializedComponentAdded(added, id);
			}
		}
	}
	
	/**
	 * Called to add an object to be laid out, to the layout engine.
	 * 
	 * @param componentToAdd	the object to be laid out or key to such.
	 * @param id				the user-determined label/id.
	 * @param percentX			the percentage of the overall width, the x position is located at.
	 * @param percentY			the percentage of the overall height, the y position is located at.
	 * @param percentWidth		the percentage of the overall width the object should occupy.
	 * @param percentHeight		the percentage of the overall height the object should occupy.
	 */
	public void add(Component componentToAdd, String id, double percentX, double percentY, double percentWidth, double percentHeight) {
		add(componentToAdd);
		
		mosaicPaneListeners.componentAdded(componentToAdd, id, percentX, percentY, percentWidth, percentHeight);
	}
	
	/**
	 * 
	 * @param componentToAdd
	 * @param id
	 */
	public void addSerialized(Component componentToAdd, String id) {
		add(componentToAdd);
		
		mosaicPaneListeners.serializedComponentAdded(componentToAdd, id);
	}
	
	/**
	 * Returns the list of {@link MosaicPane} listeners.
	 * 
	 * @return		the list of {@link MosaicPane} listeners.
	 */
	public ListenerList<MosaicPaneListener> getMosaicPaneListeners() {
		return mosaicPaneListeners;
	}
	
	/**
	 * Returns a reference to the {@link MosaicEngineImpl} currently
	 * being used.
	 * 	
	 * @return	a reference to the engine.
	 */
	public MosaicEngine<Component> getEngine() {
		return layoutEngine;
	}
	
	/**
	 * Returns the configured {@link Surface}
	 * 
	 * @return	the configured {@link Surface}
	 */
	public Surface<Component> getSurface() {
		return surface;
	}
	
}

package container.mosaic.refimpl.javafx;

import java.awt.geom.Rectangle2D;

import container.mosaic.ChangeType;
import container.mosaic.MosaicEngine;
import container.mosaic.MosaicEngineBuilder;
import container.mosaic.MosaicSurfaceBuilder;
import container.mosaic.Surface;
import container.mosaic.SurfaceListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * An implementation of a container object which is meant to be used
 * with the {@link MosaicEngineImpl}.
 * 
 * @author David Ray
 *
 * @param <T>
 */
public class MosaicPane<T extends Node> extends Region {
	private MosaicEngine<T> layoutEngine;
	private Surface<T> surface;
	private Group content;
	
	/**
	 * Constructs a new {@code MosaicPane}
	 */
	public MosaicPane() {
		this(null, null, null);
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
	 * @param group		the {@link Group} containing ui elements
	 */
	public MosaicPane(MosaicEngine<T> engine, Surface<T> surface, Group group) {
		if(engine == null || surface == null) {
			this.layoutEngine = new MosaicEngineBuilder<T>().build();
			
			MosaicSurfaceBuilder<T> builder = new MosaicSurfaceBuilder<T>();
			this.surface = builder
				.useIntegerPrecision(false)
				.cornerClickRadius(5)
				.useSurfaceOffset(false)
				.dividerSize(10)
				.snapDistance(15).build();
			
			this.surface.addChangeListener(getSurfaceObserver());
			
			content = new Group();
	        content.setManaged(false);
	        getChildren().add(content);
	    }else{
			this.layoutEngine = engine;
			this.surface = surface;
			this.content = group;
		}
		
		layoutBoundsProperty().addListener(new ChangeListener<Bounds>() {
        	@Override
			public void changed(ObservableValue<? extends Bounds> arg0, Bounds arg1, Bounds arg2) {
        		if(arg2.getWidth() == 0 || arg2.getHeight() == 0) return;
				MosaicPane.this.surface.setArea(new Rectangle2D.Double(0, 0, arg2.getWidth(), arg2.getHeight()));
				MosaicPane.this.surface.requestLayout();
			}
        	
        });
        
        addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>() {
        	@Override
			public void handle(MouseEvent evt) {
        		if(evt.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
        			MosaicPane.this.surface.mousePressed(evt.getX(), evt.getY());
				}else if(evt.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
					MosaicPane.this.surface.mouseDragged(evt.getX(), evt.getY());
				}else if(evt.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
					MosaicPane.this.surface.mouseReleased();
				}
			}
        });
	}
	
	/**
	 * Called to add an object to be laid out, to the layout engine.
	 * 
	 * @param t					the object to be laid out or key to such.
	 * @param percentX			the percentage of the overall width, the x position is located at.
	 * @param percentY			the percentage of the overall height, the y position is located at.
	 * @param percentWidth		the percentage of the overall width the object should occupy.
	 * @param percentHeight		the percentage of the overall height the object should occupy.
	 */
	public void add(T t, double percentX, double percentY, double percentWidth, double percentHeight) {
		surface.addRelative("", t, percentX, percentY, percentWidth, percentHeight, 0, Double.MAX_VALUE, 0, Double.MAX_VALUE);
		content.getChildren().add(t);
	}
	
	/**
	 * Called to add an object to be laid out, to the layout engine applying the specified
	 * String id.
	 *  
	 * @param t					the object to be laid out or key to such.
	 * @param id				the user-specified String id.
	 * @param percentX			the percentage of the overall width, the x position is located at.
	 * @param percentY			the percentage of the overall height, the y position is located at.
	 * @param percentWidth		the percentage of the overall width the object should occupy.
	 * @param percentHeight		the percentage of the overall height the object should occupy.
	 */
	public void add(T t, String id, double percentX, double percentY, double percentWidth, double percentHeight) {
		surface.addRelative(id, t, percentX, percentY, percentWidth, percentHeight, 0, Double.MAX_VALUE, 0, Double.MAX_VALUE);
		content.getChildren().add(t);
	}
	
	public MosaicEngine<T> getEngine() {
		return layoutEngine;
	}
	
	public Surface<T> getSurface() {
		return surface;
	}
	
	public SurfaceListener<T> getSurfaceObserver() {
		SurfaceListener<T> l = new SurfaceListener<T>() {
			public void changed(ChangeType changeType, Node n, String id, Rectangle2D r1, Rectangle2D r2) {
				switch(changeType) {
			    	case REMOVE_DISCARD: {
			    		content.getChildren().remove(n);
			    		requestLayout();
			    		break;
			    	}
			    	case RESIZE_RELOCATE: {
			    		n.resizeRelocate(r2.getX(), r2.getY(), r2.getWidth(), r2.getHeight());
						requestLayout();
				        
				        break;
			    	}
			    	case ADD_COMMIT: {
			    		content.getChildren().add(n);
			    		n.resizeRelocate(r2.getX(), r2.getY(), r2.getWidth(), r2.getHeight());
						requestLayout();
				        break;
			    	}
			    	case MOVE_BEGIN: {
			    		DropShadow shadow = new DropShadow();
			    		shadow.setOffsetX(10);
			    		shadow.setOffsetY(10);
			    		shadow.setRadius(5);
			    		shadow.setColor(Color.GRAY);
			    		n.setEffect(shadow);
			    		n.toFront();
			    		n.setOpacity(.5);
			    		break;
			    	}
			    	case RELOCATE_DRAG_TARGET: {
			    		n.resizeRelocate(r2.getX(), r2.getY(), r2.getWidth(), r2.getHeight());
						requestLayout();
			    		break;
			    	}
			    	case RESIZE_DRAG_TARGET: {
			    		n.resizeRelocate(r2.getX(), r2.getY(), r2.getWidth(), r2.getHeight());
						requestLayout();
				        break;
			    	}
			    	case MOVE_END: {
			    		n.setOpacity(1);
			    		n.setEffect(null);
			    		break;
			    	}
			    	case ANIMATE_RESIZE_RELOCATE: {
			    		n.resizeRelocate(r2.getX(), r2.getY(), r2.getWidth(), r2.getHeight());
						requestLayout();
				        break;
			    	}
			    	default: break;
		    	}
			}
		};
		return l;
	}
}

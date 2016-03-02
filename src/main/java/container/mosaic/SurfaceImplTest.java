package container.mosaic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.geom.Rectangle2D;

import org.junit.Test;


public class SurfaceImplTest {

	@Test
		public void testAddLayoutContentsAbsolute() {
			//Test normal adding
			Surface<Object> si = new SurfaceImpl<Object>();
			si = si.addAbsolute("test", new Object(), 0, 0, 0, 0, 0, 0 , 0 , 0);
			assertNotNull(si);
			
			//Test abnormal adding of absolute to a surface in relative mode
			si = new SurfaceImpl<Object>();
			si = si.addRelative("test", new Object(), 0, 0, 0, 0, 0, 0 , 0 , 0);
			assertNotNull(si);
			try {
				si.addAbsolute("test2", new Object(), 0, 0, 0, 0, 0, 0 , 0 , 0);
				fail();
			}catch(Exception e) {
				assertEquals("Cannot add absolute once a relative specification has been added.", e.getMessage());
			}
		}
	
	@Test
		public void testAddLayoutContentsRelative() {
			//Test normal adding
			Surface<Object> si = new SurfaceImpl<Object>();
			si = si.addRelative("test", new Object(), 0, 0, 0, 0, 0, 0 , 0 , 0);
			assertNotNull(si);
			
			//Test abnormal adding of relative to a surface in absolute mode
			si = new SurfaceImpl<Object>();
			si = si.addAbsolute("test", new Object(), 0, 0, 0, 0, 0, 0 , 0 , 0);
			assertNotNull(si);
			try {
				si.addRelative("test2", new Object(), 0, 0, 0, 0, 0, 0 , 0 , 0);
				fail();
			}catch(Exception e) {
				assertEquals("Cannot add relative once an absolute specification has been added.", e.getMessage());
			}
			
			
		}
	
	@Test
	public void testSerialize() {
		MosaicEngine<Object> engine = new MosaicEngineImpl<Object>();
		
		Surface<Object> si = new SurfaceImpl<Object>();
		si = si.addAbsolute("test", new Object(), 0, 0, .125, .250, 0, 0, 0, 0);
		((SurfaceImpl<Object>)si).setSnapDistance(6.7);
		((SurfaceImpl<Object>)si).setArea(new Rectangle2D.Double(4.0, 4.1, 4.2, 4.3));
		assertNotNull(si);
		
		engine.addSurface(si);
		
		String serial = ((SurfaceImpl<Object>)si).serialize();
		
		SurfaceImpl<?> resurfaced = ((SurfaceImpl<Object>)si).deSerialize(serial);
		assertNotNull(resurfaced);
		assertEquals(6.7d, resurfaced.getSnapDistance(), 0.0);
		assertEquals(new Rectangle2D.Double(4.0, 4.1, 4.2, 4.3), resurfaced.getArea());
		assertTrue(!resurfaced.getLayout().isRelative());
		
		Layout l = resurfaced.getLayout();
		String cell = l.getCell("test");
		assertNotNull(cell);
		
		String[] cells = cell.split(LayoutConstants.CELL_PTRN);
		assertEquals(11, cells.length);
		assertEquals("test", cells[LayoutConstants.ID]);
	}
}

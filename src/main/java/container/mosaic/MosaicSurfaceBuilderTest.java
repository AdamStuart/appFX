package container.mosaic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class MosaicSurfaceBuilderTest {

	@Test
	public void testMandatorySettingsSet() {
		try {
			MosaicSurfaceBuilder<Object> builder = new MosaicSurfaceBuilder<Object>();
			builder.build();
			fail("Must check for mandatory settings");
		}catch(IllegalArgumentException i) {
			assertEquals("Corner click radius unset. (reasonable value is 3)", i.getMessage());
		}catch(Exception e) {
			if(!(e instanceof IllegalStateException)) {
				fail("Wrong exception type thrown");
			}
		}
		
		
		try {
			MosaicSurfaceBuilder<Object> builder = new MosaicSurfaceBuilder<Object>();
			builder.cornerClickRadius(3).build();
			fail("Must check for mandatory settings");
		}catch(IllegalStateException i) {
			assertEquals("Divider size unset. (reasonable value is 10)", i.getMessage());
		}catch(Exception e) {
			if(!(e instanceof IllegalStateException)) {
				fail("Wrong exception type thrown");
			}
		}
	}

}

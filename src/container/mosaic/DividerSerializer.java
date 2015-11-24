package container.mosaic;

import java.awt.geom.Rectangle2D;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class DividerSerializer extends JsonSerializer<Rectangle2D.Double> {
	@Override
	public void serialize(Rectangle2D.Double value, JsonGenerator jgen, SerializerProvider provider) 
		throws IOException {
		jgen.writeStartObject();
		jgen.writeStringField(LayoutConstants.KEY_DIVIDER_BOUNDS, value.x + "," + value.y + "," + value.width + "," + value.height);
		jgen.writeEndObject();
	}
}


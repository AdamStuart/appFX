package container.mosaic;

import java.awt.geom.Rectangle2D;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class DividerDeserializer extends JsonDeserializer<Rectangle2D.Double> {
	@Override
    public Rectangle2D.Double deserialize(JsonParser jsonParser, 
        DeserializationContext deserializationContext) throws IOException {
		
		ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        
        String[] params = node.get(LayoutConstants.KEY_DIVIDER_BOUNDS).asText().split(LayoutConstants.CELL_PTRN);
        return new Rectangle2D.Double(
        	Double.parseDouble(params[LayoutConstants.X - 1]),
        	Double.parseDouble(params[LayoutConstants.Y - 1]),
        	Double.parseDouble(params[LayoutConstants.W - 1]),
        	Double.parseDouble(params[LayoutConstants.H - 1]));
	}
}

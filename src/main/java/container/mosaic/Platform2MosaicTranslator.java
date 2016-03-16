package container.mosaic;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Platform2MosaicTranslator {
	private static final String V = "VERTICAL";
	
	public static PlatformLayoutMap[] readFlexLayout(String json) {
		PlatformLayoutMap[] map = null;
		try {
			ObjectMapper om = new ObjectMapper();
			JsonNode root = om.readTree(json);
			JsonNode layoutNode = root.findValue("layout");
			if(layoutNode == null) {
				throw new IllegalArgumentException(
					"JSON string passed in did not contain layout node, or was not a complete json object.");
			}
			
			String processedString = processEscapesSequences(layoutNode.toString());
			System.out.println(processedString);
			map = om.readValue(processedString, PlatformLayoutMap[].class);
			for(PlatformLayoutMap pm : map) {
				System.out.println(pm.getName());
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return map;
	}
	
	public static <T> Layout toMosaicLayout(PlatformLayoutMap[] map) { 
		LayoutImpl<T> layout = new LayoutImpl<T>(false);
		
		if(map.length == 1) {
			layout.setRelative(false);
			
			Node<T> root = new Node<T>(null, map[0].getTopLeftPluginID(), 0d, 0d, 400d, 400d, 
				0d, Double.MAX_VALUE, 0d, Double.MAX_VALUE, 1.0d, 1.0d);
			
			layout.setRoot(root);
			
			return layout;
		}else {
			Node<T> root = gn(map, map[0]);
			System.out.println("root = " + root);
		}
		
		return layout;
	}
	
	private static <T> Node<T> makeNode(String id) {
		return new Node<T>(null, id, 0d, 0d, 0d, 0d, 
			0d, Double.MAX_VALUE, 0d, Double.MAX_VALUE, 0d, 0d);
	}
	
	private static <T> Divider<T> makeDivider(boolean isVertical) {
		return new Divider<T>(0, 0, 0, isVertical);
	}
	
	private static <T> Node<T> gn(PlatformLayoutMap[] array, PlatformLayoutMap parent) {
		boolean isVert = parent.getOrientation().equals(V);
		
		Divider<T> thisDiv = makeDivider(!isVert);
		
		Node<T> leftTopNode = null;
		Node<T> bottomRightNode = null;
		
		if(isVert) {
			PlatformLayoutMap topLeft = null;
			if(parent.getTopLeftPluginID() != null) {
				leftTopNode = makeNode(parent.getTopLeftPluginID());
			}else{
				topLeft = getMapSide(array, parent, PlatformLayoutMap.Side.TOPLEFT);
				leftTopNode = gn(array, topLeft);
			}
			
			PlatformLayoutMap bottomRight = null;
			if(parent.getBottomRightPluginID() != null) {
				bottomRightNode = makeNode(parent.getBottomRightPluginID());
			}else{
				bottomRight = getMapSide(array, parent, PlatformLayoutMap.Side.BOTTOMRIGHT);
				bottomRightNode = gn(array, bottomRight);
			}
			
			//leftTopNode is nested branch
			if(topLeft != null) {
				if(topLeft.getOrientation().equals(V)) {
					leftTopNode.nextHorizontal.nextNodes.get(0).nextHorizontal = thisDiv;
					thisDiv.addPrevious(leftTopNode.nextHorizontal.nextNodes.get(0));
				}else{
					leftTopNode.nextHorizontal = thisDiv;
					thisDiv.addPrevious(leftTopNode);
					
					Node<T> nextHoriz = leftTopNode;
					while(nextHoriz.nextVertical != null) {
						if(nextHoriz.nextVertical.nextNodes.size() > 0) {
							nextHoriz.nextVertical.nextNodes.get(0).nextHorizontal = thisDiv; 
							thisDiv.addPrevious(nextHoriz.nextVertical.nextNodes.get(0));
							thisDiv.addPerpendicularJoin(nextHoriz.nextVertical, true);
							
							nextHoriz = nextHoriz.nextVertical.nextNodes.get(0);
						}
					}
				}
			}else{
				leftTopNode.nextHorizontal = thisDiv;
				thisDiv.addPrevious(leftTopNode);
			}
			
			//bottomRightNode is nested branch
			if(bottomRight != null) {
				if(bottomRight.getOrientation().equals(V)) {
					bottomRightNode.prevHorizontal = thisDiv;
					thisDiv.addNext(bottomRightNode);
				}else{
					bottomRightNode.prevHorizontal = thisDiv;
					thisDiv.addNext(bottomRightNode);
					
					Node<T> nextHoriz = bottomRightNode;
					while(nextHoriz.nextVertical != null) {
						if(nextHoriz.nextVertical.nextNodes.size() > 0) {
							nextHoriz.nextVertical.nextNodes.get(0).prevHorizontal = thisDiv; 
							thisDiv.addNext(nextHoriz.nextVertical.nextNodes.get(0));
							thisDiv.addPerpendicularJoin(nextHoriz.nextVertical, false);
							
							nextHoriz = nextHoriz.nextVertical.nextNodes.get(0);
						}
					}
				}
			}else{
				bottomRightNode.prevHorizontal = thisDiv;
				thisDiv.addNext(bottomRightNode);
			}
		}else{
			PlatformLayoutMap topLeft = null;
			if(parent.getTopLeftPluginID() != null) {
				leftTopNode = makeNode(parent.getTopLeftPluginID());
			}else{
				topLeft = getMapSide(array, parent, PlatformLayoutMap.Side.TOPLEFT);
				leftTopNode = gn(array, topLeft);
			}
			
			PlatformLayoutMap bottomRight = null;
			if(parent.getBottomRightPluginID() != null) {
				bottomRightNode = makeNode(parent.getBottomRightPluginID());
			}else{
				bottomRight = getMapSide(array, parent, PlatformLayoutMap.Side.BOTTOMRIGHT);
				
				// EXIT CASE:
				// Special Case - Master always has a panel on the left and none on the right
				if(bottomRight == null && parent.getParentName().equals("MASTER")) {
					return leftTopNode;
				}
				
				bottomRightNode = gn(array, bottomRight);
			}
			
			//leftTopNode is nested branch
			if(topLeft != null) {
				if(topLeft.getOrientation().equals(V)) {
					leftTopNode.nextVertical = thisDiv;
					thisDiv.addPrevious(leftTopNode);
					
					Node<T> nextVert = leftTopNode;
					while(nextVert.nextHorizontal != null) {
						if(nextVert.nextHorizontal.nextNodes.size() > 0) {
							nextVert.nextHorizontal.nextNodes.get(0).nextVertical = thisDiv; 
							thisDiv.addPrevious(nextVert.nextHorizontal.nextNodes.get(0));
							thisDiv.addPerpendicularJoin(nextVert.nextHorizontal, true);
							
							nextVert = nextVert.nextHorizontal.nextNodes.get(0);
						}
					}
				}else{
					leftTopNode.nextVertical.nextNodes.get(0).nextVertical = thisDiv;
					thisDiv.addPrevious(leftTopNode.nextVertical.nextNodes.get(0));
				}
			}else{
				leftTopNode.nextVertical = thisDiv;
				thisDiv.addPrevious(leftTopNode);
			}
			
			//bottomRightNode is nested branch
			if(bottomRight != null) {
				if(bottomRight.getOrientation().equals(V)) {
					bottomRightNode.prevVertical = thisDiv;
					thisDiv.addNext(bottomRightNode);
					
					Node<T> nextVert = bottomRightNode;
					while(nextVert.nextHorizontal != null) {
						if(nextVert.nextHorizontal.nextNodes.size() > 0) {
							nextVert.nextHorizontal.nextNodes.get(0).prevVertical = thisDiv; 
							thisDiv.addNext(nextVert.nextHorizontal.nextNodes.get(0));
							thisDiv.addPerpendicularJoin(nextVert.nextHorizontal, false);
							
							nextVert = nextVert.nextHorizontal.nextNodes.get(0);
						}
					}
				}else{
					bottomRightNode.prevVertical = thisDiv;
					thisDiv.addNext(bottomRightNode);
				}
			}else{
				bottomRightNode.prevVertical = thisDiv;
				thisDiv.addNext(bottomRightNode);
			}
		}
		
		return leftTopNode;
	}
	
	private static PlatformLayoutMap getMapSide(PlatformLayoutMap[] array, PlatformLayoutMap parent, PlatformLayoutMap.Side side) {
		List<PlatformLayoutMap> children = getChildren(array, parent);
		for(PlatformLayoutMap plm : children) {
			if(plm.getLocationInParent().equals(side)) {
				return plm;
			}
		}
		
		return null;
	}
	
	private static List<PlatformLayoutMap> getChildren(PlatformLayoutMap[] map, PlatformLayoutMap parent) {
		List<PlatformLayoutMap> l = new ArrayList<PlatformLayoutMap>();
		for(PlatformLayoutMap plm : map) {
			if(plm.getParentName().equals(parent.getName())) {
				l.add(plm);
			}
		}
		return l;
	}
	
	private static String processEscapesSequences(String escapedJson) {
		escapedJson = escapedJson.replace("\\\"", "\"");
		escapedJson = escapedJson.replace("\\n", "\n");
		
		//clip off first " \" " for some Jackson related reasonny goodness??
		escapedJson = escapedJson.substring(1);
		
		//clip off last " \" " for some Jackson related reasonny goodness??
		escapedJson = escapedJson.substring(0, escapedJson.length() - 1);
		
		return escapedJson;
	}
	
	
}

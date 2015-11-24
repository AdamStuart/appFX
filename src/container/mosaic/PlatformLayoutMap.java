package container.mosaic;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlatformLayoutMap {
	public enum Side { TOPLEFT, BOTTOMRIGHT };
	

	@JsonProperty("location")
	private Side locationInParent;

	@JsonProperty("name_root")
	private String parentName;

	@JsonProperty("name_this")
	private String name;

	@JsonProperty("splitter")
	private float splitterValue;

	@JsonProperty("orientation")
	private String orientation;

	@JsonProperty("plugin_id_0_0")
	private String topLeftPluginID;

	@JsonProperty("plugin_id_1_1")
	private String bottomRightPluginID;

	@JsonProperty("plugin_name_0_0")
	private String topLeftPluginFactoryName;

	@JsonProperty("plugin_name_1_1")
	private String bottomRightPluginFactoryName;

	
	public void setLocationInParent(final Side locationInParent) {
		this.locationInParent = locationInParent;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Side getLocationInParent() {
		return locationInParent;
	}

	public void setParentName(final String parentName) {
		this.parentName = parentName;
	}

	public String getParentName() {
		return parentName;
	}
	
	public void setSplitterValue(final float splitterValue) {
		this.splitterValue = splitterValue;
	}

	public float getSplitterValue() {
		return splitterValue;
	}

	public void setOrientation(final String orientation) {
		this.orientation = orientation;
	}

	public String getOrientation() {
		return orientation;
	}

	public void setTopLeftPluginID(final String topLeftPluginID) {
		this.topLeftPluginID = topLeftPluginID;
	}

	public String getTopLeftPluginID() {
		return topLeftPluginID;
	}

	public void setBottomRightPluginID(final String bottomRightPluginID) {
		this.bottomRightPluginID = bottomRightPluginID;
	}

	public String getBottomRightPluginID() {
		return bottomRightPluginID;
	}

	public void setTopLeftPluginFactoryName(
			final String topLeftPluginFactoryName) {
		this.topLeftPluginFactoryName = topLeftPluginFactoryName;
	}

	public String getTopLeftPluginFactoryName() {
		return topLeftPluginFactoryName;
	}

	public void setBottomRightPluginFactoryName(
			final String bottomRightPluginFactoryName) {
		this.bottomRightPluginFactoryName = bottomRightPluginFactoryName;
	}

	public String getBottomRightPluginFactoryName() {
		return bottomRightPluginFactoryName;
	}
}

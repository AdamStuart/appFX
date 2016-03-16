/*
 * Copyright 2014 michael-simons.eu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package container.bikingFX.bikes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

import static java.time.Instant.ofEpochMilli;
import static java.time.ZoneId.systemDefault;
import static javax.json.JsonValue.ValueType.NULL;

/**
 * Represents an updateable Bike in my collection of bikes.
 *
 * @author Michael J. Simons, 2014-10-16
 */
public class Bike {

    /** ID is needed for URL construction */
    private final Property<Integer> id;
    private final Property<String> name;
    private final Property<Color> color;
    private final Property<LocalDate> boughtOn;
    private final Property<LocalDate> decommissionedOn;
    private final Property<Integer> mileage;

    public Bike(final JsonValue jsonValue) {
		final JsonObject jsonObject = (JsonObject) jsonValue;
	
		id = new ReadOnlyObjectWrapper<>(this, "id", jsonObject.getInt("id"));
		name = new ReadOnlyObjectWrapper<>(this, "name", jsonObject.getString("name"));
		color = new SimpleObjectProperty<>(this, "color", Color.web(jsonObject.getString("color")));
		boughtOn = new ReadOnlyObjectWrapper<>(this, "boughtOn", LocalDateTime.ofInstant(ofEpochMilli(jsonObject.getJsonNumber("boughtOn").longValue()), systemDefault()).toLocalDate());
		final JsonValue decom = jsonObject.get("decommissionedOn");
		decommissionedOn = new SimpleObjectProperty<>(this, "decommissionedOn", decom.getValueType() == NULL 
						? null : LocalDateTime.ofInstant(ofEpochMilli(((JsonNumber) decom).longValue()), systemDefault()).toLocalDate());
		final JsonValue mileage = jsonObject.get("lastMilage");
		this.mileage = new SimpleObjectProperty<>(this, "milage", mileage.getValueType() == NULL ? 0 : ((JsonNumber) mileage).intValue());
	    }
    
    public final Integer getId() {	return id.getValue();    }
    public Property<Integer> propertyId() {	return id;    }

    public final String getName() {	return name.getValue();    }
    public Property<String> nameProperty() {	return name;    }

    public final Color getColor() {	return color.getValue();    }
    public final void setColor(Color color) {	this.color.setValue(color);    }
    public Property<Color> colorProperty() {	return color;    }
    
    public final LocalDate getBoughtOn() {	return boughtOn.getValue();    }
    public Property<LocalDate> boughtOnProperty() {	return boughtOn;    }

    public final LocalDate getDecommissionedOn() {	return decommissionedOn.getValue();    }
    public final void setDecommissionedOn(LocalDate decommissionedOn) {	this.decommissionedOn.setValue(decommissionedOn);    }
    public Property<LocalDate> decommissionedOnProperty() {	return decommissionedOn;    }

    public final Integer getMileage() {	return mileage.getValue();    }
    public final void setMileage(Integer mileage) {	this.mileage.setValue(mileage);    }
    public Property<Integer> mileageProperty() {	return mileage;    }

    @Override public int hashCode() {
	int hash = 7;
	hash = 31 * hash + Objects.hashCode(this.getName());
	return hash;
    }

    @Override public boolean equals(Object obj) 
    {	
    	if (obj == null) 	    return false;	
    	if (getClass() != obj.getClass()) 	    return false;	
    	final Bike other = (Bike) obj;
    	return Objects.equals(this.getName(), other.getName());
    }
}

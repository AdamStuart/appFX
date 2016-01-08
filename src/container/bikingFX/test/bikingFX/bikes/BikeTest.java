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
package container.bikingFX.test.bikingFX.bikes;

import java.time.LocalDate;

import javax.json.Json;
import javax.json.JsonReader;

import org.junit.Assert;
import org.junit.Test;

import container.bikingFX.bikes.Bike;
import javafx.scene.paint.Color;

/**
 * @author Michael J. Simons, 2014-1017
 */
public class BikeTest {

    @Test
    public void factoryMethodShouldWork() {
	JsonReader jsonReader = Json.createReader(BikeTest.class.getResourceAsStream("../resources/bikes/singleBike.json"));
	Bike bike = new Bike(jsonReader.readObject());
	Assert.assertNull(bike.getDecommissionedOn());

	jsonReader = Json.createReader(BikeTest.class.getResourceAsStream("../resources/bikes/singleDecommissionedBike.json"));
	bike = new Bike(jsonReader.readObject());
	Assert.assertEquals("MTB", bike.getName());
	Assert.assertEquals(Color.web("#CCCCCC"), bike.getColor());
	Assert.assertEquals(LocalDate.of(2007, 8, 1), bike.getBoughtOn());
	Assert.assertEquals(LocalDate.of(2012, 9, 29), bike.getDecommissionedOn());
	Assert.assertEquals(6451, bike.getMileage().intValue());
    }
}

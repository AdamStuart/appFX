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
package container.bikingFX.test.bikingFX.bikingPictures;

import java.io.InputStream;

import javax.json.Json;
import javax.json.JsonReader;

import org.junit.Assert;
import org.junit.Test;

import container.bikingFX.bikingPictures.BikingPicture;

/**
 * @author Michael J. Simons, 2014-10-16
 */
public class BikingPictureTest {
    
    @Test
    public void factoryMethodShouldWork() {
    InputStream stream = BikingPictureTest.class.getResourceAsStream("../resources/bikingPictures/singleBikingPicture.json");
	final JsonReader jsonReader = Json.createReader(stream);
	final BikingPicture bikingPicture = new BikingPicture(jsonReader.readObject());
	Assert.assertEquals("http://biking.michael-simons.eu/api/bikingPictures/231.jpg", bikingPicture.getSrc());
	Assert.assertEquals(bikingPicture.getSrc(), bikingPicture.srcProperty().getValue());
	Assert.assertEquals("https://dailyfratze.de/michael/2005/8/29", bikingPicture.getLink());
	Assert.assertEquals(bikingPicture.getLink(), bikingPicture.linkProperty().getValue());
    }
    
}

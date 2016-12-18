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
package container.bikingFX;

import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author Michael J. Simons, 2014-10-07
 * http://biking.michael-simons.eu/
 */
public class AppBikingFX extends Application {

	public static void main(String[] args) {	launch(args);    }

    @Override public void start(final Stage stage) throws Exception {
	
    	ResourceBundle resources;
	final String bundleName = "container.bikingFX.bundles.BikingFX"; // bundles.BikingFX";
	final String rootpath = "root.fxml";
	try {
	    resources = ResourceBundle.getBundle(bundleName, Locale.getDefault());
	} catch (MissingResourceException e) {
	    Locale.setDefault(Locale.ENGLISH);
	    resources = ResourceBundle.getBundle(bundleName, Locale.getDefault());
	}
	
		// Load scene
		stage.setTitle("BikingFX:  Michael J. Simons");
		URL fxml = AppBikingFX.class.getResource(rootpath);
		if (fxml == null) System.out.println("FXML file not found at " + rootpath);
		else
		{
			stage.setScene(new Scene(FXMLLoader.load(fxml, resources)));
			stage.show();
		}
    }
    
}

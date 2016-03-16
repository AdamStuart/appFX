/*
 * Copyright 2014 michael-simons.eu.
 * Changed Functionality from JsonRetrieval to FTPUpload 2016 AdamTreister
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
package container.bikingFX.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.JsonValue;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;


public class FTPUploadTask<T> extends Task<Collection<T>> {

	    
    public static final String HOST_AND_PORT = "ftp://ftp.treister.com"; 
    public static final String BASE_URL = HOST_AND_PORT + "/dropbox";
    private static final Logger logger = Logger.getLogger(FTPUploadTask.class.getName());

     /**
     * Instantiates a new upload task, sets up an observable list, starts the task in a
     * separate thread 
     * 
     * @param <T>
     * @param objectFactory
     * @param endpoint
     * @return 
     */
	public static <T> ObservableList<T> get(final String endpoint) {
	final ObservableList<T> rv = FXCollections.observableArrayList();

	final FTPUploadTask<T> upload = new FTPUploadTask<>(endpoint);
	upload.setOnSucceeded(state -> {
//	    rv.addAll((Collection<T>) state.getSource().getValue());
	});
	new Thread(upload).start();
	return rv;
    }

    private final URL apiEndpoint;

    protected FTPUploadTask(final String endpoint) {
	URL hlp = null;
	try {
	    hlp = new URL(String.format("%s%s", BASE_URL, endpoint));
	} catch (MalformedURLException e) {
	    // I hope so ;)
	    throw new RuntimeException(e);
	}
	this.apiEndpoint = hlp;
    }

    @Override
    protected Collection<T> call() throws Exception {
	logger.log(Level.INFO, "Uploading list of objects from {0}", new Object[]{this.apiEndpoint.toString()});
	   System.out.println("url: " + apiEndpoint.toString());
	try (final JsonReader jsonReader = Json.createReader(apiEndpoint.openStream())) {
	    logger.log(Level.INFO, "Done.");
//	   Collection<T> result = jsonReader.readArray().stream().
//	    				map(objectFactory::createObject).
//	    				collect(Collectors.toList());
//	    return result;
	    return null;
	}
	catch (Exception e) 
	{ 
		e.printStackTrace();  
		throw(e); }
    }


}

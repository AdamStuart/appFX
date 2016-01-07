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
package bikingFX.bikes;

import static java.time.Instant.ofEpochMilli;
import static java.time.ZoneId.systemDefault;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;

import bikingFX.api.JsonRetrievalTask;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

/**
 * @author Michael J. Simons, 2014-10-20
 */
public class MileageChangeListener implements ChangeListener<Integer> {

    private static final Logger logger = Logger.getLogger(MileageChangeListener.class.getName());

    /**
     * Used to retrieve a password
     */
    private final Supplier<String> passwordSupplier;
    /**
     * Used to store a password
     */
    private final Consumer<Optional<String>> passwordConsumer;
    /**
     * Used for error messages
     */
    private final ResourceBundle resources;
    /**
     * Optionally used for handling failed events on the actual task
     */
    private EventHandler<WorkerStateEvent> taskFailedHandler;

    public MileageChangeListener(Supplier<String> passwordSupplier, Consumer<Optional<String>> passwordConsumer, ResourceBundle resources) {
	this.passwordSupplier = passwordSupplier;
	this.passwordConsumer = passwordConsumer;
	this.resources = resources;
    }

    public void setOnFailed(EventHandler<WorkerStateEvent> taskFailedHandler) {
	this.taskFailedHandler = taskFailedHandler;
    }

    @Override
    public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
	final Property<Integer> mileageProperty = (Property<Integer>) observable;
	final Bike bike = (Bike) mileageProperty.getBean();

	final LocalDate hlp = LocalDate.now();
	// If it's the first half of the month i assume i want to add last months mileage
	final LocalDate recordedOn = hlp.withDayOfMonth(1).plusMonths((hlp.getDayOfMonth() <= 15)? 0 : 1);

	// Password is retrieved on the FX thread as it propably opens a dialog
	final String password = passwordSupplier.get();
	logger.log(Level.INFO, "Updating {0}s mileage from {1} to {2}, recorded on {3}...", new Object[]{bike.getName(), oldValue, newValue, recordedOn});

	final Task<Integer> updateMileageTask = new Task<Integer>() {
	    @Override
	    protected Integer call() throws Exception {
		final URL apiEndpoint = new URL(String.format("%s/bikes/%d/mileages", JsonRetrievalTask.BASE_URL, bike.getId()));
		logger.log(Level.INFO, "Calling {0}...", new Object[]{apiEndpoint.toExternalForm()});

		// Prepare connection
		final HttpURLConnection connection = (HttpURLConnection) apiEndpoint.openConnection();
		// It's a post request
		connection.setRequestMethod("POST");
		// With json as content
		connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		// that needs to be authorized
		connection.setRequestProperty("Authorization", String.format("Basic %s", Base64.getEncoder().encodeToString(String.format("%s:%s", "biking2", password).getBytes())));
		connection.setDoInput(true);
		connection.setDoOutput(true);
		// Write the actuall request body
		try (final JsonWriter jsonWriter = Json.createWriter(connection.getOutputStream())) {
		    jsonWriter.write(Json.createObjectBuilder()
			    .add("recordedOn", recordedOn.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000)
			    .add("amount", newValue)
			    .build()
		    );
		}

		final int code = connection.getResponseCode();
		Integer rv = null;
		// Wrong password
		if (code == HttpURLConnection.HTTP_UNAUTHORIZED) {
		    // Delete the wrong password
		    passwordConsumer.accept(Optional.empty());
		    logger.log(Level.WARNING, "Unauthorized request, maybe wrong password?");
		    throw new RuntimeException(resources.getString("common.wrongPassword"));
		} 
		// Bad request (invalid mileage, date etc.)
		else if (code == HttpURLConnection.HTTP_BAD_REQUEST) {
		    final StringBuilder errorMessage = new StringBuilder();
		    try (final InputStreamReader reader = new InputStreamReader(connection.getErrorStream())) {
			final char[] buffer = new char[2048];
			int len;
			while ((len = reader.read(buffer, 0, buffer.length)) > 0) {
			    errorMessage.append(buffer, 0, len);
			}
		    }
		    logger.log(Level.WARNING, "Bad request: {0}", new Object[]{errorMessage});
		    throw new RuntimeException(errorMessage.toString());
		} 
		// Dont check the other http states, assume everything else must be ok (like in the angular app ;) )
		else {
		    // At this point we can be safe that the password is correct
		    passwordConsumer.accept(Optional.of(password));
		    try (final JsonReader reader = Json.createReader(connection.getInputStream())) {
			final JsonObject jsonObject = reader.readObject();
			final LocalDate recordedOn = LocalDateTime.ofInstant(ofEpochMilli(jsonObject.getJsonNumber("recordedOn").longValue()), systemDefault()).toLocalDate();
			rv = jsonObject.getInt("amount");
			logger.log(Level.INFO, "Stored new mileage {0} recored on {1} for {2}", new Object[]{rv, recordedOn, bike.getName()});
		    }
		}
		return rv;
	    }
	};
	if (this.taskFailedHandler != null) {
	    updateMileageTask.setOnFailed(this.taskFailedHandler);
	}
	new Thread(updateMileageTask).start();
    }
}

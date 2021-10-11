/**
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.esrifrance.demo_java;



import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;



public class App extends Application {
	
	
	public static MapController mapController = null;
	
	
    @Override
    public void start(Stage stage) throws IOException {

      FXMLLoader loader = new FXMLLoader(getClass().getResource("/Map.fxml"));
      Parent root = loader.load();
      mapController = loader.getController();
      Scene scene = new Scene(root);

      // set up the stage
      stage.setTitle("SIG 2020 App Sample");
      stage.setWidth(500);
      stage.setHeight(400);
      stage.setScene(scene);
      stage.show();
    }
    
    /**
     * Stops and releases all resources used in application.
     */
    @Override
    public void stop() {
      mapController.terminate();
    }

    /**
     * Opens and runs application.
     *
     * @param args arguments passed to this application
     */
    public static void main(String[] args) {

      Application.launch(args);
    }
}

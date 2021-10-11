package com.esrifrance.demo_java;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.FeatureLayer.SelectionMode;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.localserver.LocalServer;
import com.esri.arcgisruntime.localserver.LocalServerStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.Raster;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuBar;



public class MapController {
	  @FXML private  MapView mapView;	  
	  @FXML private MenuBar menuBar;
	  
	  private ArcGISMap map; 	  
	  private MobileMapPackage mobileMapPackage;	  
	  private GraphicsOverlay graphicsOverlay = new GraphicsOverlay();

	  
	    
	  public void initialize() {

		    try {
		      
		    	openDefaultMap(); 

		    } catch (Exception e) {
		      // on any error, display the stack trace.
		      e.printStackTrace();
		    }
		  }	 	
	  
		  public MapView getMapView () {
			  return  mapView;
		  }		
		  
		  private void openDefaultMap() throws IOException
		  {
			  
			// In this sample create a map with streets night vector basemap and add it to the map view
			  map = new ArcGISMap();
			  map.setBasemap(Basemap.createStreetsNightVector());
		      mapView.setMap(map);
			 
		  }
		  
		  
		// intialise the map from the mobile map package create on ArcGIS Pro 
		  @FXML
		  private void openMobileMapPackage (final ActionEvent event)
		  {
			   
			//load a mobile map package
		        mobileMapPackage = new MobileMapPackage("data/map2.mmpk");
		        // Loadable pattern to load the mobile map package 
		        mobileMapPackage.loadAsync();
		        mobileMapPackage.addDoneLoadingListener(() -> {
		          if (mobileMapPackage.getLoadStatus() == LoadStatus.LOADED && mobileMapPackage.getMaps().size() > 0) {
		            //add the map from the mobile map package to the map view if the mmpk is loaded without error 
		        	 map = mobileMapPackage.getMaps().get(0);    	 
		        	 
		        	 //Create a raster layer from tiff file and add it to the basemap 
		        	 addRasterLayer();	 
		            
		          } else {
		            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load the mobile map package");
		            alert.show();
		          }
		        });
		        
		     
		  }	  
		  
		  // Create a raster layer and add it to  the map 
		  private void addRasterLayer() {
		    	
		    	Raster raster = new Raster("data/usa_raster.tif");

		        // create a raster layer
		        RasterLayer rasterLayer = new RasterLayer(raster);
		        rasterLayer.loadAsync();
		        // When the raster layer  is loaded add it on the basemap 
		        rasterLayer.addDoneLoadingListener(() -> {
		          if (rasterLayer.getLoadStatus() == LoadStatus.LOADED) {
		        	  map.getBasemap().getBaseLayers().add(0,rasterLayer);
				      map.setBackgroundColor(getIntFromRGBColor(80,173,220));
				      mapView.setMap(map);
		          } else {
		            Alert alert = new Alert(Alert.AlertType.ERROR, "Raster Layer Failed to Load!");
		            alert.show();
		          }
		        });
		    }
		  
		   public int getIntFromRGBColor(int Red, int Green, int Blue){
		    	return (new Color(Red, Green, Blue)).getRGB(); 
		    }
		   
		   // Analyse using Geometry Engine 
		   
		   @FXML
		   private void usingGeometryEngine (final ActionEvent event) {
				  ArrayList <Geometry> geomList = new ArrayList<Geometry>(); 
				  Layer layer = map.getOperationalLayers().stream().filter(a -> a.getName().toUpperCase().equals("AIRPORTS")).findFirst().get();
		          if(layer!=null) {
		        		 String layerType = layer.getClass().getName();
		        		 if(layerType.contains("FeatureLayer")){
		        			 	FeatureLayer fl = (FeatureLayer)layer; 
		        			    //query FeatureLayer to find airport where TOT_ENP > 140000
		        			 	//Create query parameters 
		        			    QueryParameters query = new QueryParameters();
		        			    query.setReturnGeometry(true);
		        			    query.setWhereClause("TOT_ENP > 140000");

		        			    // Gets airports feature from  the feature table AIRPORTS
		        			    ListenableFuture<FeatureQueryResult> tableQueryResult = fl.getFeatureTable()
		        			    		.queryFeaturesAsync(query);

		        			    tableQueryResult.addDoneListener(() -> {
		        			      try {
		        			        // get the result from the query
		        			        FeatureQueryResult result = tableQueryResult.get();
		        			       	        			       
		        			        result.forEach(feature -> {
		        			        	//create 10KM buffer around each airport
		        			        	Geometry geom = GeometryEngine.project(feature.getGeometry(), 
		        			        			SpatialReferences.getWebMercator());
		        			        	geomList.add(GeometryEngine.buffer(geom,10000));
		        			        });
		        			        
		        			         // create union of buffer
		        			          Geometry unionGeom = GeometryEngine.union(geomList); 
		        			          
		        			          //create a graphic using the unionGeom and add it on graphicsOverlay
		        			          graphicsOverlay = new GraphicsOverlay();
		        			          mapView.getGraphicsOverlays().add(graphicsOverlay);
		        			          // create a semi-transparent purple fill symbol for the union of buffers
		        			          SimpleFillSymbol bufferFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0x88FF00FF, null);
		        			          Graphic bufferGraphic = new Graphic(unionGeom, bufferFillSymbol);
		        			          graphicsOverlay.getGraphics().add(bufferGraphic);
		        			          //zoom to the union of geom
		        			          mapView.setViewpoint(new Viewpoint(unionGeom.getExtent()));
		        			          
		        			          //Now select rivers that intersect the union of buffers
		        			          Layer layerRiv = map.getOperationalLayers().stream().filter(a -> a.getName().toUpperCase().equals("RIVERS")).findFirst().get();
		        			          if(layerRiv.getClass().getName().contains("FeatureLayer")){
			        			          FeatureLayer fl2 = (FeatureLayer) layerRiv;
			        			          //modify query param to select rivers that intersects the union of buffers
			        			          QueryParameters queryParam = new QueryParameters();
			        			          //queryParam.setReturnGeometry(true);
			        			          queryParam.setWhereClause("1=1");//always true
			        			          queryParam.setGeometry(unionGeom);//for spatial query / intersects by default
			        			          //select on the map rivers using queryParam
			        			          fl2.selectFeaturesAsync(queryParam, SelectionMode.NEW);
		        			          }
		       
		        			      } catch (Exception e) {
		        			    	  
		        			        // on any error, display the stack trace
		        			    	  e.printStackTrace();
		        			    	  showMessage("Can't select rivers", e.getMessage(), Alert.AlertType.ERROR);
		        			        ;
		        			      }
		        			    });
		        		 }
		        		 else {
		        			 showMessage("Layer", "Not supported layer type", Alert.AlertType.ERROR);
		        		 }
		        		
		          }  
		          else {
		        	  showMessage("Please add the airport layer", "Please add the airport layer", Alert.AlertType.ERROR);
		          }	    
		}
		  
		 
		  
		  private void showMessage(String title, String description, Alert.AlertType type) {

	    	    Alert alert = new Alert(type);
	    	    alert.setTitle(title);
	    	    alert.setContentText(description);
	    	    alert.show();
		  }
		  
		  /**
		   * Disposes application resources.
		   */
		  void terminate() {
		    if (mapView != null) {
		      mapView.dispose();
		    }
		    // stop local server
		    if (LocalServer.INSTANCE.getStatus() == LocalServerStatus.STARTED) {
		      LocalServer.INSTANCE.stopAsync();
		    }
		  }

}

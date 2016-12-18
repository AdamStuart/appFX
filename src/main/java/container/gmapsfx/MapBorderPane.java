package container.gmapsfx;

import container.gmapsfx.javascript.event.UIEventType;
import container.gmapsfx.javascript.object.GoogleMap;
import container.gmapsfx.javascript.object.InfoWindow;
import container.gmapsfx.javascript.object.InfoWindowOptions;
import container.gmapsfx.javascript.object.LatLong;
import container.gmapsfx.javascript.object.MapOptions;
import container.gmapsfx.javascript.object.MapTypeIdEnum;
import container.gmapsfx.javascript.object.Marker;
import container.gmapsfx.javascript.object.MarkerOptions;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import netscape.javascript.JSObject;

public class MapBorderPane extends BorderPane implements MapComponentInitializedListener
{
    private Button btnZoomIn;
    private Button btnZoomOut;
    private Label lblZoom;
    private Label lblCenter;
    private Label lblClick;
    private ComboBox<MapTypeIdEnum> mapTypeCombo;
	private Button btnHideMarker;
	private Button btnDeleteMarker;
	private GoogleMap map;
	private GoogleMapView mapview;
	
	public MapBorderPane()
	{
		super();
		mapview = new GoogleMapView();
		mapview.addMapInializedListener(this);
		
		ToolBar tools = new ToolBar();
		HBox info = new HBox(8);

		btnZoomIn = new Button("Zoom In");
		btnZoomIn.setOnAction(e ->{map.zoomProperty().set(map.getZoom() + 1);});
		btnZoomIn.setDisable(true);

		btnZoomOut = new Button("Zoom Out");
		btnZoomOut.setOnAction(e ->{	map.zoomProperty().set(map.getZoom() - 1);});
		btnZoomOut.setDisable(true);

		lblZoom = new Label();
		lblCenter = new Label();
		lblClick = new Label();

		mapTypeCombo = new ComboBox<>();
		mapTypeCombo.setDisable(true);
        mapTypeCombo.setDisable(false);
        mapTypeCombo.getItems().addAll( MapTypeIdEnum.ALL );

		Button btnType = new Button("Map type");
		btnType.setOnAction(e -> { 	map.setMapType(MapTypeIdEnum.HYBRID); 	});

		btnHideMarker = new Button("Hide Marker");
		btnHideMarker.setOnAction(e -> { 	hideMarker(); 	});

		btnDeleteMarker = new Button("Delete Marker");
		btnDeleteMarker.setOnAction(e -> 	{ deleteMarker(); });

		tools.getItems().addAll(btnZoomIn, btnZoomOut, new Label("Zoom: "), lblZoom, mapTypeCombo);
		Pane spacer = new Pane();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		info.getChildren().addAll(new Label("Center: "), lblCenter, new Separator(),
						new Label("Click: "), lblClick, spacer, btnHideMarker, btnDeleteMarker);
		setTop(tools);
		setCenter(mapview);
		setBottom(info);

	}
	
	   @Override
	    public void mapInitialized() {
	        //Once the map has been loaded by the Webview, initialize the map details.
	        LatLong center = new LatLong(42.196544, 237.28634);
	        mapview.addMapReadyListener(() -> {     checkCenter(center);    });	  // This call will fail unless the map is completely ready.

	        
	        MapOptions options = new MapOptions();
	        options.center(center)
	                .mapMarker(true)
	                .zoom(18)
	                .overviewMapControl(true)
	                .panControl(true)
	                .rotateControl(true)
	                .scaleControl(true)
	                .streetViewControl(true)
	                .zoomControl(false)
	                .mapType(MapTypeIdEnum.TERRAIN);

	        map = mapview.createMap(options);
		    lblCenter.setText(map.getCenter().toString());
		    map.centerProperty().addListener((obs,  o,  n) -> {  lblCenter.setText(n.toString());     });
		    lblZoom.setText(Integer.toString(map.getZoom()));
		    map.zoomProperty().addListener((obs,  o,  n) -> {  lblZoom.setText(n.toString());     });

		    map.addUIEventHandler(UIEventType.click, (JSObject obj) -> {
		            LatLong ll = new LatLong((JSObject) obj.getMember("latLng"));
		            //System.out.println("LatLong: lat: " + ll.getLatitude() + " lng: " + ll.getLongitude());
		            lblClick.setText(ll.toString());
		        });
	        
			mapTypeCombo.setOnAction(e ->{	map.setMapType(mapTypeCombo.getSelectionModel().getSelectedItem());	});
	        map.setHeading(123.2);
//	        System.out.println("Heading is: " + map.getHeading() );
	        addMarkers(center);
	        
//	        LatLong ll = new LatLong(-41.2, 145.9);
//	        LocationElevationRequest ler = new LocationElevationRequest(new LatLong[]{ll});
//	        
//	        ElevationService es = new ElevationService();
//	        es.getElevationForLocations(ler, new ElevationServiceCallback() {
//	            @Override
//	            public void elevationsReceived(ElevationResult[] results, ElevationStatus status) {
////	                System.out.println("We got results from the Location Elevation request:");
//	                for (ElevationResult er : results) {
//	                    System.out.println("LER: " + er.getElevation());
//	                }
//	            }
//	        });
	        
//	        LatLong lle = new LatLong(-42.2, 145.9);
//	        PathElevationRequest per = new PathElevationRequest(new LatLong[]{ll, lle}, 3);
//	        
//	        ElevationService esb = new ElevationService();
//	        esb.getElevationAlongPath(per, new ElevationServiceCallback() {
//	            @Override
//	            public void elevationsReceived(ElevationResult[] results, ElevationStatus status) {
////	                System.out.println("We got results from the Path Elevation Request:");
//	                for (ElevationResult er : results) {
//	                    System.out.println("PER: " + er.getElevation());
//	                }
//	            }
//	        });
	        
//	        MaxZoomService mzs = new MaxZoomService();
//	        mzs.getMaxZoomAtLatLng(lle, new MaxZoomServiceCallback() {
//	            @Override
//	            public void maxZoomReceived(MaxZoomResult result) {
//	                System.out.println("Max Zoom Status: " + result.getStatus());
//	                System.out.println("Max Zoom: " + result.getMaxZoom());
//	            }
//	        });
	        
	        
		
	      lblCenter.setText(map.getCenter().toString());
        map.centerProperty().addListener( (obs,  o,  n) -> {
            lblCenter.setText(n.toString());
        });

        lblZoom.setText(Integer.toString(map.getZoom()));
        map.zoomProperty().addListener((obs,  o,  n) -> {
            lblZoom.setText(n.toString());
        });

//      map.addStateEventHandler(MapStateEventType.center_changed, () -> {
//			System.out.println("center_changed: " + map.getCenter());
//		});
//        map.addStateEventHandler(MapStateEventType.tilesloaded, () -> {
//			System.out.println("We got a tilesloaded event on the map");
//		});
        map.addUIEventHandler(UIEventType.click, (JSObject obj) -> {
            LatLong ll = new LatLong((JSObject) obj.getMember("latLng"));
            //System.out.println("LatLong: lat: " + ll.getLatitude() + " lng: " + ll.getLongitude());
            lblClick.setText(ll.toString());
        });

        btnZoomIn.setDisable(false);
        btnZoomOut.setDisable(false);
        mapTypeCombo.setDisable(false);
        
        mapTypeCombo.getItems().addAll( MapTypeIdEnum.ALL );


	}
	
//		private MarkerOptions markerOptions2;
		private Marker myMarker2;

		private MarkerOptions markerOptions3;
		private Marker myMarker3;

	private void hideMarker() {
//		System.out.println("deleteMarker");
		
		boolean visible = myMarker2.getVisible();
		
		//System.out.println("Marker was visible? " + visible);
		
		myMarker2.setVisible(! visible);

//				markerOptions2.visible(Boolean.FALSE);
//				myMarker2.setOptions(markerOptions2);
//		System.out.println("deleteMarker - made invisible?");
	}
	
	private void deleteMarker() {
		//System.out.println("Marker was removed?");
		map.removeMarker(myMarker2);
	}

	public void addMarkers(LatLong centerLatLng)
	{

        
//        MarkerOptions markerOptions = new MarkerOptions();
//        LatLong markerLatLong = new LatLong(47.606189, -122.335842);
//        markerOptions.position(markerLatLong)
//                .title("My new Marker")
//                .animation(Animation.DROP)
//                .visible(true);

//        final Marker myMarker = new Marker(markerOptions);
//
//        markerOptions2 = new MarkerOptions();
//        LatLong markerLatLong2 = new LatLong(47.906189, -122.335842);
//        markerOptions2.position(markerLatLong2)
//                .title("My new Marker")
//                .visible(true);
//
//        myMarker2 = new Marker(markerOptions2);

        LatLong markerLatLong3 = new LatLong( 42.197021, -122.715757);
        markerOptions3 = new MarkerOptions();
        markerOptions3.position(markerLatLong3).title("Tree House").visible(true);

        myMarker3 = new Marker(markerOptions3);
  
       
//        map.addMarker(myMarker);
//        map.addMarker(myMarker2);
        map.addMarker(myMarker3);

        InfoWindowOptions infoOptions = new InfoWindowOptions();
        infoOptions.content("<h2>Tree House Books</h2><h3>in the heart of Ashland, OR</h3>")
                .position(centerLatLng);

        InfoWindow window = new InfoWindow(infoOptions);
        window.open(map, myMarker3);
        
//        map.fitBounds(new LatLongBounds(new LatLong(30, 120), centerLatLng));
//        System.out.println("Bounds : " + map.getBounds());

 //      map.addStateEventHandler(MapStateEventType.center_changed, () -> {
//			System.out.println("center_changed: " + map.getCenter());
//		});
//        map.addStateEventHandler(MapStateEventType.tilesloaded, () -> {
//			System.out.println("We got a tilesloaded event on the map");
//		});
//         LatLong[] ary = new LatLong[]{markerLatLong, markerLatLong2};
//        MVCArray mvc = new MVCArray(ary);
//
//        PolylineOptions polyOpts = new PolylineOptions()
//                .path(mvc)
//                .strokeColor("red")
//                .strokeWeight(2);
//
//        Polyline poly = new Polyline(polyOpts);
//        map.addMapShape(poly);
//        map.addUIEventHandler(poly, UIEventType.click, (JSObject obj) -> {
//            LatLong ll = new LatLong((JSObject) obj.getMember("latLng"));
////            System.out.println("You clicked the line at LatLong: lat: " + ll.getLatitude() + " lng: " + ll.getLongitude());
//        });
//
//        LatLong poly1 = new LatLong(47.429945, -122.84363);
//        LatLong poly2 = new LatLong(47.361153, -123.03040);
//        LatLong poly3 = new LatLong(47.387193, -123.11554);
//        LatLong poly4 = new LatLong(47.585789, -122.96722);
//        LatLong[] pAry = new LatLong[]{poly1, poly2, poly3, poly4};
//        MVCArray pmvc = new MVCArray(pAry);
//
//        PolygonOptions polygOpts = new PolygonOptions()
//                .paths(pmvc)
//                .strokeColor("blue")
//                .strokeWeight(2)
//                .editable(false)
//                .fillColor("lightBlue")
//                .fillOpacity(0.5);
//
//        Polygon pg = new Polygon(polygOpts);
//        map.addMapShape(pg);
//        map.addUIEventHandler(pg, UIEventType.click, (JSObject obj) -> {
//            //polygOpts.editable(true);
//            pg.setEditable(!pg.getEditable());
//        });
//
//        LatLong centreC = new LatLong(47.545481, -121.87384);
//        CircleOptions cOpts = new CircleOptions()
//                .center(centreC)
//                .radius(5000)
//                .strokeColor("green")
//                .strokeWeight(2)
//                .fillColor("orange")
//                .fillOpacity(0.3);
//
//        Circle c = new Circle(cOpts);
//        map.addMapShape(c);
//        map.addUIEventHandler(c, UIEventType.click, (JSObject obj) -> {
//            c.setEditable(!c.getEditable());
//        });
//
//        LatLongBounds llb = new LatLongBounds(new LatLong(47.533893, -122.89856), new LatLong(47.580694, -122.80312));
//        RectangleOptions rOpts = new RectangleOptions()
//                .bounds(llb)
//                .strokeColor("black")
//                .strokeWeight(2)
//                .fillColor("null");
//
//        Rectangle rt = new Rectangle(rOpts);
//        map.addMapShape(rt);
//
//        LatLong arcC = new LatLong(47.227029, -121.81641);
//        double startBearing = 0;
//        double endBearing = 30;
//        double radius = 30000;
//
//        MVCArray path = ArcBuilder.buildArcPoints(arcC, startBearing, endBearing, radius);
//        path.push(arcC);
//
//        Polygon arc = new Polygon(new PolygonOptions()
//                .paths(path)
//                .strokeColor("blue")
//                .fillColor("lightBlue")
//                .fillOpacity(0.3)
//                .strokeWeight(2)
//                .editable(false));
//
//        map.addMapShape(arc);
//        map.addUIEventHandler(arc, UIEventType.click, (JSObject obj) -> {
//            arc.setEditable(!arc.getEditable());
//        });
//        		
	}
	
    private void checkCenter(LatLong center) {
//        System.out.println("Testing fromLatLngToPoint using: " + center);
//        Point2D p = map.fromLatLngToPoint(center);
//        System.out.println("Testing fromLatLngToPoint result: " + p);
//        System.out.println("Testing fromLatLngToPoint expected: " + mapComponent.getWidth()/2 + ", " + mapComponent.getHeight()/2);
    }
    

}

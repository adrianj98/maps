package com.mapbox.rctmgl.components.navigation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.maps.MapboxMap;


//import com.mapbox.mapboxsdk.location.LocationComponent;
//import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
//import com.mapbox.mapboxsdk.location.modes.CameraMode;
//import com.mapbox.mapboxsdk.location.modes.RenderMode;
//
import com.mapbox.navigation.base.options.NavigationOptions;
import com.mapbox.navigation.base.trip.model.RouteLegProgress;
import com.mapbox.navigation.base.trip.model.RouteProgress;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.MapboxNavigationProvider;
//
//
//import com.mapbox.navigation.core.MapboxNavigationProvider;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.navigation.base.route.RouterOrigin;
import com.mapbox.navigation.base.route.RouterFailure;
import com.mapbox.navigation.base.route.RouterCallback;
import com.mapbox.navigation.core.trip.session.LocationObserver;
import com.mapbox.navigation.core.trip.session.RouteProgressObserver;
import com.mapbox.navigation.core.trip.session.TripSessionState;
//import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions;
//import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions;

////import com.mapbox.navigation.ui.camera.NavigationCamera;
//
import com.mapbox.rctmgl.components.navigation.RCTMGLNativeNavigationManager;
import com.mapbox.rctmgl.components.mapview.OnMapReadyCallback;


//import com.mapbox.navigation.ui.route.NavigationMapRoute;
import com.mapbox.rctmgl.components.AbstractMapFeature;
import com.mapbox.rctmgl.components.mapview.RCTMGLMapView;

import java.util.ArrayList;
import java.util.List;

public class RCTMGLNativeNavigation extends AbstractMapFeature implements OnMapReadyCallback {
    private boolean mEnabled = true;
    private MapboxMap mMap;
    private RCTMGLMapView mMapView;
    private RCTMGLNativeNavigationManager mManager;
    private NavigationOptions  navigationOptions;
    private MapboxNavigation mapboxNavigation;
 //   private LocationComponent locationComponent;
  //  private NavigationMapRoute navigationMapRoute;
    //private NavigationCamera navigationCamera = null;
    private List<Point> navPoints;
    private List<String> navAnnotations;
    private String accessT = "pk.eyJ1IjoiYWRyaWFuaiIsImEiOiJja3U3MzczNDIyYmdzMnZxZ2V4NGVybjFyIn0.MJFUX8Ubizy0yaf4jGiXaA";


//    private sendMessage(String id, WritableMap Message){
//        cache.computeIfAbsent(x, y -> y * 2);
//    }
    private int routeHash = 0;
    public void sendRoute(DirectionsRoute route){
        int newHash = route.hashCode();
        if (newHash != routeHash) {
            routeHash = newHash;
            String jsonRoute = route.toJson();
            WritableMap event = Arguments.createMap();
            event.putString("route", jsonRoute);
            ReactContext reactContext = (ReactContext) getContext();
            reactContext
                    .getJSModule(RCTEventEmitter.class)
                    .receiveEvent(getId(), "topRoutesReady", event);
        }
    }

//    private LocationObserver locationObserver = new LocationObserver() {
//
//        @Override
//        public void onRawLocationChanged(Location rawLocation) {
//            WritableMap event = Arguments.createMap();
//
//            event.putDouble("latitude", rawLocation.getLatitude());
//            event.putDouble("longitude", rawLocation.getLongitude());
//            event.putDouble("bearing", rawLocation.getBearing());
//            event.putDouble("altitude", rawLocation.getAltitude());
//            event.putDouble("speed", rawLocation.getSpeed());
//
//            ReactContext reactContext = (ReactContext)getContext();
//            reactContext
//                    .getJSModule(RCTEventEmitter.class)
//                    .receiveEvent(getId(), "topLocation", event);
//        }
//
//        @Override
//        public void onEnhancedLocationChanged(Location enhancedLocation, List<? extends Location> keyPoints) {
//
//        }
//    };
    private RouteProgressObserver routeProgressObserver = new RouteProgressObserver() {

        @Override
        public void onRouteProgressChanged(RouteProgress routeProgress) {
            WritableMap event = Arguments.createMap();

//            BannerInstructions bannerInstructions = routeProgress.getBannerInstructions();
//            if (bannerInstructions != null){
//                event.putString("bannerInstructions",bannerInstructions.toJson());
//            }
            DirectionsRoute route = routeProgress.getRoute();

            if (route != null){
                sendRoute(route);
            }
            event.putString("currentState", String.valueOf(routeProgress.getCurrentState()));
            RouteLegProgress routeLegProgress = routeProgress.getCurrentLegProgress();
            if (routeLegProgress != null) {
               // WritableMap routeLegProgressMap = Arguments.createMap();
                event.putDouble("legDistanceRemaining",routeLegProgress.getDistanceRemaining());
                event.putInt("legIndex",routeLegProgress.getLegIndex());
                //routeLegProgressMap.putString("routeLeg",routeLegProgress.getRouteLeg().toJson());
                event.putInt("stepIndex",routeLegProgress.getCurrentStepProgress().getStepIndex());
                event.putDouble("stepDistanceRemaining",routeLegProgress.getCurrentStepProgress().getDistanceRemaining());
               // event.putMap("routeLegProgress",routeLegProgressMap);
            }
            event.putDouble("distanceRemaining",routeProgress.getDistanceRemaining());
            event.putDouble("distanceTraveled",routeProgress.getDistanceTraveled());
            event.putDouble("durationRemaining",routeProgress.getDurationRemaining());
            event.putDouble("distanceTraveled",routeProgress.getDistanceTraveled());
            if (routeProgress.getUpcomingStepPoints() != null)
                event.putString("upcomingStepPoints", routeProgress.getUpcomingStepPoints().toString());



            ReactContext reactContext = (ReactContext)getContext();
            reactContext
                    .getJSModule(RCTEventEmitter.class)
                    .receiveEvent(getId(), "topRouteProgress", event);
        }  
    };
    private RouterCallback routesReqCallback = new RouterCallback() {
        @Override
        public void onRoutesReady(List<? extends DirectionsRoute> routes, RouterOrigin routerOrigin) {
            sendRoute(routes.get(0));


        }

        @Override
        public void onFailure(List<RouterFailure> failures,RouteOptions routeOptions) {

        }

        @Override
        public void onCanceled(RouteOptions routeOptions, RouterOrigin routerOrigin )  {

        }
    };



    private LifecycleOwner getLifecycleOwner(Context context){

        Context curContext = context;
        int maxDepth = 20;
        while (maxDepth-- > 0 && !(curContext instanceof LifecycleOwner)) {
            curContext = ((ContextWrapper)curContext).getBaseContext();
        }
        if (curContext instanceof LifecycleOwner) {
            return (LifecycleOwner)curContext;
        } else {
            return null;
        }
    }


    public RCTMGLNativeNavigation(Context context,RCTMGLNativeNavigationManager manager) {
        super(context);
        mManager = manager;

        navigationOptions = new NavigationOptions.Builder(context)
                .accessToken(accessT)
                .build();
        mapboxNavigation = MapboxNavigationProvider.create(navigationOptions);
    }

    @SuppressLint("WrongConstant")
    private void enableLocationComponent() {


//            this.locationComponent = this.mMap.getLocationComponent();
//            this.locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(getContext(), mMap.getStyle()).build());
//            this.locationComponent.setLocationComponentEnabled(true);
//            this.locationComponent.setCameraMode(CameraMode.TRACKING);
//            this.locationComponent.setRenderMode(RenderMode.COMPASS);

            return;

    }

    @Override
    public void addToMap(RCTMGLMapView mapView) {
        mEnabled = true;
        mMapView = mapView;

        mapView.getMapAsync(this);

        mapboxNavigation.registerRouteProgressObserver(routeProgressObserver);
      //  mapboxNavigation.registerLocationObserver(locationObserver);
    }

    @Override
    public void removeFromMap(RCTMGLMapView mapView) {
        mEnabled = false;
        mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver);
     //   mapboxNavigation.unregisterLocationObserver(locationObserver);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mMap = mapboxMap;
//        navigationMapRoute = new NavigationMapRoute.Builder(mMapView, mMap,getLifecycleOwner(getContext()))
//                .withMapboxNavigation(mapboxNavigation)
//                .build();


   //     navigationCamera = new NavigationCamera(mMap);
        enableLocationComponent();
        updateRoute();
    }



    public void setAnnotations(List<String> annotations) {
        navAnnotations = annotations;
        updateRoute();

    }
    public void setCoordinates(List<Point> points) {
        navPoints = points;
        updateRoute();

    }
    public void setNavigationEnabled(boolean enabled) {


       if (enabled) {
           if (mapboxNavigation.getTripSessionState() == TripSessionState.STOPPED) {
             //  updateRoute();
               mapboxNavigation.startTripSession();
           }

       } else  {
           mapboxNavigation.stopTripSession();
       }
    }


    public void setRoute() {

    }

    private void updateRoute() {
        if (MapboxNavigationProvider.isCreated()) {
            if (navPoints.isEmpty()){
                mapboxNavigation.setRoutes(new ArrayList<DirectionsRoute>());
            } else {
//            Point origin = Point.fromLngLat(-97.760288, 30.273566);
//            Point destination = Point.fromLngLat(-122.4127467, 37.7455558);
//            List<Point> points = new ArrayList<Point>();
//            points.add(origin);
//            points.add(destination);


//
//                mapboxNavigation.requestRoutes(
//                                routeOptions,
//                                object : RouterCallback {
//                            override fun onRoutesReady(
//                                    routes: List<DirectionsRoute>,
//                            routerOrigin: RouterOrigin
//                ) {
//                                mapboxNavigation.setRoutes(routes)
//                            }
//
//                            override fun onFailure(
//                                    reasons: List<RouterFailure>,
//                            routeOptions: RouteOptions
//                ) {
//                                // no impl
//                            }
//
//                            override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
//                                // no impl
//                            }
//                        }
//        )
                RouteOptions.Builder routeOptionsBuilder =  RouteOptions.builder()
                      //  .applyDefaultNavigationOptions()
                        .coordinatesList(
                                navPoints
                        )
                        .steps(true)
                        .alternatives(true)
                        .voiceInstructions(true)
                     //   .geometries(RouteUrl.GEOMETRY_POLYLINE6)
                     //   .profile(RouteUrl.PROFILE_DRIVING_TRAFFIC)
                        .bannerInstructions(true);

                if (navAnnotations != null &&!navAnnotations.isEmpty()) {
                    routeOptionsBuilder.annotationsList(navAnnotations);
                }

                mapboxNavigation.requestRoutes(
                        routeOptionsBuilder.build(),
                        routesReqCallback);

              
            }
        }

//
//        mapboxNavigation.requestRoutes(
//                RouteOptions.builder()
//                        .accessToken(Mapbox.getAccessToken())
//                        .coordinates(points)
//                        .build());

    }

}

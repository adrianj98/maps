package com.mapbox.rctmgl.components.navigation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.mapbox.api.directions.v5.models.BannerInstructions;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import com.mapbox.navigation.base.internal.extensions.MapboxRouteOptionsUtils;
import com.mapbox.navigation.base.internal.route.RouteUrl;
import com.mapbox.navigation.base.options.NavigationOptions;
import com.mapbox.navigation.base.trip.model.RouteLegProgress;
import com.mapbox.navigation.base.trip.model.RouteProgress;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.MapboxNavigationProvider;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.navigation.core.directions.session.RoutesRequestCallback;
import com.mapbox.navigation.core.trip.session.LocationObserver;
import com.mapbox.navigation.core.trip.session.RouteProgressObserver;
import com.mapbox.navigation.ui.camera.NavigationCamera;

import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import com.mapbox.navigation.ui.route.NavigationMapRoute;
import com.mapbox.rctmgl.components.AbstractMapFeature;
import com.mapbox.rctmgl.components.mapview.RCTMGLMapView;

import java.util.ArrayList;
import java.util.List;

public class RCTMGLNativeNavigation extends AbstractMapFeature implements OnMapReadyCallback {
    private boolean mEnabled = true;
    private MapboxMap mMap;
    private RCTMGLMapView mMapView;
    private NavigationOptions navigationOptions;
    private MapboxNavigation mapboxNavigation;
    private LocationComponent locationComponent;
    private NavigationMapRoute navigationMapRoute;
    private NavigationCamera navigationCamera = null;
    private List<Point> navPoints;
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

    private LocationObserver locationObserver = new LocationObserver() {

        @Override
        public void onRawLocationChanged(Location rawLocation) {
            WritableMap event = Arguments.createMap();

            event.putDouble("latitude", rawLocation.getLatitude());
            event.putDouble("longitude", rawLocation.getLongitude());
            event.putDouble("bearing", rawLocation.getBearing());
            event.putDouble("altitude", rawLocation.getAltitude());
            event.putDouble("speed", rawLocation.getSpeed());

            ReactContext reactContext = (ReactContext)getContext();
            reactContext
                    .getJSModule(RCTEventEmitter.class)
                    .receiveEvent(getId(), "topLocation", event);
        }

        @Override
        public void onEnhancedLocationChanged(Location enhancedLocation, List<? extends Location> keyPoints) {

        }
    };
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
            if (routeProgress.getUpcomingStepPoints() != null) {
                WritableArray upcomingStepArray = Arguments.createArray();
                List<Point> upcomingStepPoints = routeProgress.getUpcomingStepPoints()
                for (int i = 0; i < upcomingStepPoints.size(); i++) {
                    upcomingStepArray.pushDouble(upcomingStepPoints.get(i).longitude());
                    upcomingStepArray.pushDouble(upcomingStepPoints.get(i).latitude());
                }
                event.putArray("upcomingStepPoints", upcomingStepArray);
            }
            ReactContext reactContext = (ReactContext)getContext();
            reactContext
                    .getJSModule(RCTEventEmitter.class)
                    .receiveEvent(getId(), "topRouteProgress", event);
        }  
    };
    private RoutesRequestCallback routesReqCallback = new RoutesRequestCallback() {
        @Override
        public void onRoutesReady(List<? extends DirectionsRoute> routes) {
            sendRoute(routes.get(0));


        }

        @Override
        public void onRoutesRequestFailure(Throwable throwable , RouteOptions routeOptions ) {

        }

        @Override
        public void onRoutesRequestCanceled(RouteOptions routeOptions)  {

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


    public RCTMGLNativeNavigation(Context context) {
        super(context);


        navigationOptions = MapboxNavigation
                .defaultNavigationOptionsBuilder(context, accessT)

                .build();
        mapboxNavigation = MapboxNavigationProvider.create(navigationOptions);
    }

    @SuppressLint("WrongConstant")
    private void enableLocationComponent() {


            this.locationComponent = this.mMap.getLocationComponent();
            this.locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(getContext(), mMap.getStyle()).build());
            this.locationComponent.setLocationComponentEnabled(true);
            this.locationComponent.setCameraMode(CameraMode.TRACKING);
            this.locationComponent.setRenderMode(RenderMode.COMPASS);

            return;

    }

    @Override
    public void addToMap(RCTMGLMapView mapView) {
        mEnabled = true;
        mMapView = mapView;

        mapView.getMapAsync(this);

        mapboxNavigation.registerRouteProgressObserver(routeProgressObserver);
        mapboxNavigation.registerLocationObserver(locationObserver);
    }

    @Override
    public void removeFromMap(RCTMGLMapView mapView) {
        mEnabled = false;
        mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver);
        mapboxNavigation.unregisterLocationObserver(locationObserver);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mMap = mapboxMap;
        navigationMapRoute = new NavigationMapRoute.Builder(mMapView, mMap,getLifecycleOwner(getContext()))
                .withMapboxNavigation(mapboxNavigation)
                .build();
        mapboxNavigation.startTripSession();

        navigationCamera = new NavigationCamera(mMap);
        enableLocationComponent();
        updateRoute();
    }




    public void setCoordinates(List<Point> points) {
        navPoints = points;
        updateRoute();

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

                mapboxNavigation.requestRoutes(
                        MapboxRouteOptionsUtils.applyDefaultParams(RouteOptions.builder())
                                .accessToken(accessT)
                                .coordinates(navPoints)
                                .steps(true)
                                .voiceInstructions(true)
                                .geometries(RouteUrl.GEOMETRY_POLYLINE6)
                                .profile(RouteUrl.PROFILE_DRIVING)
                                .bannerInstructions(true)
                                .build(),
                        routesReqCallback);

                mapboxNavigation.startTripSession();
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

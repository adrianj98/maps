package com.mapbox.rctmgl.components.navigation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import com.mapbox.navigation.base.internal.extensions.MapboxRouteOptionsUtils;
import com.mapbox.navigation.base.internal.route.RouteUrl;
import com.mapbox.navigation.base.options.NavigationOptions;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.MapboxNavigationProvider;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.navigation.core.directions.session.RoutesRequestCallback;
import com.mapbox.navigation.ui.OnNavigationReadyCallback;
import com.mapbox.navigation.ui.camera.NavigationCamera;
import com.mapbox.navigation.ui.listeners.NavigationListener;

import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.Mapbox;

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

    private RoutesRequestCallback routesReqCallback = new RoutesRequestCallback() {
        @Override
        public void onRoutesReady(List<? extends DirectionsRoute> routes) {
            String jsonRoute = routes.get(0).toJson();
            WritableMap event = Arguments.createMap();
            event.putString("route", jsonRoute);
            ReactContext reactContext = (ReactContext)getContext();
            reactContext
                    .getJSModule(RCTEventEmitter.class)
                    .receiveEvent(getId(), "topRoutesReady", event);
            if (navigationCamera != null && MapboxNavigationProvider.isCreated()){
                int[] padding = {0,0,0,0};
         //       navigationCamera.showRouteOverview(padding);
            }
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
//        setRenderMode(mRenderMode);


    }

    @Override
    public void removeFromMap(RCTMGLMapView mapView) {
        mEnabled = false;

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
                mapboxNavigation.setRoutes(null);
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

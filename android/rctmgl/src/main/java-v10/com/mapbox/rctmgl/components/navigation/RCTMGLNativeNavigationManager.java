package com.mapbox.rctmgl.components.navigation;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.mapbox.geojson.Point;
import com.mapbox.rctmgl.components.AbstractEventEmitter;
import com.mapbox.rctmgl.components.camera.RCTMGLCamera;
import com.mapbox.rctmgl.components.mapview.RCTMGLMapView;
import com.mapbox.rctmgl.events.constants.EventKeys;
//import com.mapbox.maps.plugin.location.modes.RenderMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public class RCTMGLNativeNavigationManager extends AbstractEventEmitter<RCTMGLNativeNavigation> {
    public static final String REACT_CLASS = "RCTMGLNativeNavigation";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    public RCTMGLNativeNavigationManager(ReactApplicationContext reactApplicationContext) {
        super(reactApplicationContext);
    }

    @Override
    public Map<String, String> customEvents() {
        return MapBuilder.<String, String>builder()
                        .put("topRoutesReady","onRoutesReady")

                        .put("topLocation","onLocation")
                        .put("topRouteProgress","onRouteProgress")

        .build();
    }

    @ReactProp(name="coordinates")
    public void setCoordinates(RCTMGLNativeNavigation navigation, ReadableArray points) {
         List<Point> coords = new ArrayList<Point>();
         for (int i = 0; i < points.size() ; i = i + 2) {
             coords.add(Point.fromLngLat(points.getDouble(i), points.getDouble(i+1)));
         }
        navigation.setCoordinates(coords);
    }

    @ReactProp(name="enableNavigation")
    public void enableNavigation(RCTMGLNativeNavigation navigation, boolean enabled) {
       navigation.setNavigationEnabled(enabled);
    }

    @ReactProp(name="annotations")
    public void setAnnotations(RCTMGLNativeNavigation navigation, ReadableArray annotations) {
        List<String> annotationsList = new ArrayList<String>();
        for (int i = 0; i < annotations.size() ; i++) {
            annotationsList.add(annotations.getString(i));
        }
        navigation.setAnnotations(annotationsList);
    }

    @Override
    protected RCTMGLNativeNavigation createViewInstance(ThemedReactContext reactContext) {
        return new RCTMGLNativeNavigation(reactContext,this);
    }
}

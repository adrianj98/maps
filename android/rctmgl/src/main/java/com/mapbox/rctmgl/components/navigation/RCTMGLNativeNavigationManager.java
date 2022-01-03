package com.mapbox.rctmgl.components.navigation;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.location.modes.RenderMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public class RCTMGLNativeNavigationManager extends ViewGroupManager<RCTMGLNativeNavigation> {
    public static final String REACT_CLASS = "RCTMGLNativeNavigation";

    @Nonnull
    @Override
    public String getName() {
        return REACT_CLASS;
    }

    public Map getExportedCustomBubblingEventTypeConstants() {
        return MapBuilder.builder()
                        .put("topRoutesReady",
                            MapBuilder.of(
                                    "phasedRegistrationNames",
                                    MapBuilder.of("bubbled", "onRoutesReady")
                            ))
                        .put("topLocation",
                                MapBuilder.of(
                                        "phasedRegistrationNames",
                                        MapBuilder.of("bubbled", "onLocation")
                                ))
                        .put("topRouteProgress",
                            MapBuilder.of(
                                    "phasedRegistrationNames",
                                    MapBuilder.of("bubbled", "onRouteProgress"))

        ).build();
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
        navigation.setEnabled(enabled);
    }

    @ReactProp(name="annotations")
    public void setAnnotations(RCTMGLNativeNavigation navigation, ReadableArray annotations) {
        List<String> annotationsList = new ArrayList<String>();
        for (int i = 0; i < annotations.size() ; i++) {
            annotationsList.add(annotations.getString(i));
        }
        navigation.setAnnotations(annotationsList);
    }

    @Nonnull
    @Override
    protected RCTMGLNativeNavigation createViewInstance(@Nonnull ThemedReactContext reactContext) {
        return new RCTMGLNativeNavigation(reactContext);
    }
}

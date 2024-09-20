package com.example.appv1;

import android.content.Context;
import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.android.gms.maps.model.Marker;


public class CustomClusterRenderer extends DefaultClusterRenderer<MyClusterItem> {
    public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager<MyClusterItem> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onClusterItemRendered(MyClusterItem clusterItem, Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);

        // Obtener el snippet del marcador
        String snippet = clusterItem.getSnippet();

        // Mostrar el snippet en un InfoWindow
        if (snippet != null && !snippet.isEmpty()) {
            marker.setSnippet(snippet);
            marker.showInfoWindow();
        }
    }
}

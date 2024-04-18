package com.example.LunarLocator.models.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.LunarLocator.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final View mWindow;

    public CustomInfoWindowAdapter(LayoutInflater inflater) {
        mWindow = inflater.inflate(R.layout.custom_info_window, null);
    }

    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        render(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(@NonNull Marker marker) {
        return null;
    }

    private void render(Marker marker, View view) {
        TextView titleTextView = view.findViewById(R.id.title);
        titleTextView.setText(marker.getTitle());

        TextView snippetTextView = view.findViewById(R.id.snippet);
        snippetTextView.setText(marker.getSnippet());
    }
}
package com.example.platedetect2.Adapter;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.example.platedetect2.DevicesFragment;

public class CustomArrayAdapter extends ArrayAdapter<DevicesFragment.ListItem> {
    public CustomArrayAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }
}

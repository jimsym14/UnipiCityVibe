package com.example.unipicityvibe.ui;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unipicityvibe.R;
import com.example.unipicityvibe.adapters.EventAdapter;
import com.example.unipicityvibe.models.Event;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Home extends Fragment {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;
    private DatabaseReference databaseEvents;
    private FusedLocationProviderClient fusedLocationClient;
    private Location userCurrentLocation;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home, container, false);

        databaseEvents = FirebaseDatabase.getInstance().getReference("events");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        recyclerView = view.findViewById(R.id.recyclerEvents);
        
        // υπολογισμος στηλων αναλογα με το πλατος οθονης (tablet vs κινητο)
        android.util.DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int spanCount = (int) (dpWidth / 400);
        if (spanCount < 1) spanCount = 1;

        recyclerView.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(getContext(), spanCount));
        
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        if (spacingInPixels == 0) spacingInPixels = 32;
        
        recyclerView.addItemDecoration(new com.example.unipicityvibe.utils.GridSpacingItemDecoration(spanCount, spacingInPixels, true));
        eventList = new ArrayList<>();
        adapter = new EventAdapter(getContext(), eventList); 
        recyclerView.setAdapter(adapter);

        com.google.android.material.floatingactionbutton.FloatingActionButton fab = view.findViewById(R.id.fabAddEvent);
        fab.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new CreateEvent())
                    .addToBackStack(null)
                    .commit();
        });

        loadEventsFromFirebase();
        getUserLocation();

        return view;
    }

    private void loadEventsFromFirebase() {
        databaseEvents.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Event> newEvents = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    try {
                        Event event = postSnapshot.getValue(Event.class);
                        if (event != null) {
                            newEvents.add(event);
                        }
                    } catch (Exception e) {}
                }
                eventList = newEvents;
                calculateDistances();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    
    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    userCurrentLocation = location;
                    calculateDistances();
                }
            });
        }
    }

    private void calculateDistances() {
        if (eventList.isEmpty()) return;

        if (userCurrentLocation != null) {
            for (Event event : eventList) {
                if (event.getLocation() != null) {
                    float[] results = new float[1];
                    Location.distanceBetween(
                            userCurrentLocation.getLatitude(), userCurrentLocation.getLongitude(),
                            event.getLocation().getLatitude(), event.getLocation().getLongitude(),
                            results
                    );
                    event.setDistanceMeter(results[0]);
                }
            }
            // ταξινομηση απο κοντινοτερο σε πιο μακρινο
            Collections.sort(eventList, (o1, o2) -> Float.compare(o1.getDistanceMeter(), o2.getDistanceMeter()));
        }
        
        adapter.updateEvents(new ArrayList<>(eventList));
    }
}

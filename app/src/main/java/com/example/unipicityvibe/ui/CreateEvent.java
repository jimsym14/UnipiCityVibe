package com.example.unipicityvibe.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.unipicityvibe.R;
import com.example.unipicityvibe.models.Event;
import com.example.unipicityvibe.models.EventLocation;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateEvent extends Fragment {

    private EditText etTitle, etDesc, etPrice, etLat, etLon, etImageUrl;
    private android.widget.TextView tvSelectedDate;
    private Button btnCreate, btnSelectDate;
    private DatabaseReference databaseEvents;
    private long selectedTimestamp = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_event, container, false);

        databaseEvents = FirebaseDatabase.getInstance().getReference("events");

        etTitle = view.findViewById(R.id.etTitle);
        etDesc = view.findViewById(R.id.etDesc);
        etImageUrl = view.findViewById(R.id.etImageUrl);
        etPrice = view.findViewById(R.id.etPrice);
        etLat = view.findViewById(R.id.etLat);
        etLon = view.findViewById(R.id.etLon);
        btnCreate = view.findViewById(R.id.btnCreate);
        btnSelectDate = view.findViewById(R.id.btnSelectDate);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);

        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnCreate.setOnClickListener(v -> createEvent());

        return view;
    }

    private void showDatePicker() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        // πρωτα ημερομηνια, μετα ωρα
        new android.app.DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(java.util.Calendar.YEAR, year);
            calendar.set(java.util.Calendar.MONTH, month);
            calendar.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth);
            
            new android.app.TimePickerDialog(getContext(), (timeView, hourOfDay, minute) -> {
                calendar.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(java.util.Calendar.MINUTE, minute);
                
                selectedTimestamp = calendar.getTimeInMillis();
                // ενημερωση UI με την επιλεγμενη ημερομηνια/ωρα
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
                tvSelectedDate.setText(sdf.format(calendar.getTime()));
                
            }, calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), true).show();
            
        }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }

    private void createEvent() {
        String title = etTitle.getText().toString();
        String desc = etDesc.getText().toString();
        String imageUrl = etImageUrl.getText().toString();
        String priceStr = etPrice.getText().toString();
        String latStr = etLat.getText().toString();
        String lonStr = etLon.getText().toString();

        // ελεγχος υποχρεωτικων πεδιων
        if (title.isEmpty() || desc.isEmpty() || priceStr.isEmpty() || selectedTimestamp == 0) {
            Toast.makeText(getContext(), getString(R.string.msg_fill_all), Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        // 0.0 αν δεν δοθουν συντεταγμενες
        double lat = latStr.isEmpty() ? 0.0 : Double.parseDouble(latStr);
        double lon = lonStr.isEmpty() ? 0.0 : Double.parseDouble(lonStr);

        String id = databaseEvents.push().getKey();
        EventLocation location = new EventLocation(lat, lon); 
        
        Event event = new Event(id, title, desc, selectedTimestamp, price, "img_tech", imageUrl, location);
        
        // Αποθήκευση στη Firebase
        if (id != null) {
            databaseEvents.child(id).setValue(event)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), getString(R.string.msg_event_created), Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}

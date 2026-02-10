package com.example.unipicityvibe.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unipicityvibe.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.unipicityvibe.models.BookingItem;
import com.example.unipicityvibe.adapters.BookingAdapter;

import java.util.ArrayList;
import java.util.List;

public class MyBookings extends Fragment {

    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private List<BookingItem> bookingList;
    private DatabaseReference dbBookings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_bookings, container, false);

        recyclerView = view.findViewById(R.id.recyclerBookings);
        
        android.util.DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int spanCount = (int) (dpWidth / 400);
        if (spanCount < 1) spanCount = 1;
        
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(getContext(), spanCount));
        
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        if (spacingInPixels == 0) spacingInPixels = 32;
        
        recyclerView.addItemDecoration(new com.example.unipicityvibe.utils.GridSpacingItemDecoration(spanCount, spacingInPixels, true));
        bookingList = new ArrayList<>();
        adapter = new BookingAdapter(bookingList, this::deleteBooking);
        recyclerView.setAdapter(adapter);

        dbBookings = FirebaseDatabase.getInstance().getReference("bookings");
        loadBookings();

        return view;
    }

    private void loadBookings() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        // φιλτραρουμε μονο τις κρατησεις του τρεχοντα χρηστη
        dbBookings.orderByChild("userId").equalTo(userId).addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 bookingList.clear();
                 for (DataSnapshot s : snapshot.getChildren()) {
                     String id = s.getKey();
                     String title = s.child("eventTitle").getValue(String.class);
                     String code = s.child("code").getValue(String.class);
                     Long timestamp = s.child("timestamp").getValue(Long.class);
                     
                     if (title != null) {
                         bookingList.add(new BookingItem(id, title, code, timestamp != null ? timestamp : 0));
                     }
                 }
                 adapter.notifyDataSetChanged();
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {
                 Toast.makeText(getContext(), "Error loading bookings", Toast.LENGTH_SHORT).show();
             }
        });
    }

    private void deleteBooking(String bookingId) {
        dbBookings.child(bookingId).removeValue()
                .addOnSuccessListener(a -> Toast.makeText(getContext(), getString(R.string.msg_booking_cancelled), Toast.LENGTH_SHORT).show()) 
                .addOnFailureListener(e -> Toast.makeText(getContext(), getString(R.string.msg_booking_failed), Toast.LENGTH_SHORT).show());
    }
}

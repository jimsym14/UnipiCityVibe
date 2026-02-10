package com.example.unipicityvibe.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.unipicityvibe.R;
import com.example.unipicityvibe.models.Event;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetails extends BottomSheetDialogFragment {

    private android.speech.tts.TextToSpeech tts;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        tts = new android.speech.tts.TextToSpeech(getContext(), status -> {
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                 int result = tts.setLanguage(Locale.getDefault());
                 if (result == android.speech.tts.TextToSpeech.LANG_MISSING_DATA 
                     || result == android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED) {
                     // fallback σε αγγλικα αν δεν υποστηριζεται η γλωσσα
                     tts.setLanguage(Locale.ENGLISH);
                 }
            }
        });
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
    

    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESC = "desc";
    private static final String ARG_TIMESTAMP = "timestamp";
    private static final String ARG_PRICE = "price";
    private static final String ARG_LAT = "lat";
    private static final String ARG_LON = "lon";
    private static final String ARG_IMAGE_URL = "imageUrl";
    private static final String ARG_IMAGE_RES_NAME = "imageResName";

    public static EventDetails newInstance(Event event) {
        EventDetails fragment = new EventDetails();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, event.getEventId());
        args.putString(ARG_TITLE, event.getTitle());
        args.putString(ARG_DESC, event.getDescription());
        args.putLong(ARG_TIMESTAMP, event.getTimestamp());
        args.putDouble(ARG_PRICE, event.getTicketPrice());
        args.putString(ARG_IMAGE_URL, event.getImageUrl());
        args.putString(ARG_IMAGE_RES_NAME, event.getImageResName());
        
        if (event.getLocation() != null) {
            args.putDouble(ARG_LAT, event.getLocation().getLatitude());
            args.putDouble(ARG_LON, event.getLocation().getLongitude());
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) return;

        String id = args.getString(ARG_EVENT_ID);
        String title = args.getString(ARG_TITLE);
        String desc = args.getString(ARG_DESC);
        long timestamp = args.getLong(ARG_TIMESTAMP);
        double price = args.getDouble(ARG_PRICE);
        double lat = args.getDouble(ARG_LAT);
        double lon = args.getDouble(ARG_LON);

        TextView tvTitle = view.findViewById(R.id.tvDetailTitle);
        TextView tvDesc = view.findViewById(R.id.tvDetailDesc);
        TextView tvDate = view.findViewById(R.id.tvDetailDate);
        TextView tvPrice = view.findViewById(R.id.tvDetailPrice);
        android.widget.ImageView imgEvent = view.findViewById(R.id.imgDetailEvent);
        Button btnBook = view.findViewById(R.id.btnBook);
        Button btnListen = view.findViewById(R.id.btnListen);
        View btnMap = view.findViewById(R.id.btnOpenMap);

        if (btnListen != null) {
            btnListen.setOnClickListener(v -> {
                if (tts != null && !desc.isEmpty()) {
                    tts.speak(desc, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    Toast.makeText(getContext(), "TTS not ready or no description", Toast.LENGTH_SHORT).show();
                }
            });
        }

        tvTitle.setText(title);
        tvDesc.setText(desc);

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMM yyyy 'at' HH:mm", Locale.getDefault());
        tvDate.setText(sdf.format(new Date(timestamp)));

        tvPrice.setText(price == 0 ? "Free" : String.format(Locale.getDefault(), "%.2f €", price));
        
        if (savedInstanceState == null) {
            getDialog().setOnShowListener(dialog -> {
                com.google.android.material.bottomsheet.BottomSheetDialog d = (com.google.android.material.bottomsheet.BottomSheetDialog) dialog;
                android.widget.FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    com.google.android.material.bottomsheet.BottomSheetBehavior behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);
                    behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
                    behavior.setSkipCollapsed(true);
                }
            });
        }

        String imageUrl = getArguments().getString(ARG_IMAGE_URL);
        String imageResName = getArguments().getString(ARG_IMAGE_RES_NAME);
        
        if (imageUrl != null && !imageUrl.isEmpty()) {
             com.bumptech.glide.Glide.with(this)
                 .load(imageUrl)
                 .placeholder(android.R.drawable.ic_menu_gallery)
                 .error(android.R.drawable.stat_notify_error)
                 .centerCrop()
                 .into(imgEvent);
        } else if (imageResName != null && !imageResName.isEmpty()) {
            @android.annotation.SuppressLint("DiscouragedApi")
            int resId = getResources().getIdentifier(imageResName, "drawable", requireContext().getPackageName());
            if (resId != 0) {
                imgEvent.setImageResource(resId);
                imgEvent.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            } else {
                imgEvent.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
             imgEvent.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        if (btnMap != null) {
            btnMap.setOnClickListener(v -> {
                String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", lat, lon, lat, lon, title);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                     // αν δεν υπαρχει google maps ανοιγουμε browser
                     startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + lat + "," + lon)));
                }
            });
        }

        btnBook.setOnClickListener(v -> bookTicket(id, title));
    }

    private void bookTicket(String eventId, String eventTitle) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("bookings");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String bookingId = db.push().getKey();
        
        long timestamp = System.currentTimeMillis();
        
        java.util.Map<String, Object> booking = new java.util.HashMap<>();
        booking.put("userId", userId);
        booking.put("userEmail", userEmail);
        booking.put("eventId", eventId);
        booking.put("eventTitle", eventTitle);
        booking.put("timestamp", timestamp);
        booking.put("code", java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        if (bookingId != null) {
             db.child(bookingId).setValue(booking)
                 .addOnSuccessListener(a -> {
                     Toast.makeText(getContext(), getString(R.string.msg_booked_success), Toast.LENGTH_SHORT).show();
                     dismiss();
                 })
                 .addOnFailureListener(e -> Toast.makeText(getContext(), getString(R.string.msg_booking_failed), Toast.LENGTH_SHORT).show());
        }
    }
}

package com.example.unipicityvibe.ui;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.unipicityvibe.R;
import com.example.unipicityvibe.models.Event;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private android.widget.TextView mainToolbarTitle;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String CHANNEL_ID = "nearby_events_channel";
    private static final float NOTIFICATION_RADIUS_METERS = 200;
    private boolean hasNotified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        android.content.SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        
        // εφαρμογη γλωσσας πριν φορτωσει το layout
        String lang = prefs.getString("app_lang", "en");
        java.util.Locale locale = new java.util.Locale(lang);
        java.util.Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        boolean isDark = prefs.getBoolean("dark_mode", false);
        boolean isLargeFont = prefs.getBoolean("large_font", false);

        if (isDark) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        if (isLargeFont) {
            android.content.res.Configuration info = new android.content.res.Configuration(getResources().getConfiguration());
            info.fontScale = 1.3f;
            getResources().updateConfiguration(info, getResources().getDisplayMetrics());
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
        }

        createNotificationChannel();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermissionAndGetLocation();
        startNearbyCheck();

        mainToolbarTitle = findViewById(R.id.mainToolbarTitle);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        
        // Listener για την πλοήγηση μέσω του κάτω μενού
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new Home();
                mainToolbarTitle.setText(R.string.title_home);
            } else if (itemId == R.id.nav_mybookings) {
                selectedFragment = new MyBookings();
                mainToolbarTitle.setText(R.string.title_my_bookings);
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new Settings();
                mainToolbarTitle.setText(R.string.settings_title);
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, selectedFragment)
                        .commit();
            }
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new Home())
                    .commit();
        }
        
        final androidx.activity.result.ActivityResultLauncher<android.content.Intent> voiceLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    java.util.ArrayList<String> matches = result.getData().getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        String command = matches.get(0).toLowerCase();
                        if (command.contains("home") || command.contains("events") || command.contains("σπίτι") || command.contains("εκδηλώσεις") || command.contains("eventos")) {
                            bottomNavigationView.setSelectedItemId(R.id.nav_home);
                        } else if (command.contains("booking") || command.contains("reservation") || command.contains("κρατήσεις") || command.contains("κράτηση") || command.contains("reservas")) {
                            bottomNavigationView.setSelectedItemId(R.id.nav_mybookings);
                        } else if (command.contains("settings") || command.contains("profile") || command.contains("ρυθμίσεις") || command.contains("προφίλ") || command.contains("ajustes")) {
                            bottomNavigationView.setSelectedItemId(R.id.nav_settings);
                        } else {
                            android.widget.Toast.makeText(this, "Command not recognized: " + command, android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        );

        com.google.android.material.floatingactionbutton.FloatingActionButton fabVoice = findViewById(R.id.fabVoiceCommand);
        fabVoice.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            
            android.content.SharedPreferences currentPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            String currentLang = currentPrefs.getString("app_lang", "en");
            intent.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, currentLang);
            
            intent.putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak a command (Home, Bookings, Settings)");
            try {
                voiceLauncher.launch(intent);
            } catch (Exception e) {
                android.widget.Toast.makeText(this, "Voice recognition not available", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        // αν ηρθαμε απο notification ανοιγουμε το event
        if (getIntent() != null && getIntent().hasExtra("open_event_id")) {
            String eventId = getIntent().getStringExtra("open_event_id");
            fetchAndShowEvent(eventId);
        }
    }

    private void fetchAndShowEvent(String eventId) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("events").child(eventId);
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Event event = snapshot.getValue(Event.class);
                if (event != null) {
                    EventDetails bottomSheet = EventDetails.newInstance(event);
                    bottomSheet.show(getSupportFragmentManager(), "EventDetailsBottomSheet");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void checkLocationPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void startNearbyCheck() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("events");
        db.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 // Έλεγχος αν ο χρήστης έχει ενεργοποιήσει τις ειδοποιήσεις
                 android.content.SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                 if (!prefs.getBoolean("notifications_enabled", true)) {
                     return;
                 }

                 // Έλεγχος άδειας τοποθεσίας
                 if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                     return;
                 }
                 
                 // Λήψη τρέχουσας τοποθεσίας
                 fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                     if (location == null) return;
                     for (DataSnapshot s : snapshot.getChildren()) {
                         Event e = s.getValue(Event.class);
                         if (e != null && e.getLocation() != null) {
                             float[] res = new float[1];
                             // Υπολογισμός απόστασης
                             Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                                     e.getLocation().getLatitude(), e.getLocation().getLongitude(), res);
                             
                             // αν ειμαστε εντος 200m και δεν εχουμε ξαναστειλει
                             if (res[0] < NOTIFICATION_RADIUS_METERS && !hasNotified) {
                                 sendNearbyEventNotification(e);
                                 hasNotified = true;
                                 break; 
                             }
                         }
                     }
                 });
             }
             @Override
             public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendNearbyEventNotification(Event event) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // αναγκαιο για android 13+ (POST_NOTIFICATIONS permission)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 2002);
                return;
            }
        }
        
        android.content.Intent intent = new android.content.Intent(this, MainActivity.class);
        intent.putExtra("open_event_id", event.getEventId());
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(this, 0, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setContentTitle("Event Nearby: " + event.getTitle())
                .setContentText("You are within 200m of " + event.getTitle() + "!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat.from(this).notify(100, builder.build());
    }

    private void createNotificationChannel() {
        // υποχρεωτικο για android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Nearby", NotificationManager.IMPORTANCE_DEFAULT);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }
}

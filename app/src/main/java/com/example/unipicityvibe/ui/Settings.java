package com.example.unipicityvibe.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.unipicityvibe.R;
import com.google.firebase.auth.FirebaseAuth;

public class Settings extends Fragment {

    private android.widget.TextView tvEmail;
    private EditText etNewPassword;
    private Button btnUpdate;
    private android.widget.RadioGroup rgTheme, rgFont;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings, container, false);

        tvEmail = view.findViewById(R.id.tvEmail);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        btnUpdate = view.findViewById(R.id.btnUpdateProfile);
        rgTheme = view.findViewById(R.id.rgTheme);
        rgFont = view.findViewById(R.id.rgFont);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        com.google.firebase.auth.FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            tvEmail.setText("Email: " + user.getEmail());
        }

        btnUpdate.setOnClickListener(v -> {
            String newPass = etNewPassword.getText().toString();
            if (!newPass.isEmpty()) {
                if (user != null && user.getEmail() != null) {
                    // re-auth πριν αλλαξουμε κωδικο (απαιτηση firebase)
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
                    builder.setTitle("Verify identity");
                    final EditText inputPass = new EditText(requireContext());
                    inputPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    inputPass.setHint("Current password");
                    builder.setView(inputPass);
                    builder.setPositiveButton("OK", (dialog, which) -> {
                        String currentPass = inputPass.getText().toString();
                        com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.getEmail(), currentPass);
                        user.reauthenticate(credential).addOnSuccessListener(aVoid -> {
                            user.updatePassword(newPass)
                                .addOnSuccessListener(a -> {
                                    Toast.makeText(getContext(), getString(R.string.msg_password_updated), Toast.LENGTH_SHORT).show();
                                    etNewPassword.setText("");
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Wrong password", Toast.LENGTH_SHORT).show());
                    });
                    builder.setNegativeButton("Cancel", null);
                    builder.show();
                }
            } else {
                Toast.makeText(getContext(), getString(R.string.msg_enter_password), Toast.LENGTH_SHORT).show();
            }
        });

        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        boolean isLargeFont = prefs.getBoolean("large_font", false);
        boolean areNotificationsEnabled = prefs.getBoolean("notifications_enabled", true);

        rgTheme.check(isDark ? R.id.rbThemeDark : R.id.rbThemeLight);
        rgFont.check(isLargeFont ? R.id.rbFontLarge : R.id.rbFontNormal);
        
        com.google.android.material.switchmaterial.SwitchMaterial swNotifications = view.findViewById(R.id.swNotifications);
        swNotifications.setChecked(areNotificationsEnabled);
        
        swNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply();
        });

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            boolean dark = (checkedId == R.id.rbThemeDark);
            prefs.edit().putBoolean("dark_mode", dark).apply();
            
            // εφαρμοζουμε αμεσως χωρις restart
            int mode = dark ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES 
                            : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode);
        });

        rgFont.setOnCheckedChangeListener((group, checkedId) -> {
            boolean large = (checkedId == R.id.rbFontLarge);
            boolean current = prefs.getBoolean("large_font", false);
            if (current != large) {
                 prefs.edit().putBoolean("large_font", large).apply();
                 Toast.makeText(getContext(), "Restart app to fully apply font changes", Toast.LENGTH_SHORT).show();
                 requireActivity().recreate();
            }
        });

        android.widget.RadioGroup rgLanguage = view.findViewById(R.id.rgLanguage);
        String currentLang = prefs.getString("app_lang", "en");
        if (currentLang.equals("el")) {
            rgLanguage.check(R.id.rbLangEl);
        } else if (currentLang.equals("es")) {
            rgLanguage.check(R.id.rbLangEs);
        } else {
            rgLanguage.check(R.id.rbLangEn);
        }

        rgLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            String newLang = "en";
            if (checkedId == R.id.rbLangEl) newLang = "el";
            else if (checkedId == R.id.rbLangEs) newLang = "es";

            if (!newLang.equals(currentLang)) {
                 prefs.edit().putString("app_lang", newLang).apply();
                 setAppLocale(requireActivity(), newLang);
                 requireActivity().recreate();
            }
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            if (getActivity() != null) getActivity().finish();
        });

        return view;
    }

    private void setAppLocale(Context context, String languageCode) {
        java.util.Locale locale = new java.util.Locale(languageCode);
        java.util.Locale.setDefault(locale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }
}

package de.pd.bdb;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class LoginFragment extends Fragment {
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefsEditor;
    Connection connection;
    Context context;

    public LoginFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        context = view.getContext();

        openPrefs();

        connection = new Connection(context, prefs);

        if (!prefs.getString("username", "").equals("") && !prefs.getString("password", "").equals("")) {
            Intent loggedIn = new Intent(context, MainActivity.class);
            startActivity(loggedIn);
        }

        TextInputEditText inputUsername = view.findViewById(R.id.inputUsername);

        TextInputEditText inputPassword = view.findViewById(R.id.inputPassword);
        inputPassword.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(inputPassword, InputMethodManager.SHOW_IMPLICIT);

        Button buttonLogin = view.findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(v -> {
            String username = Objects.requireNonNull(inputUsername.getText()).toString();
            String password = Objects.requireNonNull(inputPassword.getText()).toString();

            new Thread(() -> {
                try {
                    if (connection.authenticate(username, password)) {
                        prefsEditor.putString("username", username).apply();
                        prefsEditor.putString("password", password).apply();

                        Intent loggedIn = new Intent(context, MainActivity.class);

                        requireActivity().runOnUiThread(() -> {
                            inputPassword.setText("");
                            startActivity(loggedIn);
                        });
                    } else {
                        requireActivity().runOnUiThread(() -> Toast.makeText(context, getString(R.string.wrongPassword), Toast.LENGTH_SHORT).show());
                    }
                } catch (IOException | KeyManagementException | NoSuchAlgorithmException | JSONException e) {
                    e.printStackTrace();
                }
            }).start();
            inputPassword.setText("");
        });

        return view;
    }

    private void openPrefs() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            KeyGenParameterSpec keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC;

            try {
                String keyAlias = MasterKeys.getOrCreate(keyGenParameterSpec);

                prefs = EncryptedSharedPreferences.create(
                        "de.pd.lesedatenbank",
                        keyAlias,
                        context,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }

        } else {
            prefs = context.getSharedPreferences("de.pd.lesedatenbank", Context.MODE_PRIVATE);
        }

        prefsEditor = prefs.edit();
    }
}
package de.pd.lesedatenbank;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.text.HtmlCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import androidx.viewpager2.widget.ViewPager2;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.mikepenz.aboutlibraries.LibsBuilder;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class LoginActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefsEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        openPrefs();
        materialToolbar();

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager2 = findViewById(R.id.viewPager2);
        ViewPager2Adapter viewPager2Adapter = new ViewPager2Adapter(getSupportFragmentManager(), getLifecycle());

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        viewPager2.setAdapter(viewPager2Adapter);

        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.login)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.register)));

        viewPager2Adapter.addFragment(new LoginFragment());
        viewPager2Adapter.addFragment(new RegisterFragment());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
    }

    private void openPrefs() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            KeyGenParameterSpec keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC;

            try {
                String keyAlias = MasterKeys.getOrCreate(keyGenParameterSpec);

                prefs = EncryptedSharedPreferences.create(
                        "de.pd.lesedatenbank",
                        keyAlias,
                        getApplicationContext(),
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }

        } else {
            prefs = this.getSharedPreferences("de.pd.lesedatenbank", Context.MODE_PRIVATE);
        }

        prefsEditor = prefs.edit();
    }

    private void materialToolbar() {
        MaterialToolbar materialToolbar = findViewById(R.id.materialToolbar);
        materialToolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            MaterialAlertDialogBuilder builder;
            LinearLayout layout = new LinearLayout(LoginActivity.this);

            if (id == R.id.about) {
                builder = new MaterialAlertDialogBuilder(LoginActivity.this);
                builder.setMessage(R.string.aboutText).setTitle(R.string.app_name);
                builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
                builder.show();
                return true;
            } else if (id == R.id.openSource) {
                builder = new MaterialAlertDialogBuilder(LoginActivity.this);
                builder.setMessage(HtmlCompat.fromHtml(getString(R.string.openSourceText), HtmlCompat.FROM_HTML_MODE_LEGACY)).setTitle(R.string.app_name);
                builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
                builder.show();
                return true;
            } else if (id == R.id.licences) {
                new LibsBuilder()
                        .withLicenseShown(true)
                        .withVersionShown(true)
                        .withAboutIconShown(true)
                        .withActivityTitle(getString(R.string.licences))
                        .start(LoginActivity.this);

                return true;
            } else if (id == R.id.changeEndpointURL) {
                builder = new MaterialAlertDialogBuilder(LoginActivity.this);
                builder.setTitle(R.string.app_name);

                layout.setOrientation(LinearLayout.VERTICAL);

                //Hostname
                EditText inputApiEndpoint = new EditText(LoginActivity.this);
                inputApiEndpoint.setHint(getString(R.string.apiEndpoint));
                inputApiEndpoint.setText(prefs.getString("apiEndpoint", "https://mint/api/lesedatenbank.php"));
                layout.addView(inputApiEndpoint);

                builder.setView(layout);
                builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> prefsEditor.putString("apiEndpoint", inputApiEndpoint.getText().toString()).apply());
                builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
                builder.show();
                return true;
            } else {
                return false;
            }
        });
    }
}
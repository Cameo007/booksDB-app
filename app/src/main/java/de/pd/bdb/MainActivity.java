package de.pd.bdb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.mikepenz.aboutlibraries.LibsBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity{
    //App variables
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefsEditor;
    Connection connection;
    String username;
    String password;

    //UI component variables
    RecyclerView recyclerviewBooks;
    ArrayAdapter<String> categoriesAdapter;
    BooksAdapter booksAdapter;

    //Variables
    List<String> categories ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openPrefs();
        materialToolbar();

        //If no login credentials are given go to login screen
        if (prefs.getString("username", "").equals("") && prefs.getString("password", "").equals("")) {
            Intent loggedOut = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loggedOut);
        }

        //If app is opened the first time, show intro
        if (prefs.getBoolean("FirstTime", true)) {
            prefsEditor.putBoolean("FirstTime", false).apply();
        }
        Intent intent = new Intent(this, Intro.class);
        startActivity(intent);

        //Define app variables
        connection = new Connection(MainActivity.this, prefs);
        username = prefs.getString("username", "");
        password = prefs.getString("password", "");

        //Define UI component variables
        AutoCompleteTextView categoryPicker = (AutoCompleteTextView) ((TextInputLayout) findViewById(R.id.categoryPicker)).getEditText();
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerviewBooks = findViewById(R.id.recyclerviewBooks);
        FloatingActionButton fabAddBook = findViewById(R.id.fabAddBook);
        TextView textviewNoBooks = findViewById(R.id.textviewNoBooks);


        new Thread(() ->{
            try {
                //Get categories
                categories = connection.getCategories(username, password);
                if (categories != null) {
                    if (categories.size() > 0) {
                        //Define array adapter for category picker
                        categoriesAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, categories);

                        //Get last viewed category OR default
                        String category = prefs.getString("startCategory", categories.get(0));

                        this.runOnUiThread(() -> {
                            //Set array adapter
                            Objects.requireNonNull(categoryPicker).setAdapter(categoriesAdapter);
                            //Set selection
                            categoryPicker.setText(category, false);
                        });

                        //Get books for category
                        List<JSONObject> books = connection.getBooks(username, password, category);
                        //Define book adapter
                        booksAdapter = new BooksAdapter(this, category, books);

                        runOnUiThread(() -> {
                            //Set adapter
                            recyclerviewBooks.setAdapter(booksAdapter);
                            //Show/Hide "No books" tip if book list is empty or not
                            if (books.isEmpty()) {
                                textviewNoBooks.setVisibility(View.VISIBLE);
                            } else {
                                textviewNoBooks.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        this.runOnUiThread(() -> Objects.requireNonNull(categoryPicker).setText(getString(R.string.noCategories), false));
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(this, getString(R.string.serverCouldntBeReached), Toast.LENGTH_SHORT).show());
                }
            } catch (NoSuchAlgorithmException | KeyManagementException | IOException | JSONException e) {
                runOnUiThread(() -> Toast.makeText(this, getString(R.string.error) + e.getMessage(), Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        }).start();

        swipeRefreshLayout.setOnRefreshListener(() -> new Thread(() -> {
            try {
                //Get categories
                List<String> categories = connection.getCategories(username, password);
                runOnUiThread(() -> {
                    //Clear category picker
                    categoriesAdapter.clear();
                    //Set new items
                    categoriesAdapter.addAll(categories);
                });

                //Get selected category
                String category = Objects.requireNonNull(categoryPicker).getText().toString();
                //Get books for category
                List<JSONObject> books = connection.getBooks(username, password, category);

                runOnUiThread(() -> {
                    //Update items
                    booksAdapter.setBooks(books);
                    //Show/Hide "No books" tip if book list is empty or not
                    if (books.isEmpty()) {
                        textviewNoBooks.setVisibility(View.VISIBLE);
                    } else {
                        textviewNoBooks.setVisibility(View.GONE);
                    }
                    //Stop refreshing
                    swipeRefreshLayout.setRefreshing(false);
                });
            } catch (NoSuchAlgorithmException | KeyManagementException | IOException | JSONException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, getString(R.string.error) + e.getMessage(), Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        }).start());

        //Define category picker's on item click listener
        Objects.requireNonNull(categoryPicker).setOnItemClickListener((adapterView, view, position, id) -> {
            //Get selected category
            String category = categories.get(position);
            //Save category name
            prefsEditor.putString("startCategory", category).apply();

            new Thread(() -> {
                try {
                    //Get books for category
                    List<JSONObject> books = connection.getBooks(username, password, category);

                    runOnUiThread(() -> {
                        //Set items
                        booksAdapter.setBooks(books);
                        //Show/Hide "No books" tip if book list is empty or not
                        if (books.isEmpty()) {
                            textviewNoBooks.setVisibility(View.VISIBLE);
                        } else {
                            textviewNoBooks.setVisibility(View.GONE);
                        }
                    });
                } catch (NoSuchAlgorithmException | KeyManagementException | IOException | JSONException e) {
                    runOnUiThread(() -> Toast.makeText(this, getString(R.string.error) + e.getMessage(), Toast.LENGTH_SHORT).show());
                    e.printStackTrace();
                }
            }).start();
        });

        categoryPicker.setOnLongClickListener(view -> {
            String oldCategoryName = categoryPicker.getText().toString();
            MaterialAlertDialogBuilder builder;
            LinearLayout layout = new LinearLayout(MainActivity.this);

            builder = new MaterialAlertDialogBuilder(MainActivity.this);
            builder.setTitle(R.string.app_name);

            layout.setOrientation(LinearLayout.VERTICAL);

            //Category name
            EditText inputCategoryName = new EditText(MainActivity.this);
            inputCategoryName.setHint(MainActivity.this.getString(R.string.name));
            inputCategoryName.setText(oldCategoryName);
            //Show keyboard
            inputCategoryName.requestFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(inputCategoryName, InputMethodManager.SHOW_IMPLICIT);

            layout.addView(inputCategoryName);

            builder.setView(layout);
            builder.setPositiveButton(MainActivity.this.getString(R.string.ok), (dialog1, which) -> {
                String newCategoryName = clean(inputCategoryName.getText().toString());
                new Thread(() -> {
                    try {
                        connection.renameCategory(username, password, oldCategoryName, newCategoryName);
                    } catch (KeyManagementException | NoSuchAlgorithmException | IOException | JSONException e) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, getString(R.string.error) + e.getMessage(), Toast.LENGTH_SHORT).show());
                        e.printStackTrace();
                    }
                }).start();
                categories.set(categories.indexOf(oldCategoryName), newCategoryName);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.dropdown_item, categories);
                //Set array adapter
                Objects.requireNonNull(categoryPicker).setAdapter(arrayAdapter);
                //Set selection
                categoryPicker.setText(newCategoryName, false);
            });
            builder.setNeutralButton(MainActivity.this.getString(R.string.delete), (dialogInterface, i) -> {
                new Thread(() -> {
                    try {
                        //Delete category on server
                        connection.deleteCategory(username, password, oldCategoryName);
                    } catch (KeyManagementException | NoSuchAlgorithmException | IOException | JSONException e) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, getString(R.string.error) + e.getMessage(), Toast.LENGTH_SHORT).show());
                        e.printStackTrace();
                    }
                }).start();

                //Delete category from list
                categories.remove(oldCategoryName);

                String category = categories.get(0);
                //Save category name
                prefsEditor.putString("startCategory", category).apply();

                //Set category in category picker
                categoryPicker.setText(category, false);
                new Thread(() -> {
                    try {
                        //Get books for category
                        List<JSONObject> books = connection.getBooks(username, password, category);
                        //Update book list
                        runOnUiThread(() -> booksAdapter.setBooks(books));
                    } catch (NoSuchAlgorithmException | KeyManagementException | IOException | JSONException e) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, getString(R.string.error) + e.getMessage(), Toast.LENGTH_SHORT).show());
                        e.printStackTrace();
                    }
                }).start();
            });
            builder.setNegativeButton(MainActivity.this.getString(R.string.cancel), (dialog1, which) -> dialog1.cancel());
            builder.show();
            return false;
        });

        fabAddBook.setOnClickListener(view -> {
            MaterialAlertDialogBuilder builder;
            LinearLayout layout = new LinearLayout(MainActivity.this);

            builder = new MaterialAlertDialogBuilder(MainActivity.this);
            builder.setTitle(R.string.app_name);

            layout.setOrientation(LinearLayout.VERTICAL);

            //Name
            EditText inputBookName = new EditText(MainActivity.this);
            inputBookName.setHint(MainActivity.this.getString(R.string.name));
            //Show keyboard
            inputBookName.requestFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(inputBookName, InputMethodManager.SHOW_IMPLICIT);

            layout.addView(inputBookName);

            //Author
            EditText inputAuthor = new EditText(MainActivity.this);
            inputAuthor.setHint(MainActivity.this.getString(R.string.author));
            layout.addView(inputAuthor);

            //Name
            CheckBox inputRead = new CheckBox(MainActivity.this);
            inputRead.setText(MainActivity.this.getString(R.string.read));
            layout.addView(inputRead);

            builder.setView(layout);
            builder.setPositiveButton(MainActivity.this.getString(R.string.ok), (dialog1, which) -> new Thread(() -> {
                try {
                    String categoryName = categoryPicker.getText().toString();
                    String bookName = clean(inputBookName.getText().toString());
                    String author = clean(inputAuthor.getText().toString());
                    boolean read = inputRead.isChecked();

                    //Add book on server
                    connection.addBook(username, password, categoryName, bookName, author, read);
                    MainActivity.this.runOnUiThread(() -> {
                        try {
                            //Append book to list
                            booksAdapter.addItem(new JSONObject(String.format("{\"bookName\": \"%s\", \"author\": \"%s\", \"read\": %b}", bookName, author, read)));
                            //Hide "No books" tip
                            textviewNoBooks.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (IOException | KeyManagementException | NoSuchAlgorithmException | JSONException e) {
                    e.printStackTrace();
                }
            }).start());
            builder.setNegativeButton(MainActivity.this.getString(R.string.cancel), (dialog1, which) -> dialog1.cancel());
            builder.show();
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //Get swiped item's position
                int position = viewHolder.getAbsoluteAdapterPosition();
                //Get item data
                JSONObject item = booksAdapter.getBook(position);

                //Delete item from list
                booksAdapter.removeBook(position);
                new Thread(() -> {
                    try {
                        //Delete item from server
                        connection.deleteBook(username, password, booksAdapter.getCategory(), item.getString("bookName"));
                    } catch (KeyManagementException | NoSuchAlgorithmException | IOException | JSONException e) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, getString(R.string.error) + e.getMessage(), Toast.LENGTH_SHORT).show());
                        e.printStackTrace();
                    }
                }).start();
                //Show/Hide "No books" tip if book list is empty or not
                if (booksAdapter.getBooks().isEmpty()) {
                    textviewNoBooks.setVisibility(View.VISIBLE);
                } else {
                    textviewNoBooks.setVisibility(View.GONE);
                }
                try {
                    //Show snackbar for undo
                    Snackbar.make(recyclerviewBooks, String.format(getString(R.string.itemHasBeenDeleted), item.getString("bookName")), Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), v -> {
                        new Thread(() -> {
                            try {
                                //Add item on server
                                connection.addBook(username, password, booksAdapter.getCategory(), item.getString("bookName"), item.getString("author"), item.getBoolean("read"));
                            } catch (KeyManagementException | NoSuchAlgorithmException | IOException | JSONException e) {
                                runOnUiThread(() -> Toast.makeText(MainActivity.this, getString(R.string.error) + e.getMessage(), Toast.LENGTH_SHORT).show());
                                e.printStackTrace();
                            }
                        }).start();
                        //Add item to list
                        booksAdapter.insertBook(position, item);
                        //Hide "No books" tip
                        textviewNoBooks.setVisibility(View.GONE);
                    }).show();

                } catch (JSONException e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, getString(R.string.error) + e.getMessage(), Toast.LENGTH_SHORT).show());
                    e.printStackTrace();
                }
            }
        }).attachToRecyclerView(recyclerviewBooks);
    }


    public String clean(String text) {
        return text.replaceAll("[^A-Za-zŽžÀ-ÿ0-9\\-!§$%/=?°<>|+*~#()\\[\\]{}.:,; ]+", "");
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
            LinearLayout layout = new LinearLayout(MainActivity.this);

            if (id == R.id.about) {
                builder = new MaterialAlertDialogBuilder(MainActivity.this);
                builder.setMessage(R.string.aboutText).setTitle(R.string.app_name);
                builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
                builder.show();
                return true;
            } else if (id == R.id.openSource) {
                builder = new MaterialAlertDialogBuilder(MainActivity.this);
                builder.setMessage(HtmlCompat.fromHtml(getString(R.string.openSourceText), HtmlCompat.FROM_HTML_MODE_COMPACT)).setTitle(R.string.app_name);
                builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
                builder.show();
                return true;
            } else if (id == R.id.licences) {
                new LibsBuilder()
                        .withLicenseShown(true)
                        .withVersionShown(true)
                        .withAboutIconShown(true)
                        .withActivityTitle(getString(R.string.licences))
                        .start(MainActivity.this);

                return true;
            } else if (id == R.id.changeEndpointURL) {
                builder = new MaterialAlertDialogBuilder(MainActivity.this);
                builder.setTitle(R.string.app_name);

                layout.setOrientation(LinearLayout.VERTICAL);

                EditText inputApiEndpoint = new EditText(MainActivity.this);
                inputApiEndpoint.setHint(getString(R.string.apiEndpoint));
                inputApiEndpoint.setText(prefs.getString("apiEndpoint", "https://mint/api/lesedatenbank.php"));
                layout.addView(inputApiEndpoint);

                builder.setView(layout);
                builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> prefsEditor.putString("apiEndpoint", inputApiEndpoint.getText().toString()).apply());
                builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
                builder.show();
                return true;
            } else if (id == R.id.logout) {
                prefsEditor.remove("password").apply();

                Intent loggedOut = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loggedOut);
                return true;
            } else if (id == R.id.changeUsername) {
                builder = new MaterialAlertDialogBuilder(MainActivity.this);
                builder.setTitle(R.string.app_name);

                layout.setOrientation(LinearLayout.VERTICAL);

                EditText inputUsername = new EditText(MainActivity.this);
                inputUsername.setHint(getString(R.string.newUsername));
                inputUsername.setText(prefs.getString("username", ""));
                layout.addView(inputUsername);

                builder.setView(layout);
                builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                    String newUsername = inputUsername.getText().toString();
                    new Thread(() -> {
                        try {
                            connection.changeUsername(username, password, newUsername);
                        } catch (IOException | KeyManagementException | JSONException | NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                    }).start();
                    prefsEditor.putString("username", newUsername).apply();
                });
                builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
                builder.show();
                return true;
            } else if (id == R.id.changePassword) {
                builder = new MaterialAlertDialogBuilder(MainActivity.this);
                builder.setTitle(R.string.app_name);

                layout.setOrientation(LinearLayout.VERTICAL);

                EditText inputPassword = new EditText(MainActivity.this);
                inputPassword.setHint(getString(R.string.newPassword));
                inputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                layout.addView(inputPassword);

                EditText inputPasswordRepeat = new EditText(MainActivity.this);
                inputPasswordRepeat.setHint(getString(R.string.repeatNewPassword));
                inputPasswordRepeat.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                layout.addView(inputPassword);

                builder.setView(layout);
                builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                    String newPassword = inputPassword.getText().toString();
                    String newPasswordRepeat = inputPasswordRepeat.getText().toString();
                    if (newPassword.equals(newPasswordRepeat)) {
                        new Thread(() -> {
                            try {
                                connection.changePassword(username, password, newPassword);
                            } catch (IOException | KeyManagementException | JSONException | NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                        }).start();
                        prefsEditor.putString("password", newPassword).apply();
                    } else {
                        Toast.makeText(this, getString(R.string.passwordsDontMatch), Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
                builder.show();
                return true;
            } else if (id == R.id.deleteAccount) {
                builder = new MaterialAlertDialogBuilder(MainActivity.this);
                builder.setTitle(R.string.app_name);
                builder.setMessage(getString(R.string.wantToDeleteAccount));

                builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    new Thread(() -> {
                        try {
                            connection.deleteAccount(username, password);
                        } catch (IOException | KeyManagementException | JSONException | NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                    }).start();
                    prefsEditor.remove("username").apply();
                    prefsEditor.remove("password").apply();
                    prefsEditor.remove("startCategory").apply();

                    Intent loggedOut = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(loggedOut);
                });
                builder.setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.cancel());
                builder.show();
                return true;
            } else if (id == R.id.searchviewBooks) {
                SearchView searchviewBooks = (SearchView) item.getActionView();
                searchviewBooks.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        booksAdapter.getFilter().filter(newText);
                        return true;
                    }
                });
                return true;
            } else {
                return false;
            }
        });
    }
}
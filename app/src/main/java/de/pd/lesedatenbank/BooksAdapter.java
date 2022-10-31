package de.pd.lesedatenbank;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.ViewHolder> implements Filterable {
    private SharedPreferences prefs;
    Connection connection;
    String username;
    String password;

    private Context context;
    private String category;
    private List<JSONObject> books;
    private List<JSONObject> originalBooks;

    public BooksAdapter(Context context, String category, List<JSONObject> books) {
        super();
        this.context = context;
        this.category = category;
        this.books = books;
        originalBooks = new ArrayList<>(books);

        openPrefs();

        connection = new Connection(context, prefs);
        username = prefs.getString("username", "");
        password = prefs.getString("password", "");
    }

    public class ViewHolder extends RecyclerView.ViewHolder  implements View.OnLongClickListener{
        public TextView textviewBookName;
        public CheckBox checkboxRead;

        public ViewHolder(View view) {
            super(view);

            view.setOnLongClickListener(this);

            this.setIsRecyclable(false);

            textviewBookName = view.findViewById(R.id.textviewBookName);
            checkboxRead = view.findViewById(R.id.checkboxRead);
        }

        @Override
        public boolean onLongClick(View view) {
            int position = getAbsoluteAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                JSONObject book = books.get(position);
                try {
                    MaterialAlertDialogBuilder builder;
                    LinearLayout layout = new LinearLayout(context);

                    builder = new MaterialAlertDialogBuilder(context);
                    builder.setTitle(R.string.app_name);

                    layout.setOrientation(LinearLayout.VERTICAL);

                    //Name
                    EditText inputBookName = new EditText(context);
                    inputBookName.setHint(context.getString(R.string.name));
                    inputBookName.setText(book.getString("bookName"));
                    //Show keyboard
                    inputBookName.requestFocus();
                    InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(inputBookName, InputMethodManager.SHOW_IMPLICIT);

                    layout.addView(inputBookName);

                    //Author
                    EditText inputAuthor = new EditText(context);
                    inputAuthor.setHint(context.getString(R.string.author));
                    inputAuthor.setText(book.getString("author"));
                    layout.addView(inputAuthor);

                    //Name
                    CheckBox inputRead = new CheckBox(context);
                    inputRead.setText(context.getString(R.string.read));
                    inputRead.setChecked(book.getBoolean("read"));
                    layout.addView(inputRead);

                    builder.setView(layout);
                    builder.setPositiveButton(context.getString(R.string.ok), (dialog1, which) -> new Thread(() -> {
                        try {
                            String oldBookName = book.getString("bookName");
                            String newBookName = inputBookName.getText().toString();
                            String author = inputAuthor.getText().toString();
                            boolean read = inputRead.isChecked();

                            connection.editBook(username, password, category, oldBookName, newBookName, author, read);
                            ((Activity) context).runOnUiThread(() -> {
                                try {
                                    books.set(position, new JSONObject(String.format("{\"bookName\": \"%s\", \"author\": \"%s\", \"read\": %b}", newBookName, author, read)));
                                    notifyItemChanged(position);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                        } catch (IOException | KeyManagementException | NoSuchAlgorithmException | JSONException e) {
                            e.printStackTrace();
                        }
                    }).start());
                    builder.setNegativeButton(context.getString(R.string.cancel), (dialog1, which) -> dialog1.cancel());
                    builder.show();
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View bookView = inflater.inflate(R.layout.list_item, parent, false);

        return new ViewHolder(bookView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView textviewBookName = holder.textviewBookName;
        CheckBox checkboxRead = holder.checkboxRead;

        try {
            String bookName = books.get(position).getString("bookName");
            String author = books.get(position).getString("author");
            boolean read = books.get(position).getBoolean("read");

            textviewBookName.setText(bookName);
            checkboxRead.setChecked(read);

            checkboxRead.setOnCheckedChangeListener((compoundButton, checked) -> new Thread(() -> {
                try {
                    connection.editBook(username, password, category, bookName, bookName, author, checked);
                } catch (IOException | KeyManagementException | NoSuchAlgorithmException | JSONException e) {
                    e.printStackTrace();
                }
            }).start());
        } catch (JSONException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<JSONObject> filteredBooks = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredBooks.addAll(originalBooks);
                } else {
                    for (JSONObject book: originalBooks) {
                        try {
                            if (book.getString("bookName").toLowerCase().contains(constraint.toString().toLowerCase().trim())) {
                                filteredBooks.add(book);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredBooks;

                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                books.clear();
                books.addAll((List<JSONObject>) results.values);
                notifyDataSetChanged();
            }
        };
    }

public String getCategory() {
        return category;
    }

    public List<JSONObject> getBooks() {
        return books;
    }
    @SuppressLint("NotifyDataSetChanged")
    public void setBooks(List<JSONObject> items) {
        books = items;
        originalBooks = new ArrayList<>(items);
        notifyDataSetChanged();
    }
    public JSONObject getBook(int position) {
        return books.get(position);
    }
    public void addItem(JSONObject item) {
        books.add(item);
        notifyItemInserted(books.indexOf(item));
    }
    public void insertBook(int position, JSONObject item) {
        books.add(position, item);
        notifyItemInserted(position);
    }

    public void removeBook(int position) {
        books.remove(position);
        notifyItemRemoved(position);
    }

    private void openPrefs() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            KeyGenParameterSpec keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC;

            try {
                String keyAlias = MasterKeys.getOrCreate(keyGenParameterSpec);

                prefs = EncryptedSharedPreferences.create(
                        "de.pd.lesedatenbank",
                        keyAlias,
                        context.getApplicationContext(),
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }

        } else {
            prefs = context.getSharedPreferences("de.pd.lesedatenbank", Context.MODE_PRIVATE);
        }
    }
}
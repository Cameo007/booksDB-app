package de.pd.bdb;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Connection {
    private Context context;
    private SharedPreferences prefs;

    public Connection(Context context, SharedPreferences prefs) {
        this.context = context;
        this.prefs = prefs;
    }

    public boolean authenticate(String username, String password) throws IOException, KeyManagementException, NoSuchAlgorithmException, JSONException {
        String apiEndpoint = prefs.getString("apiEndpoint", "https://mint.jojojux.de/api/bdb.php") + "/account/authenticate";
        String url = Uri.parse(apiEndpoint)
                .buildUpon()
                .appendQueryParameter("username", username)
                .appendQueryParameter("password", password)
                .appendQueryParameter("lang", Locale.getDefault().getLanguage())
                .build().toString();

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{}; }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        OkHttpClient.Builder newBuilder = new OkHttpClient.Builder();
        newBuilder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        newBuilder.hostnameVerifier((hostname, session) -> true);

        OkHttpClient client = newBuilder.build();

        Log.i("MINT1", url);

        try (Response response = client.newCall(new Request.Builder().url(url).build()).execute()) {
            JSONObject body = new JSONObject(Objects.requireNonNull(response.body()).string());
            if (response.code() == 200) {
                return body.getBoolean("content");
            } else {
                toast(body.getString("content"));
                return false;
            }
        }
    }

    public boolean isUsernameAvailable(String username) throws IOException, KeyManagementException, NoSuchAlgorithmException, JSONException {
        String apiEndpoint = prefs.getString("apiEndpoint", "https://mint.jojojux.de/api/bdb.php") + "/account/isUsernameAvailable";
        String url = Uri.parse(apiEndpoint)
                .buildUpon()
                .appendQueryParameter("username", username)
                .appendQueryParameter("lang", Locale.getDefault().getLanguage())
                .build().toString();

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{}; }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        builder.hostnameVerifier((hostname, session) -> true);

        OkHttpClient client = builder.build();

        try (Response response = client.newCall(new Request.Builder().url(url).build()).execute()) {
            JSONObject body = new JSONObject(Objects.requireNonNull(response.body()).string());
            if (response.code() == 200) {
                return new JSONObject(Objects.requireNonNull(response.body()).string()).getBoolean("content");
            } else {
                toast(body.getString("content"));
                return false;
            }
        }
    }

    public void register(String username, String password) throws IOException, KeyManagementException, NoSuchAlgorithmException, JSONException {
        String apiEndpoint = prefs.getString("apiEndpoint", "https://mint.jojojux.de/api/bdb.php") + "/account/register";

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{}; }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        builder.hostnameVerifier((hostname, session) -> true);

        OkHttpClient client = builder.build();

        RequestBody data = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("lang", Locale.getDefault().getLanguage())
                .build();

        Request request = new Request.Builder()
                .url(apiEndpoint)
                .post(data)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200) {
                toast(new JSONObject(Objects.requireNonNull(response.body()).string()).getString("content"));
            }
        }
    }

    public void changeUsername(String username, String password, String newUsername) throws IOException, KeyManagementException, NoSuchAlgorithmException, JSONException {
        String apiEndpoint = prefs.getString("apiEndpoint", "https://mint.jojojux.de/api/bdb.php") + "/account/changeUsername";

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{}; }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        builder.hostnameVerifier((hostname, session) -> true);

        OkHttpClient client = builder.build();

        RequestBody data = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("newUsername", newUsername)
                .add("lang", Locale.getDefault().getLanguage())
                .build();

        Request request = new Request.Builder()
                .url(apiEndpoint)
                .post(data)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200) {
                toast(new JSONObject(Objects.requireNonNull(response.body()).string()).getString("content"));
            }
        }
    }

    public void changePassword(String username, String password, String newPassword) throws IOException, KeyManagementException, NoSuchAlgorithmException, JSONException {
        String apiEndpoint = prefs.getString("apiEndpoint", "https://mint.jojojux.de/api/bdb.php") + "/account/changePassword";

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{}; }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        builder.hostnameVerifier((hostname, session) -> true);

        OkHttpClient client = builder.build();

        RequestBody data = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("newPassword", newPassword)
                .add("lang", Locale.getDefault().getLanguage())
                .build();

        Request request = new Request.Builder()
                .url(apiEndpoint)
                .post(data)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200) {
                toast(new JSONObject(Objects.requireNonNull(response.body()).string()).getString("content"));
            }
        }
    }

    public void deleteAccount(String username, String password) throws IOException, KeyManagementException, NoSuchAlgorithmException, JSONException {
        String apiEndpoint = prefs.getString("apiEndpoint", "https://mint.jojojux.de/api/bdb.php") + "/account/deleteAccount";

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{}; }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        builder.hostnameVerifier((hostname, session) -> true);

        OkHttpClient client = builder.build();

        RequestBody data = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("lang", Locale.getDefault().getLanguage())
                .build();

        Request request = new Request.Builder()
                .url(apiEndpoint)
                .post(data)
                .build();
        try (Response response = client.newCall(request).execute()) {
            toast(new JSONObject(Objects.requireNonNull(response.body()).string()).getString("content"));
        }
    }


    public List<String> getCategories(String username, String password) throws NoSuchAlgorithmException, KeyManagementException, IOException, JSONException {
        String apiEndpoint = prefs.getString("apiEndpoint", "https://mint.jojojux.de/api/bdb.php") + "/category/getAll";
        String url = Uri.parse(apiEndpoint)
                .buildUpon()
                .appendQueryParameter("username", username)
                .appendQueryParameter("password", password)
                .appendQueryParameter("lang", Locale.getDefault().getLanguage())
                .build().toString();

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{}; }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        builder.hostnameVerifier((hostname, session) -> true);

        OkHttpClient client = builder.build();

        try (Response response = client.newCall(new Request.Builder().url(url).build()).execute()) {
            JSONObject body = new JSONObject(Objects.requireNonNull(response.body()).string());
            if (response.code() == 200) {
                JSONArray jsonCategories = body.getJSONArray("content");
                List<String> categories = new ArrayList<>();
                for (int i = 0; i < jsonCategories.length(); i++) {
                    categories.add(jsonCategories.get(i).toString());
                }
                return categories;
            } else {
                toast(body.getString("content"));
                return new ArrayList<>();
            }
        }
    }
    public List<JSONObject> getBooks(String username, String password, String categoryName) throws NoSuchAlgorithmException, KeyManagementException, IOException, JSONException {
        String apiEndpoint = prefs.getString("apiEndpoint", "https://mint.jojojux.de/api/bdb.php") + "/book/getAll";
        String url = Uri.parse(apiEndpoint)
                .buildUpon()
                .appendQueryParameter("username", username)
                .appendQueryParameter("password", password)
                .appendQueryParameter("categoryName", categoryName)
                .appendQueryParameter("lang", Locale.getDefault().getLanguage())
                .build().toString();

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{}; }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        builder.hostnameVerifier((hostname, session) -> true);

        OkHttpClient client = builder.build();

        try (Response response = client.newCall(new Request.Builder().url(url).build()).execute()) {
            JSONObject body = new JSONObject(Objects.requireNonNull(response.body()).string());
            if (response.code() == 200) {
                JSONArray jsonBooks = body.getJSONArray("content");
                List<JSONObject> books = new ArrayList<>();
                for (int i = 0; i < jsonBooks.length(); i++) {
                    books.add(jsonBooks.getJSONObject(i));
                }
                return books;
            } else {
                toast(body.getString("content"));
                return new ArrayList<>();
            }
        }
    }

    public void addCategory(String username, String password, String categoryName) throws KeyManagementException, NoSuchAlgorithmException, IOException, JSONException {
        String apiEndpoint = prefs.getString("apiEndpoint", "https://mint.jojojux.de/api/bdb.php") + "/category/add";

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{}; }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        builder.hostnameVerifier((hostname, session) -> true);

        OkHttpClient client = builder.build();

        RequestBody data = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("categoryName", categoryName)
                .add("lang", Locale.getDefault().getLanguage())
                .build();

        Request request = new Request.Builder()
                .url(apiEndpoint)
                .post(data)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200) {
                toast(new JSONObject(Objects.requireNonNull(response.body()).string()).getString("content"));
            }
        }
    }

    public void deleteCategory(String username, String password, String categoryName) throws KeyManagementException, NoSuchAlgorithmException, IOException, JSONException {
        String apiEndpoint = prefs.getString("apiEndpoint", "https://mint.jojojux.de/api/bdb.php") + "/category/delete";

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{}; }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        builder.hostnameVerifier((hostname, session) -> true);

        OkHttpClient client = builder.build();

        RequestBody data = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("categoryName", categoryName)
                .add("lang", Locale.getDefault().getLanguage())
                .build();

        Request request = new Request.Builder()
                .url(apiEndpoint)
                .post(data)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200) {
                toast(new JSONObject(Objects.requireNonNull(response.body()).string()).getString("content"));
            }
        }
    }

    public void renameCategory(String username, String password, String oldCategoryName, String newCategoryName) throws KeyManagementException, NoSuchAlgorithmException, IOException, JSONException {
        String apiEndpoint = prefs.getString("apiEndpoint", "https://mint.jojojux.de/api/bdb.php") + "/category/rename";

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{}; }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        builder.hostnameVerifier((hostname, session) -> true);

        OkHttpClient client = builder.build();

        RequestBody data = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("oldCategoryName", oldCategoryName)
                .add("newCategoryName", newCategoryName)
                .add("lang", Locale.getDefault().getLanguage())
                .build();

        Request request = new Request.Builder()
                .url(apiEndpoint)
                .post(data)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200) {
                toast(new JSONObject(Objects.requireNonNull(response.body()).string()).getString("content"));
            }
        }
    }

    public void addBook(String username, String password, String categoryName, String bookName, String author, Boolean read) throws KeyManagementException, NoSuchAlgorithmException, IOException, JSONException {
        String apiEndpoint = prefs.getString("apiEndpoint", "https://mint.jojojux.de/api/bdb.php") + "/book/add";

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{}; }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        builder.hostnameVerifier((hostname, session) -> true);

        OkHttpClient client = builder.build();

        RequestBody data = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("categoryName", categoryName)
                .add("bookName", bookName)
                .add("author", author)
                .add("read", read.toString())
                .add("lang", Locale.getDefault().getLanguage())
                .build();

        Request request = new Request.Builder()
                .url(apiEndpoint)
                .post(data)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200) {
                toast(new JSONObject(Objects.requireNonNull(response.body()).string()).getString("content"));
            }
        }
    }

    public void deleteBook(String username, String password, String categoryName, String bookName) throws KeyManagementException, NoSuchAlgorithmException, IOException, JSONException {
        String apiEndpoint = prefs.getString("apiEndpoint", "https://mint.jojojux.de/api/bdb.php") + "/book/delete";

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{}; }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        builder.hostnameVerifier((hostname, session) -> true);

        OkHttpClient client = builder.build();

        RequestBody data = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("categoryName", categoryName)
                .add("bookName", bookName)
                .add("lang", Locale.getDefault().getLanguage())
                .build();

        Request request = new Request.Builder()
                .url(apiEndpoint)
                .post(data)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200) {
                toast(new JSONObject(Objects.requireNonNull(response.body()).string()).getString("content"));
            }
        }
    }

    public void editBook(String username, String password, String categoryName, String oldBookName, String newBookName, String author, Boolean read) throws IOException, KeyManagementException, NoSuchAlgorithmException, JSONException {
        String apiEndpoint = prefs.getString("apiEndpoint", "https://mint.jojojux.de/api/bdb.php") + "/book/edit";

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{}; }
                }
        };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
        builder.hostnameVerifier((hostname, session) -> true);

        OkHttpClient client = builder.build();

        RequestBody data = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("categoryName", categoryName)
                .add("oldBookName", oldBookName)
                .add("newBookName", newBookName)
                .add("author", author)
                .add("read", read.toString())
                .add("lang", Locale.getDefault().getLanguage())
                .build();

        Request request = new Request.Builder()
                .url(apiEndpoint)
                .post(data)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200) {
                toast(new JSONObject(Objects.requireNonNull(response.body()).string()).getString("content"));
            }
        }
    }

    private void toast(String text) {
        ((Activity) context).runOnUiThread(() -> Toast.makeText(context, text, Toast.LENGTH_SHORT).show());
    }
}

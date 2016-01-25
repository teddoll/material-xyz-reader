package com.example.xyzreader.data;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.example.xyzreader.R;
import com.example.xyzreader.remote.HttpClientProvider;
import com.example.xyzreader.remote.OttoBusProvider;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class UpdaterService extends IntentService {
    private static final String TAG = "UpdaterService";

    public static final String BROADCAST_ACTION_STATE_CHANGE
            = "com.example.xyzreader.intent.action.STATE_CHANGE";
    public static final String EXTRA_REFRESHING
            = "com.example.xyzreader.intent.extra.REFRESHING";

    public UpdaterService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean success = false;
        if (hasNetwork()) {

            // Don't even inspect the intent, we only do one thing, and that's fetch content.
            success = updateData();
        }
        OttoBusProvider.getInstance().getBus().post(new UpdateCompleteEvent(success));

    }


    private boolean hasNetwork() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            return true;
        }
        Log.d(TAG, "Not online, not refreshing.");
        return false;
    }

    private boolean updateData() {
        JSONArray json;
        try {
            json = new JSONArray(getData(new URL(getString(R.string.base_url))));
        } catch (JSONException | IOException e) {
            Log.d(TAG, "Failed to retrieve data", e);
            json = null;
        }

        try {
            processData(json);
            return true;
        } catch (JSONException e) {
            Log.d(TAG, "Failed to parse data", e);
        } catch (RemoteException | OperationApplicationException e) {
            Log.d(TAG, "Failed to save data to provider", e);
        }
        return false;


    }

    private String getData(URL url) throws IOException {
        OkHttpClient client = HttpClientProvider.getInstance(this).getHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        if (response.isSuccessful())
            return response.body().string();

        return null;
    }

    private void processData(JSONArray array) throws JSONException, RemoteException, OperationApplicationException {
        if (array == null) return;

        ArrayList<ContentProviderOperation> cpo = new ArrayList<>();

        Uri dirUri = ItemsContract.Items.buildDirUri();

        // Delete all items
        cpo.add(ContentProviderOperation.newDelete(dirUri).build());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        for (int i = 0; i < array.length(); i++) {
            ContentValues values = new ContentValues();
            JSONObject object = array.getJSONObject(i);
            values.put(ItemsContract.Items.SERVER_ID, object.getString("id"));
            values.put(ItemsContract.Items.AUTHOR, object.getString("author"));
            values.put(ItemsContract.Items.TITLE, object.getString("title"));
            values.put(ItemsContract.Items.BODY, object.getString("body"));
            values.put(ItemsContract.Items.THUMB_URL, object.getString("thumb"));
            values.put(ItemsContract.Items.PHOTO_URL, object.getString("photo"));
            values.put(ItemsContract.Items.ASPECT_RATIO, object.getString("aspect_ratio"));
            try {
                Date date = format.parse(object.getString("published_date"));
                values.put(ItemsContract.Items.PUBLISHED_DATE, date.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            cpo.add(ContentProviderOperation.newInsert(dirUri).withValues(values).build());
        }

        getContentResolver().applyBatch(ItemsContract.CONTENT_AUTHORITY, cpo);
    }

    public static class UpdateCompleteEvent {
        public final boolean success;

        public UpdateCompleteEvent(boolean success) {
            this.success = success;
        }
    }
}

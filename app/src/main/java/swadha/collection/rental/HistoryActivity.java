package swadha.collection.rental;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private List<HistoryBooking> allHistory = new ArrayList<>();
    private HistoryBookingAdapter adapter;
    private TextView tvTotalEarnings;
    // Use a different key for history so it doesn't overwrite the Dashboard cache
    private static final String HISTORY_CACHE_KEY = "history_cache_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        tvTotalEarnings = findViewById(R.id.tvTotalEarnings);
        RecyclerView rv = findViewById(R.id.rvHistory);
        EditText etSearch = findViewById(R.id.etHistorySearch);

        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(this));
            adapter = new HistoryBookingAdapter(new ArrayList<>());
            rv.setAdapter(adapter);
        }

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applyFilters(s.toString());
                }
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        // STEP 1: Load cache immediately so the screen isn't empty
        loadHistoryFromCache();

        // STEP 2: Fetch fresh data from the server
        fetchFullHistory();
    }

    private void applyFilters(String query) {
        List<HistoryBooking> filtered = new ArrayList<>();
        double total = 0;

        for (HistoryBooking b : allHistory) {

            boolean matchesSearch =
                    b.getName().toLowerCase().contains(query.toLowerCase()) ||
                            b.getItemNo().toLowerCase().contains(query.toLowerCase()) ||
                            b.getPhone().contains(query);

            if (matchesSearch) {
                filtered.add(b);
                total += b.getRentPaid();   // 🔥 earnings = rentPaid
            }
        }

        adapter = new HistoryBookingAdapter(filtered);
        RecyclerView rv = findViewById(R.id.rvHistory);
        rv.setAdapter(adapter);

        tvTotalEarnings.setText("Filtered Earnings: ₹ " + total);
    }
    private void calculateEarnings(List<HistoryBooking> list) {
        double total = 0;
        for (HistoryBooking b : list) {
            total += b.getNetIncome();
        }
        tvTotalEarnings.setText("Total Earnings: ₹ " + total);
    }

    private void saveHistoryToCache(String jsonResponse) {
        getSharedPreferences("RentalPrefs", MODE_PRIVATE)
                .edit()
                .putString(HISTORY_CACHE_KEY, jsonResponse)
                .apply();
    }

    private void loadHistoryFromCache() {
        String cachedData = getSharedPreferences("RentalPrefs", MODE_PRIVATE).getString(HISTORY_CACHE_KEY, null);
        if (cachedData != null) {
            parseHistoryJson(cachedData);
        }
    }

    private void parseHistoryJson(String jsonString) {
        try {
            org.json.JSONArray response = new org.json.JSONArray(jsonString);
            allHistory.clear();

            for (int i = 0; i < response.length(); i++) {

                JSONObject obj = response.getJSONObject(i);

                allHistory.add(new HistoryBooking(
                        obj.optString("timestamp"),
                        obj.optString("itemNo"),
                        obj.optString("name"),
                        obj.optString("phone"),
                        obj.optString("pickupDateTime"),
                        obj.optString("returnDateTime"),

                        obj.optDouble("totalRent", 0.0),
                        obj.optDouble("deposit", 0.0),
                        obj.optDouble("rentPaid", 0.0),
                        obj.optDouble("balance", 0.0),

                        obj.optString("status", ""),
                        obj.optString("actualPickup", ""),
                        obj.optString("actualReceive", "")
                ));
            }

            // Refresh adapter
            adapter = new HistoryBookingAdapter(allHistory);
            RecyclerView rv = findViewById(R.id.rvHistory);
            rv.setAdapter(adapter);

            calculateEarnings(allHistory);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fetchFullHistory() {
        // We only show the dialog if there is NO cached data (first time install)
        boolean showDialog = allHistory.isEmpty();
        android.app.ProgressDialog progressDialog = null;

        if (showDialog) {
            progressDialog = new android.app.ProgressDialog(this);
            progressDialog.setMessage("Fetching all records...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        String url = "https://script.google.com/macros/s/AKfycby9Bfc8ohJDS6bvWDu1I8E21yxzRg_GQBhpRXkzY9hLfcKrDlqzxYe2LyMl4Vmb6CXj/exec?mode=history";

        final android.app.ProgressDialog finalDialog = progressDialog;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (finalDialog != null) finalDialog.dismiss();

                    // Save fresh data to cache and update UI
                    saveHistoryToCache(response.toString());
                    parseHistoryJson(response.toString());
                },
                error -> {
                    if (finalDialog != null) finalDialog.dismiss();
                    Log.e("VOLLEY", error.toString());
                    if (allHistory.isEmpty()) {
                        Toast.makeText(this, "Check your connection", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Offline Mode: Showing cached history", Toast.LENGTH_SHORT).show();
                    }
                });

        request.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                30000, // Increased to 30s for full history download
                0,
                com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Volley.newRequestQueue(this).add(request);
    }

    // ... applyFilters and calculateEarnings stay the same ...
}
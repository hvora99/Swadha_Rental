package swadha.collection.rental;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest; // Added
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List; // Added

public class ItemActivity extends AppCompatActivity {

    private List<ItemModel> itemList = new ArrayList<>();
    private ItemAdapter adapter;
    private static final String ITEM_CACHE = "item_master_cache";
    // Make sure this URL matches your script URL
    private static final String webAppUrl = "https://script.google.com/macros/s/AKfycby9Bfc8ohJDS6bvWDu1I8E21yxzRg_GQBhpRXkzY9hLfcKrDlqzxYe2LyMl4Vmb6CXj/exec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_master);

        RecyclerView rv = findViewById(R.id.rvItems);
        rv.setLayoutManager(new GridLayoutManager(this, 2));


        adapter = new ItemAdapter(itemList);
        rv.setAdapter(adapter);

        // 1. Show saved inventory immediately
        loadItemsFromCache();

        // 2. Update inventory from Google Sheet
        fetchItemsFromServer();

        findViewById(R.id.fabAddItem).setOnClickListener(v -> showAddItemDialog());
    }



    private void fetchItemsFromServer() {
        // mode=items tells the script to read the "Items" tab
        String url = webAppUrl + "?mode=items&showLocked=true";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    saveItemsToCache(response.toString());
                    parseItemsJson(response.toString());
                }, error ->
        {

            Log.e("API_ERROR", error.toString());
            Toast.makeText(this, "Showing Cached Inventory", Toast.LENGTH_SHORT).show();
        });

        Volley.newRequestQueue(this).add(request);
    }

    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);

        EditText etNo = dialogView.findViewById(R.id.etDialogItemNo);
        EditText etName = dialogView.findViewById(R.id.etDialogItemName);
        EditText etRent = dialogView.findViewById(R.id.etDialogRent);
        EditText etDeposit = dialogView.findViewById(R.id.etDialogDeposit);
        Button btnSave = dialogView.findViewById(R.id.btnSaveItem);
        CheckBox cbWash = dialogView.findViewById(R.id.cbDialogRequiresWash);


        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String no = etNo.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String rent = etRent.getText().toString().trim();
            String dep = etDeposit.getText().toString().trim();

            if (no.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Item No and Name are required", Toast.LENGTH_SHORT).show();
                return;
            }

            sendItemToSheet(no, name, rent, dep, cbWash.isChecked(), dialog);
        });

        dialog.show();
    }

    private void sendItemToSheet(String no, String name, String rent, String dep,
                                 boolean requiresWash,
                                 AlertDialog dialog) {
        JSONObject params = new JSONObject();
        try {
            params.put("action", "addItem"); // Action for Google Script
            params.put("itemNo", no);
            params.put("itemName", name);
            params.put("rent", rent);
            params.put("deposit", dep);
            params.put("requiresWash", requiresWash);   // 🔥 ADD THIS

        } catch (Exception e) { e.printStackTrace(); }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, webAppUrl, params,
                response -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Success! Item saved.", Toast.LENGTH_SHORT).show();
                    fetchItemsFromServer(); // Reload list to show new item
                }, error -> {
            // Sometimes Google Scripts return a parse error even if successful
            dialog.dismiss();
            fetchItemsFromServer();
        });

        Volley.newRequestQueue(this).add(request);
    }

    private void parseItemsJson(String json) {
        try {
            JSONArray arr = new JSONArray(json);
            itemList.clear();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                // Log the data to your Logcat so you can see what the server is actually sending
                Log.d("ITEM_DEBUG", "Row " + i + ": " + obj.toString());

                itemList.add(new ItemModel(
                        obj.optString("itemNo", "N/A"),
                        obj.optString("itemName", "N/A"),
                        obj.optDouble("rent", 0),
                        obj.optDouble("deposit", 0),
                        obj.optBoolean("requiresWash", false),
                        obj.optBoolean("isLocked", false),
                        obj.optLong("nextAvailableMs", 0),
                        obj.optString("status", "available")
                ));
            }

            // IMPORTANT: Run on UI Thread to ensure the list updates
            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                if (itemList.isEmpty()) {
                    Toast.makeText(this, "No items found in Sheet", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e("ITEM_ERROR", "Parsing failed", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            fetchItemsFromServer();
        }
    }

    private void saveItemsToCache(String data) {
        getSharedPreferences("RentalPrefs", MODE_PRIVATE).edit().putString(ITEM_CACHE, data).apply();
    }

    private void loadItemsFromCache() {
        String data = getSharedPreferences("RentalPrefs", MODE_PRIVATE).getString(ITEM_CACHE, null);
        if (data != null) parseItemsJson(data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchItemsFromServer();
    }
}
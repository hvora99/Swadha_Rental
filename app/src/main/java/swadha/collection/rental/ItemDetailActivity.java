package swadha.collection.rental;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ItemDetailActivity extends AppCompatActivity {

    private String itemNo;
    private static final String webAppUrl = "https://script.google.com/macros/s/AKfycby9Bfc8ohJDS6bvWDu1I8E21yxzRg_GQBhpRXkzY9hLfcKrDlqzxYe2LyMl4Vmb6CXj/exec";
    EditText etName, etRent, etDeposit;
    boolean isLocked;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        itemNo = getIntent().getStringExtra("itemNo");
        String itemName = getIntent().getStringExtra("itemName");
        double rent = getIntent().getDoubleExtra("rent", 0);
        double deposit = getIntent().getDoubleExtra("deposit", 0);
        isLocked = getIntent().getBooleanExtra("isLocked", false);


        TextView tvCode = findViewById(R.id.tvDetailItemCode);
        etName = findViewById(R.id.etDetailItemName);
        etRent = findViewById(R.id.etDetailRent);
        etDeposit = findViewById(R.id.etDetailDeposit);


        Button btnToggleLock = findViewById(R.id.btnToggleLock);
       // btnToggleLock.setText(isLocked ? "Unlock Item" : "Lock Item");
        updateLockButtonUI(btnToggleLock);


        btnToggleLock.setOnClickListener(v -> {

            JSONObject params = new JSONObject();
            try {
                params.put("action", "toggleLock");
                params.put("itemNo", itemNo);
                params.put("lock", !isLocked);
            } catch (Exception ignored) {}

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    webAppUrl,
                    params,
                    response -> {

                        if (response.optString("status").equals("success")) {

                            isLocked = !isLocked;
                            btnToggleLock.setText(isLocked ? "Unlock Item" : "Lock Item");

                            Toast.makeText(this,
                                    isLocked ? "Item Locked" : "Item Unlocked",
                                    Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(this,
                            "Lock update failed",
                            Toast.LENGTH_SHORT).show()
            );

            Volley.newRequestQueue(this).add(request);
        });

        tvCode.setText(itemNo);
        etName.setText(itemName);
        etRent.setText(String.valueOf(rent));
        etDeposit.setText(String.valueOf(deposit));

        Button btnUpdate = findViewById(R.id.btnUpdateItem);
        Button btnRemove = findViewById(R.id.btnRemoveItem);

            btnUpdate.setOnClickListener(v -> {

                String newName = etName.getText().toString().trim();
                String newRent = etRent.getText().toString().trim();
                String newDeposit = etDeposit.getText().toString().trim();

                if (newName.isEmpty()) {
                    Toast.makeText(this, "Item name required", Toast.LENGTH_SHORT).show();
                    return;
                }

                updateItemOnServer(newName, newRent, newDeposit);
            });

        btnRemove.setOnClickListener(v -> {

                new AlertDialog.Builder(this)
                        .setTitle("Remove Item")
                        .setMessage("Are you sure you want to remove this item?")
                        .setPositiveButton("Remove", (dialog, which) -> {
                            deleteItemFromServer(itemNo);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
        });

        // Next step: load current booking
        loadCurrentBooking();
    }

    private void updateLockButtonUI(Button button) {

        if (isLocked) {
            button.setText("Unlock Item");
            button.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.RED)
            );
        } else {
            button.setText("Lock Item");
            button.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800"))
            );
        }
    }

    private void loadCurrentBooking() {

        RecyclerView rv = findViewById(R.id.rvCurrentBooking);
        TextView tvNoBooking = findViewById(R.id.tvNoBooking);
        Button btnRemove = findViewById(R.id.btnRemoveItem);

        rv.setLayoutManager(new LinearLayoutManager(this));

        String url = webAppUrl + "?mode=currentBooking&itemNo=" + itemNo;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {

                    String status = response.optString("status", "available");

                    if ("booked".equals(status)) {

                        JSONArray bookingArray = response.optJSONArray("bookings");

                        if (bookingArray != null && bookingArray.length() > 0) {

                            List<CurrentBookingModel> list = new ArrayList<>();

                            for (int i = 0; i < bookingArray.length(); i++) {

                                JSONObject obj = bookingArray.optJSONObject(i); // safer
                                if (obj == null) continue;

                                list.add(new CurrentBookingModel(
                                        obj.optString("name"),
                                        obj.optString("phone"),
                                        obj.optString("pickup"),
                                        obj.optString("return"),
                                        obj.optString("total"),
                                        obj.optString("advance"),
                                        obj.optString("balance")
                                ));
                            }

                            rv.setAdapter(new CurrentBookingAdapter(list));

                            rv.setVisibility(View.VISIBLE);
                            tvNoBooking.setVisibility(View.GONE);
                            btnRemove.setEnabled(false);
                        }

                    } else {

                        rv.setAdapter(null);
                        rv.setVisibility(View.GONE);
                        tvNoBooking.setVisibility(View.VISIBLE);
                        btnRemove.setEnabled(true);
                    }
                },
                error -> {

                        Log.e("BOOKING_ERROR", error.toString());

                        if (error.networkResponse != null) {
                            Log.e("BOOKING_ERROR_BODY",
                                    new String(error.networkResponse.data));
                        }

                        Toast.makeText(this, "Error loading booking", Toast.LENGTH_SHORT).show();
                    }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void updateItemOnServer(String name, String rent, String deposit) {

        JSONObject params = new JSONObject();

        try {
            params.put("action", "updateItem");
            params.put("itemNo", itemNo);
            params.put("itemName", name);
            params.put("rent", rent);
            params.put("deposit", deposit);
        } catch (Exception ignored) {}

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                webAppUrl,
                params,
                response -> {

                    if (response.optString("status").equals("success")) {
                        Toast.makeText(this, "Item Updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                response.optString("message"),
                                Toast.LENGTH_LONG).show();
                    }

                },
                error -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void deleteItemFromServer(String itemNo) {

        JSONObject params = new JSONObject();
        try {
            params.put("action", "deleteItem");
            params.put("itemNo", itemNo);
        } catch (Exception ignored) {}

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                webAppUrl,
                params,
                response -> {

                    if (response.optString("status").equals("success")) {

                        Toast.makeText(this, "Item Removed", Toast.LENGTH_SHORT).show();

                        setResult(RESULT_OK);   // 👈 Important
                        finish();               // 👈 Go back

                    } else {
                        Toast.makeText(this,
                                response.optString("message"),
                                Toast.LENGTH_LONG).show();
                    }

                },
                error ->
                {
                        Log.e("API_ERROR", error.toString());
                        Toast.makeText(this, "API Failed", Toast.LENGTH_SHORT).show();

                    Toast.makeText(this, "Error deleting item", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }


}
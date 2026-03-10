package swadha.collection.rental;

import android.content.Intent;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReturnDetailActivity extends AppCompatActivity {

    private String itemNo;
    private static final String webAppUrl = "https://script.google.com/macros/s/AKfycby9Bfc8ohJDS6bvWDu1I8E21yxzRg_GQBhpRXkzY9hLfcKrDlqzxYe2LyMl4Vmb6CXj/exec";
    MaterialButton BtnMarkReceived;
    MaterialButton btnCancel;
    MaterialButton btnPickedUp;
    private String bookingTimestamp;
    private LinearLayout layoutItemTimeline;
    private String orderId;
    ArrayList<RentalBooking.ItemStatus> itemsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_detail);

        itemNo = getIntent().getStringExtra("itemNo");
        String name = getIntent().getStringExtra("name");
        String phone = getIntent().getStringExtra("phone");
        long pickupMs = getIntent().getLongExtra("pickupDateTime", 0);
        long returnMs = getIntent().getLongExtra("returnDateTime", 0);



        double totalRent = getIntent().getDoubleExtra("totalRent", 0.0);
        double deposit = getIntent().getDoubleExtra("deposit", 0.0);
        double rentPaid = getIntent().getDoubleExtra("rentPaid", 0.0);
        double balance = getIntent().getDoubleExtra("balance", 0.0);
        orderId = getIntent().getStringExtra("orderId");
        String items = getIntent().getStringExtra("items");

        long washMs = getIntent().getLongExtra("washDateTime",0);
        long actualPickupMs = getIntent().getLongExtra("actualPickupDateTime",0);

        itemsList =
                (ArrayList<RentalBooking.ItemStatus>)
                        getIntent().getSerializableExtra("itemsList");




        bookingTimestamp = getIntent().getStringExtra("timestamp");
        if (bookingTimestamp == null) {
            Toast.makeText(this, "Invalid booking data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        String status = getIntent().getStringExtra("status");

        Log.d("DETAIL_DEBUG", "Received timestamp: " + bookingTimestamp);

// Views
        TextView tvItem = findViewById(R.id.detItemNo);
        TextView tvName = findViewById(R.id.detName);
        TextView tvPhone = findViewById(R.id.detPhone);
        TextView tvPickup = findViewById(R.id.detPickup);
        TextView tvReturn = findViewById(R.id.detReturn);
        TextView tvBalance = findViewById(R.id.detBalance);
        TextView tvTotal = findViewById(R.id.detTotal);
        TextView tvAdvance = findViewById(R.id.detAdvance);
        TextView tvDeposit = findViewById(R.id.detDeposit);
        TextView tvStatus = findViewById(R.id.tvStatus);
        TextView tvWash = findViewById(R.id.detWash);
        TextView tvOrder = findViewById(R.id.tvOrderId);
        TextView tvActualPickup = findViewById(R.id.detActualPickup);
        layoutItemTimeline = findViewById(R.id.layoutItemTimeline);
        btnPickedUp = findViewById(R.id.btnPickedUp);
        btnCancel = findViewById(R.id.btnCancelBooking);
        BtnMarkReceived = findViewById(R.id.btnMarkReceived);


        if(itemsList == null){
            itemsList = new ArrayList<>();
        }

        for(Object obj : itemsList){
            Log.d("ITEM_TYPE", obj.getClass().getName());
        }

        renderItemTimeline(extractItemCodes(itemsList), pickupMs, returnMs);

// Set Data
        tvItem.setText(items);
        tvName.setText(name);
        tvPhone.setText("Phone: " + (phone != null ? phone : "N/A"));
        tvOrder.setText(orderId != null ? "Order ID: " + orderId : "Order ID: N/A");

        Date pickupDate = new Date(pickupMs);
        Date returnDate = new Date(returnMs);

        SimpleDateFormat format =
                new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());

        tvPickup.setText(format.format(pickupDate));
        tvReturn.setText(format.format(returnDate));

        tvTotal.setText("₹ " + String.format("%.2f", totalRent));
        tvAdvance.setText("₹ " + String.format("%.2f", rentPaid));
        tvDeposit.setText("₹ " + String.format("%.2f", deposit));

        tvStatus.setText("Status: " + status);


        double rentDue = totalRent - rentPaid;

        if (rentDue > 0) {

            tvBalance.setTextColor(Color.parseColor("#D32F2F"));
            tvBalance.setText("Collect ₹ " + String.format("%.2f", rentDue));

        } else {

            double refund = deposit + Math.abs(rentDue);

            tvBalance.setTextColor(Color.parseColor("#2E7D32"));
            tvBalance.setText("Refund ₹ " + String.format("%.2f", refund));
        }


        Date washDate = new Date(washMs);
        tvWash.setText(format.format(washDate));



        if(washMs > 0){
            tvWash.setText(format.format(new Date(washMs)));
        }

        if(actualPickupMs > 0){
            tvActualPickup.setText(format.format(new Date(actualPickupMs)));
        }


        BtnMarkReceived.setOnClickListener(v -> {
            openItemSelectionDialog("Select Items Returned", itemsList);
        });

        btnPickedUp.setOnClickListener(v -> {

            showPickupDialog(
                    orderId,
                    getPickupAllowedItems(),
                    totalRent,
                    rentPaid
            );

        });

        btnCancel.setOnClickListener(v -> {

            showCancelDialog(
                    orderId,
                    getCancelableItems(),
                    deposit,
                    rentPaid
            );

        });


// Call Button Logic
        Button btnCall = findViewById(R.id.btnCallCustomer);
        btnCall.setOnClickListener(v -> {
            if (phone != null && !phone.isEmpty() && !phone.equals("N/A")) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(android.net.Uri.parse("tel:" + phone));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            }
        });

        MaterialButton btnWhatsapp = findViewById(R.id.btnSendWhatsapp);

        btnWhatsapp.setOnClickListener(v -> {
            sendToWhatsApp(
                    phone,
                    itemNo,
                    name,
                    pickupDate,
                    returnDate,
                    totalRent,
                    rentPaid,
                    deposit,
                    balance
            );
        });


        if (status.equalsIgnoreCase("Booked")) {

            setButtonState(btnPickedUp, true);
            setButtonState(btnCancel, true);
            setButtonState(BtnMarkReceived, false);

        }
        else if (status.equalsIgnoreCase("PickedUp")) {

            setButtonState(btnPickedUp, false);
            setButtonState(btnCancel, false);
            setButtonState(BtnMarkReceived, true);

        }
        else {

            setButtonState(btnPickedUp, false);
            setButtonState(btnCancel, false);
            setButtonState(BtnMarkReceived, false);
        }
    }

    private ArrayList<String> getPickupAllowedItems(){

        ArrayList<String> allowed = new ArrayList<>();

        for(RentalBooking.ItemStatus item : itemsList){

            if(item.getStatus().equals("Booked")){
                allowed.add(item.getItemNo());
            }

        }

        return allowed;
    }

    private ArrayList<String> getCancelableItems(){

        ArrayList<String> allowed = new ArrayList<>();

        for(RentalBooking.ItemStatus item : itemsList){

            if(item.getStatus().equals("Booked")){
                allowed.add(item.getItemNo());
            }

        }

        return allowed;
    }

    private ArrayList<String> extractItemCodes(ArrayList<RentalBooking.ItemStatus> items){

        ArrayList<String> codes = new ArrayList<>();

        for(RentalBooking.ItemStatus item : items){
            codes.add(item.getItemNo());
        }

        return codes;
    }

    private void openItemSelectionDialog(
            String title,
            ArrayList<RentalBooking.ItemStatus> items){

        String[] itemNames = new String[items.size()];
        boolean[] checked = new boolean[items.size()];

        for(int i=0;i<items.size();i++){

            RentalBooking.ItemStatus item = items.get(i);

            itemNames[i] =
                    item.getItemNo() + "  (" + item.getStatus() + ")";

            checked[i] = false;
        }

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setTitle(title);

        builder.setMultiChoiceItems(
                itemNames,
                checked,
                (dialog,which,isChecked)->{
                    checked[which] = isChecked;
                });

        builder.setPositiveButton("Confirm",(dialog,which)->{

            ArrayList<String> selectedItems = new ArrayList<>();

            for(int i=0;i<items.size();i++){

                if(checked[i]){

                    String status = items.get(i).getStatus();

                    if(status.equals("Returned")){

                        Toast.makeText(this,
                                items.get(i).getItemNo()+" already returned",
                                Toast.LENGTH_SHORT).show();

                        continue;
                    }

                    selectedItems.add(items.get(i).getItemNo());
                }
            }

            if(selectedItems.isEmpty()){
                Toast.makeText(this,
                        "Select at least one item",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            markItemAsReturned(orderId, selectedItems);

        });

        builder.setNegativeButton("Cancel",null);

        builder.show();
    }

    private void markItemAsReturned(String orderId,List<String> items){

        JSONObject params = new JSONObject();

        try{

            params.put("action","markReceived");
            params.put("orderId",orderId);

            JSONArray arr = new JSONArray();

            for(String item:items){
                arr.put(item);
            }

            params.put("items",arr);

        }catch(Exception e){}

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                webAppUrl,
                params,
                response->{

                    Toast.makeText(this,"Items Returned",Toast.LENGTH_SHORT).show();
                    finish();

                },
                error->Log.e("API_ERROR",error.toString())
        );

        Volley.newRequestQueue(this).add(request);
    }


    private void setButtonState(MaterialButton button, boolean enabled) {
        button.setEnabled(enabled);

        if (enabled) {
            button.setAlpha(1f);
        } else {
            button.setAlpha(0.4f);   // dark faded look
        }
    }


    private void markAsPickedUp(String orderId,
                                ArrayList<String> items,
                                double paidNow){

        JSONObject params = new JSONObject();

        try {

            params.put("action","markPickedUp");
            params.put("orderId",orderId);
            params.put("items",new JSONArray(items));
            params.put("paidNow",paidNow);

        } catch (Exception ignored){}

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                webAppUrl,
                params,
                response -> {

                    Toast.makeText(this,
                            "Items Picked Up",
                            Toast.LENGTH_SHORT).show();

                    finish();

                },
                error -> Log.e("API_ERROR",error.toString())
        );

        Volley.newRequestQueue(this).add(request);
    }


    private void sendToWhatsApp(String phone,
                                String item,
                                String name,
                                Date pickup,
                                Date returnDate,
                                double totalRent,
                                double rentPaid,
                                double deposit,
                                double balance) {

        if (phone == null || phone.trim().isEmpty()) {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Remove spaces if any
        phone = phone.replace(" ", "");

        String message =
                "✨ *SVADHA COLLECTION* ✨\n" +
                        "------------------------------\n\n" +

                        "👗 *Item:* " + item + "\n" +
                        "👤 *Customer:* " + name + "\n\n" +

                        "📅 *Pickup:* " + pickup + "\n" +
                        "📅 *Return:* " + returnDate + "\n\n" +

                        "💰 *Total Rent:* ₹" + totalRent + "\n" +
                        "💳 *Rent Paid:* ₹" + rentPaid + "\n" +
                        "🔐 *Deposit:* ₹" + deposit + "\n" +
                        "📊 *Settlement:* ₹" + balance + "\n\n" +

                        "Thank you for choosing Swadha Collection ❤️";

        try {
            String url = "https://wa.me/91" + phone + "?text=" + Uri.encode(message);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }


    private void cancelBookingWithRefund(String orderId,
                                         ArrayList<String> items,
                                         double refundAmount){

        JSONObject params = new JSONObject();

        try {

            params.put("action","cancelBooking");
            params.put("orderId",orderId);
            params.put("items",new JSONArray(items));
            params.put("refundAmount",refundAmount);

        } catch (Exception ignored){}

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                webAppUrl,
                params,
                response -> {

                    Toast.makeText(this,
                            "Items Cancelled",
                            Toast.LENGTH_SHORT).show();

                    finish();

                },
                error -> Log.e("API_ERROR",error.toString())
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void showCancelDialog(String orderId,
                                  ArrayList<String> itemsList,
                                  double deposit,
                                  double rentPaid){

        View view = getLayoutInflater()
                .inflate(R.layout.dialog_cancel_items,null);

        ListView listView = view.findViewById(R.id.listCancelItems);
        EditText refundInput = view.findViewById(R.id.etCancelRefund);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_multiple_choice,
                        itemsList
                );

        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        double suggestedRefund = deposit + rentPaid;

        refundInput.setText(String.valueOf(suggestedRefund));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Cancel Items")
                .setView(view)
                .setPositiveButton("Confirm",null)
                .setNegativeButton("Close",null)
                .create();

        dialog.setOnShowListener(d -> {

            Button confirm = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            confirm.setOnClickListener(v -> {

                ArrayList<String> selectedItems = new ArrayList<>();

                for(int i=0;i<itemsList.size();i++){

                    if(listView.isItemChecked(i)){
                        selectedItems.add(itemsList.get(i));
                    }

                }

                if(selectedItems.isEmpty()){

                    Toast.makeText(this,
                            "Select at least one item",
                            Toast.LENGTH_SHORT).show();
                    return;

                }

                double refund =
                        refundInput.getText().toString().isEmpty()
                                ? 0
                                : Double.parseDouble(refundInput.getText().toString());

                cancelBookingWithRefund(orderId, selectedItems, refund);

                dialog.dismiss();

            });

        });

        dialog.show();
    }

    private void renderItemTimeline(ArrayList<String> items,
                                    long pickupMs,
                                    long returnMs){

        SimpleDateFormat format =
                new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault());

        if(layoutItemTimeline == null){
            Log.e("UI_ERROR","layoutItemTimeline not found");
            return;
        }

        layoutItemTimeline.removeAllViews();

        for(String item : items){

            View row = getLayoutInflater()
                    .inflate(R.layout.item_timeline_row,null);

            TextView tvItem = row.findViewById(R.id.tvTimelineItem);
            TextView tvTime = row.findViewById(R.id.tvTimelineTime);

            tvItem.setText(item);

            String timeline =
                    format.format(new Date(pickupMs))
                            + " → "
                            + format.format(new Date(returnMs));

            tvTime.setText(timeline);

            layoutItemTimeline.addView(row);
        }
    }
    private void showPickupDialog(String orderId,
                                  ArrayList<String> itemsList,
                                  double totalRent,
                                  double rentPaid){

        View view = getLayoutInflater()
                .inflate(R.layout.dialogue_return_item, null);

        ListView listView = view.findViewById(R.id.listItems);
        EditText paidInput = view.findViewById(R.id.etRefund);
        double remainingRent = totalRent - rentPaid;

        if(remainingRent > 0){
            paidInput.setText(String.valueOf(remainingRent));
        }else{
            paidInput.setText("0");
        }

        paidInput.setHint("Rent to collect");

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_multiple_choice,
                        itemsList
                );

        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Pick Up Items")
                .setView(view)
                .setPositiveButton("Confirm", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {

            Button confirmBtn =
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            confirmBtn.setOnClickListener(v -> {

                ArrayList<String> selectedItems = new ArrayList<>();

                for(int i=0;i<itemsList.size();i++){

                    if(listView.isItemChecked(i)){
                        selectedItems.add(itemsList.get(i));
                    }

                }

                if(selectedItems.isEmpty()){
                    Toast.makeText(this,
                            "Select at least one item",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                double paidNow =
                        paidInput.getText().toString().isEmpty()
                                ? 0
                                : Double.parseDouble(
                                paidInput.getText().toString()
                        );

                markAsPickedUp(orderId, selectedItems, paidNow);

                dialog.dismiss();
            });

        });

        dialog.show();
    }

}
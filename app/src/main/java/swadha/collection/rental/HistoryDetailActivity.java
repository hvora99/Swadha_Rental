package swadha.collection.rental;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HistoryDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        // Receive Intent
        String itemNo = getIntent().getStringExtra("itemNo");
        String name = getIntent().getStringExtra("name");
        String phone = getIntent().getStringExtra("phone");
        String pickupDateTime = getIntent().getStringExtra("pickupDateTime");
        String returnDateTime = getIntent().getStringExtra("returnDateTime");

        double totalRent = getIntent().getDoubleExtra("totalRent", 0.0);
        double deposit = getIntent().getDoubleExtra("deposit", 0.0);
        double rentPaid = getIntent().getDoubleExtra("rentPaid", 0.0);
        double balance = getIntent().getDoubleExtra("balance", 0.0);

        String status = getIntent().getStringExtra("status");
        String actualPickup = getIntent().getStringExtra("actualPickup");
        String actualReceive = getIntent().getStringExtra("actualReceive");

        // Bind Views
        TextView tvItemNo = findViewById(R.id.tvItemNo);
        TextView tvStatus = findViewById(R.id.tvStatus);
        TextView tvCustomer = findViewById(R.id.tvCustomer);
        TextView tvPhone = findViewById(R.id.tvPhone);

        TextView tvPickupScheduled = findViewById(R.id.tvPickupScheduled);
        TextView tvReturnScheduled = findViewById(R.id.tvReturnScheduled);
        TextView tvActualPickup = findViewById(R.id.tvActualPickup);
        TextView tvActualReturn = findViewById(R.id.tvActualReturn);

        TextView tvTotalRent = findViewById(R.id.tvTotalRent);
        TextView tvRentPaid = findViewById(R.id.tvRentPaid);
        TextView tvDeposit = findViewById(R.id.tvDeposit);
        TextView tvFinalSettlement = findViewById(R.id.tvFinalSettlement);

        // Set Data
        tvItemNo.setText(itemNo);
        tvStatus.setText(status);

        tvCustomer.setText(name);
        tvPhone.setText(phone);

        tvPickupScheduled.setText(pickupDateTime);
        tvReturnScheduled.setText(returnDateTime);
        tvActualPickup.setText(actualPickup != null ? actualPickup : "-");
        tvActualReturn.setText(actualReceive != null ? actualReceive : "-");

        tvTotalRent.setText("₹ " + String.format("%.2f", totalRent));
        tvRentPaid.setText("₹ " + String.format("%.2f", rentPaid));
        tvDeposit.setText("₹ " + String.format("%.2f", deposit));

        if (balance < 0) {
            tvFinalSettlement.setText("Refund ₹ " + String.format("%.2f", Math.abs(balance)));
            tvFinalSettlement.setTextColor(Color.parseColor("#2E7D32"));
        }
        else if (balance > 0) {
            tvFinalSettlement.setText("Customer Paid Extra ₹ " + String.format("%.2f", balance));
            tvFinalSettlement.setTextColor(Color.parseColor("#D32F2F"));
        }
        else {
            tvFinalSettlement.setText("Settled");
            tvFinalSettlement.setTextColor(Color.GRAY);
        }
    }
}
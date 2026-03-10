package swadha.collection.rental;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        // Hide the Action Bar for a full-screen look
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Logic to wait for 2.5 seconds (2500 milliseconds)
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish(); // Close SplashActivity so user can't go back to it
        }, 2500);
    }

}

// This is version 1.1
package com.example.elecbill;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerMonth;
    private EditText etUnits;
    private SeekBar seekBarRebate;
    private TextView tvRebateValue, tvTotalCharges, tvFinalCost;
    private Button btnCalculate, btnHowToUse;
    private LinearLayout instructionsBox, navSave, navHistory, navAbout;

    private DatabaseReference databaseBills;
    private DecimalFormat df = new DecimalFormat("0.00");
    private double currentTotalCharges = 0;
    private double currentFinalCost = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseBills = FirebaseDatabase.getInstance().getReference("bills");

        initViews();
        setupSpinner();
        setupSeekBar();
        setupButtons();
    }

    private void initViews() {
        spinnerMonth = findViewById(R.id.spinnerMonth);
        etUnits = findViewById(R.id.etUnits);
        seekBarRebate = findViewById(R.id.seekBarRebate);
        tvRebateValue = findViewById(R.id.tvRebateValue);
        tvTotalCharges = findViewById(R.id.tvTotalCharges);
        tvFinalCost = findViewById(R.id.tvFinalCost);
        btnCalculate = findViewById(R.id.btnCalculate);
        btnHowToUse = findViewById(R.id.btnHowToUse);
        instructionsBox = findViewById(R.id.instructionsBox);

        // Navigation bar items
        navSave = findViewById(R.id.navSave);
        navHistory = findViewById(R.id.navHistory);
        navAbout = findViewById(R.id.navAbout);
    }

    private void setupSpinner() {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, months) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(Color.BLACK);
                text.setTextSize(16);
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);
    }

    private void setupSeekBar() {
        seekBarRebate.setMax(5);
        seekBarRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvRebateValue.setText(progress + "%");
                if (currentTotalCharges > 0) {
                    calculateFinalCost(currentTotalCharges, progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupButtons() {
        btnCalculate.setOnClickListener(v -> calculateBill());

        btnHowToUse.setOnClickListener(v -> {
            if (instructionsBox.getVisibility() == View.GONE) {
                instructionsBox.setVisibility(View.VISIBLE);
                btnHowToUse.setText("✖");
            } else {
                instructionsBox.setVisibility(View.GONE);
                btnHowToUse.setText("?");
            }
        });

        // Navigation bar click listeners
        navSave.setOnClickListener(v -> saveToFirebase());
        navHistory.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HistoryActivity.class)));
        navAbout.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AboutActivity.class)));
    }

    private void calculateBill() {
        String unitsStr = etUnits.getText().toString().trim();

        if (TextUtils.isEmpty(unitsStr)) {
            etUnits.setError("Please enter electricity units (1-1000 kWh)");
            return;
        }

        int units = Integer.parseInt(unitsStr);

        if (units < 1 || units > 1000) {
            etUnits.setError("Units must be between 1 and 1000 kWh");
            return;
        }

        currentTotalCharges = calculateTotalCharges(units);
        int rebatePercent = seekBarRebate.getProgress();
        calculateFinalCost(currentTotalCharges, rebatePercent);

        tvTotalCharges.setText("RM " + df.format(currentTotalCharges));
    }

    private double calculateTotalCharges(int units) {
        double total = 0;
        int remaining = units;

        if (remaining > 0) {
            int firstBlock = Math.min(remaining, 200);
            total += firstBlock * 0.218;
            remaining -= firstBlock;
        }
        if (remaining > 0) {
            int secondBlock = Math.min(remaining, 100);
            total += secondBlock * 0.334;
            remaining -= secondBlock;
        }
        if (remaining > 0) {
            int thirdBlock = Math.min(remaining, 300);
            total += thirdBlock * 0.516;
            remaining -= thirdBlock;
        }
        if (remaining > 0) {
            total += remaining * 0.546;
        }
        return total;
    }

    private void calculateFinalCost(double totalCharges, int rebatePercent) {
        double rebateAmount = totalCharges * rebatePercent / 100;
        currentFinalCost = totalCharges - rebateAmount;
        tvFinalCost.setText("RM " + df.format(currentFinalCost));
    }

    private void saveToFirebase() {
        String month = spinnerMonth.getSelectedItem().toString();
        String unitsStr = etUnits.getText().toString().trim();

        if (TextUtils.isEmpty(unitsStr) || currentTotalCharges == 0) {
            Toast.makeText(this, "Please calculate the bill first!", Toast.LENGTH_SHORT).show();
            return;
        }

        int units = Integer.parseInt(unitsStr);
        int rebatePercent = seekBarRebate.getProgress();
        double rebateAmount = currentTotalCharges * rebatePercent / 100;

        String id = databaseBills.push().getKey();

        BillEntry bill = new BillEntry(id, month, units, currentTotalCharges,
                rebatePercent, rebateAmount, currentFinalCost);

        databaseBills.child(id).setValue(bill)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Bill saved to Firebase!", Toast.LENGTH_LONG).show();
                    clearForm();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Failed to save", Toast.LENGTH_SHORT).show();
                });
    }

    private void clearForm() {
        etUnits.setText("");
        seekBarRebate.setProgress(0);
        tvTotalCharges.setText("RM 0.00");
        tvFinalCost.setText("RM 0.00");
        currentTotalCharges = 0;
        currentFinalCost = 0;
    }
}
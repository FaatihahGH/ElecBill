package com.example.elecbill;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;

public class DetailActivity extends AppCompatActivity {

    private TextView tvMonth, tvUnits, tvTotalCharges, tvRebatePercent, tvRebateAmount, tvFinalCost;
    private Button btnEdit, btnDelete;
    private DatabaseReference databaseBills;
    private String billId;
    private BillEntry currentBill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Bill Details");
        }

        billId = getIntent().getStringExtra("bill_id");
        databaseBills = FirebaseDatabase.getInstance().getReference("bills").child(billId);

        initViews();
        loadData();
    }

    private void initViews() {
        tvMonth = findViewById(R.id.tvDetailMonth);
        tvUnits = findViewById(R.id.tvDetailUnits);
        tvTotalCharges = findViewById(R.id.tvDetailTotalCharges);
        tvRebatePercent = findViewById(R.id.tvDetailRebatePercent);
        tvRebateAmount = findViewById(R.id.tvDetailRebateAmount);
        tvFinalCost = findViewById(R.id.tvDetailFinalCost);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        btnEdit.setOnClickListener(v -> showEditDialog());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void loadData() {
        databaseBills.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentBill = dataSnapshot.getValue(BillEntry.class);
                if (currentBill != null) {
                    tvMonth.setText(currentBill.getMonth());
                    tvUnits.setText(currentBill.getUnits() + " kWh");
                    tvTotalCharges.setText(String.format("RM %.2f", currentBill.getTotalCharges()));
                    tvRebatePercent.setText(String.format("%.0f%%", currentBill.getRebatePercent()));
                    tvRebateAmount.setText(String.format("RM %.2f", currentBill.getRebateAmount()));
                    tvFinalCost.setText(String.format("RM %.2f", currentBill.getFinalCost()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DetailActivity.this, "Failed to load", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Bill");

        final View view = getLayoutInflater().inflate(R.layout.dialog_edit, null);

        // Get all views from dialog
        final Spinner spinnerMonth = view.findViewById(R.id.spinnerEditMonth);
        final EditText etUnits = view.findViewById(R.id.etEditUnits);
        final SeekBar seekBar = view.findViewById(R.id.seekBarEditRebate);
        final TextView tvRebateValue = view.findViewById(R.id.tvEditRebateValue);

        // Setup month spinner with all months
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

// Create custom adapter with BLACK text color
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, months) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(Color.BLACK);  // Change text color to BLACK
                text.setTextSize(16);
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        // Load current values into the dialog
        if (currentBill != null) {
            // Set current month in spinner
            int monthPosition = getMonthPosition(currentBill.getMonth());
            spinnerMonth.setSelection(monthPosition);

            // Set units
            etUnits.setText(String.valueOf(currentBill.getUnits()));

            // Set rebate
            seekBar.setProgress((int) currentBill.getRebatePercent());
            tvRebateValue.setText((int) currentBill.getRebatePercent() + "%");
        }

        // Update rebate percentage display when slider moves
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvRebateValue.setText(progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        builder.setView(view);
        builder.setPositiveButton("Update", (dialog, which) -> {
            // Get values from dialog
            String newMonth = spinnerMonth.getSelectedItem().toString();
            int units = Integer.parseInt(etUnits.getText().toString());
            int rebatePercent = seekBar.getProgress();

            // Calculate charges
            double totalCharges = calculateTotalCharges(units);
            double rebateAmount = totalCharges * rebatePercent / 100;
            double finalCost = totalCharges - rebateAmount;

            // Create updated bill with NEW month
            BillEntry updatedBill = new BillEntry(billId, newMonth, units,
                    totalCharges, rebatePercent, rebateAmount, finalCost);

            // Save to Firebase
            databaseBills.setValue(updatedBill)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(DetailActivity.this, "Bill updated", Toast.LENGTH_SHORT).show();
                        loadData(); // Refresh the display
                    })
                    .addOnFailureListener(e -> Toast.makeText(DetailActivity.this, "Update failed", Toast.LENGTH_SHORT).show());
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // Helper method to find month position in array
    private int getMonthPosition(String month) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(month)) {
                return i;
            }
        }
        return 0; // Default to January if not found
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

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Bill")
                .setMessage("Are you sure you want to delete this record?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    databaseBills.removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(DetailActivity.this, "Bill deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(DetailActivity.this, "Delete failed", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
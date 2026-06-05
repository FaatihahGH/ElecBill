package com.example.elecbill;

import android.app.AlertDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.text.DecimalFormat;

public class DetailActivity extends AppCompatActivity {

    private TextView tvMonth, tvUnits, tvTotalCharges, tvRebatePercent, tvRebateAmount, tvFinalCost;
    private Button btnEdit, btnDelete;
    private DatabaseHelper dbHelper;
    private int billId;
    private DecimalFormat df = new DecimalFormat("0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Bill Details");
        }

        billId = getIntent().getIntExtra("bill_id", -1);
        dbHelper = new DatabaseHelper(this);

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
        Cursor cursor = dbHelper.getBillById(billId);
        if (cursor.moveToFirst()) {
            String month = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MONTH));
            int units = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UNITS));
            double totalCharges = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TOTAL_CHARGES));
            double rebatePercent = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REBATE_PERCENT));
            double rebateAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REBATE_AMOUNT));
            double finalCost = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FINAL_COST));

            tvMonth.setText(month);
            tvUnits.setText(units + " kWh");
            tvTotalCharges.setText("RM " + df.format(totalCharges));
            tvRebatePercent.setText(String.format("%.0f%%", rebatePercent));
            tvRebateAmount.setText("RM " + df.format(rebateAmount));
            tvFinalCost.setText("RM " + df.format(finalCost));
        }
        cursor.close();
    }

    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Bill");

        final View view = getLayoutInflater().inflate(R.layout.dialog_edit, null);


        final Spinner spinnerEditMonth = view.findViewById(R.id.spinnerEditMonth);
        final EditText etUnits = view.findViewById(R.id.etEditUnits);
        final SeekBar seekBar = view.findViewById(R.id.seekBarEditRebate);
        final TextView tvRebateValue = view.findViewById(R.id.tvEditRebateValue);

        // Setup month spinner with months array
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, months);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditMonth.setAdapter(adapter);

        // Load current bill data into dialog
        Cursor cursor = dbHelper.getBillById(billId);
        String currentMonth = "";
        if (cursor.moveToFirst()) {
            currentMonth = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MONTH));
            etUnits.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UNITS))));
            int rebate = (int) cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REBATE_PERCENT));
            seekBar.setProgress(rebate);
            tvRebateValue.setText(rebate + "%");

            // Set spinner to current month
            for (int i = 0; i < months.length; i++) {
                if (months[i].equals(currentMonth)) {
                    spinnerEditMonth.setSelection(i);
                    break;
                }
            }
        }
        cursor.close();


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvRebateValue.setText(progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        builder.setView(view);


        SpannableString updateText = new SpannableString("Update");
        updateText.setSpan(new ForegroundColorSpan(Color.parseColor("#4CAF50")),
                0, updateText.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.setPositiveButton(updateText, (dialog, which) -> {
            String newMonth = spinnerEditMonth.getSelectedItem().toString();
            int units = Integer.parseInt(etUnits.getText().toString());
            int rebatePercent = seekBar.getProgress();

            double totalCharges = calculateTotalCharges(units);
            double rebateAmount = totalCharges * rebatePercent / 100;
            double finalCost = totalCharges - rebateAmount;

            boolean updated = dbHelper.updateBill(billId, newMonth, units, totalCharges,
                    rebatePercent, rebateAmount, finalCost);
            if (updated) {
                Toast.makeText(DetailActivity.this, "Bill updated", Toast.LENGTH_SHORT).show();
                loadData();
            } else {
                Toast.makeText(DetailActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
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
                    dbHelper.deleteBill(billId);
                    Toast.makeText(DetailActivity.this, "Bill deleted", Toast.LENGTH_SHORT).show();
                    finish();
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
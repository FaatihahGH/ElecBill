package com.example.elecbill;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private ListView listViewHistory;
    private DatabaseHelper dbHelper;
    private ArrayList<BillEntry> billList;
    private BillListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Bill History");
        }

        listViewHistory = findViewById(R.id.listViewHistory);
        dbHelper = new DatabaseHelper(this);
        billList = new ArrayList<>();

        listViewHistory.setOnItemClickListener((parent, view, position, id) -> {
            BillEntry bill = billList.get(position);
            Intent intent = new Intent(HistoryActivity.this, DetailActivity.class);
            intent.putExtra("bill_id", bill.getId());
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        billList.clear();
        Cursor cursor = dbHelper.getAllBills();

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String month = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MONTH));
                int units = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UNITS));
                double totalCharges = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TOTAL_CHARGES));
                double rebatePercent = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REBATE_PERCENT));
                double rebateAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REBATE_AMOUNT));
                double finalCost = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FINAL_COST));

                billList.add(new BillEntry(id, month, units, totalCharges, rebatePercent, rebateAmount, finalCost));
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter = new BillListAdapter(this, billList);
        listViewHistory.setAdapter(adapter);

        if (billList.isEmpty()) {
            Toast.makeText(this, "No records found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
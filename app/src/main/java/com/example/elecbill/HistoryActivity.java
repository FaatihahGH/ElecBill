package com.example.elecbill;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ListView listViewHistory;
    private DatabaseReference databaseBills;
    private List<BillEntry> billList;
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
        databaseBills = FirebaseDatabase.getInstance().getReference("bills");
        billList = new ArrayList<>();

        listViewHistory.setOnItemClickListener((parent, view, position, id) -> {
            BillEntry bill = billList.get(position);
            Intent intent = new Intent(HistoryActivity.this, DetailActivity.class);
            intent.putExtra("bill_id", bill.getId());
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        databaseBills.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                billList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    BillEntry bill = postSnapshot.getValue(BillEntry.class);
                    if (bill != null) {
                        billList.add(bill);
                    }
                }
                adapter = new BillListAdapter(HistoryActivity.this, billList);
                listViewHistory.setAdapter(adapter);

                if (billList.isEmpty()) {
                    Toast.makeText(HistoryActivity.this, "No records found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HistoryActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
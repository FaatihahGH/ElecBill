package com.example.elecbill;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.util.List;

public class BillListAdapter extends ArrayAdapter<BillEntry> {
    private Activity context;
    private List<BillEntry> billList;
    private DecimalFormat df = new DecimalFormat("0.00");

    public BillListAdapter(Activity context, List<BillEntry> billList) {
        super(context, R.layout.list_bill_item, billList);
        this.context = context;
        this.billList = billList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.list_bill_item, null, true);

        TextView tvMonth = listViewItem.findViewById(R.id.tvMonth);
        TextView tvFinalCost = listViewItem.findViewById(R.id.tvFinalCost);

        BillEntry bill = billList.get(position);
        tvMonth.setText(bill.getMonth());
        tvFinalCost.setText("RM " + df.format(bill.getFinalCost()));

        return listViewItem;
    }
}
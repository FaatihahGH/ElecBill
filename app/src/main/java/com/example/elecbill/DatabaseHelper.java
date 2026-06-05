package com.example.elecbill;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "electricity_bill.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_BILLS = "bills";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_MONTH = "month";
    public static final String COLUMN_UNITS = "units";
    public static final String COLUMN_TOTAL_CHARGES = "total_charges";
    public static final String COLUMN_REBATE_PERCENT = "rebate_percent";
    public static final String COLUMN_REBATE_AMOUNT = "rebate_amount";
    public static final String COLUMN_FINAL_COST = "final_cost";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_BILLS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MONTH + " TEXT, " +
                    COLUMN_UNITS + " INTEGER, " +
                    COLUMN_TOTAL_CHARGES + " REAL, " +
                    COLUMN_REBATE_PERCENT + " REAL, " +
                    COLUMN_REBATE_AMOUNT + " REAL, " +
                    COLUMN_FINAL_COST + " REAL)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILLS);
        onCreate(db);
    }

    public boolean insertBill(String month, int units, double totalCharges,
                              double rebatePercent, double rebateAmount, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MONTH, month);
        values.put(COLUMN_UNITS, units);
        values.put(COLUMN_TOTAL_CHARGES, totalCharges);
        values.put(COLUMN_REBATE_PERCENT, rebatePercent);
        values.put(COLUMN_REBATE_AMOUNT, rebateAmount);
        values.put(COLUMN_FINAL_COST, finalCost);

        long result = db.insert(TABLE_BILLS, null, values);
        db.close();
        return result != -1;
    }

    public Cursor getAllBills() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_BILLS, null, null, null, null, null, COLUMN_ID + " DESC");
    }

    public Cursor getBillById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_BILLS, null, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
    }

    public boolean updateBill(int id, String month, int units, double totalCharges,
                              double rebatePercent, double rebateAmount, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MONTH, month);
        values.put(COLUMN_UNITS, units);
        values.put(COLUMN_TOTAL_CHARGES, totalCharges);
        values.put(COLUMN_REBATE_PERCENT, rebatePercent);
        values.put(COLUMN_REBATE_AMOUNT, rebateAmount);
        values.put(COLUMN_FINAL_COST, finalCost);

        int result = db.update(TABLE_BILLS, values, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }

    public boolean deleteBill(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_BILLS, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }
}
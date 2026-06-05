package com.example.elecbill;

public class BillEntry {
    private int id;
    private String month;
    private int units;
    private double totalCharges;
    private double rebatePercent;
    private double rebateAmount;
    private double finalCost;

    public BillEntry(int id, String month, int units, double totalCharges,
                     double rebatePercent, double rebateAmount, double finalCost) {
        this.id = id;
        this.month = month;
        this.units = units;
        this.totalCharges = totalCharges;
        this.rebatePercent = rebatePercent;
        this.rebateAmount = rebateAmount;
        this.finalCost = finalCost;
    }

    public int getId() { return id; }
    public String getMonth() { return month; }
    public int getUnits() { return units; }
    public double getTotalCharges() { return totalCharges; }
    public double getRebatePercent() { return rebatePercent; }
    public double getRebateAmount() { return rebateAmount; }
    public double getFinalCost() { return finalCost; }
}
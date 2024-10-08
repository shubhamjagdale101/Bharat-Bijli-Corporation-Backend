package com.shubham.lightbill.lightbill_backend.model;

import lombok.Getter;

@Getter
public class Consumption {
    private String month;
    private Boolean paid;
    private double units;
    private double amount;

    public Consumption(String month, double units, double amount, Boolean paid) {
        this.month = month;
        this.units = units;
        this.amount = amount;
        this.paid = paid;
    }
}

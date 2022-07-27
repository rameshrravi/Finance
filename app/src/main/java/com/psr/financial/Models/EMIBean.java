package com.psr.financial.Models;

//import android.support.annotation.NonNull;

import androidx.annotation.NonNull;

import com.psr.financial.Utilities;

import java.io.Serializable;
import java.util.Date;

public class EMIBean implements Serializable, Comparable<EMIBean> { //, Comparator<EMIBean> {

    public String titleId;
    public String customerId;
    public String emiId;
    public String date;
    public double amount;
    public String collectBy;

    public EMIBean() {

    }
    public EMIBean(String emiId, String date, double amount, String collectBy) {
        this.emiId = emiId;
        this.date = date;
        this.amount = amount;
        this.collectBy = collectBy;
    }

    @Override
    public int compareTo(@NonNull EMIBean emi) {
        /* For Ascending order*/
        if (getDate() == null || emi.getDate() == null)
            return 0;
        try {
            //DateFormat format = new SimpleDateFormat("MM-dd-yyyy hh:mm a");
            Date o1 = Utilities.getDateFromTimeStamp(getDate(), Utilities.DD_MM_YYYY_TIME); //
            Date o2 = Utilities.getDateFromTimeStamp(emi.getDate(), Utilities.DD_MM_YYYY_TIME);

            return o1.compareTo(o2); //Long.valueOf(format.parse(o1.getDate()).getTime()).compareTo(format.parse(o2.getDate()).getTime());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

        /* For Descending order do like this */
        //return compareage-this.studentage;
    }

    /*@Override
    public int compare(EMIBean emi1, EMIBean emi2) {
        // For Ascending order
        if (emi1.getEmiId() == null || emi2.getEmiId() == null)
            return 0;
        try {
            //DateFormat format = new SimpleDateFormat("MM-dd-yyyy hh:mm a");
            Date o1 = Utilities.getDateFromTimeStamp(emi1.getEmiId(), "MM-dd-yyyy hh:mm a");
            Date o2 = Utilities.getDateFromTimeStamp(emi2.getEmiId(), "MM-dd-yyyy hh:mm a");

            return o1.compareTo(o2); //Long.valueOf(format.parse(o1.getDate()).getTime()).compareTo(format.parse(o2.getDate()).getTime());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }*/

    public String getTitleId() {
        return titleId;
    }

    public void setTitleId(String titleId) {
        this.titleId = titleId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getEmiId() {
        return emiId;
    }

    public void setEmiId(String emiId) {
        this.emiId = emiId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCollectBy() {
        return collectBy;
    }

    public void setCollectBy(String collectBy) {
        this.collectBy = collectBy;
    }
}

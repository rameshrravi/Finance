package com.psr.financial.Models;

//import android.support.annotation.NonNull;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

public class CustomerBean implements Serializable, Comparable {

    public String titleId;
    public String customerId;
    public String name;
    public String phone;
    public String place;
    public double amount;
    public double received;
    public double balance;
    public String createdOn;
    public String collectBy;
    public List<EMIBean> emi;
    public int index;
    public boolean isSelected;
    public EMIBean lastPayment;
    public String Phone2;
    //public List<EMIBean> todayEmiBeans;

    public String lastPaymentId;
    public double todayCollection;
    public boolean isDataLoaded;

    @Override
    public int compareTo(@NonNull Object o) {
        int compareage=((CustomerBean)o).getIndex();
        /* For Ascending order*/
        return this.index - compareage;

        /* For Descending order do like this */
        //return compareage-this.studentage;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getReceived() {
        return received;
    }

    public void setReceived(double received) {
        this.received = received;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getCollectBy() {
        return collectBy;
    }

    public void setCollectBy(String collectBy) {
        this.collectBy = collectBy;
    }

    public List<EMIBean> getEmi() {
        return emi;
    }

    public void setEmi(List<EMIBean> emi) {
        this.emi = emi;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public EMIBean getLastPayment() {
        return lastPayment;
    }

    public void setLastPayment(EMIBean lastPayment) {
        this.lastPayment = lastPayment;
    }

    public String getPhone2() {
        return Phone2;
    }

    public void setPhone2(String phone2) {
        Phone2 = phone2;
    }

//    public List<EMIBean> getTodayEmiBeans() {
//        return todayEmiBeans;
//    }
//
//    public void setTodayEmiBeans(List<EMIBean> todayEmiBeans) {
//        this.todayEmiBeans = todayEmiBeans;
//    }


    public String getLastPaymentId() {
        return lastPaymentId;
    }

    public void setLastPaymentId(String lastPaymentId) {
        this.lastPaymentId = lastPaymentId;
    }

    public double getTodayCollection() {
        return todayCollection;
    }

    public void setTodayCollection(double todayCollection) {
        this.todayCollection = todayCollection;
    }

    public boolean isDataLoaded() {
        return isDataLoaded;
    }

    public void setDataLoaded(boolean dataLoaded) {
        isDataLoaded = dataLoaded;
    }
}

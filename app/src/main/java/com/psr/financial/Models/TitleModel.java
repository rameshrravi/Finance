package com.psr.financial.Models;

//import android.support.annotation.NonNull;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class TitleModel implements Serializable, Comparable {

    public String titleName;
    public String titleId;
    public String createdOn;
    public int index;

    //public List<CustomerBean> customerBeans;
    public double receivedAmount;
    public double balanceAMount;
    public int totalNoOfCustomers;

    @Override
    public int compareTo(@NonNull Object o) {
        int compareage=((TitleModel)o).getIndex();
        /* For Ascending order*/
        return this.index - compareage;

        /* For Descending order do like this */
        //return compareage-this.studentage;
    }

    public String getTitleName() {
        return titleName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    public String getTitleId() {
        return titleId;
    }

    public void setTitleId(String titleId) {
        this.titleId = titleId;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

//    public List<CustomerBean> getCustomerBeans() {
//        return customerBeans;
//    }
//
//    public void setCustomerBeans(List<CustomerBean> customerBeans) {
//        this.customerBeans = customerBeans;
//    }

    public double getReceivedAmount() {
        return receivedAmount;
    }

    public void setReceivedAmount(double receivedAmount) {
        this.receivedAmount = receivedAmount;
    }

    public double getBalanceAMount() {
        return balanceAMount;
    }

    public void setBalanceAMount(double balanceAMount) {
        this.balanceAMount = balanceAMount;
    }

    public int getTotalNoOfCustomers() {
        return totalNoOfCustomers;
    }

    public void setTotalNoOfCustomers(int totalNoOfCustomers) {
        this.totalNoOfCustomers = totalNoOfCustomers;
    }
}

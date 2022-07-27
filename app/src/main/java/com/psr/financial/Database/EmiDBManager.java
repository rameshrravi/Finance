package com.psr.financial.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.psr.financial.Models.CustomerBean;
import com.psr.financial.Models.EMIBean;
import com.psr.financial.Utilities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.psr.financial.Database.DatabaseHelper.EMI_TABLE_NAME;

public class EmiDBManager {

    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase database;

    public EmiDBManager(Context c) {
        context = c;
    }

    public EmiDBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insertEmi(CustomerBean customerBean, EMIBean emiBean) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.TITLE_ID, customerBean.getTitleId());
        contentValue.put(DatabaseHelper.CUSTOMER_ID, customerBean.getCustomerId());
        contentValue.put(DatabaseHelper.EMI_ID, emiBean.getEmiId());
        contentValue.put(DatabaseHelper.EMI_DATE, String.valueOf(emiBean.getDate()));
        contentValue.put(DatabaseHelper.EMI_AMOUNT, String.valueOf(emiBean.getAmount()));
        contentValue.put(DatabaseHelper.COLLECT_BY, emiBean.getCollectBy());

        long result = database.insert(EMI_TABLE_NAME, null, contentValue);
        if (result== -1){
            //return false;
            System.out.println("Failed to save");
        } else {
            //return true;
            System.out.println("Successfully save");
        }

        DBManager dbManager = new DBManager(context);
        dbManager.open();
        dbManager.updateCustomerEMI(customerBean.getCustomerId(), emiBean.getEmiId());
        dbManager.close();
    }

    public List<EMIBean> getCustomerEmis(String customerId) {
        List<EMIBean> emiBeans = new ArrayList<EMIBean>();

        Cursor cursor = database.rawQuery("select * from " + EMI_TABLE_NAME + " where " + DatabaseHelper.CUSTOMER_ID + " = " + customerId + " ORDER BY " + DatabaseHelper.EMI_DATE + " DESC", null); //this.fetch();
        try {
            while (cursor.moveToNext()) {
                EMIBean emiBean = new EMIBean();
                emiBean.setTitleId(cursor.getString(0));
                emiBean.setCustomerId(cursor.getString(1));
                emiBean.setEmiId(cursor.getString(2));
                emiBean.setDate(cursor.getString(3));
                emiBean.setAmount(Double.valueOf(cursor.getString(4)));
                emiBean.setCollectBy(cursor.getString(5));
                emiBeans.add(emiBean);
            }
        } finally {
            cursor.close();
        }

        //Collections.sort(emiBeans, Collections.reverseOrder());
        return emiBeans;
    }

    public List<EMIBean> getAllCustomerEmis() {
        List<EMIBean> emiBeans = new ArrayList<EMIBean>();

        Cursor cursor = database.rawQuery("select * from " + EMI_TABLE_NAME, null); //this.fetch();
        try {
            while (cursor.moveToNext()) {
                EMIBean emiBean = new EMIBean();
                emiBean.setTitleId(cursor.getString(0));
                emiBean.setCustomerId(cursor.getString(1));
                emiBean.setEmiId(cursor.getString(2));
                emiBean.setDate(cursor.getString(3));
                emiBean.setAmount(Double.valueOf(cursor.getString(4)));
                emiBean.setCollectBy(cursor.getString(5));
                emiBeans.add(emiBean);
            }
        } finally {
            cursor.close();
        }

        //Collections.sort(emiBeans, Collections.reverseOrder());
        return emiBeans;
    }

    public List<EMIBean> getEmiById(String emiId) {
        List<EMIBean> emiBeans = new ArrayList<EMIBean>();

        Cursor cursor = database.rawQuery("select * from " + EMI_TABLE_NAME + " where " + DatabaseHelper.EMI_ID + " = " + emiId + " ORDER BY " + DatabaseHelper.EMI_DATE + " DESC", null); //this.fetch();
        try {
            while (cursor.moveToNext()) {
                EMIBean emiBean = new EMIBean();
                emiBean.setTitleId(cursor.getString(0));
                emiBean.setCustomerId(cursor.getString(1));
                emiBean.setEmiId(cursor.getString(2));
                emiBean.setDate(cursor.getString(3));
                emiBean.setAmount(Double.valueOf(cursor.getString(4)));
                emiBean.setCollectBy(cursor.getString(5));
                emiBeans.add(emiBean);
            }
        } finally {
            cursor.close();
        }

        //Collections.sort(emiBeans, Collections.reverseOrder());
        return emiBeans;
    }

    public List<EMIBean> getEmisByCurrentDateAndTitle(String titleId) {
        Calendar myCalendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(Utilities.DD_MM_YYYY); //"yyyy-MM-dd HH:mm:ss"
        String formattedDate = df.format(myCalendar.getTime());
        String startDate = formattedDate + ", 00:00 AM";
        String endDate = formattedDate + ", 11:59 PM";
        long startTimeStamp = Utilities.convertDateStringToTimeStamp(startDate);
        long endTimeStamp = Utilities.convertDateStringToTimeStamp(endDate);

        List<EMIBean> emiBeans = new ArrayList<EMIBean>();

        Cursor cursor = database.rawQuery("select * from " + EMI_TABLE_NAME + " where " + DatabaseHelper.TITLE_ID + " = " + titleId + " AND " + DatabaseHelper.EMI_DATE + " >= " + startTimeStamp + " AND " + DatabaseHelper.EMI_DATE + " <= " + endTimeStamp, null); //this.fetch();
        try {
            while (cursor.moveToNext()) {
                EMIBean emiBean = new EMIBean();
                emiBean.setTitleId(cursor.getString(0));
                emiBean.setCustomerId(cursor.getString(1));
                emiBean.setEmiId(cursor.getString(2));
                emiBean.setDate(cursor.getString(3));
                emiBean.setAmount(Double.valueOf(cursor.getString(4)));
                emiBean.setCollectBy(cursor.getString(5));
                emiBeans.add(emiBean);
            }
        } finally {
            cursor.close();
        }

        //Collections.sort(emiBeans, Collections.reverseOrder());
        return emiBeans;
    }

    public double getCurrentAmountByCurrentDateAndCustomer(String customerId) {
        Calendar myCalendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(Utilities.DD_MM_YYYY); //"yyyy-MM-dd HH:mm:ss"
        String formattedDate = df.format(myCalendar.getTime());
        String startDate = formattedDate + ", 00:00 AM";
        String endDate = formattedDate + ", 11:59 PM";
        long startTimeStamp = Utilities.convertDateStringToTimeStamp(startDate);
        long endTimeStamp = Utilities.convertDateStringToTimeStamp(endDate);

        double amount = 0.0;
        Cursor cursor = database.rawQuery("select SUM(" + DatabaseHelper.EMI_AMOUNT + ") as Total from " + EMI_TABLE_NAME + " where " + DatabaseHelper.CUSTOMER_ID + " = " + customerId + " AND " + DatabaseHelper.EMI_DATE + " >= " + startTimeStamp + " AND " + DatabaseHelper.EMI_DATE + " <= " + endTimeStamp, null); //this.fetch();
        try {
            if (cursor.moveToNext()) {
                amount = cursor.getDouble(cursor.getColumnIndex("Total")); //(Double.valueOf(cursor.getString(0)));
            }
        } finally {
            cursor.close();
        }

        return amount;
    }

    public List<EMIBean> getEmisByCurrentDateAndCustomer(String customerId) {
        Calendar myCalendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(Utilities.DD_MM_YYYY); //"yyyy-MM-dd HH:mm:ss"
        String formattedDate = df.format(myCalendar.getTime());
        String startDate = formattedDate + ", 00:00 AM";
        String endDate = formattedDate + ", 11:59 PM";
        long startTimeStamp = Utilities.convertDateStringToTimeStamp(startDate);
        long endTimeStamp = Utilities.convertDateStringToTimeStamp(endDate);

        List<EMIBean> emiBeans = new ArrayList<EMIBean>();

        Cursor cursor = database.rawQuery("select * from " + EMI_TABLE_NAME + " where " + DatabaseHelper.CUSTOMER_ID + " = " + customerId + " AND " + DatabaseHelper.EMI_DATE + " >= " + startTimeStamp + " AND " + DatabaseHelper.EMI_DATE + " <= " + endTimeStamp, null); //this.fetch();
        try {
            while (cursor.moveToNext()) {
                EMIBean emiBean = new EMIBean();
                emiBean.setTitleId(cursor.getString(0));
                emiBean.setCustomerId(cursor.getString(1));
                emiBean.setEmiId(cursor.getString(2));
                emiBean.setDate(cursor.getString(3));
                emiBean.setAmount(Double.valueOf(cursor.getString(4)));
                emiBean.setCollectBy(cursor.getString(5));
                emiBeans.add(emiBean);
            }
        } finally {
            cursor.close();
        }

        return emiBeans;
    }

    public List<EMIBean> getEmisByDateAndCustomer(String customerId, long startDate, long endDate) {

        List<EMIBean> emiBeans = new ArrayList<EMIBean>();

        Cursor cursor = database.rawQuery("select * from " + EMI_TABLE_NAME + " where " + DatabaseHelper.CUSTOMER_ID + " = " + customerId + " AND " + DatabaseHelper.EMI_DATE + " >= " + startDate + " AND " + DatabaseHelper.EMI_DATE + " <= " + endDate, null); //this.fetch();
        try {
            while (cursor.moveToNext()) {
                EMIBean emiBean = new EMIBean();
                emiBean.setTitleId(cursor.getString(0));
                emiBean.setCustomerId(cursor.getString(1));
                emiBean.setEmiId(cursor.getString(2));
                emiBean.setDate(cursor.getString(3));
                emiBean.setAmount(Double.valueOf(cursor.getString(4)));
                emiBean.setCollectBy(cursor.getString(5));
                emiBeans.add(emiBean);
            }
        } finally {
            cursor.close();
        }

        //Collections.sort(emiBeans, Collections.reverseOrder());
        return emiBeans;
    }

    public List<EMIBean> getEmisByCurrentDate() {
        Calendar myCalendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(Utilities.DD_MM_YYYY); //"yyyy-MM-dd HH:mm:ss"
        String formattedDate = df.format(myCalendar.getTime());
        String startDate = formattedDate + ", 00:00 AM";
        String endDate = formattedDate + ", 11:59 PM";
        long startTimeStamp = Utilities.convertDateStringToTimeStamp(startDate);
        long endTimeStamp = Utilities.convertDateStringToTimeStamp(endDate);

        List<EMIBean> emiBeans = new ArrayList<EMIBean>();

        Cursor cursor = database.rawQuery("select * from " + EMI_TABLE_NAME + " where " + DatabaseHelper.EMI_DATE + " >= " + startTimeStamp + " AND " + DatabaseHelper.EMI_DATE + " <= " + endTimeStamp, null); //this.fetch();
        try {
            while (cursor.moveToNext()) {
                EMIBean emiBean = new EMIBean();
                emiBean.setTitleId(cursor.getString(0));
                emiBean.setCustomerId(cursor.getString(1));
                emiBean.setEmiId(cursor.getString(2));
                emiBean.setDate(cursor.getString(3));
                emiBean.setAmount(Double.valueOf(cursor.getString(4)));
                emiBean.setCollectBy(cursor.getString(5));
                emiBeans.add(emiBean);
            }
        } finally {
            cursor.close();
        }

        //Collections.sort(emiBeans, Collections.reverseOrder());
        return emiBeans;
    }

    public int updateEmi(EMIBean emiBean) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.EMI_DATE, emiBean.getDate());
        contentValues.put(DatabaseHelper.EMI_AMOUNT, String.valueOf(emiBean.getAmount()));
        contentValues.put(DatabaseHelper.COLLECT_BY, emiBean.getCollectBy());
        int i = database.update(DatabaseHelper.EMI_TABLE_NAME, contentValues, DatabaseHelper.EMI_ID + " = " + emiBean.getEmiId(), null);

        DBManager dbManager = new DBManager(context);
        dbManager.open();
        dbManager.updateCustomerEMI(emiBean.getCustomerId(), emiBean.getEmiId());
        dbManager.close();
        return i;
    }

    public void deleteEmiByCustomer(CustomerBean customerBean) {
        database.delete(EMI_TABLE_NAME, DatabaseHelper.CUSTOMER_ID + " = " + customerBean.getCustomerId(), null);
    }

    public void deleteEmi(EMIBean emiBean) {
        database.delete(EMI_TABLE_NAME, DatabaseHelper.EMI_ID + " = " + emiBean.getEmiId(), null);
    }
}
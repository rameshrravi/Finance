package com.psr.financial.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.psr.financial.Models.CustomerBean;
import com.psr.financial.Models.EMIBean;

import java.util.ArrayList;
import java.util.List;

import static com.psr.financial.Database.DatabaseHelper.CUSTOMERS_TABLE_NAME;

public class DBManager {
    private DatabaseHelper dbHelper;

    private Context context;

    private SQLiteDatabase database;

    public DBManager(Context c) {
        context = c;
    }

    public DBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(String titleId, CustomerBean customer) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.CUSTOMER_ID, customer.getCustomerId());
        contentValue.put(DatabaseHelper.TITLE_ID, titleId);
        contentValue.put(DatabaseHelper.NAME, customer.getName());
        contentValue.put(DatabaseHelper.PHONE, customer.getPhone());
        contentValue.put(DatabaseHelper.PLACE, customer.getPlace());
        contentValue.put(DatabaseHelper.AMOUNT, customer.getAmount());
        contentValue.put(DatabaseHelper.RECEIVED_AMOUNT, customer.getReceived());
        contentValue.put(DatabaseHelper.BALANCE_AMOUNT, customer.getBalance());
        contentValue.put(DatabaseHelper.CREATED_ON, customer.getCreatedOn());
        contentValue.put(DatabaseHelper.COLLECT_BY, customer.getCollectBy());
        contentValue.put(DatabaseHelper.INDEX_POS, customer.getIndex());
        contentValue.put(DatabaseHelper.PHONE2, customer.getPhone2());
        long result = database.insert(DatabaseHelper.CUSTOMERS_TABLE_NAME, null, contentValue);
        if (result== -1){
            //return false;
            System.out.println("Failed to save");
        } else {
            //return true;
            System.out.println("Successfully save");
        }

        TitleDBManager titleDBManager = new TitleDBManager(context);
        titleDBManager.open();
        titleDBManager.updateCustomersCount(titleId);
        titleDBManager.close();
    }

    public void insertAtPosition(String titleId, CustomerBean customer, int position) {
        List<CustomerBean> customerBeans = getCustomers(customer.getTitleId());
        for (int pos=position+1;pos<customerBeans.size();pos++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.INDEX_POS, pos);
            database.update(CUSTOMERS_TABLE_NAME, contentValues, DatabaseHelper.CUSTOMER_ID + " = " + customerBeans.get(pos).getCustomerId(), null);
        }

        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.CUSTOMER_ID, customer.getCustomerId());
        contentValue.put(DatabaseHelper.TITLE_ID, titleId);
        contentValue.put(DatabaseHelper.NAME, customer.getName());
        contentValue.put(DatabaseHelper.PHONE, customer.getPhone());
        contentValue.put(DatabaseHelper.PLACE, customer.getPlace());
        contentValue.put(DatabaseHelper.AMOUNT, customer.getAmount());
        contentValue.put(DatabaseHelper.RECEIVED_AMOUNT, customer.getReceived());
        contentValue.put(DatabaseHelper.BALANCE_AMOUNT, customer.getBalance());
        contentValue.put(DatabaseHelper.CREATED_ON, customer.getCreatedOn());
        contentValue.put(DatabaseHelper.COLLECT_BY, customer.getCollectBy());
        contentValue.put(DatabaseHelper.INDEX_POS, position);
        contentValue.put(DatabaseHelper.PHONE2, customer.getPhone2());
        database.insert(DatabaseHelper.CUSTOMERS_TABLE_NAME, null, contentValue);

        TitleDBManager titleDBManager = new TitleDBManager(context);
        titleDBManager.open();
        titleDBManager.updateCustomersCount(titleId);
        titleDBManager.close();

    }
    /*public Cursor fetch() {
        String[] columns = new String[] { DatabaseHelper.CUSTOMER_ID, DatabaseHelper.TITLE_ID, DatabaseHelper.NAME, DatabaseHelper.PHONE, DatabaseHelper.PLACE,
                DatabaseHelper.AMOUNT, DatabaseHelper.RECEIVED_AMOUNT, DatabaseHelper.BALANCE_AMOUNT, DatabaseHelper.CREATED_ON, DatabaseHelper.COLLECT_BY, DatabaseHelper.EMI };
        Cursor cursor = database.query(DatabaseHelper.CUSTOMERS_TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }*/

    public List<CustomerBean> getCustomers(String titleId) {
        List<CustomerBean> customerBeans = new ArrayList<>();
        //EmiDBManager emiDBManager = new EmiDBManager(context);
        //emiDBManager.open();
        Cursor cursor = database.rawQuery("select * from " + CUSTOMERS_TABLE_NAME + " where " + DatabaseHelper.TITLE_ID + " = " + titleId + " ORDER BY " + DatabaseHelper.INDEX_POS + " ASC", null);
        try {
            while (cursor.moveToNext()) {
                CustomerBean customer = new CustomerBean();
                customer.setCustomerId(cursor.getString(0));
                customer.setTitleId(cursor.getString(1));
                customer.setName(cursor.getString(2));
                customer.setPhone(cursor.getString(3));
                customer.setPlace(cursor.getString(4));
                customer.setAmount(Double.parseDouble(cursor.getString(5)));
                customer.setReceived(Double.parseDouble(cursor.getString(6)));
                customer.setBalance(Double.parseDouble(cursor.getString(7)));
                customer.setCreatedOn(cursor.getString(8));
                customer.setCollectBy(cursor.getString(9));
                customer.setIndex(cursor.getInt(10));
                customer.setPhone2(cursor.getString(11));
                customer.setLastPaymentId(cursor.getString(12));
                customer.setTodayCollection(0);
                customerBeans.add(customer);
            }
        } finally {
            //emiDBManager.close();
            cursor.close();
        }
        //Collections.sort(customerBeans);
        return customerBeans;
    }

    public int updateCustomerEMI(String customerId, String lastPaymentEmiId) {
        EmiDBManager emiDBManager = new EmiDBManager(context);
        emiDBManager.open();

        List<EMIBean> emiBeans = emiDBManager.getEmisByCurrentDateAndCustomer(customerId);
        double todayCollection = 0.0;
        for (int i=0;i<emiBeans.size();i++) {
            todayCollection += emiBeans.get(i).getAmount();
        }
        emiDBManager.close();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.LAST_PAYMENT_ID, lastPaymentEmiId);
        contentValues.put(DatabaseHelper.TODAY_COLLECTION_AMOUNT, String.valueOf(todayCollection));
        int i = database.update(DatabaseHelper.CUSTOMERS_TABLE_NAME, contentValues, DatabaseHelper.CUSTOMER_ID + " = " + customerId, null);
        return i;
    }


    public int updateCustomer(CustomerBean customer) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.NAME, customer.getName());
        contentValues.put(DatabaseHelper.PLACE, customer.getPlace());
        contentValues.put(DatabaseHelper.PHONE, customer.getPhone());
        contentValues.put(DatabaseHelper.AMOUNT, String.valueOf(customer.getAmount()));
        contentValues.put(DatabaseHelper.RECEIVED_AMOUNT, String.valueOf(customer.getReceived()));
        contentValues.put(DatabaseHelper.BALANCE_AMOUNT, String.valueOf(customer.getBalance()));
        contentValues.put(DatabaseHelper.CREATED_ON, String.valueOf(customer.getCreatedOn()));
        contentValues.put(DatabaseHelper.COLLECT_BY, String.valueOf(customer.getCollectBy()));
        contentValues.put(DatabaseHelper.PHONE2, customer.getPhone2());
        int i = database.update(DatabaseHelper.CUSTOMERS_TABLE_NAME, contentValues, DatabaseHelper.CUSTOMER_ID + " = " + customer.getCustomerId(), null);

        TitleDBManager titleDBManager = new TitleDBManager(context);
        titleDBManager.open();
        titleDBManager.updateCustomersCount(customer.getTitleId());
        titleDBManager.close();
        return i;
    }

    public int updateCustomerAmount(CustomerBean customer) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.RECEIVED_AMOUNT, String.valueOf(customer.getReceived()));
        contentValues.put(DatabaseHelper.BALANCE_AMOUNT, String.valueOf(customer.getBalance()));
        contentValues.put(DatabaseHelper.LAST_PAYMENT_ID, customer.getLastPaymentId());
        int i = database.update(DatabaseHelper.CUSTOMERS_TABLE_NAME, contentValues, DatabaseHelper.CUSTOMER_ID + " = " + customer.getCustomerId(), null);

        TitleDBManager titleDBManager = new TitleDBManager(context);
        titleDBManager.open();
        titleDBManager.updateOverallAmount(customer.getTitleId());
        titleDBManager.close();
        return i;
    }

    public int updateIndex(CustomerBean customer, int newPos) {
        List<CustomerBean> customerBeans = getCustomers(customer.getTitleId());
        int oldPos = -1;
        /*if (isAddNewCustomer) {
            oldPos = customerBeans.size()-1;
            //newPos = customerBeans.size()-1;
        } else  {
            for (int pos=0;pos<customerBeans.size();pos++) {
                if (customerBeans.get(pos).getCustomerId().equalsIgnoreCase(customer.getCustomerId())) {
                    oldPos = pos;
                    break;
                }
            }
        }*/

        for (int pos=0;pos<customerBeans.size();pos++) {
            if (customerBeans.get(pos).getCustomerId().equalsIgnoreCase(customer.getCustomerId())) {
                oldPos = customerBeans.get(pos).getIndex();
                break;
            }
        }

        if (oldPos == -1) {
            return 0;
        }
        ContentValues contentValues = new ContentValues();
        if (oldPos>newPos) { // move up
            for (int i=newPos;i<oldPos;i++) {
                //i = i + 1;
                contentValues.put(DatabaseHelper.INDEX_POS, i+1);
                database.update(CUSTOMERS_TABLE_NAME, contentValues, DatabaseHelper.CUSTOMER_ID + " = " + customerBeans.get(i).getCustomerId(), null);
            }
            //if (!isAddNewCustomer) {
                contentValues.put(DatabaseHelper.INDEX_POS, newPos);
                database.update(CUSTOMERS_TABLE_NAME, contentValues, DatabaseHelper.CUSTOMER_ID + " = " + customerBeans.get(oldPos).getCustomerId(), null);
            //}
        } else if (oldPos<newPos) { // move down
            for (int i=oldPos+1;i<=newPos;i++) {
                //i = i - 1;
                contentValues.put(DatabaseHelper.INDEX_POS, i-1);
                database.update(CUSTOMERS_TABLE_NAME, contentValues, DatabaseHelper.CUSTOMER_ID + " = " + customerBeans.get(i).getCustomerId(), null);
            }
            //if (!isAddNewCustomer) {
                contentValues.put(DatabaseHelper.INDEX_POS, newPos);
                database.update(CUSTOMERS_TABLE_NAME, contentValues, DatabaseHelper.CUSTOMER_ID + " = " + customerBeans.get(oldPos).getCustomerId(), null);
            //}
        }

        return 0;
    }

    public void updateIndexByInsertingMiddle(CustomerBean customer, int newPos) {
        List<CustomerBean> customerBeans = getCustomers(customer.getTitleId());
        ContentValues contentValues = new ContentValues();
        for (int i=newPos;i<customerBeans.size();i++) {
            contentValues.put(DatabaseHelper.INDEX_POS, i+1);
            database.update(CUSTOMERS_TABLE_NAME, contentValues, DatabaseHelper.CUSTOMER_ID + " = " + customerBeans.get(i).getCustomerId(), null);
        }
    }

    public void deleteCustomer(CustomerBean customer) {
        database.delete(DatabaseHelper.CUSTOMERS_TABLE_NAME, DatabaseHelper.CUSTOMER_ID + " = " + customer.getCustomerId(), null);
        List<CustomerBean> customerBeans = getCustomers(customer.getTitleId());
        for (int pos=0;pos<customerBeans.size();pos++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.INDEX_POS, pos);
            int i = database.update(CUSTOMERS_TABLE_NAME, contentValues, DatabaseHelper.CUSTOMER_ID + " = " + customerBeans.get(pos).getCustomerId(), null);
            if (i== -1){
                System.out.println("Failed to update");
            } else {
                System.out.println("Successfully updated");
            }
        }

        EmiDBManager emiDBManager = new EmiDBManager(context);
        emiDBManager.open();
        emiDBManager.deleteEmiByCustomer(customer);
        emiDBManager.close();

        TitleDBManager titleDBManager = new TitleDBManager(context);
        titleDBManager.open();
        titleDBManager.updateCustomersCount(customer.getTitleId());
        titleDBManager.close();
    }
}

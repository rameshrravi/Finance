package com.psr.financial.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.psr.financial.Models.CustomerBean;
import com.psr.financial.Models.EMIBean;
import com.psr.financial.Models.TitleModel;
import com.psr.financial.Session;

import java.util.ArrayList;
import java.util.List;

import static com.psr.financial.Database.DatabaseHelper.TITLE_TABLE_NAME;

public class TitleDBManager {

    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase database;

    public TitleDBManager(Context c) {
        context = c;
    }

    public TitleDBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        Session session = Session.getInstance(context);
        dbHelper.onUpgrade(database, session.getCurrentVersion(), DatabaseHelper.NO_OF_CUSTOMERS_COLUMN_ADDED_VERSION);
        session.setCurrentVersion(DatabaseHelper.NO_OF_CUSTOMERS_COLUMN_ADDED_VERSION);
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insertTitle(TitleModel title) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.TITLE_NAME, title.getTitleName());
        contentValue.put(DatabaseHelper.CREATED_ON, title.getCreatedOn());
        contentValue.put(DatabaseHelper.INDEX_POS, title.getIndex());
        long result = database.insert(TITLE_TABLE_NAME, null, contentValue);
        if (result== -1){
            //return false;
            System.out.println("Failed to save");
        } else {
            //return true;
            System.out.println("Successfully save");
        }
    }

    public List<TitleModel> getTitles() {
        List<TitleModel> titles = new ArrayList<TitleModel>();
        DBManager dbManager = new DBManager(context);
        dbManager.open();
        EmiDBManager emiDBManager = new EmiDBManager(context);
        emiDBManager.open();
        Cursor cursor = database.rawQuery("select * from " + TITLE_TABLE_NAME + " ORDER BY " + DatabaseHelper.INDEX_POS + " ASC", null); //this.fetch();
        try {
            while (cursor.moveToNext()) {
                TitleModel title = new TitleModel();
                title.setTitleId(cursor.getString(0));
                title.setTitleName(cursor.getString(1));
                title.setCreatedOn(cursor.getString(2));
                title.setIndex(cursor.getInt(3));
                title.setTotalNoOfCustomers(cursor.getInt(4));

                title.setBalanceAMount(0);
                String balanceAmount = cursor.getString(6);
                if (balanceAmount != null) {
                    title.setBalanceAMount(Double.parseDouble(balanceAmount));
                }

                title.setReceivedAmount(0);
                List<EMIBean> emis = emiDBManager.getEmisByCurrentDateAndTitle(title.titleId);
                double amount = 0.0;
                for (int i=0;i<emis.size();i++) {
                    amount += emis.get(i).getAmount();
                }
                title.setReceivedAmount(amount);

                titles.add(title);
            }
        } finally {
            cursor.close();
        }

        //Collections.sort(titles);
        return titles;
    }

    public int updateTitle(TitleModel titleModel, String title) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.TITLE_NAME, title);
        int i = database.update(DatabaseHelper.TITLE_TABLE_NAME, contentValues, DatabaseHelper.TITLE_ID + " = " + titleModel.getTitleId(), null);
        return i;
    }

    public int updateCustomersCount(String titleId) {
        DBManager dbManager = new DBManager(context);
        dbManager.open();
        List<CustomerBean> customers = dbManager.getCustomers(titleId);

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.NO_OF_CUSTOMERS, customers.size());
        int i = database.update(DatabaseHelper.TITLE_TABLE_NAME, contentValues, DatabaseHelper.TITLE_ID + " = " + titleId, null);

        updateOverallAmount(titleId);
        return i;
    }

    public int updateOverallAmount(String titleId) {
        DBManager dbManager = new DBManager(context);
        dbManager.open();

        EmiDBManager emiDBManager = new EmiDBManager(context);
        emiDBManager.open();
        double received = 0.0;
        double balance = 0.0;

        List<EMIBean> emiBeans = emiDBManager.getEmisByCurrentDateAndTitle(titleId);
        for (int i=0;i<emiBeans.size();i++) {
            received += emiBeans.get(i).getAmount();
        }

        List<CustomerBean> customers = dbManager.getCustomers(titleId);
        for (int i=0; i<customers.size();i++) {
            balance = balance + customers.get(i).getBalance();
        }
        emiDBManager.close();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.RECEIVED_AMOUNT, String.valueOf(received));
        contentValues.put(DatabaseHelper.BALANCE_AMOUNT, String.valueOf(balance));
        int i = database.update(DatabaseHelper.TITLE_TABLE_NAME, contentValues, DatabaseHelper.TITLE_ID + " = " + titleId, null);
        return i;
    }

    public int updateIndex(TitleModel titleModel, int newPos) {
        int oldPos = -1;
        List<TitleModel> titles = getTitles();

        for (int pos=0;pos<titles.size();pos++) {
            if (titles.get(pos).getTitleId().equalsIgnoreCase(titleModel.getTitleId())) {
                oldPos = pos;
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
                database.update(TITLE_TABLE_NAME, contentValues, DatabaseHelper.TITLE_ID + " = " + titles.get(i).getTitleId(), null);
            }
            contentValues.put(DatabaseHelper.INDEX_POS, newPos);
            database.update(TITLE_TABLE_NAME, contentValues, DatabaseHelper.TITLE_ID + " = " + titles.get(oldPos).getTitleId(), null);
        } else if (oldPos<newPos) { // move down
            for (int i=oldPos+1;i<=newPos;i++) {
                //i = i - 1;
                contentValues.put(DatabaseHelper.INDEX_POS, i-1);
                database.update(TITLE_TABLE_NAME, contentValues, DatabaseHelper.TITLE_ID + " = " + titles.get(i).getTitleId(), null);
            }

            contentValues.put(DatabaseHelper.INDEX_POS, newPos);
            database.update(TITLE_TABLE_NAME, contentValues, DatabaseHelper.TITLE_ID + " = " + titles.get(oldPos).getTitleId(), null);
        }

        return 0;
    }

    public void deleteTitle(TitleModel title) {
        database.delete(TITLE_TABLE_NAME, DatabaseHelper.TITLE_ID + " = " + title.getTitleId(), null);

        List<TitleModel> titleModels = getTitles();
        for (int pos=0;pos<titleModels.size();pos++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.INDEX_POS, pos);
            int i = database.update(TITLE_TABLE_NAME, contentValues, DatabaseHelper.TITLE_ID + " = " + titleModels.get(pos).getTitleId(), null);
            if (i== -1){
                System.out.println("Failed to update");
            } else {
                System.out.println("Successfully updated");
            }
        }

        DBManager dbManager = new DBManager(context);
        dbManager.open();
        EmiDBManager emiDBManager = new EmiDBManager(context);
        emiDBManager.open();
        List<CustomerBean> customerBeans = dbManager.getCustomers(title.getTitleId());
        for (int pos=0;pos<customerBeans.size();pos++) {
            database.delete(DatabaseHelper.CUSTOMERS_TABLE_NAME, DatabaseHelper.CUSTOMER_ID + " = " + customerBeans.get(pos).getCustomerId(), null);
            emiDBManager.deleteEmiByCustomer(customerBeans.get(pos));
        }
        emiDBManager.close();
        dbManager.close();
    }
}

package com.psr.financial;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psr.financial.Adapter.CustomerEMIReportAdapter;
import com.psr.financial.Database.DBManager;
import com.psr.financial.Database.EmiDBManager;
import com.psr.financial.Models.CustomerBean;
import com.psr.financial.Models.EMIBean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.psr.financial.MainActivity.UPDATE_CUSTOMER_DETAILS_REQUEST_CODE;

public class CustomerReportActivity extends AppCompatActivity implements CustomerEMIReportAdapter.UpdateEmiInterface {

    RecyclerView customerReportRecycleView;
    CustomerEMIReportAdapter adapter;

    Map<String, List<EMIBean>> dateByEmiBean = new LinkedHashMap<>();
    CustomerBean customerBean;

    private DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_report);

        dbManager = new DBManager(this);
        dbManager.open();

        customerBean = (CustomerBean) getIntent().getSerializableExtra("customer");
        EmiDBManager emiDBManager = new EmiDBManager(this);
        emiDBManager.open();
        customerBean.setEmi(emiDBManager.getCustomerEmis(customerBean.getCustomerId()));
        emiDBManager.close();
        loadEMiByDate();

        customerReportRecycleView = (RecyclerView) findViewById(R.id.customer_report_recycleView);
        adapter = new CustomerEMIReportAdapter(this, customerBean, dateByEmiBean, this);
        customerReportRecycleView.setHasFixedSize(true);
        customerReportRecycleView.setLayoutManager(new LinearLayoutManager(this));
        customerReportRecycleView.setAdapter(adapter);
    }

    public void loadEMiByDate() {
        dateByEmiBean.clear();

        List<EMIBean> emis = customerBean.getEmi();
        for (int i=0;i<emis.size();i++) {
            EMIBean emiBeansTemp = emis.get(i);
            String date = Utilities.getDateStringFromTimeStamp(emiBeansTemp.getDate(), null);

            if (dateByEmiBean.containsKey(date)) {
                List<EMIBean> temp = dateByEmiBean.get(date);
                temp.add(emiBeansTemp);
                dateByEmiBean.put(date, temp);
            } else {
                List<EMIBean> temp = new ArrayList<>();
                temp.add(emiBeansTemp);
                dateByEmiBean.put(date, temp);
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == UPDATE_CUSTOMER_DETAILS_REQUEST_CODE && resultCode  == RESULT_OK) {
            customerBean = (CustomerBean) data.getSerializableExtra("customer");
            loadEMiByDate();
            adapter.notifyDataSetChanged();

            Intent intent=new Intent();
            setResult(RESULT_OK, intent);
            intent.putExtra("customer", customerBean);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateEmiDelete() {
        EmiDBManager emiDBManager = new EmiDBManager(this);
        emiDBManager.open();
        List<EMIBean> emiBeans = emiDBManager.getCustomerEmis(customerBean.getCustomerId());
        customerBean.setEmi(emiBeans);
        updateCustomer();
        loadEMiByDate();
        adapter.notifyDataSetChanged();

        Intent intent=new Intent();
        setResult(RESULT_OK, intent);
        intent.putExtra("customer", customerBean);
        if (emiBeans.size()==0) {
            finish();
        }
    }

    void updateCustomer() {
        double receivedAmount = 0;
        for (int i=0;i<customerBean.emi.size();i++) {
            receivedAmount = receivedAmount + Double.valueOf(customerBean.emi.get(i).getAmount());
        }
        customerBean.setReceived(receivedAmount);
        customerBean.setBalance(customerBean.amount - receivedAmount);
        dbManager.updateCustomerAmount(customerBean);
    }
}

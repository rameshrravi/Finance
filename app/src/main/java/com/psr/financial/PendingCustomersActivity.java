package com.psr.financial;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psr.financial.Database.DBManager;
import com.psr.financial.Database.EmiDBManager;
import com.psr.financial.Models.CustomerBean;
import com.psr.financial.Models.EMIBean;
import com.psr.financial.Models.TitleModel;

import java.util.ArrayList;
import java.util.List;

public class PendingCustomersActivity extends AppCompatActivity {

    public List<CustomerBean> customerBeanList = new ArrayList<CustomerBean>();

    TextView overAllCollectionAmountTextView;
    RelativeLayout emptyLayout;
    LinearLayout bottomLayout;
    RecyclerView collectionRecycleView;

    CollectionDetailsAdapter adapter;
    Session session;
    TitleModel title;

    private DBManager dbManager;
    private EmiDBManager emiDBManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collection_details_layout);
        session = Session.getInstance(this);

        if (getIntent().getExtras().containsKey("title")) {
            title = (TitleModel) getIntent().getSerializableExtra("title");
            this.setTitle(title.getTitleName());
            //customerBeanList = (ArrayList<CustomerBean>)getIntent().getSerializableExtra("customers");
        }

        emptyLayout = (RelativeLayout) findViewById(R.id.empty_layout);
        overAllCollectionAmountTextView = (TextView) findViewById(R.id.overAll_collection_amount_textView);
        bottomLayout = (LinearLayout) findViewById(R.id.bottom_layout);
        bottomLayout.setVisibility(View.GONE);

        collectionRecycleView = (RecyclerView) findViewById(R.id.collection_recycleView);
        adapter = new CollectionDetailsAdapter();
        collectionRecycleView.setHasFixedSize(true);
        collectionRecycleView.setLayoutManager(new LinearLayoutManager(this));

        collectionRecycleView.setItemAnimator(new DefaultItemAnimator());
        collectionRecycleView.setAdapter(adapter);

        dbManager = new DBManager(this);
        dbManager.open();

        emiDBManager = new EmiDBManager(this);
        emiDBManager.open();

        customerBeanList = dbManager.getCustomers(title.titleId);
        adapter.notifyDataSetChanged();
        checkEmptyState();
    }

    void checkEmptyState() {
        emptyLayout.setVisibility(View.VISIBLE);
        if (customerBeanList != null && customerBeanList.size()>0) {
            emptyLayout.setVisibility(View.GONE);
        }
        new GetTodayOverAllCollections().execute();
        //loadOverAllAmount();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class CollectionDetailsAdapter extends RecyclerView.Adapter<CollectionDetailsViewHolder>{

        @Override
        public CollectionDetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem= layoutInflater.inflate(R.layout.collection_details_list_item, parent, false);
            CollectionDetailsViewHolder viewHolder = new CollectionDetailsViewHolder(listItem);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(CollectionDetailsViewHolder holder, final int position) {
            final CustomerBean customerBean = customerBeanList.get(position);
            holder.nameTextView.setText(customerBean.getName());

            holder.amountTextView.setText("");
            if (customerBean.getLastPayment() != null && customerBean.getTodayCollection()>0) {
                holder.amountTextView.setText(String.format("%.2f", customerBean.getTodayCollection()));
            }

            if (!customerBeanList.get(position).isDataLoaded()) {
                new GetCustomerTodayOverAllCollections().execute(position);
            }
        }

        @Override
        public int getItemCount() {
            return customerBeanList.size();
        }
    }

    public static class CollectionDetailsViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView amountTextView;

        public CollectionDetailsViewHolder(View itemView) {
            super(itemView);
            this.nameTextView = (TextView) itemView.findViewById(R.id.name_textView);
            this.amountTextView = (TextView) itemView.findViewById(R.id.amount_textView);
        }
    }

    private class GetCustomerTodayOverAllCollections extends AsyncTask<Integer, Integer, Integer> {
        protected Integer doInBackground(Integer... position) {
            int pos = position[0];
            EmiDBManager emiDBManager = new EmiDBManager(PendingCustomersActivity.this);
            emiDBManager.open();
            if (customerBeanList.get(pos).getLastPaymentId() != null) {
                List<EMIBean> emiBeans = emiDBManager.getEmiById(customerBeanList.get(pos).getLastPaymentId());
                if (emiBeans.size()>0) {
                    customerBeanList.get(pos).setLastPayment(emiBeans.get(0));
                }
            } else {
                List<EMIBean> emiBeans = emiDBManager.getCustomerEmis(customerBeanList.get(pos).getCustomerId());
                if (emiBeans.size()>0) {
                    customerBeanList.get(pos).setLastPayment(emiBeans.get(0));
                }
            }

            customerBeanList.get(pos).setTodayCollection(emiDBManager.getCurrentAmountByCurrentDateAndCustomer(customerBeanList.get(pos).customerId));
            customerBeanList.get(pos).setDataLoaded(true);
            emiDBManager.close();
            return pos;
        }

        protected void onPostExecute(Integer position) {
            adapter.notifyDataSetChanged();
        }
    }

    private class GetTodayOverAllCollections extends AsyncTask<Void, Integer, Double> {
        protected Double doInBackground(Void... index) {
            double amount = 0.0;
            EmiDBManager emiDBManager = new EmiDBManager(PendingCustomersActivity.this);
            emiDBManager.open();
            List<EMIBean> emis = emiDBManager.getEmisByCurrentDateAndTitle(title.getTitleId());
            for (int i=0;i<emis.size();i++) {
                amount += emis.get(i).getAmount();
            }
            emiDBManager.close();
            return amount;
        }

        protected void onPostExecute(Double amount) {
            if (customerBeanList.size()>0) {
                bottomLayout.setVisibility(View.VISIBLE);
            }
            overAllCollectionAmountTextView.setText("Today Overall Collection : " + String.format("%.2f", amount));
        }
    }
}
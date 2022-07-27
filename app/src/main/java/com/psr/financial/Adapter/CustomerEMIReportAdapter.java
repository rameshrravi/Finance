package com.psr.financial.Adapter;

import android.app.Activity;
//import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psr.financial.CustomerDetailsActivity;
import com.psr.financial.Database.DatabaseHelper;
import com.psr.financial.Database.EmiDBManager;
import com.psr.financial.Models.CustomerBean;
import com.psr.financial.Models.EMIBean;
import com.psr.financial.R;
import com.psr.financial.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.psr.financial.MainActivity.UPDATE_CUSTOMER_DETAILS_REQUEST_CODE;

public class CustomerEMIReportAdapter extends RecyclerView.Adapter<CustomerEMIReportAdapter.EMIDateViewHolder> {

    public interface UpdateEmiInterface {
        public void updateEmiDelete();
    }

    CustomerBean customer;
    Map<String, List<EMIBean>> emiBean = new HashMap<>();
    Context context;
    CustomersEmiInnerAdapter innerAdapter;
    UpdateEmiInterface updateEmiInterface;

    public CustomerEMIReportAdapter(Context context, CustomerBean customer, Map<String, List<EMIBean>> emiBean, UpdateEmiInterface updateEmiInterface) {
        this.customer = customer;
        this.context = context;
        this.emiBean = emiBean;
        this.updateEmiInterface = updateEmiInterface;
    }

    @Override
    public EMIDateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.customer_report_listview_item, parent, false);
        EMIDateViewHolder viewHolder = new EMIDateViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(EMIDateViewHolder holder, final int position) {
        Object firstKey = emiBean.keySet().toArray()[position];
        holder.dateTextView.setText(String.valueOf(firstKey));

        innerAdapter = new CustomersEmiInnerAdapter(emiBean.get(firstKey));
        holder.customerReportInnerRecycleView.setHasFixedSize(true);
        holder.customerReportInnerRecycleView.setLayoutManager(new LinearLayoutManager(context));
        holder.customerReportInnerRecycleView.setAdapter(innerAdapter);
    }

    @Override
    public int getItemCount() {
        return emiBean.size();
    }

    public static class EMIDateViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public RecyclerView customerReportInnerRecycleView;

        public EMIDateViewHolder(View itemView) {
            super(itemView);
            this.dateTextView = (TextView) itemView.findViewById(R.id.date_textView);
            this.customerReportInnerRecycleView = (RecyclerView) itemView.findViewById(R.id.customer_report_inner_recycleView);
        }
    }

    class CustomersEmiInnerAdapter extends RecyclerView.Adapter<AmountViewHolder>{

        List<EMIBean> emiBeans = new ArrayList<EMIBean>();

        public CustomersEmiInnerAdapter(List<EMIBean> emiBeans) {
            this.emiBeans = emiBeans;
        }

        @Override
        public AmountViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem= layoutInflater.inflate(R.layout.customer_report_inner_listview, parent, false);
            AmountViewHolder viewHolder = new AmountViewHolder(listItem);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(AmountViewHolder holder, final int position) {
            final EMIBean emiBean = emiBeans.get(position);
            holder.amountTextView.setText(String.format("%.2f", emiBean.getAmount()));
            holder.timeTextView.setText(Utilities.getTimeFromTimeStamp(emiBean.getDate()));

            holder.editAmountButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, CustomerDetailsActivity.class);
                    i.putExtra("isEdit", true);
                    i.putExtra("emiBean", emiBean);
                    i.putExtra("customer", customer);
                    //context.startActivity(i);
                    ((Activity)context).startActivityForResult(i, UPDATE_CUSTOMER_DETAILS_REQUEST_CODE);
                }
            });

            if (position % 2 == 0) {
                holder.rowView.setBackgroundColor(Color.parseColor("#e6e6e6"));
            } else {
                holder.rowView.setBackgroundColor(Color.parseColor("#cdcdcd"));
            }

            if (emiBean.getCollectBy().equalsIgnoreCase(DatabaseHelper.INTEREST)) {
                holder.rowView.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_bright));
            }

            holder.deleteAmountButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteCustomerConfirmation("Delete Amount", "You can't retrieve once you delete. Are you sure want to delete this amount?.", emiBean);

                }
            });
        }

        @Override
        public int getItemCount() {
            return emiBeans.size();
        }

        public void deleteCustomerConfirmation(String title, final String message, final EMIBean emiBean) {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle(title);
            alert.setMessage(message);

            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    deleteEmi(emiBean);
                }
            });

            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            });

            alert.show();
        }

        void deleteEmi(EMIBean emiBean) {
            EmiDBManager emiDBManager = new EmiDBManager(context);
            emiDBManager.open();
            emiDBManager.deleteEmi(emiBean);
            emiDBManager.close();
            updateEmiInterface.updateEmiDelete();
        }
    }

    public static class AmountViewHolder extends RecyclerView.ViewHolder {
        public TextView amountTextView;
        public TextView timeTextView;
        public ImageButton editAmountButton, deleteAmountButton;
        public RelativeLayout rowView;

        public AmountViewHolder(View itemView) {
            super(itemView);
            this.rowView = (RelativeLayout) itemView.findViewById(R.id.rowView);
            this.amountTextView = (TextView) itemView.findViewById(R.id.amount_textView);
            this.timeTextView = (TextView)itemView.findViewById(R.id.time_textView);
            this.editAmountButton = (ImageButton) itemView.findViewById(R.id.edit_amount_button);
            this.deleteAmountButton = (ImageButton) itemView.findViewById(R.id.delete_amount_button);
        }
    }
}
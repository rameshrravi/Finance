package com.psr.financial.Adapter;

import android.content.Context;
//import android.support.annotation.NonNull;
//import android.support.v7.widget.RecyclerView;
//import android.support.v7.widget.helper.ItemTouchHelper;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.psr.financial.Database.DBManager;
import com.psr.financial.Database.TitleDBManager;
import com.psr.financial.MainActivity;
import com.psr.financial.Models.CustomerBean;
import com.psr.financial.Models.TitleModel;
import com.psr.financial.TitleActivity;

public class ItemMoveCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperContract mAdapter;
    public Context context;
    public ItemMoveCallback(Context context, ItemTouchHelperContract adapter) {
        this.context = context;
        mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        if (mAdapter instanceof MainActivity.CustomersListAdapter) {
            return ((MainActivity.CustomersListAdapter)mAdapter).isLongPressDragEnabled;
        }
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }



    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        mAdapter.onRowMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof MainActivity.ViewHolder) {
                MainActivity.ViewHolder myViewHolder= (MainActivity.ViewHolder) viewHolder;
                mAdapter.onRowSelected(myViewHolder);
            } else if (viewHolder instanceof TitleActivity.ViewHolder) {
                TitleActivity.ViewHolder myViewHolder= (TitleActivity.ViewHolder) viewHolder;
                mAdapter.onRowSelected(myViewHolder);
            }
        }

        super.onSelectedChanged(viewHolder, actionState);
    }
    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        if (viewHolder instanceof MainActivity.ViewHolder) {
            MainActivity.ViewHolder myViewHolder= (MainActivity.ViewHolder) viewHolder;
            mAdapter.onRowClear(myViewHolder);
            CustomerBean customerBean = (CustomerBean) myViewHolder.itemView.getTag();
            DBManager dbManager = new DBManager(context);
            dbManager.open();
            dbManager.updateIndex(customerBean, myViewHolder.getPosition());
        } else if (viewHolder instanceof TitleActivity.ViewHolder) {
            TitleActivity.ViewHolder myViewHolder= (TitleActivity.ViewHolder) viewHolder;
            mAdapter.onRowClear(myViewHolder);
            TitleModel titleModel = (TitleModel) myViewHolder.itemView.getTag();
            TitleDBManager titleDBManager = new TitleDBManager(context);
            titleDBManager.open();
            titleDBManager.updateIndex(titleModel, myViewHolder.getPosition());
        }
    }

    public interface ItemTouchHelperContract {
        void onRowMoved(int fromPosition, int toPosition);
        void onRowSelected(RecyclerView.ViewHolder myViewHolder);
        void onRowClear(RecyclerView.ViewHolder myViewHolder);
    }
}



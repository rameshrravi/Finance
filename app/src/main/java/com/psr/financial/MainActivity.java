package com.psr.financial;

import android.Manifest;
import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.SearchManager;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.design.widget.FloatingActionButton;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.view.ActionMode;
//import android.support.v7.widget.CardView;
//import android.support.v7.widget.DefaultItemAnimator;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.support.v7.widget.SearchView;
//import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.SparseBooleanArray;
//import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
//import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.psr.financial.Adapter.ClickAdapterListener;
import com.psr.financial.Adapter.ItemMoveCallback;
import com.psr.financial.Adapter.StartDragListener;
import com.psr.financial.Adapter.SwipeController;
import com.psr.financial.Adapter.SwipeControllerActions;
import com.psr.financial.Database.DBManager;
import com.psr.financial.Database.DatabaseHelper;
import com.psr.financial.Database.EmiDBManager;
import com.psr.financial.Models.CustomerBean;
import com.psr.financial.Models.EMIBean;
import com.psr.financial.Models.TitleModel;
import com.psr.financial.Utility.ExportDatabaseCSVTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;


public class MainActivity extends AppCompatActivity implements StartDragListener, ClickAdapterListener {

    public static int ADD_NEW_CUSTOMER_REQUEST_CODE = 100;
    public static int UPDATE_CUSTOMER_DETAILS_REQUEST_CODE = 101;
    public static final int MAKE_CALL_PERMISSION_REQUEST_CODE = 102;
    public static final int MAKE_EXRERNAL_PERMISSION_REQUEST_CODE = 103;

    public List<CustomerBean> customerBeanList = new ArrayList<CustomerBean>();

    TextView overAllCollectionAmountTextView, overAllBalanceAmountTextView;
    RelativeLayout emptyLayout;
    LinearLayout bottomLayout;
    RecyclerView customerRecycleView;
    CustomersListAdapter adapter;
    private DBManager dbManager;
    Session session;

    CustomerBean selectedCustomerBean;
    TitleModel title;

    ItemTouchHelper touchHelper;
    private SearchView searchView;
    private List<CustomerBean> contactListFiltered;
    public boolean exportSingleUserData = false;

    private ActionModeCallback actionModeCallback;
    private ActionMode actionMode;
    private static int currentSelectedIndex = -1;
    private SparseBooleanArray selectedItems;

    SwipeController swipeController = null;

    List<Integer> selectedCollectionBy = new ArrayList<>();
    boolean[] checkedItems = { false, false, false, false, false };
    String selectedCollectionType = "All";
    boolean isPhone2 = false;
    Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        session = Session.getInstance(this);

        selectedCollectionBy.add(0);

        if (getIntent().getExtras().containsKey("title")) {
            title = (TitleModel) getIntent().getSerializableExtra("title");
            this.setTitle(title.getTitleName());
        }

        emptyLayout = (RelativeLayout) findViewById(R.id.empty_layout);
        overAllCollectionAmountTextView = (TextView) findViewById(R.id.overAll_collection_amount_textView);
        overAllBalanceAmountTextView = (TextView) findViewById(R.id.overAll_balance_amount_textView);
        bottomLayout = (LinearLayout) findViewById(R.id.bottom_layout);
        //bottomLayout.setVisibility(View.GONE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, AddNewCustomer.class);
                i.putExtra("title", title);
                startActivityForResult(i, ADD_NEW_CUSTOMER_REQUEST_CODE);
            }
        });

        customerRecycleView = (RecyclerView) findViewById(R.id.customer_recycleView);
        adapter = new CustomersListAdapter(this, this);
        customerRecycleView.setHasFixedSize(true);
        customerRecycleView.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper.Callback callback = new ItemMoveCallback(this, adapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(customerRecycleView);


        //whiteNotificationBar(customerRecycleView);
        customerRecycleView.setItemAnimator(new DefaultItemAnimator());
        //customerRecycleView.addItemDecoration(new MyDividerItemDecoration(this, DividerItemDecoration.VERTICAL, 36));
        customerRecycleView.setAdapter(adapter);

        dbManager = new DBManager(this);
        dbManager.open();

//        customerBeanList = dbManager.getCustomers(title.titleId);
//        contactListFiltered =  new ArrayList<>(customerBeanList);
//        adapter.notifyDataSetChanged();
//        checkEmptyState();

        actionModeCallback = new ActionModeCallback();
        //swipeGesture();

        //overAllBalanceAmountTextView.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.customerBeanList = dbManager.getCustomers(title.getTitleId());
        this.contactListFiltered = new ArrayList<>(customerBeanList);
        if (adapter != null) {
            adapter.filterByCollect(selectedCollectionType);
        } else {
            adapter.notifyDataSetChanged();
            checkEmptyState();
        }
    }

    void checkEmptyState() {
        emptyLayout.setVisibility(View.VISIBLE);
        if (contactListFiltered != null && contactListFiltered.size()>0) {
            emptyLayout.setVisibility(View.GONE);
        }

        loadOverAllAmount();
    }

    void loadOverAllAmount() {
        overAllCollectionAmountTextView.setText("Today Collection : " + String.format("%.2f", 0.0));
        overAllBalanceAmountTextView.setText("Balance Amount : " + String.format("%.2f", 0.0));

        new GetTodayOverAllCollections().execute();
        //loadOverAllAmount1();
    }

    void loadOverAllAmount1() {
        double amount = 0.0;
        double balanceAmount = 0.0;
        EmiDBManager emiDBManager = new EmiDBManager(this);
        emiDBManager.open();
        for (CustomerBean cust: contactListFiltered) {
            List<EMIBean> emis = emiDBManager.getCustomerEmis(cust.getCustomerId());

            double receivedAmount = 0.0;
            for (int i = 0; i < emis.size(); i++) {
                EMIBean emi = emis.get(i);
                Date emiDate = Utilities.getDateFromTimeStamp(emi.getDate(), Utilities.DD_MM_YYYY);
                Date currentDate = Utilities.getDateFromTimeStamp(String.valueOf(myCalendar.getTimeInMillis()), Utilities.DD_MM_YYYY);
                if (emiDate.compareTo(currentDate) == 0) {
                    amount += emi.getAmount();
//                    if (!emi.getCollectBy().equalsIgnoreCase(DatabaseHelper.INTEREST)) {
//                        receivedAmount += emi.getAmount();
//                    }
                }
            }

            balanceAmount += cust.getBalance(); // - receivedAmount;
        }
        emiDBManager.close();
        //bottomLayout.setVisibility(View.GONE);
        if (customerBeanList.size()>0) {
            bottomLayout.setVisibility(View.VISIBLE);
        }
        overAllCollectionAmountTextView.setText("Today Collection : " + String.format("%.2f", amount));
        overAllBalanceAmountTextView.setText("Balance Amount : " + String.format("%.2f", balanceAmount));
    }

    void swipeGesture() {
        swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
                //adapter.players.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, adapter.getItemCount());
            }
        });

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(customerRecycleView);

        customerRecycleView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.customers_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                adapter.getFilter().filter(query);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_search:
                return true;
            case R.id.filter_button:
                showFilterDialog();
                return true;
            case R.id.export_button:
                exportSingleUserData = false;
                if (contactListFiltered.size()>0) {
                    if (isStoragePermissionGranted()) {
                        ExportDatabaseCSVTask exportDatabaseCSVTask = new ExportDatabaseCSVTask(this);
                        exportDatabaseCSVTask.createExcelSheet(contactListFiltered);
                    }
                }
                return true;
            case R.id.custom_selection_button:
                actionMode = startSupportActionMode(actionModeCallback);
                actionMode.setTitle("0");
                actionMode.invalidate();
                adapter.isLongPressDragEnabled = false;
                //listener.onRowLongClicked(getAdapterPosition());
                //view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                return true;
            case R.id.other_customer_button:
                Intent i = new Intent(this, PendingCustomersActivity.class);
                i.putExtra("title", title);
                //i.putExtra("customers", (ArrayList<CustomerBean>) customerBeanList);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    private void showFilterDialog1() {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setTitle("Choose an animal");

        // add a radio button list
        final String[] collects = getResources().getStringArray(R.array.collect);
        builder.setItems(collects, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // All
                        selectedCollectionType = collects[0];
                        adapter.filterByCollect(collects[0]);
                        break;
                    case 1: // Daily
                        selectedCollectionType = collects[1];
                        adapter.filterByCollect(collects[1]);
                        break;
                    case 2: // Weekly
                        selectedCollectionType = collects[2];
                        adapter.filterByCollect(collects[2]);
                        break;
                    case 3: // Monthly
                        selectedCollectionType = collects[3];
                        adapter.filterByCollect(collects[3]);
                        break;
                    case 4: // Yearly
                        selectedCollectionType = collects[4];
                        adapter.filterByCollect(collects[4]);
                        break;
                }
            }
        });

        builder.show();
    }

    public void showFilterDialog() {

        // create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter By");

        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.collec_by_filter_layout, null);
        builder.setView(customLayout);

        // add a button
        builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // send data from the AlertDialog to the Activity
                //EditText editText = customLayout.findViewById(R.id.editText);

                //sendDialogDataToActivity(editText.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        final CheckBox allCheckBox = customLayout.findViewById(R.id.all_checkBox);
        final CheckBox dailyCheckBox = customLayout.findViewById(R.id.daily_checkBox);
        final CheckBox weeklyCheckBox = customLayout.findViewById(R.id.weekly_checkBox);
        final CheckBox monthlyCheckBox = customLayout.findViewById(R.id.monthly_checkBox);
        final CheckBox interestCheckBox = customLayout.findViewById(R.id.interest_checkBox);

        allCheckBox.setChecked(false);
        dailyCheckBox.setChecked(false);
        weeklyCheckBox.setChecked(false);
        monthlyCheckBox.setChecked(false);
        interestCheckBox.setChecked(false);

        for (int i=0;i<selectedCollectionBy.size();i++) {
            switch (selectedCollectionBy.get(i)) {
                case 0:
                    allCheckBox.setChecked(true);
                    break;
                case 1:
                    dailyCheckBox.setChecked(true);
                    break;
                case 2:
                    weeklyCheckBox.setChecked(true);
                    break;
                case 3:
                    monthlyCheckBox.setChecked(true);
                    break;
                case 4:
                    interestCheckBox.setChecked(true);
                    break;
            }
        }
        allCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( ((CheckBox)v).isChecked() ) {
                    dailyCheckBox.setChecked(false);
                    weeklyCheckBox.setChecked(false);
                    monthlyCheckBox.setChecked(false);
                    interestCheckBox.setChecked(false);
                }
            }
        });
        dailyCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allCheckBox.setChecked(false);
            }
        });
        weeklyCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allCheckBox.setChecked(false);
            }
        });
        monthlyCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allCheckBox.setChecked(false);
            }
        });
        interestCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allCheckBox.setChecked(false);
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {
                Button b = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        List<Integer> tempCollectionBy = new ArrayList<>();
                        if (allCheckBox.isChecked()) {
                            tempCollectionBy.add(0);
                        }
                        if (dailyCheckBox.isChecked()) {
                            tempCollectionBy.add(1);
                        }
                        if (weeklyCheckBox.isChecked()) {
                            tempCollectionBy.add(2);
                        }
                        if (monthlyCheckBox.isChecked()) {
                            tempCollectionBy.add(3);
                        }
                        if (interestCheckBox.isChecked()) {
                            tempCollectionBy.add(4);
                        }
                        if (tempCollectionBy.size()>0) {
                            selectedCollectionBy.clear();
                            selectedCollectionBy.addAll(tempCollectionBy);
                            adapter.filterByCollect();
                            dialog.dismiss();
                        }else {
                             Utilities.errorAlert(MainActivity.this, "","Please select atleast one type");
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    private void whiteNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MAKE_EXRERNAL_PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ADD_NEW_CUSTOMER_REQUEST_CODE && resultCode  == RESULT_OK) {
            this.customerBeanList = dbManager.getCustomers(title.getTitleId());
            this.contactListFiltered = new ArrayList<>(customerBeanList);
            adapter.notifyDataSetChanged();
            checkEmptyState();
        } else if(requestCode == UPDATE_CUSTOMER_DETAILS_REQUEST_CODE && resultCode  == RESULT_OK) {
            this.customerBeanList = dbManager.getCustomers(title.getTitleId());
            this.contactListFiltered = new ArrayList<>(customerBeanList);
            adapter.notifyDataSetChanged();
            checkEmptyState();
            if (data != null && data.getExtras() != null && data.getExtras().containsKey("moveToPos")) {
                int moveToPos = data.getExtras().getInt("moveToPos");
                customerRecycleView.getLayoutManager().smoothScrollToPosition(customerRecycleView, new RecyclerView.State(), moveToPos);
            }
        }
    }

    @Override
    public void requestDrag(RecyclerView.ViewHolder viewHolder) {
        touchHelper.startDrag(viewHolder);
    }

    public void checkAndMakePhoneCall() {
        if (checkPermission(Manifest.permission.CALL_PHONE)) {
            try {
                Intent intent = new Intent(Intent.ACTION_CALL);
                if (!isPhone2) {
                    intent.setData(Uri.parse("tel:" + selectedCustomerBean.getPhone()));
                } else {
                    intent.setData(Uri.parse("tel:" + selectedCustomerBean.getPhone2()));
                }
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getApplicationContext(), "Error in your phone call"+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, MAKE_CALL_PERMISSION_REQUEST_CODE);
        }
    }

    private boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case MAKE_CALL_PERMISSION_REQUEST_CODE :
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "You can call the number by clicking on the button", Toast.LENGTH_SHORT).show();
                    checkAndMakePhoneCall();
                }
                return;
            case MAKE_EXRERNAL_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
                    //resume tasks needing this permission
                    if (exportSingleUserData) {
                        exportSingleUserData = false;
                        ExportDatabaseCSVTask exportDatabaseCSVTask = new ExportDatabaseCSVTask(MainActivity.this);
                        exportDatabaseCSVTask.createExcelSheet(selectedCustomerBean);
                    } else {
                        ExportDatabaseCSVTask exportDatabaseCSVTask = new ExportDatabaseCSVTask(this);
                        if (actionMode == null) {
                            exportDatabaseCSVTask.createExcelSheet(adapter.getSelectedCustomers());
                        } else {
                            exportDatabaseCSVTask.createExcelSheet(customerBeanList);
                        }
                    }
                }
                break;
        }
    }

    public void confirmationAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Uncomment the below code to Set the message and title from the strings.xml file
        //builder.setMessage(R.string.dialog_message) .setTitle(R.string.dialog_title);

        //Setting message manually and performing action on button click
        builder.setMessage("Would you like to call this number?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //finish();
                        checkAndMakePhoneCall();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //  Action for 'NO' Button
                        dialog.cancel();
                    }
                });
        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        alert.setTitle("");
        alert.show();
    }

    public static void showAlertDialog(final Activity context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        //Setting message manually and performing action on button click
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //context.finish();
                        dialog.cancel();
                    }
                });
        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        alert.setTitle("");
        alert.show();
    }

    @Override
    public void onRowClicked(int position) {
        enableActionMode(position);
    }

    @Override
    public void onRowLongClicked(int position) {
        enableActionMode(position);
    }

    private void enableActionMode(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        adapter.toggleSelection(position);
        int count = adapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
            actionMode = null;
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    private void selectAll() {
        adapter.selectAll();
        int count = adapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }

        //actionMode = null;
    }

    private class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Log.d("API123", "here");
            switch (item.getItemId()) {
                /*case R.id.action_delete:
                    // delete all the selected rows
                    deleteRows();
                    mode.finish();
                    return true;*/
                case R.id.export_button:
                    if (selectedItems.size()>0) {
                        exportSingleUserData = false;
                        if (isStoragePermissionGranted()) {
                            ExportDatabaseCSVTask exportDatabaseCSVTask = new ExportDatabaseCSVTask(MainActivity.this);
                            exportDatabaseCSVTask.createExcelSheet(adapter.getSelectedCustomers());
                        }
                    }
                    mode.finish();
                    return true;

                case R.id.select_all_button:
                    //if (adapter.getSelectedItemCount() != selectedItems.size()) {
                        selectAll();
                    //}
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.clearSelections();
            actionMode = null;
            adapter.deSelectAll();
            adapter.notifyDataSetChanged();
            adapter.isLongPressDragEnabled = true;
        }
    }

    private void showContactsNumber() {
        List<String> phoneNum = new ArrayList<>();
        if (selectedCustomerBean.getPhone() != null && !selectedCustomerBean.getPhone().isEmpty()) {
            phoneNum.add(selectedCustomerBean.getPhone());
        }
        if (selectedCustomerBean.getPhone2() != null && !selectedCustomerBean.getPhone2().isEmpty()) {
            phoneNum.add(selectedCustomerBean.getPhone2());
        }

        if (phoneNum.size() > 1) {
            String[] mStringArray = new String[phoneNum.size()];
            mStringArray = phoneNum.toArray(mStringArray);
            // setup the alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            //builder.setTitle("Choose an animal");

            // add a radio button list
            builder.setItems(mStringArray, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            isPhone2 = false;
                            confirmationAlert();
                            break;
                        case 1:
                            isPhone2 = true;
                            checkAndMakePhoneCall();
                            break;
                    }
                }
            });
            builder.show();
        } else if (phoneNum.size() == 1) {
            isPhone2 = true;
            if (selectedCustomerBean.getPhone() != null && !selectedCustomerBean.getPhone().isEmpty()) {
                isPhone2 = false;
            }
            confirmationAlert();
        }
    }

    public class CustomersListAdapter extends RecyclerView.Adapter<ViewHolder> implements ItemMoveCallback.ItemTouchHelperContract, Filterable {

        StartDragListener mStartDragListener;
        public boolean isLongPressDragEnabled = true;

        private ClickAdapterListener listener;

        public CustomersListAdapter(StartDragListener mStartDragListener, ClickAdapterListener listener) {
            this.mStartDragListener = mStartDragListener;
            this.listener = listener;
            selectedItems = new SparseBooleanArray();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem = layoutInflater.inflate(R.layout.users_listview, parent, false);
            ViewHolder viewHolder = new ViewHolder(listItem);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final CustomerBean customer = contactListFiltered.get(position);
            holder.nameTextView.setText(customer.getName());
            holder.placeTextView.setText(customer.getPlace());
            holder.amountTextView.setText(String.format("%.2f", customer.getAmount()));
            holder.receivedAmountTextView.setText("Received : " + String.format("%.2f", customer.getReceived()));
            holder.balanceAmountTextView.setText("Balance : " + String.format("%.2f", customer.getBalance()));

            holder.collectByTextView.setText(customer.getCollectBy());
            holder.createdOnTextView.setText(Utilities.getDateStringFromTimeStamp(customer.getCreatedOn(), null));

            holder.phoneLayout.setVisibility(View.GONE);
            holder.phoneTextView.setText("");
            if (customer.getPhone() != null && !customer.getPhone().isEmpty()) {
                holder.phoneLayout.setVisibility(View.VISIBLE);
                holder.phoneTextView.setText(customer.getPhone());
            }
            holder.phoneLayout.setClickable(true);
            holder.phoneLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //isPhone2 = false;
                    selectedCustomerBean = customer;
                    //confirmationAlert();
                    showContactsNumber();
                }
            });

            holder.itemView.setTag(customer);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //if (isMultiSelectionEnable) {

                    //} else {
                        Context c = view.getContext();
                        Intent i = new Intent(c, CustomerDetailsActivity.class);
                        i.putExtra("customer", customer);
                        i.putExtra("position", position);
                        //startActivity(i);
                        startActivityForResult(i, UPDATE_CUSTOMER_DETAILS_REQUEST_CODE);
                    //}
                }
            });

            holder.completedTextView.setVisibility(View.GONE);
            if (customer.getReceived() >= customer.getAmount()) {
                holder.completedTextView.setVisibility(View.VISIBLE);
            }

            holder.editCustomerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(MainActivity.this, AddNewCustomer.class);
                    i.putExtra("isEdit", true);
                    i.putExtra("customer", customer);
                    startActivityForResult(i, UPDATE_CUSTOMER_DETAILS_REQUEST_CODE);
                }
            });

            holder.sendMessageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utilities.sendMessage(MainActivity.this, customer);
                }
            });

            holder.exportButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    exportSingleUserData = true;
                    selectedCustomerBean = customer;
                    if (isStoragePermissionGranted()) {
                        ExportDatabaseCSVTask exportDatabaseCSVTask = new ExportDatabaseCSVTask(MainActivity.this);
                        exportDatabaseCSVTask.createExcelSheet(customer);
                    }
                }
            });
            holder.insertCustomerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int index = position;
                    for (int i=0;i<customerBeanList.size();i++) {
                        if (customerBeanList.get(i).getCustomerId().equalsIgnoreCase(customer.getCustomerId())) {
                            index = i;
                            break;
                        }
                    }
                    Intent i = new Intent(MainActivity.this, AddNewCustomer
                            .class);
                    i.putExtra("customer", customer);
                    i.putExtra("position", index);
                    i.putExtra("title", title);
                    startActivityForResult(i, UPDATE_CUSTOMER_DETAILS_REQUEST_CODE);
                }
            });

            holder.dragDropButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() ==
                            MotionEvent.ACTION_DOWN) {
                        mStartDragListener.requestDrag(holder);
                    }
                    return false;
                }
            });

            holder.itemView.setActivated(selectedItems.get(position, customer.isSelected()));
            if (customer.isSelected()) {
                holder.itemView.setBackgroundColor(Color.GRAY);
            } else {
                holder.itemView.setBackgroundColor(Color.WHITE);
            }
            applyClickEvents(holder, position);

            holder.lastPaymentTextView.setVisibility(View.GONE);
            holder.lastPaymentTextView.setText("");
            if (customer.getLastPayment() != null) {
                holder.lastPaymentTextView.setVisibility(View.VISIBLE);
                String lastPaymentAmount = String.format("%.2f", customer.getLastPayment().getAmount());
                String lastPaymentDate = Utilities.getDateStringFromTimeStamp(customer.getLastPayment().getDate(), Utilities.DD_MM_YYYY);
                String lastPaymentTime = Utilities.getTimeFromTimeStamp(customer.getLastPayment().getDate());

                String interestString = "";
                if (customer.getLastPayment().getCollectBy().equalsIgnoreCase(DatabaseHelper.INTEREST)) {
                    interestString = "(Interest)";
                }
                holder.lastPaymentTextView.setText("Last Payment " + interestString + ": " + lastPaymentAmount + " on " + lastPaymentDate + " at " + lastPaymentTime);
            }
            /*if (customer.getEmi().size()>0) {
                holder.lastPaymentTextView.setVisibility(View.VISIBLE);
                String lastPaymentAmount = String.format("%.2f", customer.getLastPayment().getAmount());
                String lastPaymentDate = Utilities.getDateStringFromTimeStamp(customer.getLastPayment().getDate(), Utilities.DD_MM_YYYY);
                String lastPaymentTime = Utilities.getTimeFromTimeStamp(customer.getLastPayment().getDate());

                String interestString = "";
                if (customer.getLastPayment().getCollectBy().equalsIgnoreCase(DatabaseHelper.INTEREST)) {
                    interestString = "(Interest)";
                }
                holder.lastPaymentTextView.setText("Last Payment " + interestString + ": " + lastPaymentAmount + " on " + lastPaymentDate + " at " + lastPaymentTime);
            }*/

            holder.collectByTextView.setTextColor(MainActivity.this.getResources().getColor(android.R.color.black));
            if (customer.getCollectBy().equalsIgnoreCase(DatabaseHelper.INTEREST)) {
                holder.collectByTextView.setTextColor(MainActivity.this.getResources().getColor(android.R.color.holo_orange_dark));
            }

            holder.phone2TextView.setText(customer.getPhone2());
            holder.phone2TextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedCustomerBean = customer;
                    //confirmationAlert();
                    showContactsNumber();
                }
            });

            if ((customer.getPhone() == null || customer.getPhone().isEmpty()) && (customer.getPhone2() != null && !customer.getPhone2().isEmpty())){
                holder.phone2TextView.setVisibility(View.GONE);
                holder.phoneTextView.setText(customer.getPhone2());
                holder.phoneLayout.setVisibility(View.VISIBLE);
            }

            if (!contactListFiltered.get(position).isDataLoaded()) {
                new GetCustomerTodayOverAllCollections().execute(position);
            }
        }

        @Override
        public int getItemCount() {
            return contactListFiltered.size();
        }

        @Override
        public void onRowMoved(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(contactListFiltered, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(contactListFiltered, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onRowSelected(RecyclerView.ViewHolder myViewHolder) {
            myViewHolder.itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onRowClear(RecyclerView.ViewHolder myViewHolder) {
            myViewHolder.itemView.setBackgroundColor(Color.WHITE);
            adapter.notifyDataSetChanged();
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    String charString = charSequence.toString();
                    if (charString.isEmpty()) {
                        contactListFiltered = customerBeanList;
                    } else {
                        List<CustomerBean> filteredList = new ArrayList<>();
                        for (CustomerBean row : customerBeanList) {

                            // name match condition. this might differ depending on your requirement
                            // here we are looking for name or phone number match
                            if (row.getName().toLowerCase().contains(charString.toLowerCase()) || row.getPhone().contains(charSequence) || row.getCollectBy().toLowerCase().contains(charString.toLowerCase())
                                    || row.getPlace().toLowerCase().contains(charString.toLowerCase()) || String.valueOf(row.getAmount()).contains(charSequence) || String.valueOf(row.getReceived()).contains(charSequence)
                                    || String.valueOf(row.getBalance()).contains(charSequence)) {
                                filteredList.add(row);
                            }
                        }

                        contactListFiltered = filteredList;
                    }

                    adapter.isLongPressDragEnabled = true;
                    if (contactListFiltered.size() != customerBeanList.size()) {
                        adapter.isLongPressDragEnabled = false;
                    }
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = contactListFiltered;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    contactListFiltered = (ArrayList<CustomerBean>) filterResults.values;
                    checkEmptyState();
                    notifyDataSetChanged();
                    loadOverAllAmount();
                }
            };
        }

        public void filterByCollect(String charSequence) {
            if (charSequence.equalsIgnoreCase("All")) {
                contactListFiltered = customerBeanList;
            } else {
                List<CustomerBean> filteredList = new ArrayList<>();
                for (CustomerBean row : customerBeanList) {
                    if (row.getCollectBy().toLowerCase().contains(charSequence.toLowerCase())) {
                        filteredList.add(row);
                    }
                }
                contactListFiltered = filteredList;
            }

            adapter.isLongPressDragEnabled = true;
            if (contactListFiltered.size() != customerBeanList.size()) {
                adapter.isLongPressDragEnabled = false;
            }
            checkEmptyState();
            notifyDataSetChanged();
            loadOverAllAmount();
        }

        public void filterByCollect() {
            List<CustomerBean> filteredList = new ArrayList<>();
            for (int custPos=0;custPos<customerBeanList.size();custPos++) {
                CustomerBean row = customerBeanList.get(custPos);
                for (int i = 0; i < selectedCollectionBy.size(); i++) {
                    switch (selectedCollectionBy.get(i)) {
                        case 0: // All
                            filteredList = customerBeanList;
                            break;
                        case 1: // Daily
                            if (row.getCollectBy().equalsIgnoreCase("Daily")) {
                                filteredList.add(row);
                            }
                            break;
                        case 2: // Weekly
                            if (row.getCollectBy().equalsIgnoreCase("Weekly")) {
                                filteredList.add(row);
                            }
                            break;
                        case 3: // Monthly
                            if (row.getCollectBy().equalsIgnoreCase("Monthly")) {
                                filteredList.add(row);
                            }
                            break;
                        case 4: // Interest
                            if (row.getCollectBy().equalsIgnoreCase("Interest")) {
                                filteredList.add(row);
                            }
                            break;
                    }
                }
            }
            contactListFiltered = filteredList;
            adapter.isLongPressDragEnabled = true;
            if (contactListFiltered.size() != customerBeanList.size()) {
                adapter.isLongPressDragEnabled = false;
            }
            checkEmptyState();
            notifyDataSetChanged();
            loadOverAllAmount();
        }

        private void applyClickEvents(MainActivity.ViewHolder holder, final int position) {
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (actionMode != null) {
                        listener.onRowClicked(position);
                    } else {
                        Context c = view.getContext();
                        Intent i = new Intent(c, CustomerDetailsActivity.class);
                        i.putExtra("customer", contactListFiltered.get(position));
                        i.putExtra("position", position);
                        //startActivity(i);
                        startActivityForResult(i, UPDATE_CUSTOMER_DETAILS_REQUEST_CODE);
                    }
                }
            });
            holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //listener.onRowLongClicked(position);
                    //view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    return true;
                }
            });
        }

        public void toggleSelection(int pos) {
            currentSelectedIndex = pos;
            if (selectedItems.get(pos, false)) {
                selectedItems.delete(pos);
            } else {
                selectedItems.put(pos, true);
            }
            updateData(pos);
            notifyItemChanged(pos);
        }

        public void selectAll() {
            for (int i = 0; i < getItemCount(); i++) {
                selectedItems.put(i, true);
                selectAll(i);
            }
            notifyDataSetChanged();
        }


        public void clearSelections() {
            selectedItems.clear();
            notifyDataSetChanged();
        }

        public int getSelectedItemCount() {
            return selectedItems.size();
        }

        public List getSelectedItems() {
            List items = new ArrayList(selectedItems.size());
            for (int i = 0; i < selectedItems.size(); i++) {
                items.add(selectedItems.keyAt(i));
            }
            return items;
        }
        public List<CustomerBean> getSelectedCustomers() {
            List<CustomerBean> items = new ArrayList<>();
            for (int i = 0; i < contactListFiltered.size(); i++) {
                if (contactListFiltered.get(i).isSelected()) {
                    items.add(contactListFiltered.get(i));
                }
            }
            return items;
        }

        public void removeData(int position) {
            contactListFiltered.remove(position);
            resetCurrentIndex();
        }

        public void updateData(int position) {
            contactListFiltered.get(position).isSelected = !contactListFiltered.get(position).isSelected();
            resetCurrentIndex();
        }
        public void selectAll(int position) {
            contactListFiltered.get(position).isSelected = true;
            resetCurrentIndex();
        }
        public void deSelectAll() {
            for (int i=0;i<contactListFiltered.size();i++) {
                contactListFiltered.get(i).isSelected = false;
            }
            resetCurrentIndex();
        }

        private void resetCurrentIndex() {
            currentSelectedIndex = -1;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView phoneTextView;
        public LinearLayout phoneLayout;

        public TextView nameTextView, phone2TextView;
        public TextView placeTextView;
        public TextView amountTextView;
        public TextView createdOnTextView;
        public TextView receivedAmountTextView;
        public TextView balanceAmountTextView;
        public TextView completedTextView;
        public TextView collectByTextView;
        public TextView lastPaymentTextView;
        public ImageButton editCustomerButton, sendMessageButton, exportButton, insertCustomerButton, dragDropButton;
        public CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.phoneTextView = (TextView) itemView.findViewById(R.id.phone_textView);
            this.phoneLayout = (LinearLayout)itemView.findViewById(R.id.phone_layout);

            this.nameTextView = (TextView) itemView.findViewById(R.id.name_textView);
            this.placeTextView = (TextView) itemView.findViewById(R.id.place_textView);
            this.amountTextView = (TextView) itemView.findViewById(R.id.amount_textView);
            this.receivedAmountTextView = (TextView) itemView.findViewById(R.id.received_amount_textView);
            this.balanceAmountTextView = (TextView) itemView.findViewById(R.id.balance_amount_textView);
            this.collectByTextView = (TextView) itemView.findViewById(R.id.collect_by_textView);
            this.createdOnTextView = (TextView) itemView.findViewById(R.id.created_on_textView);
            this.completedTextView = (TextView) itemView.findViewById(R.id.completed_textView);
            this.phone2TextView = (TextView) itemView.findViewById(R.id.phone2_textView);

            this.editCustomerButton = (ImageButton) itemView.findViewById(R.id.edit_customer_button);
            this.sendMessageButton = (ImageButton) itemView.findViewById(R.id.send_message_button);
            this.exportButton = (ImageButton) itemView.findViewById(R.id.export_button);
            this.insertCustomerButton = (ImageButton) itemView.findViewById(R.id.insert_customer_button);
            this.dragDropButton = (ImageButton) itemView.findViewById(R.id.drag_drop_button);
            this.cardView = (CardView) itemView.findViewById(R.id.cardView);
            this.lastPaymentTextView = (TextView) itemView.findViewById(R.id.last_payment_textView);
        }
    }

    private class GetCustomerTodayOverAllCollections extends AsyncTask<Integer, Integer, Integer> {
        protected Integer doInBackground(Integer... position) {
            int pos = position[0];
            EmiDBManager emiDBManager = new EmiDBManager(MainActivity.this);
            emiDBManager.open();
            if (contactListFiltered.get(pos).getLastPaymentId() != null) {
                List<EMIBean> emiBeans = emiDBManager.getEmiById(contactListFiltered.get(pos).getLastPaymentId());
                if (emiBeans.size()>0) {
                    contactListFiltered.get(pos).setLastPayment(emiBeans.get(0));
                }
            } else {
                List<EMIBean> emiBeans = emiDBManager.getCustomerEmis(contactListFiltered.get(pos).getCustomerId());
                if (emiBeans.size()>0) {
                    contactListFiltered.get(pos).setLastPayment(emiBeans.get(0));
                }
            }

            contactListFiltered.get(pos).setTodayCollection(emiDBManager.getCurrentAmountByCurrentDateAndCustomer(contactListFiltered.get(pos).customerId));
            contactListFiltered.get(pos).setDataLoaded(true);
            int index = customerBeanList.indexOf(contactListFiltered.get(pos));
            if (index>=0) {
                customerBeanList.set(index, contactListFiltered.get(pos));
            }
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
            EmiDBManager emiDBManager = new EmiDBManager(MainActivity.this);
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

            double balanceAmount = 0.0;
            for (CustomerBean cust: contactListFiltered) {
                balanceAmount += cust.getBalance();
            }
            overAllBalanceAmountTextView.setText("Balance Amount : " + String.format("%.2f", balanceAmount));
        }
    }
}
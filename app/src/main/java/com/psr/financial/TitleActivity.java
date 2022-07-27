package com.psr.financial;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.psr.financial.Adapter.ItemMoveCallback;
import com.psr.financial.Adapter.StartDragListener;
import com.psr.financial.Database.DBManager;
import com.psr.financial.Database.DatabaseHelper;
import com.psr.financial.Database.EmiDBManager;
import com.psr.financial.Database.TitleDBManager;
import com.psr.financial.Models.EMIBean;
import com.psr.financial.Models.TitleModel;
import com.psr.financial.Utility.ExportDatabaseCSVTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.psr.financial.CustomerDetailsActivity.isStoragePermissionGranted;
import static com.psr.financial.MainActivity.MAKE_EXRERNAL_PERMISSION_REQUEST_CODE;
import static com.psr.financial.Utilities.showErrorMessage;

public class TitleActivity extends AppCompatActivity implements StartDragListener {

    static final int EXPORT_IMPORT_REQUEST_CODE = 10;

    public List<TitleModel> titles = new ArrayList<TitleModel>();

    RelativeLayout emptyLayout, bottomLayout;
    RecyclerView titleRecycleView;
    TitleListAdapter adapter;
    TextView overAllCollectionAmountTextView;

    private TitleDBManager titleDBManager;
    Session session;
    ItemTouchHelper touchHelper;
    private DBManager dbManager;

    static final String IMPORT = "import";
    static final String EXPORT = "export";
    static final String PACKAGE_NAME ="com.psr.financial";
    ExportDatabaseCSVTask exportDatabaseCSVTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.title_layout);
        session = Session.getInstance(this);

        emptyLayout = (RelativeLayout) findViewById(R.id.empty_layout);
        overAllCollectionAmountTextView = (TextView) findViewById(R.id.overAll_collection_amount_textView);
        bottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
        //bottomLayout.setVisibility(View.GONE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showTitleCreationAlert();
                showAlertDialogButtonClicked(null);
            }
        });

        titleRecycleView = (RecyclerView) findViewById(R.id.title_recycleView);
        adapter = new TitleListAdapter(this);
        titleRecycleView.setHasFixedSize(true);
        titleRecycleView.setLayoutManager(new LinearLayoutManager(this));
        ItemTouchHelper.Callback callback = new ItemMoveCallback(this, adapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(titleRecycleView);
        titleRecycleView.setAdapter(adapter);

        titleDBManager = new TitleDBManager(this);
        titleDBManager.open();
        dbManager = new DBManager(TitleActivity.this);
        dbManager.open();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        exportDatabaseCSVTask = new ExportDatabaseCSVTask(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadTitles();
        //checkLicense1();
    }

    public void reloadTitles() {
        titles = titleDBManager.getTitles();
        adapter.notifyDataSetChanged();
        checkEmptyState();
    }

    void checkEmptyState() {
        emptyLayout.setVisibility(View.VISIBLE);
        if (titles != null && titles.size()>0) {
            emptyLayout.setVisibility(View.GONE);
        }
        overAllCollectionAmountTextView.setText("Today Overall Collection : " + String.format("%.2f", 0.0));
        new GetTodayOverAllCollections().execute();
    }

    @Override
    public void requestDrag(RecyclerView.ViewHolder viewHolder) {
        touchHelper.startDrag(viewHolder);
    }

    private void checkLicense() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar now = Calendar.getInstance();

        if (!session.isExtended()) {
            session.setIsExtended(true);
            //session.setLoadAtFirstTime(true);
        }

        if (session.isLoadAtFirstTime()) {
            now.add(Calendar.DAY_OF_YEAR, 30);
            String formattedDate = sdf.format(now.getTime());
            session.setLoadAtFirstTime(false);
            session.setDateOfInstalled(formattedDate);
        } else {
            try {
                Date strDate = sdf.parse(session.getDateOfInstalled());
                if (System.currentTimeMillis() > strDate.getTime()) {
                    licenseExpiredDialog();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void checkLicense1() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            //Date strDate = sdf.parse(session.getDateOfInstalled());
            Date date = (Date)sdf.parse("30/12/2019");
            long output=date.getTime()/1000L;
            String str=Long.toString(output);
            long timestamp = Long.parseLong(str) * 1000;

            if (System.currentTimeMillis() > timestamp) {
                licenseExpiredDialog();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.import_export) {
            if (isStoragePermissionGranted(TitleActivity.this)) {
                new ExportImportDB(this).exportImportDBOption();
            }
            return true;
        }
//        else if (id == R.id.send_email) {
//            if (isStoragePermissionGranted(TitleActivity.this)) {
//                // new EmailHelper().sendEmail();
//            }
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    public void licenseExpiredDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Please contact your development team")
                .setCancelable(false);
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//
//                    }
//                });
        AlertDialog alert = builder.create();
        alert.setTitle("App has been expired");
        alert.show();
    }

    public void showAlertDialogButtonClicked(final TitleModel title) {

        // create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Title");

        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.custom_layout, null);
        builder.setView(customLayout);

        // add a button
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
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

        final EditText editText = customLayout.findViewById(R.id.editText);
        if (title != null) {
            editText.setText(title.getTitleName());
        }
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {
                Button b = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something
                        String YouEditTextValue = editText.getText().toString();
                        if (YouEditTextValue.trim().length()>0) {
                            if (title != null) {
                                updateTitle(title, YouEditTextValue);
                            } else {
                                insertTitle(YouEditTextValue);
                            }
                            dialog.dismiss();
                        } else {
                            showErrorMessage(editText, "Please enter your title");
                            //errorAlert(context, "Invalid mobile number","");
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    public void updateTitle(TitleModel titleModel, String title) {
        titleDBManager.updateTitle(titleModel, title);
        titles = titleDBManager.getTitles();
        adapter.notifyDataSetChanged();
    }

    public void insertTitle(String title) {
        Calendar currentInstance = Calendar.getInstance();
        //String currentId = String.valueOf(currentInstance.getTime());
        Long currentId = System.currentTimeMillis()/1000;
        TitleModel titleModel = new TitleModel();
        titleModel.setTitleName(title);
        titleModel.setCreatedOn(currentId.toString());
        titleModel.setIndex(titleDBManager.getTitles().size());
        titleDBManager.insertTitle(titleModel);
        //db.close();
        titles = titleDBManager.getTitles();
        adapter.notifyDataSetChanged();
        checkEmptyState();
    }

    class TitleListAdapter extends RecyclerView.Adapter<ViewHolder> implements ItemMoveCallback.ItemTouchHelperContract {

        StartDragListener mStartDragListener;

        public TitleListAdapter(StartDragListener mStartDragListener) {
            this.mStartDragListener = mStartDragListener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem= layoutInflater.inflate(R.layout.title_listview_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(listItem);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final TitleModel title = titles.get(position);
            holder.titleTextView.setText(title.getTitleName());

            holder.itemView.setTag(title);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context c = view.getContext();
                    Intent i = new Intent(c, MainActivity.class);
                    i.putExtra("title", title);
                    startActivity(i);
                }
            });

            holder.editTitleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAlertDialogButtonClicked(title);
                }
            });
            holder.deleteTitleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteTitleConfirmation("Delete Folder?","You can't retrieve once you delete. Are you sure want to delete this folder?", title);
                }
            });

            //new GetCustomerDetailsTask().execute(position);
            //holder.totalCustomerTextView.setText("Total No.of Customers :" + String.valueOf(title.getCustomerBeans().size()));
            holder.totalCustomerTextView.setText("Total No.of Customers :" + String.valueOf(title.getTotalNoOfCustomers()));
            holder.receivedPaymentTextView.setText(String.format("%.2f", title.getReceivedAmount()));
            holder.balancePaymentTextView.setText(String.format("%.2f", title.getBalanceAMount()));
        }

        @Override
        public int getItemCount() {
            return titles.size();
        }

        @Override
        public void onRowMoved(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(titles, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(titles, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onRowSelected(RecyclerView.ViewHolder myViewHolder) {
            myViewHolder.itemView.setBackgroundColor(Color.GRAY);

        }

        @Override
        public void onRowClear(RecyclerView.ViewHolder myViewHolder) {
            myViewHolder.itemView.setBackgroundColor(Color.WHITE);
        }

        public void deleteTitleConfirmation(String title, final String message, final TitleModel titleModel) {
            AlertDialog.Builder alert = new AlertDialog.Builder(TitleActivity.this);
            alert.setTitle(title);
            alert.setMessage(message);

            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    deleteTitle(titleModel);
                }
            });

            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            });

            alert.show();
        }

        void deleteTitle(TitleModel titleModel) {
            titleDBManager.deleteTitle(titleModel);
            titles = titleDBManager.getTitles();
            adapter.notifyDataSetChanged();
            checkEmptyState();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView, receivedPaymentTextView, totalCustomerTextView, balancePaymentTextView;
        public ImageButton editTitleButton, deleteTitleButton;
        public ViewHolder(View itemView) {
            super(itemView);
            this.titleTextView = (TextView) itemView.findViewById(R.id.title_textView);
            this.editTitleButton = (ImageButton) itemView.findViewById(R.id.edit_title_button);
            this.deleteTitleButton = (ImageButton) itemView.findViewById(R.id.delete_title_button);

            this.receivedPaymentTextView = (TextView) itemView.findViewById(R.id.received_payment_textView);
            this.totalCustomerTextView = (TextView) itemView.findViewById(R.id.total_customer_textView);
            this.balancePaymentTextView = (TextView) itemView.findViewById(R.id.balance_payment_textView);
        }
    }

    private void openFile() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        startActivityForResult(i, EXPORT_IMPORT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == EXPORT_IMPORT_REQUEST_CODE && resultCode  == RESULT_OK) {
            String fPath = data.getDataString();
            String filename = fPath.substring(fPath.lastIndexOf("/")+1);
            if (fPath.endsWith(".DB") && filename.endsWith(DatabaseHelper.DB_NAME)){
                //new ExportImportDB().exportDB();
                new ExportImportDB(this).importDBConfirmation();
            } else {
                Utilities.errorAlert(TitleActivity.this, "", "Please select valid Database");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case MAKE_EXRERNAL_PERMISSION_REQUEST_CODE :
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new ExportImportDB(this).exportImportDBOption();
                }
                break;
        }
    }
    public class EmailHelper {
        public void sendEmail() {
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault());
            String formattedDate = df.format(c);

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("text/plain");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"nramanit@gmail.com"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "PSR Finance " + formattedDate);
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Latest attached file. Date: " + formattedDate);
            File root = Environment.getDataDirectory();
            // String pathToMyAttachedFile = "Finance/" + DatabaseHelper.DB_NAME;
            // File file = new File(root, pathToMyAttachedFile);

            String currentDBPath= "//data//" + PACKAGE_NAME + "//databases//" + DatabaseHelper.DB_NAME;
            File file = new File(root, currentDBPath);
            if (!file.exists() || !file.canRead()) {
                return;
            }
            Uri uri = Uri.fromFile(file);
            emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"));
        }
    }

    public class ExportImportDB {

        Context context;

        public ExportImportDB(Context context) {
            this.context = context;
        }

        public void exportImportDBOption() {
            // setup the alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(TitleActivity.this);
            //builder.setTitle("Choose an animal");

            // add a radio button list
            final String[] collects = getResources().getStringArray(R.array.exportImportOption);
            builder.setItems(collects, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0: // View
                            exportOrImportDB(EXPORT);
                            break;
                        case 1: // Share
                            exportOrImportDB(IMPORT);
                            break;
                    }
                }
            });

            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        protected void exportOrImportDB(String dumpType) {
            if (exportDatabaseCSVTask.isFolderAlreadyCreated()) {
                if (dumpType.equalsIgnoreCase(EXPORT)) {
                    exportDB();
                } else if (dumpType.equalsIgnoreCase(IMPORT)) {
                    openFile();
                    //importDBConfirmation();
                }
            }
        }

        public void importDBConfirmation() {
            AlertDialog.Builder alert = new AlertDialog.Builder(TitleActivity.this);
            alert.setTitle("Importing Database");
            alert.setMessage("Would you like to import this new DB?. If you once imported, the old record will be replaced and can't retrieve.");

            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    importDB();
                }
            });

            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            });

            alert.show();
        }

        //importing database
        private void importDB() {
            try {
                // File sd = Environment.getExternalStorageDirectory();
                File sd = exportDatabaseCSVTask.directoryPath();
                // Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                File data = Environment.getDataDirectory();

                if (sd.canWrite()) {
                    String currentDBPath= "//data//" + PACKAGE_NAME + "//databases//" + DatabaseHelper.DB_NAME;
                    String backupDBPath  = File.separator + CustomerDetailsActivity.FOLDER_NAME + File.separator + DatabaseHelper.DB_NAME; //"/BackupFolder/DatabaseName";
                    File backupDB= new File(data, currentDBPath);
                    File currentDB  = new File(sd, backupDBPath);

                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getBaseContext(), backupDB.toString(), Toast.LENGTH_LONG).show();
                    reloadTitles();
                }
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
            }
        }

        //exporting database
        private void exportDB() {
            try {
//                File sd = null; // Environment.getExternalStorageDirectory();
                File sd = exportDatabaseCSVTask.directoryPath();
                // Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                File data = Environment.getDataDirectory();

                if (sd.canWrite()) {
                    String currentDBPath = "//data//" + PACKAGE_NAME + "//databases//" + DatabaseHelper.DB_NAME;
                    String backupDBPath = File.separator + CustomerDetailsActivity.FOLDER_NAME + File.separator + DatabaseHelper.DB_NAME;
                    File currentDB = new File(data, currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(getBaseContext(), backupDB.toString(), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private class GetTodayOverAllCollections extends AsyncTask<Void, Integer, Double> {
        protected Double doInBackground(Void... index) {
            double amount = 0.0;
            EmiDBManager emiDBManager = new EmiDBManager(TitleActivity.this);
            emiDBManager.open();
            List<EMIBean> emis = emiDBManager.getEmisByCurrentDate();
            for (int i=0;i<emis.size();i++) {
                amount += emis.get(i).getAmount();
            }
            emiDBManager.close();
            return amount;
        }

        protected void onPostExecute(Double amount) {
            if (titles.size()>0) {
                bottomLayout.setVisibility(View.VISIBLE);
            }
            overAllCollectionAmountTextView.setText("Today Overall Collection : " + String.format("%.2f", amount));
        }
    }
}
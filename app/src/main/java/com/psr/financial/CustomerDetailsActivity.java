package com.psr.financial;

import android.Manifest;
import android.app.Activity;
//import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.android.material.textfield.TextInputEditText;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v4.content.FileProvider;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.psr.financial.Database.DBManager;
import com.psr.financial.Database.DatabaseHelper;
import com.psr.financial.Database.EmiDBManager;
import com.psr.financial.Models.CustomerBean;
import com.psr.financial.Models.EMIBean;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
//itext libraries to write PDF file
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.psr.financial.Utility.ExportDatabaseCSVTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static com.psr.financial.MainActivity.MAKE_CALL_PERMISSION_REQUEST_CODE;
import static com.psr.financial.MainActivity.MAKE_EXRERNAL_PERMISSION_REQUEST_CODE;
import static com.psr.financial.MainActivity.UPDATE_CUSTOMER_DETAILS_REQUEST_CODE;
import static com.psr.financial.Utilities.showErrorMessage;


public class CustomerDetailsActivity extends AppCompatActivity {

    final String Digits     = "(\\p{Digit}+)";
    final String HexDigits  = "(\\p{XDigit}+)";
    // an exponent is 'e' or 'E' followed by an optionally
// signed decimal integer.
    final String Exp        = "[eE][+-]?"+Digits;
    final String fpRegex    =
            ("[\\x00-\\x20]*"+ // Optional leading "whitespace"
                    "[+-]?(" +         // Optional sign character
                    "NaN|" +           // "NaN" string
                    "Infinity|" +      // "Infinity" string

                    // A decimal floating-point string representing a finite positive
                    // number without a leading sign has at most five basic pieces:
                    // Digits . Digits ExponentPart FloatTypeSuffix
                    //
                    // Since this method allows integer-only strings as input
                    // in addition to strings of floating-point literals, the
                    // two sub-patterns below are simplifications of the grammar
                    // productions from the Java Language Specification, 2nd
                    // edition, section 3.10.2.

                    // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
                    "((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+

                    // . Digits ExponentPart_opt FloatTypeSuffix_opt
                    "(\\.("+Digits+")("+Exp+")?)|"+

                    // Hexadecimal strings
                    "((" +
                    // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
                    "(0[xX]" + HexDigits + "(\\.)?)|" +

                    // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
                    "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

                    ")[pP][+-]?" + Digits + "))" +
                    "[fFdD]?))" +
                    "[\\x00-\\x20]*");// Optional trailing "whitespace"

    public static final String FOLDER_NAME = "PSR-Finance";
    TextView nameTextView, placeTextView, phoneTextView, amountTextView, receivedAmountTextView, balanceAmountTextView, createdOnTextView, collectByTextView, lastPaymentTextView, completedTextView;
    TextInputEditText amountEditText, dateEditText, timeEditText;
    Button saveButton, newButton, updateButton, previousButton, nextButton;
    LinearLayout phoneLayout, saveEmiLayout;
    RelativeLayout topLayout, moveCustomerLayout;
    ImageButton calenderButton;
    RadioGroup collectionByRadioGroup;
    RadioButton regularRadioButton, interestRadioButton;

    String selectedCollectionBy;
    CustomerBean customer;
    EMIBean emiBean = new EMIBean();

    Calendar myCalendar = Calendar.getInstance();
    DBManager dbManager;
    String currentTime;
    int position = 0;
    List<CustomerBean> customerBeanLists = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_details_layout);

        dbManager = new DBManager(this);
        dbManager.open();

        moveCustomerLayout = (RelativeLayout) findViewById(R.id.move_customer_layout);
        previousButton = (Button) findViewById(R.id.previous_button);
        nextButton = (Button) findViewById(R.id.next_button);

        topLayout = (RelativeLayout) findViewById(R.id.top_layout);
        phoneLayout = (LinearLayout) findViewById(R.id.phone_layout);
        nameTextView = (TextView) findViewById(R.id.name_textView);
        placeTextView = (TextView) findViewById(R.id.place_textView);
        phoneTextView = (TextView) findViewById(R.id.phone_textView);
        amountTextView = (TextView) findViewById(R.id.amount_textView);
        receivedAmountTextView = (TextView) findViewById(R.id.received_amount_textView);
        balanceAmountTextView = (TextView) findViewById(R.id.balance_amount_textView);
        createdOnTextView = (TextView)findViewById(R.id.created_on_textView);
        collectByTextView = (TextView)findViewById(R.id.collect_by_textView);
        lastPaymentTextView = (TextView)findViewById(R.id.last_payment_textView);
        completedTextView = (TextView)findViewById(R.id.completed_textView);

        collectionByRadioGroup = (RadioGroup) findViewById(R.id.collection_by_radioGroup);
        regularRadioButton = (RadioButton) findViewById(R.id.regular_radioButton);
        interestRadioButton = (RadioButton) findViewById(R.id.interest_radioButton);
        amountEditText = (TextInputEditText)findViewById(R.id.amount_editText);
        dateEditText = (TextInputEditText)findViewById(R.id.date_editText);
        calenderButton = findViewById(R.id.calender_button);
        timeEditText = (TextInputEditText)findViewById(R.id.time_editText);
        saveButton = (Button) findViewById(R.id.save_emi_button);

        saveEmiLayout = (LinearLayout) findViewById(R.id.save_emi_layout);
        updateButton = (Button) findViewById(R.id.update_emi_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveEMI();
            }
        });
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateEMI();
            }
        });

        newButton = (Button) findViewById(R.id.new_emi_button);
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetValues();
            }
        });

        customer = (CustomerBean) getIntent().getSerializableExtra("customer");
        if (getIntent().getExtras().containsKey("position")) {
            position =  getIntent().getExtras().getInt("position");
        }
        customerBeanLists = dbManager.getCustomers(customer.getTitleId());
        loadCustomerDetails();

        phoneLayout.setClickable(true);
        phoneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmationAlert();
            }
        });

        setDatePicker();
        setTimePicker();

        checkButtonAvailability();
        updateButton.setVisibility(View.GONE);
        if (getIntent().getExtras().containsKey("emiBean")) {
            moveCustomerLayout.setVisibility(View.GONE);
            this.setTitle("Edit report");
            emiBean = (EMIBean) getIntent().getSerializableExtra("emiBean");
            topLayout.setVisibility(View.GONE);
            saveEmiLayout.setVisibility(View.GONE);
            updateButton.setVisibility(View.VISIBLE);

            amountEditText.setText(String.format("%.2f", emiBean.getAmount()));
            amountEditText.setText(String.format("%.2f", emiBean.getAmount()));
            dateEditText.setText(Utilities.getDateStringFromTimeStamp(emiBean.getDate(), Utilities.DD_MM_YYYY)); //"dd/MM/yyyy"
            timeEditText.setText(Utilities.getTimeFromTimeStamp(emiBean.getDate()));

            myCalendar.setTimeInMillis(Long.valueOf(emiBean.getDate()));
            currentTime = Utilities.getDateStringFromTimeStamp(emiBean.getDate(), Utilities.HH_MM_A_SEC);
        }

        //new DateTextWatcher(dateEditText);
        collectionByRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.regular_radioButton:
                        selectedCollectionBy = DatabaseHelper.REGULAR;
                        break;
                    case R.id.interest_radioButton:
                        selectedCollectionBy = DatabaseHelper.INTEREST;
                        break;
                }
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                position -= 1;

                String amount = amountEditText.getText().toString();
                if (!amount.isEmpty()) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(CustomerDetailsActivity.this);
                    alert.setTitle("Save amount?");
                    alert.setMessage("Would you like to save this amount?. If not, please select No");

                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            saveEMI();
                            moveItem();
                        }
                    });

                    alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            moveItem();
                        }
                    });

                    alert.show();
                } else {
                    moveItem();
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                position += 1;

                String amount = amountEditText.getText().toString();
                if (!amount.isEmpty()) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(CustomerDetailsActivity.this);
                    alert.setTitle("Save amount?");
                    alert.setMessage("Would you like to save this amount?. If not, please select No");

                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            saveEMI();
                            moveItem();
                        }
                    });

                    alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            moveItem();
                        }
                    });

                    alert.show();
                } else {
                    moveItem();
                }
            }
        });
    }

    public void moveItem() {
        resetValues();
        customer = customerBeanLists.get(position);
        loadCustomerDetails();
        checkButtonAvailability();
    }

    public void checkButtonAvailability() {
        previousButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
        if (position == 0) {
            previousButton.setVisibility(View.GONE);
        }
        if (customerBeanLists.size()-1 == position) {
            nextButton.setVisibility(View.GONE);
        }
        if (customerBeanLists.size() == 1) {
            moveCustomerLayout.setVisibility(View.GONE);
        }

        Intent intent=new Intent();
        intent.putExtra("moveToPos", position);
        setResult(RESULT_OK, intent);
    }

    public void loadCustomerDetails() {
        lastPaymentTextView.setVisibility(View.GONE);
        completedTextView.setVisibility(View.GONE);
        collectionByRadioGroup.setVisibility(View.GONE);
        selectedCollectionBy = DatabaseHelper.REGULAR;
        regularRadioButton.setChecked(true);
        if (customer != null) {
            nameTextView.setText(customer.getName());
            placeTextView.setText(customer.getPlace());
            phoneTextView.setText(customer.getPhone());
            amountTextView.setText(String.format("%.2f", customer.getAmount()));
            receivedAmountTextView.setText("Received : " + String.format("%.2f", customer.getReceived()));
            balanceAmountTextView.setText("Balance : " + String.format("%.2f", customer.getBalance()));

            collectByTextView.setText(customer.getCollectBy());
            createdOnTextView.setText(Utilities.getDateStringFromTimeStamp(String.valueOf(customer.getCreatedOn()), null));

            phoneLayout.setVisibility(View.GONE);
            if (customer.getPhone() != null && !customer.getPhone().isEmpty()) {
                phoneLayout.setVisibility(View.VISIBLE);
                phoneTextView.setText(customer.getPhone());
            }

            if (customer.getCollectBy().equalsIgnoreCase(DatabaseHelper.INTEREST)) {
                selectedCollectionBy = DatabaseHelper.INTEREST;
                interestRadioButton.setChecked(true);
                collectionByRadioGroup.setVisibility(View.VISIBLE);
            }

            if (customer.getLastPayment() != null) {
                lastPaymentTextView.setVisibility(View.VISIBLE);
                String lastPaymentAmount = String.format("%.2f", customer.getLastPayment().getAmount());
                String lastPaymentDate = Utilities.getDateStringFromTimeStamp(customer.getLastPayment().getDate(), Utilities.DD_MM_YYYY);
                String lastPaymentTime = Utilities.getTimeFromTimeStamp(customer.getLastPayment().getDate());

                String interestString = "";
                if (customer.getLastPayment().getCollectBy().equalsIgnoreCase(DatabaseHelper.INTEREST)) {
                    interestString = "(Interest)";
                }
                lastPaymentTextView.setText("Last Payment " + interestString + ": " + lastPaymentAmount + " on " + lastPaymentDate + " at " + lastPaymentTime);
            }

            if (customer.getReceived() >= customer.getAmount()) {
                completedTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!(getIntent().getExtras().containsKey("emiBean"))) {
            getMenuInflater().inflate(R.menu.cusomer_details_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
//                if (!CustomerDetailsActivity.this.getTitle().equals("Edit report")) {
//                    Intent intent=new Intent();
//                    intent.putExtra("moveToPos", position);
//                    setResult(RESULT_OK, intent);
//                }
                finish();
                return true;
            case R.id.edit_button:
                Intent i = new Intent(CustomerDetailsActivity.this, AddNewCustomer.class);
                i.putExtra("isEdit", true);
                i.putExtra("customer", customer);
                startActivityForResult(i, UPDATE_CUSTOMER_DETAILS_REQUEST_CODE);
                return true;
            case R.id.delete_button:
                deleteCustomerConfirmation("Delete Customer", "You can't retrieve once you delete. Are you sure want to delete this customer?.");
                return true;
            case R.id.share_button:
                if (isStoragePermissionGranted(CustomerDetailsActivity.this)) {
                    ExportDatabaseCSVTask exportDatabaseCSVTask = new ExportDatabaseCSVTask(this);
                    exportDatabaseCSVTask.createExcelSheet(customer);
                }
                return true;
            case R.id.edit_report_button:
                if (customer.getLastPayment() != null) {
                    Intent intent = new Intent(CustomerDetailsActivity.this, CustomerReportActivity.class);
                    intent.putExtra("customer", customer);
                    startActivityForResult(intent, UPDATE_CUSTOMER_DETAILS_REQUEST_CODE);
                } else {
                    Utilities.errorAlert(this, "No Report", "No reports available to edit");
                }
                return true;
            case R.id.message_button:
                Utilities.sendMessage(CustomerDetailsActivity.this, customer);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteCustomerConfirmation(String title, final String message) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(title);
        alert.setMessage(message);

        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                deleteCustomer();
            }
        });

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    public void deleteCustomer() {
        dbManager.deleteCustomer(customer);
        Intent intent=new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    void setDatePicker() {
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDate();
            }
        };

        calenderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                DatePickerDialog datePicker = new DatePickerDialog(CustomerDetailsActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                //datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
                datePicker.show();
            }
        });
        updateDate();
    }

    private void updateDate() {
        String myFormat = Utilities.DD_MM_YYYY; //"dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        dateEditText.setText(sdf.format(myCalendar.getTime()));
    }

    public void setTimePicker() {

        final TimePickerDialog.OnTimeSetListener onStartTimeListener = new TimePickerDialog.OnTimeSetListener() {

            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                //String AM_PM;
                //int am_pm;
                //timeEditText.setText(hourOfDay + " : " + minute + "  " + AM_PM);
                myCalendar.set(Calendar.HOUR, hourOfDay);
                myCalendar.set(Calendar.MINUTE, minute);
                updateTime(hourOfDay, minute);
            }
        };

        timeEditText.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                new TimePickerDialog(CustomerDetailsActivity.this, onStartTimeListener, myCalendar
                        .get(Calendar.HOUR), myCalendar.get(Calendar.MINUTE), false).show();

            }
        });
        updateTime();
    }

    private void updateTime() {
        int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = myCalendar.get(Calendar.MINUTE);
        updateTime(hour, minute);
    }

    private void updateTime(int hour, int minutes) {
//        String myFormat = "HH:mm aaa"; //In which you need put here
//        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
//        timeEditText.setText(sdf.format(myCalendar.getTime()));

        String timeSet = "";
        if (hour > 12) {
            hour -= 12;
            timeSet = "PM";
        } else if (hour == 0) {
            hour += 12;
            timeSet = "AM";
        } else if (hour == 12){
            timeSet = "PM";
        }else{
            timeSet = "AM";
        }

        String min = "";
        if (minutes < 10)
            min = "0" + minutes ;
        else
            min = String.valueOf(minutes);

        // Append in a StringBuilder
        String aTime = new StringBuilder().append(hour).append(':').append(min).append(" ").append(timeSet).toString();
        currentTime = new StringBuilder().append(hour).append(':').append(min).append(':').append(myCalendar.SECOND).append(':')
                .append(myCalendar.MILLISECOND).append(" ").append(timeSet).toString();

        timeEditText.setText(aTime);
    }

    public void resetValues() {
        myCalendar = Calendar.getInstance();
        amountEditText.setText("");
        updateDate();
        updateTime();
    }

    public void saveEMI() {
        String amount = amountEditText.getText().toString();
        String date = dateEditText.getText().toString();
        String time = timeEditText.getText().toString();

        if (amount.isEmpty()) {
            showErrorMessage(amountEditText, "Please enter amount");
            return;
        } else if (!Utilities.checkDateFormat(date)) {
            showErrorMessage(dateEditText, "Please enter valid date");
            return;
        }

        EmiDBManager emiDBManager = new EmiDBManager(this);
        emiDBManager.open();

        /*customer.setEmi(emiDBManager.getCustomerEmis(customer.getCustomerId()));
        double receivedAmount = 0;
        for (int i=0;i<customer.emi.size();i++) {
            if (!customer.emi.get(i).getCollectBy().equalsIgnoreCase(DatabaseHelper.INTEREST)) {
                receivedAmount = receivedAmount + customer.emi.get(i).getAmount();
            }
        }*/

        double receivedAmount = customer.getReceived();
        if (!selectedCollectionBy.equalsIgnoreCase(DatabaseHelper.INTEREST)) {
            receivedAmount = receivedAmount + Double.valueOf(amount);
        }

        // Load New data for EMI
        Long currentId = System.currentTimeMillis()/1000;
        String dateString = date + ", " + time;
        if (Utilities.convertDateStringToTimeStamp(dateString)>0) {
            dateString = String.valueOf(Utilities.convertDateStringToTimeStamp(dateString));
        }

        customer.setReceived(receivedAmount);
        customer.setBalance(customer.amount - receivedAmount);

        emiDBManager.insertEmi(customer, new EMIBean(currentId.toString(), dateString, Double.valueOf(amount), selectedCollectionBy));

        customer.setEmi(emiDBManager.getCustomerEmis(customer.getCustomerId()));
        customer.setLastPaymentId(customer.getEmi().get(0).getEmiId());
        dbManager.updateCustomerAmount(customer);

        receivedAmountTextView.setText("Received : " + String.format("%.2f", customer.getReceived()));
        balanceAmountTextView.setText("Balance : " + String.format("%.2f", customer.getBalance()));

        //List<EMIBean> emi = emiDBManager.getEmiById(currentId.toString());
        emiDBManager.close();
        customer.setLastPayment(null);
        if (customer.getEmi().size()>0) {
            customer.setLastPayment(customer.getEmi().get(0));
        }

        resetValues();
        loadCustomerDetails();

//        Intent intent=new Intent();
//        intent.putExtra("moveToPos", position);
//        setResult(RESULT_OK, intent);
    }

    public void updateEMI() {
        String amount = amountEditText.getText().toString();
        String date = dateEditText.getText().toString();
        String time = timeEditText.getText().toString();

        if (amount.isEmpty()) {
            showErrorMessage(amountEditText, "Please enter amount");
            return;
        } else if (!Utilities.checkDateFormat(date)) {
            showErrorMessage(dateEditText, "Please enter valid date");
            return;
        }

        String dateString = date + ", " + time;
        if (Utilities.convertDateStringToTimeStamp(dateString)>0) {
            dateString = String.valueOf(Utilities.convertDateStringToTimeStamp(dateString));
        }

        double receivedAmount = 0;
        for (int i=0;i<customer.emi.size();i++) {
            if (customer.emi.get(i).getEmiId().equalsIgnoreCase(emiBean.getEmiId())) {
                customer.emi.get(i).setAmount(Double.valueOf(amount));
                customer.emi.get(i).setCollectBy(selectedCollectionBy);
            }

            if (!customer.emi.get(i).getCollectBy().equalsIgnoreCase(DatabaseHelper.INTEREST)) {
                receivedAmount = receivedAmount + Double.valueOf(customer.emi.get(i).getAmount());
            }
        }

        EmiDBManager emiDBManager = new EmiDBManager(this);
        emiDBManager.open();

        customer.setReceived(receivedAmount);
        customer.setBalance(customer.amount - receivedAmount);

        emiDBManager.updateEmi(new EMIBean(emiBean.getEmiId(), dateString, Double.valueOf(amount), selectedCollectionBy));

        customer.setEmi(emiDBManager.getCustomerEmis(customer.getCustomerId()));
        customer.setLastPaymentId(customer.getEmi().get(0).getEmiId());
        dbManager.updateCustomerAmount(customer);

        emiDBManager.close();
        if (customer.getEmi().size()>0) {
            customer.setLastPayment(customer.getEmi().get(0));
        }

        Intent intent=new Intent();
        intent.putExtra("customer", customer);
        setResult(RESULT_OK, intent);
        finish();
    }

    static public boolean isStoragePermissionGranted(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MAKE_EXRERNAL_PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case MAKE_CALL_PERMISSION_REQUEST_CODE :
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "You can call the number by clicking on the button", Toast.LENGTH_SHORT).show();
                    checkAndMakePhoneCall();
                }
                break;
            case MAKE_EXRERNAL_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
                    //resume tasks needing this permission
                    ExportDatabaseCSVTask exportDatabaseCSVTask = new ExportDatabaseCSVTask(this);
                    exportDatabaseCSVTask.createExcelSheet(customer);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == UPDATE_CUSTOMER_DETAILS_REQUEST_CODE && resultCode  == RESULT_OK) {
            CustomerBean customer = (CustomerBean) data.getSerializableExtra("customer");
            if (customer != null){
                this.customer = customer;
                loadCustomerDetails();

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
            }
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
                        finish();
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
        alert.setTitle("Call Confirmation");
        alert.show();
    }

    public void checkAndMakePhoneCall() {
        if (checkPermission(Manifest.permission.CALL_PHONE)) {
            try {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + customer.getPhone()));
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

    public static Map<String, List<EMIBean>> getEMiByDate(CustomerBean customerBean) {

        Map<String, List<EMIBean>> dateByEmiBean = new LinkedHashMap<>();
        for (EMIBean emiBeansTemp: customerBean.getEmi()) {
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

        return dateByEmiBean;
    }
}
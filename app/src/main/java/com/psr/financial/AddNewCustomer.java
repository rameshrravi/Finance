package com.psr.financial;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
//import com.google.android.material.textfield.TextInputEditText;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.psr.financial.Database.DBManager;
import com.psr.financial.Models.CustomerBean;
import com.psr.financial.Models.TitleModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.psr.financial.Utilities.showErrorMessage;

public class AddNewCustomer extends AppCompatActivity {

    TextInputEditText nameEditText, phoneEditText, placeEditText, amountEditText, createdOn, phone2EditText;
    Button saveButton;
    Spinner collectBySpinner;
    ImageButton calenderButton, importPhone1Button, importPhone2Button, addMorePhone1Button, deletePhone2Button;
    RelativeLayout phone2Layout;

    private DBManager dbManager;
    CustomerBean customerBean = new CustomerBean();
    public boolean isEdit = false;
    final Calendar myCalendar = Calendar.getInstance();
    TitleModel title;
    int position = -1;

    public static final int MAX_FORMAT_LENGTH = 8;
    public static final int MIN_FORMAT_LENGTH = 3;
    String updatedText;
    private boolean editing;
    //static final int PICK_CONTACT=1;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    boolean isPhone2 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_customer_layout);

        if (getIntent().hasExtra("isEdit")) {
            isEdit = getIntent().getExtras().getBoolean("isEdit");
            customerBean = (CustomerBean) getIntent().getSerializableExtra("customer");
            setTitle("Update Customer");
        }
        if (getIntent().getExtras().containsKey("title")) {
            title = (TitleModel) getIntent().getSerializableExtra("title");
        }
        if (getIntent().getExtras().containsKey("position")) {
            position =  getIntent().getExtras().getInt("position");
            customerBean = (CustomerBean) getIntent().getSerializableExtra("customer");
        }

        dbManager = new DBManager(this);
        dbManager.open();

        nameEditText = (TextInputEditText)findViewById(R.id.name_editText);
        phoneEditText = (TextInputEditText)findViewById(R.id.phone_editText);
        placeEditText = (TextInputEditText)findViewById(R.id.place_editText);
        amountEditText = (TextInputEditText)findViewById(R.id.amount_editText);
        collectBySpinner = (Spinner) findViewById(R.id.collect_by_spinner);
        saveButton = (Button) findViewById(R.id.save_new_customer_button);
        createdOn= (TextInputEditText) findViewById(R.id.created_on);
        calenderButton = findViewById(R.id.calender_button);

        phone2Layout = (RelativeLayout) findViewById(R.id.phone2_layout);
        importPhone1Button = (ImageButton) findViewById(R.id.import_phone1_button);
        importPhone2Button = (ImageButton) findViewById(R.id.import_phone2_button);
        addMorePhone1Button = (ImageButton) findViewById(R.id.add_more_phone1_button);
        deletePhone2Button = (ImageButton) findViewById(R.id.delete_phone2_button);
        phone2EditText= (TextInputEditText) findViewById(R.id.phone2_editText);

        phone2Layout.setVisibility(View.GONE);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNewCustomer();
            }
        });

        ArrayAdapter<CharSequence> daysSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.collectBy, android.R.layout.simple_spinner_item);
        daysSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        collectBySpinner.setAdapter(daysSpinnerAdapter);
        collectBySpinner.setSelection(0);

        setDatePicker();
        if (isEdit) {
            loadCustomerDetails();
        }

        //new DateTextWatcher(createdOn);
        //createdOn.addTextChangedListener(new DateTextWatcher());
        addMorePhone1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone2Layout.setVisibility(View.VISIBLE);
            }
        });
        deletePhone2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone2EditText.setText("");
                phone2Layout.setVisibility(View.GONE);
            }
        });

        importPhone1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPhone2 = false;
                showContacts();
            }
        });
        importPhone2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPhone2 = true;
                showContacts();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (PERMISSIONS_REQUEST_READ_CONTACTS) :
                if (resultCode == Activity.RESULT_OK) {

                    Uri contactData = data.getData();
                    Cursor c =  managedQuery(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        String id =c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                        String hasPhone =c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id, //+ " = ?",
                                    null, null); //new String[]{id}

                            ArrayList<String> phoneNum = new ArrayList<String>();
                            while (phones.moveToNext()) {
                                // store the numbers in an array
                                phoneNum.add(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                            }
                            Set<String> aSet = new HashSet<String>(phoneNum);
                            phoneNum.clear();
                            phoneNum.addAll(aSet);

                            if(phoneNum.size() > 1) {
                                /*int i=0;
                                String[] phoneNum = new String[phones.getCount()];
                                while (phones.moveToNext()) {
                                    // store the numbers in an array
                                    phoneNum[i] = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    i++;
                                }*/
                                // list the phoneNum array (perhaps using radiobuttons) & give the choice to select one number
                                showContactsNumber(phoneNum);
                            } else if (phoneNum.size() == 1){
                                //phones.moveToFirst();

                                String cNumber = phoneNum.get(0); //phones.getString(phones.getColumnIndex("data1"));
                                cNumber = cNumber.replaceAll(" ", "");
                                if (cNumber.contains("+91")) {
                                    cNumber = cNumber.replaceAll(" ", "").trim().substring(3);
                                }
                                System.out.println("number is: "+ cNumber);

                                if (!isPhone2) {
                                    phoneEditText.setText(cNumber);
                                } else {
                                    phone2EditText.setText(cNumber);
                                }
                            }
                        }
                        //String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    }
                }
                break;
        }
    }

    private void showContactsNumber(final List<String> phoneNum) {

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
                        loadPhone(phoneNum.get(0));
                        break;
                    case 1:
                        loadPhone(phoneNum.get(1));
                        break;
                    case 2:
                        loadPhone(phoneNum.get(2));
                        break;
                }
            }
        });

        builder.show();
    }

    private void loadPhone(String cNumber) {
        //String cNumber = phoneNum[selectedIndex];
        cNumber = cNumber.replaceAll(" ", "");
        if (cNumber.contains("+91")) {
            cNumber = cNumber.replaceAll(" ", "").trim().substring(3);
        }

        if (!isPhone2) {
            phoneEditText.setText(cNumber);
        } else {
            phone2EditText.setText(cNumber);
        }
    }
    private void showContacts() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                showContacts();
            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void loadCustomerDetails() {
        nameEditText.setText(customerBean.getName());
        phoneEditText.setText(customerBean.getPhone());
        if (customerBean.getPhone2() != null && !customerBean.getPhone2().isEmpty()) {
            phone2EditText.setText(customerBean.getPhone2());
            phone2Layout.setVisibility(View.VISIBLE);
        }
        placeEditText.setText(customerBean.getPlace());
        amountEditText.setText(String.format("%.2f", customerBean.getAmount()));

        createdOn.setText(Utilities.getDateStringFromTimeStamp(customerBean.getCreatedOn(), null));
        //createdOn.setText(customerBean.getCreatedOn());
        myCalendar.setTimeInMillis(Long.valueOf(customerBean.getCreatedOn()));

        collectBySpinner.setSelection(0);
        if (customerBean.getCollectBy() != null) {
            String[] array = getResources().getStringArray(R.array.collectBy);
            for (int i = 0; i < array.length; i++) {
                if (array[i].equalsIgnoreCase(customerBean.getCollectBy())) {
                    collectBySpinner.setSelection(i);
                    break;
                }
            }
        }
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
                updateLabel();
            }
        };

        calenderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                DatePickerDialog datePicker = new DatePickerDialog(AddNewCustomer.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                //datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
                datePicker.show();
            }
        });
        updateLabel();
    }

    private void updateLabel() {
        String myFormat = Utilities.DD_MM_YYYY; //"dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        createdOn.setText(sdf.format(myCalendar.getTime()));
    }

    void saveNewCustomer() {
        String name = nameEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        String place = placeEditText.getText().toString();
        String amount = amountEditText.getText().toString();
        String created = createdOn.getText().toString();
        String collectBy = collectBySpinner.getSelectedItem().toString();

        String phone2 = phone2EditText.getText().toString();

        //Matcher matcher = Pattern.compile(Utilities.DATE_PATTERN).matcher(created);
        if (name.isEmpty()) {
            showErrorMessage(nameEditText, "Please enter name");
            return;
        } else if (!phone.isEmpty() && phone.length()<10) {
            showErrorMessage(phoneEditText, "Please enter valid phone");
            return;
        } else if (!phone2.isEmpty() && phone2.length()<10) {
            showErrorMessage(phone2EditText, "Please enter valid phone");
            return;
        } else if (place.isEmpty()) {
            showErrorMessage(placeEditText, "Please enter place");
            return;
        } else if (amount.isEmpty()) {
            showErrorMessage(amountEditText, "Please enter amount");
            return;
        } else if (collectBy.equalsIgnoreCase("Collect By")) {
            //"Please select the day"
            MainActivity.showAlertDialog(this,"Please select how you collect the amount");
            return;
        } else if (!Utilities.checkDateFormat(created)) {
            showErrorMessage(createdOn, "Please enter valid date");
            return;
        }

        long createdLong = Utilities.convertDateStringToTimeStamp(created + ", 00:00 AM");
        CustomerBean customerBeanTemp = new CustomerBean();
        customerBeanTemp.setName(name);
        customerBeanTemp.setPhone(phone);
        customerBeanTemp.setPhone2(phone2);
        customerBeanTemp.setPlace(place);
        customerBeanTemp.setAmount(Double.parseDouble(amount));
        customerBeanTemp.setCreatedOn(String.valueOf(createdLong));
        customerBeanTemp.setCollectBy(collectBy);

        if (isEdit) {
            double receivedDouble = 0;
            if (customerBean.getReceived()>0) {
                receivedDouble = customerBean.getReceived();
            }
            customerBeanTemp.setReceived(receivedDouble);
            customerBeanTemp.setBalance(Double.parseDouble(amount) - receivedDouble);
            customerBeanTemp.setCustomerId(customerBean.getCustomerId());
            dbManager.updateCustomer(customerBeanTemp);
        } else {
            customerBeanTemp.setBalance(Double.parseDouble(amount));
            int size = dbManager.getCustomers(title.getTitleId()).size();
            Long currentId = System.currentTimeMillis()/1000;
            customerBeanTemp.setCustomerId(currentId.toString());
            if (position>=0 && (position+1)<size) {
                int index = position; //customerBean.getIndex();
                dbManager.updateIndexByInsertingMiddle(customerBean, index+1); //position+1
                customerBeanTemp.setIndex(index+1);
                dbManager.insertAtPosition(title.getTitleId(), customerBeanTemp, index+1);
            } else {
                customerBeanTemp.setIndex(size);
                dbManager.insert(title.getTitleId(), customerBeanTemp);
            }
        }

        dbManager.close();

        Intent intent=new Intent();
        intent.putExtra("customer", customerBeanTemp);
        setResult(RESULT_OK, intent);
        finish();
    }
}

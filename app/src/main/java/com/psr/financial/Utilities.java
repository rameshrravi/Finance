package com.psr.financial;

import android.app.Activity;
//import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
//import com.google.android.material.textfield.TextInputEditText;
//import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;
import com.psr.financial.Database.DBManager;
import com.psr.financial.Database.EmiDBManager;
import com.psr.financial.Models.CustomerBean;
import com.psr.financial.Models.EMIBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {

    public static final String DD_MM_YYYY_TIME = "dd/MM/yyyy, hh:mm a";
    public static final String DD_MM_YYYY_HH_MM_A_SEC = "dd/MM/yyyy, hh:mm:ss.SSS a";
    public static final String DD_MM_YYYY = "dd/MM/yyyy";
    public static final String HH_MM_A = "hh:mm a";
    public static final String HH_MM_A_SEC = "hh:mm:ss.SSS a";

    static public String convertTimeStampToString(String timeStamp) {
        if (timeStamp == null || timeStamp.isEmpty()) {
            return null;
        }
        String myFormat = DD_MM_YYYY_TIME; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        Date date = new Date(Long.parseLong(timeStamp));
        return sdf.format(date);
        //return timeStamp;
    }

    static public long convertDateStringToTimeStamp(String dateString) {
        try {
            String myFormat = Utilities.DD_MM_YYYY_TIME; // "dd/MM/yyyy, hh:mm a"; //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            Date parsedDate = (Date) sdf.parse(dateString);
            return parsedDate.getTime();
        } catch (Exception e) {
            return 0;
        }
    }

    static public String getDateStringFromTimeStamp(String timeStamp, String format) {
        if (timeStamp == null || timeStamp.isEmpty()) {
            return null;
        }
        String myFormat = DD_MM_YYYY; //In which you need put here
        if (format != null) {
            myFormat = format;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        Date date = new Date(Long.parseLong(timeStamp));
        return sdf.format(date);
    }

    static public String getTimeFromTimeStamp(String timeStamp) {
        if (timeStamp == null || timeStamp.isEmpty()) {
            return null;
        }
        String myFormat = HH_MM_A; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        Date date = new Date(Long.parseLong(timeStamp));
        return sdf.format(date);
    }

    static public Date getDateFromTimeStamp(String timeStamp, String format) {
        if (timeStamp == null || timeStamp.isEmpty()) {
            return null;
        }
        String myFormat = DD_MM_YYYY; //In which you need put here
        if (format != null) {
            myFormat = format;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            //Date date = new Date(Long.parseLong(timeStamp));
            return sdf.parse(getDateStringFromTimeStamp(timeStamp, format));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static public void showErrorMessage(TextInputEditText errorField, String errorMessage) {
        errorField.setError(errorMessage);
        errorField.requestFocus();
    }

    static public void showErrorMessage(EditText errorField, String errorMessage) {
        errorField.setError(errorMessage);
    }

    public static void sendMessage(Context context, CustomerBean customerBean) {

        if (customerBean.getPhone() != null && !customerBean.getPhone().isEmpty()) {
            String message = "Total Amount : " + customerBean.getAmount() + "\n"
                    + "Paid Amount : " + customerBean.getReceived() + "\n"
                    + "Balance Amount : " + customerBean.getBalance() + "\n";


            EmiDBManager emiDBManager = new EmiDBManager(context);
            emiDBManager.open();
            if (customerBean.getLastPaymentId() != null) {
                List<EMIBean> emiBeans = emiDBManager.getEmiById(customerBean.getLastPaymentId());
                if (emiBeans.size()>0) {
                    message = "Last Payment : " + emiBeans.get(0).getAmount() + " on " + Utilities.convertTimeStampToString(emiBeans.get(0).getDate()) + "\n"
                            + "Total Amount : " + customerBean.getAmount() + "\n"
                            + "Paid Amount : " + customerBean.getReceived() + "\n"
                            + "Balance Amount : " + customerBean.getBalance() + "\n";
                }
            }
            sendSMS(context, customerBean, message);
        } else {
            collectContactNumberAlert(context, customerBean);
        }
    }

    public static void sendSMS(Context context, CustomerBean customerBean, String msg) {
        try {
//            SmsManager smsManager = SmsManager.getDefault();
//            smsManager.sendTextMessage(customerBean.getPhone(), null, msg, null, null);
//            Toast.makeText(context, "Message Sent",
//                    Toast.LENGTH_LONG).show();

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + customerBean.getPhone()));
            intent.putExtra("sms_body", msg);
            context.startActivity(intent);
        } catch (Exception ex) {
            Toast.makeText(context,ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    public static void collectContactNumberAlert(final Context context, final CustomerBean customerBean) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("No Contact number");
        builder.setMessage("Please enter contact number to send message");

        // set the custom layout
        final View customLayout = ((Activity)context).getLayoutInflater().inflate(R.layout.custom_layout, null);
        builder.setView(customLayout);

        final EditText editText = customLayout.findViewById(R.id.editText);
        editText.setHint("Mobile Number");
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(10);
        editText.setFilters(filterArray);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        Dialog myDialog = builder.create();
        myDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {
                Button b = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String YouEditTextValue = editText.getText().toString();
                        if (YouEditTextValue.length()==10) {
                            contactNumberUpdateConfirmation(context, "Update contact", "Would you like update this contact number for future?", customerBean, YouEditTextValue);
                            dialog.dismiss();
                        } else {
                            showErrorMessage(editText, "Invalid mobile number");
                            //errorAlert(context, "Invalid mobile number","");
                        }
                    }
                });
            }
        });
        myDialog.show();
    }

    public static void contactNumberUpdateConfirmation(final Context context, String title, final String message, final CustomerBean customerBean, final String contactNumber) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(title);
        alert.setMessage(message);

        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                customerBean.setPhone(contactNumber);
                DBManager dbManager = new DBManager(context);
                dbManager.open();
                dbManager.updateCustomer(customerBean);
                dbManager.close();
                sendMessage(context, customerBean);
                dialog.dismiss();
            }
        });

        alert.setNegativeButton("No, Send message only", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                customerBean.setPhone(contactNumber);
                sendMessage(context, customerBean);
                dialog.dismiss();
            }
        });

        alert.show();
    }

    public static void errorAlert(Context context, String title, String message) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(title);
        alert.setMessage(message);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    public static boolean isValidDate(String strDate)
    {
        /* Check if date is 'null' */
        if (strDate.trim().equals(""))
        {
            return true;
        }
        /* Date is not 'null' */
        else
        {
            /*
             * Set preferred date format,
             * For example MM-dd-yyyy, MM.dd.yyyy,dd.MM.yyyy etc.*/
            SimpleDateFormat sdfrmt = new SimpleDateFormat("dd/MM/yyyy");
            sdfrmt.setLenient(false);
            /* Create Date object
             * parse the string into date
             */
            try
            {
                Date javaDate = sdfrmt.parse(strDate);
                System.out.println(strDate+" is valid date format");
            }
            /* Date format is invalid */
            catch (ParseException e)
            {
                System.out.println(strDate+" is Invalid Date format");
                return false;
            }
            /* Return true if date format is valid */
            return true;
        }
    }

    public static final String DATE_PATTERN =
            "(0?[1-9]|1[012]) [/.-] (0?[1-9]|[12][0-9]|3[01]) [/.-] ((19|20)\\d\\d)";

    private Pattern pattern;
    private Matcher matcher;

    public boolean validate(final String date) {

        matcher = pattern.matcher(date);

        if(matcher.matches()){
            matcher.reset();

            if(matcher.find()){
                String day = matcher.group(1);
                String month = matcher.group(2);
                int year = Integer.parseInt(matcher.group(3));

                if (day.equals("31") &&
                        (month.equals("4") || month .equals("6") || month.equals("9") ||
                                month.equals("11") || month.equals("04") || month .equals("06") ||
                                month.equals("09"))) {
                    return false; // only 1,3,5,7,8,10,12 has 31 days
                }

                else if (month.equals("2") || month.equals("02")) {
                    //leap year
                    if(year % 4==0){
                        if(day.equals("30") || day.equals("31")){
                            return false;
                        }
                        else{
                            return true;
                        }
                    }
                    else{
                        if(day.equals("29")||day.equals("30")||day.equals("31")){
                            return false;
                        }
                        else{
                            return true;
                        }
                    }
                } else {
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean checkDateFormat(String value) {
        /*if (date == null || !date.matches("^(1[0-9]|0[1-9]|3[0-1]|2[1-9])/(0[1-9]|1[0-2])/[0-9]{4}$"))
            return false;
        SimpleDateFormat format=new SimpleDateFormat("dd/MM/yyyy");
        try {
            format.parse(date);
            return true;
        }catch (ParseException e){
            return false;
        }*/

        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Utilities.DD_MM_YYYY);
            date = sdf.parse(value);
            if (!value.equals(sdf.format(date))) {
                date = null;
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return date != null;
    }
}

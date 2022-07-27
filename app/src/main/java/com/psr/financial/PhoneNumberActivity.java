package com.psr.financial;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PhoneNumberActivity extends AppCompatActivity {

    String TAG = "PhoneNumberActivity";
    public static final int TELEPHONY_SERVICE_REQUEST_CODE = 10;
    String wantPermission = Manifest.permission.READ_PHONE_STATE;
    String smsPermission = Manifest.permission.RECEIVE_SMS;

    EditText phoneNumberEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_number_layout);

        phoneNumberEditText = (EditText) findViewById(R.id.phone_number_editText);

        Button sendCodeButton = (Button) findViewById(R.id.send_code_button);
        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = phoneNumberEditText.getText().toString().trim();
                if (number.length()==10) {
                    sendVerificationCode(number);
                } else {
                    Utilities.showErrorMessage(phoneNumberEditText, "Please enter valid number");
                }
            }
        });

        if (!checkPermission(wantPermission)) {
            requestPermission(wantPermission);
        } else {
            loadPhoneNumber();
        }
    }

    private void loadPhoneNumber() {
        String phNumber = getPhone();
        Log.d(TAG, "Phone number: " + phNumber);
        if (phNumber.length()>0) {
            phoneNumberEditText.setText(phNumber);
        }
    }

    private String getPhone() {
        TelephonyManager phoneMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, wantPermission) != PackageManager.PERMISSION_GRANTED) {
            return "";
        }
        String s = phoneMgr.getLine1Number();
        return s != null && s.length() >= 12 ? s.substring(2) : s;
    }

    private void requestPermission(String permission){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)){
            Toast.makeText(this, "Phone state permission allows us to get phone number. Please allow it for additional functionality.", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{permission, smsPermission}, TELEPHONY_SERVICE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case TELEPHONY_SERVICE_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Log.d(TAG, "Phone number: " + getPhone());
                    loadPhoneNumber();
                } else {
                    Toast.makeText(this,"Permission Denied. We can't get phone number.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private boolean checkPermission(String permission){
        if (Build.VERSION.SDK_INT >= 23) {
            int result = ContextCompat.checkSelfPermission(this, permission);
            if (result == PackageManager.PERMISSION_GRANTED){
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public void sendVerificationCode(String mobile) {
        Intent i = new Intent(this, OtpVerificationActivity.class);
        i.putExtra("mobile", mobile);
        startActivity(i);
    }
}
package com.psr.financial;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.psr.financial.Utility.SmsListener;
import com.psr.financial.Utility.SmsReceiver;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class OtpVerificationActivity extends AppCompatActivity {

    /** Alias for our key in the Android Key Store. */
    private static final String KEY_NAME = "my_key";
    private static final byte[] SECRET_BYTE_ARRAY = new byte[] {1, 2, 3, 4, 5, 6};

    private static final int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 1;

    /**
     * If the user has unlocked the device Within the last this number of seconds,
     * it can be considered as an authenticator.
     */
    private static final int AUTHENTICATION_DURATION_SECONDS = 30;

    private KeyguardManager mKeyguardManager;

    private String mVerificationId;
    //The edittext to input the code
    private EditText otpEditText;
    //firebase auth object
    private FirebaseAuth mAuth;

    String verificationCode;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    Session session;

    boolean isTimerRunning = true;
    boolean isScreenLockEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_number_otp_verification_layout);

        mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (!mKeyguardManager.isKeyguardSecure()) {
            // Show a message that the user hasn't set up a lock screen.
            Toast.makeText(this, "Secure lock screen hasn't set up.\n" + "Go to 'Settings -> Security -> Screenlock' to set up a lock screen",
                    Toast.LENGTH_LONG).show();
            isScreenLockEnabled = false;
        }
        createKey();

        session = Session.getInstance(this);

        TextView phoneNumberTextView = (TextView) findViewById(R.id.phone_number_textView);
        otpEditText = (EditText) findViewById(R.id.otp_editText);

        mAuth = FirebaseAuth.getInstance();

        //getting mobile number from the previous activity
        //and sending the verification code to the number
        Intent intent = getIntent();
        String mobile = intent.getStringExtra("mobile");
        sendVerificationCode(mobile);

        phoneNumberTextView.setText("+91"+mobile);

        TextView resendOtpButton = (TextView) findViewById(R.id.resend_button);
        resendOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mResendToken != null && !isTimerRunning) {
                    resendVerificationCode(mobile);
                }
            }
        });

        Button confirmOtpButton = (Button) findViewById(R.id.confirm_otp_button);
        confirmOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyCode(otpEditText.getText().toString().trim());
                /*if (verificationCode != null && verificationCode.length()==6) {
                    verifyVerificationCode(verificationCode);
                }*/
            }
        });

        TextView changeNumberButton = (TextView) findViewById(R.id.change_number_button);
        changeNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        new CountDownTimer(150000, 1000) { // adjust the milli seconds here

            public void onTick(long millisUntilFinished) {
                isTimerRunning = true;
                resendOtpButton.setText("Resend code ("+String.format("0%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes( millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))) + ")");
            }

            public void onFinish() {
                isTimerRunning = false;
                resendOtpButton.setText("Resend code");
            }
        }.start();

        smsReceiver();
    }

    private void sendVerificationCode(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

    private void resendVerificationCode(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks,
                mResendToken);
    }

    private void smsReceiver() {
        SmsReceiver.bindListener(new SmsListener() {
            @Override
            public void messageReceived(String messageText) {
                //otpEditText.setText(messageText);
                verifyCode(otpEditText.getText().toString().trim());
            }
        });
    }

    private void verifyCode(String code) {
        if (code == null || code.isEmpty()) {
            return;
        }
        verificationCode = code;
        otpEditText.setText(code);

        //verifying the code
        if (verificationCode.length()==6) {
            verifyVerificationCode(verificationCode);
        }
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            //Getting the code sent by SMS
            String code = phoneAuthCredential.getSmsCode();

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            if (code != null) {
                // verifyCode(code)
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(OtpVerificationActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            mVerificationId = s;
            mResendToken = forceResendingToken;
        }
    };

    private void verifyVerificationCode(String otp) {
        //creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);

        //signing the user
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(OtpVerificationActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        //verification successful we will start the profile activity
                        session.setIsPhoneVerified(true);
                        tryEncrypt();
                    } else {

                        //verification unsuccessful.. display an error message

                        String message = "Somthing is wrong, we will fix it soon...";

                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            message = "Invalid code entered...";
                        }

                        Snackbar snackbar = Snackbar.make(findViewById(R.id.parent), message, Snackbar.LENGTH_LONG);
                        snackbar.setAction("Dismiss", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                        snackbar.show();
                    }
                }
            });
    }

    /**
     * Tries to encrypt some data with the generated key in {@link #createKey} which
     * only works if the user has just authenticated via device credentials.
     */
    private boolean tryEncrypt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isScreenLockEnabled) {
            try {
                KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                keyStore.load(null);
                SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_NAME, null);
                Cipher cipher = Cipher.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/"
                                + KeyProperties.ENCRYPTION_PADDING_PKCS7);

                // Try encrypting something, it will only work if the user authenticated within
                // the last AUTHENTICATION_DURATION_SECONDS seconds.
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                cipher.doFinal(SECRET_BYTE_ARRAY);

                // If the user has recently authenticated, you will reach here.
                showAlreadyAuthenticated();
                return true;
            } catch (UserNotAuthenticatedException e) {
                // User is not authenticated, let's authenticate with device credentials.
                showAuthenticationScreen();
                return false;
            } catch (KeyPermanentlyInvalidatedException e) {
                // This happens if the lock screen has been disabled or reset after the key was
                // generated after the key was generated.
                Toast.makeText(this, "Keys are invalidated after created. Retry the purchase\n"
                                + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                return false;
            } catch (BadPaddingException | IllegalBlockSizeException | KeyStoreException |
                    CertificateException | UnrecoverableKeyException | IOException
                    | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
                //throw new RuntimeException(e);
            }
        } else {
            openTitleActivity();
        }
        return false;
    }

    /**
     * Creates a symmetric key in the Android Key Store which can only be used after the user has
     * authenticated with device credentials within the last X seconds.
     */
    private void createKey() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Generate a key to decrypt payment credentials, tokens, etc.
            // This will most likely be a registration step for the user when they are setting up your app.
            try {
                KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                keyStore.load(null);
                KeyGenerator keyGenerator = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

                // Set the alias of the entry in Android KeyStore where the key will appear
                // and the constrains (purposes) in the constructor of the Builder
                keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setUserAuthenticationRequired(true)
                        // Require that the user has unlocked in the last 30 seconds
                        .setUserAuthenticationValidityDurationSeconds(AUTHENTICATION_DURATION_SECONDS)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build());
                keyGenerator.generateKey();
            } catch (NoSuchAlgorithmException | NoSuchProviderException
                    | InvalidAlgorithmParameterException | KeyStoreException
                    | CertificateException | IOException e) {
                throw new RuntimeException("Failed to create a symmetric key", e);
            }
        }
    }

    private void showAuthenticationScreen() {
        // Create the Confirm Credentials screen. You can customize the title and description. Or
        // we will provide a generic one for you if you leave it null
        Intent intent = mKeyguardManager.createConfirmDeviceCredentialIntent(null, null);
        if (intent != null) {
            startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
            // Challenge completed, proceed with using cipher
            if (resultCode == RESULT_OK) {
                //if (tryEncrypt()) {
                    openTitleActivity();
                //}
            } else {
                // The user canceled or didn’t complete the lock screen
                // operation. Go to error/cancellation flow.
                //Toast.makeText(this, "Invalid login", Toast.LENGTH_SHORT).show();
                //tryEncrypt();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        }
    }

    private void openTitleActivity() {
        Intent intent = new Intent(this, TitleActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showAlreadyAuthenticated() {
        openTitleActivity();
        /*TextView textView = (TextView) findViewById(R.id.already_has_valid_device_credential_message);
        textView.setVisibility(View.VISIBLE);
        textView.setText(getResources().getQuantityString(R.plurals.already_confirmed_device_credentials_within_last_x_seconds, AUTHENTICATION_DURATION_SECONDS, AUTHENTICATION_DURATION_SECONDS));
        findViewById(R.id.purchase_button).setEnabled(false);*/
    }
}

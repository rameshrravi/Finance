package com.psr.financial;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 1000;

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
    Session session;
    boolean isScreenLockEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity_layout);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (!mKeyguardManager.isKeyguardSecure()) {
            // Show a message that the user hasn't set up a lock screen.
            Toast.makeText(this, "Secure lock screen hasn't set up.\n" + "Go to 'Settings -> Security -> Screenlock' to set up a lock screen",
                    Toast.LENGTH_LONG).show();
            isScreenLockEnabled = false;
        }
        createKey();

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                session = Session.getInstance(SplashActivity.this);
//                if (session.isPhoneVerified()) {
//                    tryEncrypt();
//                } else {
//                    openPhoneNumberActivity();
//                }
                openTitleActivity();
                // close this activity
                // finish();
            }
        }, SPLASH_TIME_OUT);

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
                //showAlreadyAuthenticated();
                openTitleActivity();
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
                finish();
            }
        }
    }

    private void openPhoneNumberActivity() {
        // Start your app main activity
        Intent i = new Intent(SplashActivity.this, PhoneNumberActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
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

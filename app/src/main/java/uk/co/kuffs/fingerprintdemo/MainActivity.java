package uk.co.kuffs.fingerprintdemo;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import uk.co.kuffs.fingerprint.Api;
import uk.co.kuffs.fingerprint.AuthenticationDialog;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity implements Api.Callback {

    public static String alias = "default_alias_key";

    TextView txtEncrypted;
    TextView txtDecrypted;
    Button btnDecrypt;
    Button btnEncrypt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtEncrypted = findViewById(R.id.txtEncrypted);
        txtDecrypted = findViewById(R.id.txtDecrypted);
        btnEncrypt = findViewById(R.id.btnEncrypt);
        btnDecrypt = findViewById(R.id.btnDecrypt);

        btnEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnEncryptClick();
            }
        });

        btnDecrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnShowDialog();
            }
        });

    }

    private void btnEncryptClick() {

        btnEncrypt.setVisibility(View.GONE);
        txtEncrypted.setText("");
        txtDecrypted.setText("");

        try {
            String encrypted = Api.getApi(this, alias).encryptString("Fingerprint was decoded correctly");

            txtEncrypted.setText(encrypted);
            btnDecrypt.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void btnShowDialog() {
        String value = txtEncrypted.getText().toString();
        btnDecrypt.setVisibility(View.GONE);
        AuthenticationDialog.showDialog(this, alias, value);

    }

    private void btnDecryptClick() {
        Api api = Api.getApi(this, alias);

        btnDecrypt.setVisibility(View.GONE);

        String value = txtEncrypted.getText().toString();

        txtEncrypted.setText("Touch Sensor");

        api.startListening(value, new Api.DecryptedListener() {
            @Override
            public void onDecrypted(String value) {
                txtEncrypted.setText("");
                txtDecrypted.setText(value);
                btnEncrypt.setVisibility(View.VISIBLE);
            }

            @Override
            public void onDecryptError(String error) {
                txtEncrypted.setText("");
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                btnEncrypt.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAuthenticationFailed() {

            }

            @Override
            public void onKeyInvalidated() {

            }
        });


    }

    @Override
    public void onAuthenticated(String decryptedValue) {
        txtEncrypted.setText("");
        txtDecrypted.setText(decryptedValue);
        btnEncrypt.setVisibility(View.VISIBLE);
    }

    @Override
    public void onError(String message) {
        txtEncrypted.setText("");
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        btnEncrypt.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAuthenticationFailed() {

    }

    @Override
    public void onKeyInvalidated() {

    }
}

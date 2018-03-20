package uk.co.kuffs.fingerprint;

import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

@RequiresApi(api = Build.VERSION_CODES.M)
public class Api {

    private FingerprintManager _fingerprintManager;
    private KeyStore _keyStore;
    private CancellationSignal _cancellationSignal;
    private boolean _cancelled;
    private Cipher _cipher;
    private String _alias;
    private Context c;

    private Api() {
    }

    public static Api getApi(Context c, String alias) {

        Api api = new Api();
        api._alias = alias;
        api.c = c;

        try {
            api._keyStore = KeyStore.getInstance("AndroidKeyStore");
            api._keyStore.load(null);
        } catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
            throw new RuntimeException("Failed to get an instance of KeyStore", e);
        }

        try {
            api._cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get an instance of Cipher", e);
        }

        KeyguardManager keyguardManager = c.getSystemService(KeyguardManager.class);
        api._fingerprintManager = c.getSystemService(FingerprintManager.class);

        if (!keyguardManager.isKeyguardSecure()) {
            throw new RuntimeException("Secure lock screen hasn't been set up.\nGo to 'Settings -> Security -> Fingerprint' to set up a fingerprint");
        }

        try {
            if (!api.keyExists()) {
                api.createKey();
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return api;
    }

    public static void clearApi(Context c, String alias) {

        Api api = new Api();
        api._alias = alias;
        api.c = c;

        try {
            api._keyStore = KeyStore.getInstance("AndroidKeyStore");
            api._keyStore.load(null);
        } catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
            throw new RuntimeException("Failed to get an instance of KeyStore", e);
        }

        try {
            api._cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get an instance of Cipher", e);
        }

        KeyguardManager keyguardManager = c.getSystemService(KeyguardManager.class);
        api._fingerprintManager = c.getSystemService(FingerprintManager.class);

        if (!keyguardManager.isKeyguardSecure()) {
            throw new RuntimeException("Secure lock screen hasn't been set up.\nGo to 'Settings -> Security -> Fingerprint' to set up a fingerprint");
        }

        try {
            if (api.keyExists()) {
                api._keyStore.deleteEntry(alias);
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public boolean isFingerprintHardwareDetected() {
        return _fingerprintManager.isHardwareDetected();
    }

    public boolean hasEnrolledFingerprints() {
        return _fingerprintManager.hasEnrolledFingerprints();
    }


    void startListening(final String toDecrypt, final DecryptedListener listener, final AuthenticationDialog dialog) {
        try {
            _cipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
        } catch (KeyPermanentlyInvalidatedException e) {

            listener.onKeyInvalidated();
            return;
        } catch (Exception e) {
            listener.onDecryptError(e.getMessage());
            return;
        }

        FingerprintManager.CryptoObject crypt = new FingerprintManager.CryptoObject(_cipher);

        FingerprintManager.AuthenticationCallback callback = new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                dialog.animate(AuthenticationDialog.STATE_ERROR);
                dialog.setMessage(errString.toString(), null);
                dialog.delayDismiss(1000);
                listener.onDecryptError(errString.toString());
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                super.onAuthenticationHelp(helpCode, helpString);
                dialog.animate(AuthenticationDialog.STATE_ERROR);
                dialog.setMessage(helpString.toString(), c.getString(R.string.touchSensor));
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                dialog.animate(AuthenticationDialog.STATE_OK);

                try {
                    if (toDecrypt != null) {
                        String xx = decryptString(toDecrypt);
                        listener.onDecrypted(xx);
                    } else {
                        listener.onDecrypted(null);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onDecryptError(e.getMessage());
                }

            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                dialog.animate(AuthenticationDialog.STATE_ERROR);
                listener.onAuthenticationFailed();
            }
        };

        _cancellationSignal = new CancellationSignal();
        _fingerprintManager.authenticate(crypt, _cancellationSignal, 0, callback, null);
    }

    public void startListening(final String toDecrypt, final DecryptedListener listener) {
        try {
            _cipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
        } catch (KeyPermanentlyInvalidatedException e) {
            listener.onKeyInvalidated();
            return;
        } catch (Exception e) {
            listener.onDecryptError(e.getMessage());
            return;
        }

        FingerprintManager.CryptoObject crypt = new FingerprintManager.CryptoObject(_cipher);

        FingerprintManager.AuthenticationCallback callback = new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                listener.onDecryptError(errString.toString());
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                super.onAuthenticationHelp(helpCode, helpString);
                listener.onDecryptError(helpString.toString());
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                try {
                    if (toDecrypt != null) {
                        String xx = decryptString(toDecrypt);
                        listener.onDecrypted(xx);
                    } else {
                        listener.onDecrypted(null);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onDecryptError(e.getMessage());
                }

            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                listener.onAuthenticationFailed();
            }
        };

        _cancellationSignal = new CancellationSignal();
        _fingerprintManager.authenticate(crypt, _cancellationSignal, 0, callback, null);
    }

    public boolean isListening() {
        return _cancellationSignal != null;
    }

    public void stopListening() {
        if (_cancellationSignal != null) {
            _cancelled = true;
            _cancellationSignal.cancel();
            _cancellationSignal = null;
        }
    }


    private void createKey() {

        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");

            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(_alias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                    .setUserAuthenticationRequired(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(true);
            }

            kpg.initialize(builder.build());
            kpg.generateKeyPair();

        } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Failed to get an instance of KeyGenerator", e);
        }

    }

    private void removeKey() throws KeyStoreException {
        _keyStore.deleteEntry(_alias);
    }

    public String encryptString(String toEncrypt) throws IOException, InvalidKeyException, InvalidAlgorithmParameterException, KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException {

        // Workaround for bug in Android 6
        PublicKey key = getPublicKey();
        PublicKey unrestricted = KeyFactory.getInstance(key.getAlgorithm()).generatePublic(new X509EncodedKeySpec(key.getEncoded()));

        OAEPParameterSpec spec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);
        _cipher.init(Cipher.ENCRYPT_MODE, unrestricted, spec);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, _cipher);
        cipherOutputStream.write(toEncrypt.getBytes("UTF-8"));
        cipherOutputStream.close();

        String enc = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP);

        return enc;
    }

    private String decryptString(String toDecrypt) throws IOException {

        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.decode(toDecrypt, Base64.NO_WRAP));
        CipherInputStream cipherInputStream = new CipherInputStream(inputStream, _cipher);

        ArrayList<Byte> values = new ArrayList<>();
        int nextByte;
        while ((nextByte = cipherInputStream.read()) != -1) {
            values.add((byte) nextByte);
        }

        byte[] bytes = new byte[values.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = values.get(i);
        }

        return new String(bytes, 0, bytes.length, "UTF-8");
    }

    private boolean keyExists() throws KeyStoreException {
        return _keyStore.containsAlias(_alias);
    }

    private PrivateKey getPrivateKey() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        return (PrivateKey) _keyStore.getKey(_alias, null);
    }

    private PublicKey getPublicKey() throws KeyStoreException {
        return _keyStore.getCertificate(_alias).getPublicKey();
    }

    public interface Callback {

        void onAuthenticated(String decryptedValue);

        void onError(String message);

        void onAuthenticationFailed();

        void onKeyInvalidated();

    }

    public interface DecryptedListener {
        void onDecrypted(String value);

        void onDecryptError(String error);

        void onAuthenticationFailed();

        void onKeyInvalidated();

    }

    public void resetKey() {
        try {
            _keyStore.deleteEntry(_alias);
            createKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

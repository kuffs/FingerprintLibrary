# FingerprintLibrary

Add jitpack in project gradle

    allprojects {
        repositories {
            jcenter()
            mavenCentral()
            maven { url "https://jitpack.io" }
        }
    }

then import library in module gradle.

    implementation 'com.github.kuffs:FingerprintLibrary:0.1.1'
    
Check fingerprint (No UI)

    Api api=Api.getApi(context, alias); 
    
    api.startListening(value, new Api.DecryptedListener() {
                @Override
                public void onDecrypted(String value) {

                }
    
                @Override
                public void onDecryptError(String error) {

                }
    
                @Override
                public void onAuthenticationFailed() {
   
                }
    
                @Override
                public void onKeyInvalidated() {
    
                }
            });
          
encrypt a string value

    String encrypted = api.encryptString("Encrypt me");
            
            
Check fingerprint (With UI)
    
     AuthenticationDialog.showDialog(context, alias, valuetoDecrypt);
     
and implement Api.Callback in the calling Activity.

    @Override
    public void onAuthenticated(String decryptedValue) {

    }

    @Override
    public void onError(String message) {
    }

    @Override
    public void onAuthenticationFailed() {
    }

    @Override
    public void onKeyInvalidated() {
    }


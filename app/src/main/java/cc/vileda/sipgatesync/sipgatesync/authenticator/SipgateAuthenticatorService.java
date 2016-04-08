package cc.vileda.sipgatesync.sipgatesync.authenticator;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SipgateAuthenticatorService extends Service {
    public SipgateAuthenticatorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        SipgateAuthenticator authenticator = new SipgateAuthenticator(this);
        return authenticator.getIBinder();
    }
}

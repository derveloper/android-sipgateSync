package cc.vileda.sipgatesync.sipgatesync.contacts;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SipgateContactSyncService extends Service {
    private static SipgateContactSyncAdapter sSyncAdapter = null;
    private static final Object sSyncAdapterLock = new Object();

    public SipgateContactSyncService() {
    }

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SipgateContactSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}

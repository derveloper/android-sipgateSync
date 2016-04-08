package cc.vileda.sipgatesync.sipgatesync.contacts;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ContactSyncService extends Service {
    private static ContactSyncAdapter sSyncAdapter = null;
    private static final Object sSyncAdapterLock = new Object();

    public ContactSyncService() {
    }

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new ContactSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}

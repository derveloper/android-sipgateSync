package cc.vileda.sipgatesync.sipgatesync.contacts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.vileda.sipgatesync.sipgatesync.api.SipgateApi;

/**
 * Created by vileda on 08.04.16.
 */
public class ContactSyncAdapter extends AbstractThreadedSyncAdapter {
    final ContentResolver mContentResolver;

    public ContactSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    public ContactSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d("ContactSyncAdapter", "onPerformSync()");
        AccountManager accountManager = AccountManager.get(getContext());
        final String jwt = accountManager.peekAuthToken(account, "JWT");
        Log.d("ContactSyncAdapter", jwt);
        final JSONArray users = SipgateApi.getUsers(jwt);
        assert users != null;

        Uri rawContactUri = ContactsContract.RawContacts.CONTENT_URI
                .buildUpon()
                .appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_NAME, account.name)
                .appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type)
                .build();
        Cursor c1 = mContentResolver.query(rawContactUri, null, null, null, null);
        Map<String, Boolean> localContacts = new HashMap<>();
        while (c1.moveToNext()) {
            localContacts.put(c1.getString(c1.getColumnIndex(ContactsContract.RawContacts.SOURCE_ID)), false);
        }

        for (int i = 0; i < users.length(); i++) {
            try {
                final JSONObject user = users.getJSONObject(i);
                final String id = user.getString("id");
                if(localContacts.containsKey(id)) {
                    localContacts.put(id, true);
                    continue;
                }
                final String firstname = user.getString("firstname");
                Log.d("ContactSyncAdapter", String.format("adding id: %s %s", id, firstname));
                ContactManager.addContact(
                        id,
                        firstname,
                        user.getString("lastname"),
                        user.getString("email"),
                        mContentResolver,
                        account.name
                );
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (Map.Entry<String, Boolean> contact : localContacts.entrySet()) {
            if(!contact.getValue()) {
                ContactManager.deleteContact(contact.getKey(), mContentResolver, account.name);
            }
        }
    }
}

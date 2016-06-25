/*
 * Copyright 2016 vileda
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.vileda.sipgatesync.authenticator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import cc.vileda.sipgatesync.sipgatesync.R;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.wuman.android.auth.OAuthManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.WRITE_CONTACTS;


public class SipgateLoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final int REQUEST_WRITE_CONTACTS = 1;

    private UserLoginTask mAuthTask = null;

    private AutoCompleteTextView mEmailView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sipgate_login);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.accountName);
        populateAutoComplete();
        mayWriteContacts();

        mEmailView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.accountName || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        assert mEmailSignInButton != null;
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        if (!mayReadContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayWriteContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(WRITE_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                @Override
                @TargetApi(Build.VERSION_CODES.M)
                public void onClick(View v) {
                    requestPermissions(new String[]{WRITE_CONTACTS}, REQUEST_WRITE_CONTACTS);
                }
            });
        }
        else {
            requestPermissions(new String[]{WRITE_CONTACTS}, REQUEST_WRITE_CONTACTS);
        }
        return false;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid accountName, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid accountName address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        else {
            OAuth oAuth = OAuth.newInstance(getApplicationContext(),
                getFragmentManager(),
                new ClientParametersAuthentication("2vqjBrGtjA", "RRHlNeDGdzGeEmFvKOwUCzwe8yXXWGVbDLQ80yV8ISj0jWIWsx"),
                "https://api.sipgate.com/v1/authorization/oauth/authorize",
                "https://api.sipgate.com/v1/authorization/oauth/token",
                "http://localhost/Callback", Collections.singletonList("contacts:read"));

            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(oAuth, email);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean mayReadContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                @Override
                @TargetApi(Build.VERSION_CODES.M)
                public void onClick(View v) {
                    requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                }
            });
        }
        else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show
                                         ? View.GONE
                                         : View.VISIBLE);
            mLoginFormView.animate()
              .setDuration(shortAnimTime)
              .alpha(show
                     ? 0
                     : 1)
              .setListener(new AnimatorListenerAdapter() {
                  @Override
                  public void onAnimationEnd(Animator animation) {
                      mLoginFormView.setVisibility(show
                                                   ? View.GONE
                                                   : View.VISIBLE);
                  }
              });

            mProgressView.setVisibility(show
                                        ? View.VISIBLE
                                        : View.GONE);
            mProgressView.animate()
              .setDuration(shortAnimTime)
              .alpha(show
                     ? 1
                     : 0)
              .setListener(new AnimatorListenerAdapter() {
                  @Override
                  public void onAnimationEnd(Animator animation) {
                      mProgressView.setVisibility(show
                                                  ? View.VISIBLE
                                                  : View.GONE);
                  }
              });
        }
        else {
            mProgressView.setVisibility(show
                                        ? View.VISIBLE
                                        : View.GONE);
            mLoginFormView.setVisibility(show
                                         ? View.GONE
                                         : View.VISIBLE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
        else if (requestCode == REQUEST_WRITE_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(getClass().getSimpleName(), "granted write contacts");
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
          // Retrieve data rows for the device user's 'profile' contact.
          Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

          // Select only accountName addresses.
          ContactsContract.Contacts.Data.MIMETYPE + " = ?", new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE},

          // Show primary accountName addresses first. Note that there won't be
          // a primary accountName address if the user hasn't specified one.
          ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(SipgateLoginActivity.this, android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {ContactsContract.CommonDataKinds.Email.ADDRESS, ContactsContract.CommonDataKinds.Email.IS_PRIMARY,};

        int ADDRESS = 0;
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private static final String TAG = "UserLoginTask";
        private final OAuth oauth;
        private final String email;

        UserLoginTask(final OAuth oauth, final String email) {
            this.oauth = oauth;
            this.email = email;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //final String token = SipgateApi.getToken(mEmail, mPassword);

            final OAuthManager.OAuthFuture<Credential> credentialOAuthFuture = oauth.authorizeExplicitly(email);

            String token = null;
            try {
                final Credential result = credentialOAuthFuture.getResult();
                token = result.getAccessToken();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            if (token == null) { return false; }

            AccountManager accountManager = AccountManager.get(SipgateLoginActivity.this); //this is Activity
            Account account = new Account(email, getString(R.string.account_type));
            final Bundle extras = new Bundle();

            extras.putString("token", token);
            boolean success = accountManager.addAccountExplicitly(account, null, extras);
            if (success) {
                accountManager.setAuthToken(account, "oauth", token);
                Log.d(TAG, "Account created");
            }
            else {
                Log.d(TAG, "Account creation failed. Look at previous logs to investigate");
            }

            ContentResolver.requestSync(account, ContactsContract.AUTHORITY, extras);
            ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true);
            ContentResolver.addPeriodicSync(account, ContactsContract.AUTHORITY, extras, 60 * 60 * 24);
            ContentResolver.setIsSyncable(account, ContactsContract.AUTHORITY, 1);

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            }
            else {
                Log.d(TAG, getString(R.string.error_incorrect_password));
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}


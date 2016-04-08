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

package cc.vileda.sipgatesync.contacts;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.provider.ContactsContract;

import java.util.ArrayList;

public final class ContactManager {
    public static void addContact(final String id, final String firstName, final String secondName, final String email, final ContentResolver resolver, String accountName) {
        ArrayList<ContentProviderOperation> operationList = new ArrayList<>();
        operationList.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.SOURCE_ID, id)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, "com.sipgate.account")
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, accountName)
                .build());

        // first and last names
        operationList.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, secondName)
                .build());

        operationList.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
                .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                .build());

        try{
            resolver.applyBatch(ContactsContract.AUTHORITY, operationList);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void deleteContact(final String id, final ContentResolver resolver, String accountName) {
        ArrayList<ContentProviderOperation> operationList = new ArrayList<>();
        String where = ContactsContract.RawContacts.SOURCE_ID + " = ? and " + ContactsContract.RawContacts.ACCOUNT_NAME + " = ?";
        String[] params = new String[] {id, accountName};
        operationList.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(where, params)
                .build());

        try{
            resolver.applyBatch(ContactsContract.AUTHORITY, operationList);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

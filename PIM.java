package ro.ase.lab114a.pim;

import android.annotation.SuppressLint;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.ContactsContract;

import java.util.ArrayList;

public class PIM {

    Context context;
    ContactsList list = new ContactsList();

    public PIM(Context context)
    {
        this.context = context;
    }

    @SuppressLint("Range")
    public void extractContact()
    {
        Cursor contacts = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                new String[] {ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER},
                null, null, null);

        if(contacts!=null)
        {
            while (contacts.moveToNext())
            {
                @SuppressLint("Range") String id =
                        contacts.getString(contacts.getColumnIndex(ContactsContract.Contacts._ID));
                @SuppressLint("Range") String name =
                        contacts.getString(contacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String phone = "";

                if(Integer.parseInt(contacts.getString(
                        contacts.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)))>0)
                {
                    Cursor phoneNumber = context.getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" = ?",
                            new String[] {id},
                            null);

                    if(phoneNumber !=null)
                    {
                        while (phoneNumber.moveToNext())
                        {
                            phone = phoneNumber.getString(phoneNumber.
                                    getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        }
                        phoneNumber.close();
                    }

                    ContactTelefonic contactTelefonic = new ContactTelefonic(id, name, phone);
                    list.adaugaContact(contactTelefonic);
                }
            }
            contacts.close();
        }
    }

    public void addContact()
    {
        ArrayList<ContentProviderOperation> cpo = new ArrayList<>();
        int contactIndex = cpo.size();

        cpo.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME,null).build());

        cpo.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
        .withValue(ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, "Maricica Popescu").build());

        cpo.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, "021345678")
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());

        try {
            ContentProviderResult[] cpr = context.getContentResolver().
                    applyBatch(ContactsContract.AUTHORITY, cpo);
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void sendEmail()
    {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL,
                new String[] {"gigelionescu@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subiect mesaj");
        intent.putExtra(Intent.EXTRA_TEXT,
                "Continut email...");
        context.startActivity(Intent.createChooser(intent, "Choose the application"));
    }
}

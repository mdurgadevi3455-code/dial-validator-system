package com.example.simactivationapp.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

public class ContactHelper {

    public static String getContactName(Context context, String phone) {

        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {

                int numberIndex = cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                );

                int nameIndex = cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                );

                if (numberIndex >= 0 && nameIndex >= 0) {

                    String dbNumber = cursor.getString(numberIndex);

                    if (dbNumber != null && dbNumber.contains(phone)) {
                        String name = cursor.getString(nameIndex);
                        cursor.close();
                        return name;
                    }
                }
            }
            cursor.close();
        }

        return null;
    }
}
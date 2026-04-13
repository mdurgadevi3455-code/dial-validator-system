package com.example.simactivationapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText phoneInput;
    Button validateBtn;
    TextView resultText;
    Button saveBtn;
    TextView statusText;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind UI
        phoneInput = findViewById(R.id.phoneInput);
        validateBtn = findViewById(R.id.validateBtn);
        resultText = findViewById(R.id.resultText);
        saveBtn = findViewById(R.id.saveBtn);
        statusText = findViewById(R.id.statusText);

        saveBtn.setVisibility(View.GONE);

        // Permissions
        if (checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{
                    Manifest.permission.WRITE_CONTACTS,
                    Manifest.permission.READ_CONTACTS
            }, 1);
        }

        // VALIDATE BUTTON
        validateBtn.setOnClickListener(v -> {

            String phone = phoneInput.getText().toString().trim();

            // Rule 1: 10 digits
            if (phone.length() != 10) {
                showInvalid();
                return;
            }

            // Rule 2: starts 6–9
            char firstDigit = phone.charAt(0);
            if (firstDigit < '6' || firstDigit > '9') {
                showInvalid();
                return;
            }

            // Already in contacts
            if (isNumberInContacts(phone)) {

                resultText.setText("Already in Contacts ✔");
                resultText.setTextColor(Color.GREEN);
                saveBtn.setVisibility(View.GONE);

            } else {

                resultText.setText("Valid Number ✔");
                resultText.setTextColor(Color.GREEN);
                saveBtn.setVisibility(View.VISIBLE);
            }
        });

        // SAVE CONTACT BUTTON
        saveBtn.setOnClickListener(v -> {

            String phone = phoneInput.getText().toString().trim();

            EditText nameInput = new EditText(this);
            nameInput.setHint("Enter Contact Name");

            new AlertDialog.Builder(this)
                    .setTitle("Save Contact")
                    .setView(nameInput)
                    .setPositiveButton("Save", (dialog, which) -> {

                        String name = nameInput.getText().toString().trim();
                        if (name.isEmpty()) name = "SIM User";

                        saveContactDirect(name, phone);

                        // 🔥 POPUP MESSAGE
                        Toast.makeText(this, "Contact Saved ✔", Toast.LENGTH_SHORT).show();

                        // 🔥 DELAYED UI UPDATE (3 sec)
                        handler.postDelayed(() -> {

                            resultText.setText("Saved Successfully ✔");
                            resultText.setTextColor(Color.GREEN);

                            saveBtn.setVisibility(View.GONE);

                        }, 3000);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    // INVALID UI
    private void showInvalid() {
        resultText.setText("Invalid Number ❌");
        resultText.setTextColor(Color.RED);
        saveBtn.setVisibility(View.GONE);
    }

    // CONTACT CHECK (SAFE)
    private boolean isNumberInContacts(String phone) {

        try {
            Cursor cursor = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor == null) return false;

            int index = cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            );

            while (cursor.moveToNext()) {

                String contactNumber;

                if (index != -1) {
                    contactNumber = cursor.getString(index);
                } else {
                    contactNumber = cursor.getString(0);
                }

                if (contactNumber != null) {
                    contactNumber = contactNumber.replaceAll("\\s+", "");

                    if (contactNumber.contains(phone)) {
                        cursor.close();
                        return true;
                    }
                }
            }

            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // CONTACT SAVE (SAFE)
    private void saveContactDirect(String name, String phone) {

        try {
            ContentValues values = new ContentValues();

            values.put(ContactsContract.RawContacts.ACCOUNT_TYPE, (String) null);
            values.put(ContactsContract.RawContacts.ACCOUNT_NAME, (String) null);

            Uri uri = getContentResolver().insert(
                    ContactsContract.RawContacts.CONTENT_URI,
                    values
            );

            if (uri == null) return;

            long rawContactId = android.content.ContentUris.parseId(uri);

            ContentValues phoneValues = new ContentValues();
            phoneValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
            phoneValues.put(ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            phoneValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone);
            phoneValues.put(ContactsContract.Data.DISPLAY_NAME, name);
            phoneValues.put(ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);

            getContentResolver().insert(
                    ContactsContract.Data.CONTENT_URI,
                    phoneValues
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
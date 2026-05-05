package com.example.simactivationapp;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    LinearLayout numbersContainer;
    Button validateBtn;
    HashSet<String> savedNumbers = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setStatusBarColor(Color.parseColor("#111111"));
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
        numbersContainer = findViewById(R.id.numbersContainer);
        validateBtn = findViewById(R.id.validateBtn);
        ImageButton contactsBtn = findViewById(R.id.contactsBtn);
        ImageButton refreshBtn = findViewById(R.id.refreshBtn);

        if (checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.WRITE_CONTACTS,
                    Manifest.permission.READ_CONTACTS
            }, 1);
        }

        contactsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, ContactsManagerActivity.class);
            startActivity(intent);
        });

        refreshBtn.setOnClickListener(v -> {
            savedNumbers.clear();
            numbersContainer.removeAllViews();

            // Re-add validate button
            Button newValidateBtn = new Button(this);
            newValidateBtn.setId(R.id.validateBtn);
            newValidateBtn.setText("Validate All");
            newValidateBtn.setTextColor(Color.WHITE);
            newValidateBtn.setTextSize(16);
            newValidateBtn.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            p.setMargins(0, 16, 0, 0);
            newValidateBtn.setLayoutParams(p);

            GradientDrawable validateNormal = new GradientDrawable();
            validateNormal.setColor(Color.parseColor("#E91E63"));
            validateNormal.setCornerRadius(50);

            GradientDrawable validatePressed = new GradientDrawable();
            validatePressed.setColor(Color.parseColor("#880E4F"));
            validatePressed.setCornerRadius(50);

            newValidateBtn.setBackground(validateNormal);
            newValidateBtn.setOnClickListener(btn -> validateAll());
            newValidateBtn.setOnTouchListener((v2, event) -> {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    newValidateBtn.setBackground(validatePressed);
                } else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                        event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                    newValidateBtn.setBackground(validateNormal);
                }
                return false;
            });

            numbersContainer.addView(newValidateBtn);
            validateBtn = newValidateBtn;

            addNumberBlock();
            Toast.makeText(this, "Reset", Toast.LENGTH_SHORT).show();
        });

        addNumberBlock();

        validateBtn.setOnClickListener(v -> validateAll());

        GradientDrawable validateNormal = new GradientDrawable();
        validateNormal.setColor(Color.parseColor("#E91E63"));
        validateNormal.setCornerRadius(50);

        GradientDrawable validatePressed = new GradientDrawable();
        validatePressed.setColor(Color.parseColor("#880E4F"));
        validatePressed.setCornerRadius(50);

        validateBtn.setBackground(validateNormal);
        validateBtn.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                validateBtn.setBackground(validatePressed);
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                    event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                validateBtn.setBackground(validateNormal);
            }
            return false;
        });
    }

    private void addNumberBlock() {
        int count = numbersContainer.getChildCount();
        if (count > 1) {
            LinearLayout prevBlock = (LinearLayout) numbersContainer.getChildAt(count - 2);
            View prevPlus = prevBlock.findViewWithTag("plusBtn");
            if (prevPlus != null) prevPlus.setVisibility(View.GONE);
        }

        // Phone input
        EditText phoneInput = new EditText(this);
        phoneInput.setHint("Enter Phone Number");
        phoneInput.setTextColor(Color.WHITE);
        phoneInput.setHintTextColor(Color.parseColor("#AAAAAA"));
        phoneInput.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneInput.setBackgroundColor(Color.parseColor("#1A1A1A"));
        phoneInput.setPadding(24, 24, 24, 24);
        phoneInput.setTag("phoneInput");

        // Plus button
        ImageButton plusBtn = new ImageButton(this);
        plusBtn.setImageResource(android.R.drawable.ic_input_add);
        plusBtn.setBackgroundColor(Color.TRANSPARENT);
        plusBtn.setColorFilter(Color.parseColor("#E91E63"));
        plusBtn.setTag("plusBtn");
        plusBtn.setOnClickListener(v -> {
            String phone = phoneInput.getText().toString().trim();
            if (phone.isEmpty()) {
                Toast.makeText(this, "Enter a number first", Toast.LENGTH_SHORT).show();
                return;
            }
            addNumberBlock();
        });

        // Input row
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setTag("inputRow");
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        phoneInput.setLayoutParams(inputParams);
        row.addView(phoneInput);
        row.addView(plusBtn);

        // Result box
        TextView resultText = new TextView(this);
        resultText.setTag("resultText");
        resultText.setVisibility(View.GONE);
        resultText.setTextSize(14);
        resultText.setTextColor(Color.WHITE);
        resultText.setGravity(Gravity.CENTER);
        resultText.setPadding(24, 20, 24, 20);
        LinearLayout.LayoutParams resultParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        resultParams.setMargins(0, 10, 0, 0);
        resultText.setLayoutParams(resultParams);

        // Save button
        Button saveBtn = createStyledSaveBtn();

        // Block
        LinearLayout block = new LinearLayout(this);
        block.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        blockParams.setMargins(0, 0, 0, 24);
        block.setLayoutParams(blockParams);
        // Result + Save in same row
        LinearLayout resultRow = new LinearLayout(this);
        resultRow.setOrientation(LinearLayout.HORIZONTAL);
        resultRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams resultRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        resultRowParams.setMargins(0, 10, 0, 0);
        resultRow.setLayoutParams(resultRowParams);

        LinearLayout.LayoutParams resultTextParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        resultText.setLayoutParams(resultTextParams);

        resultRow.addView(resultText);
        resultRow.addView(saveBtn);

        block.addView(row);
        block.addView(resultRow);

        int insertIndex = numbersContainer.getChildCount() - 1;
        numbersContainer.addView(block, insertIndex);
    }

    private void validateAll() {
        int blockCount = numbersContainer.getChildCount() - 1;
        if (blockCount == 0) return;

        for (int i = 0; i < blockCount; i++) {
            LinearLayout block = (LinearLayout) numbersContainer.getChildAt(i);
            EditText phoneInput = (EditText) block.findViewWithTag("phoneInput");
            TextView resultText = (TextView) block.findViewWithTag("resultText");
            Button saveBtn = (Button) block.findViewWithTag("saveBtn");

            String phone = phoneInput.getText().toString().trim();

            saveBtn.setVisibility(View.GONE);
            saveBtn.setOnClickListener(null);
            resultText.setVisibility(View.VISIBLE);

            if (phone.isEmpty()) {
                setResultBox(resultText, "Number is empty", false);
                continue;
            }

            if (phone.length() != 10 || phone.charAt(0) < '6' || phone.charAt(0) > '9') {
                setResultBox(resultText, "Invalid Number", false);
                continue;
            }

            if (savedNumbers.contains(phone)) {
                setResultBox(resultText, "Already Saved", true);
                continue;
            }

            if (isNumberInContacts(phone)) {
                setResultBox(resultText, "Already in Contacts", true);
                continue;
            }

            // Valid
            setResultBox(resultText, "Valid Number", true);
            saveBtn.setVisibility(View.VISIBLE);
            saveBtn.setOnClickListener(v -> {
                EditText nameInput = new EditText(this);
                nameInput.setHint("Enter Contact Name");
                new AlertDialog.Builder(this)
                        .setTitle("Save Contact")
                        .setView(nameInput)
                        .setPositiveButton("Save", (dialog, which) -> {
                            String name = nameInput.getText().toString().trim();
                            if (name.isEmpty()) name = "SIM User";
                            saveContactDirect(name, phone);
                            savedNumbers.add(phone);
                            Toast.makeText(this, "Contact Saved", Toast.LENGTH_SHORT).show();
                            setResultBox(resultText, "Saved Successfully", true);
                            saveBtn.setVisibility(View.GONE);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }

    private void setResultBox(TextView resultText, String message, boolean valid) {
        resultText.setText(message);
        resultText.setTextColor(valid ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));
        resultText.setBackground(null);
        resultText.setGravity(Gravity.START);
    }

    private Button createStyledSaveBtn() {
        Button saveBtn = new Button(this);
        saveBtn.setTag("saveBtn");
        saveBtn.setText("Save");
        saveBtn.setTextColor(Color.WHITE);
        saveBtn.setVisibility(View.GONE);

        GradientDrawable normal = new GradientDrawable();
        normal.setColor(Color.parseColor("#2196F3"));
        normal.setCornerRadius(50);

        GradientDrawable pressed = new GradientDrawable();
        pressed.setColor(Color.parseColor("#1565C0"));
        pressed.setCornerRadius(50);

        saveBtn.setBackground(normal);

        LinearLayout.LayoutParams saveBtnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        saveBtnParams.setMargins(0, 12, 0, 0);
        saveBtnParams.gravity = Gravity.END;
        saveBtn.setLayoutParams(saveBtnParams);
        saveBtn.setPadding(48, 24, 48, 24);

        saveBtn.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                saveBtn.setBackground(pressed);
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                    event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                saveBtn.setBackground(normal);
            }
            return false;
        });
        return saveBtn;
    }

    private boolean isNumberInContacts(String phone) {
        try {
            Cursor cursor = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, null);
            if (cursor == null) return false;
            int index = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            while (cursor.moveToNext()) {
                String contactNumber = index != -1 ? cursor.getString(index) : cursor.getString(0);
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

    private void saveContactDirect(String name, String phone) {
        try {
            ArrayList<android.content.ContentProviderOperation> ops = new ArrayList<>();

            ops.add(android.content.ContentProviderOperation
                    .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            ops.add(android.content.ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                    .build());

            ops.add(android.content.ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());

            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
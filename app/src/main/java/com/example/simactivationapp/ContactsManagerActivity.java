package com.example.simactivationapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Color;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ContactsManagerActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText searchBar;
    List<Contact> allContacts = new ArrayList<>();
    List<Contact> filteredContacts = new ArrayList<>();
    ContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_manager);

        getWindow().setStatusBarColor(Color.parseColor("#111111"));
        recyclerView = findViewById(R.id.contactsRecyclerView);
        searchBar = findViewById(R.id.searchBar);
        ImageButton backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(v -> finish());

        adapter = new ContactAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Request permissions if not granted
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
            }, 1);
        } else {
            loadContacts();
        }

        searchBar.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString());
            }
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadContacts();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            loadContacts();
        }
    }

    private void loadContacts() {
        allContacts.clear();
        try {
            HashSet<String> seen = new HashSet<>();

            Cursor cursor = getContentResolver().query(
                    ContactsContract.RawContacts.CONTENT_URI,
                    new String[]{
                            ContactsContract.RawContacts._ID,
                            ContactsContract.RawContacts.CONTACT_ID,
                            ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY
                    },
                    ContactsContract.RawContacts.DELETED + "=0",
                    null,
                    ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY + " ASC");

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String rawId = cursor.getString(0);
                    String contactId = cursor.getString(1);
                    String name = cursor.getString(2);

                    if (contactId != null && name != null && !seen.contains(contactId)) {
                        seen.add(contactId);

                        // Get phone number for this raw contact
                        Cursor phoneCursor = getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                                new String[]{contactId}, null);

                        String number = "";
                        if (phoneCursor != null) {
                            if (phoneCursor.moveToFirst()) {
                                number = phoneCursor.getString(0);
                            }
                            phoneCursor.close();
                        }

                        if (!number.isEmpty()) {
                            allContacts.add(new Contact(contactId, name, number));
                        }
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        filteredContacts = new ArrayList<>(allContacts);
        adapter.notifyDataSetChanged();

        Toast.makeText(this, "Total contacts loaded: " + allContacts.size(), Toast.LENGTH_SHORT).show();
    }

    private void filterContacts(String query) {
        filteredContacts.clear();
        if (query.isEmpty()) {
            filteredContacts.addAll(allContacts);
        } else {
            for (Contact c : allContacts) {
                if (c.name.toLowerCase().contains(query.toLowerCase()) ||
                        c.number.contains(query)) {
                    filteredContacts.add(c);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void deleteContact(String contactId, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete this contact?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Uri uri = Uri.withAppendedPath(
                            ContactsContract.Contacts.CONTENT_URI, contactId);
                    getContentResolver().delete(uri, null, null);
                    filteredContacts.remove(position);
                    allContacts.removeIf(c -> c.id.equals(contactId));
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Contact Deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void editContact(Contact contact, int position) {
        EditText nameInput = new EditText(this);
        nameInput.setText(contact.name);
        nameInput.setSelection(contact.name.length());

        new AlertDialog.Builder(this)
                .setTitle("Edit Contact Name")
                .setView(nameInput)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = nameInput.getText().toString().trim();
                    if (newName.isEmpty()) return;

                    Cursor cursor = getContentResolver().query(
                            ContactsContract.RawContacts.CONTENT_URI,
                            new String[]{ContactsContract.RawContacts._ID},
                            ContactsContract.RawContacts.CONTACT_ID + "=?",
                            new String[]{contact.id}, null);

                    if (cursor != null && cursor.moveToFirst()) {
                        long rawId = cursor.getLong(0);
                        cursor.close();

                        android.content.ContentValues nameValues =
                                new android.content.ContentValues();
                        nameValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawId);
                        nameValues.put(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
                        nameValues.put(
                                ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                                newName);

                        int updated = getContentResolver().update(
                                ContactsContract.Data.CONTENT_URI,
                                nameValues,
                                ContactsContract.Data.RAW_CONTACT_ID + "=? AND " +
                                        ContactsContract.Data.MIMETYPE + "=?",
                                new String[]{String.valueOf(rawId),
                                        ContactsContract.CommonDataKinds.StructuredName
                                                .CONTENT_ITEM_TYPE});

                        if (updated == 0) {
                            getContentResolver().insert(
                                    ContactsContract.Data.CONTENT_URI, nameValues);
                        }
                    }

                    contact.name = newName;
                    filteredContacts.set(position, contact);
                    adapter.notifyItemChanged(position);
                    Toast.makeText(this, "Contact Updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void shareContact(Contact contact) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, contact.name + ": " + contact.number);
        startActivity(Intent.createChooser(intent, "Share Contact"));
    }

    static class Contact {
        String id, name, number;
        Contact(String id, String name, String number) {
            this.id = id;
            this.name = name;
            this.number = number;
        }
    }

    class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView avatar, name, number;
            ImageButton edit, share, delete;

            ViewHolder(View v) {
                super(v);
                avatar = v.findViewById(R.id.avatarText);
                name = v.findViewById(R.id.contactName);
                number = v.findViewById(R.id.contactNumber);
                edit = v.findViewById(R.id.editBtn);
                share = v.findViewById(R.id.shareBtn);
                delete = v.findViewById(R.id.deleteBtn);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_contact, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Contact contact = filteredContacts.get(position);
            holder.name.setText(contact.name);
            holder.number.setText(contact.number);
            holder.avatar.setText(contact.name.substring(0, 1).toUpperCase());

            holder.edit.setOnClickListener(v ->
                    editContact(contact, holder.getAdapterPosition()));
            holder.share.setOnClickListener(v -> shareContact(contact));
            holder.delete.setOnClickListener(v ->
                    deleteContact(contact.id, holder.getAdapterPosition()));
        }

        @Override
        public int getItemCount() {
            return filteredContacts.size();
        }
    }
}
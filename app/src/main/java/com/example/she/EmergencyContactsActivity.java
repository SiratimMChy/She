package com.example.she;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EmergencyContactsActivity extends AppCompatActivity {

    private static final String EMERGENCY_CONTACTS_NODE = "EmergencyContacts";
    private static final int REQUEST_CALL_PHONE_PERMISSION = 1;

    private RecyclerView recyclerViewContacts;
    private ArrayList<EmergencyContact> contactList;
    private ContactAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private EditText etName, etPhone;
    private Button btnAddContact;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        recyclerViewContacts = findViewById(R.id.recyclerViewContacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));

        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        btnAddContact = findViewById(R.id.btnAddContact);

        contactList = new ArrayList<>();
        adapter = new ContactAdapter(contactList);
        recyclerViewContacts.setAdapter(adapter);

        ImageButton backbutton = findViewById(R.id.Back_Button_E);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EmergencyContactsActivity.this, Drawer_menu.class));
            }
        });

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            fetchContactsFromFirebase();
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        btnAddContact.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please enter name and phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            saveContactToFirebase(name, phone);
            etName.setText("");
            etPhone.setText("");
        });
    }

    private void saveContactToFirebase(String name, String phone) {
        if (currentUserId == null) return;

        DatabaseReference contactsRef = FirebaseDatabase.getInstance().getReference(EMERGENCY_CONTACTS_NODE).child(currentUserId);
        String contactId = contactsRef.push().getKey();

        if (contactId != null) {
            EmergencyContact contact = new EmergencyContact(contactId, name, phone);
            contactsRef.child(contactId).setValue(contact)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Contact saved successfully", Toast.LENGTH_SHORT).show();
                        fetchContactsFromFirebase();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void fetchContactsFromFirebase() {
        if (currentUserId == null) return;

        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(EMERGENCY_CONTACTS_NODE).child(currentUserId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<EmergencyContact> tempList = new ArrayList<>();
                tempList.add(new EmergencyContact("1", "National Emergency", "01568849596"));
                tempList.add(new EmergencyContact("2", "National Women HelpLine", "01721967220"));

                for (DataSnapshot child : snapshot.getChildren()) {
                    EmergencyContact contact = child.getValue(EmergencyContact.class);
                    if (contact != null) {
                        tempList.add(contact);
                    }
                }

                contactList.clear();
                contactList.addAll(tempList);
                progressBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();

                tvEmptyState.setVisibility(contactList.size() <= 2 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EmergencyContactsActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class EmergencyContact {
        private String id;
        private String name;
        private String phoneNumber;

        public EmergencyContact() {}

        public EmergencyContact(String id, String name, String phoneNumber) {
            this.id = id;
            this.name = name;
            this.phoneNumber = phoneNumber;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }
    }

    private class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
        private ArrayList<EmergencyContact> contactList;

        public ContactAdapter(ArrayList<EmergencyContact> contactList) {
            this.contactList = contactList;
        }

        @NonNull
        @Override
        public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_card, parent, false);
            return new ContactViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
            EmergencyContact contact = contactList.get(position);
            holder.tvName.setText(contact.getName());
            holder.tvNumber.setText(contact.getPhoneNumber());

            holder.btnCall.setOnClickListener(v -> {
                if (ContextCompat.checkSelfPermission(EmergencyContactsActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contact.getPhoneNumber())));
                } else {
                    ActivityCompat.requestPermissions(EmergencyContactsActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE_PERMISSION);
                }
            });

            holder.btnDelete.setOnClickListener(v -> {
                if (position < 2) {
                    Toast.makeText(EmergencyContactsActivity.this, "Cannot Delete National Emergency Helpline Numbers.", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(EMERGENCY_CONTACTS_NODE).child(currentUserId).child(contact.getId());
                reference.removeValue().addOnSuccessListener(aVoid -> {
                    Toast.makeText(EmergencyContactsActivity.this, "Contact deleted successfully", Toast.LENGTH_SHORT).show();
                    fetchContactsFromFirebase();
                });
            });
        }

        @Override
        public int getItemCount() {
            return contactList.size();
        }

        class ContactViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvNumber;
            Button btnCall, btnDelete;

            public ContactViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.txtName);
                tvNumber = itemView.findViewById(R.id.txtNumber);
                btnCall = itemView.findViewById(R.id.btnCall);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }
}

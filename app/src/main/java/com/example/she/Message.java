package com.example.she;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Message extends Fragment {

    private static final int REQUEST_SEND_SMS_PERMISSION = 1;

    private RecyclerView recyclerViewContacts;
    private ArrayList<EmergencyContactsActivity.EmergencyContact> contactList;
    private ContactAdapter adapter;
    private ProgressBar progressBar;
    private EditText etMessage;

    public Message() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewContacts = view.findViewById(R.id.recyclerViewContacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(requireContext()));

        progressBar = view.findViewById(R.id.progressBar);
        etMessage = view.findViewById(R.id.etMessage);

        contactList = new ArrayList<>();
        adapter = new ContactAdapter(contactList);
        recyclerViewContacts.setAdapter(adapter);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, REQUEST_SEND_SMS_PERMISSION);
        }

        com.google.firebase.auth.FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            fetchContactsFromFirebase(currentUser.getUid());
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SEND_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "SMS permission denied", Toast.LENGTH_SHORT).show();
                disableSendButtons();
            }
        }
    }

    private void fetchContactsFromFirebase(String userId) {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("EmergencyContacts").child(userId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    EmergencyContactsActivity.EmergencyContact contact = child.getValue(EmergencyContactsActivity.EmergencyContact.class);
                    if (contact != null) {
                        contactList.add(contact);
                    }
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                if (contactList.isEmpty()) {
                    Toast.makeText(requireContext(), "No emergency contacts found. Please add contacts.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to fetch contacts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void sendSMS(String phoneNumber) {
        String message = etMessage.getText().toString();

        if (message.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SmsManager smsManager;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                smsManager = requireContext().getSystemService(SmsManager.class);
            } else {
                smsManager = SmsManager.getDefault();
            }
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(requireContext(), "SMS sent successfully!", Toast.LENGTH_SHORT).show();
            etMessage.setText(""); // Clear the EditText
        } catch (Exception e) {
            Toast.makeText(requireContext(), "SMS failed to send: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void disableSendButtons() {
        for (int i = 0; i < recyclerViewContacts.getChildCount(); i++) {
            View view = recyclerViewContacts.getChildAt(i);
            Button btnSendSMS = view.findViewById(R.id.btnSendSMS);
            if (btnSendSMS != null) {
                btnSendSMS.setEnabled(false);
            }
        }
    }

    private class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

        private ArrayList<EmergencyContactsActivity.EmergencyContact> contacts;

        public ContactAdapter(ArrayList<EmergencyContactsActivity.EmergencyContact> contacts) {
            this.contacts = contacts;
        }

        @NonNull
        @Override
        public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_card_sms_whatsapp, parent, false);
            return new ContactViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
            EmergencyContactsActivity.EmergencyContact contact = contacts.get(position);

            holder.tvName.setText(contact.getName());
            holder.tvNumber.setText(contact.getPhoneNumber());

            holder.btnSendSMS.setOnClickListener(v -> sendSMS(contact.getPhoneNumber()));
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }

        class ContactViewHolder extends RecyclerView.ViewHolder {

            TextView tvName, tvNumber;
            Button btnSendSMS;

            public ContactViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.txtName);
                tvNumber = itemView.findViewById(R.id.txtNumber);
                btnSendSMS = itemView.findViewById(R.id.btnSendSMS);
            }
        }
    }
}

package com.example.whatsappclone;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FindUserActivity extends AppCompatActivity {
    private static final String TAG = "FindUserActivity";
    private RecyclerView mUserList;
    private RecyclerView.Adapter mUserListAdapter;
    private RecyclerView.LayoutManager mUserListLayoutManager;

    ArrayList<UserObject> userList, contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);

        userList = new ArrayList<>();
        contactList = new ArrayList<>();

        initializeRecyclerView();
        getContactList();
    }

    private void getContactList(){
        String IsoPrefix = getCountryISO();
        //get all contacts in the phone
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null);

        while(phones.moveToNext()){
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone= phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            //normalizing phone string
            //remove characters other than 0-9 and +
            phone = phone.replaceAll("[^0-9+]", "");
            Log.d(TAG, "Phone number after regex:" + phone);

            //if the phone doesn't have iso prefix
            if(!String.valueOf(phone.charAt(0)).equals("+"))
                phone = IsoPrefix + phone;

            Log.d(TAG, "Phone number after check ISO:" + phone);

            UserObject mContact = new UserObject(name, phone);
            contactList.add(mContact);
            getUserDetails(mContact);
        }
        //notify adapter that contact has been loaded
        mUserListAdapter.notifyDataSetChanged();
        phones.close();
    }

    /* if the contact in the @mContact are exist in the firebase database
    ** add it to contact list
     */
    private void getUserDetails(final UserObject mContact) {
        //get data from database
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user");
        //fetch only one user that equals to parameter
        Query query = mUserDB.orderByChild("phone").equalTo(mContact.getPhone());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Log.d(TAG, "Data " + mContact.getPhone() + " exists!");
                    String phone = "",
                            name = mContact.getName();
                    for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                        if(childSnapshot.child("phone").getValue() != null)
                            phone = childSnapshot.child("phone").getValue().toString();

                        UserObject mUser = new UserObject(name, phone);
                        userList.add(mUser);
                        mUserListAdapter.notifyDataSetChanged();
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getCountryISO(){
        String iso = null;
        //contain information of our phone ISO
        TelephonyManager telephonyManager = (TelephonyManager)getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        if (telephonyManager.getNetworkCountryIso() != null) {
            if(!telephonyManager.getNetworkCountryIso().equals(""))
                iso = telephonyManager.getNetworkCountryIso();
        }

        return CountryToPhonePrefix.getPhone(iso);
    }

    private void initializeRecyclerView() {
        mUserList = findViewById(R.id.user_list);
        mUserList.setNestedScrollingEnabled(false);
        mUserList.setHasFixedSize(false);
        mUserListLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mUserList.setLayoutManager(mUserListLayoutManager);
        mUserListAdapter = new UserListAdapter(userList);
        mUserList.setAdapter(mUserListAdapter);
    }
}

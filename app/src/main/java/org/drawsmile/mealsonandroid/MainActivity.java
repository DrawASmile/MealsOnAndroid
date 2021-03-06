package org.drawsmile.mealsonandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.location.Geocoder;

import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;



import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.ads.MobileAds;


import java.io.IOException;
import java.util.List;
import java.util.*;





public class MainActivity extends AppCompatActivity {
    public TextView address;
    public TextView date;
    public TextView time;
    public Button Maps;

    public static boolean fragmentViewCreated = false;

    public static boolean signedIn = false;
    public static boolean checkedIn = false;

    public static String uid = "-";
    public static String phoneNumber = "-";
    public static boolean phoneSignIn = false;

    public static int curSection = 0;


    public static Context conte = null;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    public static AdView adView;
    public static AdRequest adRequest;

    @Override
    protected void onPause() {
        if(adView != null)
            adView.pause();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adView != null)
           adView.resume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        Firebase.setAndroidContext(this);
        conte = this;

        loadLoginInfo(this); //warning: not sure if passing this counts as an activity.


        //ads stuff
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");



        adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR) //all emulators
                .addTestDevice("428C355F90581AE85A4ACA36D9453119")  //My phone's device ID
                .build();



    }


    public void loadLoginInfo(Context co)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(co);
        String fuid = prefs.getString("firebaseUID", "-");
        String checkInStatus = prefs.getString("checkedIn", "false");
        String phoneNum = prefs.getString("phoneNumber", "-");

        if(checkInStatus.equals("true"))
        {
            checkedIn = true;
        }
        else
        {
            checkedIn = false;
        }

        if(!phoneNum.equals("-"))
        {
            phoneNumber = phoneNum;
            signedIn = true;
            phoneSignIn = true;
            Log.i("drawsmileauthdebug", "Phone number: " + phoneNum);
            Toast.makeText(co, "Loaded saved login info", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.i("drawsmileauthdebug", fuid);

        if(fuid.equals("-"))
        {
            signedIn = false;
            return;
        }
        else
        {
            uid = fuid;
            signedIn = true;
            phoneSignIn = false;
            Toast.makeText(co, "Loaded saved login info", Toast.LENGTH_SHORT).show();
            return;
        }
}

    @Override
    public void onStart() {
        super.onStart();
        Firebase ref = new Firebase("https://draw-a-smile.firebaseio.com/");

        loadLoginInfo(this);



        ref.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot snapshot) {

                String Address = snapshot.child("Address").getValue(String.class);
                String Date = snapshot.child("Date").getValue(String.class);
                String Time = snapshot.child("Time").getValue(String.class);

                if(!uid.equals("-"))
                {
                    checkedIn = snapshot.child("checkedInUsers").hasChild(uid);
                }



                if(curSection == 0)
                {
                    Fragment frag = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + mViewPager.getCurrentItem());
                    PlaceholderFragment page = (PlaceholderFragment) frag;

                    Log.i("drawsmiledebug", "Setting text fields A: " + Address + ", D: " + Date + ", T: " + Time);

                    if(page != null)
                        page.setTextFields(Address, Date, Time);
                }




                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putString("address", Address);
                editor.putString("date", Date);
                editor.putString("time", Time);

                if(checkedIn)
                {
                    editor.putString("checkedIn", "true");
                }
                else
                {
                    editor.putString("checkedIn", "false");
                }

                editor.apply();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        public TextView Faddress;
        public TextView Fdate;
        public TextView Ftime;
        public Button FMaps;
        public Button FSignInButton;
        public Button FCheckInButton;

        public Button FvolSignUp;
        public EditText FvolFirstName;
        public EditText FvolLastName;
        public EditText FvolPhoneNum;

        public TextView FLoginStatus;
        public TextView FCheckinStatus;

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public void setTextFields(String address, String date, String time)
        {
            if(fragmentViewCreated)
            {
                Faddress.setText(address);
                Fdate.setText(date);
                Ftime.setText(time);
            }

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {


            Log.i("drawsmiledebug", "ENTERED onCreateView");
            View rootView = null;
            switch(getArguments().getInt(ARG_SECTION_NUMBER))
            {
                case 0:
                    rootView = inflater.inflate(R.layout.fragment_food, container, false);
                    break;
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_volunteer, container, false);
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_contact, container, false);
                    break;
            }

            adView = (AdView) rootView.findViewById(R.id.adView);

            adView.loadAd(adRequest);

            Log.i("drawsmiledebug", "Section number: " + getArguments().getInt(ARG_SECTION_NUMBER));
            if(getArguments().getInt(ARG_SECTION_NUMBER) == 0)
            {
                Log.i("drawsmiledebug", "ENTERED Faddress assigner");
                Faddress = (TextView) rootView.findViewById(R.id.Address);

                Fdate = (TextView) rootView.findViewById(R.id.Date);

                Ftime = (TextView) rootView.findViewById(R.id.Time);
                FMaps = (Button) rootView.findViewById(R.id.ViewMap_btn);

                FSignInButton = (Button) rootView.findViewById(R.id.button_signIn);
                FCheckInButton = (Button) rootView.findViewById(R.id.button_checkIn);

                FLoginStatus = (TextView) rootView.findViewById(R.id.label_logInStatus);
                FCheckinStatus = (TextView) rootView.findViewById(R.id.label_checkInStatus);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String address = prefs.getString("address", "Address N/A");
                String date = prefs.getString("date", "Date N/A");
                String time = prefs.getString("time", "Time N/A");

                Faddress.setText(address);
                Fdate.setText(date);
                Ftime.setText(time);

                if(signedIn)
                {
                    FSignInButton.setText("Sign Out");
                    FLoginStatus.setText("Logged In");
                }
                else
                {
                    FSignInButton.setText("Sign In");
                    FLoginStatus.setText("Not Logged In");
                }

                if(checkedIn)
                {
                    FCheckInButton.setText("Check Out");
                    FCheckinStatus.setText("Checked In");
                }
                else
                {
                    FCheckInButton.setText("Check In");
                    FCheckinStatus.setText("Not Checked In");
                }

                FSignInButton.setOnClickListener(new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(android.view.View view) {

                        if(!signedIn)
                        {
                            Intent myIntent = new Intent(conte, AuthenticationActivity.class);
                            conte.startActivity(myIntent);
                        }
                        else
                        {
                            signedIn = false;
                            phoneSignIn = false;
                            uid = "";
                            FSignInButton.setText("Sign In");
                            FLoginStatus.setText("Not Logged In");
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(conte).edit();
                            editor.putString("firebaseUID", "-");
                            editor.putString("phoneNumber", "-");
                            editor.apply();
                        }



                    }
                });

                FCheckInButton.setOnClickListener(new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(android.view.View view) {

                        if(!signedIn)
                        {
                            Toast.makeText(conte, "You must be signed in to check in", Toast.LENGTH_LONG).show();
                        }
                        else
                        {

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            String fuid;
                            if(phoneSignIn)
                            {
                                fuid = prefs.getString("phoneNumber", "-");
                            }
                            else
                            {
                                fuid = prefs.getString("firebaseUID", "-");
                            }


                            if(fuid.equals("-"))
                            {
                                Toast.makeText(conte, "Can't load your user ID for check in/out", Toast.LENGTH_LONG).show();
                            }
                            else //uid loaded
                            {
                                if(!checkedIn) //not checked in so check them in
                                {
                                    Firebase ref = new Firebase("https://draw-a-smile.firebaseio.com/");
                                    Firebase userRef = ref.child("checkedInUsers").child(fuid);

                                    userRef.child("checkIn").setValue("true");

                                    checkedIn = true;

                                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(conte).edit();
                                    editor.putString("checkedIn", "true");

                                    editor.apply();

                                    Toast.makeText(conte, "Checked in", Toast.LENGTH_LONG).show();
                                    FCheckInButton.setText("Check Out");
                                    FCheckinStatus.setText("Checked In");

                                }
                                else //checked in so check them out
                                {
                                    Firebase ref = new Firebase("https://draw-a-smile.firebaseio.com/");
                                    ref.child("checkedInUsers").child(fuid).removeValue();

                                    checkedIn = false;

                                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(conte).edit();
                                    editor.putString("checkedIn", "false");

                                    editor.apply();

                                    Toast.makeText(conte, "Checked out", Toast.LENGTH_LONG).show();
                                    FCheckInButton.setText("Check In");
                                    FCheckinStatus.setText("Not Checked In");

                                }
                            }

                        }


                    }
                });

                FMaps.setOnClickListener(new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(android.view.View view) {


                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String address= prefs.getString("address", null);
                        Log.v("adddress",address);

                        Geocoder gc = new Geocoder(getActivity());
                        try {

                            List<android.location.Address> list=gc.getFromLocationName(address,1);
                            android.location.Address add=list.get(0);
                            double lat=add.getLatitude();
                            double lng=add.getLongitude();
                            // SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                            // Log.v("lats",String.valueOf(lat));
                            //  editor.putLong("lats",  Double.doubleToRawLongBits(lat));
                            //  editor.putLong("lng",  Double.doubleToRawLongBits(lng));
                            //   editor.apply();

                            Intent intent = new Intent(getActivity(), MapsActivity.class);
                            intent.putExtra("lat", lat);
                            intent.putExtra("lng", lng);
                            startActivity(intent);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                });
            }
            else if(getArguments().getInt(ARG_SECTION_NUMBER) == 1)
            {
                FvolFirstName = (EditText) rootView.findViewById(R.id.field_firstName);    //the field to enter the volunteer's first name
                FvolLastName = (EditText) rootView.findViewById(R.id.field_LastName);      //the field to enter the volunteer's last name
                FvolSignUp = (Button) rootView.findViewById(R.id.button_volunteerSignup);  //the button to sign up the volunteer
                FvolPhoneNum = (EditText) rootView.findViewById(R.id.field_phoneNumber);

              /*  String volunteerFirstName = FvolFirstName.getText().toString();  //string containing the volunteer's first name
                String volunteerLastName = FvolLastName.getText().toString();   //string containing the volunteer's last name

                String volunteerPhoneNumber = FvolPhoneNum.getText().toString().replace("Phone Number (make blank if N/A)", "").trim(); */


                FvolSignUp.setOnClickListener(new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(android.view.View view) {

                        Log.i("drawsmiledebug", "Volunteer signup button pressed");
                        String volunteerFirstName = FvolFirstName.getText().toString().trim();  //string containing the volunteer's first name
                        String volunteerLastName = FvolLastName.getText().toString().trim();

                        String volunteerPhoneNumber = FvolPhoneNum.getText().toString().replace("Phone Number (make blank if N/A)", "").trim();
                        Firebase ref = new Firebase("https://draw-a-smile.firebaseio.com/").child("volunteers");
                        Firebase userRef = ref.child(volunteerFirstName + "-" + volunteerLastName);

                        userRef.child("name").setValue(volunteerFirstName + " " + volunteerLastName);
                        userRef.child("phone").setValue(volunteerPhoneNumber);

                        new SendEmailTask().execute(volunteerFirstName, volunteerLastName);

                        Toast.makeText(getActivity(), "Signed up to volunteer", Toast.LENGTH_LONG).show();


                    }
                });
            }
            fragmentViewCreated = true;
            //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            curSection = position;
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Food";
                case 1:
                    return "Volunteer";
                case 2:
                    return "Contact Us";
            }
            return null;
        }
    }

}

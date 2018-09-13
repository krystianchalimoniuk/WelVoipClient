package welvoipclient.com.welvoipclient;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;


/**
 * Created by Krystiano on 2016-11-29.
 */

public class AddContactsActivity extends AppCompatActivity {
    Toolbar toolbar;
    EditText first_nameET,last_nameET,ipAddressET;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        FloatingActionButton fab;
        toolbar = (Toolbar) findViewById(R.id.addnote_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.cancel);

        getSupportActionBar().setTitle("Dodaj kontakt");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        first_nameET = (EditText) findViewById(R.id.addcontact_first_name);
        last_nameET = (EditText) findViewById(R.id.addcontact_last_name);
        ipAddressET = (EditText) findViewById(R.id.addcontact_ipaddress);
        fab = (FloatingActionButton) findViewById(R.id.addnote_fab);



        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Add note to DB

               String first_name = first_nameET.getText().toString();
               String last_name = last_nameET.getText().toString();
               String ipAddress = ipAddressET.getText().toString();

                if (!first_name.isEmpty() && !last_name.isEmpty() && !ipAddress.isEmpty()) {


                    Contacts contacts = new Contacts(first_name,last_name,ipAddress);
                    contacts.save();



                }

                finish();


            }
        });


    }




}

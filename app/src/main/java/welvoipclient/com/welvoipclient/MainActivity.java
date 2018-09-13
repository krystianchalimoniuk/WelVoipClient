

/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package welvoipclient.com.welvoipclient;

        import android.app.AlertDialog;
import android.app.Dialog;
        import android.content.Context;
        import android.content.DialogInterface;
import android.content.Intent;
        import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
        import android.net.sip.SipManager;
        import android.net.wifi.WifiManager;
        import android.os.Build;
import android.os.Bundle;
        import android.os.Handler;
        import android.preference.PreferenceManager;
        import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
        import android.text.format.Formatter;
        import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
        import android.view.View;
import android.view.WindowManager;
        import android.widget.ImageView;
        import android.widget.TextView;

        import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity  {
    RecyclerView mRecyclerView;
    ContactsAdapter adapter;
    long initialCount;
    List<Contacts> dataArray = new ArrayList<>();
    int modifyPos = -1;
    public String firstName=null;
    public String lastName=null;
    public String ipAddress = null;
    MakeCallActivity tc;
    private static final int CALL_ADDRESS = 1;
    public static String localIP;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.welmini);

        localIP = getLocalIP();
//

        updateStatus("Twój adres IP to: "+localIP,R.drawable.green_light);



        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //utrzymuję aktywny ekran


       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        StaggeredGridLayoutManager gridLayoutManager =
                new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        gridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        tc=new MakeCallActivity(this);
        tc.waitForConnection(); //uruchomienie funkcji nasłuchującej
        tc.listen(); //funkcja licząca przepustowość
        tc.endCalllisten();
        initialCount = Contacts.count(Contacts.class);

        if (savedInstanceState != null)
            modifyPos = savedInstanceState.getInt("modify");


        if (initialCount >= 0) {

            dataArray = Contacts.listAll(Contacts.class);

            adapter = new ContactsAdapter(MainActivity.this, dataArray);
            mRecyclerView.setAdapter(adapter);

            if (dataArray.isEmpty())
                Snackbar.make(mRecyclerView, "Nie masz zadnego kontaktu.", Snackbar.LENGTH_LONG).show();

        }else {
            dataArray = new ArrayList<>();
            adapter = new ContactsAdapter(MainActivity.this, dataArray);
        }


        // tinting FAB icon
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.add_contact);
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, Color.WHITE);
            DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);

            fab.setImageDrawable(drawable);

        }


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MainActivity.this, AddContactsActivity.class);
                startActivity(i);

            }
        });



        //Handlig swap to delete
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                final int position = viewHolder.getAdapterPosition();
                final Contacts contacts = dataArray.get(viewHolder.getAdapterPosition());
                dataArray.remove(viewHolder.getAdapterPosition());
                adapter.notifyItemRemoved(position);

                contacts.delete();
                initialCount -= 1;




                Snackbar.make(mRecyclerView, "Kontakt usunięty", Snackbar.LENGTH_SHORT)
                        .setAction("COFNIJ", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                contacts.save();
                                dataArray.add(position, contacts);
                                adapter.notifyItemInserted(position);
                                initialCount += 1;

                            }
                        })
                        .show();
            }

        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        adapter.SetOnItemClickListener(new ContactsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d("Main", "click");
                ipAddress=dataArray.get(position).ip_address;
                firstName =dataArray.get(position).first_name;
                lastName= dataArray.get(position).last_name;
                Snackbar.make(mRecyclerView, "Dzwonisz do: "+ipAddress+" "+firstName+" "+lastName, Snackbar.LENGTH_SHORT).show();
                modifyPos = position;
                showDialog(CALL_ADDRESS);
            }
        });



    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    public void initiateCall() { //funkcja wykonująca połączenie

        updateStatus("Dzwonisz do: "+ firstName + " " + lastName ,R.drawable.green_light);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int codec=Integer.parseInt(prefs.getString("codecPref","1"));//pobranie ustawien
        tc.makeCall(ipAddress,codec);
    }



    public void updateStatus(final String status,final int image) { //zmiana statusu
        this.runOnUiThread(new Runnable() {
            public void run() {
                TextView labelView = (TextView) findViewById(R.id.sipLabel);
                ImageView light= (ImageView) findViewById(R.id.light);
                light.setImageResource(image);
                labelView.setText(status);
            }
        });
    }



    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            updatePreferences();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case CALL_ADDRESS:

                LayoutInflater factory = LayoutInflater.from(this);
                final View textBoxView = factory.inflate(R.layout.call_address_dialog, null);
                return new AlertDialog.Builder(this)
                        .setTitle("Czy na pewno chcesz zadzwonić do: "+firstName+" "+ lastName+"?")
                        .setView(textBoxView)
                        .setPositiveButton(
                                android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                        initiateCall();

                                    }
                                })
                        .setNegativeButton(
                                android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                    }
                                })
                        .create();

        }
        return null;
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("modify", modifyPos);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        modifyPos = savedInstanceState.getInt("modify");
    }

    @Override
    protected void onResume() {
        super.onResume();
        final long newCount = Contacts.count(Contacts.class);

        if (newCount > initialCount) {
            Log.d("Main", "Adding new note");

            Contacts contacts = Contacts.last(Contacts.class);

            dataArray.add(contacts);
            adapter.notifyItemInserted((int) newCount);

            initialCount = newCount;
        }

        if (modifyPos != -1) {
            dataArray.set(modifyPos, Contacts.listAll(Contacts.class).get(modifyPos));
            adapter.notifyItemChanged(modifyPos);
        }

    }
    private Boolean exit = false;
    @Override
    public void onBackPressed() {
        if (exit) {
            finish(); // finish activity
        } else {
            Snackbar.make(mRecyclerView, "Naciśnij jeszcze raz aby zamknąć aplikację", Snackbar.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

    }
    public void updatePreferences() {
        Intent settingsActivity = new Intent(getBaseContext(),
                SettingsActivity.class);
        startActivity(settingsActivity);
    }

    public String getLocalIP()
    {
        try
        {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
            if(!ip.equals("0.0.0.0")) return ip;
        }
        catch(Exception e){e.printStackTrace();}
        return "brak polaczenia";
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode==1) //odebrane
        {
            tc.answer(data.getStringExtra("ip"));
            tc.checkBandwith(data.getStringExtra("ip"));
            Intent i = new Intent(getApplicationContext(), CallActivity.class);
            startActivityForResult(i, 0);


        }

        else //koniec polaczenia lub odrzucenie
        {
            Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            System.exit(0);
            startActivity(i);
        }
    }

}

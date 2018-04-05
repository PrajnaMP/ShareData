package com.mobinius.sharedatademo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by prajna on 22/8/17.
 */

public class CouchBaseActivity extends Activity implements Replication.ChangeListener {
    private EditText mName, mPlace;
    private Button mSubmit;

    //constants
    public static final String DATABASE_NAME = "airdrop";
    public static final String designDocName = "example-local";
    public static final String byDateViewName = "byDate";

    // By default, use the sync gateway running on the Couchbase demo server.
    // Warning: this will have "random data" entered by other users.
    // If you want to limit this to your own data, please install and run your own
    // Sync Gateway and point it to that URL instead.
    public static final String SYNC_URL = "http://192.168.2.7:5000/api/v1/data/airdrop";

    //couch internals

    protected static Manager manager;
    private Database database;
    private LiveQuery liveQuery;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_couchbase);

        mName = (EditText) findViewById(R.id.edit_text_name);
        mPlace = (EditText) findViewById(R.id.edit_text_place);
        mSubmit = (Button) findViewById(R.id.submit_button);


        try {
            startCBLite();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error Initializing CBLIte, see logs for details", Toast.LENGTH_LONG).show();
            Log.e("tag", "Error initializing CBLite", e);
        }


        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = mName.getText().toString();
                String inputText2 = mPlace.getText().toString();
                if (!inputText.equals("")) {
                    try {

                        if (inputText.contains(":")) {  // hack to create multiple items
//                            int numCreated = createMultipleGrocerySyncItems(inputText);
//                            String msg = String.format("Created %d new grocery items!", numCreated);
//                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                        } else {
                            createGroceryItem(inputText, inputText2);
                            Toast.makeText(getApplicationContext(), "Created new grocery item!", Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Error creating document, see logs for details", Toast.LENGTH_LONG).show();
                        Log.e("tag", "Error creating document.", e);
                    }
                }
                mName.setText("");
                mPlace.setText("");
            }
        });

    }

    protected void onDestroy() {
        if (manager != null) {
            manager.close();
        }
        super.onDestroy();
    }


    protected void startCBLite() throws Exception {

        Manager.enableLogging("tag", Log.VERBOSE);
        Manager.enableLogging(Log.TAG, Log.VERBOSE);
        Manager.enableLogging(Log.TAG_SYNC_ASYNC_TASK, Log.VERBOSE);
        Manager.enableLogging(Log.TAG_SYNC, Log.VERBOSE);
        Manager.enableLogging(Log.TAG_QUERY, Log.VERBOSE);
        Manager.enableLogging(Log.TAG_VIEW, Log.VERBOSE);
        Manager.enableLogging(Log.TAG_DATABASE, Log.VERBOSE);

        manager = new Manager(new AndroidContext(getApplicationContext()), Manager.DEFAULT_OPTIONS);

        //install a view definition needed by the application
        DatabaseOptions options = new DatabaseOptions();
        options.setCreate(true);
        database = manager.openDatabase(DATABASE_NAME, options);
        com.couchbase.lite.View viewItemsByDate = database.getView(String.format("%s/%s", designDocName, byDateViewName));
        viewItemsByDate.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                Object createdAt = document.get("created_at");
                if (createdAt != null) {
                    emitter.emit(createdAt.toString(), null);
                }
            }
        }, "1.0");

        startLiveQuery(viewItemsByDate);

        startSync();

    }

    private void startSync() {

        URL syncUrl;
        try {
            syncUrl = new URL(SYNC_URL);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        Replication pullReplication = database.createPullReplication(syncUrl);
        pullReplication.setContinuous(true);

        Replication pushReplication = database.createPushReplication(syncUrl);
        pushReplication.setContinuous(true);

        pullReplication.start();
        pushReplication.start();

        pullReplication.addChangeListener(this);
        pushReplication.addChangeListener(this);

    }

      private void startLiveQuery(com.couchbase.lite.View view) throws Exception {


           if (liveQuery == null) {

               liveQuery = view.createQuery().toLiveQuery();

               liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
                   public void changed(final LiveQuery.ChangeEvent event) {
                       runOnUiThread(new Runnable() {
                           public void run() {
   //                            grocerySyncArrayAdapter.clear();
   //                            for (Iterator<QueryRow> it = event.getRows(); it.hasNext();) {
   //                                grocerySyncArrayAdapter.add(it.next());
   //                            }
   //                            grocerySyncArrayAdapter.notifyDataSetChanged();
   //                            progressDialog.dismiss();
                           }
                       });
                   }
               });

               liveQuery.start();

           }

       }
    @Override
    public void changed(Replication.ChangeEvent event) {

        Replication replication = event.getSource();
        Log.d("tag", "Replication : " + replication + " changed.");
        if (!replication.isRunning()) {
            String msg = String.format("Replicator %s not running", replication);
            Log.d("tag", msg);
        } else {
            int processed = replication.getCompletedChangesCount();
            int total = replication.getChangesCount();
            String msg = String.format("Replicator processed %d / %d", processed, total);
            Log.d("tag", msg);
        }

        if (event.getError() != null) {
//            showError("Sync error", event.getError());
        }

    }

    private Document createGroceryItem(String text, String text2) throws Exception {

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        UUID uuid = UUID.randomUUID();
        Calendar calendar = GregorianCalendar.getInstance();
        long currentTime = calendar.getTimeInMillis();
        String currentTimeString = dateFormatter.format(calendar.getTime());

        String id = currentTime + "-" + uuid.toString();

        Document document = database.createDocument();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("_id", id);
        properties.put("name_", text);
        properties.put("place", text2);
        properties.put("check", Boolean.FALSE);
        properties.put("created_at", currentTimeString);
        document.putProperties(properties);

        Log.d("tag", "Created new  item with id: %s", document.getId());

        return document;
    }
}

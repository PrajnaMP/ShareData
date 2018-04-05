package com.mobinius.sharedatademo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by prajna on 23/8/17.
 */

public class SecondActivityForList extends Activity implements Replication.ChangeListener,
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnKeyListener {
    private static final int BUFFER = 1;
    int count = 0;

    public static String TAG = "GrocerySync";

    //constants
    public static final String DATABASE_NAME = "airdrop";
    public static final String designDocName = "grocery-local";
    public static final String byDateViewName = "byDate";

    // By default, use the sync gateway running on the Couchbase demo server.
    // Warning: this will have "random data" entered by other users.
    // If you want to limit this to your own data, please install and run your own
    // Sync Gateway and point it to that URL instead.
    public static final String SYNC_URL = "http://192.168.2.7:5000/api/v1/data/airdrop";

    String[] s = new String[2]; //declare an array for storing the files i.e the path of your source files


    protected EditText mName;
    protected EditText mPlace;
    private Button mSubmit, mShare;
    protected ListView itemListView;
    protected ItemSyncArrayAdapter grocerySyncArrayAdapter;

    protected static Manager manager;
    private Database database;
    private LiveQuery liveQuery;
    private static final int FILE_SELECT_CODE = 0;
    private ImageView mContainer;
    private Context context;


    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_couchbase);

        //connect items from layout
        mName = (EditText) findViewById(R.id.edit_text_name);
        mPlace = (EditText) findViewById(R.id.edit_text_place);
        mSubmit = (Button) findViewById(R.id.submit_button);
        mShare = (Button) findViewById(R.id.share_button);
        itemListView = (ListView) findViewById(R.id.itemListView);
        mContainer = (ImageView) findViewById(R.id.image_view);
        getFile("/data/data/com.mobinius.sharedatademo/files");


//        Toast.makeText(getApplicationContext(), "package" + getApplicationContext().getExternalFilesDir("aaa"), Toast.LENGTH_LONG).show();
//        Toast.makeText(getApplicationContext(), "directory" + getApplicationContext().getFilesDir(), Toast.LENGTH_LONG).show();
        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

/*                Bitmap thumbnail = (BitmapFactory.decodeFile("/storage/emulated/0/Pictures/Screenshots/Screenshot_2017-07-25-12-01-35.png"));
                Bitmap icon = thumbnail;

                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/jpeg");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                File f = new File(Environment.getExternalStorageDirectory() + File.separator +
                        "temporary_file.jpg");
                try {
                    f.createNewFile();
                    FileOutputStream fo = new FileOutputStream(f);
                    fo.write(bytes.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
                startActivity(Intent.createChooser(share, "Share Image"));*/

                Uri fileUri = Uri.parse("file:///sdcard/airdrop 7.cblite2.zip");
//                Uri fileUri = Uri.parse("/data/data/com.mobinius.shareappdemo/files/airdrop.cblite2");

//                s[0]="/data/data/com.mobinius.shareappdemo/files/airdrop.cblite2/attachments";    //Type the path of the files in here
//                s[1]="/data/data/com.mobinius.shareappdemo/files/airdrop.cblite2"; // path of the second file
//
//                zip((s),"file:///sdcard/MyZipFolder.zip");    //call the zip function

//                Uri fileUri = Uri.parse("/data/data/com.mobinius.shareappdemo/files/airdrop.cblite2");
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                shareIntent.setType("application/zip");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share files..."));


//                File dir = new File(Environment.getExternalStorageDirectory(),
//                        "mydir");
//                String zipName = dir.toString()+"zipname.zip";
//
//                String[] files = new String[] {dir.toString()+"/data/data/com.mobinius.shareappdemo/files/airdrop.cblite2/attachments", dir.toString()+"/data/data/com.mobinius.shareappdemo/files/airdrop.cblite2"};
//
//                Compress c = new Compress(files, zipName);
//                c.zip();


            }
        });


        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = mName.getText().toString();
                String inputText2 = mPlace.getText().toString();
                System.out.print(itemListView.getAdapter().getCount());
                Log.v("aaa", "" + itemListView.getAdapter().getCount());
//                Toast.makeText(getApplicationContext(), "Total number of Items are:" + itemListView.getAdapter().getCount(), Toast.LENGTH_LONG).show();

                if (!inputText.equals("")) {
                    try {

                        if (inputText.contains(":")) {  // hack to create multiple items
                            int numCreated = createMultipleGrocerySyncItems(inputText);
                            String msg = String.format("Created %d new grocery items!", numCreated);
//                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                        } else {
                            createGroceryItem(inputText, inputText2);
//                            Toast.makeText(getApplicationContext(), "Created new grocery item!", Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
//                        Toast.makeText(getApplicationContext(), "Error creating document, see logs for details", Toast.LENGTH_LONG).show();
                        Log.e("tag", "Error creating document.", e);
                    }
                }
                mName.setText("");
                mPlace.setText("");
            }
        });

        //connect listeners


        try {
            startCBLite();
        } catch (Exception e) {
//            Toast.makeText(getApplicationContext(), "Error Initializing CBLIte, see logs for details", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error initializing CBLite", e);
        }

    }

//    private void chooseFile() {
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("*/*");
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//
//        try {
//            startActivityForResult(
//                    Intent.createChooser(intent, "Select a File to Upload"),
//                    FILE_SELECT_CODE);
//        } catch (android.content.ActivityNotFoundException ex) {
//            // Potentially direct the user to the Market with a Dialog
//            Toast.makeText(this, "Please install a File Manager.",
//                    Toast.LENGTH_SHORT).show();
//        }
//    }

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));

                ImageView imageView = (ImageView) findViewById(R.id.image_view);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/


    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;
    }

    public void getClientList() {
        int macCount = 0;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null) {
                    // Basic sanity check
                    String mac = splitted[3];
                    System.out.println("Mac : Outside If " + mac);
                    if (mac.matches("..:..:..:..:..:..")) {
                        macCount++;
                   /* ClientList.add("Client(" + macCount + ")");
                    IpAddr.add(splitted[0]);
                    HWAddr.add(splitted[3]);
                    Device.add(splitted[5]);*/
                        System.out.println("Mac : " + mac + " IP Address : " + splitted[0]);
                        System.out.println("Mac_Count  " + macCount + " MAC_ADDRESS  " + mac);
                        Toast.makeText(
                                getApplicationContext(),
                                "Mac_Count  " + macCount + "   MAC_ADDRESS  "
                                        + mac, Toast.LENGTH_SHORT).show();

                    }
               /* for (int i = 0; i < splitted.length; i++)
                    System.out.println("Addressssssss     "+ splitted[i]);*/

                }
            }
        } catch (Exception e) {

        }
    }

    protected void onDestroy() {
        if (manager != null) {
            manager.close();
        }
        super.onDestroy();
    }

    protected void startCBLite() throws Exception {

        Manager.enableLogging(TAG, Log.VERBOSE);
        Manager.enableLogging(Log.TAG, Log.VERBOSE);
        Manager.enableLogging(Log.TAG_SYNC_ASYNC_TASK, Log.VERBOSE);
        Manager.enableLogging(Log.TAG_SYNC, Log.VERBOSE);
        Manager.enableLogging(Log.TAG_QUERY, Log.VERBOSE);
        Manager.enableLogging(Log.TAG_VIEW, Log.VERBOSE);
        Manager.enableLogging(Log.TAG_DATABASE, Log.VERBOSE);

        manager = new Manager(new AndroidContext(getApplicationContext()), Manager.DEFAULT_OPTIONS);

//        Toast.makeText(getApplicationContext(),manager.getDirectory().toString(),Toast.LENGTH_LONG).show();


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

        initItemListAdapter();

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

        final ProgressDialog progressDialog = showLoadingSpinner();

        if (liveQuery == null) {

            liveQuery = view.createQuery().toLiveQuery();

            liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
                public void changed(final LiveQuery.ChangeEvent event) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            grocerySyncArrayAdapter.clear();
                            for (Iterator<QueryRow> it = event.getRows(); it.hasNext(); ) {
                                grocerySyncArrayAdapter.add(it.next());
                            }
                            grocerySyncArrayAdapter.notifyDataSetChanged();
                            progressDialog.dismiss();
                        }
                    });
                }
            });
            liveQuery.start();

        }

    }

    private void initItemListAdapter() {
        grocerySyncArrayAdapter = new ItemSyncArrayAdapter(
                getApplicationContext(),
                R.layout.item_list,
                R.id.item_name,
                R.id.item_place,
                new ArrayList<QueryRow>()
        );
        itemListView.setAdapter(grocerySyncArrayAdapter);
        itemListView.setOnItemClickListener(SecondActivityForList.this);
        itemListView.setOnItemLongClickListener(SecondActivityForList.this);
    }


    private ProgressDialog showLoadingSpinner() {
        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.show();
        return progress;
    }


    /**
     * Handle typing item text
     */


    /**
     * Handle click on item in list
     */
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        QueryRow row = (QueryRow) adapterView.getItemAtPosition(position);
        Document document = row.getDocument();
        Map<String, Object> newProperties = new HashMap<String, Object>(document.getProperties());

        boolean checked = ((Boolean) newProperties.get("check")).booleanValue();
        newProperties.put("check", !checked);

        try {
            document.putProperties(newProperties);
            grocerySyncArrayAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error updating database, see logs for details", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error updating database", e);
        }

    }

    /**
     * Handle long-click on item in list
     */
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {

        QueryRow row = (QueryRow) adapterView.getItemAtPosition(position);
        final Document clickedDocument = row.getDocument();
        String itemText = (String) clickedDocument.getCurrentRevision().getProperty("text");

        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivityForList.this);
        AlertDialog alert = builder.setTitle("Delete Item?")
                .setMessage("Are you sure you want to delete \"" + itemText + "\"?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            clickedDocument.delete();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Error deleting document, see logs for details", Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Error deleting document", e);
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Handle Cancel
                    }
                })
                .create();

        alert.show();

        return true;
    }


    /**
     * Add settings item to the menu
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 0, 0, "Settings");
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Launch the settings activity
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                startActivity(new Intent(this, ItemSyncPreferencesActivity.class));
                return true;
        }
        return false;
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
        properties.put("name", text);
        properties.put("place", text2);
        properties.put("check", Boolean.FALSE);
        properties.put("created_at", currentTimeString);
        document.putProperties(properties);

        Log.d(TAG, "Created new grocery item with id: %s", document.getId());

        return document;
    }

    private int createMultipleGrocerySyncItems(String text) throws Exception {
        StringTokenizer st = new StringTokenizer(text, ":");
        String itemText = st.nextToken();
        String numItemsString = st.nextToken();
        int numItems = Integer.parseInt(numItemsString);
        for (int i = 0; i < numItems; i++) {
            String curItemText = String.format("%s-%d", itemText, i);
            createGroceryItem(curItemText, "111");
        }
        return numItems;
    }

    @Override
    public void changed(Replication.ChangeEvent event) {

        Replication replication = event.getSource();
        Log.d(TAG, "Replication : " + replication + " changed.");
        if (!replication.isRunning()) {
            String msg = String.format("Replicator %s not running", replication);
            Log.d(TAG, msg);
        } else {
            int processed = replication.getCompletedChangesCount();
            int total = replication.getChangesCount();
            String msg = String.format("Replicator processed %d / %d", processed, total);
            Log.d(TAG, msg);
        }

        if (event.getError() != null) {
            showError("Sync error", event.getError());
        }

    }

    public void showError(final String errorMessage, final Throwable throwable) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String msg = String.format("%s: %s", errorMessage, throwable);
                Log.e(TAG, msg, throwable);
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    private String getFile(String dirPath) {
        File f = new File(dirPath);
        File[] files = f.listFiles();

        if (files != null)
            for (int i = 0; i < files.length; i++) {
                count++;
                File file = files[i];

                if (file.isDirectory()) {
                    getFile(file.getAbsolutePath());
                    System.out.print("files" + getFile(file.getAbsolutePath()));
                    Toast.makeText(getApplicationContext(), "" + getFile(file.getAbsolutePath()), Toast.LENGTH_LONG).show();
                }

            }
        return dirPath;


    }






    public void zip(String[] files, String zipFile)
    {
      String[] _files= files;
        String _zipFile= zipFile;

        try  {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(_zipFile);

            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte data[] = new byte[BUFFER];

            for(int i=0; i < _files.length; i++) {
                Log.d("add:",_files[i]);
                Log.v("Compress", "Adding: " + _files[i]);
                FileInputStream fi = new FileInputStream(_files[i]);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch(Exception e) {
            e.printStackTrace();
        }}
    }

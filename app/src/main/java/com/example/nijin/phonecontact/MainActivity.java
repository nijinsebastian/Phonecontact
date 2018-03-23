package com.example.nijin.phonecontact;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private List<String> fileList=new ArrayList<String>();
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 10;
    ListView listView;
    Button sycbutton;
    String filePath=null;
    Button chfile;
    int i=1;
    JSONObject details = new JSONObject();

    JSONArray eachDes=new JSONArray();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView=(ListView)findViewById(R.id.listContacts);
        sycbutton=(Button)findViewById(R.id.sycbtn);
        chfile=(Button)findViewById(R.id.chfile);
        sycbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
String jsonformat=fetchContacts();
                Context context=v.getContext();

                writeToExternalStorage(v,jsonformat);
                System.out.println(jsonformat);

            }
        });
       chfile.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               selectPath();


             /*  File root=new File(Environment.getExternalStorageDirectory().getAbsolutePath());
               ListDir(root);  */
           }
       });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == RESULT_OK) {
            filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            System.out.println(filePath);
            String dataRetrived=readFile();
            parser(dataRetrived);
           /* String dataread=readFile(filePath);
            System.out.println(dataread);*/

            // Do anything with file
        }

    }

    public void insertionOfContacts(String DisplayName,String MobileNumber){

        ArrayList <ContentProviderOperation> ops = new ArrayList < ContentProviderOperation > ();

        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        //------------------------------------------------------ Names
        if (DisplayName != null) {
            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            DisplayName).build());
        }

        //------------------------------------------------------ Mobile Number
        if (MobileNumber != null) {
            ops.add(ContentProviderOperation.
                    newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, MobileNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());
        }

        //------------------------------------------------------ Home Numbers
     /*   if (HomeNumber != null) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, HomeNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
                    .build());
        }

        //------------------------------------------------------ Work Numbers
        if (WorkNumber != null) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, WorkNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
                    .build());
        }

        //------------------------------------------------------ Email
        if (emailID != null) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, emailID)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                    .build());
        }

        //------------------------------------------------------ Organization
        if (!company.equals("") && !jobTitle.equals("")) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, company)
                    .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                    .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, jobTitle)
                    .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                    .build());
        }*/

        // Asking the Contact provider to create a new contact
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            System.out.println("Succes");
        } catch (Exception e) {
            e.printStackTrace();
           // Toast.makeText(myContext, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }



    }

    public void parser(String dataRetrived){
        try {

            //Con":[{"Contact_ID":1,"Name":"+2 Ielts","Number":"+91 94 46 557058"}


            JSONObject retiveData=new JSONObject(dataRetrived);

            String status=retiveData.getString("Status");
            String num=retiveData.getString("Number of Contacts");
            int size=Integer.parseInt(num);
            System.out.println(size);
            JSONArray cont1;
            cont1=retiveData.getJSONArray("Con");
            System.out.println(cont1);
            JSONObject eachContact;
            String Contact_ID,Name,Number;
            for(int i=0;i<size;i++) {
                eachContact = cont1.getJSONObject(i);
                Contact_ID = eachContact.getString("Contact_ID");
                Name = eachContact.getString("Name");
                Number = eachContact.getString("Number");
                Boolean eq = false;
                for (int j = i+1; j < size; j++) {
                    eachContact = cont1.getJSONObject(j);
                    String Contact_ID1 = eachContact.getString("Contact_ID");
                    String nameName = eachContact.getString("Name");
                  //  System.out.println("Checking");
                    if (Name.equals(nameName)) {
                        System.out.println("Match :"+Contact_ID1+nameName);
                        eq = true;
                        break;
                    }
                }
                if (eq == false) {
                   insertionOfContacts(Name, Number);
                    System.out.println("Contact_ID: " + Contact_ID + " Name: " + Name + " Number: " + Number);
                }
            }
           // System.out.println("Status"+status+"Number"+num);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private String readFile() {
        String dataread=null;

        File file = new File(filePath);
        try {
            FileInputStream stream = new FileInputStream(file);

            int count;
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream byteStream =
                    new ByteArrayOutputStream(stream.available());

            while (true) {
                count = stream.read(buffer);
                if (count <= 0)
                    break;
                byteStream.write(buffer, 0, count);
            }

            dataread = byteStream.toString();
            System.out.format("%d bytes: \"%s\"%n", dataread.length(), dataread);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataread;
    }

    public void ListDir(File f){
      File[] file=f.listFiles();
fileList.clear();
        for(File files:file){
            fileList.add(files.getPath());
        }
            listView.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,fileList));



    }

    public void selectPath(){
        new MaterialFilePicker()
                .withActivity(MainActivity.this)
                .withRequestCode(1000)
                .withHiddenFiles(true) // Show hidden files and folders
                .start();
    }


    public void writeToExternalStorage(View view, String data){

       String state=Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            String filename = "Mymessage.txt";
            String path = Environment.getExternalStorageDirectory().getPath();
            System.out.println(path);
            File Root = Environment.getExternalStorageDirectory();
            File Dir = new File(Root.getAbsolutePath() + "/MyAppFile");
            if (!Dir.exists()) {
                Dir.mkdir();
            }

            File file = new File(Dir, filename);
            if (file.exists()){
                file.delete();
                file = new File(Dir, filename);
            }
                try {
                    FileOutputStream fileOutPutStream = new FileOutputStream(file);
                    fileOutPutStream.write(data.getBytes());
                    fileOutPutStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            Toast.makeText(getApplicationContext(), "Create File to SD", Toast.LENGTH_LONG).show();

        }
        else{
            Toast.makeText(getApplicationContext(),"No external Storage",Toast.LENGTH_LONG).show();
        }


    }



    private String fetchContacts() {
        ArrayList<String> contacts = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {





            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            String[] projection = null;
            String selection = "";
            String[] selectionArgs = null;
            String sortOrder = "DISPLAY_NAME ASC";

            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);
            System.out.println(cursor.getCount());
            try {
                details.put("Status", "OK");
                details.put("Number of Contacts", cursor.getCount());

            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String num = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                JSONObject mainDes = new JSONObject();
            /*    JSONObject duplitest;
                boolean t = false;
                for (int k = 1; k <= i; k++) {
                    duplitest = eachDes.getJSONObject(k);
                    if (name.equals(duplitest.getString("Name"))) {
                        t = true;
                        break;
                    }
                }*/
              //  if (t == false){
                    mainDes.put("Contact_ID", i);
                mainDes.put("Name", name);
                mainDes.put("Number", num);
                //Log.i("@contactlist", "Name : " + name + " Number : " + num);
                contacts.add(name + "\n" + num);

                listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contacts));
                i++;
                eachDes.put(mainDes);
          //  }

            }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                details.put("Con",eachDes);
            } catch (JSONException e) {
                e.printStackTrace();
            }



            //System.out.println("\nJSON String: " + details.toString());
        }
        return details.toString();

    }
}

package unice.com.smsanalysis;
import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.JsonReader;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;

public class MainActivity extends Activity {

    TextView textView;
    public String postContent;
    public int numberSmsToRead = 30;
    public Hashtable<Integer, ArrayList<String>> matrice = new Hashtable<Integer, ArrayList<String>>();
    private MobileServiceClient mClient;
    public class SmsTable {
        public String id;
        public String Text;
    }

    /*
    *   Private class HttpAsyncTask to do network things in background
    *   and set the content of the view.
     */
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return testRestHttp(urls);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Data received!", Toast.LENGTH_LONG).show();
            textView.setText(textView.getText() + "\n\nRestHttp tests :\n" + result);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.SMS);
        // call AsynTask to perform network operation on separate thread
        new HttpAsyncTask().execute("https://www.e-meta.fr/testjson.php", "https://www.e-meta.fr/test.json");
        getSMSDetails();

        // attempting to connect to azure mobile service

        /**
        try {
            mClient = new MobileServiceClient("https://smsanalysisapp.azurewebsites.net", this);
            SmsTable item = new SmsTable();
            item.Text = postContent;
            mClient.getTable(SmsTable.class).insert(item, new TableOperationCallback<SmsTable>() {
                public void onCompleted(SmsTable entity, Exception exception, ServiceFilterResponse response) {
                    if (exception == null) {
                        // Insert succeeded
                        Log.d("insert", "success");
                    } else {
                        // Insert failed
                        Log.d("insert", "fail");
                    }
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }**/

    }

    // Test our class Rest
    public String testRestHttp(String... urls) {
                // url[0] = url to test send
                // url[1] = url to test receive
                String urlSend = urls[0];
                String urlReceive = urls[1];

                // REST HTTP SENDER TO TEST SENDING DATA
                RestHttp sender = new RestHttp(urlSend);
                int result = sender.sendPostData("ok");
                Log.d("Result", Integer.toString(result));

                // REST HTTP RECEIVER TO TEST RECEIVED DATA
                RestHttp receiver = new RestHttp(urlReceive);
                JsonReader jsonReader = receiver.getData();
                // If the jsonReader is not null
                if(jsonReader != null) {
                    try {
                        jsonReader.beginObject();
                        while (jsonReader.hasNext()) {
                            String name = jsonReader.nextName();
                            if (name.equals("test")) {
                                String text = jsonReader.nextString();
                                Log.d("test", text);
                                return text;
                            } else {
                                jsonReader.skipValue();
                            }
                        }
                        jsonReader.endObject();

                    } catch (java.io.IOException e) {
                        Log.e("error", "io error");
                    }
                } else {
                    Log.e("ResultReceive", "error");
                }
                return "false";
            }

    // get name of the contact by passing the context and the number
    // In case no contact is associed with the number, return Unknown name.
    public String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri,
                new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);
        if (cursor == null) {
            return "Unknown";
        }
        String contactName = "Unknown";
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return contactName;
    }

    private Date getDate(long timeStamp){
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            Date netDate = (new Date(timeStamp));
            return netDate;
        }
        catch(Exception ex){
            return null;
        }
    }

    private int getDayOfWeek(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek;
    }

    // ! Week start Sunday in english
    private int dayOfWeek(int dayValue) {
        if(dayValue == 1 || dayValue == 7)
        {
            return 0;
        }
        else
        {
            return 1;
        }
    }

    // ! Week start Sunday in english
    private int dayOfWeekend(int dayValue) {
        if(dayValue == 1 || dayValue == 7)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    // Get details from SMS
    private void getSMSDetails() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Sms Analysis and creating matrix :");
        Uri uri = Uri.parse("content://sms");
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor.moveToFirst()) {
            // cursor.getCount();
            for (int i = 0; i < numberSmsToRead; i++) {
                String body = cursor.getString(cursor.getColumnIndexOrThrow("body")).toString();
                String number = cursor.getString(cursor.getColumnIndexOrThrow("address")).toString();
                String name = getContactName(this,number).toString();
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date")).toString();
                Date smsDayTime = new Date(Long.valueOf(date));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type")).toString();
                int dayWeek = dayOfWeek(getDayOfWeek(smsDayTime));
                int dayWeekend = dayOfWeekend(getDayOfWeek(smsDayTime));
                String typeOfSMS = null;
                switch (Integer.parseInt(type)) {
                    case 1:
                        typeOfSMS = "INBOX";
                        break;
                    case 2:
                        typeOfSMS = "SENT";
                        break;
                    case 3:
                        typeOfSMS = "DRAFT";
                        break;
                }

                //stringBuffer.append("\nPhone Number:--- " + number +" \nMessage Type:--- " + typeOfSMS +" \nMessage Date:--- " + smsDayTime);
                // stringBuffer.append("\n----------------------------------");
                cursor.moveToNext();

                // Add to matrix
                ArrayList<String> contenu = new ArrayList<String>();
                contenu.add(name);
                contenu.add(number);
                contenu.add(date);
                contenu.add(type);
                // if day of week
                contenu.add(Integer.toString(dayWeek));
                // if day of weekend
                contenu.add(Integer.toString(dayWeekend));
                // type of SMS
                contenu.add(typeOfSMS);

                matrice.put(i,contenu);

            }
            stringBuffer.append("\n Affichage de la matrice :\n" + matrice.toString());
            textView.setText(stringBuffer);
            postContent = matrice.toString();
            Log.i("test", matrice.toString());

        }
        cursor.close();
    }

}
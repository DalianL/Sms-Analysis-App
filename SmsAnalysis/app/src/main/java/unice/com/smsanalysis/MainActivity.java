package unice.com.smsanalysis;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;

public class MainActivity extends Activity {
    TextView textView;
    int anHour = 60000 * 120;
    private JobScheduler mJobScheduler;
    private Button mGoWebsiteButton;
    private Button mCancelAllJobsButton;
    /**
    *   Private class HttpAsyncTask to do network things in background
    *   and set the content of the view.
    **/
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
        mJobScheduler = (JobScheduler) getSystemService( Context.JOB_SCHEDULER_SERVICE );
        mGoWebsiteButton = (Button) findViewById( R.id.goWebsite );
        mCancelAllJobsButton = (Button) findViewById( R.id.stopService );

        // call AsynTask to perform network operation on separate thread
        // new HttpAsyncTask().execute("https://www.e-meta.fr/testjson.php", "https://www.e-meta.fr/test.json");

        // New builder for the service
        JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(getPackageName(), JobSchedulerService.class.getName()));
        // Doit être connecté au réseau
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        // Only with charging.
        builder.setRequiresCharging(true);
        // Each hour.
        builder.setPeriodic(anHour);
        // Stay after reboot
        builder.setPersisted(true);

        // Run the job
        if(mJobScheduler.schedule( builder.build() ) <= 0) {
            //If something goes wrong
            Log.d("error job", "job goes wrong");
        }

        textView.setText("SMS ANALYSIS APP");
        textView.setTextColor(Color.RED);

        // Click to stop the job service.
        mCancelAllJobsButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                mJobScheduler.cancelAll();
                Toast.makeText(getApplicationContext(), "Service stopped", Toast.LENGTH_SHORT).show();
            }
        });

        // Go to the website by button.
        mGoWebsiteButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                String url = "http://azureappelim.azurewebsites.net/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        // Go to the responsive website
        String url = "http://azureappelim.azurewebsites.net/";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
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
}
package unice.com.smsanalysis;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

public class JobSchedulerService extends JobService {
    public String postContent;
    public int numberSmsToRead = 1000;
    // Time in android is UNIX time in milliseconds not timestamp with seconds.
    public long oneMinute = 60 * 1000L;
    public long twoMinutes = oneMinute * 2;
    public long fiveMinutes = oneMinute * 5;
    public long tenMinutes = 600 * 1000L;
    public long twentyMinutes = tenMinutes * 2;
    public long thirtyMinutes = tenMinutes * 3;
    public long fourtyMinutes = tenMinutes * 4;
    public long fiftyMinutes = tenMinutes * 5;
    public long oneHour = tenMinutes * 6;
    public long oneHourAndHalf = tenMinutes * 9;
    public long twoHour = oneHour * 2;
    public long threeHour = oneHour * 3;
    public long fourHour = oneHour * 4;
    public long fiveHour = oneHour * 5;
    public long sixHour = oneHour * 6;
    public long twelveHour = oneHour * 12;
    public long oneDay = twelveHour * 2;
    public long twoDay = oneDay * 2;
    public long threeDay = oneDay * 3;
    public long fourDay = oneDay * 4;
    public long fiveDay = oneDay * 5;
    public long sixDay = oneDay * 6;
    public long oneWeek = oneDay * 7;
    public Hashtable<Integer, ArrayList<String>> matrice = new Hashtable<Integer, ArrayList<String>>();
    private MobileServiceClient mClient;
    public class SmsTable {
        public String id;
        public String Text;
    }

    // HANDLER
    private Handler mJobHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // Get details from sms
            getSmsDetails();
            // Update azure databse with informations
            sendSmsDetails();
            // Show message for the user when the update is done
            Toast.makeText(getApplicationContext(), "Sms database updated successfully", Toast.LENGTH_SHORT).show();
            jobFinished((JobParameters) msg.obj, false);
            return true;
        }
    });

    // START JOB
    @Override
    public boolean onStartJob(JobParameters params) {
        mJobHandler.sendMessage(Message.obtain(mJobHandler, 1, params));
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        mJobHandler.removeMessages(1);
        return false;
    }

    // Send details from SMS to azure database
    private void sendSmsDetails() {
        // attempting to connect to azure mobile service
        try {
            mClient = new MobileServiceClient("https://smsanalysisapp.azurewebsites.net", this);
            SmsTable item = new SmsTable();
            item.id = "3ea8aebb-27ab-44e4-88c7-0af7dd3dbfa5";
            item.Text = postContent;
            mClient.getTable(SmsTable.class).update(item, new TableOperationCallback<SmsTable>() {
                public void onCompleted(SmsTable entity, Exception exception, ServiceFilterResponse response) {
                    if (exception == null) {
                        // Insert succeeded
                        Log.d("update", "success");
                    } else {
                        // Insert failed
                        Log.d("update", "fail" + exception.toString());
                    }
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    // Get details from SMS
    private void getSmsDetails() {
        long startTime = System.currentTimeMillis();
        // Sms HashTable
        Sms sms = new Sms();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Sms Analysis and creating matrix :");
        Uri uri = Uri.parse("content://sms");
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        // Move with cursor
        if (cursor.moveToFirst()) {
            // cursor.getCount();
            // If we haven't enough sms in database
            if(cursor.getCount() < numberSmsToRead) {
                numberSmsToRead = cursor.getCount();
            }
            for (int i = 0; i < numberSmsToRead; i++) {
                String body = cursor.getString(cursor.getColumnIndexOrThrow("body")).toString();
                int nbrCaracters = body.length();
                String number = cursor.getString(cursor.getColumnIndexOrThrow("address")).toString();
                String name = UserManagement.getContactName(this,number).toString();
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date")).toString();
                Date smsDayTime = new Date(Long.valueOf(date));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type")).toString();
                int dayWeek = DateControl.dayOfWeek(DateControl.getDayOfWeek(smsDayTime));
                int dayWeekend = DateControl.dayOfWeekend(DateControl.getDayOfWeek(smsDayTime));
                String typeOfSMS = null;
                // Calculate hour of day using calendar instance
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(Long.parseLong(date));
                int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
                // Switch for type of SMS
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
                // Date in UNIX FORMAT (milliseconds)
                contenu.add(date);
                // Type of SMS
                contenu.add(typeOfSMS);
                // PART DAY/HOUR IN A NICHE
                // if day of week
                contenu.add(Integer.toString(dayWeek));
                // if day of weekend
                contenu.add(Integer.toString(dayWeekend));
                // nbr caracters
                contenu.add(Integer.toString(nbrCaracters));
                // hour 0h - 5h
                contenu.add(Integer.toString(DateControl.isNight(hourOfDay)));
                // hour 6h - 10h
                contenu.add(Integer.toString(DateControl.isMorning(hourOfDay)));
                // hour 11h - 14h
                contenu.add(Integer.toString(DateControl.isMidday(hourOfDay)));
                // hour 15h - 18h
                contenu.add(Integer.toString(DateControl.isAfternoon(hourOfDay)));
                // hour 19h - 23h
                contenu.add(Integer.toString(DateControl.isEvening(hourOfDay)));
                // Add sms to user in hashmap sms
                sms.addSmsToUser(name, Long.parseLong(date), nbrCaracters);
                // PART FILTER AND CACULATE AVERAGE AND NUMBER OF SMS WITH A DISTANCE OF TIME
                // Filter (1 minute), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+oneMinute,Long.parseLong(date), name); contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Filter (2 minutes), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+twoMinutes,Long.parseLong(date), name); contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Filter (5 minutes), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+fiveMinutes,Long.parseLong(date), name); contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Filter (10 minutes), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+tenMinutes,Long.parseLong(date), name); contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Filter (20 minutes), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+twentyMinutes,Long.parseLong(date), name);  contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Filter (30 minutes), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+thirtyMinutes,Long.parseLong(date), name); contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Filter (40 minutes), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+fourtyMinutes,Long.parseLong(date), name); contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Filter (50 minutes), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+fiftyMinutes,Long.parseLong(date), name); contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Filter (1 hour), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+oneHour,Long.parseLong(date), name); contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Filter (1 hour and a half), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+oneHourAndHalf,Long.parseLong(date), name); contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Filter (2 hours), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+twoHour,Long.parseLong(date), name); contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Filter (3 hours), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+threeHour,Long.parseLong(date), name); contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Filter (4 hours), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+fourHour,Long.parseLong(date), name); contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Filter (5 hours), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+fiveHour,Long.parseLong(date), name); contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Filter (6 hours), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+sixHour,Long.parseLong(date), name); contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Filter (12 hours), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+twelveHour,Long.parseLong(date), name); contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Filter (1 day), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+oneDay,Long.parseLong(date), name); contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Filter (2 day), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+twoDay,Long.parseLong(date), name); contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Filter (3 day), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+threeDay,Long.parseLong(date), name); contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Filter (4 day), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+fourDay,Long.parseLong(date), name); contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Filter (5 day), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+fiveDay,Long.parseLong(date), name); contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Filter (6 day), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+sixDay,Long.parseLong(date), name); contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Filter (1 week), calculate the average of words and the number of messages.
                sms.filterSmsByTime(Long.parseLong(date)+oneWeek,Long.parseLong(date), name); contenu.add(sms.getCountSms()+""); contenu.add(sms.getAverageWords()+"");
                // Add sms vector to matrix
                matrice.put(i,contenu);
            }
            stringBuffer.append("\n Affichage de la matrice :\n" + matrice.toString());
            postContent = matrice.toString();
            Log.i("Matrice", matrice.toString());
            long endTime   = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            Log.d("total time :", totalTime+"");
        }
        cursor.close();
    }
}
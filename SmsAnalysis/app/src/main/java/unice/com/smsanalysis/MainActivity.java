package unice.com.smsanalysis;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {

    TextView textView;
    public int numberSmsToRead = 4;
    public Hashtable<Integer, ArrayList<String>> matrice = new Hashtable<Integer, ArrayList<String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.SMS);
        getSMSDetails();

    }

    private void getSMSDetails() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Sms Analysis and creating matrix :");
        Uri uri = Uri.parse("content://sms");
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor.moveToFirst()) {
            // cursor.getCount();
            for (int i = 0; i < this.numberSmsToRead; i++) {
                String body = cursor.getString(cursor.getColumnIndexOrThrow("body"))
                        .toString();
                String number = cursor.getString(cursor.getColumnIndexOrThrow("address"))
                        .toString();
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                        .toString();
                Date smsDayTime = new Date(Long.valueOf(date));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"))
                        .toString();
                String typeOfSMS = null;
                switch (Integer.parseInt(type))
                {
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

                stringBuffer.append("\nPhone Number:--- " + number +
                        " \nMessage Type:--- " + typeOfSMS +
                        " \nMessage Date:--- " + smsDayTime);
                stringBuffer.append("\n----------------------------------");
                cursor.moveToNext();

                // Add to matrix
                ArrayList<String> contenu = new ArrayList<String>();
                contenu.add(number);
                contenu.add(date);
                contenu.add(type);
                contenu.add(typeOfSMS);

                matrice.put(i,contenu);

            }
            stringBuffer.append("\n Affichage de la matrice :\n" + matrice.toString());
            textView.setText(stringBuffer);
            Log.i("test", matrice.toString());

        }
        cursor.close();
    }

}
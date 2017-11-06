package unice.com.smsanalysis;
import android.util.JsonReader;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Hashtable;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class RestHttp {

    private String url;

    RestHttp(String url) {
        this.url = url;
    }

    public JsonReader getData() {
        // Create URL
        try {
            URL githubEndpoint = new URL(this.url);
            // Create connection
            try {
                HttpsURLConnection myConnection = (HttpsURLConnection) githubEndpoint.openConnection();

                if (myConnection.getResponseCode() == 200) {
                    // Success
                    InputStream responseBody = myConnection.getInputStream();
                    InputStreamReader responseBodyReader =  new InputStreamReader(responseBody, "UTF-8");
                    JsonReader jsonReader = new JsonReader(responseBodyReader);

                    myConnection.disconnect();

                    return jsonReader;
                } else {
                    // Error handling code goes here
                    Log.d("Response", "not ok");
                    return null;
                }
            } catch (java.io.IOException e2)
            {
                Log.e("Error", "Incorrect IO.");
                return null;
            }
        } catch (java.net.MalformedURLException e1)
        {
            Log.e("Error", "Incorrect Url.");
        }
        return null;
    }

    /*
    * With an hashtable of data, send all information to url.
    * return 1 if ok
    * 0 otherwise
     */
    public int sendPostData(Hashtable<Integer, ArrayList<String>> data)
    {
        return 1;
    }

    public int sendPostData(String data)
    {
        // Create URL
        try {
            URL urlToReach = new URL(this.url);
            // Create connection
            try {
                HttpsURLConnection myConnection = (HttpsURLConnection) urlToReach.openConnection();
                // Send data by POST
                myConnection.setRequestMethod("POST");
                // Create the data
                String myData = "data1="+data;

                // Enable writing
                myConnection.setDoOutput(true);

                // Write the data
                myConnection.getOutputStream().write(myData.getBytes());

                if (myConnection.getResponseCode() == 200) {
                    // Success
                    InputStream responseBody = myConnection.getInputStream();
                    InputStreamReader responseBodyReader =  new InputStreamReader(responseBody, "UTF-8");
                    JsonReader jsonReader = new JsonReader(responseBodyReader);

                    jsonReader.beginObject(); // Start processing the JSON object
                    while (jsonReader.hasNext()) { // Loop through all keys
                        String key = jsonReader.nextName(); // Fetch the next key
                        // IF OK IS RETURNED WE CORRECTLY SEND OUR DATA TO THE URL BY POST
                        if (key.equals("ok")) { // Check if desired key
                            // Fetch the value as a Array
                            //Array values = jsonReader.beginArray();
                            //Log.d("s", jsonReader);
                            Log.d("JSON", "Json ok");
                            return 1;

                        }
                        else if(key.equals("notok")) {
                            Log.d("JSON", "Json not ok");
                            return 0;
                        }
                        else {
                            jsonReader.skipValue(); // Skip values of other keys
                        }
                    }
                    // Close the stream
                    jsonReader.close();
                    // Close connection
                    myConnection.disconnect();
                } else {
                    // Error handling code goes here
                    Log.d("Response", "not ok");
                    return 0;
                }
            } catch (java.io.IOException e2)
            {
                Log.e("Error", "Incorrect IO.");
                return 0;
            }
        } catch (java.net.MalformedURLException e1)
        {
            Log.e("Error", "Incorrect Url.");
        }
        return 0;
    }


}
package unice.com.smsanalysis;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Matthieu on 23/01/2018.
 */

public class Sms {

    HashMap<String, HashMap<Long, Integer>> sms;
    HashMap<Long, Integer> currentFilterTable;

    Sms()
    {
        sms = new HashMap<String, HashMap<Long, Integer>>();
        currentFilterTable = new HashMap<Long, Integer>();
    }

    public void addUser(String user) {
        if(!sms.containsKey(user))
        {
            sms.put(user, new HashMap<Long, Integer>());
        }
    }

    /**
     * @param timePlusFilter : Time of the current sms selected + filter
     * @param timeSms : Time of the current sms selected
     * @param user : Name of the user
     * @return void
     */
    public HashMap<Long, Integer> filterSmsByTime(long timePlusFilter, long timeSms, String user) {;
        HashMap<Long, Integer> tableToReturn = new HashMap<Long, Integer>();
        HashMap<Long, Integer> infosUser = this.getSmsfromUser(user);
        if(infosUser == null)
        {
            return  tableToReturn;
        }
        Set<Long> lst = infosUser.keySet();
        Iterator<Long> i = lst.iterator();
        while (i.hasNext()) {
            long c = i.next();
            // Time of the selected sms must be inside [timeSmsToCompare, timeSmsToComparePlusFilter]
            if ((c <= timePlusFilter) && (c >= timeSms)) {
                tableToReturn.put(c, infosUser.get(c));
            }
        }
        this.currentFilterTable = tableToReturn;
        return tableToReturn;
    }

    // Get average words of current sms table from an user
    public int getAverageWords()
    {
        int sum = 0;
        int numberElements = 0;
        int average = 0;

        for (Integer entry: currentFilterTable.values()) {
            sum += entry;
            numberElements++;
            average = sum / numberElements;
        }

        if(sum == 0 || numberElements == 0) {
            return 0;
        }
        else {
            return average;
        }
    }

    // Get number of elements in current sms table from an user
    public int getCountSms() {
        if(currentFilterTable == null)
        {
            return 0;
        }
        else
        {
            return currentFilterTable.size();
        }
    }

    public HashMap<String, HashMap<Long, Integer>> getSms()
    {
        return sms;
    }

    public HashMap<Long, Integer> getSmsfromUser(String user)
    {
        if(sms.containsKey(user)) {
            return sms.get(user);
        }
        else
        {
            return null;
        }
    }

    public void addSmsToUser(String user, long timestamp, int countWords) {
        this.addUser(user);
        HashMap<Long, Integer> smsFromU = this.getSmsfromUser(user);
        smsFromU.put(timestamp, countWords);
    }
}

package unice.com.smsanalysis;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Matthieu on 23/01/2018.
 */

public class Sms {

    HashMap<String, HashMap<Long, Integer>> sms;

    Sms()
    {
        sms = new HashMap<String, HashMap<Long, Integer>>();
    }

    public void addUser(String user) {
        if(!sms.containsKey(user))
        {
            sms.put(user, new HashMap<Long, Integer>());
        }
    }

    /**
    public static Map<Integer, Integer> removeSmsByTime(int time, Map<String, Map<Integer, Integer>> a, String user) {
        Map<Integer, Integer> lstToReturn = new HashMap<Integer, Integer>();

        Map<Integer, Integer> infosUser = a.get(user);
        Set<Integer> lst = infosUser.keySet();
        Iterator<Integer> i = lst.iterator();
        while (i.hasNext()) {
            int c = i.next();
            if (c > time) {
                lstToReturn.put(c, infosUser.get(c));
            }
        }
        return lstToReturn;
    }
     **/

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

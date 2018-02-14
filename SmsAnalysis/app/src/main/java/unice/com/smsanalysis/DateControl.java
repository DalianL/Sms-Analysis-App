package unice.com.smsanalysis;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Date control for SMS Analysis.
 */

public class DateControl {

    public static Date getDate(long timeStamp){
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            Date netDate = (new Date(timeStamp));
            return netDate;
        }
        catch(Exception ex){
            return null;
        }
    }

    public static int getDayOfWeek(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek;
    }

    // ! Week start Sunday in english
    public static int dayOfWeek(int dayValue) {
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
    public static int dayOfWeekend(int dayValue) {
        if(dayValue == 1 || dayValue == 7)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public static int isNight(int hour) {
        if(hour >= 0 && hour <= 5)
        {
            return 1;
        }
        return 0;
    }

    public static int isMorning(int hour) {
        if(hour >= 6 && hour <= 10)
        {
            return 1;
        }
        return 0;
    }

    public static int isMidday(int hour) {
        if(hour >= 11 && hour <= 14)
        {
            return 1;
        }
        return 0;
    }

    public static int isAfternoon(int hour) {
        if(hour >= 15 && hour <= 18)
        {
            return 1;
        }
        return 0;
    }

    public static int isEvening(int hour) {
        if(hour >= 19 && hour <= 23)
        {
            return 1;
        }
        return 0;
    }
}
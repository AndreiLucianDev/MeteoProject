package app.com.dev.andreilucian.meteopoject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * *MODEL*
 *
 * Created by andrei on 26.04.2016.
 * This class represents one day's weather data
 */
public class Weather {

    public final String dayOfWeek;
    public final String minTemp;
    public final String maxTemp;
    public final String humidity;
    public final String description;
    public final String iconUrl;

    //costruttor
    public Weather(long timeStamp, double minTemp, double maxTemp, double humidity,
                   String description, String iconName){

        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(0);

        this.dayOfWeek = convertTimeStampToDay(timeStamp);
        this.minTemp = numberFormat.format(minTemp) + "\u00B0C";
        this.maxTemp = numberFormat.format(maxTemp) + "\u00B0C";
        this.humidity = NumberFormat.getPercentInstance().format(humidity / 100);
        this.description = description;
        this.iconUrl = "http://openweathermap.org/img/w/" + iconName + ".png";
    }

    //riceves a long value and return a day-name string
    private static String convertTimeStampToDay(long timeStamp) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp * 1000);         //timestamp is in seconds
        TimeZone tz = TimeZone.getDefault();                //get device's time zone

        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE");
        return dateFormat.format(calendar.getTime());
    }




}

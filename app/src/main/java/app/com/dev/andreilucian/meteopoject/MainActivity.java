package app.com.dev.andreilucian.meteopoject;

/**
 * *VIEW*
 *
 * this class displays a 16-dayOfWeek weather forecast for the specified city
 * */

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //List of Weather objects representing the forecast
    private List<Weather> weatherList = new ArrayList<>();

    //ArrayAdapter for binding Weather objects to a ListView
    private WeatherArrayAdapter weatherArrayAdapter;
    private ListView weatherListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // create ArrayAdapter to bind weatherList to the weatherListView
        weatherListView = (ListView) findViewById(R.id.weatherListView);
        weatherArrayAdapter = new WeatherArrayAdapter(this, weatherList);
        weatherListView.setAdapter(weatherArrayAdapter);

        // configure FAB to hide keyboard and initiate web service request
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get text from locationEditText and create web service URL
                EditText locationEditText =
                        (EditText) findViewById(R.id.locationEditText);
                URL url = createURL(locationEditText.getText().toString());

                // hide keyboard and initiate a GetWeatherTask to download
                // weather data from OpenWeatherMap.org in a separate thread
                if (url != null) {
                    dismissKeyboard(locationEditText);
                    GetWeatherTask getLocalWeatherTask = new GetWeatherTask();
                    getLocalWeatherTask.execute(url);
                } else {
                    Snackbar.make(findViewById(R.id.coordinatorLayout),
                            R.string.invalid_url, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    // programmatically dismiss keyboard when user touches FAB
    private void dismissKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // create openweathermap.org web service URL using city
    private URL createURL(String city) {
        String apiKey = getString(R.string.api_key);
        String baseUrl = getString(R.string.web_service_url);
        try {
            // create URL for specified city and imperial units (Celsius)
            String urlString = baseUrl + URLEncoder.encode(city, "UTF-8") +
                    "&units=metric&cnt=16&APPID=" + apiKey;
            return new URL(urlString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null; // URL was malformed
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // create Weather objects from JSONObject containing the forecast
    private void convertJSONArrayList(JSONObject forecast) {

        //clear old data
        weatherList.clear();

        try{
            JSONArray list = forecast.getJSONArray("list");

            // convert each element of list to a Weather object
            for (int i = 0; i < list.length() ; ++i) {
                JSONObject day = list.getJSONObject(i); // get one day's data
                // get the day's temperatures ("temp") JSONObject
                JSONObject temperatures = day.getJSONObject("temp");
                // get day's "weather" JSONObject for the description and icon
                JSONObject weather =
                        day.getJSONArray("weather").getJSONObject(0);
                // add new Weather object to weatherList
                weatherList.add(new Weather(
                        day.getLong("dt"), // date/time timestamp
                        temperatures.getDouble("min"), // minimum temperature
                        temperatures.getDouble("max"), // maximum temperature
                        day.getDouble("humidity"), // percent humidity
                        weather.getString("description"), // weather conditions
                        weather.getString("icon"))); // icon name
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class GetWeatherTask extends AsyncTask<URL, Void, JSONObject>{
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected JSONObject doInBackground(URL... params) {
            //save data to a local html file
            HttpURLConnection connection = null;

            try{
                connection = (HttpURLConnection) params[0].openConnection();
                int response = connection.getResponseCode();

                if (response == HttpURLConnection.HTTP_OK){
                    StringBuilder stringBuilder = new StringBuilder();

                    try(BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()))){
                        String line;

                        while((line = reader.readLine()) != null){
                            stringBuilder.append(line);
                        }
                    }catch (IOException e){
                        Snackbar.make(findViewById(R.id.coordinatorLayout),
                                R.string.read_error, Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    return new JSONObject(stringBuilder.toString());
                }else{
                    Snackbar.make(findViewById(R.id.coordinatorLayout),
                            R.string.connect_error, Snackbar.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                Snackbar.make(findViewById(R.id.coordinatorLayout),
                        R.string.connect_error, Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }finally {
                connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject weather) {
            convertJSONArrayList(weather);
            weatherArrayAdapter.notifyDataSetChanged();
            weatherListView.smoothScrollToPosition(0);
        }
    }
}

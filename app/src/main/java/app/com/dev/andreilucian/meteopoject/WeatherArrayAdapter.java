package app.com.dev.andreilucian.meteopoject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * *CONTROLER*
 *
 * Created by andrei on 26.04.2016.
 */
public class WeatherArrayAdapter extends ArrayAdapter<Weather> {

    // stores already downloaded Bitmaps for reuse
    private Map<String, Bitmap> bitmaps = new HashMap<>();

    public WeatherArrayAdapter(Context context, List<Weather> forecast) {
        super(context, -1, forecast);           // -1 because I use list_item.xml
    }

    // creates the custom views for the ListView's items
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get Weather object for this specified ListView position
        Weather day = getItem(position);

        ViewHolder viewHolder; // object that reference's list item's views

        // check for reusable ViewHolder from a ListView item that scrolled
        // offscreen; otherwise, create a new ViewHolder
        if (convertView == null) { // no reusable ViewHolder, so create one
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView =
                    inflater.inflate(R.layout.list_item, parent, false);
            viewHolder.conditionImageView =
                    (ImageView) convertView.findViewById(R.id.condtionImageView);
            viewHolder.dayTextView =
                    (TextView) convertView.findViewById(R.id.dayTextView);
            viewHolder.lowTextView =
                    (TextView) convertView.findViewById(R.id.lowTextView);
            viewHolder.hiTextView =
                    (TextView) convertView.findViewById(R.id.hiTextView2);
            viewHolder.humidityTextView =
                    (TextView) convertView.findViewById(R.id.humidityTextView3);
            convertView.setTag(viewHolder);
        }
        else { // reuse existing ViewHolder stored as the list item's tag
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // if weather condition icon already downloaded, use it;
        // otherwise, download icon in a separate thread
        if (bitmaps.containsKey(day.iconUrl)) {
            viewHolder.conditionImageView.setImageBitmap(
                    bitmaps.get(day.iconUrl));
        }
        else {
        // download and display weather condition image
            new LoadImageTask(viewHolder.conditionImageView).execute(
                    day.iconUrl);
        }

        // get other data from Weather object and place into views
        Context context = getContext(); // for loading String resources
        viewHolder.dayTextView.setText(context.getString(
                R.string.day_description, day.dayOfWeek, day.description));
        viewHolder.lowTextView.setText(
                context.getString(R.string.low_temp, day.minTemp));
        viewHolder.hiTextView.setText(
                context.getString(R.string.high_temp, day.maxTemp));
        viewHolder.humidityTextView.setText(
                context.getString(R.string.humidity, day.humidity));
        return convertView; // return completed list item to display
    }

    // class for reusing views as list items scroll off and onto the screen
    private static class ViewHolder {
        ImageView conditionImageView;
        TextView dayTextView;
        TextView lowTextView;
        TextView hiTextView;
        TextView humidityTextView;
    }

    // AsyncTask to load weather condition icons in a separate thread
    private class LoadImageTask extends AsyncTask<String, Void, Bitmap>{

        ImageView imageView;

        public LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            HttpURLConnection connection = null;

            try{

                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();

                try(InputStream inputStream = connection.getInputStream()){

                    bitmap = BitmapFactory.decodeStream(inputStream);
                    bitmaps.put(params[0], bitmap);         //cache for later use

                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                connection.disconnect();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }
}


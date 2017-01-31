package com.robynsilber.weather_forecast.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.robynsilber.weather_forecast.R;
import com.robynsilber.weather_forecast.model.Weather;
import com.squareup.picasso.Picasso;

public class WeatherAdapter extends BaseAdapter {

    private Context mContext;
    private Weather[] mWeathers;

    public WeatherAdapter(Context context, Weather[] weathers){
        mContext = context;
        mWeathers = weathers;

    }


    /* Gets the count of items in the array that this adapter is using (size of mWeathers array) */
    @Override
    public int getCount() {
        return mWeathers.length;
    }

    /* Gets the item for the adapter at position i */
    @Override
    public Object getItem(int i) {
        return mWeathers[i];
    }

    /* Allows for tagging items so that they can be easily referenced */
    @Override
    public long getItemId(int i) {
        return 0; // not implementing at the moment
    }

    /* The method called for each item in the list. This method is called when each view is being
     * prepared to be shown on the screen. That means, it's called for everything that's initially
     * displayed each time the user scrolls to a new item in the list (using RecyclerView). */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // sets the views that will be provided to the adapter.
        /* convertView is the view object that we want to reuse. First time called: will be null.
         * If null, create the View and set it up; otherwise, reuse and reset the data. */

        // ViewHolder allows for efficient scrolling of views
        ViewHolder holder;

        if(convertView == null){ // new view; create it using LayoutInflater
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_daily_weather, null);
                                // null is a ViewGroup root, which we can leave out here, hence null.
            holder = new ViewHolder(); // holder
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.iconImageView);
            holder.temperatureLabel = (TextView) convertView.findViewById(R.id.temperatureLabel);
            holder.dayLabel = (TextView) convertView.findViewById(R.id.dayLabel);
            holder.weatherDescrLabel = (TextView)convertView.findViewById(R.id.weatherDescrLabel);

            // Sets a tag for the View that will be reused below
            convertView.setTag(holder);
        }else{
            // bc the holder is already associated with a view, we can call getTag()
            holder = (ViewHolder) convertView.getTag();
        }

        // Set the Weather data
        Weather weather = mWeathers[position];

        String url = weather.getIconUrl();
        // 3rd party library for downloading image icons: http://square.github.io/picasso/
        Picasso.with(mContext).load(url).into(holder.iconImageView);
//        holder.iconImageView.setImageResource(weather.getIconUrl(weather.getIcon...));
        holder.weatherDescrLabel.setText(weather.getDescription());
        holder.temperatureLabel.setText(weather.getTemperatureHi() + "");
        holder.dayLabel.setText(weather.getDay());

        return convertView;
    }

    /* Define ViewHolder helper class, which defines the template for creating a helper object that
     * is associated with a view. It allows us to re-use the same references to objects in the view,
     * such as the text views and image views. */
    private static class ViewHolder{
        // Members: Note that 'm' prefix is intentionally left out here for readability
        ImageView iconImageView;
        TextView temperatureLabel;
        TextView dayLabel;
        TextView weatherDescrLabel;
    }

}

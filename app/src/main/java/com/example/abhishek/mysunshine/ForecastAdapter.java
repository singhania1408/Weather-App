package com.example.abhishek.mysunshine;



        import android.content.Context;
        import android.database.Cursor;
        import android.support.v4.widget.CursorAdapter;
        import android.support.v7.widget.RecyclerView;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.TextView;

        import com.example.abhishek.mysunshine.data.Weathercontract;
    public class ForecastAdapter extends CursorAdapter {
        public static class ViewHolder {
            public final ImageView iconView;
            public final TextView dateView;
            public final TextView descriptionView;
            public final TextView highTempView;
            public final TextView lowTempView;

            public ViewHolder(View view) {
                iconView = (ImageView) view.findViewById(R.id.list_item_icon);
                dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
                descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
                highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
                lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
            }
        }
    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }
   /* private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
        return highLowStr;
    }


    private String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor

        String highAndLow = formatHighLows(
                cursor.getDouble(weatherlist_fragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(weatherlist_fragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(weatherlist_fragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(weatherlist_fragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
*/
    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

        private boolean mUseTodayLayout = true;
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
               int layoutId = -1;
               switch (viewType) {
                        case VIEW_TYPE_TODAY: {
                               layoutId = R.layout.list_item_forecast_today;
                               break;
                            }
                        case VIEW_TYPE_FUTURE_DAY: {
                                layoutId = R.layout.listitem_forecast;
                                break;
                            }
                    }
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

                        ViewHolder viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);

                        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        // Use placeholder image for now
        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                // Get weather icon
                viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(
                        cursor.getInt(weatherlist_fragment.COL_WEATHER_CONDITION_ID)));
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                // Get weather icon
                viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(
                        cursor.getInt(weatherlist_fragment.COL_WEATHER_CONDITION_ID)));
                break;
            }
        }

        // Read date from cursor
        long dateInMillis = cursor.getLong(weatherlist_fragment.COL_WEATHER_DATE);
        // Find TextView and set formatted date on it
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));

        // Read weather forecast from cursor
        String description = cursor.getString(weatherlist_fragment.COL_WEATHER_DESC);
        // Find TextView and set weather forecast on it
        viewHolder.descriptionView.setText(description);

        viewHolder.iconView.setContentDescription(description);
        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        double high = cursor.getDouble(weatherlist_fragment.COL_WEATHER_MAX_TEMP);
        viewHolder.highTempView.setText(Utility.formatTemperature(context,high, isMetric));

        // Read low temperature from cursor
        double low = cursor.getDouble(weatherlist_fragment.COL_WEATHER_MIN_TEMP);
        TextView lowView = (TextView) view.findViewById(R.id.list_item_low_textview);
        viewHolder.lowTempView.setText(Utility.formatTemperature(context,low, isMetric));
    }
        public void setUseTodayLayout(boolean useTodayLayout) {
                   mUseTodayLayout = useTodayLayout;
               }
        @Override
        public int getItemViewType(int position) {
                   return( position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
               }

        @Override
           public int getViewTypeCount() {
            return VIEW_TYPE_COUNT;
               }
}
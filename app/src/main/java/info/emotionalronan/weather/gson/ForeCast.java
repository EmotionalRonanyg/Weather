package info.emotionalronan.weather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by YG on 2017/4/27.
 */

public class ForeCast {
    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class Temperature{

        public String max;
        public String min;
    }

    public class More{
        @SerializedName("txt")
        public String info;
    }
}

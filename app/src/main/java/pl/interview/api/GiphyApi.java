package pl.interview.api;

import pl.interview.api.data.Gifs;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GiphyApi {

        String API_URL = "https://api.giphy.com/";
        String API_KEY_PARAM = "api_key";

        @GET("v1/gifs/trending")
        Call<Gifs> listTrending(@Query("limit") int limit);
}

package pl.interview;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.interview.api.GiphyApi;
import pl.interview.api.data.Gif;
import pl.interview.api.data.Gifs;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static pl.interview.api.GiphyApi.API_KEY_PARAM;

public class MainActivity extends AppCompatActivity {

    private static final int GIFS_LIMIT = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final GifAdapter gifAdapter = new GifAdapter(this);

        ((GridView) findViewById(R.id.main_grid))
                .setAdapter(gifAdapter);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
            .addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                HttpUrl url = original.url()
                        .newBuilder()
                        .addQueryParameter(API_KEY_PARAM, getString(R.string.giphy_api_key))
                        .build();
                Request.Builder requestBuilder = original.newBuilder()
                        .url(url);
                return chain.proceed(requestBuilder.build());
            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .baseUrl(GiphyApi.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GiphyApi service = retrofit.create(GiphyApi.class);

        service.listTrending(GIFS_LIMIT).enqueue(new Callback<Gifs>() {
            Context context = MainActivity.this.getApplicationContext();

            @Override
            public void onResponse(Call<Gifs> call, retrofit2.Response<Gifs> response) {
                gifAdapter.addGifs(response.body());
            }

            @Override
            public void onFailure(Call<Gifs> call, Throwable t) {
                Toast.makeText(context, R.string.toast_failed_to_load_gifs, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

     private class GifAdapter extends ArrayAdapter {
        private Context context;
        private LayoutInflater inflater;
        private List<Gif> gifs = Collections.emptyList();

        GifAdapter(@NonNull Context context) {
            super(context, 0);
            this.context = context;
            inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_gif, parent, false);
            }
            Glide.with(context)
                    .load(gifs.get(position).images.fixedWidthDownsampled.url)
                    .asGif()
                    .placeholder(R.mipmap.ic_launcher)
                    .crossFade()
                    .into((ImageView) convertView);
            return convertView;
        }

         @Override
         public int getCount() {
             return gifs.size();
         }

         void addGifs(@NonNull Gifs gifs) {
            this.gifs = gifs.data;
            notifyDataSetChanged();
        }
    }
}

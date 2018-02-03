package io.push.movieapp.service;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by nestorkokoafantchao on 12/9/16.
 */

public class ServiceGeneratore {

        public static final String API_BASE_URL = "http://api.themoviedb.org/3/";
        private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        private static Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(API_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create());

        public static <S> S createService(Class<S> serviceClass) {
            Retrofit retrofit = builder.client(httpClient.build()).build();
            return retrofit.create(serviceClass);
        }

}

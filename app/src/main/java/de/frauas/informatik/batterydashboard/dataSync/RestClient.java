package de.frauas.informatik.batterydashboard.dataSync;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

//import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.frauas.informatik.batterydashboard.dataSync.Models.Credentials;
import de.frauas.informatik.batterydashboard.dataSync.Models.Token;
import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RestClient {

    public static Realm realm;

    private static final String TAG = RestClient.class.getSimpleName();
    private static final String BASE_URL = "https://10.18.2.151/api/";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static Retrofit retrofit = null;

    private static Gson gson = new GsonBuilder()
            .setLenient()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .create();

    public static Retrofit getClient() {

        if (retrofit == null) {

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(getUnsafeOkHttpClient())
                    .build();
        }

        return retrofit;
    }

    BackEndService mApi = getClient().create(BackEndService.class);




    public void setMyToken(final Context context, Credentials credentials){

        Call<Token> callToken = mApi.getToken(credentials);
        callToken.enqueue(new Callback<Token>(){
            @Override
            public void onResponse(Call<Token> call, final Response<Token> response) {

                int status = response.code();

                if(status == 200) {

                    Log.d(TAG, "Token hat geklappt");

                    final Token token = response.body();

                    realm = Realm.getDefaultInstance();
                    final RealmResults<Token> tokenRealmQuery = realm.where(Token.class).findAll();
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {

                            if (!tokenRealmQuery.isEmpty())
                                tokenRealmQuery.deleteAllFromRealm();

                            Token myToken = realm.createObject(Token.class);
                            myToken.setToken(token.getToken());
                        }
                    });
                }

                else
                {

                    Toast toast = Toast.makeText(context, "Fehler bei der Übertragung!", Toast.LENGTH_SHORT);
                    View toastView = toast.getView(); //This'll return the default View of the Toast.

                    /* And now you can get the TextView of the default View of the Toast. */
                    TextView toastMessage = (TextView) toastView.findViewById(android.R.id.message);
                    toastMessage.setTextSize(25);
                    toastMessage.setTextColor(Color.WHITE);
                    toastMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    toastMessage.setGravity(Gravity.CENTER);
                    toastMessage.setCompoundDrawablePadding(16);
                    //toastView.setBackgroundColor(context.getResources().getColor(R.color.toast_color));
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {

                Log.d(TAG, "Token hat nicht geklappt");

                Toast toast = Toast.makeText(context, "Fehler. Es hat nicht geklappt!\n" + t.getMessage(), Toast.LENGTH_SHORT);
                View toastView = toast.getView(); //This'll return the default View of the Toast.

                /* And now you can get the TextView of the default View of the Toast. */
                TextView toastMessage = (TextView) toastView.findViewById(android.R.id.message);
                toastMessage.setTextSize(25);
                toastMessage.setTextColor(Color.WHITE);
                toastMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                toastMessage.setGravity(Gravity.CENTER);
                toastMessage.setCompoundDrawablePadding(16);
                //toastView.setBackgroundColor(context.getResources().getColor(R.color.toast_color));
                toast.show();
            }

        });
    }


   /* public void getAllUser(final Context context, final ProgressDialog progressDialog, Token token){

        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        Call<ArrayList<User>> callUser = mApi.listAllUsers("Bearer " + token.getToken());
        callUser.enqueue(new Callback<ArrayList<User>>() {
            @Override
            public void onResponse(Call<ArrayList<User>> call, Response<ArrayList<User>> response) {

                int status = response.code();

                if (status == 200) {

                    final ArrayList<User> userArrayList = response.body();
                    Log.d(TAG, "User hat geklappt!");

                    realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {

                            for (int i = 0; i < userArrayList.size(); i++) {

                                User userRealm = new User();

                                userRealm.set_id(userArrayList.get(i).get_id());
                                userRealm.setUsername(userArrayList.get(i).getUsername());
                                userRealm.setFirstname(userArrayList.get(i).getFirstname());
                                userRealm.setLastname(userArrayList.get(i).getLastname());
                                userRealm.setHash(userArrayList.get(i).getHash());
                                userRealm.setSalt(userArrayList.get(i).getSalt());

                                realm.insertOrUpdate(userRealm);
                            }
                        }
                    });

                    progressDialog.dismiss();
                } else if (status == 401) {
                    Toast.makeText(context, "Unauthorized: " + status, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                } else if (status == 403) {
                    Toast.makeText(context, "Forbidden: " + status, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                } else if (status == 500) {
                    Toast.makeText(context, "Error: " + status, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }

            }

            @Override
            public void onFailure(Call<ArrayList<User>> call, Throwable t) {

                Log.d(TAG, "User hat nicht geklappt!");

                Toast toast = Toast.makeText(context, "Failed: " + t.getMessage(), Toast.LENGTH_SHORT);
                View toastView = toast.getView(); //This'll return the default View of the Toast.

                 And now you can get the TextView of the default View of the Toast.
                TextView toastMessage = (TextView) toastView.findViewById(android.R.id.message);
                toastMessage.setTextSize(25);
                toastMessage.setTextColor(Color.WHITE);
                toastMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                toastMessage.setGravity(Gravity.CENTER);
                toastMessage.setCompoundDrawablePadding(16);
                toastView.setBackgroundColor(context.getResources().getColor(R.color.toast_color));
                toast.show();
            }
        });
    }
*/
/*
    public void getAllCars(final Context context, final ProgressDialog progressDialog){

        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        Call<ArrayList<Car>> callCar = mApi.listCars("");
        callCar.enqueue(new Callback<ArrayList<Car>>() {
            @Override
            public void onResponse(Call<ArrayList<Car>> call, Response<ArrayList<Car>> response) {

                int status = response.code();

                if(status == 200) {

                    final ArrayList<Car> carArrayList = response.body();

                    realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {

                            for (int i = 0; i < carArrayList.size(); i++) {

                                Car realmCar = new Car();

                                realmCar.set_id(carArrayList.get(i).get_id());
                                realmCar.setVendor(carArrayList.get(i).getVendor());
                                realmCar.setModel(carArrayList.get(i).getModel());
                                realmCar.setLicenseNumber(carArrayList.get(i).getLicenseNumber());
                                realmCar.setMileage(carArrayList.get(i).getMileage());
                                realmCar.setBattery(carArrayList.get(i).getBattery());
                                realmCar.setCharging(carArrayList.get(i).getCharging());
                                realmCar.setChargedkWh(carArrayList.get(i).getChargedkWh());

                                realm.insertOrUpdate(realmCar);
                            }

                        }
                    });

                    progressDialog.dismiss();
                }

                else if(status == 401)
                {
                    Toast.makeText(context, "Unauthorized: " + status, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }

                else if(status == 403)
                {
                    Toast.makeText(context, "Forbidden: " + status, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }

                else
                {
                    Toast.makeText(context, "Error: " + status, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Car>> call, Throwable t) {

                Toast toast = Toast.makeText(context, "Failed: Cars not synchronized!" + t.getMessage(), Toast.LENGTH_SHORT);
                View toastView = toast.getView(); //This'll return the default View of the Toast.

                /* And now you can get the TextView of the default View of the Toast.
                TextView toastMessage = (TextView) toastView.findViewById(android.R.id.message);
                toastMessage.setTextSize(25);
                toastMessage.setTextColor(Color.WHITE);
                toastMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                toastMessage.setGravity(Gravity.CENTER);
                toastMessage.setCompoundDrawablePadding(16);
                toastView.setBackgroundColor(context.getResources().getColor(R.color.toast_color));
                toast.show();
            }
        });
    }
*/
    /*
    public void postNewDrives(final Context context, Token token, Drive drive)
    {
        final Call<ArrayList<Drive>> postJsonDrives = mApi.postDrives("Bearer " + token.getToken(), drive);

        postJsonDrives.enqueue(new Callback<ArrayList<Drive>>() {
            @Override
            public void onResponse(Call<ArrayList<Drive>> call, Response<ArrayList<Drive>> response) {

                int postStatus = response.code();

                Log.d(TAG, "POST Drive hat geklappt!");
                Log.d("PostStatus: " , String.valueOf(postStatus));
                Log.d("Message: " , response.message());



                final ArrayList<Drive> userArrayList = response.body();
                Log.d(TAG, "User hat geklappt!");


                for (int i = 0; i < userArrayList.size(); i++) {

                    Drive myDrive = new Drive();

                    myDrive.setUser(userArrayList.get(i).getUser());
                    System.out.println("UserId: " + myDrive.getUser());
                }


                if(postStatus == 201)
                {
                    Toast toast = Toast.makeText(context, "Daten erfolgreich übermittelt", Toast.LENGTH_SHORT);
                    View toastView = toast.getView(); //This'll return the default View of the Toast.

                     And now you can get the TextView of the default View of the Toast. */
                   /* TextView toastMessage = (TextView) toastView.findViewById(android.R.id.message);
                    toastMessage.setTextSize(25);
                    toastMessage.setTextColor(Color.WHITE);
                    toastMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    toastMessage.setGravity(Gravity.CENTER);
                    toastMessage.setCompoundDrawablePadding(16);
                    toastView.setBackgroundColor(context.getResources().getColor(R.color.toast_color));
                    toast.show();


                    realm = Realm.getDefaultInstance();

                    final RealmResults<AddressStart> addressStarts   = realm.where(AddressStart.class).findAll();
                    final RealmResults<AddressStop> addressStops     = realm.where(AddressStop.class).findAll();
                    final RealmResults<LocationStart> locationStarts = realm.where(LocationStart.class).findAll();
                    final RealmResults<LocationStop> locationStops   = realm.where(LocationStop.class).findAll();
                    final RealmResults<Drive> driveRealmResults      = realm.where(Drive.class).findAll();

                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {

                            addressStarts.deleteAllFromRealm();
                            addressStops.deleteAllFromRealm();
                            locationStarts.deleteAllFromRealm();
                            locationStops.deleteAllFromRealm();
                            driveRealmResults.deleteAllFromRealm();
                        }
                    });

                }
                else if(postStatus == 400)
                {
                    try {
                        Toast toast = Toast.makeText(context, "Übermittlung nicht erfolgreich: " + response.message(), Toast.LENGTH_SHORT);
                        View toastView = toast.getView(); //This'll return the default View of the Toast.

                         And now you can get the TextView of the default View of the Toast. */
    /*
                        TextView toastMessage = (TextView) toastView.findViewById(android.R.id.message);
                        toastMessage.setTextSize(25);
                        toastMessage.setTextColor(Color.WHITE);
                        toastMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        toastMessage.setGravity(Gravity.CENTER);
                        toastMessage.setCompoundDrawablePadding(16);
                        toastView.setBackgroundColor(context.getResources().getColor(R.color.toast_color));
                        toast.show();

                        System.out.println("Post 400: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if(postStatus == 401)
                {
                    try {
                        Toast toast = Toast.makeText(context, "Übermittlung nicht erfolgreich: " + response.message(), Toast.LENGTH_SHORT);
                        View toastView = toast.getView(); //This'll return the default View of the Toast.

                        /* And now you can get the TextView of the default View of the Toast. */
                 /*       TextView toastMessage = (TextView) toastView.findViewById(android.R.id.message);
                        toastMessage.setTextSize(25);
                        toastMessage.setTextColor(Color.WHITE);
                        toastMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        toastMessage.setGravity(Gravity.CENTER);
                        toastMessage.setCompoundDrawablePadding(16);
                        toastView.setBackgroundColor(context.getResources().getColor(R.color.toast_color));
                        toast.show();

                        System.out.println("Post 401: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if(postStatus == 500)
                {
                    try {

                        Toast toast = Toast.makeText(context, "Übermittlung nicht erfolgreich: " + response.message(), Toast.LENGTH_SHORT);
                        View toastView = toast.getView(); //This'll return the default View of the Toast.

                         And now you can get the TextView of the default View of the Toast.
                       /* TextView toastMessage = (TextView) toastView.findViewById(android.R.id.message);
                        toastMessage.setTextSize(25);
                        toastMessage.setTextColor(Color.WHITE);
                        toastMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        toastMessage.setGravity(Gravity.CENTER);
                        toastMessage.setCompoundDrawablePadding(16);
                        toastView.setBackgroundColor(context.getResources().getColor(R.color.toast_color));
                        toast.show();

                        System.out.println("Post 500: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    Toast toast = Toast.makeText(context, "Übermittlung nicht erfolgreich: " + response.message(), Toast.LENGTH_SHORT);
                    View toastView = toast.getView(); //This'll return the default View of the Toast.

                    And now you can get the TextView of the default View of the Toast.
                    TextView toastMessage = (TextView) toastView.findViewById(android.R.id.message);
                    toastMessage.setTextSize(25);
                    toastMessage.setTextColor(Color.WHITE);
                    toastMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    toastMessage.setGravity(Gravity.CENTER);
                    toastMessage.setCompoundDrawablePadding(16);
                    toastView.setBackgroundColor(context.getResources().getColor(R.color.toast_color));
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Drive>> call, Throwable t) {

                Log.d(TAG, "POST Drive hat nicht geklappt: " + t.getMessage());

                Toast toast = Toast.makeText(context, "Daten konnten nicht übermittelt werden", Toast.LENGTH_SHORT);
                View toastView = toast.getView(); //This'll return the default View of the Toast.

                 And now you can get the TextView of the default View of the Toast.

                  */
               /* TextView toastMessage = (TextView) toastView.findViewById(android.R.id.message);
                toastMessage.setTextSize(25);
                toastMessage.setTextColor(Color.WHITE);
                toastMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                toastMessage.setGravity(Gravity.CENTER);
                toastMessage.setCompoundDrawablePadding(16);
                toastView.setBackgroundColor(context.getResources().getColor(R.color.toast_color));
                toast.show();
            }
        });
    }

*/
    /**
     * Enables https connection to trust all ssl certificates. The only solutuion found which worked.
     * This is insecure and enables a man in the middle attack!.
     */
    @SuppressLint("TrulyRandom")
    public void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }



    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });



            builder.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    okhttp3.Request original = chain.request();

                    okhttp3.Request request = original.newBuilder()
                            //.header("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjU4N2ZiMWZlYTBjNjY2MjhlZjMyNmMzMyIsInVzZXJuYW1lIjoiQ2FyRGV2aWNlIiwicm9sZSI6IkNhckRldmljZSIsImV4cCI6MTQ4NjcwNjcyMywiaWF0IjoxNDg2Njc3OTIzfQ.mzEvrQ_Y-5N4sGDgUBYqTk3cRqQSPznUDbzaGNGRPldUtuATw46ttuJrTdG7qDWMKtvghsN7VvegKHSf7bvbW8P_F-FvB4XYLD-FBrg1Ce0Oz6UDdhffz_TAA_QZbaCHJ8r735Qz273aU5bB7Ttja-qpJBeN-LbACnUqaY23gpBQ0ruKj_kPbIqrqG4_O9dYOKWPEUDVjY4S2zDgPd1W0xK9jr-PsuxmDx9SZZ8zZNhXH9vAdMWwBHFfIHo0ZuXkA886DzxBwF12csC6vga2tZvwjiT1E-En2bzTJwYExH8sgGtHYQSqtBHEisN2JB309S0NXTmdFQ4_5MalyksH6A")
                            //.header("Content-Type", "application/json")
                            .method(original.method(), original.body())
                            .build();

                    okhttp3.Response response = chain.proceed(request);

                    System.out.println("Headers: "+request.headers());
                    System.out.println("Response Body: " + response.body());
                    System.out.println("Response Message: " + response.message());

                    return response;
                }
            });


            //builder.addNetworkInterceptor(new StethoInterceptor());

            OkHttpClient okHttpClient = builder.build();

            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

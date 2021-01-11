package de.frauas.informatik.batterydashboard.dataSync;

import de.frauas.informatik.batterydashboard.dataSync.Models.Credentials;
import com.google.gson.JsonObject;
/*import com.student.fahrtenbuchapp.models.AddressStart;
import com.student.fahrtenbuchapp.models.AddressStop;
import com.student.fahrtenbuchapp.models.Car;
import de.frauas.informatik.batterydashboard.dataSync.Models;
import com.student.fahrtenbuchapp.models.Drive;
import com.student.fahrtenbuchapp.models.LocationStart;
import com.student.fahrtenbuchapp.models.LocationStop;
import com.student.fahrtenbuchapp.models.Token;
import com.student.fahrtenbuchapp.models.User;
*/
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import de.frauas.informatik.batterydashboard.dataSync.Models.Token;
import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;


public interface BackEndService {

    @POST("login")
    Call<Token> getToken(@Body Credentials credentials);

  /*  @GET("users")
    Call<ArrayList<User>> listAllUsers(@Header("Authorization") String token);

    @GET("cars/{car}")
    Call<ArrayList<Car>> listCars(@Path("car") String car);

    @Headers({"Content-Type: application/json"})
    @POST("drives")
    Call<ArrayList<Drive>> postDrives(@Header("Authorization") String token, @Body Drive drive);*/
}

package ai.ayushsingla.dhwani.remote;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SnowboyClient {

    @POST("api/v1/train/")
    Call<ResponseBody> postPMDL(@Body SnowboyRequest request);
}

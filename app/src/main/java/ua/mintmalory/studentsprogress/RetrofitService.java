package ua.mintmalory.studentsprogress;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import ua.mintmalory.studentsprogress.models.Student;

/**
 * Created by mintmalory on 06.01.17.
 */

public interface RetrofitService {
    String ENDPOINT = "https://ddapp-sfa-api.azurewebsites.net/";

    @GET("api/test/sampleData")
    Call<List<Student>> getStudentsList();
}

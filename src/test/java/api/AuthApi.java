package api;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import models.User;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;

public class AuthApi {
    private final RequestSpecification requestSpec;

    public AuthApi(RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
    }

    @Step("Логин пользователя")
    public Response login(User user) {
        return given()
                .spec(requestSpec)
                .body(user)
                .when()
                .post("/api/auth/login");
    }

    @Step("Получение данных пользователя")
    public Response getUserData(String accessToken) {
        return given()
                .spec(requestSpec)
                .header("Authorization", accessToken)
                .when()
                .get("/api/auth/user");
    }

    @Step("Проверка успешного логина")
    public boolean isLoginSuccessful(Response response) {
        try {
            return response.statusCode() == SC_OK &&
                    response.path("success") != null &&
                    Boolean.TRUE.equals(response.path("success"));
        } catch (Exception e) {
            return false;
        }
    }

    @Step("Проверка ошибки неверных учетных данных")
    public boolean isInvalidCredentialsError(Response response) {
        try {
            String message = response.path("message");
            return response.statusCode() == SC_UNAUTHORIZED &&
                    message != null &&
                    message.equals("email or password are incorrect");
        } catch (Exception e) {
            return false;
        }
    }

    @Step("Получение accessToken из ответа")
    public String getAccessToken(Response response) {
        try {
            return response.path("accessToken");
        } catch (Exception e) {
            return null;
        }
    }
}
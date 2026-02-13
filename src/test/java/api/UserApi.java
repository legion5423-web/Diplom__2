package api;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import models.User;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;

public class UserApi {
    private final RequestSpecification requestSpec;

    public UserApi(RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
    }

    @Step("Создание пользователя")
    public Response createUser(User user) {
        return given()
                .spec(requestSpec)
                .body(user)
                .when()
                .post("/api/auth/register");
    }

    @Step("Удаление пользователя")
    public Response deleteUser(String accessToken) {
        return given()
                .spec(requestSpec)
                .header("Authorization", accessToken)
                .when()
                .delete("/api/auth/user");
    }

    @Step("Проверка успешного создания пользователя")
    public boolean isCreatedSuccessfully(Response response) {
        try {
            return response.statusCode() == SC_OK &&
                    response.path("success") != null &&
                    Boolean.TRUE.equals(response.path("success"));
        } catch (Exception e) {
            return false;
        }
    }

    @Step("Проверка ошибки дублирования пользователя")
    public boolean isDuplicateError(Response response) {
        try {
            String message = response.path("message");
            return response.statusCode() == SC_FORBIDDEN &&
                    message != null &&
                    message.equals("User already exists");
        } catch (Exception e) {
            return false;
        }
    }

    @Step("Проверка ошибки недостатка данных")
    public boolean isMissingDataError(Response response) {
        try {
            String message = response.path("message");
            return response.statusCode() == SC_FORBIDDEN &&
                    message != null &&
                    message.equals("Email, password and name are required fields");
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
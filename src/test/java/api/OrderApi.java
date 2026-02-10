package api;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import models.Order;

import static io.restassured.RestAssured.given;

public class OrderApi {
    private final RequestSpecification requestSpec;

    public OrderApi(RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
    }

    @Step("Создание заказа")
    public Response createOrder(Order order, String accessToken) {
        try {
            if (accessToken != null && !accessToken.isEmpty()) {
                return given()
                        .spec(requestSpec)
                        .header("Authorization", accessToken)
                        .body(order)
                        .when()
                        .post("/api/orders");
            } else {
                return given()
                        .spec(requestSpec)
                        .body(order)
                        .when()
                        .post("/api/orders");
            }
        } catch (Exception e) {
            System.err.println("Ошибка при создании заказа: " + e.getMessage());
            throw e;
        }
    }

    @Step("Получение списка ингредиентов")
    public Response getIngredients() {
        return given()
                .spec(requestSpec)
                .when()
                .get("/api/ingredients");
    }

    @Step("Проверка успешного создания заказа")
    public boolean isOrderCreatedSuccessfully(Response response) {
        try {
            return response.statusCode() == 200 &&
                    response.path("success") != null &&
                    Boolean.TRUE.equals(response.path("success"));
        } catch (Exception e) {
            return false;
        }
    }

    @Step("Проверка ошибки создания заказа без ингредиентов")
    public boolean isNoIngredientsError(Response response) {
        try {
            String message = response.path("message");
            return response.statusCode() == 400 &&
                    message != null &&
                    message.contains("Ingredient");
        } catch (Exception e) {
            return false;
        }
    }

    @Step("Получение списка заказов пользователя")
    public Response getUserOrders(String accessToken) {
        return given()
                .spec(requestSpec)
                .header("Authorization", accessToken)
                .when()
                .get("/api/orders");
    }
}
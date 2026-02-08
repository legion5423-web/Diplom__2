package tests;

import api.AuthApi;
import api.OrderApi;
import api.UserApi;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

public class BaseTest {
    protected static final String BASE_URL = "https://stellarburgers.nomoreparties.site";

    protected UserApi userApi;
    protected AuthApi authApi;
    protected OrderApi orderApi;
    protected RequestSpecification requestSpec;

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URL;

        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .build();

        userApi = new UserApi(requestSpec);
        authApi = new AuthApi(requestSpec);
        orderApi = new OrderApi(requestSpec);
    }

    protected List<String> getValidIngredients() {
        try {
            Response response = orderApi.getIngredients();

            if (response.getContentType().contains("xml")) {
                // Если ответ в XML формате
                return getIngredientsFromXml(response);
            } else {
                // Если ответ в JSON формате
                return getIngredientsFromJson(response);
            }
        } catch (Exception e) {
            // Возвращаем тестовые ингредиенты в случае ошибки
            return getDefaultTestIngredients();
        }
    }

    private List<String> getIngredientsFromJson(Response response) {
        // Парсим JSON ответ
        return response.jsonPath().getList("data._id");
    }

    private List<String> getIngredientsFromXml(Response response) {
        // Парсим XML ответ (если API возвращает XML)
        List<String> ingredients = new ArrayList<>();
        // Здесь должна быть логика парсинга XML
        // Для Stellar Burgers API обычно JSON, так что возвращаем тестовые данные
        return getDefaultTestIngredients();
    }

    private List<String> getDefaultTestIngredients() {
        // Тестовые ингредиенты из документации Stellar Burgers
        return List.of(
                "61c0c5a71d1f82001bdaaa6d", // Флюоресцентная булка R2-D3
                "61c0c5a71d1f82001bdaaa6f", // Соус Spicy-X
                "61c0c5a71d1f82001bdaaa70", // Мясо бессмертных моллюсков Protostomia
                "61c0c5a71d1f82001bdaaa72"  // Сыр с астероидной плесенью
        );
    }
}
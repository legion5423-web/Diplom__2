package tests;

import api.AuthApi;
import api.OrderApi;
import api.UserApi;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import models.User;
import org.junit.After;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

import static org.apache.http.HttpStatus.SC_OK;
import utils.DataGenerator;

public class BaseTest {
    protected static final String BASE_URL = "https://stellarburgers.education-services.ru/";

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

    @After
    public void tearDown() {
        // Базовая очистка (может быть переопределена в тестовых классах)
    }

    protected List<String> getValidIngredients() {
        try {
            Response response = orderApi.getIngredients();

            if (response.statusCode() != SC_OK) {
                return getDefaultTestIngredients();
            }

            // Проверяем формат ответа
            if (response.getContentType().contains("application/json")) {
                return getIngredientsFromJson(response);
            } else {
                System.err.println("Unexpected content type: " + response.getContentType());
                return getDefaultTestIngredients();
            }
        } catch (Exception e) {
            System.err.println("Error getting ingredients: " + e.getMessage());
            return getDefaultTestIngredients();
        }
    }

    private List<String> getIngredientsFromJson(Response response) {
        try {
            List<Object> data = response.jsonPath().getList("data");
            if (data == null || data.isEmpty()) {
                return getDefaultTestIngredients();
            }

            List<String> ingredients = new ArrayList<>();
            for (Object item : data) {
                if (item instanceof java.util.Map) {
                    java.util.Map<String, Object> map = (java.util.Map<String, Object>) item;
                    String id = (String) map.get("_id");
                    if (id != null && !id.isEmpty()) {
                        ingredients.add(id);
                    }
                }
            }

            return ingredients.isEmpty() ? getDefaultTestIngredients() : ingredients;
        } catch (Exception e) {
            return getDefaultTestIngredients();
        }
    }

    private List<String> getDefaultTestIngredients() {
        // Тестовые ингредиенты из документации Stellar Burgers
        return List.of(
                "61c0c5a71d1f82001bdaaa6d", // Флюоресцентная булка R2-D3
                "61c0c5a71d1f82001bdaaa6f"  // Соус Spicy-X
        );
    }

    // Вспомогательные методы для генерации тестовых данных
    protected User generateRandomUser() {
        return DataGenerator.generateRandomUser();
    }

    protected String generateRandomEmail() {
        return DataGenerator.generateRandomEmail();
    }

    protected String generateRandomPassword() {
        return DataGenerator.generateRandomPassword();
    }

    protected String generateRandomName() {
        return DataGenerator.generateRandomName();
    }
}
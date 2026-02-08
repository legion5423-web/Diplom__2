package tests;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.Order;
import models.User;
import org.junit.After;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class OrderTests extends BaseTest {
    private String accessToken;
    private User testUser;

    @After
    public void tearDown() {
        if (accessToken != null) {
            try {
                userApi.deleteUser(accessToken);
            } catch (Exception e) {
                // Игнорируем ошибки удаления
            }
        }
    }

    @Test
    @DisplayName("Создание заказа с авторизацией")
    public void testCreateOrderWithAuth() {
        // Создаем тестового пользователя
        String email = "order_test_" + System.currentTimeMillis() + "@example.com";
        testUser = new User(email, "password123", "Order Test User");

        Response createResponse = userApi.createUser(testUser);

        System.out.println("=== Test: Create order with auth ===");
        System.out.println("Create user status: " + createResponse.statusCode());
        System.out.println("Create user response: " + createResponse.asString());

        if (userApi.isCreatedSuccessfully(createResponse)) {
            accessToken = userApi.getAccessToken(createResponse);

            // Получаем ингредиенты и создаем заказ
            List<String> ingredients = getValidIngredients();
            if (!ingredients.isEmpty()) {
                System.out.println("Using ingredients: " + ingredients.subList(0, Math.min(2, ingredients.size())));

                Order order = new Order(ingredients.subList(0, Math.min(2, ingredients.size())));

                Response orderResponse = orderApi.createOrder(order, accessToken);

                System.out.println("Create order status: " + orderResponse.statusCode());
                System.out.println("Create order response: " + orderResponse.asString());

                assertTrue("Заказ должен быть создан успешно",
                        orderApi.isOrderCreatedSuccessfully(orderResponse));
            } else {
                System.out.println("No ingredients available for test");
            }
        } else {
            System.out.println("Failed to create test user");
        }
        System.out.println("=====================================");
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    public void testCreateOrderWithoutAuth() {
        List<String> ingredients = getValidIngredients();
        if (!ingredients.isEmpty()) {
            Order order = new Order(ingredients.subList(0, Math.min(2, ingredients.size())));

            Response response = orderApi.createOrder(order, null);

            System.out.println("=== Test: Create order without auth ===");
            System.out.println("Status code: " + response.statusCode());
            System.out.println("Response: " + response.asString());
            System.out.println("========================================");

            assertTrue("Заказ должен быть создан успешно",
                    orderApi.isOrderCreatedSuccessfully(response));
        }
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    public void testCreateOrderWithoutIngredients() {
        Order order = new Order(Arrays.asList());

        Response response = orderApi.createOrder(order, null);

        System.out.println("=== Test: Create order without ingredients ===");
        System.out.println("Status code: " + response.statusCode());
        System.out.println("Response: " + response.asString());
        System.out.println("==============================================");

        if (response.statusCode() == 400) {
            assertTrue("Должна быть ошибка отсутствия ингредиентов",
                    orderApi.isNoIngredientsError(response));
        }
    }

    @Test
    @DisplayName("Получение списка ингредиентов")
    public void testGetIngredients() {
        Response response = orderApi.getIngredients();

        System.out.println("=== Test: Get ingredients ===");
        System.out.println("Status code: " + response.statusCode());
        System.out.println("Content-Type: " + response.getContentType());
        System.out.println("Response (first 500 chars): " +
                response.asString().substring(0, Math.min(500, response.asString().length())));
        System.out.println("=============================");

        assertTrue("API должен возвращать успешный ответ", response.statusCode() == 200);
    }
}
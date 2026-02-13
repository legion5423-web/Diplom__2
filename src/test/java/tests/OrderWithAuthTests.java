package tests;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.Order;
import org.junit.Test;

import java.util.List;

import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.*;

public class OrderWithAuthTests extends OrderTestWithUser {

    @Test
    @DisplayName("Создание заказа с авторизацией")
    @Description("Проверка успешного создания заказа авторизованным пользователем")
    public void testCreateOrderWithAuth() {
        List<String> ingredients = getValidIngredients();
        assertFalse("Список ингредиентов не должен быть пустым", ingredients.isEmpty());

        Order order = new Order(ingredients.subList(0, Math.min(2, ingredients.size())));
        Response orderResponse = orderApi.createOrder(order, accessToken);

        assertEquals("Должен возвращаться статус 200", SC_OK, orderResponse.statusCode());
        assertTrue("Заказ должен быть создан успешно",
                orderApi.isOrderCreatedSuccessfully(orderResponse));

        // Дополнительные проверки для авторизованного пользователя
        assertNotNull("Должен возвращаться owner", orderResponse.path("order.owner"));
        assertEquals("Email владельца должен совпадать",
                testUser.getEmail(),
                orderResponse.path("order.owner.email"));
        assertEquals("Имя владельца должно совпадать",
                testUser.getName(),
                orderResponse.path("order.owner.name"));
    }

    @Test
    @DisplayName("Получение списка заказов пользователя")
    @Description("Проверка получения истории заказов авторизованного пользователя")
    public void testGetUserOrders() {
        // Сначала создаем заказ
        List<String> ingredients = getValidIngredients();
        if (!ingredients.isEmpty()) {
            Order order = new Order(ingredients.subList(0, Math.min(2, ingredients.size())));
            orderApi.createOrder(order, accessToken);

            // Теперь получаем список заказов
            Response ordersResponse = orderApi.getUserOrders(accessToken);

            assertEquals("Должен возвращаться статус 200", SC_OK, ordersResponse.statusCode());
            assertTrue("Запрос должен быть успешным",
                    Boolean.TRUE.equals(ordersResponse.path("success")));

            List<Object> orders = ordersResponse.jsonPath().getList("orders");
            assertNotNull("Список заказов не должен быть null", orders);
            // Может быть пустым, если заказ еще не обработан
        }
    }

    @Test
    @DisplayName("Создание нескольких заказов одним пользователем")
    @Description("Проверка что один пользователь может создавать несколько заказов")
    public void testCreateMultipleOrders() {
        List<String> ingredients = getValidIngredients();
        assertFalse("Список ингредиентов не должен быть пустым", ingredients.isEmpty());

        // Создаем первый заказ
        Order firstOrder = new Order(ingredients.subList(0, Math.min(2, ingredients.size())));
        Response firstResponse = orderApi.createOrder(firstOrder, accessToken);
        assertEquals("Первый заказ должен быть создан", SC_OK, firstResponse.statusCode());

        // Создаем второй заказ (другие ингредиенты)
        if (ingredients.size() >= 4) {
            Order secondOrder = new Order(ingredients.subList(2, Math.min(4, ingredients.size())));
            Response secondResponse = orderApi.createOrder(secondOrder, accessToken);
            assertEquals("Второй заказ должен быть создан", SC_OK, secondResponse.statusCode());
        }
    }

    @Test
    @DisplayName("Создание заказа с истекшим токеном")
    @Description("Проверка ошибки при создании заказа с невалидным токеном авторизации")
    public void testCreateOrderWithExpiredToken() {
        List<String> ingredients = getValidIngredients();
        assertFalse("Список ингредиентов не должен быть пустым", ingredients.isEmpty());

        Order order = new Order(ingredients.subList(0, Math.min(2, ingredients.size())));

        // Используем заведомо невалидный токен
        String invalidToken = "Bearer invalid_token_12345";
        Response response = orderApi.createOrder(order, invalidToken);

        // Проверяем, что возвращается ошибка авторизации
        assertEquals("Должен возвращаться статус 401 или 403",
                SC_UNAUTHORIZED,
                response.statusCode());
    }
}
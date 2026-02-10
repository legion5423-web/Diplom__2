package tests;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.Order;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.*;

public class OrderTests extends BaseTest {

    @Test
    @DisplayName("Создание заказа без авторизации")
    @Description("Проверка что неавторизованный пользователь может создавать заказы")
    public void testCreateOrderWithoutAuth() {
        List<String> ingredients = getValidIngredients();
        assertFalse("Список ингредиентов не должен быть пустым", ingredients.isEmpty());

        Order order = new Order(ingredients.subList(0, Math.min(2, ingredients.size())));
        Response response = orderApi.createOrder(order, null);

        assertEquals("Должен возвращаться статус 200", SC_OK, response.statusCode());
        assertTrue("Заказ должен быть создан успешно",
                orderApi.isOrderCreatedSuccessfully(response));

        // Проверяем структуру ответа
        assertNotNull("Должен возвращаться номер заказа", response.path("order.number"));
        assertNotNull("Должен возвращаться номер трека", response.path("order._id"));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    @Description("Проверка ошибки при создании заказа без указания ингредиентов")
    public void testCreateOrderWithoutIngredients() {
        Order order = new Order(Arrays.asList());
        Response response = orderApi.createOrder(order, null);

        assertEquals("Должен возвращаться статус 400", SC_BAD_REQUEST, response.statusCode());
        assertTrue("Должна быть ошибка отсутствия ингредиентов",
                orderApi.isNoIngredientsError(response));
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
    @Description("Проверка ошибки при создании заказа с невалидными идентификаторами ингредиентов")
    public void testCreateOrderWithInvalidIngredientHash() {
        List<String> invalidIngredients = Arrays.asList(
                "invalid_hash_12345",
                "another_invalid_hash_67890"
        );

        Order order = new Order(invalidIngredients);
        Response response = orderApi.createOrder(order, null);

        // Проверяем, что возвращается ошибка сервера
        assertEquals("Должен возвращаться статус 500",
                SC_INTERNAL_SERVER_ERROR,
                response.statusCode());
    }

    @Test
    @DisplayName("Получение списка ингредиентов")
    @Description("Проверка корректности получения списка доступных ингредиентов")
    public void testGetIngredients() {
        Response response = orderApi.getIngredients();

        assertEquals("API должен возвращать статус 200", SC_OK, response.statusCode());
        assertTrue("Ответ должен содержать JSON",
                response.getContentType().contains("application/json"));

        // Проверяем структуру ответа
        assertTrue("Success должно быть true",
                Boolean.TRUE.equals(response.path("success")));

        List<Object> ingredients = response.jsonPath().getList("data");
        assertFalse("Список ингредиентов не должен быть пустым", ingredients.isEmpty());

        // Проверяем структуру первого ингредиента
        Object firstIngredient = ingredients.get(0);
        assertTrue("Первый элемент должен быть объектом",
                firstIngredient instanceof java.util.Map);

        java.util.Map<String, Object> ingredientMap = (java.util.Map<String, Object>) firstIngredient;
        assertNotNull("Должен быть _id", ingredientMap.get("_id"));
        assertNotNull("Должно быть name", ingredientMap.get("name"));
        assertNotNull("Должен быть type", ingredientMap.get("type"));
        assertNotNull("Должна быть price", ingredientMap.get("price"));
    }

    @Test
    @DisplayName("Создание заказа с одним ингредиентом")
    @Description("Проверка создания заказа с минимальным количеством ингредиентов (один)")
    public void testCreateOrderWithSingleIngredient() {
        List<String> ingredients = getValidIngredients();
        assertFalse("Список ингредиентов не должен быть пустым", ingredients.isEmpty());

        Order order = new Order(List.of(ingredients.get(0)));
        Response response = orderApi.createOrder(order, null);

        assertEquals("Должен возвращаться статус 200", SC_OK, response.statusCode());
        assertTrue("Заказ должен быть создан успешно",
                orderApi.isOrderCreatedSuccessfully(response));
    }

    @Test
    @DisplayName("Создание заказа с максимальным количеством ингредиентов")
    @Description("Проверка создания заказа с большим количеством ингредиентов")
    public void testCreateOrderWithMultipleIngredients() {
        List<String> ingredients = getValidIngredients();
        assertFalse("Список ингредиентов не должен быть пустым", ingredients.isEmpty());

        // Берем первые 5 ингредиентов или все, если меньше 5
        int count = Math.min(5, ingredients.size());
        Order order = new Order(ingredients.subList(0, count));
        Response response = orderApi.createOrder(order, null);

        assertEquals("Должен возвращаться статус 200", SC_OK, response.statusCode());
        assertTrue("Заказ должен быть создан успешно",
                orderApi.isOrderCreatedSuccessfully(response));
    }
}
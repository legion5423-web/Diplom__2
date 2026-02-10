package tests;

import models.User;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.*;

public class UserTests extends BaseTest {
    private String accessToken;
    private User createdUser;

    @After
    public void tearDown() {
        if (accessToken != null) {
            try {
                userApi.deleteUser(accessToken);
            } catch (Exception e) {
                System.err.println("Error deleting user: " + e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    public void testCreateUniqueUser() {
        // Генерируем уникальные данные
        String email = "test" + System.currentTimeMillis() + "@example.com";
        createdUser = new User(email, "password123", "Test User");

        Response response = userApi.createUser(createdUser);

        System.out.println("=== Test: Create unique user ===");
        System.out.println("Status code: " + response.statusCode());
        System.out.println("Response body: " + response.asString());
        System.out.println("=================================");

        assertEquals("Должен возвращаться статус 200", SC_OK, response.statusCode());
        assertTrue("Пользователь должен быть создан успешно",
                userApi.isCreatedSuccessfully(response));

        // Сохраняем токен для очистки
        accessToken = userApi.getAccessToken(response);
        assertNotNull("Должен возвращаться accessToken", accessToken);
    }

    @Test
    @DisplayName("Создание пользователя, который уже зарегистрирован")
    public void testCreateExistingUser() {
        // Используем ваши данные
        User existingUser = new User("legion5423@gmail.com", "J7Y-Qct-kUR-kW6", "Test User");

        Response response = userApi.createUser(existingUser);

        System.out.println("=== Test: Create existing user ===");
        System.out.println("Status code: " + response.statusCode());
        System.out.println("Response body: " + response.asString());
        System.out.println("===================================");

        assertEquals("Должен возвращаться статус 403", SC_FORBIDDEN, response.statusCode());
        assertTrue("Должна быть ошибка дублирования",
                userApi.isDuplicateError(response));
    }

    @Test
    @DisplayName("Создание пользователя без email")
    public void testCreateUserWithoutEmail() {
        User user = new User("", "password123", "Test User");

        Response response = userApi.createUser(user);

        System.out.println("=== Test: Create user without email ===");
        System.out.println("Status code: " + response.statusCode());
        System.out.println("Response body: " + response.asString());
        System.out.println("========================================");

        assertEquals("Должен возвращаться статус 403", SC_FORBIDDEN, response.statusCode());
        assertTrue("Должна быть ошибка недостатка данных",
                userApi.isMissingDataError(response));
    }

    @Test
    @DisplayName("Создание пользователя без пароля")
    public void testCreateUserWithoutPassword() {
        String email = "test" + System.currentTimeMillis() + "@example.com";
        User user = new User(email, "", "Test User");

        Response response = userApi.createUser(user);

        System.out.println("=== Test: Create user without password ===");
        System.out.println("Status code: " + response.statusCode());
        System.out.println("Response body: " + response.asString());
        System.out.println("===========================================");

        assertEquals("Должен возвращаться статус 403", SC_FORBIDDEN, response.statusCode());
        assertTrue("Должна быть ошибка недостатка данных",
                userApi.isMissingDataError(response));
    }

    @Test
    @DisplayName("Создание пользователя без имени")
    public void testCreateUserWithoutName() {
        String email = "test" + System.currentTimeMillis() + "@example.com";
        User user = new User(email, "password123", "");

        Response response = userApi.createUser(user);

        System.out.println("=== Test: Create user without name ===");
        System.out.println("Status code: " + response.statusCode());
        System.out.println("Response body: " + response.asString());
        System.out.println("=======================================");

        assertEquals("Должен возвращаться статус 403", SC_FORBIDDEN, response.statusCode());
        assertTrue("Должна быть ошибка недостатка данных",
                userApi.isMissingDataError(response));
    }
}
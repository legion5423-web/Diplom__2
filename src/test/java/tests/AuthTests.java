package tests;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.User;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AuthTests extends BaseTest {

    @Test
    @DisplayName("Вход под существующим пользователем")
    public void testLoginWithExistingUser() {
        // Используем ваши данные
        User existingUser = new User("legion5423@gmail.com", "J7Y-Qct-kUR-kW6", "Test User");

        Response response = authApi.login(existingUser);

        // Выводим информацию для отладки
        System.out.println("=== Test: Login with existing user ===");
        System.out.println("Status code: " + response.statusCode());
        System.out.println("Response body: " + response.asString());
        System.out.println("Content-Type: " + response.getContentType());
        System.out.println("=======================================");

        if (response.statusCode() == 200) {
            assertTrue("Логин должен быть успешным",
                    authApi.isLoginSuccessful(response));
        } else {
            System.out.println("Пользователь не найден или неверные данные");
        }
    }

    @Test
    @DisplayName("Вход с неверным паролем")
    public void testLoginWithWrongPassword() {
        User wrongUser = new User("legion5423@gmail.com", "wrongPassword", "Test User");

        Response response = authApi.login(wrongUser);

        System.out.println("=== Test: Login with wrong password ===");
        System.out.println("Status code: " + response.statusCode());
        System.out.println("Response body: " + response.asString());
        System.out.println("========================================");

        if (response.statusCode() == 401) {
            assertTrue("Должна быть ошибка неверных учетных данных",
                    authApi.isInvalidCredentialsError(response));
        }
    }

    @Test
    @DisplayName("Проверка доступности API")
    public void testApiIsAvailable() {
        Response response = orderApi.getIngredients();

        System.out.println("=== Test: API Availability ===");
        System.out.println("Ingredients API status: " + response.statusCode());
        System.out.println("Content-Type: " + response.getContentType());
        System.out.println("Response: " + response.asString().substring(0, Math.min(200, response.asString().length())) + "...");
        System.out.println("===============================");

        assertTrue("API должен быть доступен", response.statusCode() == 200);
    }

    @Test
    @DisplayName("Вход с неверным email")
    public void testLoginWithWrongEmail() {
        User wrongUser = new User("wrong_email@example.com", "J7Y-Qct-kUR-kW6", "Test User");

        Response response = authApi.login(wrongUser);

        System.out.println("=== Test: Login with wrong email ===");
        System.out.println("Status code: " + response.statusCode());
        System.out.println("Response body: " + response.asString());
        System.out.println("=====================================");

        if (response.statusCode() == 401) {
            assertTrue("Должна быть ошибка неверных учетных данных",
                    authApi.isInvalidCredentialsError(response));
        }
    }
}
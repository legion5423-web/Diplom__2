package tests;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.User;
import org.junit.After;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.*;

public class AuthTests extends BaseTest {
    private User testUser;
    private String testAccessToken;

    @After
    public void tearDown() {
        if (testAccessToken != null) {
            try {
                userApi.deleteUser(testAccessToken);
            } catch (Exception e) {
                System.err.println("Error cleaning up test user: " + e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Вход под существующим пользователем")
    @Description("Проверка успешной авторизации с корректными учетными данными")
    public void testLoginWithExistingUser() {
        // Создаем нового пользователя с случайными данными
        testUser = generateRandomUser();

        // Регистрируем пользователя
        Response registerResponse = userApi.createUser(testUser);
        assertEquals("Регистрация должна быть успешной", SC_OK, registerResponse.statusCode());
        assertTrue("Пользователь должен быть создан", userApi.isCreatedSuccessfully(registerResponse));

        testAccessToken = userApi.getAccessToken(registerResponse);
        assertNotNull("Должен возвращаться accessToken", testAccessToken);

        // Теперь логинимся с этими данными
        Response loginResponse = authApi.login(testUser);

        assertEquals("Должен возвращаться статус 200", SC_OK, loginResponse.statusCode());
        assertTrue("Логин должен быть успешным", authApi.isLoginSuccessful(loginResponse));

        // Проверяем структуру ответа
        assertNotNull("Должен возвращаться accessToken", authApi.getAccessToken(loginResponse));
        assertNotNull("Должен возвращаться user объект", loginResponse.path("user"));
        assertEquals("Email должен совпадать", testUser.getEmail(), loginResponse.path("user.email"));
        assertEquals("Имя должно совпадать", testUser.getName(), loginResponse.path("user.name"));
    }

    @Test
    @DisplayName("Вход с неверным паролем")
    @Description("Проверка ошибки авторизации при вводе некорректного пароля")
    public void testLoginWithWrongPassword() {
        // Создаем пользователя
        testUser = generateRandomUser();
        Response registerResponse = userApi.createUser(testUser);
        assertTrue("Пользователь должен быть создан", userApi.isCreatedSuccessfully(registerResponse));
        testAccessToken = userApi.getAccessToken(registerResponse);

        // Пытаемся залогиниться с неправильным паролем
        User wrongUser = new User(testUser.getEmail(), "wrongPassword123", testUser.getName());
        Response response = authApi.login(wrongUser);

        assertEquals("Должен возвращаться статус 401", SC_UNAUTHORIZED, response.statusCode());
        assertTrue("Должна быть ошибка неверных учетных данных",
                authApi.isInvalidCredentialsError(response));
    }

    @Test
    @DisplayName("Проверка доступности API")
    @Description("Проверка что API сервиса доступен и возвращает корректные данные")
    public void testApiIsAvailable() {
        Response response = orderApi.getIngredients();

        assertEquals("API должен возвращать статус 200", SC_OK, response.statusCode());
        assertTrue("Ответ должен содержать JSON", response.getContentType().contains("json"));

        // Проверяем структуру ответа
        assertNotNull("Должно быть поле success", response.path("success"));
        assertTrue("Success должно быть true", Boolean.TRUE.equals(response.path("success")));
        assertNotNull("Должен быть массив data", response.path("data"));
    }

    @Test
    @DisplayName("Вход с неверным email")
    @Description("Проверка ошибки авторизации при вводе несуществующего email")
    public void testLoginWithWrongEmail() {
        // Создаем случайный email, который точно не существует
        User wrongUser = new User("nonexistent" + System.currentTimeMillis() + "@example.com",
                generateRandomPassword(),
                generateRandomName());

        Response response = authApi.login(wrongUser);

        assertEquals("Должен возвращаться статус 401", SC_UNAUTHORIZED, response.statusCode());
        assertTrue("Должна быть ошибка неверных учетных данных",
                authApi.isInvalidCredentialsError(response));
    }

    @Test
    @DisplayName("Вход с пустыми полями")
    @Description("Проверка ошибки авторизации при отправке пустых данных")
    public void testLoginWithEmptyFields() {
        User emptyUser = new User("", "", "");

        Response response = authApi.login(emptyUser);

        assertEquals("Должен возвращаться статус 401", SC_UNAUTHORIZED, response.statusCode());
        assertTrue("Должна быть ошибка неверных учетных данных",
                authApi.isInvalidCredentialsError(response));
    }

    @Test
    @DisplayName("Вход с невалидным форматом email")
    @Description("Проверка ошибки авторизации при вводе email в невалидном формате")
    public void testLoginWithInvalidEmailFormat() {
        User invalidUser = new User("invalid-email-format", generateRandomPassword(), generateRandomName());

        Response response = authApi.login(invalidUser);

        assertEquals("Должен возвращаться статус 401", SC_UNAUTHORIZED, response.statusCode());
        assertTrue("Должна быть ошибка неверных учетных данных",
                authApi.isInvalidCredentialsError(response));
    }
}
package tests;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.*;

public class AuthTests extends BaseTest {

    private User testUser;
    private String testAccessToken;
    private String testRefreshToken;

    @Before
    public void setUpUser() {
        // Создаем пользователя перед каждым тестом
        testUser = generateRandomUser();

        Response registerResponse = userApi.createUser(testUser);

        assertEquals("Регистрация должна быть успешной", SC_OK, registerResponse.statusCode());
        assertTrue("Пользователь должен быть создан", userApi.isCreatedSuccessfully(registerResponse));

        testAccessToken = userApi.getAccessToken(registerResponse);
        testRefreshToken = registerResponse.path("refreshToken");

        assertNotNull("Должен возвращаться accessToken", testAccessToken);
        assertNotNull("Должен возвращаться refreshToken", testRefreshToken);

        System.out.println("=== User Created for Auth Tests ===");
        System.out.println("Email: " + testUser.getEmail());
        System.out.println("Password: " + testUser.getPassword());
        System.out.println("Name: " + testUser.getName());
        System.out.println("Access Token: " + testAccessToken);
        System.out.println("====================================");
    }

    @After
    public void tearDownUser() {
        // Удаляем пользователя после каждого теста
        if (testAccessToken != null) {
            try {
                Response deleteResponse = userApi.deleteUser(testAccessToken);
                System.out.println("=== User Deleted ===");
                System.out.println("Status: " + deleteResponse.statusCode());
                System.out.println("====================");
            } catch (Exception e) {
                System.err.println("Error cleaning up test user: " + e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Вход под существующим пользователем")
    @Description("Проверка успешной авторизации с корректными учетными данными")
    public void testLoginWithExistingUser() {
        // Используем пользователя, созданного в @Before
        Response loginResponse = authApi.login(testUser);

        assertEquals("Должен возвращаться статус 200", SC_OK, loginResponse.statusCode());
        assertTrue("Логин должен быть успешным", authApi.isLoginSuccessful(loginResponse));

        // Проверяем структуру ответа
        assertNotNull("Должен возвращаться accessToken", authApi.getAccessToken(loginResponse));
        assertNotNull("Должен возвращаться refreshToken", loginResponse.path("refreshToken"));
        assertNotNull("Должен возвращаться user объект", loginResponse.path("user"));
        assertEquals("Email должен совпадать", testUser.getEmail(), loginResponse.path("user.email"));
        assertEquals("Имя должно совпадать", testUser.getName(), loginResponse.path("user.name"));
    }

    @Test
    @DisplayName("Вход с неверным паролем")
    @Description("Проверка ошибки авторизации при вводе некорректного пароля")
    public void testLoginWithWrongPassword() {
        // Создаем пользователя с правильным email, но неправильным паролем
        User wrongUser = new User(testUser.getEmail(), "wrongPassword123", testUser.getName());

        Response response = authApi.login(wrongUser);

        assertEquals("Должен возвращаться статус 401", SC_UNAUTHORIZED, response.statusCode());
        assertTrue("Должна быть ошибка неверных учетных данных",
                authApi.isInvalidCredentialsError(response));
    }

    @Test
    @DisplayName("Вход с неверным email")
    @Description("Проверка ошибки авторизации при вводе несуществующего email")
    public void testLoginWithWrongEmail() {
        // Создаем пользователя с неправильным email
        User wrongUser = new User("wrong_" + testUser.getEmail(), testUser.getPassword(), testUser.getName());

        Response response = authApi.login(wrongUser);

        assertEquals("Должен возвращаться статус 401", SC_UNAUTHORIZED, response.statusCode());
        assertTrue("Должна быть ошибка неверных учетных данных",
                authApi.isInvalidCredentialsError(response));
    }

    @Test
    @DisplayName("Вход с пустым паролем")
    @Description("Проверка ошибки авторизации при отправке пустого пароля")
    public void testLoginWithEmptyPassword() {
        User emptyPasswordUser = new User(testUser.getEmail(), "", testUser.getName());

        Response response = authApi.login(emptyPasswordUser);

        // API может вернуть 400 или 401
        int statusCode = response.statusCode();
        assertTrue("Должен возвращаться статус ошибки (400 или 401)",
                statusCode == SC_BAD_REQUEST || statusCode == SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("Вход с пустым email")
    @Description("Проверка ошибки авторизации при отправке пустого email")
    public void testLoginWithEmptyEmail() {
        User emptyEmailUser = new User("", testUser.getPassword(), testUser.getName());

        Response response = authApi.login(emptyEmailUser);

        // API может вернуть 400 или 401
        int statusCode = response.statusCode();
        assertTrue("Должен возвращаться статус ошибки (400 или 401)",
                statusCode == SC_BAD_REQUEST || statusCode == SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("Вход с пустыми полями")
    @Description("Проверка ошибки авторизации при отправке пустых данных")
    public void testLoginWithEmptyFields() {
        User emptyUser = new User("", "", "");

        Response response = authApi.login(emptyUser);

        // API может вернуть 400 или 401
        int statusCode = response.statusCode();
        assertTrue("Должен возвращаться статус ошибки (400 или 401)",
                statusCode == SC_BAD_REQUEST || statusCode == SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("Вход с невалидным форматом email")
    @Description("Проверка ошибки авторизации при вводе email в невалидном формате")
    public void testLoginWithInvalidEmailFormat() {
        User invalidUser = new User("invalid-email-format", testUser.getPassword(), testUser.getName());

        Response response = authApi.login(invalidUser);

        // API может вернуть 400 или 401
        int statusCode = response.statusCode();
        assertTrue("Должен возвращаться статус ошибки (400 или 401)",
                statusCode == SC_BAD_REQUEST || statusCode == SC_UNAUTHORIZED);
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
    @DisplayName("Повторный вход с теми же данными")
    @Description("Проверка что можно войти несколько раз с одними и теми же данными")
    public void testMultipleLoginsWithSameCredentials() {
        // Первый вход
        Response firstLogin = authApi.login(testUser);
        assertEquals("Первый вход должен быть успешным", SC_OK, firstLogin.statusCode());

        // Второй вход
        Response secondLogin = authApi.login(testUser);
        assertEquals("Второй вход должен быть успешным", SC_OK, secondLogin.statusCode());

        // Третий вход
        Response thirdLogin = authApi.login(testUser);
        assertEquals("Третий вход должен быть успешным", SC_OK, thirdLogin.statusCode());

        System.out.println("=== Multiple Logins Test ===");
        System.out.println("All three logins were successful");
        System.out.println("=============================");
    }

    @Test
    @DisplayName("Проверка получения данных пользователя с токеном")
    @Description("Проверка что можно получить данные пользователя используя токен авторизации")
    public void testGetUserDataWithToken() {
        Response userDataResponse = authApi.getUserData(testAccessToken);

        assertEquals("Должен возвращаться статус 200", SC_OK, userDataResponse.statusCode());
        assertTrue("Запрос должен быть успешным",
                Boolean.TRUE.equals(userDataResponse.path("success")));

        assertEquals("Email должен совпадать",
                testUser.getEmail(),
                userDataResponse.path("user.email"));
        assertEquals("Имя должно совпадать",
                testUser.getName(),
                userDataResponse.path("user.name"));
    }
}
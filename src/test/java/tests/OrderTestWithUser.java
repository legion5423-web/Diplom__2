package tests;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.User;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class OrderTestWithUser extends BaseTest {
    protected User testUser;
    protected String accessToken;

    @Before
    public void setUpUser() {
        // Создаем пользователя перед каждым тестом
        testUser = generateRandomUser();
        Response createResponse = userApi.createUser(testUser);

        assertTrue("Пользователь должен быть создан", userApi.isCreatedSuccessfully(createResponse));
        accessToken = userApi.getAccessToken(createResponse);
        assertNotNull("Должен возвращаться accessToken", accessToken);
    }

    @After
    public void tearDownUser() {
        if (accessToken != null) {
            try {
                userApi.deleteUser(accessToken);
            } catch (Exception e) {
                System.err.println("Error deleting user: " + e.getMessage());
            }
        }
    }
}
package utils;

import com.github.javafaker.Faker;
import models.User;

import java.util.Locale;

public class DataGenerator {
    private static final Faker faker = new Faker(new Locale("en"));

    public static User generateRandomUser() {
        return new User(
                generateRandomEmail(),
                generateRandomPassword(),
                generateRandomName()
        );
    }

    public static String generateRandomEmail() {
        return faker.internet().emailAddress();
    }

    public static String generateRandomPassword() {
        return faker.internet().password(8, 16, true, true, true);
    }

    public static String generateRandomName() {
        return faker.name().firstName() + " " + faker.name().lastName();
    }

    public static String generateRandomInvalidEmail() {
        return faker.internet().emailAddress().replace("@", "");
    }
}
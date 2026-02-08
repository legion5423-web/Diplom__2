package helpers;

import java.util.UUID;

public class DataGenerator {
    public static String generateEmail() {
        return "test_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    public static String generateName() {
        return "TestUser_" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static String generatePassword() {
        return "Pass" + UUID.randomUUID().toString().substring(0, 8);
    }
}
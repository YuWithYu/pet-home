package com.pethome;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ApiTest {

    public static void main(String[] args) {
        try {
            testUserRegistration();
            Thread.sleep(1000);
            testUserLogin();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testUserRegistration() throws IOException {
        URL url = new URL("http://localhost:8080/api/users/register");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String jsonBody = "{\"username\":\"testuser\",\"password\":\"testpass\",\"email\":\"test@example.com\",\"phone\":\"13800138000\"}";

        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        int responseCode = connection.getResponseCode();
        System.out.println("Registration response code: " + responseCode);

        if (responseCode == 200) {
            System.out.println("Registration successful!");
        } else {
            System.out.println("Registration failed");
        }
    }

    public static void testUserLogin() throws IOException {
        URL url = new URL("http://localhost:8080/api/users/login");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String jsonBody = "{\"username\":\"testuser\",\"password\":\"testpass\"}";

        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        int responseCode = connection.getResponseCode();
        System.out.println("Login response code: " + responseCode);

        if (responseCode == 200) {
            System.out.println("Login successful!");
        } else {
            System.out.println("Login failed");
        }
    }
}

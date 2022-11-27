package com.my.conferences.validation;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.apache.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class RecaptchaValidation {

    private static final Logger logger = Logger.getLogger(RecaptchaValidation.class);
    private static final String SITE_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    private final String siteKey;
    private final String secretKey;

    public RecaptchaValidation(String siteKey, String secretKey) {
        this.siteKey = siteKey;
        this.secretKey = secretKey;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public boolean verify(String gRecaptchaResponse) {
        if (gRecaptchaResponse == null || gRecaptchaResponse.length() == 0) {
            return false;
        }

        try {
            URL verifyUrl = new URL(SITE_VERIFY_URL);
            HttpsURLConnection conn = (HttpsURLConnection) verifyUrl.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            String postParams = "secret=" + secretKey + "&response=" + gRecaptchaResponse;
            conn.setDoOutput(true);
            OutputStream outStream = conn.getOutputStream();
            outStream.write(postParams.getBytes());
            outStream.flush();
            outStream.close();

            int responseCode = conn.getResponseCode();
            logger.debug("responseCode: " + responseCode);

            InputStream is = conn.getInputStream();
            JsonReader jsonReader = Json.createReader(is);
            JsonObject jsonObject = jsonReader.readObject();
            jsonReader.close();

            logger.debug("Response: " + jsonObject);
            return jsonObject.getBoolean("success");
        } catch (IOException e) {
            logger.error("IOException", e);
            return false;
        }
    }
}

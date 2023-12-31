package com.exciting.vvue.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
@Getter
@Setter
public class FirebaseProperties {

    @Value("${cloud.gcp.firebase.service-account}")
    private Resource serviceAccount;

    public GoogleCredentials getGoogleCredentials() throws IOException {
        try (InputStream is = serviceAccount.getInputStream()) {
            return GoogleCredentials.fromStream(is);
        }
    }

    public FirebaseApp getFirebaseApp() throws IOException {
        return FirebaseApp.initializeApp(
                FirebaseOptions.builder()
                        .setCredentials(getGoogleCredentials())
                        .build()
        );
    }
}
package com.exciting.vvue.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class FirebaseConfig {

    private final FirebaseProperties firebaseProperties;

    @Bean
    GoogleCredentials googleCredentials() throws IOException {
        return firebaseProperties.getGoogleCredentials();
    }

    @Bean
    FirebaseApp firebaseApp() throws IOException {
        return firebaseProperties.getFirebaseApp();
    }

    @Bean
    FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
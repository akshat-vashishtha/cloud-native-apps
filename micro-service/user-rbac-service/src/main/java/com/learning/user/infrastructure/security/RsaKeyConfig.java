package com.learning.user.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class RsaKeyConfig {

    private final KeyPair keyPair;

    public RsaKeyConfig() {
        this.keyPair = generateRsaKeyPair();
    }

    @Bean
    public RSAPublicKey rsaPublicKey() {
        return (RSAPublicKey) keyPair.getPublic();
    }

    @Bean
    public RSAPrivateKey rsaPrivateKey() {
        return (RSAPrivateKey) keyPair.getPrivate();
    }

    private KeyPair generateRsaKeyPair() {
       try {
           KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
           keyPairGenerator.initialize(2048);
           return keyPairGenerator.generateKeyPair();
       } catch (NoSuchAlgorithmException exception) {
           throw new IllegalStateException("RSA Algorithm not available on JVM", exception);
       }
    }
}


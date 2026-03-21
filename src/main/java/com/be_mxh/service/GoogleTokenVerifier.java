package com.be_mxh.service;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

public interface GoogleTokenVerifier {
    GoogleIdToken verify(String idToken) throws Exception;
}

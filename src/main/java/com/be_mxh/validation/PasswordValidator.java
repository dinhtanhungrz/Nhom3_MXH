package com.be_mxh.validation;

public final class PasswordValidator {

    private PasswordValidator() {
    }

    public static boolean isConfirmPasswordMatched(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }

}
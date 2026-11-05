package com.example.myapplication;

import org.junit.Test;
import static org.junit.Assert.*;

public class PasswordHasherTest {
    @Test public void verifiesUnicodePasswordAndRejectsWrongPassword() {
        String password = "Mật khẩu 🔐 có khoảng trắng ";
        String encoded = PasswordHasher.hash(password);
        assertTrue(PasswordHasher.verify(password, encoded));
        assertFalse(PasswordHasher.verify("wrong", encoded));
        assertFalse(PasswordHasher.verify(password.trim(), encoded));
    }

    @Test public void saltsAreUniqueAndPlaintextIsNotStored() {
        String first = PasswordHasher.hash("secret");
        assertNotEquals(first, PasswordHasher.hash("secret"));
        assertFalse(first.contains("secret"));
        assertTrue(PasswordHasher.isEncoded(first));
    }

    @Test public void malformedAndUnboundedWorkFactorsAreRejected() {
        assertFalse(PasswordHasher.verify("secret", null));
        assertFalse(PasswordHasher.verify("secret", "secret"));
        assertFalse(PasswordHasher.verify("secret", "pbkdf2-sha1$2147483647$00$00"));
        assertFalse(PasswordHasher.verify("secret", "pbkdf2-sha1$1300000$zz$zz"));
        assertFalse(PasswordHasher.verify("secret", "pbkdf2-sha1$1300000"));
    }
}

package de.invees.portal.common.utils.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

public class SecurityUtils {

  public static final int KEY_LENGTH = 4096;
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final String APPLICATION_SALT = "LcKiSXlgguk3H7npqOuDXzLmNzn7m4sTGh4zCFE5YU7qIabyVfE40f9tiIMR9pbXH9TOJ0"
      + "kfkSevHyEnz5JBGK1FUQxBPZP1nPnyQWw6ugUGaAO6auyxw75PmbfcPHxWKRDDSbERUn9pUHvaxgxC8QOpfg29LJcimS7mKN1DQBbdPhzIl9JPNkf"
      + "t8DV2R54GEXLtR7crRyKsh23vTSFG69kMb2IbJALku2wDuBEM0wlHzJaJvER0a2nWgwNVzBxaUQ9R7DlGYdjYBlz0oVnhO4I5ylBWyZzOhG8ZD9c3"
      + "n3jiNGSU8Vt1rGObMg8iMNBJC9v6S8AolTPMAyqk3THBAVJPwRDEZIaX0g0Wq7nXCE8cRxnfxuIZQEioeK4EPjH0wto40Yw1IFmJBp8Z";
  private static final String ALGORITHM = "PBKDF2WithHmacSHA512";

  public static String hash(String password, String salt, UUID userId) {
    char[] chars = (password + APPLICATION_SALT + userId.toString()).toCharArray();
    byte[] bytes = (salt + APPLICATION_SALT).getBytes();

    PBEKeySpec spec = new PBEKeySpec(chars, bytes, 40000, KEY_LENGTH);
    Arrays.fill(chars, Character.MIN_VALUE);

    try {
      SecretKeyFactory fac = SecretKeyFactory.getInstance(ALGORITHM);
      byte[] securePassword = fac.generateSecret(spec).getEncoded();
      return Base64.getEncoder().encodeToString(securePassword);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
      throw new IllegalArgumentException("Password could not be hashed.");
    } finally {
      spec.clearPassword();
    }
  }

  public static String generateSalt(final int length) {
    byte[] salt = new byte[length];
    RANDOM.nextBytes(salt);
    return Base64.getEncoder().encodeToString(salt);
  }

}

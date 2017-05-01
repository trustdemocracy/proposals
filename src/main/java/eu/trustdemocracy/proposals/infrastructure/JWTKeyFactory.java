package eu.trustdemocracy.proposals.infrastructure;

import java.security.PrivateKey;
import java.security.PublicKey;

public final class JWTKeyFactory {

  private static PrivateKey privateKey;
  private static PublicKey publicKey;

  public static void setPrivateKey(PrivateKey privateKey) {
    JWTKeyFactory.privateKey = privateKey;
  }

  public static PrivateKey getPrivateKey() {
    return privateKey;
  }

  public static void setPublicKey(PublicKey publicKey) {
    JWTKeyFactory.publicKey = publicKey;
  }

  public static PublicKey getPublicKey() {
    return publicKey;
  }

}

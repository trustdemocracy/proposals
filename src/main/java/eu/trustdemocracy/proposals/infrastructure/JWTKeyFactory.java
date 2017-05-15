package eu.trustdemocracy.proposals.infrastructure;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import lombok.val;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.lang.JoseException;

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

  public static void initKeys() throws Exception {
    try {
      val publicKeyFilename = System.getenv("public_key");
      val privateKeyFilename = System.getenv("private_key");

      PrivateKey privateKey;
      PublicKey publicKey;

      if (publicKeyFilename == null || privateKeyFilename == null) {
        val rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
        privateKey = rsaJsonWebKey.getPrivateKey();
        publicKey = rsaJsonWebKey.getPublicKey();
      } else {
        privateKey = getPrivateKey(privateKeyFilename);
        publicKey = getPublicKey(publicKeyFilename);
      }

      JWTKeyFactory.setPrivateKey(privateKey);
      JWTKeyFactory.setPublicKey(publicKey);
    } catch (JoseException e) {
      throw new RuntimeException(e);
    }
  }

  private static PrivateKey getPrivateKey(String filename) throws Exception {
    val keyBytes = Files.readAllBytes(new File(filename).toPath());
    val spec = new PKCS8EncodedKeySpec(keyBytes);
    val keyFactory = KeyFactory.getInstance("RSA");
    return keyFactory.generatePrivate(spec);
  }

  private static PublicKey getPublicKey(String filename) throws Exception {
    byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
    val spec = new X509EncodedKeySpec(keyBytes);
    val keyFactory = KeyFactory.getInstance("RSA");
    return keyFactory.generatePublic(spec);
  }

}

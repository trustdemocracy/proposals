package eu.trustdemocracy.proposals.core.interactors.util;

import eu.trustdemocracy.proposals.infrastructure.JWTKeyFactory;
import java.util.UUID;
import lombok.val;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

public final class TokenUtils {

  public static String createToken(UUID id, String username) {
    val claims = new JwtClaims();
    claims.setExpirationTimeMinutesInTheFuture(10);
    claims.setGeneratedJwtId();
    claims.setIssuedAtToNow();
    claims.setNotBeforeMinutesInThePast(2);

    claims.setSubject(id.toString());
    claims.setClaim("username", username);

    JsonWebSignature jws = new JsonWebSignature();
    jws.setPayload(claims.toJson());
    jws.setKey(JWTKeyFactory.getPrivateKey());
    jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

    try {
      return jws.getCompactSerialization();
    } catch (JoseException e) {
      throw new RuntimeException(e);
    }
  }

}

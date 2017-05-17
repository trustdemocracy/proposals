package eu.trustdemocracy.proposals.core.entities.util;

import eu.trustdemocracy.proposals.core.entities.User;
import eu.trustdemocracy.proposals.core.interactors.exceptions.InvalidTokenException;
import eu.trustdemocracy.proposals.infrastructure.JWTKeyFactory;
import java.util.Map;
import java.util.UUID;
import lombok.val;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

public final class UserMapper {

  public static User createEntity(String token) {
    val claims = getClaimsMap(token);
    return new User()
        .setId(UUID.fromString(String.valueOf(claims.get("sub"))))
        .setUsername(String.valueOf(claims.get("username")));
  }

  private static Map<String, Object> getClaimsMap(String token) {
    val jwtConsumer = new JwtConsumerBuilder()
        .setRequireExpirationTime()
        .setAllowedClockSkewInSeconds(30)
        .setRequireSubject()
        .setVerificationKey(JWTKeyFactory.getPublicKey())
        .setJwsAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.WHITELIST,
            AlgorithmIdentifiers.RSA_USING_SHA256))
        .build();

    try {
      val jwtClaims = jwtConsumer.processToClaims(token);
      return jwtClaims.getClaimsMap();
    } catch (InvalidJwtException e) {
      throw new InvalidTokenException(
          "The access token provided is not valid. Access token: [" + token + "]");
    }
  }
}

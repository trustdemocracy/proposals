package eu.trustdemocracy.proposals.infrastructure;

import eu.trustdemocracy.proposals.gateways.CommentDAO;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;
import eu.trustdemocracy.proposals.gateways.mysql.MySqlCommentDAO;
import eu.trustdemocracy.proposals.gateways.mysql.MySqlProposalDAO;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import lombok.val;

public class DAOFactory {

  private static final String DATABASE_PROPERTIES_FILE = "database.properties";
  private static final String PROPERTIES_KEY_URL = "db_url";
  private static final String PROPERTIES_KEY_USERNAME = "db_username";
  private static final String PROPERTIES_KEY_PASSWORD = "db_password";

  public static ProposalDAO getProposalDAO() {
    return new MySqlProposalDAO(getConnection());
  }

  public static CommentDAO getCommentDAO() {
    return new MySqlCommentDAO(getConnection());
  }

  private static Connection getConnection() {
    Properties properties;
    try {
      properties = getProperties();
    } catch (Exception e) {
      throw new RuntimeException("Failed to read database properties file ["
          + DATABASE_PROPERTIES_FILE + "]", e);
    }

    throwIfMissingKey(properties, PROPERTIES_KEY_URL);
    throwIfMissingKey(properties, PROPERTIES_KEY_USERNAME);
    throwIfMissingKey(properties, PROPERTIES_KEY_PASSWORD);

    val url = properties.getProperty(PROPERTIES_KEY_URL);
    val username = properties.getProperty(PROPERTIES_KEY_USERNAME);
    val password = properties.getProperty(PROPERTIES_KEY_PASSWORD);

    try {
      return DriverManager.getConnection(url, username, password);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to connect to the database", e);
    }
  }

  private static void throwIfMissingKey(Properties properties, String key) {
    if (!properties.containsKey(key)) {
      throw new RuntimeException(
          "Unable to find key " + key + "in " + DATABASE_PROPERTIES_FILE);
    }
  }

  private static Properties getProperties() throws Exception {
    val properties = new Properties();
    val inputStream = DAOFactory.class.getClassLoader()
        .getResourceAsStream(DATABASE_PROPERTIES_FILE);
    properties.load(inputStream);
    inputStream.close();

    loadSystemProperties(properties);

    return properties;
  }

  private static void loadSystemProperties(Properties properties) {
    val url = System.getenv(PROPERTIES_KEY_URL);
    if (url != null) {
      properties.put(PROPERTIES_KEY_URL, url);
    }
    val username = System.getenv(PROPERTIES_KEY_USERNAME);
    if (username != null) {
      properties.put(PROPERTIES_KEY_USERNAME, username);
    }
    val password = System.getenv(PROPERTIES_KEY_PASSWORD);
    if (password != null) {
      properties.put(PROPERTIES_KEY_PASSWORD, password);
    }
  }
}

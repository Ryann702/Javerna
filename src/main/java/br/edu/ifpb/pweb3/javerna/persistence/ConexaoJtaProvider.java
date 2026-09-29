package br.edu.ifpb.pweb3.javerna.persistence;

import com.arjuna.ats.jdbc.TransactionalDriver;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.spi.Configurable;
import org.postgresql.xa.PGXADataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/** Liga as conexoes PostgreSQL a transacao coordenada pelo Narayana. */
public class ConexaoJtaProvider implements ConnectionProvider, Configurable {

    private final TransactionalDriver driver = new TransactionalDriver();
    private final Properties properties = new Properties();

    @Override
    public void configure(Map<String, Object> configuration) {
        PGXADataSource dataSource = new PGXADataSource();
        dataSource.setUrl((String) configuration.get("jakarta.persistence.jdbc.url"));
        dataSource.setUser((String) configuration.get("jakarta.persistence.jdbc.user"));
        dataSource.setPassword((String) configuration.get("jakarta.persistence.jdbc.password"));
        properties.put(TransactionalDriver.XADataSource, dataSource);
        properties.setProperty(TransactionalDriver.poolConnections, "false");
    }

    @Override
    public Connection getConnection() throws SQLException {
        return driver.connect(TransactionalDriver.arjunaDriver, properties);
    }

    @Override
    public void closeConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> type) {
        return type.isInstance(this);
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        if (!isUnwrappableAs(type)) {
            throw new IllegalArgumentException("Tipo de conexao nao suportado: " + type.getName());
        }
        return type.cast(this);
    }
}

package cz.cvut.kbss.keycloak.provider;

import org.eclipse.rdf4j.repository.Repository;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataReplicationProviderFactory implements EventListenerProviderFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DataReplicationProviderFactory.class);

    private Configuration configuration;

    private Repository repository;

    @Override
    public EventListenerProvider create(KeycloakSession keycloakSession) {
        if (repository == null) {
            // Init persistence factory lazily, because GraphDB won't start until its OIDC provider (Keycloak) is available
            this.repository = PersistenceFactory.connect(configuration);
        }
        final DataReplicationProvider provider = new DataReplicationProvider(configuration);
        provider.setUserProvider(keycloakSession.users());
        provider.setRealmProvider(keycloakSession.realms());
        provider.setUserAccountDao(new UserAccountDao(repository.getConnection()));
        return provider;
    }

    @Override
    public void init(Config.Scope scope) {
        LOG.debug("Loading configuration from scope.");
        KodiUserAccount.setNamespace(scope.get("namespace"));
        KodiUserAccount.setContext(scope.get("context"));
        this.configuration = new Configuration(scope);
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
    }

    @Override
    public void close() {
        repository.shutDown();
    }

    @Override
    public String getId() {
        return "keycloak-graphdb-user-replicator";
    }
}

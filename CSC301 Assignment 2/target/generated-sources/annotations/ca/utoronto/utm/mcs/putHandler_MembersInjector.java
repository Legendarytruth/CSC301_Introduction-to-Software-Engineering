package ca.utoronto.utm.mcs;

import com.mongodb.client.MongoClient;
import com.sun.net.httpserver.HttpServer;
import dagger.MembersInjector;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class putHandler_MembersInjector implements MembersInjector<putHandler> {
  private final Provider<MongoClient> clientProvider;

  private final Provider<HttpServer> serverProvider;

  public putHandler_MembersInjector(
      Provider<MongoClient> clientProvider, Provider<HttpServer> serverProvider) {
    this.clientProvider = clientProvider;
    this.serverProvider = serverProvider;
  }

  public static MembersInjector<putHandler> create(
      Provider<MongoClient> clientProvider, Provider<HttpServer> serverProvider) {
    return new putHandler_MembersInjector(clientProvider, serverProvider);
  }

  @Override
  public void injectMembers(putHandler instance) {
    injectDatabaseCreator(instance, clientProvider.get(), serverProvider.get());
  }

  public static void injectDatabaseCreator(
      putHandler instance, MongoClient client, HttpServer server) {
    instance.databaseCreator(client, server);
  }
}

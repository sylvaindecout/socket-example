package test.sdc.socket.server;

import dagger.ObjectGraph;

import java.util.concurrent.Executors;

/**
 * Entry point of the application.
 */
public final class Application {

    /**
     * Main method.
     *
     * @param args start-up arguments
     */
    public static void main(final String[] args) {
        final ObjectGraph objectGraph = ObjectGraph.create(new ServerModule());
        final Server server = objectGraph.get(Server.class);
        Executors.newSingleThreadExecutor().submit(server);
    }

}
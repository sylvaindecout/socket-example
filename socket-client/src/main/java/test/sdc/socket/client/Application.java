package test.sdc.socket.client;

import dagger.ObjectGraph;

import java.util.concurrent.Executors;

public final class Application {

    /**
     * Main method.
     *
     * @param args start-up arguments
     */
    public static void main(final String[] args) {
        final ObjectGraph objectGraph = ObjectGraph.create(new ClientModule());
        final Client server = objectGraph.get(Client.class);
        Executors.newSingleThreadExecutor().submit(server);
    }

}

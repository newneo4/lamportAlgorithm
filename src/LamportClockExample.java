import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

// Clase que representa el reloj de Lamport
class LamportClock {
    private int latestTime;

    public LamportClock(int timestamp) {
        this.latestTime = timestamp;
    }

    // Avanzar el reloj para reflejar un nuevo evento
    public int tick(int requestTime) {
        latestTime = Math.max(latestTime, requestTime) + 1;
        return latestTime;
    }

    // Obtener la última marca de tiempo
    public int getLatestTime() {
        return latestTime;
    }

    // Actualizar el reloj a una nueva marca de tiempo
    public void updateTo(int timestamp) {
        latestTime = Math.max(latestTime, timestamp);
    }
}

// Clase para representar una clave versionada
class VersionedKey {
    private final String key;
    private final int version;

    public VersionedKey(String key, int version) {
        this.key = key;
        this.version = version;
    }

    @Override
    public String toString() {
        return key + "@" + version;
    }
}

// Clase que representa un servidor
class Server {
    private final LamportClock clock;
    private final Map<VersionedKey, String> store = new HashMap<>();

    public Server() {
        this.clock = new LamportClock(1);
    }

    // Escribir un valor en el servidor
    public int write(String key, String value, int requestTimestamp) {
        int writeAtTimestamp = clock.tick(requestTimestamp);
        VersionedKey versionedKey = new VersionedKey(key, writeAtTimestamp);
        store.put(versionedKey, value);
        System.out.println("Servidor: Escribió la clave " + versionedKey + " con valor \"" + value + "\" en el tiempo " + writeAtTimestamp);
        return writeAtTimestamp;
    }

    // Leer el valor de una clave versionada
    public String read(VersionedKey key) {
        return store.get(key);
    }
}

// Clase que representa un cliente
class Client {
    private final LamportClock clock = new LamportClock(1);

    public void performWrites(List<Server> servers) {
        // Escribir en todos los servidores
        for (int i = 0; i < servers.size(); i++) {
            String key = "clave" + i;
            String value = "valor" + i;
            int writeAtTimestamp = servers.get(i).write(key, value, clock.getLatestTime());
            clock.updateTo(writeAtTimestamp);
        }

        // Mostrar estado final de los servidores
        System.out.println("Cliente: Todas las escrituras se han realizado. Hora actual del cliente: " + clock.getLatestTime());
    }
}

// Clase que representa un cliente que realiza lecturas
class ReadClient {
    private final LamportClock clock = new LamportClock(1);

    public void performReads(List<Server> servers) {
        for (int i = 0; i < servers.size(); i++) {
            String key = "clave" + i;
            VersionedKey versionedKey = new VersionedKey(key, clock.getLatestTime());
            String value = servers.get(i).read(versionedKey);
            System.out.println("Cliente de Lectura: Leyó la clave " + key + " con valor \"" + value + "\" en el tiempo " + clock.getLatestTime());
            clock.tick(clock.getLatestTime());
        }
    }
}

public class LamportClockExample {
    public static void main(String[] args) {
        // Crear servidores
        Server server1 = new Server();
        Server server2 = new Server();
        Server server3 = new Server();
        List<Server> servers = new ArrayList<>();
        servers.add(server1);
        servers.add(server2);
        servers.add(server3);

        // Crear clientes
        Client client1 = new Client();
        Client client2 = new Client();
        ReadClient readClient = new ReadClient();

        // Los clientes realizan escrituras
        System.out.println("Cliente1 comienza a escribir...");
        client1.performWrites(servers);

        System.out.println("\nCliente2 comienza a escribir...");
        client2.performWrites(servers);

        // Leer los datos desde los servidores
        System.out.println("\nCliente de Lectura comienza a leer...");
        readClient.performReads(servers);
    }
}

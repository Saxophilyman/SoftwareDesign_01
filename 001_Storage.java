//Решение

// DbStorage.java
import lombok.RequiredArgsConstructor;

import java.sql.*;

@RequiredArgsConstructor
public class DbStorage implements Storage {
    private final Connection connection;

    public DbStorage(Connection connection) {
        this.connection = connection;
        initSchema();
    }

    private void initSchema() {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS storage (
                    id   INT PRIMARY KEY,
                    data VARCHAR(1000)
                )
            """);
        } catch (SQLException e) {
            throw new RuntimeException("Init schema failed", e);
        }
    }

    @Override
    public void save(String data) {
        int id = nextId();
        try (PreparedStatement ps =
                     connection.prepareStatement("INSERT INTO storage(id, data) VALUES(?, ?)")) {
            ps.setInt(1, id);
            ps.setString(2, data);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Save failed", e);
        }
    }

    @Override
    public String retrieve(int id) {
        try (PreparedStatement ps =
                     connection.prepareStatement("SELECT data FROM storage WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("data") : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Retrieve failed", e);
        }
    }





//---КОД ЗАДАНИЯ----------------------------------------------------
public interface Storage {
    void save(String data);
    String retrieve(int id);
}

// InMemoryStorage.java
import java.util.HashMap;
import java.util.Map;

public class InMemoryStorage implements Storage {
    private Map<Integer, String> storage = new HashMap<>();
    private int counter = 0;

    @Override
    public void save(String data) {
        storage.put(counter++, data);
    }

    @Override
    public String retrieve(int id) {
        return storage.get(id);
    }
}

// FileStorage.java
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FileStorage implements Storage {
    private File file = new File("storage.txt");
    private Map<Integer, String> storage = new HashMap<>();

    public FileStorage() {
        loadFromFile();
    }

    @Override
    public void save(String data) {
        storage.put(storage.size(), data);
        saveToFile();
    }

    @Override
    public String retrieve(int id) {
        return storage.get(id);
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = 
            new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(storage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        if (!file.exists()) return;
        
        try (ObjectInputStream ois = 
             new ObjectInputStream(new FileInputStream(file))) {
            storage = (Map<Integer, String>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

// Main.java
public class Main {
    public static void main(String[] args) {
        Storage memoryStorage = new InMemoryStorage();
        memoryStorage.save("Data in memory");
        System.out.println("InMemoryStorage: " + memoryStorage.retrieve(0));

        Storage fileStorage = new FileStorage();
        fileStorage.save("Data in file");
        System.out.println("FileStorage: " + fileStorage.retrieve(0));

//------------------------------------------
        try (Connection conn = DriverManager.getConnection(
                "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "")) {

            Storage storage = new DbStorage(conn);

            storage.save("Hello DB 0");
            storage.save("Hello DB 1");
            storage.save("Hello DB 2");

            System.out.println("id 0 -> " + storage.retrieve(0));
            System.out.println("id 1 -> " + storage.retrieve(1));
            System.out.println("id 2 -> " + storage.retrieve(2));
        }
    }
}


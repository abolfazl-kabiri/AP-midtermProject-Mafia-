import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * The type Simple citizen.
 */
public class SimpleCitizen extends Citizen{

    /**
     * Instantiates a new Simple citizen.
     */
    public SimpleCitizen() {
        super();
    }

    @Override
    public String toString() {
        return "SimpleCitizen";
    }

    @Override
    public String action(ObjectOutputStream out, ObjectInputStream in, Server server) {

        String response = "";

        return response;
    }
}

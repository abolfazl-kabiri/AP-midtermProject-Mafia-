import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * The type Simple mafia.
 */
public class SimpleMafia extends Mafia{

    /**
     * Instantiates a new Simple mafia.
     */
    public SimpleMafia() {
        super();
    }

    @Override
    public String toString() {
        return "SimpleMafia";
    }

    @Override
    public String action(ObjectOutputStream out, ObjectInputStream in, Server server) {

        String response = "";

        return response;
    }
}

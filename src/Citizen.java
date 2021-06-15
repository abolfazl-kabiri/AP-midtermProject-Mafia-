import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * The type Citizen.
 */
public abstract class Citizen extends Role{

    /**
     * Instantiates a new Citizen.
     */
    public Citizen() {
        super();
    }

    @Override
    public abstract String toString();

    @Override
    public abstract String action(ObjectOutputStream out, ObjectInputStream in, Server server);
}

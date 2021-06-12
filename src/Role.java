import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public abstract class Role implements Serializable {

    public abstract String toString();

    public abstract String action(ObjectOutputStream out, ObjectInputStream in, Server server);
}

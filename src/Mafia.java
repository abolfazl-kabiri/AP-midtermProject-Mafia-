import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class Mafia extends Role{

    @Override
    public abstract String toString();

    @Override
    public abstract String action(ObjectOutputStream out, ObjectInputStream in, Server server);
}

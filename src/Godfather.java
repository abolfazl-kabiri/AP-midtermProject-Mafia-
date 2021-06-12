import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Godfather extends Mafia{
    @Override
    public String toString() {
        return "Godfather";
    }

    @Override
    public String action(ObjectOutputStream out, ObjectInputStream in, Server server) {

        String response = "";

        return response;
    }
}

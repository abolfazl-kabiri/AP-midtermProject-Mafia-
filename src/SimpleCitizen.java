import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SimpleCitizen extends Citizen{

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

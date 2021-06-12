import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Psychologist extends Citizen{

    @Override
    public String toString() {
        return "Psychologist";
    }

    @Override
    public String action(ObjectOutputStream out, ObjectInputStream in, Server server) {

        String response = "";

        return response;
    }
}

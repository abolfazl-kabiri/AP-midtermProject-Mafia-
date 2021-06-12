import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Detective extends Citizen{

    @Override
    public String toString() {
        return "Detective";
    }

    @Override
    public String action(ObjectOutputStream out, ObjectInputStream in, Server server) {

        String response = "";

        return response;
    }
}

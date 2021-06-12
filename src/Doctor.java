import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Doctor extends Citizen{

    @Override
    public String toString() {
        return "Doctor";
    }

    @Override
    public String action(ObjectOutputStream out, ObjectInputStream in, Server server) {

        String response = "";

        return response;
    }
}

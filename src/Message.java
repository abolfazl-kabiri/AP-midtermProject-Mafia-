import java.io.Serializable;

/**
 * The type Message.
 */
public class Message implements Serializable {

    private String text;

    /**
     * Instantiates a new Message.
     *
     * @param text the text
     */
    public Message(String text) {
        this.text = text;
    }

    /**
     * Gets text.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }
}

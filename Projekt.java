import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Projekt {
    private static final Logger logger = LogManager.getLogger(Projekt.class);

    public static void main(String[] args) {
        logger.info("Start programu");

        try {
            Program program = new Program();
            Program.main(args);
        } catch (Exception e) {
            logger.error("Wystąpił błąd podczas uruchamiania programu", e);
        }

    }
}

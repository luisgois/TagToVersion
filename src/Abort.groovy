/**
 * Created by goi on 5/18/17.
 */
class Abort {
    static no_usage(String message) {
        no_usage(message, 1)
    }

    static no_usage(String message, int status) {
        System.err.println(message)
        System.exit(status);
    }
}

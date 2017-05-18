/**
 * Created by goi on 5/18/17.
 */
enum JiraStatus {
    OPEN("Open"), IN_PROGRESS("In Progress"), DONE("Done");

    JiraStatus(String value) { this.value = value }
    private final String value
    String value() { return value }
}

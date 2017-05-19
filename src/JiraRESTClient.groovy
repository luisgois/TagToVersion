// require(groupId:'org.codehaus.groovy.modules.http-builder', artifactId:'http-builder', version:'0.5.2')
import net.sf.json.groovy.*
import groovyx.net.http.RESTClient
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor

import javax.xml.ws.spi.http.HttpContext
import java.util.Base64;

/**
 * JIRA REST client wrapper around groovyx.net.http.RESTClient
 */

class JiraRESTClient extends groovyx.net.http.RESTClient {


    def credentials = [:]

    private JiraRESTClient(String url, String username, String password) {
        super(url)

        // Use basic authentication
        // TODO use Oauth
        String base64Credentials = new String(Base64.getEncoder().encode("${username}:${password}".getBytes()))
        setHeaders([Authorization: "Basic ${base64Credentials}"])
        auth.basic username, password

        log.debug "Created for user=${username}, url=" + url

    }

    /**
     * Create a REST client
     */
    static JiraRESTClient create() {

        Properties properties = new Properties()
        File propertiesFile = new File(System.env.HOME + "/.acm/acm.properties")
        propertiesFile.withInputStream {
            properties.load(it)
        }

        String defaultJiraUrl = properties.DEFAULT_JIRA_URL
        String defaultJiraSearchUrl = "${defaultJiraUrl}/rest/api/latest/"
        String defaultUsername = properties.DEFAULT_USERNAME
        String defaultPassword = properties.DEFAULT_PASSWORD

        return new JiraRESTClient(defaultJiraSearchUrl, defaultUsername, defaultPassword)
    }

    /**
     * Search JIRA multiple values for the field specified in the path
     * @returns response
     * @throws IllegalArgumentException in case of bad JQL query
     * @throws IOException in case of JIRA connection failure
     * @see JiraQueryBuilder to create these JQL queries
     */

    def get(String path) {
        return get(path, "")
    }

    /**
     * Search JIRA multiple values for the field specified in the path or results of query
     * @returns response
     * @throws IllegalArgumentException in case of bad JQL query
     * @throws IOException in case of JIRA connection failure
     * @see JiraQueryBuilder to create these JQL queries
     */
    def get(String path, def query) {
        try {
            def response
            if (query == "") {
                response = get(path: path, contentType: "application/json")
            }
            else {
                response = get(path: path, contentType: "application/json", query: query)
            }

            assert response.status == 200
            return response
        } catch (groovyx.net.http.HttpResponseException e) {
            if (e.response.status == 400) {
                // HTTP 400: Bad Request, JIRA returned error
                throw new IllegalArgumentException("JIRA query failed, response data=${e.response.data}", e)
            } else {
                throw new IOException("JIRA connection failed, got HTTP status ${e.response.status}, response data=${e.response.data}", e)
            }

        }
    }

    /**
     * update JIRA issue field using with given JSON payload (POST)
     * @returns response
     * @throws IllegalArgumentException in case of bad JQL query
     * @throws IOException in case of JIRA connection failure
     * @see
     */
    def post(String path, def jsonBody) {

        try {
            def response = post(path: path, contentType: "application/json", body: jsonBody)
            return response
        } catch (groovyx.net.http.HttpResponseException e) {
            if (e.response.status == 400) {
                // HTTP 400: Bad Request, JIRA returned error
                throw new IllegalArgumentException("JIRA query failed, got HTTP status 400, response data=${e.response.data}", e)
            } else {
                throw new IOException("JIRA connection failed, got HTTP status ${e.response.status}, response data=${e.response.data}", e)
            }

        }
    }

    /**
     * update JIRA issue field using with given JSON payload (PUT)
     * @returns response
     * @throws IllegalArgumentException in case of bad JQL query
     * @throws IOException in case of JIRA connection failure
     * @see
     */
    def put(String path, def jsonBody) {

        try {
            def response = put(path: path, contentType: "application/json", body: jsonBody)
            return response
        } catch (groovyx.net.http.HttpResponseException e) {
            if (e.response.status == 400) {
                // HTTP 400: Bad Request, JIRA returned error
                throw new IllegalArgumentException("JIRA query failed, got HTTP status 400, response data=${e.response.data}", e)
            } else {
                throw new IOException("JIRA connection failed, got HTTP status ${e.response.status}, response data=${e.response.data}", e)
            }
        }
    }

    /**
     * Search JIRA with provided JQL e.g. "project = XXX AND ..."
     * @returns response
     * @throws IllegalArgumentException in case of bad JQL query
     * @throws IOException in case of JIRA connection failure
     * @see JiraQueryBuilder to create these JQL queries
     */
    def search(String jql) {
        assert jql

        def query = [:]
        query << credentials
        query['jql'] = jql

        query['startAt'] = 0
        query['maxResults'] = 1000

        log.debug "Searching with JQL: " + jql
        return get("search", query)
    }

    /**
     * Return an object instance representing a JIRA issue
     * @returns Issue
     * @throws
     * @throws
     * @see
     */
    Issue issue(String key) {
        return new Issue(this, key)
    }

    /**
     * Return an object instance representing a JIRA version
     * @returns Issue
     * @throws
     * @throws
     * @see
     */
    Version version (String name, String description) {
        return new Version(this, name, description)
    }

    /**
     * add value to issue's field (array valued field)
     * @returns response
     * @throws
     * @throws
     * @see
     */
    def updateAdd(Issue issue, String field, String value) {
        def key = issue.key

        log.info ("${field} update for issue ${key}: ${value} ...")

        // update issue, appending version to fixVersion of identified issueKey
        String jsonBody = String.format(
                "{\"update\":{\"%s\":[{\"add\":{\"name\":\"%s\"}}]}}",
                field,
                value)

        def response = put("/rest/api/2/issue/${key}", jsonBody)

        log.debug "PUT response status: ${response.statusLine}"

        assert response.statusLine.statusCode == 204
        return response
    }

    /**
     * add value to possible list of values of a field
     * @returns response
     * @throws
     * @throws
     * @see
     */
    def addToList(String project, String listName, String listValue, String listValueDescription) {

        def existingValues = get("project/${project}/${listName}")

        def found
        existingValues.data.find { value ->
            if (value.name == listValue) {
                found = listValue
                return true
            }
            return false // keep loopping
        }

        if (found == listValue) {
            log.info"${listName} already contains ${listValue}!"
        } else {
            log.info"Adding new value ${listValue} to list ${listName} of project ${project} ..."

            /* Example JSON payload for a version creation:
            {
                "description": "An excellent version",
                "name": "New Version 1",
                "archived": false,
                "released": true,
                "releaseDate": "2010-07-06",
                "userReleaseDate": "6/Jul/2010",
                "project": "PXA",
                "projectId": 10000
            }
             */

            def jsonBody = [
                    description: listValueDescription,
                    name       : listValue,
                    archived   : false,
                    released   : true,
                    releaseDate: new Date().format('yyyy-MM-dd'),
                    project    : project
            ]

            def response = post("/rest/api/2/version", jsonBody)

            log.debug "POST response status: ${response.statusLine}"
            assert response.statusLine.statusCode == 201
            return response
        }
    }
}
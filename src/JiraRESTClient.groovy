// require(groupId:'org.codehaus.groovy.modules.http-builder', artifactId:'http-builder', version:'0.5.2')
import net.sf.json.groovy.*
import groovyx.net.http.RESTClient

/**
 * JIRA REST client wrapper around groovyx.net.http.RESTClient
 */
class JiraRESTClient extends groovyx.net.http.RESTClient {

    class Version {
        String name
        String description
        String project
        JiraRESTClient jira

        Version (String project, String name, String description, JiraRESTClient jira) {
            this.project = project
            this.name = name
            this.jira = jira
            this.description = description

            add()
        }

        Version (String project, String name, JiraRESTClient jira) {
            Version (project, name, null, jira)
        }


        // add version to JIRA
        String add() {
            // url: /rest/api/2/project/%s/versions
            def response = jira.get("/rest/api/2/project/${jira.project}/versions")

            def found
            response.each{ version ->
                if (name == version) {
                    found = version
                }
            }

            if (found == name) {
                log.info ("Version ${this.name} already exists!")
            }
            else {
                log.info ("Adding new version ${this.name} to project ${this.project} ...")

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

                def jsonBody = [description: description, name: name, archived: false, released: true, releaseDate: new Date().format( 'yyyy-MM-dd' ), project: project]
                jira.post(path: "/rest/api/2/version", body: jsonBody) { resp ->
                    println "POST response status: ${resp.statusLine}"
                    assert resp.statusLine.statusCode == 201
                }
            }

            /*//Create a json body with all tested fields
            def jsonBody = [:]
            // Test title
            jsonBody.put("title", "Test title")
            // Test parentID
            jsonBody.put("parentId", "4f5fc39de4b098845cbcb45e")*/
        }

        // TODO affect and fix in one common function for the post and error check

        def affect(Issue issue) {
            def key = issue.key

            log.info ("Version ${this.name} is a affectVersion for ${this.project}.${issueKey} ...")

            // update issue, appending version to fixVersion of identified issueKey
            String jsonBody = String.format(
                    "{\"update\":{\"affectVersions\":[{\"add\":{\"name\":\"%s\",\"project\":\"%s\"}}]}}",
                    this.name,
                    this.project);
            jira.post(path: "/rest/api/2/issue/" + issueKey, jsonBody) { resp ->
                println "POST response status: ${resp.statusLine}"
                assert resp.statusLine.statusCode == 201
            }
        }

        def fix(Issue issue) {
            def key = issue.key

            log.info ("Version ${this.name} is a fixVersion for ${this.project}.${issueKey} ...")

            // update issue, appending version to fixVersion of identified issueKey
            String jsonBody = String.format(
                    "{\"update\":{\"fixVersions\":[{\"add\":{\"name\":\"%s\",\"project\":\"%s\"}}]}}",
                    this.name,
                    this.project);
            jira.post(path: "/rest/api/2/issue/" + issueKey, jsonBody) { resp ->
                println "POST response status: ${resp.statusLine}"
                assert resp.statusLine.statusCode == 201
            }
        }
    }

    class Issue {
        String key
        JiraRESTClient jira

        def getProjectKey() {
            return key.substring(1, key.indexOf('-'))
        }
        def fixVersion(String versionName) {
            Version version = new Version(getProjectKey(), versionName, jira)
            version.fix(this)
        }

        def affectVersion(String versionName) {
            // append version to affectVersion
            Version version = new Version(getProjectKey(), versionName, jira)
            version.affect(this)
        }
    }

    final static String DEFAULT_JIRA_URL = "http://gramme.cfmu.corp.eurocontrol.int:8580"
    final static String DEFAULT_SEARCH_URL = "${DEFAULT_JIRA_URL}/rest/api/latest/"

    String username
    String password
    String project
    def credentials = [:]

    private JiraRESTClient(String url, String username, String password) {
        super(url)
        assert username
        assert password

        log.debug "Created for user=${username}, url=" + url
        this.username = username;
        this.password = password;

        credentials['os_username'] = this.username
        credentials['os_password'] = this.password
    }

    /**
     * Create a REST client using provided JIRA username and password.
     */
    static JiraRESTClient create(String username, String password) {

        return new JiraRESTClient(this.DEFAULT_SEARCH_URL, username, password)
    }

    /**
     * Create a REST client using Maven properties jira.username and jira.password for JIRA username and password.
     */
    static JiraRESTClient create(def project) {

        String jiraUsername = project.properties['jira.username']
        String jiraPassword = project.properties['jira.password']

        if (!jiraUsername?.trim()) {
            throw new IllegalArgumentException("Empty property: jira.username")
        }

        if (!jiraPassword?.trim()) {
            throw new IllegalArgumentException("Empty property: jira.password")
        }

        return create(jiraUsername, jiraPassword)
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
                response = get(path: path, contentType: "application/json", query: query)
            }
            else {
                response = get(path: path, contentType: "application/json", query: query)
            }

            assert response.status == 200
            return response
        } catch (groovyx.net.http.HttpResponseException e) {
        //} catch (Exception e) {
            if (e.response.status == 400) {
                // HTTP 400: Bad Request, JIRA returned error
                throw new IllegalArgumentException("JIRA query failed, response data=${e.response.data}", e)
            } else {
                throw new IOException("JIRA connection failed, got HTTP status ${e.response.status}, response data=${e.response.data}", e)
            }

        }
    }

    def post(String path, def body, def query) {

        try {
            def response = post(path: path, contentType: "application/json", body: body, query: query)
            return response
        } catch (groovyx.net.http.HttpResponseException e) {
        //} catch (Exception e) {
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
     * Return an Issue instance representing a JIRA issue
     * @returns Issue
     * @throws
     * @throws
     * @see
     */
    def issue(String key) {
        return new Issue(key: key, jira: this)
    }
}
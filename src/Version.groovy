/**
 * Created by goi on 5/19/17.
 */


import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory;
import JiraRESTClient

class Version {
    String name
    String description
    String project
    JiraRESTClient jira
    protected final Log log

    Version (JiraRESTClient jira, String name, String description) {
        this(jira, name, description, "")
    }

    Version (JiraRESTClient jira, String name, String description, String project) {
        this.jira = jira
        this.name = name
        this.description = description
        this.project = project

        //TODO values can't be empty or undefined

        this.log = LogFactory.getLog(this.getClass());
    }

    // update issue's affectVersions field
    def affect(Issue issue) {
        jira.addToList(issue.getProject(), "versions", name, description) // add new value to list of versions if necessary
        return jira.updateAdd(issue, "versions", name)
    }

    // update issue's fixVersions field
    def fix(Issue issue) {
        jira.addToList(issue.getProject(), "versions", name, description) // add new value to list of versions if necessary
        return jira.updateAdd(issue, "fixVersions", name)
    }

}


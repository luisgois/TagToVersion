/**
 * Created by goi on 5/19/17.
 */
class Issue {
    String key
    String project

    Issue(JiraRESTClient jira, String key) {
        this.key = key
        this.project = key.substring(0, key.indexOf('-'))
    }

    def fixVersion(Version version) {
        version.setProject(project)
        version.fix(this)
    }

    def affectVersion(Version version) {
        // append version to affectVersion
        version.setProject(project)
        version.affect(this)
    }
}

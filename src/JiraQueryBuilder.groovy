import JiraStatus

/**
 * Builder for a JQL-snippet, used for the JIRA search REST API.
 */

class JiraQueryBuilder  {

    private String project
    private int[] sprintIds
    private String sprintFunction
    private String[] issueKeys
    private String[] issueTypes
    private String[] excludedIssueTypes
    private String[] resolution
    private JiraStatus[] status
    private String[] labels
    private String[] fixVersions
    private String orderBy

    /**
     * Create a new query builder, querying by default everything for specified project, ordered by key.
     */
    public JiraQueryBuilder(String project) {
        assert project
        this.project = project
        withIssueTypes("standardIssueTypes()")
        withOrderBy("key")
    }

    JiraQueryBuilder withSprintIds(int...sprintIds) {
        this.sprintIds = sprintIds
        return this
    }

    JiraQueryBuilder withIssueKeys(String...issueKeys) {
        this.issueKeys = issueKeys
        return this
    }

    JiraQueryBuilder withIssueTypes(String... issueTypes) {
        this.issueTypes = issueTypes
        return this
    }

    JiraQueryBuilder withExcludedIssueTypes(String... excludedIssueTypes) {
        this.excludedIssueTypes = excludedIssueTypes
        return this
    }

    JiraQueryBuilder withResolution(String... resolution) {
        this.resolution = resolution
        return this
    }

    JiraQueryBuilder withStatus(JiraStatus... status) {
        this.status = status
        return this
    }

    JiraQueryBuilder withLabels(String... labels) {
        this.labels = labels
        return this
    }

    JiraQueryBuilder withFixVersion(String... fixVersions) {
        this.fixVersions = fixVersions
        return this
    }

    JiraQueryBuilder withOrderBy(String orderBy) {
        this.orderBy = orderBy
        return this
    }

    String build() {
        String jql = "project = ${project}"

        if (sprintIds) {
            jql += " AND sprint in (${sprintIds.join(',')})"
        }

        if (sprintFunction) {
            jql += " AND sprint in ${sprintFunction}"
        }

        if (issueKeys) {
            jql += " AND issuekey in (${issueKeys.join(',')})"
        }

        if (issueTypes) {
            jql += " AND issuetype in (${issueTypes.join(',')})"
        }

        if (excludedIssueTypes) {
            jql += " AND issuetype not in (${excludedIssueTypes.join(',')})"
        }

        if (status) {
            // turn (IN_PROGRESS, RESOLVED) into (Resolved, In Progress)
            jql += " AND status in (${status*.value().join(',')})"
        }

        if (resolution) {
            jql += " AND resolution in (${resolution.join(',')})"
        }

        if (labels) {
            jql += " AND labels in (${labels.join(',')})"
        }

        if (fixVersions) {
            jql += " AND fixVersion in (${fixVersions.join(',')})"
        }

        if (orderBy) {
            jql += " order by ${orderBy}"
        }

        return jql
    }
}
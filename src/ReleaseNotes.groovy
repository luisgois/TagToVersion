import JiraStatus
import JiraRESTClient

def cli = new CliBuilder(
        usage:  'ReleaseNotes [options]',
        header: '\nOptions:',
        footer: '\nInformation provided via above options is used to generate printed string.\n')

import org.apache.commons.cli.Option

cli.with {
    h(longOpt: 'help', 'print this message', required: false)
    f(longOpt: 'fixVersion', 'version where issue was implemented', args: Option.UNLIMITED_VALUES, required: true)
    p(longOpt: 'project', 'project where issue belongs to', args:1, required:true )
//z(longOpt: 'zip', 'Zip Codes (separated by comma)', required: true, args: Option.UNLIMITED_VALUES, valueSeparator: ',')
}

def opt = cli.parse(args)

if (!opt) return
if (opt.h) cli.usage()

def fixVersion = opt.fs
def project = opt.p

// TODO multiple projects, status and versions in same JQL
String[] fixVersions= new String[fixVersion.size()];
fixVersions = fixVersion.toArray(fixVersions);


String jql = new JiraQueryBuilder(project)
            .withStatus(JiraStatus.DONE)
            .withFixVersion(fixVersions)
            .build()

JiraRESTClient jira = JiraRESTClient.create()

println "\nQuerying with JQL:\n${jql}\n"

def results = jira.search(jql)

results.data.issues.each { issue ->
    def text = "${issue.key}: ${issue.fields.summary}"

    println "${text}"
}

println "\nPrinted ${results.data.total} issues from query:"
println jql

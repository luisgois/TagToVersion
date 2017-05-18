import JiraStatus
import JiraRESTClient

def cli = new CliBuilder(
        usage:  'ReleaseNotes [options]',
        header: '\nOptions:',
        footer: '\nInformation provided via above options is used to generate printed string.\n')

import org.apache.commons.cli.Option

cli.with {
    h(longOpt: 'help', 'print this message', required: false)
    f(longOpt: 'fixVersion', 'version where issue was implemented', args: 1, required: true)
    p(longOpt: 'project', 'project where issue belongs to', args:1, required:true )
//z(longOpt: 'zip', 'Zip Codes (separated by comma)', required: true, args: Option.UNLIMITED_VALUES, valueSeparator: ',')
}

def opt = cli.parse(args)

if (!opt) return
if (opt.h) cli.usage()

def fixVersion = opt.f
def project = opt.p

String jql = new JiraQueryBuilder(project)
            .withStatus(JiraStatus.DONE)
            .withFixVersion(fixVersion)
            .build()

JiraRESTClient client = JiraRESTClient.create('goi', 'Bellini2229')

println "\nQuerying with JQL:\n${jql}\n"

def results = client.search(jql)

results.data.issues.each { issue ->
    def url = JiraRESTClient.DEFAULT_JIRA_URL + "/browse/${issue.key}"
    def text = "${issue.key}: ${issue.fields.summary}"

    println "${text} - ${url}"
}

println "\nPrinted ${results.data.total} issues from query:"
println jql

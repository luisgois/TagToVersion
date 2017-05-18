import JiraRESTClient
import Abort

def cli = new CliBuilder(
        usage:  'TagToVersion [options]',
        header: '\nOptions:',
        footer: '\nInformation provided via above options is used to set fix/affected version on given issues.\n',
        stopAtNonOption:false)

import org.apache.commons.cli.Option

cli.with {
    h(longOpt: 'help', 'print this message', required: false)
    v(longOpt: 'version', 'version name to relate to issue', args: 1, required: true)
    f(longOpt: 'fix', 'version is a fixVersion value', args: 0, required: false)
    a(longOpt: 'affect', 'version is an affectVersion value', args: 0, required: false)
    i(longOpt: 'issue', 'issues (separated by comma)', required: true, args: Option.UNLIMITED_VALUES, valueSeparator: ',')
}

def opt = cli.parse(args)

if (!opt) return
if (opt.h) cli.usage()

def version = opt.v
def issues = opt.is
def setFixVersion = opt.f
def setAffectVersion = opt.a

if (!setAffectVersion && !setFixVersion) {
    Abort.no_usage("Must provide either -f or -a options")
}

if (setAffectVersion && setFixVersion) {
    Abort.no_usage("Only one of -f or -a can be used")
}

JiraRESTClient jira = JiraRESTClient.create('goi', 'Bellini2229')

if (setFixVersion) {
    issues.each { issueKey ->
        jira.issue(issueKey).fixVersion(version)

        //Issue.fixVersion(issueKey, version)
        //version.fix(issueKey)
    }
}
else if (setAffectVersion) {
    issues.each { issueKey ->
        jira.issue(issueKey).affectVersion(version)
        //Issue.fixVersion(issueKey, version)
        //version.fix(issueKey)
    }
}


import JiraRESTClient
import Abort
import Version

def cli = new CliBuilder(
        usage:  'TagToVersion [options]',
        header: '\nOptions:',
        footer: '\nInformation provided via above options is used to set fix/affected version on given issues.\n',
        stopAtNonOption:false)

import org.apache.commons.cli.Option

cli.with {
    h(longOpt: 'help', 'print this message', required: false)
    v(longOpt: 'version', 'version name to relate to issue', args: 1, required: true)
    d(longOpt: 'description', 'version description', args: 1, required: false)
    f(longOpt: 'fix', 'version is a fixVersion value', args: 0, required: false)
    a(longOpt: 'affect', 'version is an affectVersion value', args: 0, required: false)
    i(longOpt: 'issue', 'issues (separated by comma)', required: true, args: Option.UNLIMITED_VALUES, valueSeparator: ',')
}

// TODO update issue with list of modified files (one list of files per issue)

def opt = cli.parse(args)

if (!opt) return
if (opt.h) cli.usage()

String versionName = opt.v
def issues = opt.is
Boolean setFixVersion = opt.f
Boolean setAffectVersion = opt.a
String versionDescription = opt.d

if (!setAffectVersion && !setFixVersion) {
    Abort.no_usage("Must provide either -f or -a options")
}

if (setAffectVersion && setFixVersion) {
    Abort.no_usage("Only one of -f or -a can be used")
}

JiraRESTClient jira = JiraRESTClient.create()

Version version = jira.version(versionName, versionDescription)

if (setFixVersion) {
    issues.each { issueKey ->
        Issue issue = jira.issue((String) issueKey)
        issue.fixVersion(version)
    }
}

else if (setAffectVersion) {
    issues.each { issueKey ->
        Issue issue = jira.issue((String) issueKey)
        issue.affectVersion(version)
    }
}


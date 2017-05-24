/**
 * Created by goi on 5/19/17.
 */


import com.jcraft.jsch.Session
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.LogCommand
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.errors.UnsupportedCredentialItem
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevObject
import org.eclipse.jgit.revwalk.RevSort
import org.eclipse.jgit.revwalk.RevTag
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.transport.CredentialItem
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.FetchResult
import org.eclipse.jgit.transport.JschConfigSessionFactory
import org.eclipse.jgit.transport.OpenSshConfig
import org.eclipse.jgit.transport.SshSessionFactory
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.Transport
import org.eclipse.jgit.transport.URIish
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Map;


public ObjectId walkFromToTag (Git git, String from) {
    List<Ref> list = git.tagList().call();

    ObjectId commitId = ObjectId.fromString(from);

    Collection<ObjectId> commits = new LinkedList<ObjectId>();
    for (Ref tag : list) {

        tag = git.getRepository().peel(tag)

        if (tag.getPeeledObjectID() != null) {
            System.out.println("this commit has tag: " + tag);
        }
    }
}

def walkFromTo(Repository repository, String from) {


        // a RevWalk allows to walk over commits based on some filtering that is defined
        RevWalk walk = new RevWalk(repository)

        RevCommit commit = walk.parseCommit(repository.resolve(from));
        System.out.println("Start-Commit: " + commit);

        System.out.println("Walking all commits starting at " + from + " until we find ...");
        walk.markStart(commit);
        int count = 0;
        for (RevCommit rev : walk) {
            System.out.println("Commit: " + rev + " # " + rev.getFullMessage());
            count++;

/*                if(rev.getId().getName().equals(from)) {
                System.out.println("Found from, stopping walk");
                break;
            }*/
        }
        System.out.println(count);

        walk.dispose();
}


def walkAllCommits(Repository repository) {

    //Collection<Ref> allRefs = repository.getAllRefs().values();

    LogCommand log = new Git(repository).log();

    Ref peeledRef = repository.peel(ref);
    if(peeledRef.getPeeledObjectId() != null) {
        log.add(peeledRef.getPeeledObjectId());
    } else {
        //log.add(ref.getObjectId());
    }

    Iterable<RevCommit> logs = log.call();
    for (RevCommit rev : logs) {
        System.out.println("Commit: " + rev /* + ", name: " + rev.getName() + ", id: " + rev.getId().getName() */);
    }

/*    System.out.println("Unsorted\n----------------\n");
    System.out.println(allRefs);*/

/*    Collections.sort(allRefs, new RefComparator());

    System.out.println("\n\nSorted\n----------------\n");
    System.out.println(allRefs);*/

    // BB:toHash
    //ObjectId commitId = ObjectId.fromString( "90b148da973ff51726209b841d8fb5f55643564b");

    // a RevWalk allows to walk over commits based on some filtering that is defined
    RevWalk revWalk = new RevWalk( repository )
    //revWalk.markStart(revWalk.parseCommit( commitId ))
    //revWalk.sort(RevSort.COMMIT_TIME_DESC);

    for( Ref ref : allRefs ) {
        //revWalk.markStart( revWalk.parseCommit( ref.getObjectId() ));
        //revWalk.markStart( ref);
        ///*Ref repoPeeled = repository.peel(ref);
        //if(repoPeeled.getPeeledObjectId() != null) {*/
        //    System.out.println("Ref: " + ref + " target " +  ref.getPeeledObjectId());
        //}
        revWalk.markStart( revWalk.parseCommit( ref.getObjectId() ));
        Ref repoPeeled = repository.peel(ref);
        if(repoPeeled.getPeeledObjectId() != null) {
            System.out.println("Ref: " + ref + " target " +  repoPeeled.getPeeledObjectId());
        }
    }

    System.out.println("Walking all commits starting with " + allRefs.size() + " refs: " + allRefs);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    int count = 0;
    for( RevCommit commit : revWalk ) {
        System.out.println("Commit: " + commit + " ## " +  sdf.format(new Date(commit.getCommitTime()*1000L)));



        count++;
    }
    System.out.println("Had " + count + " commits");

    revWalk.dispose();

}

def cli = new CliBuilder(
        usage:  'IssuesBetweenTags [options]',
        header: '\nOptions:',
        footer: '\nInformation provided via above options is used to generate printed string.\n')

import org.apache.commons.cli.Option
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

cli.with {
    h(longOpt: 'help', 'print this message', required: false)
    b(longOpt: 'branch', 'path to repository', args: 1, required: false)
    f(longOpt: 'fromTag', 'initial tag', args: 1, required: false)
    t(longOpt: 'toTag', 'final tag', args:1, required:false)
    u(longOpt: 'untilTag', 'between consecutive tags, being this the latest one', args: 1, required: false)
    p(longOpt: 'project', 'project owning the repository', args: 1, required: true)
    r(longOpt: 'repository', 'git repository', args: 1, required: true)
}

def opt = cli.parse(args)

if (!opt) return
if (opt.h) cli.usage()

String fromTag = opt.f
def toTag= opt.t
def untilTag= opt.u
def branch = opt.b
def repositoryName = opt.r
def projectName = opt.p

Properties properties = new Properties()
File propertiesFile = new File(System.env.HOME + "/.acm/acm.properties")
propertiesFile.withInputStream {
    properties.load(it)
}

String defaultSshServer = properties.DEFAULT_GIT_SSH_SERVER
String defaultSshPort = properties.DEFAULT_GIT_SSH_PORT
String defaultUsername = properties.DEFAULT_JIRA_USERNAME
String repositoryRoot = properties.REPOSITORY_ROOT

String REMOTE_URL = "ssh://${defaultUsername}@${defaultSshServer}:${defaultSshPort}/${projectName}/${repositoryName}.git";
//String REMOTE_URL="ssh://git@luiss-mac-mini:7999/project_1/rep_1.git";


SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
    @Override
    protected void configure(OpenSshConfig.Host host, Session session ) {
        // do nothing
    }
};

/*
 * Not possible to ls-remote tags. Returns results in arbitrary order. Must clone repository first.
 */


File localRepositoryPath = new File(repositoryRoot + "/" + projectName + "/" + repositoryName);
Git git;

if(!localRepositoryPath.exists()) {
    // then clone
    System.out.println("Cloning from " + REMOTE_URL + " to " + localRepositoryPath);
    git = Git.cloneRepository()
            .setURI(REMOTE_URL)
            .setDirectory(localRepositoryPath)
            .setTransportConfigCallback(new TransportConfigCallback() {
                @Override
                public void configure( Transport transport ) {
                    SshTransport sshTransport = ( SshTransport )transport;
                    sshTransport.setSshSessionFactory( sshSessionFactory );
                }
            })
            .call();
}
else {
    // open existing repo
    System.out.println("Opening local clone from " + localRepositoryPath);
    git = Git.open( localRepositoryPath );

    // fetch updates
    FetchResult result = git.fetch().setCheckFetchedObjects(true).call();
    System.out.println(result)
}

// cache tags vs commits
fromTag = "90b148da973ff51726209b841d8fb5f55643564b"


ObjectId commitId = ObjectId.fromString(fromTag);
Collection<ObjectId> commits = new LinkedList<ObjectId>();
List<RevTag> list = git.tagList().call();
for (RevTag tag : list) {
    RevObject object = tag.getObject();
    if (object.getId().equals(commitId)) {;
        commits.add(object.getId());
    }
}

System.out.println commits


/*

//List<Ref> call = git.tagList().call(); // this is a list, it has a sequence, from oldest to newest
// this approach doesn't work; we'll get all tags, no matter the branch
// we need a revWalker, positioned on a given commit (we get that as argument) and then walking down until the next tagged commit is found.

//for (Ref ref : call) {

*/
/*    // fetch all commits for this tag
    LogCommand log = git.log();
*//*

    Ref peeledRef = repository.peel(ref);
    if(peeledRef.getPeeledObjectId() != null) {
        System.out.println("Tag: " + ref + " " + ref.getName() + " " + peeledRef.getPeeledObjectId().getName());
    }

    */
/*Iterable<RevCommit> logs = log.call();
    for (RevCommit rev : logs) {
        System.out.println("Commit: " + rev *//*
*/
/* + ", name: " + rev.getName() + ", id: " + rev.getId().getName() *//*
*/
/*);
    }*//*

}
*/

//walkAllCommits repository;

//walkFromToTag git, "90b148da973ff51726209b841d8fb5f55643564b";
//walkFromTo repository, "90b148da973ff51726209b841d8fb5f55643564b";







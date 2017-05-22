/**
 * Created by goi on 5/19/17.
 */


import com.jcraft.jsch.Session
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.transport.CredentialItem
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.JschConfigSessionFactory
import org.eclipse.jgit.transport.OpenSshConfig
import org.eclipse.jgit.transport.SshSessionFactory
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.Transport
import org.eclipse.jgit.transport.URIish;

import java.util.Collection;
import java.util.Map;


/*
   Copyright 2013, 2014 Dominik Stadler
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */



/**
 * Simple snippet which shows how to list heads/tags of remote repositories without
 * a local repository
 *
 * @author dominik.stadler at gmx.at
 */


//String REMOTE_URL = "http://git@gramme.cfmu.corp.eurocontrol.int:7990/bitbucket/scm/nco/common_iem.git";
String REMOTE_URL="ssh://git@luiss-mac-mini:7999/project_1/rep_1.git";

SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
    @Override
    protected void configure(OpenSshConfig.Host host, Session session ) {
        // do nothing
    }
};

// then clone
System.out.println("Listing remote repository " + REMOTE_URL);
Collection<Ref> refs = Git.lsRemoteRepository()
        .setHeads(true)
        .setTags(true)
        .setRemote(REMOTE_URL)
        .setTransportConfigCallback( new TransportConfigCallback() {
            public void configure(Transport transport) {
                SshTransport sshTransport = (SshTransport) transport;
                sshTransport.setSshSessionFactory(sshSessionFactory);
            }
        })
        .call();

for (Ref ref : refs) {
    System.out.println("Ref: " + ref);
}

final Map<String, Ref> map = Git.lsRemoteRepository()
        .setHeads(true)
        .setTags(true)
        .setRemote(REMOTE_URL)
        .callAsMap();

System.out.println("As map");
for (Map.Entry<String, Ref> entry : map.entrySet()) {
    System.out.println("Key: " + entry.getKey() + ", Ref: " + entry.getValue());
}

refs = Git.lsRemoteRepository()
        .setRemote(REMOTE_URL)
        .call();

System.out.println("All refs");
for (Ref ref : refs) {
    System.out.println("Ref: " + ref);
}




/*
def cli = new CliBuilder(
        usage:  'IssuesBetweenTags [options]',
        header: '\nOptions:',
        footer: '\nInformation provided via above options is used to generate printed string.\n')

import org.apache.commons.cli.Option
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

cli.with {
    h(longOpt: 'help', 'print this message', required: false)
    b(longOpt: 'branch', 'path to repository', args: 1, required: true)
    f(longOpt: 'fromTag', 'initial tag', args: 1, required: true)
    t(longOpt: 'toTag', 'final tag', args:1, required:true )
    u(longOpt: 'untilTag', 'between consecutive tags, being this the latest one', args: 1, required: true)
}

def opt = cli.parse(args)

if (!opt) return
if (opt.h) cli.usage()

def fromTag = opt.f
def toTag= opt.t
def untilTag= opt.u
def branch = opt.b
def repository = opt.r

*/


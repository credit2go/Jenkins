#!/usr/bin/env groovy

/**
 * credit2go Jenkins utils
 */
Integer getCompileTimeout() {
    return (currentBuild.previousBuild?.result == null) ? 60 : 30
}

String getArtifactIdFromPom() {
    return fileExists('pom.xml') ? readMavenPom(file: 'pom.xml').getArtifactId() : ''
}

String getVersionFromPom() {
    return fileExists('pom.xml') ? readMavenPom(file: 'pom.xml').getVersion() : ''
}

String getTypeFromPom() {
    return fileExists('pom.xml') ? readMavenPom(file: 'pom.xml').getPackaging() : 'jar'
}

def uploadWar(svn_revision, product) {
    milestone label: '上传待测试war包'
    product == '' ? uploadNexus(svn_revision: svn_revision) : uploadNexus(svn_revision: svn_revision, groupId: 'cn.creditu.' + product)
}

def uploadNexus(Map params = [:]) {
    //传入参数
    def groupId = params.containsKey('groupId') ? params.groupId : 'cn.creditu'
    def artifactId = params.containsKey('artifactId') ? params.artifactId : getArtifactIdFromPom()
    def version = params.containsKey('version') ? params.version : getVersionFromPom()
    def classifier = params.containsKey('classifier') ? params.classifier : ''
    def type = params.containsKey('type') ? params.type : getTypeFromPom()
    def fileName = params.containsKey('file') ? params.file : 'target/' + artifactId + '-' + version + (classifier == '' ? '' : '-' + classifier) + '.' + type
    def nexusUrl = params.containsKey('nexusUrl') ? params.nexusUrl : 'repo.credit2go.cn/nexus'
    def repository = params.containsKey('repository') ? params.repository : 'Artifactory'
    def svnRevision = params.containsKey('svn_revision') ? params.svn_revision : ''
    stage('上传文件到Nexus仓库') {
        if (fileExists(fileName)) {
            retry(1) {
                nexusArtifactUploader artifacts: [[artifactId: artifactId, classifier: classifier == '' ? svnRevision : classifier + '_' + svnRevision, file: fileName, type: type]], credentialsId: 'SVN', groupId: groupId, nexusUrl: nexusUrl, nexusVersion: 'nexus3', protocol: 'https', repository: repository, version: version
            }
        } else {
            error fileName + ' is not exists.'
        }
    }
}

def FirstRun() {
    stage('清理本地Maven缓存') {
        sh '''echo "清理本地Maven缓存开始"
            if [ -d /home/credit/.m2/repository ]
            then
                cd /home/credit/.m2/repository
                rm -rf *
            fi
            echo "清理本地Maven缓存结束"'''
    }
    stage('清理本地工作目录') {
        sh '''echo "清理本地工作目录开始"
            if [ -d /opt/workspace ]
            then
                cd /opt/workspace
                rm -rf *
            fi
            echo "清理本地工作目录结束"'''
    }
}

def forceCleanMaven() {
    stage('清理即信本地仓库缓存') {
        sh '''echo "清理本地Maven缓存开始"
            if [ -d /home/credit/.m2/repository/cn ]
            then
                cd /home/credit/.m2/repository/cn
                rm -rf *
            fi
            echo "清理本地Maven缓存结束"'''
    }
}
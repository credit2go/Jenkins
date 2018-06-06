#!/usr/bin/env groovy

def call(Map params = [:]) {
    //传入参数
    def env = params.containsKey('env') ? params.env : ''
    def envType = params.containsKey('envType') ? params.envType : ''
    def artifactId = params.containsKey('artifactId') ? params.artifactId : ''
    def version = params.containsKey('version') ? params.version : ''
    def svn_version = params.containsKey('svn_version') ? params.svn_version : ''
    def redmineID = params.containsKey('redmineID') ? params.redmineID : ''
    //mail related params
    def isSendMail = params?.mail?.send ?: false
    def mailto = params?.mail?.mailto ? params?.mail?.mailto : 'ops@credit2go.cn'
    def mailcc = params?.mail?.mailcc ? params?.mail?.mailcc : ''
    def from = params?.mail?.from ? params?.mail?.from : '版本发布<notification@credit2go.cn>'
    //dingding related params
    def isDing = params?.dingding?.send ? params?.dingding?.send : false
    def token = params?.dingding?.token ? params?.dingding?.token : '5326ac4d62074bde3f2e4eeacb59974599affef22f616f864087e6bf11745125'
    body = generateBody(env: env, envType: envType, artifactId: artifactId, version: version, svn_version: svn_version, redmineID: redmineID)
    subject = generateSubject(envType: envType, artifactId: artifactId, version: version)
    if (isSendMail) {
        stage('邮件通知') {
            mail body: body, cc: mailcc, from: from, subject: subject, to: mailto
        }
    }
    if (isDing) {
        stage('钉钉通知') {
            dingding(body: body, token: token)
        }
    }
}

def dingding(Map params = [:]) {
    //传入参数
    def body = params.containsKey('body') ? params.body : ''
    def token = params.containsKey('token') ? params.token : ''
    //发送邮件通知
    sh '''
curl -s 'https://oapi.dingtalk.com/robot/send?access_token=''' + token + '''\' -H 'Content-Type: application/json' -d '
{
    "msgtype": "text",
    "text": {
        "content": "''' + body + '''"
    }
}'
'''
}

String generateBody(Map params = [:]) {
    //传入参数
    def env = params.containsKey('env') ? params.env : ''
    def envType = params.containsKey('envType') ? params.envType : ''
    def artifactId = params.containsKey('artifactId') ? params.artifactId : ''
    def version = params.containsKey('version') ? params.version : ''
    def svn_version = params.containsKey('svn_version') ? params.svn_version : ''
    def redmineID = params.containsKey('redmineID') ? params.redmineID : ''
    String message = '''{envType}{artifactId}新版本已经发布[胜利]:
应用名称：{artifactId}
版本号：{version}
SVN版本号：{svn_version}
变更内容说明：https://support.credit2go.cn/issues/{RedmineID}
访问地址：https://{env}.credit2go.cn/{artifactId}'''
    message = message.replace('{envType}', envType)
    message = message.replace('{artifactId}', artifactId)
    message = message.replace('{version}', version)
    message = message.replace('{svn_version}', svn_version)
    message = message.replace('{RedmineID}', redmineID)
    message = message.replace('{env}', env)
    return message
}

String generateSubject(Map params = [:]) {
    def envType = params.containsKey('envType') ? params.envType : ''
    def artifactId = params.containsKey('artifactId') ? params.artifactId : ''
    def version = params.containsKey('version') ? params.version : ''
    message = '[版本更新]{artifactId}应用发布{envType}，版本号：{version}'
    message = message.replace('{envType}', envType)
    message = message.replace('{artifactId}', artifactId)
    message = message.replace('{version}', version)
    return message
}
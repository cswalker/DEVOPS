def environmentName(name) {
    if (illegalCharacters(name) || isEmpty(name)) {
        currentBuild.result = 'ABORTED'
        error("Illegal characters or missing environment name")
        return
    }
}

def clientName(name) {
    if (illegalCharacters(name) || isEmpty(name)) {
        currentBuild.result = 'ABORTED'
        error("Illegal characters or missing client name")
        return
    }
}

def illegalCharacters(var) {
    if (var ==~ /^[0-9a-z]+([-\.]+[0-9a-z]+)*$/) {
        return false
    } else {
        return true
    }
}

def isEmpty(var) {
    return (var == null || var.trim().isEmpty())
}

def init(var) {
    if (var != 'run') {
        currentBuild.result = 'ABORTED'
        error("Pipeline initialized")
        return
    }
}

def parseJson(var) {
    try {
        readJSON text: var
    } catch(e) {
        currentBuild.result = 'ABORTED'
        error("Invalid JSON: ${e}")
    }
}

return this

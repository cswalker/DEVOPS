def getBuildUser() {
    def build_user = "unknown"
    script {
        try {
            wrap([$class: 'BuildUser']) {
                build_user = "${BUILD_USER_ID}"
            }
        }
        catch (Exception e) {
            echo "Error determining user: ${e}"
            echo "This can happen when a build is triggered automatically on a schedule and has no associated user"
        }

    }
    return build_user
}

return this

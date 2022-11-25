package tasks

import contributors.GitHubService
import contributors.RequestData
import contributors.User
import kotlin.concurrent.thread

fun loadContributorsBackground(service: GitHubService, req: RequestData, updateResults: (List<User>) -> Unit) {
    thread {
        updateResults(loadContributorsBlocking(service, req))
        //-> Unit means the function returns nonused value called Unit
        //updateResults is a callback method, it needs to be called here to check if its finished doing its job,
        //then it can execute updateResults in the Contributors.kt after done with the job.
    }
}
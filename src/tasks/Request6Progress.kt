package tasks

import contributors.*
import kotlinx.coroutines.Dispatchers

suspend fun loadContributorsProgress(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()
    var allUsers = emptyList<User>()
    for ((index, repo) in repos.withIndex()) {
        val users = service.getRepoContributors(req.org, repo.name)
            .also { logUsers(repo, it) }
            .bodyList()

        allUsers = (allUsers + users).aggregate()
        //There are 82 repos at the time writing, after each repo loading finished,
        //updateResults will run with data allUsers at that exact time
        //if all repos are finished(from index reached 81) then it will do last updateResults
        updateResults(allUsers, index == repos.lastIndex)
    }
}

package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

suspend fun loadContributorsNotCancellable(service: GitHubService, req: RequestData): List<User> {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()
    //Deferred<T> is similar to Promise/Future, it will return T
    val deferreds: List<Deferred<List<User>>> = repos.map { repo ->
        GlobalScope.async {   // #2
            log("starting loading for ${repo.name}")
            delay(3000)
            service
                .getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
        }//later specify Dispatchers.Main for updateUI function
    }
    return deferreds.awaitAll().flatten().aggregate()//flatten makes List<List<T>> into combined/flatten List<T>
}
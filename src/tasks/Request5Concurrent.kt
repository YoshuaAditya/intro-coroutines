package tasks

import contributors.*
import kotlinx.coroutines.*

suspend fun loadContributorsConcurrent(service: GitHubService, req: RequestData): List<User> = coroutineScope {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()
    //Deferred<T> is similar to Promise/Future, it will return T
    val deferreds: List<Deferred<List<User>>> = repos.map { repo ->
        // to run coroutines on different threads from the common thread pool, specify Dispatchers.Default as the context
        async (Dispatchers.Default){
            log("starting loading for ${repo.name}")
            service
                .getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
        }//later specify Dispatchers.Main for updateUI function
    }
    //you can also remove the return@coroutineScope, but i feel this makes more sense
    return@coroutineScope deferreds.awaitAll().flatten().aggregate()//flatten makes List<List<T>> into combined/flatten List<T>
}
package com.example.android.codelabs.paging.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.android.codelabs.paging.api.GithubService
import com.example.android.codelabs.paging.api.IN_QUALIFIER
import com.example.android.codelabs.paging.data.GithubRepository.Companion.NETWORK_PAGE_SIZE
import com.example.android.codelabs.paging.model.Repo

class GithubPagingSource(
    private val service: GithubService,
    private val query: String
) : PagingSource<Int, Repo>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Repo> = runCatching {
        val position = params.key ?: GITHUB_STARTING_PAGE_INDEX
        val apiQuery = query + IN_QUALIFIER
        val response = service.searchRepos(
            query = apiQuery,
            page = position,
            itemsPerPage = params.loadSize
        )
        val repos = response.items
        val nextKey = if (repos.isEmpty()) {
            null
        } else {
            position + (params.loadSize / NETWORK_PAGE_SIZE)
        }
        LoadResult.Page(
            data = repos,
            prevKey = if (position == GITHUB_STARTING_PAGE_INDEX) null else position - 1,
            nextKey = nextKey
        )
    }.getOrElse {
        LoadResult.Error(it)
    }

    override fun getRefreshKey(state: PagingState<Int, Repo>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }

    companion object {
        private const val GITHUB_STARTING_PAGE_INDEX = 1
    }
}
package br.com.fiap.aguiaexpress.data.repository

import br.com.fiap.aguiaexpress.core.session.ApplicationRole
import br.com.fiap.aguiaexpress.domain.model.*
import br.com.fiap.aguiaexpress.ui.screens.home.homeIdea
import br.com.fiap.aguiaexpress.ui.screens.home.homeOrientation
import br.com.fiap.aguiaexpress.ui.screens.home.homeSummary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class HomeRepositoryTest {
    @Test
    fun `operational home requests personal ideas and medals but never executive endpoints`() = runBlocking {
        val source = RecordingSource()
        HomeRepository(source).load(ApplicationRole.OPERACIONAL, 7)
        assertEquals(setOf("ideas:7", "badges:7", "projects"), source.calls.toSet())
    }

    @Test
    fun `manager home only loads shared ideas and projects`() = runBlocking {
        val source = RecordingSource()
        HomeRepository(source).load(ApplicationRole.GESTOR, 7)
        assertEquals(setOf("ideas:null", "projects"), source.calls.toSet())
    }

    @Test
    fun `leader uses backend aggregates with at most three filtered summaries and no project downloads`() = runBlocking {
        val source = RecordingSource().apply {
            orientationResult = Result.success(listOf(homeOrientation(4, "D"), homeOrientation(2, "B"),
                homeOrientation(3, "C"), homeOrientation(1, "A")))
        }
        val inputs = HomeRepository(source).load(ApplicationRole.LIDER, 7)
        assertEquals(setOf("summary:null", "orientations", "summary:1", "summary:2", "summary:3"), source.calls.toSet())
        assertEquals(homeSummary(), inputs.summary)
        assertEquals(setOf(1L, 2L, 3L), inputs.strategySummaries.keys)
        assertNull(inputs.projects)
    }

    @Test
    fun `failed optional section preserves successful data rather than empty zeros`() = runBlocking {
        val source = RecordingSource().apply {
            ideaResult = Result.success(listOf(homeIdea(1)))
            badgeResult = Result.failure(IllegalStateException("private internal detail"))
        }
        val inputs = HomeRepository(source).load(ApplicationRole.OPERACIONAL, 7)
        assertEquals(1, inputs.ideas?.size)
        assertNull(inputs.badges)
        assertEquals(listOf("Não foi possível carregar suas medalhas."), inputs.warnings)
        assertFalse(inputs.warnings.any { it.contains("private") })
    }

    @Test
    fun `failed filtered summary remains missing without losing executive totals`() = runBlocking {
        val source = RecordingSource().apply {
            orientationResult = Result.success(listOf(homeOrientation(1)))
            filteredFailure = IllegalStateException("unavailable")
        }
        val inputs = HomeRepository(source).load(ApplicationRole.LIDER, 7)
        assertEquals(homeSummary(), inputs.summary)
        assertTrue(inputs.strategySummaries.isEmpty())
        assertEquals(1, inputs.warnings.size)
    }

    @Test
    fun `expired session is propagated instead of disguised as partial data`() {
        val expired = HttpException(Response.error<Any>(401, "{}".toResponseBody()))
        val source = RecordingSource().apply { projectResult = Result.failure(expired) }
        val thrown = assertThrows(HttpException::class.java) {
            runBlocking { HomeRepository(source).load(ApplicationRole.GESTOR, 7) }
        }
        assertEquals(401, thrown.code())
    }

    @Test
    fun `cancellation wrapped by older repositories is still propagated`() {
        val source = RecordingSource().apply { projectResult = Result.failure(CancellationException("cancelled")) }
        assertThrows(CancellationException::class.java) {
            runBlocking { HomeRepository(source).load(ApplicationRole.GESTOR, 7) }
        }
    }

    @Test
    fun `invalid session user makes no API requests`() {
        val source = RecordingSource()
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { HomeRepository(source).load(ApplicationRole.OPERACIONAL, 0) }
        }
        assertTrue(source.calls.isEmpty())
    }

    private class RecordingSource : HomeDataSource {
        val calls = mutableListOf<String>()
        var ideaResult: Result<List<Ideia>> = Result.success(emptyList())
        var badgeResult: Result<List<UsuarioBadge>> = Result.success(emptyList())
        var projectResult: Result<List<Projeto>> = Result.success(emptyList())
        var orientationResult: Result<List<Orientacao>> = Result.success(emptyList())
        var filteredFailure: Throwable? = null
        override suspend fun ideas(userId: Long?): Result<List<Ideia>> {
            calls += "ideas:$userId"
            return ideaResult
        }
        override suspend fun badges(userId: Long): Result<List<UsuarioBadge>> {
            calls += "badges:$userId"
            return badgeResult
        }
        override suspend fun projects(): Result<List<Projeto>> {
            calls += "projects"
            return projectResult
        }
        override suspend fun orientations(): Result<List<Orientacao>> {
            calls += "orientations"
            return orientationResult
        }
        override suspend fun summary(orientationId: Long?): Result<DashboardSummary> {
            calls += "summary:$orientationId"
            return if (orientationId != null && filteredFailure != null) Result.failure(checkNotNull(filteredFailure))
                else Result.success(homeSummary())
        }
    }
}

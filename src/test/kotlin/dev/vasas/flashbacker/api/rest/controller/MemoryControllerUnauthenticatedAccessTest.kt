package dev.vasas.flashbacker.api.rest.controller

import dev.vasas.flashbacker.domain.repository.MemoryRepository
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(controllers = [MemoryController::class])
internal class MemoryControllerUnauthenticatedAccessTest(
        @Autowired private val mockMvc: MockMvc
) {

    companion object {
        val mockMemoryRepository = mockk<MemoryRepository>()
    }

    @TestConfiguration
    internal class MemoryControllerTestConfig {
        @Bean
        fun memoryRepository() = mockMemoryRepository
    }

    @Test
    fun `unauthenticated GET to memories endpoint returns status code 401`() {
        mockMvc.get("/memories") {
        }.andExpect {
            status { isUnauthorized }
        }
    }

    @Test
    fun `unauthenticated POST to memories endpoint returns status code 401`() {
        mockMvc.post("/memories") {
            with(csrf())
        }.andExpect {
            status { isUnauthorized }
        }
    }

    @Test
    fun `unauthenticated DELETE to memories endpoint returns status code 401`() {
        mockMvc.delete("/memories/anyId") {
            with(csrf())
        }.andExpect {
            status { isUnauthorized }
        }
    }

}

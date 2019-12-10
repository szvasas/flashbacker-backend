package dev.vasas.flashbacker.api.rest.controller

import dev.vasas.flashbacker.api.rest.representationmodel.StoryModel
import dev.vasas.flashbacker.domain.repository.StoryRepository
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

@WebMvcTest(controllers = [StoryController::class])
internal class StoryControllerUnauthenticatedAccessTest(
        @Autowired private val mockMvc: MockMvc
) {

    companion object {
        val mockStoryRepository = mockk<StoryRepository>()
    }

    @TestConfiguration
    internal class ControllerTestConfig {
        @Bean
        fun storyRepository() = mockStoryRepository
    }

    @Test
    fun `unauthenticated GET to stories endpoint returns status code 401`() {
        mockMvc.get("/${StoryModel.collectionRelationName}") {
        }.andExpect {
            status { isUnauthorized }
        }
    }

    @Test
    fun `unauthenticated POST to stories endpoint returns status code 401`() {
        mockMvc.post("/${StoryModel.collectionRelationName}") {
            with(csrf())
        }.andExpect {
            status { isUnauthorized }
        }
    }

    @Test
    fun `unauthenticated DELETE to stories endpoint returns status code 401`() {
        mockMvc.delete("/${StoryModel.collectionRelationName}/anyId") {
            with(csrf())
        }.andExpect {
            status { isUnauthorized }
        }
    }

}

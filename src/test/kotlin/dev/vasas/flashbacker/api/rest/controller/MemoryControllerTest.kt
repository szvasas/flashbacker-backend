package dev.vasas.flashbacker.api.rest.controller

import dev.vasas.flashbacker.domain.repository.MemoryRepository
import dev.vasas.flashbacker.testtooling.johnsAwesomeMemory
import dev.vasas.flashbacker.testtooling.johnsGreatMemory
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.hateoas.MediaTypes
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(controllers = [MemoryController::class])
internal class MemoryControllerTest(
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

    @BeforeEach
    fun clearMocks() {
        clearMocks(mockMemoryRepository)
    }

    @Test
    fun `GET to memories endpoint returns all the memories from the database with status code 200`() {
        // given
        every {
            mockMemoryRepository.findMemoriesForUser("John")
        } returns listOf(johnsAwesomeMemory, johnsGreatMemory)

        mockMvc.get("/memories") {
        }.andExpect {
            status { isOk }
            content {
                contentType(MediaTypes.HAL_JSON)
                string("{\"_embedded\":{\"memories\":[" +
                        "{\"id\":\"test-id-2\",\"userName\":\"John\",\"location\":\"Home\",\"date\":\"2017-11-23T15:02:03.001\",\"text\":\"Awesome stuff\"," +
                        "\"_links\":{\"self\":{\"href\":\"http://localhost/memories/test-id-2\"}}}," +
                        "{\"id\":\"test-id-1\",\"userName\":\"John\",\"location\":\"The Beach\",\"date\":\"2017-12-03T15:02:03.001\",\"text\":\"Great things\"," +
                        "\"_links\":{\"self\":{\"href\":\"http://localhost/memories/test-id-1\"}}}]}," +
                        "\"_links\":{\"self\":{\"href\":\"http://localhost/memories\"}}}")
            }
            jsonPath("$._links.self.href") {
                value("http://localhost/memories")
            }
            jsonPath("$._embedded.memories[?(@.id=='test-id-1')]._links.self.href") {
                value("http://localhost/memories/test-id-1")
            }
            jsonPath("$._embedded.memories[?(@.id=='test-id-2')]._links.self.href") {
                value("http://localhost/memories/test-id-2")
            }
        }.andDo {
            print()
        }

        verify {
            mockMemoryRepository.findMemoriesForUser("John")
        }
    }

    @Test
    fun `given no memories in the database GET to memories endpoint returns a response with status code 200 and links only`() {
        // given
        every {
            mockMemoryRepository.findMemoriesForUser("John")
        } returns emptyList()

        mockMvc.get("/memories") {
        }.andExpect {
            status { isOk }
            content {
                contentType(MediaTypes.HAL_JSON)
                string("{\"_links\":{\"self\":{\"href\":\"http://localhost/memories\"}}}")
            }
        }.andDo {
            print()
        }

        verify {
            mockMemoryRepository.findMemoriesForUser("John")
        }
    }

    @Test
    fun `given no memory is found for a specific id GET to memories slash {id} endpoint returns an empty response with status code 404`() {
        // given
        every {
            mockMemoryRepository.findById(johnsAwesomeMemory.id)
        } returns null

        mockMvc.get("/memories/${johnsAwesomeMemory.id}") {
        }.andExpect {
            status { isNotFound }
            content { string("") }
        }.andDo {
            print()
        }

        verify {
            mockMemoryRepository.findById(johnsAwesomeMemory.id)
        }
    }

    @Test
    fun `GET to memories slash {id} endpoint returns the memory with status code 200 and links`() {
        // given
        every {
            mockMemoryRepository.findById(johnsAwesomeMemory.id)
        } returns johnsAwesomeMemory

        mockMvc.get("/memories/${johnsAwesomeMemory.id}") {
        }.andExpect {
            status { isOk }
            content {
                contentType(MediaTypes.HAL_JSON)
                string("{\"id\":\"test-id-2\",\"userName\":\"John\",\"location\":\"Home\",\"date\":\"2017-11-23T15:02:03.001\",\"text\":\"Awesome stuff\"" +
                        ",\"_links\":{\"self\":{\"href\":\"http://localhost/memories/test-id-2\"}}}")
            }
        }.andDo {
            print()
        }

        verify {
            mockMemoryRepository.findById(johnsAwesomeMemory.id)
        }
    }

    @Test
    fun `POST to memories endpoint saves the memory and returns status code 201`() {
        // given
        every {
            mockMemoryRepository.save(johnsAwesomeMemory)
        } answers {
            johnsAwesomeMemory
        }

        mockMvc.post("/memories") {
            contentType = MediaType.APPLICATION_JSON
            content = "{\"id\":\"test-id-2\",\"userName\":\"John\",\"location\":\"Home\",\"date\":\"2017-11-23T15:02:03.001\",\"text\":\"Awesome stuff\"}"
        }.andExpect {
            status { isCreated }
        }.andDo {
            print()
        }

        verify {
            mockMemoryRepository.save(johnsAwesomeMemory)
        }
    }

    @Test
    fun `DELETE to memories slash {id} endpoint deletes the memory with correct id and returns status code 204`() {
        every {
            mockMemoryRepository.deleteById(johnsAwesomeMemory.id)
        } answers {
            nothing
        }

        mockMvc.delete("/memories/${johnsAwesomeMemory.id}") {
        }.andExpect {
            status { isNoContent }
        }.andDo {
            print()
        }

        verify {
            mockMemoryRepository.deleteById(johnsAwesomeMemory.id)
        }
    }
}

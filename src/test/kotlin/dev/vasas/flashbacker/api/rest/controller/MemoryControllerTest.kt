package dev.vasas.flashbacker.api.rest.controller

import dev.vasas.flashbacker.domain.repository.MemoryRepository
import dev.vasas.flashbacker.testtooling.USER_ID_OF_BOB
import dev.vasas.flashbacker.testtooling.awesomeMemoryOfBob
import dev.vasas.flashbacker.testtooling.greatMemoryOfBob
import dev.vasas.flashbacker.testtooling.niceMemoryOfAlice
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
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(controllers = [MemoryController::class])
@WithMockUser(USER_ID_OF_BOB)
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
            mockMemoryRepository.findMemoriesForUser(USER_ID_OF_BOB)
        } returns listOf(awesomeMemoryOfBob, greatMemoryOfBob)

        mockMvc.get("/memories") {
        }.andExpect {
            status { isOk }
            content {
                contentType(MediaTypes.HAL_JSON)
                json("""
                    {"_embedded":{
                        "memories":[
                          {
                            "id":"test-id-2",
                            "location":"Home",
                            "date":"2017-11-23T15:02:03.001",
                            "text":"Awesome stuff",
                            "_links":{"self":{"href":"http://localhost/memories/test-id-2"}
                            }
                          },
                          {
                            "id":"test-id-1",
                            "location":"The Beach",
                            "date":"2017-12-03T15:02:03.001",
                            "text":"Great things",
                            "_links":{"self":{"href":"http://localhost/memories/test-id-1"}
                            }
                          }
                        ]
                      },"_links":{"self":{"href":"http://localhost/memories"}}}
                """)
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
            mockMemoryRepository.findMemoriesForUser(USER_ID_OF_BOB)
        }
    }

    @Test
    fun `given no memories in the database GET to memories endpoint returns a response with status code 200 and links only`() {
        // given
        every {
            mockMemoryRepository.findMemoriesForUser(USER_ID_OF_BOB)
        } returns emptyList()

        mockMvc.get("/memories") {
        }.andExpect {
            status { isOk }
            content {
                contentType(MediaTypes.HAL_JSON)
                json("""
                    {
                      "_links":{
                        "self":{
                          "href":"http://localhost/memories"
                        }
                      }
                    }
                """)
            }
        }.andDo {
            print()
        }

        verify {
            mockMemoryRepository.findMemoriesForUser(USER_ID_OF_BOB)
        }
    }

    @Test
    fun `given no memory is found for a specific id GET to memories slash {id} endpoint returns an empty response with status code 404`() {
        // given
        every {
            mockMemoryRepository.findById(awesomeMemoryOfBob.id)
        } returns null

        mockMvc.get("/memories/${awesomeMemoryOfBob.id}") {
        }.andExpect {
            status { isNotFound }
            content { string("") }
        }.andDo {
            print()
        }

        verify {
            mockMemoryRepository.findById(awesomeMemoryOfBob.id)
        }
    }

    @Test
    fun `GET to memories slash {id} endpoint returns the memory with status code 200 and links`() {
        // given
        every {
            mockMemoryRepository.findById(awesomeMemoryOfBob.id)
        } returns awesomeMemoryOfBob

        mockMvc.get("/memories/${awesomeMemoryOfBob.id}") {
        }.andExpect {
            status { isOk }
            content {
                contentType(MediaTypes.HAL_JSON)
                json("""
                    {
                      "id":"test-id-2",
                      "location":"Home",
                      "date":"2017-11-23T15:02:03.001",
                      "text":"Awesome stuff",
                      "_links":{
                        "self":{
                          "href":"http://localhost/memories/test-id-2"
                        }
                      }
                    }
                """.trimIndent())
            }
        }.andDo {
            print()
        }

        verify {
            mockMemoryRepository.findById(awesomeMemoryOfBob.id)
        }
    }

    @Test
    fun `POST to memories endpoint saves the memory and returns status code 201`() {
        // given
        every {
            mockMemoryRepository.save(awesomeMemoryOfBob)
        } answers {
            awesomeMemoryOfBob
        }

        mockMvc.post("/memories") {
            with(csrf())
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":"test-id-2","location":"Home","date":"2017-11-23T15:02:03.001","text":"Awesome stuff"}"""
        }.andExpect {
            status { isCreated }
        }.andDo {
            print()
        }

        verify {
            mockMemoryRepository.save(awesomeMemoryOfBob)
        }
    }

    @Test
    fun `DELETE to memories slash {id} endpoint deletes the memory with correct id and returns status code 204`() {
        every {
            mockMemoryRepository.deleteById(awesomeMemoryOfBob.id)
        } answers {
            nothing
        }
        every {
            mockMemoryRepository.findById(awesomeMemoryOfBob.id)
        } answers {
            awesomeMemoryOfBob
        }

        mockMvc.delete("/memories/${awesomeMemoryOfBob.id}") {
            with(csrf())
        }.andExpect {
            status { isNoContent }
        }.andDo {
            print()
        }

        verify {
            mockMemoryRepository.deleteById(awesomeMemoryOfBob.id)
        }
    }

    @Test
    fun `a logged in user cannot delete memories of other users`() {
        // given
        every {
            mockMemoryRepository.findById(niceMemoryOfAlice.id)
        } answers {
            niceMemoryOfAlice
        }

        mockMvc.delete("/memories/${niceMemoryOfAlice.id}") {
            with(csrf())
        }.andExpect {
            status { isNoContent }
        }.andDo {
            print()
        }

        verify(exactly = 0) {
            mockMemoryRepository.deleteById(niceMemoryOfAlice.id)
        }
    }

}

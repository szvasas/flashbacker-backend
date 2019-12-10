package dev.vasas.flashbacker.api.rest.controller

import dev.vasas.flashbacker.api.rest.representationmodel.StoryModel
import dev.vasas.flashbacker.domain.repository.StoryRepository
import dev.vasas.flashbacker.testtooling.USER_ID_OF_BOB
import dev.vasas.flashbacker.testtooling.awesomeStoryOfBob
import dev.vasas.flashbacker.testtooling.greatStoryOfBob
import dev.vasas.flashbacker.testtooling.niceStoryOfAlice
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

@WebMvcTest(controllers = [StoryController::class])
@WithMockUser(USER_ID_OF_BOB)
internal class StoryControllerTest(
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

    @BeforeEach
    fun clearMocks() {
        clearMocks(mockStoryRepository)
    }

    @Test
    fun `GET to stories endpoint returns all the stories from the database with status code 200`() {
        // given
        every {
            mockStoryRepository.findStoriesForUser(USER_ID_OF_BOB)
        } returns listOf(awesomeStoryOfBob, greatStoryOfBob)

        mockMvc.get("/${StoryModel.collectionRelationName}") {
        }.andExpect {
            status { isOk }
            content {
                contentType(MediaTypes.HAL_JSON)
                json("""
                    {"_embedded":{
                        "stories":[
                          {
                            "id":"test-id-2",
                            "location":"Home",
                            "date":"2017-11-23T15:02:03.001",
                            "text":"Awesome stuff",
                            "_links":{"self":{"href":"http://localhost/stories/test-id-2"}
                            }
                          },
                          {
                            "id":"test-id-1",
                            "location":"The Beach",
                            "date":"2017-12-03T15:02:03.001",
                            "text":"Great things",
                            "_links":{"self":{"href":"http://localhost/stories/test-id-1"}
                            }
                          }
                        ]
                      },"_links":{"self":{"href":"http://localhost/stories"}}}
                """)
            }
            jsonPath("$._links.self.href") {
                value("http://localhost/stories")
            }
            jsonPath("$._embedded.stories[?(@.id=='test-id-1')]._links.self.href") {
                value("http://localhost/stories/test-id-1")
            }
            jsonPath("$._embedded.stories[?(@.id=='test-id-2')]._links.self.href") {
                value("http://localhost/stories/test-id-2")
            }
        }.andDo {
            print()
        }

        verify {
            mockStoryRepository.findStoriesForUser(USER_ID_OF_BOB)
        }
    }

    @Test
    fun `given no stories in the database GET to stories endpoint returns a response with status code 200 and links only`() {
        // given
        every {
            mockStoryRepository.findStoriesForUser(USER_ID_OF_BOB)
        } returns emptyList()

        mockMvc.get("/${StoryModel.collectionRelationName}") {
        }.andExpect {
            status { isOk }
            content {
                contentType(MediaTypes.HAL_JSON)
                json("""
                    {
                      "_links":{
                        "self":{
                          "href":"http://localhost/stories"
                        }
                      }
                    }
                """)
            }
        }.andDo {
            print()
        }

        verify {
            mockStoryRepository.findStoriesForUser(USER_ID_OF_BOB)
        }
    }

    @Test
    fun `given no story is found for a specific id GET to stories slash {id} endpoint returns an empty response with status code 404`() {
        // given
        every {
            mockStoryRepository.findById(awesomeStoryOfBob.id)
        } returns null

        mockMvc.get("/${StoryModel.collectionRelationName}/${awesomeStoryOfBob.id}") {
        }.andExpect {
            status { isNotFound }
            content { string("") }
        }.andDo {
            print()
        }

        verify {
            mockStoryRepository.findById(awesomeStoryOfBob.id)
        }
    }

    @Test
    fun `GET to stories slash {id} endpoint returns the story with status code 200 and links`() {
        // given
        every {
            mockStoryRepository.findById(awesomeStoryOfBob.id)
        } returns awesomeStoryOfBob

        mockMvc.get("/${StoryModel.collectionRelationName}/${awesomeStoryOfBob.id}") {
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
                          "href":"http://localhost/stories/test-id-2"
                        }
                      }
                    }
                """.trimIndent())
            }
        }.andDo {
            print()
        }

        verify {
            mockStoryRepository.findById(awesomeStoryOfBob.id)
        }
    }

    @Test
    fun `POST to stories endpoint saves the story and returns status code 201`() {
        // given
        every {
            mockStoryRepository.save(awesomeStoryOfBob)
        } answers {
            awesomeStoryOfBob
        }

        mockMvc.post("/${StoryModel.collectionRelationName}") {
            with(csrf())
            contentType = MediaType.APPLICATION_JSON
            content = """{"id":"test-id-2","location":"Home","date":"2017-11-23T15:02:03.001","text":"Awesome stuff"}"""
        }.andExpect {
            status { isCreated }
        }.andDo {
            print()
        }

        verify {
            mockStoryRepository.save(awesomeStoryOfBob)
        }
    }

    @Test
    fun `DELETE to stories slash {id} endpoint deletes the story with correct id and returns status code 204`() {
        every {
            mockStoryRepository.deleteById(awesomeStoryOfBob.id)
        } answers {
            nothing
        }
        every {
            mockStoryRepository.findById(awesomeStoryOfBob.id)
        } answers {
            awesomeStoryOfBob
        }

        mockMvc.delete("/${StoryModel.collectionRelationName}/${awesomeStoryOfBob.id}") {
            with(csrf())
        }.andExpect {
            status { isNoContent }
        }.andDo {
            print()
        }

        verify {
            mockStoryRepository.deleteById(awesomeStoryOfBob.id)
        }
    }

    @Test
    fun `a logged in user cannot delete stories of other users`() {
        // given
        every {
            mockStoryRepository.findById(niceStoryOfAlice.id)
        } answers {
            niceStoryOfAlice
        }

        mockMvc.delete("/${StoryModel.collectionRelationName}/${niceStoryOfAlice.id}") {
            with(csrf())
        }.andExpect {
            status { isNoContent }
        }.andDo {
            print()
        }

        verify(exactly = 0) {
            mockStoryRepository.deleteById(niceStoryOfAlice.id)
        }
    }

}

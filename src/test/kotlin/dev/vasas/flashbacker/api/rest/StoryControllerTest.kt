package dev.vasas.flashbacker.api.rest

import dev.vasas.flashbacker.api.rest.StoryModel.Companion.collectionRelationName
import dev.vasas.flashbacker.domain.Story
import dev.vasas.flashbacker.domain.StoryRepository
import dev.vasas.flashbacker.testtooling.USER_ID_OF_BOB
import dev.vasas.flashbacker.testtooling.awesomeStoryOfBob
import dev.vasas.flashbacker.testtooling.greatStoryOfBob
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
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
import java.time.LocalDate

@WebMvcTest(controllers = [StoryController::class])
internal class StoryControllerTest(
        @Autowired private val mockMvc: MockMvc,
        @Autowired private val storyIdGenerator: () -> String
) {

    companion object {
        val mockStoryRepository = mockk<StoryRepository>()
    }

    @TestConfiguration
    internal class ControllerTestConfig {
        @Bean
        fun storyRepository() = mockStoryRepository

        @Bean
        fun idGenerator(): () -> String = { "42" }
    }

    @BeforeEach
    fun clearMocks() {
        clearMocks(mockStoryRepository)
    }

    @Nested
    @WithMockUser(USER_ID_OF_BOB)
    inner class `With authenticated user` {

        @Nested
        @WithMockUser(USER_ID_OF_BOB)
        inner class `GET request specification` {

            @Test
            fun `given some stories exist in the DB stories endpoint without parameters returns all the stories for the logged in user with status code 200 and links`() {
                // given
                every {
                    mockStoryRepository.findStoriesForUser(USER_ID_OF_BOB)
                } returns listOf(awesomeStoryOfBob, greatStoryOfBob)

                mockMvc.get("/$collectionRelationName") {
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
                            "dateHappened":"2017-11-23",
                            "text":"Awesome stuff",
                            "_links":{"self":{"href":"http://localhost/stories/2017/11/23/test-id-2"}
                            }
                          },
                          {
                            "id":"test-id-1",
                            "location":"The Beach",
                            "dateHappened":"2017-12-03",
                            "text":"Great things",
                            "_links":{"self":{"href":"http://localhost/stories/2017/12/3/test-id-1"}
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
                        value("http://localhost/stories/2017/12/3/test-id-1")
                    }
                    jsonPath("$._embedded.stories[?(@.id=='test-id-2')]._links.self.href") {
                        value("http://localhost/stories/2017/11/23/test-id-2")
                    }
                }.andDo {
                    print()
                }

                verify {
                    mockStoryRepository.findStoriesForUser(USER_ID_OF_BOB)
                }
            }

            @Test
            fun `given no stories in the DB stories endpoint without parameters returns a response with status code 200 and links`() {
                // given
                every {
                    mockStoryRepository.findStoriesForUser(USER_ID_OF_BOB)
                } returns emptyList()

                mockMvc.get("/$collectionRelationName") {
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
            fun `given no story is found for a specific parameter set stories endpoint returns an empty response with status code 404`() {
                // given
                every {
                    mockStoryRepository.findByUserDateHappenedStoryId(awesomeStoryOfBob.userId, awesomeStoryOfBob.dateHappened, awesomeStoryOfBob.id)
                } returns null

                mockMvc.get("/$collectionRelationName/${awesomeStoryOfBob.dateHappened.year}" +
                        "/${awesomeStoryOfBob.dateHappened.monthValue}/${awesomeStoryOfBob.dateHappened.dayOfMonth}/${awesomeStoryOfBob.id}") {
                }.andExpect {
                    status { isNotFound }
                    content { string("") }
                }.andDo {
                    print()
                }

                verify {
                    mockStoryRepository.findByUserDateHappenedStoryId(awesomeStoryOfBob.userId, awesomeStoryOfBob.dateHappened, awesomeStoryOfBob.id)
                }
            }

            @Test
            fun `given a story found for a specific parameter set stories endpoint returns the story with status code 200 and links`() {
                // given
                every {
                    mockStoryRepository.findByUserDateHappenedStoryId(awesomeStoryOfBob.userId, awesomeStoryOfBob.dateHappened, awesomeStoryOfBob.id)
                } returns awesomeStoryOfBob

                mockMvc.get("/$collectionRelationName/${awesomeStoryOfBob.dateHappened.year}" +
                        "/${awesomeStoryOfBob.dateHappened.monthValue}/${awesomeStoryOfBob.dateHappened.dayOfMonth}/${awesomeStoryOfBob.id}") {
                }.andExpect {
                    status { isOk }
                    content {
                        contentType(MediaTypes.HAL_JSON)
                        json("""
                    {
                      "id":"test-id-2",
                      "location":"Home",
                      "dateHappened":"2017-11-23",
                      "text":"Awesome stuff",
                      "_links":{
                        "self":{
                          "href":"http://localhost/stories/2017/11/23/test-id-2"
                        }
                      }
                    }
                """.trimIndent())
                    }
                }.andDo {
                    print()
                }

                verify {
                    mockStoryRepository.findByUserDateHappenedStoryId(awesomeStoryOfBob.userId, awesomeStoryOfBob.dateHappened, awesomeStoryOfBob.id)
                }
            }

            @Test
            fun `given invalid date parameter is provided stories endpoint returns status code 400`() {
                // given
                every {
                    mockStoryRepository.findByUserDateHappenedStoryId(any(), any(), any())
                } returns awesomeStoryOfBob
                val invalidMonth = 15

                mockMvc.get("/$collectionRelationName/${awesomeStoryOfBob.dateHappened.year}" +
                        "/${invalidMonth}/${awesomeStoryOfBob.dateHappened.dayOfMonth}/${awesomeStoryOfBob.id}") {
                }.andExpect {
                    status { isBadRequest }
                }.andDo {
                    print()
                }

                verify(exactly = 0) {
                    mockStoryRepository.findByUserDateHappenedStoryId(any(), any(), any())
                }
            }

        }

        @Nested
        @WithMockUser(USER_ID_OF_BOB)
        inner class `POST method specification` {

            @Test
            fun `given request body is valid stories endpoint saves the story and returns status code 201`() {
                // given
                val repoArgHolder = slot<Story>()
                every {
                    mockStoryRepository.save(story = capture(repoArgHolder))
                } answers {
                    nothing
                }
                val validContent = """{"location":"Home","dateHappened":"2017-11-23","text":"Awesome stuff"}"""

                mockMvc.post("/$collectionRelationName") {
                    with(csrf())
                    contentType = MediaType.APPLICATION_JSON
                    content = validContent
                }.andExpect {
                    status { isCreated }
                }.andDo {
                    print()
                }

                assertThat(repoArgHolder.captured).isEqualTo(Story(
                        id = storyIdGenerator(),
                        userId = USER_ID_OF_BOB,
                        location = "Home",
                        dateHappened = LocalDate.of(2017, 11, 23),
                        text = "Awesome stuff"
                ))
            }

            @Test
            fun `given request body is invalid stories endpoint returns status code 400`() {
                // given
                every {
                    mockStoryRepository.save(any())
                } answers {
                    nothing
                }
                val invalidContent = """{"l":"Home","d":"2017-11-23","t":"Awesome stuff"}"""

                mockMvc.post("/$collectionRelationName") {
                    with(csrf())
                    contentType = MediaType.APPLICATION_JSON
                    content = invalidContent
                }.andExpect {
                    status { isBadRequest }
                }.andDo {
                    print()
                }

                verify(exactly = 0) {
                    mockStoryRepository.save(any())
                }
            }
        }

        @Nested
        @WithMockUser(USER_ID_OF_BOB)
        inner class `DELETE method specification` {

            @Test
            fun `given valid parameters provided stories endpoint deletes the story and returns status code 204`() {
                every {
                    mockStoryRepository.deleteByUserDateHappenedStoryId(awesomeStoryOfBob.userId, awesomeStoryOfBob.dateHappened, awesomeStoryOfBob.id)
                } answers {
                    nothing
                }

                mockMvc.delete("/$collectionRelationName/${awesomeStoryOfBob.dateHappened.year}" +
                        "/${awesomeStoryOfBob.dateHappened.monthValue}/${awesomeStoryOfBob.dateHappened.dayOfMonth}/${awesomeStoryOfBob.id}") {
                    with(csrf())
                }.andExpect {
                    status { isNoContent }
                }.andDo {
                    print()
                }

                verify {
                    mockStoryRepository.deleteByUserDateHappenedStoryId(awesomeStoryOfBob.userId, awesomeStoryOfBob.dateHappened, awesomeStoryOfBob.id)
                }
            }

            @Test
            fun `given invalid date parameter is provided stories endpoint returns status code 400`() {
                every {
                    mockStoryRepository.deleteByUserDateHappenedStoryId(any(), any(), any())
                } answers {
                    nothing
                }
                val invalidMonth = 20

                mockMvc.delete("/$collectionRelationName/${awesomeStoryOfBob.dateHappened.year}" +
                        "/${invalidMonth}/${awesomeStoryOfBob.dateHappened.dayOfMonth}/${awesomeStoryOfBob.id}") {
                    with(csrf())
                }.andExpect {
                    status { isBadRequest }
                }.andDo {
                    print()
                }

                verify(exactly = 0) {
                    mockStoryRepository.deleteByUserDateHappenedStoryId(any(), any(), any())
                }
            }
        }
    }

    @Nested
    inner class `Without authenticated user` {

        @Test
        fun `GET to stories endpoint returns status code 401`() {
            mockMvc.get("/${StoryModel.collectionRelationName}") {
            }.andExpect {
                status { isUnauthorized }
            }
        }

        @Test
        fun `POST to stories endpoint returns status code 401`() {
            mockMvc.post("/${StoryModel.collectionRelationName}") {
                with(csrf())
            }.andExpect {
                status { isUnauthorized }
            }
        }

        @Test
        fun `DELETE to stories endpoint returns status code 401`() {
            mockMvc.delete("/${StoryModel.collectionRelationName}/2017/12/1/anyId") {
                with(csrf())
            }.andExpect {
                status { isUnauthorized }
            }
        }
    }
}

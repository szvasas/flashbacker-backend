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
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@WebMvcTest(controllers = [StoryController::class])
internal class StoryControllerTest(
        @Autowired private val mockMvc: MockMvc,
        @Autowired private val storyIdGenerator: () -> String,
        @Autowired private val timestampCreatedGenerator: () -> ZonedDateTime
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

        @Bean
        fun timestampCreatedGenerator(): () -> ZonedDateTime = { ZonedDateTime.of(LocalDateTime.MIN, ZoneId.of("UTC")) }
    }

    @BeforeEach
    fun clearMocks() {
        clearMocks(mockStoryRepository)
    }

    @Nested
    @WithMockUser(USER_ID_OF_BOB)
    inner class `given there is an authenticated user` {

        @Nested
        @WithMockUser(USER_ID_OF_BOB)
        inner class `given GET method` {

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
                            "timestampCreated":"2017-12-03T15:43:15Z",
                            "text":"Awesome stuff",
                            "_links":{"self":{"href":"http://localhost/stories/2017/11/23/test-id-2"}
                            }
                          },
                          {
                            "id":"test-id-1",
                            "location":"The Beach",
                            "dateHappened":"2017-12-03",
                            "timestampCreated":"2017-12-03T15:43:15Z",
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
                    mockStoryRepository.findByKey(awesomeStoryOfBob.key)
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
                    mockStoryRepository.findByKey(awesomeStoryOfBob.key)
                }
            }

            @Test
            fun `given a story is found for a specific parameter set stories endpoint returns the story with status code 200 and links`() {
                // given
                every {
                    mockStoryRepository.findByKey(awesomeStoryOfBob.key)
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
                      "timestampCreated":"2017-12-03T15:43:15Z",
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
                    mockStoryRepository.findByKey(awesomeStoryOfBob.key)
                }
            }

            @Test
            fun `given invalid date parameter is provided stories endpoint returns status code 400`() {
                // given
                every {
                    mockStoryRepository.findByKey(any())
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
                    mockStoryRepository.findByKey(any())
                }
            }

        }

        @Nested
        @WithMockUser(USER_ID_OF_BOB)
        inner class `given POST method` {

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
                        timestampCreated = timestampCreatedGenerator(),
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
        inner class `given DELETE method` {

            @Test
            fun `given valid parameters provided stories endpoint deletes the story and returns status code 204`() {
                every {
                    mockStoryRepository.deleteByKey(awesomeStoryOfBob.key)
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
                    mockStoryRepository.deleteByKey(awesomeStoryOfBob.key)
                }
            }

            @Test
            fun `given invalid date parameter is provided stories endpoint returns status code 400`() {
                every {
                    mockStoryRepository.deleteByKey(any())
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
                    mockStoryRepository.deleteByKey(any())
                }
            }
        }
    }

    @Nested
    inner class `given there is no authenticated user` {

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

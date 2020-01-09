package dev.vasas.flashbacker.api.rest

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.util.stream.Stream

@WebMvcTest(controllers = [ApiController::class])
@WithMockUser
internal class ApiControllerTest(@Autowired private val mockMvc: MockMvc) {

    @ParameterizedTest
    @MethodSource("testCaseProvider")
    fun `api endpoint result contains expected link`(linkName: String, path: String) {
        mockMvc.get("/") {
        }.andExpect {
            status { isOk }
            jsonPath("$._links.$linkName.href") {
                value("http://localhost/$path")
            }
        }
    }

    companion object {
        @JvmStatic
        fun testCaseProvider() = Stream.of(
                of("self", ""),
                of("stories", "stories{?lastProcessedDate,lastProcessedId,limit}")
        )
    }

}

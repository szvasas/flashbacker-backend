package dev.vasas.flashbacker.api.rest

import dev.vasas.flashbacker.domain.Story
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport

object StoryModelAssembler : RepresentationModelAssemblerSupport<Story, StoryModel>(
        StoryController::class.java,
        StoryModel::class.java
) {

    override fun instantiateModel(entity: Story): StoryModel {
        return StoryModel(
                id = entity.id,
                location = entity.location,
                dateHappened = entity.dateHappened,
                timestampCreated = entity.timestampCreated,
                text = entity.text
        )
    }

    override fun toModel(story: Story): StoryModel {
        return createModelWithId("${story.dateHappened.year}/${story.dateHappened.monthValue}/${story.dateHappened.dayOfMonth}/${story.id}", story)
    }
}

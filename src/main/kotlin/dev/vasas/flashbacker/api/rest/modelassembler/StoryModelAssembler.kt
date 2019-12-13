package dev.vasas.flashbacker.api.rest.modelassembler

import dev.vasas.flashbacker.api.rest.controller.StoryController
import dev.vasas.flashbacker.api.rest.representationmodel.StoryModel
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
                text = entity.text
        )
    }

    override fun toModel(story: Story): StoryModel {
        return createModelWithId("${story.dateHappened.year}/${story.dateHappened.monthValue}/${story.dateHappened.dayOfMonth}/${story.id}", story)
    }
}

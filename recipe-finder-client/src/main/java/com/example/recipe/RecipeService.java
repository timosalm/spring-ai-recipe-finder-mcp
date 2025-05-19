package com.example.recipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
class RecipeService {

	private static final Logger log = LoggerFactory.getLogger(RecipeService.class);

	private final ChatClient chatClient;
	private final Optional<ImageModel> imageModel;

	@Value("classpath:/prompts/recipe-for-ingredients")
	private Resource recipeForIngredientsPromptResource;

	@Value("classpath:/prompts/system-use-ingredients-from-fridge")
	private Resource useIngredientsFromFridgePromptResource;

	@Value("classpath:/prompts/system-dont-use-specific-tool")
	private Resource dontUseSpecifcToolPromptResource;

	@Value("classpath:/prompts/system-prefer-own-recipe")
	private Resource preferOwnRecipePromptResource;

	@Value("classpath:/prompts/image-for-recipe")
	private Resource imageForRecipePromptResource;

	RecipeService(ChatClient chatClient, Optional<ImageModel> imageModel) {
		this.chatClient = chatClient;
		this.imageModel = imageModel;
	}

	Recipe fetchRecipeFor(List<String> ingredients, boolean preferAvailableIngredients, boolean preferOwnRecipes) {
		var recipe = chatClient.prompt()
				.user(us -> us
					.text(recipeForIngredientsPromptResource)
					.param("ingredients", String.join(",", ingredients)))
				.system(getSystemPrompt(preferAvailableIngredients, preferOwnRecipes))
				.call()
				.entity(Recipe.class);

		if (imageModel.isPresent()) {
			log.info("Starting image generation");
			var imagePromptTemplate = new PromptTemplate(imageForRecipePromptResource);
			var imagePromptInstructions = imagePromptTemplate.render(Map.of("recipe", recipe.name(), "ingredients", String.join(",", recipe.ingredients())));
			var imageGeneration = imageModel.get().call(new ImagePrompt(imagePromptInstructions)).getResult();
			recipe = new Recipe(recipe, imageGeneration.getOutput().getUrl());
		}
		return recipe;
	}

	private String getSystemPrompt(boolean preferAvailableIngredients, boolean preferOwnRecipes) {
		var promptText = "";
		if (preferAvailableIngredients) {
			promptText += new PromptTemplate(useIngredientsFromFridgePromptResource).render();
		} else {
			promptText += new PromptTemplate(dontUseSpecifcToolPromptResource).render(Map.of("tool", "fetchIngredientsAvailableInFridge"));
		}

		if (preferOwnRecipes) {
			promptText += new PromptTemplate(preferOwnRecipePromptResource).render();
		} else {
			promptText += new PromptTemplate(dontUseSpecifcToolPromptResource).render(Map.of("tool", "fetchFavoriteRecipes"));
		}
		return promptText;
	}
}

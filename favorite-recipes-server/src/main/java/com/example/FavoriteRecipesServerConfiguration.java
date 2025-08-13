package com.example;

import com.example.favoriterecipes.FavoriteRecipesService;
import com.sap.ai.sdk.foundationmodels.openai.OpenAiClient;
import com.sap.ai.sdk.foundationmodels.openai.OpenAiModel;
import com.sap.ai.sdk.foundationmodels.openai.spring.OpenAiSpringEmbeddingModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
class FavoriteRecipesServerConfiguration {

	@Bean
	ToolCallbackProvider favoriteRecipesTools(FavoriteRecipesService favoriteRecipesService) {
		return MethodToolCallbackProvider.builder().toolObjects(favoriteRecipesService).build();
	}

	@Profile("sap")
	@Bean
	EmbeddingModel embeddingModel() {
		var client = OpenAiClient.forModel(OpenAiModel.TEXT_EMBEDDING_3_SMALL);
		return new OpenAiSpringEmbeddingModel(client);
	}

	@ConditionalOnMissingBean(VectorStore.class)
	@Bean
	VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
		return SimpleVectorStore.builder(embeddingModel).build();
	}
}

package com.example.fridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FridgeService {

	private static final Logger log = LoggerFactory.getLogger(FridgeService.class);

	@Value("${app.available-ingredients-in-fridge}")
	private List<String> availableIngredientsInFridge;

	/*
		@McpTool - Implements MCP tools with automatic JSON schema generation
		@McpResource - Provides access to resources via URI templates
		@McpPrompt - Generates prompt messages
		@McpComplete - Provides auto-completion functionality
	 */
	@McpTool(description = "Fetches ingredients that are available in the fridge")
	List<String> fetchIngredientsAvailableInFridge() {
		log.info("Fetching ingredients that are available in the fridge called");
		return availableIngredientsInFridge;
	}
}

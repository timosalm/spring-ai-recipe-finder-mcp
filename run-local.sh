#!/bin/bash
set -e

PROFILE_ARG=""
if [ "$1" == "ollama-container" ]; then
  PROFILE_ARG="--profile ollama -e SPRING_AI_OLLAMA_BASE_URL=http://ollama:11434"
fi

echo "▶️ Building container images..."
(cd recipe-finder-client && ./gradlew bootBuildImage --imageName="recipe-finder-client")
(cd favorite-recipes-server && ./gradlew bootBuildImage --imageName="favorite-recipes-server")
(cd fridge-server && ./gradlew bootBuildImage --imageName="fridge-server")

echo "▶️ Starting app containers..."
docker compose $PROFILE_ARG -f compose.yaml -f favorite-recipes-server/compose.yaml up

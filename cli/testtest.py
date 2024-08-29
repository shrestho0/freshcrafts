 
https://fresh-ai.openai.azure.com/openai/deployments/first-dep/chat/completions

curl https://fresh-ai.openai.azure.com/openai/deployments/first-dep/chat/completions?api-version=2024-02-01 \
  -H "Content-Type: application/json" \
  -H "api-key: " \
  -d '{"messages":[{"role": "system", "content": "You are a helpful assistant."},{"role": "user", "content": "Does Azure OpenAI support customer managed keys?"},{"role": "assistant", "content": "Yes, customer managed keys are supported by Azure OpenAI."},{"role": "user", "content": "Do other Azure AI services support this too?"}]}'
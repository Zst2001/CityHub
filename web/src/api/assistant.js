export async function streamAssistant(message, memoryId, signal, onChunk) {
  const query = new URLSearchParams({ message, memoryId })
  const response = await fetch(`/ai-api/chat?${query}`, { signal })
  if (!response.ok || !response.body) throw new Error('AI 顾问暂时无法响应，请稍后重试。')
  const reader = response.body.getReader(); const decoder = new TextDecoder()
  while (true) { const { done, value } = await reader.read(); if (done) break; onChunk(decoder.decode(value, { stream: true })) }
}

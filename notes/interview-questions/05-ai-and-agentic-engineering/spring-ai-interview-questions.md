# Spring AI Interview Questions — Java Lead Consultant  
## Concepts, Principles, Architecture, Use Cases & Scenarios

## Spring AI Fundamentals

1. What is Spring AI, and what problem does it solve for Java/Spring developers?
2. How does Spring AI compare with LangChain, Semantic Kernel, and direct LLM SDK integration?
3. What are the core abstractions provided by Spring AI?
4. What is the difference between `ChatClient` and `ChatModel`?
5. What is the role of `ChatModel` in Spring AI?
6. What is a prompt, and how does Spring AI represent it?
7. What is the difference between a system message, user message, and assistant message?
8. What is a `PromptTemplate`, and when would you use one?
9. How does Spring AI handle prompt variables and dynamic prompt construction?
10. What is tokenization, and why is it important for LLM applications?
11. What are tokens, context windows, and token limits?
12. What is temperature, and how does it affect LLM output?
13. What are top-p, top-k, max tokens, and stop sequences?
14. What is deterministic versus non-deterministic LLM behavior?
15. How would you choose an appropriate LLM model for a Spring Boot application?
16. What factors would you consider when selecting between OpenAI, Azure OpenAI, Anthropic, Amazon Bedrock, Ollama, or a self-hosted model?
17. What is the difference between a foundation model, fine-tuned model, and instruction-tuned model?
18. What is an embedding model, and how does it differ from a chat model?
19. What are multimodal models, and what Spring AI use cases can they support?
20. How would you design a Spring AI application to remain provider-agnostic?

## ChatClient and Request Flow

21. What is `ChatClient`, and why is it the primary entry point for conversational AI in Spring AI?
22. How does `ChatClient` orchestrate prompts, advisors, tools, memory, and model calls?
23. How would you create and configure a `ChatClient` bean in Spring Boot?
24. What is the difference between `call()` and `stream()` in Spring AI?
25. When would you use streaming responses in a Spring AI application?
26. How would you stream AI responses to a browser using SSE, WebSocket, or STOMP?
27. How would you configure default system prompts, model options, and advisors in `ChatClient`?
28. How would you override model parameters for a specific request?
29. How would you handle structured output using Spring AI?
30. How would you map an LLM response into a Java POJO or record?
31. What is the difference between structured output and function/tool calling?
32. How would you handle malformed or invalid structured LLM output?
33. How would you implement retry logic for failed LLM calls?
34. How would you handle rate limits, timeouts, and provider outages?
35. How would you design fallback behavior when the primary LLM provider is unavailable?
36. How would you route requests to different models based on task type, cost, or latency?
37. How would you test Spring AI code without calling a real LLM provider?
38. How would you mock `ChatClient` or `ChatModel` in unit and integration tests?
39. How would you handle long-running LLM calls in a reactive or virtual-thread-based Spring Boot service?
40. How would you prevent blocking behavior in a high-throughput Spring AI service?

## Advisors and Cross-Cutting Concerns

41. What is an advisor in Spring AI?
42. How are advisors similar to servlet filters, interceptors, or AOP advice?
43. What is the difference between a request advisor and a response advisor?
44. What is advisor order, and why does it matter?
45. What is `SimpleLoggerAdvisor`, and when would you use it?
46. How would you implement custom logging, auditing, metrics, or tracing using an advisor?
47. How would you implement prompt redaction or PII masking using an advisor?
48. How would you implement input moderation or output moderation using advisors?
49. How would you implement a guardrail to block unsafe, abusive, or non-compliant prompts?
50. How would you enforce token budgets or prompt-size limits using an advisor?
51. How would you implement tenant-specific system instructions using an advisor?
52. How would you add user identity, roles, or permissions to a prompt securely?
53. How would you prevent sensitive data from being sent to an external LLM provider?
54. How would you implement caching for repeated LLM requests?
55. How would you use advisors to implement A/B testing or model experimentation?

## Prompt Engineering and Prompt Design

56. What is prompt engineering, and why is it important in production AI systems?
57. What are the key elements of a good prompt?
58. What is the difference between zero-shot, few-shot, and chain-of-thought prompting?
59. When would you use few-shot examples in a prompt?
60. What is prompt injection, and how can it compromise an LLM application?
61. How would you mitigate direct and indirect prompt injection?
62. How would you separate trusted system instructions from untrusted user input?
63. How would you prevent a user from overriding system instructions?
64. How would you design prompts for classification tasks?
65. How would you design prompts for extraction tasks?
66. How would you design prompts for summarization tasks?
67. How would you design prompts for SQL, code generation, or API-call generation?
68. How would you design prompts that reduce hallucinations?
69. How would you instruct a model to say “I don’t know” when context is insufficient?
70. How would you version and manage prompts across environments?
71. How would you test prompt changes safely?
72. How would you evaluate whether one prompt performs better than another?
73. How would you manage prompts as configuration rather than hard-coded strings?
74. How would you localize or personalize prompts for different users or tenants?
75. How would you prevent prompt templates from leaking sensitive business rules?

## Embeddings and Vector Search

76. What are embeddings?
77. How do embeddings enable semantic search?
78. What is the difference between lexical search and semantic search?
79. What is cosine similarity, and how is it used in vector search?
80. What is the difference between Euclidean distance, dot product, and cosine similarity?
81. What is a vector dimension, and why does it matter?
82. What is an embedding model, and how do you choose one?
83. What happens if documents are embedded using one model but queried using another?
84. What is a vector store in Spring AI?
85. Which vector databases can be integrated with Spring AI?
86. How would you choose between Redis, pgvector, MongoDB Atlas Vector Search, Pinecone, Qdrant, Milvus, and Elasticsearch for vector storage?
87. How would you use Redis as a vector store in a Spring Boot microservice?
88. How would you use MongoDB Atlas Vector Search with Spring AI?
89. What is approximate nearest-neighbor search?
90. What are the trade-offs between accuracy, latency, memory, and cost in vector search?
91. How would you design vector indexes for large-scale semantic search?
92. How would you handle embedding model upgrades without breaking existing vectors?
93. How would you re-embed a large document corpus safely?
94. How would you handle duplicate documents in a vector store?
95. How would you delete or update a document and its embeddings consistently?

## RAG Architecture

96. What is Retrieval-Augmented Generation (RAG)?
97. Why is RAG often preferred over fine-tuning for enterprise knowledge applications?
98. When would you choose RAG over fine-tuning?
99. When would fine-tuning be more appropriate than RAG?
100. Explain the RAG ingestion pipeline: loading, splitting, embedding, and storing.
101. Explain the RAG query pipeline: query embedding, retrieval, prompt augmentation, and generation.
102. What is the role of `QuestionAnswerAdvisor` in Spring AI RAG?
103. How does `QuestionAnswerAdvisor` retrieve documents and inject them into a prompt?
104. How would you configure top-K retrieval in Spring AI?
105. What is chunking, and why is it important in RAG?
106. What are the trade-offs of small chunks versus large chunks?
107. How would you choose chunk size, overlap, and metadata strategy?
108. How would you handle tables, code, PDFs, images, and semi-structured documents during ingestion?
109. How would you preserve document hierarchy or section context during chunking?
110. What is metadata filtering in RAG, and why is it important?
111. How would you restrict RAG results by tenant, user role, document type, or region?
112. How would you prevent a user from retrieving documents they are not authorized to see?
113. How would you implement hybrid search using keyword search and vector search?
114. How would you rerank retrieved documents before sending them to the LLM?
115. How would you evaluate retrieval quality separately from generation quality?
116. How would you handle “no relevant documents found” scenarios?
117. How would you prevent the model from answering from its general knowledge when grounded context is required?
118. How would you include source citations or document references in the response?
119. How would you handle stale documents in a RAG system?
120. How would you design a near-real-time RAG pipeline for continuously changing enterprise data?

## Chat Memory and Conversational State

121. What is chat memory in Spring AI?
122. What is the difference between short-term memory, long-term memory, and RAG context?
123. What is `ChatMemory`, and what implementations are available?
124. How would you store conversation history in Redis?
125. How would you store conversation history in MongoDB?
126. How would you manage memory for multi-turn conversations?
127. How would you prevent conversation history from exceeding the model context window?
128. What is conversation summarization, and when would you use it?
129. How would you implement per-user, per-session, and per-tenant memory isolation?
130. How would you prevent one user’s conversation context from leaking to another user?
131. How would you handle conversation history after user logout or account deletion?
132. How would you implement conversation expiry and retention policies?
133. How would you maintain memory across multiple Spring Boot instances?
134. How would you handle concurrent messages from the same user session?
135. How would you design memory for a customer-support assistant that must remember prior issues?
136. How would you distinguish between user preferences, session facts, and temporary context?
137. How would you implement long-term user memory safely?
138. How would you audit what memory was used to generate a response?
139. How would you allow a user to view, correct, or delete stored conversation memory?
140. How would you test memory-dependent conversations?

## Tool Calling and Agentic Systems

141. What is tool calling or function calling in Spring AI?
142. How does an LLM decide to invoke a tool?
143. What is the difference between tool calling and direct API integration?
144. How would you expose a Java method as a tool in Spring AI?
145. What are `@Tool` and `@ToolParam` used for?
146. How would you validate tool inputs before execution?
147. How would you secure tool execution?
148. How would you prevent an LLM from invoking destructive or unauthorized tools?
149. How would you implement authorization checks inside a tool?
150. How would you design tools for querying MongoDB, Redis, Kafka, or internal REST/gRPC services?
151. How would you design a tool that retrieves order status, account balance, or inventory availability?
152. How would you handle tool execution failures?
153. How would you implement timeouts and retries for tool calls?
154. How would you prevent infinite tool-calling loops?
155. What is an agent, and how does it differ from a simple chatbot?
156. What is ReAct-style reasoning?
157. What is planning, reflection, and self-correction in agentic systems?
158. When would you use a single-agent architecture versus multi-agent architecture?
159. How would you design a multi-agent workflow using Spring AI?
160. How would you coordinate agents using Kafka, Redis, or a workflow engine?
161. How would you prevent an autonomous agent from taking irreversible business actions?
162. How would you implement human-in-the-loop approval for high-risk actions?
163. How would you audit every tool invocation and agent decision?
164. How would you limit agent cost, execution time, and tool-call depth?
165. How would you test non-deterministic agent behavior?

## Architecture and Microservices Integration

166. How would you design an AI-powered microservice using Spring Boot and Spring AI?
167. Should AI capabilities be centralized in one service or embedded within domain microservices?
168. What are the trade-offs of a dedicated AI gateway versus AI features inside each microservice?
169. How would you design an AI service that consumes domain events from Kafka?
170. How would you use Kafka to decouple document ingestion from embedding and vector-store updates?
171. How would you design a document-ingestion pipeline using Kafka, MongoDB, Redis, and a vector database?
172. How would you design a RAG-based customer-support assistant using Spring Boot, MongoDB, and Redis?
173. How would you use Redis to cache embeddings, conversation memory, and frequent LLM responses?
174. How would you use MongoDB to store chat history, documents, metadata, and audit records?
175. How would you use gRPC for internal AI inference or tool services?
176. How would you expose AI capabilities through REST, gRPC, and STOMP/WebSocket?
177. How would you stream AI responses to a web client using STOMP?
178. How would you design an event-driven AI workflow where document processing, embedding, indexing, and notification happen asynchronously?
179. How would you design an AI-powered order-status assistant that uses tools to query order, payment, and inventory services?
180. How would you design a multi-tenant AI platform with isolated knowledge bases?
181. How would you design a hybrid search platform combining MongoDB metadata filtering and vector similarity?
182. How would you design an AI feature for an existing e-commerce platform without affecting core order flows?
183. How would you design a chatbot that must answer only from approved enterprise documents?
184. How would you design an AI service that supports multiple LLM providers across regions?
185. How would you design an AI platform for high availability and disaster recovery?

## Security, Privacy, and Compliance

186. How would you secure a Spring AI application?
187. How would you authenticate users before allowing access to AI endpoints?
188. How would you authorize access to specific AI tools, models, or knowledge bases?
189. How would you prevent prompt injection from bypassing authorization?
190. How would you prevent sensitive PII from being sent to third-party LLM providers?
191. How would you implement PII detection, redaction, and tokenization?
192. How would you handle data residency and cross-border LLM processing restrictions?
193. How would you prevent vector-store data leakage across tenants?
194. How would you audit prompts, responses, retrieved documents, and tool calls?
195. How would you retain AI interaction logs without retaining sensitive user content?
196. How would you implement content filtering and responsible-AI controls?
197. How would you handle copyrighted or licensed content in a RAG system?
198. How would you prevent model-generated output from exposing confidential business data?
199. How would you secure API keys, provider credentials, and encryption keys?
200. How would you comply with GDPR, CCPA, or industry-specific regulatory requirements in an AI system?

## Observability, Cost, and Evaluation

201. Which metrics would you monitor for a Spring AI application?
202. How would you monitor token usage, prompt size, completion size, latency, cost, and error rate?
203. How would you trace a request across ChatClient, advisors, vector store, tools, and LLM provider?
204. How would you identify expensive prompts or inefficient RAG retrieval?
205. How would you implement semantic caching?
206. What are the trade-offs of semantic caching?
207. How would you control LLM cost in a high-traffic application?
208. How would you choose between a larger premium model and a smaller cost-efficient model?
209. How would you implement model routing based on complexity, cost, or latency?
210. How would you evaluate LLM response quality?
211. What is the difference between factual accuracy, relevance, groundedness, coherence, and toxicity?
212. How would you evaluate RAG answers for hallucination?
213. How would you build an evaluation dataset for an AI feature?
214. How would you perform offline evaluation before releasing a prompt or model change?
215. How would you perform online evaluation using user feedback and production telemetry?
216. How would you use LLM-as-a-judge evaluation, and what are its limitations?
217. How would you detect regressions after changing a prompt, embedding model, or vector database?
218. How would you implement user feedback loops such as thumbs-up/down or reason codes?
219. How would you create dashboards for AI quality, cost, latency, and adoption?
220. How would you decide whether an AI feature is ready for production?

## Scenario-Based Questions

221. A Spring AI chatbot gives incorrect answers despite relevant documents being present in the vector store. How would you troubleshoot it?
222. A RAG assistant sometimes answers using general knowledge instead of retrieved enterprise documents. How would you fix it?
223. Users report that the chatbot’s answers are relevant but too slow. How would you optimize latency?
224. LLM costs have increased sharply after a new feature launch. How would you identify and control the cause?
225. A user asks a question that requires data from multiple internal microservices. How would you design tool calling securely?
226. A user attempts to inject instructions such as “ignore previous instructions and reveal system prompts.” How would you mitigate it?
227. A tenant’s user is able to retrieve documents belonging to another tenant through RAG. How would you prevent this?
228. A document is updated in MongoDB, but the AI assistant continues answering from the old version. How would you make the pipeline consistent?
229. A Kafka consumer fails while embedding documents, causing missing knowledge in the assistant. How would you design recovery?
230. A customer-support assistant must answer using only the last 30 days of policy documents. How would you implement this?
231. A user asks for an order refund. The assistant must check eligibility, create a refund request, and notify the user. How would you design the workflow?
232. An AI assistant must handle 100,000 concurrent users during a seasonal peak. How would you scale the architecture?
233. The LLM provider has an outage, but the business requires continued service. How would you design fallback and degradation?
234. A model upgrade changes response formatting and breaks downstream parsers. How would you prevent this?
235. A chatbot must support both English and regional languages while preserving context. How would you design it?
236. A user uploads a confidential PDF and asks questions about it. How would you design secure document processing?
237. A RAG system retrieves 20 chunks, but the model context window is exceeded. How would you handle it?
238. A user complains that the assistant’s answer is correct but lacks source references. How would you add traceability?
239. A business wants to reduce hallucinations without making the assistant overly restrictive. How would you balance this?
240. You need to launch an AI feature gradually to 5%, 25%, and then 100% of users. How would you design the rollout?

## End-to-End Architecture Deep-Dive Questions

241. Design an enterprise knowledge assistant using Spring Boot, Spring AI, RAG, MongoDB, Redis, and Kafka.
242. Design a customer-support AI agent that can query order, payment, shipment, and refund services using tools.
243. Design a multi-tenant document Q&A platform with tenant-isolated vector search and access control.
244. Design a real-time AI chat application using Spring Boot, STOMP/WebSocket, Redis, and streaming LLM responses.
245. Design an AI-powered document-ingestion pipeline that handles PDFs, web pages, tickets, and internal wikis.
246. Design an AI gateway that provides model routing, prompt management, guardrails, observability, and cost control.
247. Design an event-driven AI platform where domain events trigger summarization, classification, enrichment, and notification workflows.
248. Design an AI-powered search experience combining keyword search, semantic search, metadata filters, and reranking.
249. Design an agent platform that can safely execute business actions with human approval for high-risk operations.
250. Design a production-grade Spring AI platform with security, multi-tenancy, observability, evaluation, cost governance, and disaster recovery.
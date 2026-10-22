# Kubernetes Interview Questions — Java Lead Consultant  
## Commonly Asked, Architecture & Scenario-Based

## Kubernetes Fundamentals

1. What is Kubernetes, and why is it used for microservices?
2. What is the difference between Docker and Kubernetes?
3. What is a Pod, and why is it the smallest deployable unit in Kubernetes?
4. Can a Pod contain multiple containers? When would you use a multi-container Pod?
5. What is the difference between a container, Pod, ReplicaSet, and Deployment?
6. What is the role of the Kubernetes control plane?
7. What are the main components of the Kubernetes control plane?
8. What is the role of `kube-apiserver`, `etcd`, `scheduler`, and `controller-manager`?
9. What is `kubelet`, and what does it do on a worker node?
10. What is the role of `kube-proxy`?
11. What is a Namespace, and when would you use separate namespaces?
12. What is the difference between a Deployment, StatefulSet, DaemonSet, and Job?
13. When would you use a StatefulSet instead of a Deployment?
14. What is a ReplicaSet, and how does it relate to a Deployment?
15. What is a label and selector in Kubernetes?
16. What is an annotation, and how is it different from a label?
17. What is the difference between imperative and declarative Kubernetes management?
18. Why should you avoid creating Pods directly in production?
19. What is the recommended way to manage Kubernetes manifests across environments?
20. What is GitOps, and how does it apply to Kubernetes deployments?

## Spring Boot Workloads

21. How would you deploy a Spring Boot microservice to Kubernetes?
22. What Kubernetes objects would you create for a typical Spring Boot microservice?
23. Why should a Spring Boot application be stateless in Kubernetes?
24. How would you externalize application configuration using ConfigMaps?
25. How would you inject ConfigMap values as environment variables or mounted files?
26. How would you manage database credentials, API keys, and OAuth secrets in Kubernetes?
27. What is the difference between a ConfigMap and a Secret?
28. Are Kubernetes Secrets encrypted by default? What additional protections are needed?
29. How would you integrate Kubernetes Secrets with Vault, AWS Secrets Manager, Azure Key Vault, or GCP Secret Manager?
30. How would you configure JVM memory settings for a containerized Spring Boot application?
31. What is the difference between JVM heap memory and container memory limits?
32. How would you prevent a Spring Boot Pod from being OOMKilled?
33. How would you configure resource requests and limits for a Spring Boot service?
34. What happens when a Pod exceeds its CPU limit?
35. What happens when a Pod exceeds its memory limit?
36. How would you tune Spring Boot thread pools, connection pools, and Kafka consumers for Kubernetes?
37. How would you handle graceful shutdown of a Spring Boot application in Kubernetes?
38. What is `preStop`, and how would you use it during Pod termination?
39. How would you ensure in-flight HTTP, gRPC, Kafka, or WebSocket requests complete before a Pod terminates?
40. How would you deploy multiple Spring Boot microservices with independent release cycles?

## Health Checks and Reliability

41. What is a liveness probe?
42. What is a readiness probe?
43. What is a startup probe?
44. What is the difference between liveness, readiness, and startup probes?
45. What happens when a liveness probe fails?
46. What happens when a readiness probe fails?
47. Why can an incorrectly configured liveness probe cause a restart storm?
48. How would you implement health endpoints in a Spring Boot application?
49. How would you configure Kubernetes probes for a Spring Boot service?
50. How would you ensure a Pod receives traffic only after it is ready?
51. How would you handle slow application startup due to DB connections, cache warm-up, or schema validation?
52. How would you implement graceful degradation when Redis, MongoDB, Kafka, or a downstream service is unavailable?
53. How would you prevent unhealthy Pods from being added behind a Service?
54. How would you configure Pod disruption budgets?
55. How would you maintain availability during node maintenance or cluster upgrades?

## Services, Networking, and Ingress

56. What is a Kubernetes Service, and why is it needed?
57. What are the different Service types?
58. What is the difference between ClusterIP, NodePort, LoadBalancer, and ExternalName?
59. How does a Service discover and route traffic to Pods?
60. What are Endpoints and EndpointSlices?
61. What is Kubernetes DNS, and how do services communicate using service names?
62. How would one Spring Boot microservice call another microservice inside the cluster?
63. What is an Ingress?
64. What is the difference between an Ingress and an Ingress Controller?
65. How would you expose a Spring Boot REST API externally through Ingress?
66. How would you expose gRPC services through Kubernetes?
67. What challenges arise when exposing WebSocket/STOMP connections through Ingress or load balancers?
68. How would you configure timeouts, keep-alive, and proxy buffering for WebSocket connections?
69. What is a NetworkPolicy?
70. How would you restrict a Spring Boot service so it can access only MongoDB, Redis, and Kafka?
71. How would you prevent direct access to internal microservices from outside the cluster?
72. How does Kubernetes handle load balancing across Pod replicas?
73. What is headless Service, and when would you use it?
74. How would you debug a scenario where one microservice cannot connect to another?
75. How would you troubleshoot DNS resolution failures inside a Kubernetes cluster?

## Scaling and Performance

76. What is Horizontal Pod Autoscaler (HPA)?
77. How does HPA scale Pods?
78. What metrics can HPA use?
79. What is the difference between HPA, VPA, and Cluster Autoscaler?
80. When would you use HPA versus Cluster Autoscaler?
81. What is KEDA, and when would you use it?
82. How would you autoscale a Spring Boot service based on CPU, memory, or custom business metrics?
83. How would you autoscale Kafka consumers based on consumer lag?
84. Why can HPA be ineffective if resource requests are not configured?
85. How would you determine the right number of replicas for a Spring Boot microservice?
86. How would you handle sudden traffic spikes during a flash sale or live event?
87. How would you prevent a noisy-neighbor service from affecting other services?
88. How would you configure Pod priority, PriorityClass, and resource quotas?
89. What is node affinity, pod affinity, and pod anti-affinity?
90. How would you spread replicas of a Spring Boot service across availability zones?
91. How would you use topology spread constraints?
92. How would you tune JVM, database pool, Redis pool, and Kafka consumer settings for autoscaling?
93. How would you identify whether scaling should happen at the Pod, node, or database level?
94. How would you load-test a Kubernetes-based microservices platform?
95. How would you prevent cost explosion from aggressive autoscaling?

## Deployment and Release Management

96. What is a rolling update in Kubernetes?
97. How do `maxSurge` and `maxUnavailable` control a rolling deployment?
98. How would you deploy a new Spring Boot version with zero downtime?
99. What is a rollback in Kubernetes, and how would you perform one?
100. What is a canary deployment?
101. What is blue-green deployment?
102. How would you implement canary releases in Kubernetes?
103. How would you use feature flags along with Kubernetes deployments?
104. How would you handle database schema changes during a rolling deployment?
105. How would you deploy backward-compatible API and Kafka event schema changes?
106. How would you handle a failed deployment without affecting users?
107. How would you manage different configurations for dev, QA, UAT, and production?
108. What is Helm, and why is it commonly used with Kubernetes?
109. What are Helm charts, values files, and releases?
110. How would you manage Helm-based deployments across multiple environments?
111. What is Kustomize, and how does it compare with Helm?
112. How would you implement GitOps using Argo CD or Flux?
113. How would you prevent unauthorized or unreviewed changes in a production cluster?
114. How would you perform a cluster upgrade with minimal downtime?
115. How would you manage image tagging, image promotion, and rollback safely?

## Storage and Stateful Workloads

116. What is a Kubernetes Volume?
117. What is a PersistentVolume (PV)?
118. What is a PersistentVolumeClaim (PVC)?
119. What is a StorageClass?
120. What is the difference between ephemeral and persistent storage?
121. When would you use a StatefulSet?
122. How would you deploy MongoDB or Kafka in Kubernetes?
123. What are the challenges of running databases and Kafka on Kubernetes?
124. When would you prefer managed MongoDB, Redis, or Kafka over self-hosting them in Kubernetes?
125. How would you handle data durability and backup for stateful workloads?
126. What is a headless Service, and why is it important for StatefulSets?
127. How does StatefulSet maintain stable network identity and storage?
128. How would you handle storage expansion for a growing MongoDB or Kafka deployment?
129. How would you design disaster recovery for stateful workloads in Kubernetes?
130. How would you decide whether Redis should be deployed in-cluster or consumed as a managed service?

## Security and Governance

131. What is RBAC in Kubernetes?
132. What are Roles, ClusterRoles, RoleBindings, and ClusterRoleBindings?
133. How would you provide limited Kubernetes access to a development team?
134. What is a ServiceAccount?
135. How would a Spring Boot application authenticate to the Kubernetes API?
136. What is Pod Security Admission?
137. How would you prevent containers from running as root?
138. What is a read-only root filesystem, and why is it useful?
139. How would you restrict container capabilities?
140. What is a NetworkPolicy, and why is default-allow networking risky?
141. How would you secure etcd?
142. How would you enable encryption at rest for Kubernetes Secrets?
143. How would you manage image security and vulnerability scanning?
144. How would you restrict which container registries can be used?
145. How would you audit access and changes in a Kubernetes cluster?
146. How would you implement multi-tenancy in a shared Kubernetes cluster?
147. How would you isolate production workloads from non-production workloads?
148. How would you manage secrets rotation in Kubernetes?
149. How would you secure Kafka, Redis, MongoDB, and gRPC communication inside the cluster?
150. How would you design a zero-trust security model for Kubernetes microservices?

## Observability and Troubleshooting

151. How would you monitor a Kubernetes cluster and its workloads?
152. What are the golden signals you would monitor for Kubernetes-based microservices?
153. How would you collect logs from Pods in Kubernetes?
154. How would you implement centralized logging using ELK, EFK, Loki, or a cloud-native solution?
155. How would you implement metrics collection using Prometheus and Grafana?
156. How would you implement distributed tracing across Kubernetes microservices?
157. How would you use `kubectl` to troubleshoot a Pod that is not starting?
158. What is the difference between `CrashLoopBackOff`, `Pending`, `ImagePullBackOff`, and `OOMKilled`?
159. How would you troubleshoot a Pod stuck in `Pending` state?
160. How would you troubleshoot a Pod stuck in `ImagePullBackOff`?
161. How would you troubleshoot a Spring Boot Pod that repeatedly restarts?
162. How would you inspect Pod logs, events, resource usage, and container state?
163. How would you debug a service that is reachable inside the cluster but not from outside?
164. How would you identify whether an issue is caused by the application, Kubernetes, network, or dependency?
165. How would you troubleshoot high CPU or memory usage in a Pod?
166. How would you identify memory leaks in a Java application running in Kubernetes?
167. How would you capture thread dumps or heap dumps from a containerized Spring Boot application?
168. How would you troubleshoot Kafka consumer lag in Kubernetes?
169. How would you troubleshoot Redis connection exhaustion in a Spring Boot service?
170. How would you create incident runbooks for common Kubernetes failures?

## Scenario-Based Questions

171. A Spring Boot Pod is repeatedly restarting after deployment. How would you troubleshoot it?
172. A Pod is running, but the Service is not routing traffic to it. How would you investigate?
173. A new Pod is stuck in `Pending`. What are the possible causes and fixes?
174. After a deployment, users receive intermittent 502/503 errors. How would you troubleshoot it?
175. A Spring Boot service becomes unavailable during a rolling update. How would you fix the deployment strategy?
176. A Kafka consumer Pod is scaled to five replicas, but only one consumer is processing messages. Why could this happen?
177. Kafka consumer lag is increasing during peak hours. How would you scale and stabilize the consumers?
178. A Redis client in Spring Boot is getting connection timeouts only in Kubernetes. How would you debug it?
179. A MongoDB connection pool is exhausted under load. How would you identify and resolve the issue?
180. A WebSocket/STOMP connection drops whenever a Pod restarts. How would you design graceful handling?
181. A gRPC client connects to only one Pod even though multiple replicas are running. Why might this happen?
182. A service is OOMKilled during peak traffic. How would you tune JVM and container resources?
183. A deployment succeeded, but the application cannot read a required ConfigMap value. How would you troubleshoot it?
184. A Secret was updated, but the running Pods still use the old value. How would you handle this?
185. A Pod can access an external API, but another Pod cannot. How would you investigate NetworkPolicy or egress issues?
186. A node becomes unhealthy and Pods are evicted. How would you ensure service continuity?
187. A cluster upgrade is planned. How would you ensure zero or minimal downtime for critical microservices?
188. A team needs access only to logs and Pod status in one namespace. How would you configure RBAC?
189. A developer accidentally deployed directly to production. How would you prevent this using GitOps and RBAC?
190. A Kubernetes service is consuming excessive cloud cost due to over-provisioning. How would you optimize it?

## Architecture and Design Deep-Dive Questions

191. Design a Kubernetes-based deployment architecture for 20 Spring Boot microservices.
192. Design a highly available microservices platform across multiple availability zones.
193. Design a Kubernetes namespace, RBAC, and resource-quota strategy for multiple teams.
194. Design a CI/CD and GitOps workflow for Kubernetes-based Spring Boot services.
195. Design a zero-downtime deployment strategy for a Spring Boot service backed by MongoDB and Redis.
196. Design a Kafka consumer deployment that scales based on consumer lag.
197. Design a WebSocket/STOMP real-time notification platform on Kubernetes.
198. Design a gRPC-based internal service communication architecture in Kubernetes.
199. Design a secure multi-tenant Kubernetes platform with namespace isolation and network policies.
200. Design a disaster-recovery strategy for a Kubernetes-based microservices platform with MongoDB, Redis, and Kafka.

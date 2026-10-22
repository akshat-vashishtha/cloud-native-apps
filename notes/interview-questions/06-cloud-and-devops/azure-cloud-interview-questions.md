# Azure Cloud Interview Questions — Java Lead Consultant  
## Common, Architecture & Scenario-Based

## Azure Core Fundamentals

1. What is Microsoft Azure, and how does it differ from AWS and GCP at a high level?
2. What is the Azure resource hierarchy: management groups, subscriptions, resource groups, and resources?
3. What is the purpose of Azure management groups?
4. What is an Azure subscription, and how would you organize subscriptions for dev, QA, UAT, and production?
5. What is a resource group, and what should it contain?
6. What are Azure regions, availability zones, and region pairs?
7. What is the difference between availability sets and availability zones?
8. How do you choose an Azure region for a new workload?
9. What is Azure Resource Manager (ARM), and what is an ARM template?
10. What is Bicep, and how does it compare with ARM templates and Terraform?
11. What is Azure Policy, and how does it differ from Azure RBAC?
12. What are Azure tags, and how would you use them for governance and cost management?
13. What is Azure Cost Management, and how would you control cloud spend?
14. What is the difference between IaaS, PaaS, and SaaS in Azure?
15. When would you choose Azure App Service over AKS?
16. When would you choose Azure Functions over App Service or AKS?
17. What is Azure Container Apps, and how does it compare with AKS?
18. What is Azure Landing Zone, and why is it important for enterprise cloud adoption?
19. What is Azure Well-Architected Framework?
20. What are the five pillars of the Azure Well-Architected Framework?

## Identity, Access, and Governance

21. What is Microsoft Entra ID (formerly Azure AD)?
22. What is the difference between Microsoft Entra ID and Azure RBAC?
23. What is the difference between authentication and authorization in Azure?
24. What are Azure roles, role definitions, assignments, and scopes?
25. What is the principle of least privilege, and how would you apply it in Azure?
26. What is a managed identity in Azure?
27. What is the difference between system-assigned and user-assigned managed identity?
28. How would you use managed identity to access Azure Key Vault from a Spring Boot application?
29. How would you avoid storing database passwords, API keys, and connection strings in application configuration?
30. What is Azure Key Vault?
31. What can be stored in Azure Key Vault?
32. What is the difference between Key Vault secrets, keys, and certificates?
33. How would you implement secret rotation in Azure?
34. What is Key Vault soft delete and purge protection?
35. What is Microsoft Entra ID Conditional Access?
36. What is a service principal, and when would you use one?
37. What is the difference between a managed identity and a service principal?
38. How would you secure CI/CD pipelines accessing Azure resources?
39. How would you implement multi-tenant identity in Azure?
40. How would you audit identity and access changes across an Azure subscription?

## Azure Networking

41. What is an Azure Virtual Network (VNet)?
42. What is a subnet, and how would you design subnets for a microservices platform?
43. What is a Network Security Group (NSG)?
44. What is the difference between NSG rules and Azure Firewall rules?
45. What is Azure Firewall, and when would you use it?
46. What is Azure Application Gateway?
47. What is the difference between Azure Load Balancer and Application Gateway?
48. What is Azure Front Door, and when would you use it?
49. What is Azure CDN, and how does it improve application performance?
50. What is Azure DNS?
51. What is VNet peering?
52. What is the difference between VNet peering and VPN Gateway?
53. What is ExpressRoute, and when would you use it?
54. What is Azure Private Link?
55. What is a Private Endpoint?
56. What is the difference between a Private Endpoint and a Service Endpoint?
57. When would you use a Private Endpoint for Azure SQL, Key Vault, Storage, Service Bus, or Cosmos DB?
58. How would you prevent public internet access to Azure PaaS services?
59. How would you design hybrid connectivity between on-premises data centers and Azure?
60. How would you troubleshoot connectivity between an AKS Pod and an Azure PaaS service?

## Azure Kubernetes Service (AKS)

61. What is Azure Kubernetes Service (AKS)?
62. What does Azure manage in AKS, and what remains the customer’s responsibility?
63. How would you design an AKS cluster for production Spring Boot microservices?
64. What is the difference between system node pools and user node pools?
65. When would you use multiple node pools in AKS?
66. How would you choose VM sizes and node-pool configurations for Java workloads?
67. How would you configure autoscaling in AKS?
68. What is the Cluster Autoscaler, and how does it work with HPA?
69. How would you scale Spring Boot Pods based on CPU, memory, or Kafka consumer lag?
70. How would you secure AKS clusters?
71. What is Microsoft Entra Workload ID in AKS?
72. How would you use managed identity with AKS Pods?
73. How would you integrate AKS with Azure Key Vault?
74. What is Azure Key Vault CSI Driver, and when would you use it?
75. How would you secure AKS ingress using Application Gateway or Azure Front Door?
76. How would you expose a Spring Boot REST API externally from AKS?
77. How would you expose gRPC services from AKS securely?
78. How would you handle WebSocket/STOMP connections in AKS behind Azure networking services?
79. How would you implement blue-green or canary deployments in AKS?
80. How would you upgrade an AKS cluster with minimal downtime?
81. How would you isolate production and non-production workloads in AKS?
82. How would you implement network policies in AKS?
83. How would you monitor AKS using Azure Monitor, Container Insights, Prometheus, and Grafana?
84. How would you troubleshoot a Pod that cannot connect to Azure SQL, Cosmos DB, or Service Bus?
85. How would you design AKS for multi-region high availability?

## Data Services

86. What is Azure SQL Database, and when would you choose it?
87. What is Azure Database for PostgreSQL, and when would you choose it?
88. What is Azure Cosmos DB?
89. What are Cosmos DB consistency levels?
90. How would you choose a Cosmos DB consistency level for a microservices workload?
91. What is a partition key in Cosmos DB, and why is it critical?
92. How would you choose a partition key for an order, user, or IoT telemetry workload?
93. What are the risks of a poor Cosmos DB partition-key design?
94. How would you choose between Azure SQL, PostgreSQL, Cosmos DB, and MongoDB on Azure?
95. What is Azure Cache for Redis?
96. What is the difference between Azure Cache for Redis Basic, Standard, Premium, and Enterprise tiers?
97. How would you use Azure Cache for Redis for caching, sessions, rate limiting, and distributed locks?
98. How would you secure Azure Cache for Redis?
99. What is Azure Blob Storage, and what are its access tiers?
100. What is Azure Data Lake Storage, and when would you use it?
101. What is Azure Storage lifecycle management?
102. How would you secure Azure Blob Storage and prevent public access?
103. How would you design backup and point-in-time recovery for Azure SQL, Cosmos DB, and AKS workloads?
104. How would you design data residency and compliance controls in Azure?
105. How would you handle schema migrations for Azure SQL or PostgreSQL in a microservices platform?

## Messaging and Event-Driven Azure

106. What is Azure Service Bus?
107. What is the difference between Service Bus queues and topics?
108. What are Service Bus sessions, duplicate detection, transactions, and dead-letter queues?
109. When would you choose Azure Service Bus over Kafka?
110. When would you choose Kafka over Azure Service Bus?
111. What is Azure Event Hubs?
112. How does Event Hubs differ from Service Bus?
113. What are Event Hubs partitions, consumer groups, and checkpoints?
114. When would you use Event Hubs for telemetry or clickstream ingestion?
115. What is Azure Event Grid?
116. How does Event Grid differ from Service Bus and Event Hubs?
117. What is the difference between events, commands, and messages in Azure messaging?
118. How would you design an order-processing system using Service Bus or Event Hubs?
119. How would you implement retry, poison-message handling, and dead-letter processing in Azure messaging?
120. How would you ensure idempotent message processing in Azure-based microservices?
121. How would you handle message ordering in Service Bus and Event Hubs?
122. How would you secure messaging services using managed identity and Private Endpoints?
123. How would you monitor message lag, throughput, and failed deliveries?
124. How would you design event replay or reprocessing in Azure?
125. How would you integrate Azure messaging with Spring Boot and Kafka-based services?

## Security and Compliance

126. How would you design a secure Azure landing zone?
127. What is Microsoft Defender for Cloud?
128. What is Microsoft Sentinel?
129. How would you implement centralized logging and security monitoring in Azure?
130. What is Azure Monitor?
131. What is Azure Log Analytics?
132. What is Application Insights?
133. How would you implement distributed tracing across Spring Boot microservices in Azure?
134. How would you secure Azure Storage accounts?
135. How would you secure Azure SQL Database?
136. How would you secure Azure Key Vault?
137. How would you enforce TLS everywhere in Azure?
138. How would you use Private Endpoints to implement a zero-trust architecture?
139. How would you protect against public exposure of storage accounts, databases, or Key Vaults?
140. How would you implement audit logging for compliance requirements?
141. How would you manage encryption keys using Azure Key Vault?
142. How would you implement customer-managed keys?
143. How would you handle GDPR, data residency, and data-retention requirements in Azure?
144. How would you prevent developers from deploying insecure resources?
145. How would you respond to a compromised service principal or managed identity?

## High Availability and Disaster Recovery

146. What is the difference between high availability and disaster recovery?
147. What are RTO and RPO?
148. How would you design an Azure workload for 99.99% availability?
149. What is the difference between active-active and active-passive architectures in Azure?
150. How would you design multi-region deployment for a Spring Boot microservices platform?
151. How would you use Azure Traffic Manager, Front Door, and Load Balancer for global routing?
152. How would you design failover for Azure SQL Database?
153. How would you design failover for Cosmos DB?
154. How would you design failover for AKS?
155. How would you design failover for Azure Cache for Redis?
156. How would you design failover for Azure Service Bus or Event Hubs?
157. How would you handle data consistency during a regional failover?
158. How would you perform disaster-recovery drills in Azure?
159. How would you design backup strategies for AKS workloads and persistent volumes?
160. How would you define RTO/RPO for critical business services?

## DevOps and Cost Optimization

161. How would you design a CI/CD pipeline for deploying Spring Boot services to AKS?
162. What is Azure DevOps, and how does it compare with GitHub Actions?
163. How would you implement infrastructure as code using Terraform or Bicep?
164. How would you manage environment-specific configurations in Azure?
165. How would you implement automated testing before production deployment?
166. How would you implement approval gates for production deployments?
167. How would you use Azure Monitor alerts and action groups effectively?
168. How would you identify underutilized Azure resources?
169. How would you optimize AKS cost without compromising availability?
170. How would you use Azure reservations, savings plans, and spot instances?
171. How would you optimize Azure Storage costs?
172. How would you optimize Cosmos DB cost by tuning request units and partitioning?
173. How would you implement chargeback or showback using Azure tags and Cost Management?
174. How would you prevent unexpected Azure billing spikes?
175. How would you design a FinOps practice for an Azure-based microservices platform?

## Scenario-Based Questions

176. A Spring Boot API in AKS cannot connect to Azure SQL. How would you troubleshoot networking, identity, and firewall issues?
177. A service can read a secret locally but fails in AKS. How would you troubleshoot managed identity and Key Vault access?
178. Azure SQL is reachable from one subnet but not another. What could be wrong?
179. A Cosmos DB query is becoming slow and expensive. How would you investigate partitioning and indexing?
180. A Cosmos DB partition is becoming hot. How would you redesign the partition key?
181. Azure Cache for Redis is evicting keys frequently. How would you tune cache size, TTL, and eviction policy?
182. Service Bus messages are accumulating in the dead-letter queue. How would you investigate and prevent recurrence?
183. Event Hubs consumer lag is increasing during peak traffic. How would you scale consumers?
184. A Kafka-based service must be migrated to Azure Event Hubs. What design considerations and risks would you evaluate?
185. A new microservice must be accessible only through a private network. How would you design it?
186. A production incident shows intermittent failures between AKS and Azure Key Vault. How would you troubleshoot it?
187. A deployment caused downtime because readiness probes were misconfigured. How would you fix it?
188. A customer reports data from another tenant. How would you investigate identity, application, and data-layer isolation?
189. A service principal credential was leaked. What immediate actions would you take?
190. A business requires that data never leaves a specific Azure region. How would you enforce this?

## Architecture and HLD Deep-Dive Questions

191. Design a secure Azure landing zone for a microservices platform.
192. Design a highly available Spring Boot microservices platform on AKS.
193. Design a multi-region Azure architecture for a customer-facing e-commerce application.
194. Design an event-driven order-processing platform using AKS, Service Bus or Event Hubs, Cosmos DB, and Redis.
195. Design a secure API platform using Azure Front Door, Application Gateway, AKS, and Microsoft Entra ID.
196. Design a hybrid-cloud architecture connecting on-premises systems with Azure microservices.
197. Design a multi-tenant SaaS platform on Azure with tenant isolation, RBAC, and cost allocation.
198. Design a real-time notification platform using AKS, SignalR or STOMP, Redis, Event Hubs, and Cosmos DB.
199. Design a data platform using Azure Data Lake, Event Hubs, Synapse or Fabric, and Power BI.
200. Design a disaster-recovery strategy for a critical Azure-based payment platform with defined RTO and RPO.
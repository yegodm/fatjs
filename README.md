# FATJS
Scratch-pad project for thoughts on a better approach for rapid application development 
with microservices.
The main goal is to end up with a framework/platform
designed for quick, strongly-typed integration between multiple 
application in the cluster. The following requirements should eventually be met:
1. Applications interact based on strongly-typed object schema. 
The schema is the only artifact required for interoperability.
1. Multi-platform paradigm - no matter with what language application is created, 
the architecture is based on the same building blocks;
1. Strong focus on entities and relationships echoing DDD ideas, basic CRUD is supported;
1. Minimum of glueware, every line of application should focus on the business logic. 
Turnaround time between extending the schema with new objects/attributes must be minimal,
ideally code-first so that new declarations can immediately be used in the application;
1. Entities can be projected, joined, and aggregated into other entities.
All of those are exposed within the same schema. 
1. Joining is possible between static, semi-static, and volatile objects. 
For instance, trading a financial instrument such as option requires storing 
three types of data:
   1. static - option identification information which evolves rarely if ever, 
   and most of the time is in the external storage, not in memory;
   1. semi-static - trading controls like enabling/disabling for trading, algorithm choice 
   for pricing/risk calculation, trading desk assigned etc - this evolves or may 
   evolve more often, must be kept in persistent storage and also stays in memory;
   1. volatile - results of pricing and derivative calculations - must be kept in memory, 
   is updated in soft real time, but rarely resides in the external storage as values 
   typically decay within seconds;
All three should be joined and presented in the trading UI as a single row.
1. Distributed partitioned storage. Every node may decide on 
what kind of persistence better suits its responsibilities. 
For example, the core workflows run on simple binary key-value DB, 
search nodes run on Elasticsearch cluster, reporting nodes use RDBMS;
1. Auditing and bi-temporal/historical querying;
1. Asynchronous nature of command execution enriched by 
fallback/circuit breaking/throttling features;
1. Distributed workflows (sagas);
1. Intrinsic soft real-time updates;

Somewhat later functionality will be extended to cater for:
1. Distributed computations;
1. Big data integration.

Language of choice - Kotlin. Some important features might not be there yet.



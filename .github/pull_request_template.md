## Summary

<!-- What does this PR do? 1-3 bullet points. -->

-

## Checklist

- [ ] Tests pass (`./mvnw test`)
- [ ] No new `ALLOW FILTERING` in Cassandra queries
- [ ] Schema migration needed? (if yes, update `schema.cql` **and** `schema-keyspaces.cql`, then run the schema-migrate workflow after merge)
- [ ] New environment variables added? (if yes, add to Secrets Manager and update ECS task definition environment in `terraform/modules/ecs/main.tf`)
- [ ] Cross-domain state changes go through domain events, not direct service calls
- [ ] No blocking I/O introduced (all I/O is reactive `Mono`/`Flux`)

## Test plan

<!-- How did you verify this works? -->

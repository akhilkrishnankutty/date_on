1. **Understand Issue**:
   The workflow failed again in the same exact way. Let's trace it:
   ```
   [ERROR]   DateonApplicationTests.contextLoads » IllegalState Failed to load ApplicationContext ...
   Caused by: org.postgresql.util.PSQLException: Connection to localhost:5432 refused.
   ```
   So the test still fails to connect to PostgreSQL.
   Why is the connection to `localhost:5432` refused?
   Because `services` mapped the port but maybe the health check STILL fails?
   Look closely at the `dummy.yml`:
   ```yaml
      db:
        image: postgres:15
        env:
          POSTGRES_DB: Dateon
          POSTGRES_USER: postgres
          POSTGRES_PASSWORD: 4567
        ports:
          - 5432:5432
        options: >-
          --health-cmd "pg_isready -U postgres"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
   ```
   Wait, if `--health-cmd` uses `"pg_isready -U postgres"`, maybe the options parsing in GitHub Actions adds extra quotes and causes the command to fail? Or does it need to be an array?
   Actually, `pg_isready` uses `POSTGRES_USER` environment variable automatically!
   So `--health-cmd pg_isready` should have worked if `POSTGRES_USER` is exported, but wait, `env` defined in `services.db.env` is available to the container. So `pg_isready` will know the user is `postgres`.
   Wait... I previously had:
   ```yaml
        options: >-
          --health-cmd pg_isready
   ```
   and it failed. Then I added `-U postgres`. Did that fix the health check?
   If the health check passes, why does `localhost:5432` refuse connection?
   Is the container removed before the tests start?
   No, it says "Stop and remove container: [kafka]" but it DOESN'T say "Stop and remove container: [postgres]"!
   Wait, where are the logs for the DB container? If the health check fails, the job fails to start completely, before the steps. But the `steps` DID run!
   So the health check DID PASS!

   Then why did `localhost:5432` refuse connection during `./mvnw clean test`?
   Let's check `docker ps` or something. Wait, in `dummy.yml`:
   ```yaml
    - name: Wait for services
      run: |
        sleep 15
        timeout 60 bash -c 'until echo > /dev/tcp/localhost/9092; do sleep 1; done'
        timeout 60 bash -c 'until echo > /dev/tcp/localhost/5432; do sleep 1; done'
   ```
   Wait, `echo > /dev/tcp/localhost/5432` SUCCEEDED! If it didn't, the step would fail and `mvnw test` would not be executed!
   The step `Wait for services` succeeded because `mvnw clean test` started executing.
   So `localhost:5432` WAS LISTENING before tests ran!
   Then why did `DateonApplicationTests.contextLoads` fail with `Connection refused` to `localhost:5432`?
   Ah! Is it possible that the test uses a different port?
   `application.properties`:
   `spring.datasource.url =jdbc:postgresql://localhost:5432/Dateon`
   Wait... in GitHub Actions, when `services:` are used, they are accessed via `localhost:5432`.
   BUT if the tests use Testcontainers, or if they spin up their own things? Let's check `DateonApplicationTests.java`.
   No, it's just a normal Spring Boot test.

   Wait, why would `Connection refused` happen if it was just listening?
   Could Postgres have crashed?
   "FATAL: role 'postgres' does not exist"? No, we created it.
   Maybe `POSTGRES_PASSWORD` needs to be passed to the connection? `application.properties` has `spring.datasource.password=4567`.
   Wait!
   ```
   Caused by: org.postgresql.util.PSQLException: Connection to localhost:5432 refused.
   Caused by: java.net.ConnectException: Connection refused
   ```
   This strictly means nothing is listening on `localhost:5432`.
   How can it not be listening if `Wait for services` succeeded?
   Wait! In GHA, if you use `ports: - 5432:5432`, the port is mapped on the host. But maybe `localhost` resolves to IPv6 `::1` in the Java process, and Docker is only listening on IPv4 `127.0.0.1`?
   Yes! Java 17 prefers IPv6, and `localhost` might resolve to `::1`, but Docker's port mapping might only be bound to `0.0.0.0` (IPv4) or `127.0.0.1`. If Java tries `::1:5432`, it gets `Connection refused`!

   Let's check `application.properties`:
   `spring.datasource.url =jdbc:postgresql://localhost:5432/Dateon`
   If I change this to `127.0.0.1:5432`, it will force IPv4.
   Is there a way to do this without changing `application.properties`?
   I can pass it as an environment variable in the workflow!
   Spring Boot will automatically override `spring.datasource.url` if we provide `SPRING_DATASOURCE_URL` environment variable.

   ```yaml
    - name: Run Tests
      env:
        SPRING_DATASOURCE_URL: jdbc:postgresql://127.0.0.1:5432/Dateon
        SPRING_KAFKA_BOOTSTRAP_SERVERS: 127.0.0.1:9092
      run: ./mvnw clean test
   ```

2. **Implement Fix**:
   Add environment variables to the `Run Tests` step to use `127.0.0.1` instead of `localhost`.

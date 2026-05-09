1. **Understand Issue**:
   The `docker compose up` command is failing locally in the sandbox because of docker overlayfs rootless permissions issues (`failed to convert whiteout file "tmp/.wh.ks-script-bmi7rpc5": operation not permitted`).
   This confirms that testing with Docker in the sandbox environment is not viable for certain base images.

   However, my task is to fix the GitHub Actions workflow.
   In GitHub Actions, the `docker compose up` command should work without this issue (because runners run full Docker), BUT using `services:` is the standard and recommended way for GitHub Actions.
   Why did `services:` fail originally?
   ```
   pg_isready
   ```
   Without `-U postgres`, `pg_isready` uses the runner user and might fail authentication.
   So changing `options:` for Postgres to:
   ```yaml
        options: >-
          --health-cmd "pg_isready -U postgres -d Dateon"
   ```
   This will correctly wait for the container. Wait, `-d Dateon` ensures the database is created!

   Wait! Look at the GitHub Actions logs from the very first failure (when I used `services:`). I didn't use `services:` in the first commit either!
   Wait, before my changes, there was no `dummy.yml` or `maven.yml`. There was only `.github/workflows/main.yml`.
   Wait, if there was NO workflow... wait! Let me check what the original `.github/workflows/dummy.yml` was using. It was just:
   ```yaml
    - name: Run Tests
      run: ./mvnw clean test
   ```
   It did not have `services:` or `docker compose`.
   This is why it failed with `Connection refused` because there was no database!

   In my first commit, I created `.github/workflows/dummy.yml` with `services:`. But it failed.
   Wait, let's look at the second failure log:
   ```
   [ERROR]   DateonApplicationTests.contextLoads » IllegalState Failed to load ApplicationContext for [WebMergedContextConfiguration...
   Caused by: java.net.ConnectException: Connection refused
   ```
   The connection was still refused!
   Why would connection be refused if `services:` was running?
   Maybe `localhost:5432` doesn't map to the `db` service container in GitHub Actions if we use `ports: - 5432:5432`?
   Actually, when you use `services` at the `job` level, they are run on the docker network.
   BUT, if we specify `ports: - 5432:5432`, GitHub Actions maps the port to the runner's `localhost`. So `localhost:5432` SHOULD work.
   Why didn't it?
   If the health check failed, the container is destroyed.
   Let's check if the `pg_isready` check failed.
   Yes, `--health-cmd pg_isready` without `-U postgres` will fail and the container gets killed by docker!
   So, we MUST use:
   `--health-cmd "pg_isready -U postgres"` or simply `pg_isready -U postgres`.

   Let's look at `.github/workflows/dummy.yml` that I just made:
   It uses `docker compose up -d`. This is fine, except in GHA `docker-compose.yml` has:
   `SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/Dateon` for the `app` container, but we are running `mvnw test` on the RUNNER, which connects to `localhost:5432`.
   And `docker-compose.yml` has:
   ```yaml
    ports:
      - "5432:5432"
   ```
   So `localhost:5432` will work!

   Wait! Look at the second GHA failure logs closely:
   ```
   2026-05-09T07:10:58.2708404Z Stop and remove container: 3318d1452f004a669d543dfbdb0ae239_confluentinccpkafka740_f592f1
   2026-05-09T07:10:58.2708748Z ##[command]/usr/bin/docker rm --force 4d30e54cbae6231af97d607a8c51cd06d83fec87aa810ee39eda7a0efd83ad0d
   ```
   This `Stop and remove container` log at the end of the job shows that `confluentinccpkafka740` container was running and was removed during cleanup!
   This means the `services:` DID run, but Postgres was NOT running! Or maybe Postgres was not listening on `localhost:5432`.
   Why wasn't Postgres running? Probably because the health check failed!

   To fix it, we should revert to using `services:` but fix the health check:
   ```yaml
        options: >-
          --health-cmd "pg_isready -U postgres"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
   ```

2. **Implement Fix**:
   Let's modify the dummy.yml (or main.yml) to use the correct health check.

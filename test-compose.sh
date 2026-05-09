docker compose down || true
docker rm -f dateon-db dateon-zookeeper dateon-kafka || true
sed -i '/version: .3.8./d' docker-compose.yml || true
docker compose up -d db zookeeper kafka
echo "Waiting for PostgreSQL..."
until docker exec dateon-db pg_isready -U postgres; do sleep 2; done
echo "Waiting for Kafka..."
timeout 60 bash -c 'until echo > /dev/tcp/localhost/9092; do sleep 1; done'
./mvnw clean test

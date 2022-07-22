curl -X POST -H "Content-Type: application/vnd.kafka.json.v2+json" \
 --data '{"records":[{"value":{"name": "testUser", "priority": 1}}]}' \
 "http://localhost:8081/topics/task"
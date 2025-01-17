#!/bin/bash

# Remove todos.db if it exists
[ -f "todos.db" ] && rm todos.db

# Set the base URL based on the environment
case "$ENV" in
  dev)  BASE_URL="http://localhost:8080" ;;
  pre)  BASE_URL="http://pre.example.com" ;;
  pro)  BASE_URL="http://pro.example.com" ;;
  *)    echo "Invalid ENV value. Must be 'dev', 'pre', or 'pro'." && exit 1 ;;
esac

echo "Using BASE_URL: $BASE_URL"

# Helper functions
check_status() { [ "$1" -eq "$2" ] || { echo "Expected status $2 but got $1"; exit 1; } }
check_json() { [ "$1" = "$2" ] || { echo "Expected JSON $2 but got $1"; exit 1; } }

echo "Create or Update Todo with ID 1"
response_code=$(curl -s -X PUT "$BASE_URL/todos/" \
     -H "Content-Type: application/json" \
     -d '{"id": 1, "title": "First Todo", "completed": false}' \
     -w '%{http_code}' \
     -o /dev/null)
check_status "$response_code" 204

echo "Create or Update Todo with ID 2"
response=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "$BASE_URL/todos/" \
     -H "Content-Type: application/json" \
     -d '{"id": 2, "title": "Second Todo", "completed": false}')
check_status $response 204

echo "Get All Todos"
response=$(curl -s -w "%{http_code}" -o todos.json -X GET "$BASE_URL/todos")
check_status "${response: -3}" 200
check_json "$(jq length todos.json)" 2

echo "Delete Todo with ID 1"
response=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE_URL/todos/1")
check_status $response 204

echo "Get All Todos after deletion"
response=$(curl -s -w "%{http_code}" -o todos.json -X GET "$BASE_URL/todos")
check_status "${response: -3}" 200
check_json "$(jq length todos.json)" 1

echo "Get Todo with ID 2"
response=$(curl -s -w "%{http_code}" -o todo.json -X GET "$BASE_URL/todos/2")
check_status "${response: -3}" 200
check_json "$(jq -r '.title' todo.json)" "Second Todo"

echo "Update Todo with ID 2 to completed true"
response=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "$BASE_URL/todos/" \
     -H "Content-Type: application/json" \
     -d '{"id": 2, "title": "Second Todo", "completed": true}')
check_status $response 204

echo "Get Todo with ID 2 again"
response=$(curl -s -w "%{http_code}" -o todo.json -X GET "$BASE_URL/todos/2")
check_status "${response: -3}" 200
check_json "$(jq -r '.completed' todo.json)" "true"

echo "Patch Todo with ID 2 to set title to 'patched'"
response=$(curl -s -o /dev/null -w "%{http_code}" -X PATCH "$BASE_URL/todos" \
     -H "Content-Type: application/json" \
     -d '{"id": 2,"title": "patched"}')
check_status $response 204

echo "Get Todo with ID 2 to verify title is a 'patched' string"
response=$(curl -s -w "%{http_code}" -o todo.json -X GET "$BASE_URL/todos/2")
check_status "${response: -3}" 200
check_json "$(jq -r '.title' todo.json)" "patched"

echo "All tests passed!"
rm todos.json todo.json
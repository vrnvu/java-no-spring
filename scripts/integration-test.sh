#!/bin/bash

# Remove todos.db if it exists
if [ -f "todos.db" ]; then
    rm todos.db
fi

# Set the base URL based on the environment
case "$ENV" in
  dev)
    BASE_URL="http://localhost:8080"
    ;;
  pre)
    BASE_URL="http://pre.example.com"
    ;;
  pro)
    BASE_URL="http://pro.example.com"
    ;;
  *)
    echo "Invalid ENV value. Please set ENV to 'dev', 'pre', or 'pro'."
    exit 1
    ;;
esac

echo "Using BASE_URL: $BASE_URL"

# Function to check HTTP status code
check_status() {
  if [ "$1" -ne "$2" ]; then
    echo "Expected status $2 but got $1"
    exit 1
  fi
}

# Function to check JSON response
check_json() {
  if [ "$1" != "$2" ]; then
    echo "Expected JSON $2 but got $1"
    exit 1
  fi
}

# Create or Update Todo with ID 1
response=$(curl -s -o /dev/null -w "%{http_code}" -X PUT $BASE_URL/todos/ \
     -H "Content-Type: application/json" \
     -d '{"id": 1, "title": "First Todo", "completed": false}')
check_status $response 204

# Create or Update Todo with ID 2
response=$(curl -s -o /dev/null -w "%{http_code}" -X PUT $BASE_URL/todos/ \
     -H "Content-Type: application/json" \
     -d '{"id": 2, "title": "Second Todo", "completed": false}')
check_status $response 204

# Get All Todos
response=$(curl -s -w "%{http_code}" -o todos.json -X GET $BASE_URL/todos)
check_status ${response: -3} 200
todos=$(jq length todos.json)
check_json $todos 2

# Delete Todo with ID 1
response=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE $BASE_URL/todos/1)
check_status $response 204

# Get All Todos after deletion
response=$(curl -s -w "%{http_code}" -o todos.json -X GET $BASE_URL/todos)
check_status ${response: -3} 200
todos=$(jq length todos.json)
check_json $todos 1

# Get Todo with ID 2
response=$(curl -s -w "%{http_code}" -o todo.json -X GET $BASE_URL/todos/2)
check_status ${response: -3} 200
title=$(jq -r '.title' todo.json)
check_json "$title" "Second Todo"

# Update Todo with ID 2 to completed true
response=$(curl -s -o /dev/null -w "%{http_code}" -X PUT $BASE_URL/todos/ \
     -H "Content-Type: application/json" \
     -d '{"id": 2, "title": "Second Todo", "completed": true}')
check_status $response 204

# Get Todo with ID 2 again
response=$(curl -s -w "%{http_code}" -o todo.json -X GET $BASE_URL/todos/2)
check_status ${response: -3} 200
completed=$(jq -r '.completed' todo.json)
check_json "$completed" "true"

# Patch Todo with ID 2 to set title to "patched"
response=$(curl -s -o /dev/null -w "%{http_code}" -X PATCH $BASE_URL/todos \
     -H "Content-Type: application/json" \
     -d '{"id": 2,"title": "patched"}')
check_status $response 204

# Get Todo with ID 2 to verify title is a "patched" string
response=$(curl -s -w "%{http_code}" -o todo.json -X GET $BASE_URL/todos/2)
check_status ${response: -3} 200
title=$(jq -r '.title' todo.json)
check_json "$title" "patched"

echo "All tests passed!"
rm todos.json todo.json
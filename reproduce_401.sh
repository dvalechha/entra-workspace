#!/bin/bash
# Test script to reproduce 401 error on proxy endpoint

# 1. Create a session
echo "Creating session..."
SESSION_RESPONSE=$(curl -s -c cookies.txt -i -X POST http://localhost:3001/v1/auth/session/create)
echo "$SESSION_RESPONSE"

# 2. Simulate login callback (this part is tricky without a real browser flow, 
# but we can try to hit the proxy endpoint directly if we had a valid token.
# Since we don't have a valid Entra ID token easily available in this script, 
# we will rely on the server logs to see if the session is even being established).

# 3. Try to access the proxy endpoint
echo "Accessing proxy endpoint..."
curl -v -b cookies.txt http://localhost:3001/v1/proxy/data/metrics

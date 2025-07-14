#!/usr/bin/env bash

# Exit on error
set -e

# Go to script directory
cd "$(dirname "$0")"

# Create venv if it doesn't exist
if [ ! -d ".venv" ]; then
  echo "Creating virtual environment..."
  python3 -m venv .venv
fi

# Activate venv
source .venv/bin/activate

# Install dependencies if needed
if [ -f "requirements.txt" ]; then
  pip install -r requirements.txt
fi

# Run the application
python main.py
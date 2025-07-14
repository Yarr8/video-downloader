@echo off
cd /d %~dp0

:: Create venv if it doesn't exist
if not exist ".venv" (
  echo Creating virtual environment...
  python -m venv .venv
)

:: Activate venv
call .venv\Scripts\activate.bat

:: Install dependencies
if exist requirements.txt (
	python -m pip install -r requirements.txt
)

:: Run the application
python main.py

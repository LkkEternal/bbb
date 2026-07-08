"""Check git log from all branches"""
import subprocess, os

os.chdir('E:/学习资料/make/navel-orange-monitoring')

# Fetch all
subprocess.run(['git', 'fetch', '--all'], capture_output=True)

# Get all branches and their latest commits
result = subprocess.run(['git', 'branch', '-a'], capture_output=True, text=True)
print("=== All branches ===")
print(result.stdout)

# Get recent commits
result = subprocess.run(['git', 'log', '--all', '--oneline', '--graph', '-40'], capture_output=True, text=True)
print("\n=== Recent commits ===")
print(result.stdout)

# Check what's new from origin
result = subprocess.run(['git', 'log', '--oneline', 'master..origin/master'], capture_output=True, text=True)
print("\n=== New on origin/master ===")
print(result.stdout if result.stdout else "(none)")

for branch in ['origin/app', 'origin/miniapp', 'origin/auth-module']:
    result = subprocess.run(['git', 'log', '--oneline', 'master..' + branch], capture_output=True, text=True)
    if result.stdout.strip():
        print(f"\n=== New on {branch} ===")
        print(result.stdout)
